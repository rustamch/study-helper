import birthdays.BirthdayManager;
import exceptions.InvalidDateFormatException;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;


public class Bot extends ListenerAdapter {

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
                .addEventListeners(new Bot())
                .setActivity(Activity.playing("Type !ping"))
                .build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        MessageChannel channel = event.getChannel();

        String rawMsg = msg.getContentRaw();
        if (rawMsg.equals("!ping")) {
            long time = System.currentTimeMillis();
            channel.sendMessage("Pong!") /* => RestAction<Message> */
                    .queue(response /* => Message */ -> {
                        response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue();
                    });
        } else if (rawMsg.equalsIgnoreCase("!hi")) {
            channel.sendMessage("hi!!!!").queue();
        } else if (rawMsg.length() > 5 && rawMsg.substring(0, 5).equalsIgnoreCase("!bday")) {
            BirthdayManager bdayManager = new BirthdayManager();
            bdayManager.handleBdayActions(event);
        }
    }
}

