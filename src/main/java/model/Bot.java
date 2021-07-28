package model;
import events.StudyTimeEvent.StudyTimeEvent;
import javax.security.auth.login.LoginException;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;


public class Bot {
    public static DiscordApi API;

    public static void main(String[] args) throws LoginException {
        API = new DiscordApiBuilder()
        .setToken(System.getenv("discord_token"))
        .login().join();
        API.addMessageCreateListener(new MessageListener());
        API.addListener(new StudyTimeEvent());
    }
}

