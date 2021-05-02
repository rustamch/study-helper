package events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class AboutEvent extends ListenerAdapter {
    public void onMessageReceived(MessageReceivedEvent event) {
        String messageSent = event.getMessage().getContentRaw();
        if(messageSent.equalsIgnoreCase("!about")) {
            EmbedBuilder about = new EmbedBuilder();
            about.setTitle("🌿 Study Hall Bot Information");
            about.setDescription("A bot to help YOU study better :D");
            about.addField("Creators", "A couple UBC 1st year students in *RUHacks 2021*!", false);
            about.addField("Birthday Commands", "!bday setbday year/month/date or year.month.date\n!bday lookup @<user>", false);
            about.setColor(0x9CD08F);

            event.getChannel().sendTyping().queue();
            event.getChannel().sendMessage(about.build()).queue();
            about.clear();
        }
    }
}