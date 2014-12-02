package com.soundcloud.challenge.streamer;

public interface UserSystenLogResolver {
    void onUserMessage(String text);
    void onSystemMessage(String text);
    void onLogMessage(String text);
}
