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
            about.addField("Creators", "A couple UBC 1st year students in *RUHacks 2021!*\n\n**__Commands List:__**", false);
            about.addField("___Birthday Commands___", "**Set birthday**: !bday setbday year/month/date\n" +
                    "**Look up birthday**: !bday lookup <user nickname>", false);
            about.addField("___Reminder Commands___", "**Set reminder date**: !reminder YYYY.MM.DD\n" +
                    "**Set reminder date & time**: !reminder YYYY.MM.DDTHH:MM\n" +
                    "**Check reminders**: !reminders", false);
            about.addField("___Todo Commands___", "**Set new todo**: !todo add <course name> <course #> \"todo description\" due <due date>\n" +
                    " ㄴ Course name & course # are optional. Set a todo for today by omitting due date " +
                    "**Remove a specific todo**: !todo rm <todo #>\n" +
                    "**Remove all todo**: !todo rm all\n" +
                    "**Check existing todo**: !todo check\n" +
                    "**Mark todo as finished**: !todo done <todo #>" +
                    "**Post todo to 'todos' channel**: !todo post\n", false);
            about.setColor(0x9CD08F);

            event.getChannel().sendTyping().queue();
            event.getChannel().sendMessage(about.build()).queue();
            about.clear();
        }
    }
}