#Mobile jni Test

Some simple logic in the native layer of an Android application that's sending notifications
to the application layer. There are three types of events that can be generated :

- User Generated Events : A message sent from a specific user
- System Generated Events : An notification representing a system event
- Logging Events : An event representing a piece of information to log

The engine which generates one of these events (randomly chosen) and sends it to the application to display on a user interface. 

The application should stop sending events when put into the background and then restart sending events when the it comes back into the forground.

#Solution

Every time the main activity is resumed, it spawns a native thread.
The native thread, `event_source` runs with a global reference to the `Activity` object and:

0. attaches to the Java environment
1. generates a random number 0 = user, 1 = system, 2 = logging
2. if it's not done (activity was `onPause`d) it sends it to the `Activity` object
3. repeat with the number generation step

`OnPause` will call `native_stop` which will cause `event_source` to eventually exit, after up to 1.5 seconds.

There is a special case when the application is paused and resumed during `nanosleep`.
To avoid spawning too many threads, I simply keep track, with the help of global variables

    static volatile int running;
    static volatile int sources;

of what is happening at any time. `running` means the event source should be running, while `sources`
is 0 when no event sources are expected to generate an event in the future.
