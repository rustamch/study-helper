package model;

import events.BirthdayEvent.BirthdayEvent;
import events.SimpleEvents.AboutEvent;
import events.SimpleEvents.DoraListener;
import events.StudyTimeEvent.StudyTimeEvent;
import events.TodoEvent.TodoEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;


public class Bot {

    public static JDA jda;

    public static void main(String[] args) throws LoginException {
        args = new String[1];
        args[0] = System.getenv("discord_token");
        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_EMOJIS, // for Guild#getEmotes (not very useful)
                GatewayIntent.GUILD_VOICE_STATES, // for member voice states
                GatewayIntent.GUILD_MESSAGES // for message received event
        );
        jda = JDABuilder.createDefault(args[0],intents)
                .addEventListeners(new AboutEvent())
                .addEventListeners(new BirthdayEvent())
                .addEventListeners(new TodoEvent())
                .addEventListeners(new ReminderFeature())
                .addEventListeners(new StudyTimeEvent())
                .addEventListeners(new DoraListener())
                .setActivity(Activity.playing("On the watch!"))
                .build();
    }
}

