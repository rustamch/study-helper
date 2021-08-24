package model;

import events.MessageDeleteListener;
import events.ReactionEvent.MessageReactionListener;
import events.StudyTimeEvent.StudyTimeLogger;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.util.logging.FallbackLoggerConfiguration;


public class Bot {
    public static DiscordApi API;

    public static void main(String[] args) {
        FallbackLoggerConfiguration.setDebug(true);
        FallbackLoggerConfiguration.setTrace(true);
        API = new DiscordApiBuilder()
        .setToken(System.getenv("discord_token"))
        .setAllIntentsExcept(Intent.GUILD_PRESENCES, Intent.GUILD_WEBHOOKS)
        .login().join();
        API.addMessageCreateListener(new MessageListener());
        API.addListener(new StudyTimeLogger());
        API.addMessageDeleteListener(new MessageDeleteListener());
        API.addListener(new MessageReactionListener());
        new DailyExecutor();
    }
}

