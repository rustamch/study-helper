package model;
import events.BirthdayEvent.BirthdayEvent;
import events.ReminderEvent.ReminderFeature;
import events.SimpleEvents.AboutEvent;
import events.SimpleEvents.DoraListener;
import events.StudyTimeEvent.StudyTimeEvent;
import events.TodoEvent.TodoEvent;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import javax.security.auth.login.LoginException;
import java.util.EnumSet;


public class Bot {
    public static void main(String[] args) throws LoginException {
        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_EMOJIS, 
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MESSAGES
        );
        JDABuilder.createDefault(System.getenv("discord_token"),intents)
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

