#include <android/log.h>
#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <pthread.h>



#ifdef __cplusplus
extern "C" {
#endif

static JavaVM *java_vm;
static jmethodID event_callback;

pthread_mutex_t run_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t source_mutex = PTHREAD_MUTEX_INITIALIZER;
static volatile int running;
static volatile int sources;

static JNIEnv *attach_env()
{
    JNIEnv* env;
    JavaVMAttachArgs args;
    args.version = JNI_VERSION_1_4;
    args.name = NULL;
    args.group = NULL;
    if ((*java_vm)->GetEnv(java_vm, (void**)&env, JNI_VERSION_1_4) == JNI_EDETACHED) {
        jint attachResponse = (*java_vm)->AttachCurrentThread(java_vm, &env, &args);
        if ((*java_vm)->GetEnv(java_vm, (void**)&env, JNI_VERSION_1_4) != JNI_OK) {
            return NULL;
        }
    }
    return env;
}

static void *event_source(void *data)
{
    JNIEnv *env = attach_env();
    if (env == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, "Streamer",
            "Failed to get JNIEnv for JNI 1.4+ inside native thread");
        return;
    }

    jobject thiz = (jobject)data;

    static const size_t BUF_LEN = 128;
    char buffer[BUF_LEN];
    struct timespec ts; // 1.5s
    ts.tv_sec = 1;
    ts.tv_nsec = 500000000;

    int event_number = 0;
    for (; ; ) {
        int type = rand() % 3;
        snprintf(buffer, BUF_LEN, "Event #%d of type %d.", event_number++, type);
        jstring msg = (*env)->NewStringUTF(env, buffer);

        pthread_mutex_lock(&run_mutex);
        if (running != 0) {
            (*env)->CallVoidMethod (env, thiz, event_callback, type, msg);
        } else {
            pthread_mutex_lock(&source_mutex);
            sources = 0;
            pthread_mutex_unlock(&source_mutex);

            pthread_mutex_unlock(&run_mutex);
            break;
        }
        pthread_mutex_unlock(&run_mutex);

        nanosleep(&ts, &ts);
    }

    (*env)->DeleteGlobalRef(env, thiz);

    (*java_vm)->DetachCurrentThread(java_vm);
    return NULL;
}

static void native_start(JNIEnv *env, jobject thiz)
{
    pthread_mutex_lock(&run_mutex);
    running = 1;
    pthread_mutex_unlock(&run_mutex);

    pthread_mutex_lock(&source_mutex);
    if (sources == 0) {
        // only spawn a new thread if there's no current worker or if the current worker
        // is on the brink of dying
        sources = 1;
        pthread_t thread;
        pthread_create(&thread, NULL, event_source, (*env)->NewGlobalRef(env, thiz));
    }
    pthread_mutex_unlock(&source_mutex);
}

static void native_stop(JNIEnv * env, jobject thiz) {
    pthread_mutex_lock(&run_mutex);
    running = 0;
    pthread_mutex_unlock(&run_mutex);
}

static jstring native_get_info(JNIEnv * env, jobject thiz) {
    return (*env)->NewStringUTF(env, "My Info");
}

/* List of implemented native methods */
static JNINativeMethod native_methods[] = {
  {"stopEvents", "()V", (void *) native_stop},
  {"startEvents", "()V", (void *) native_start},
  {"getInfo", "()Ljava/lang/String;", (void *) native_get_info},
};


/* Library initializer */
jint
JNI_OnLoad (JavaVM * vm, void *reserved)
{
  JNIEnv *env = NULL;

  java_vm = vm;
  if ((*vm)->GetEnv (vm, (void **) &env, JNI_VERSION_1_4) != JNI_OK) {
    __android_log_print (ANDROID_LOG_ERROR, "Streamer",
        "Failed to get JNIEnv for JNI 1.4+");
    return 0;
  }

  jclass klass =
      (*env)->FindClass (env, "com/soundcloud/challenge/streamer/Streamer");
  event_callback =
      (*env)->GetMethodID (env, klass, "onEvent", "(ILjava/lang/String;)V");

  (*env)->RegisterNatives (env, klass, native_methods, 3);

  srand(time(NULL));

  return JNI_VERSION_1_4;
}



#ifdef __cplusplus
}
#endif
