package com.soundcloud.challenge.streamer;

enum MessageType {
    USER(0) {
        @Override
        public void process(UserSystenLogResolver r, String text) {
            r.onUserMessage(text);
        }
    },
    SYSTEM(1) {
        @Override
        public void process(UserSystenLogResolver r, String text) {
            r.onSystemMessage(text);
        }
    },
    LOG(2) {
        @Override
        public void process(UserSystenLogResolver r, String text) {
            r.onLogMessage(text);
        }
    };

    protected int id;

    MessageType(int id) {
        this.id = id;
    }

    public static MessageType fromId(int id) {
        for (MessageType mt : MessageType.values()) {
            if (mt.id == id) {
                return mt;
            }
        }
        return null;
    }

    public static void process(UserSystenLogResolver r, int id, String text) {
        fromId(id).process(r, text);
    }

    public abstract void process(UserSystenLogResolver r, String text);
}
