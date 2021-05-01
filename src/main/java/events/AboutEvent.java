package events;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class AboutEvent extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent event){
        String messageSent = event.getMessage().getContentRaw();
        if(messageSent.equalsIgnoreCase("!about")){
            event.getChannel().sendMessage("Here's more information about this bot...").queue();
        }
    }
}
