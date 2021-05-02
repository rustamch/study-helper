
import events.AboutEvent;
import events.DoraListener;
import events.PingEvent;
import events.birthdayEvent.BirthdayEvent;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;


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
        JDABuilder.createLight(args[0], GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES)
                .addEventListeners(new PingEvent())
                .addEventListeners(new AboutEvent())
                .addEventListeners(new BirthdayEvent())
                .addEventListeners(new ReminderFeature())
                .addEventListeners(new DoraListener())
                .setActivity(Activity.playing("Type !ping"))
                .build();
    }
}

