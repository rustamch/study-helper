
import events.AboutEvent;
import events.StudyTimeEvent;
import events.TodoEvent.TodoEvent;
import events.DoraListener;
import events.PingEvent;
import events.birthdayEvent.BirthdayEvent;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;


public class Bot {

    public static void main(String[] args) throws LoginException {
        args = new String[1];
        args[0] = "ODM3OTIyMTc5MjkwNzU5MjA4.YIzl1w.zxiD1l_7_dH_c3WFQYojw-g4dk4";
        if (args.length < 1) {
            System.out.println("You have to provide a token as first argumenta!");
            System.exit(1);
        }
        // args[0] should be the token
        // We only need 2 intents in this bot. We only respond to messages in guilds and private channels.
        // All other events will be disabled.
        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_EMOJIS, // for Guild#getEmotes (not very useful)
                GatewayIntent.GUILD_VOICE_STATES, // for member voice states
                GatewayIntent.GUILD_MESSAGES // for message received event
        );
        JDABuilder.createDefault(args[0], intents)
                .addEventListeners(new Bot())
                .addEventListeners(new AboutEvent())
                .addEventListeners(new BirthdayEvent())
                .addEventListeners(new TodoEvent())
                .addEventListeners(new ReminderFeature())
                .addEventListeners(new StudyTimeEvent())
                .addEventListeners(new DoraListener())
                .setActivity(Activity.playing("Type !ping"))
                .build();
    }
}

