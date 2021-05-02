package events;

import java.io.FileNotFoundException;
import java.time.Instant;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONObject;
import persistence.JSONReader;
import persistence.JSONWriter;

public class StudyTimeEvent extends ListenerAdapter {
    private final Map<String, Instant> membersInVC = new HashMap<>();

    TextChannel textChannel;
    Instant finish;
    String memberID;

    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        if (event.getChannelJoined().getName().equalsIgnoreCase("silent study")) {
            Member m = event.getMember();
            Instant start = Instant.now();
            textChannel = event.getGuild().getTextChannelsByName("study-records", true).get(0);
            membersInVC.put(m.getId(), start);
            textChannel.sendMessage(m.getEffectiveName() + " has started studying!").queue();
        }
    }

    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (event.getChannelJoined().getName().equalsIgnoreCase("silent study")) {
            finish = Instant.now();
            memberID = event.getMember().getId();
            Instant start = membersInVC.get(memberID);
            long timeElapsed = Duration.between(start, finish).toMillis();

            sendTimeElapsedMessage(timeElapsed);
            storeElapsedTime(memberID, timeElapsed);
        }
    }

    private void storeElapsedTime(String memberID, long timeElapsed) {
        JSONReader reader = new JSONReader("./times.json");
        JSONWriter writer = new JSONWriter("./times.json");
        JSONObject jobj = reader.getStoredTimes();

        long timeAcc = timeElapsed / 1000 / 60;
        if (jobj.has(memberID)) {
            timeAcc += jobj.getLong(memberID);
            jobj.remove(memberID);
        }
        jobj.put(memberID, timeAcc);
        try {
            writer.saveString(jobj.toString(4));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendTimeElapsedMessage(long timeElapsed) {
        if(timeElapsed /1000 > 3600)
            textChannel.sendMessage("<@" + memberID + ">" +" has studied for **" + timeElapsed /1000/60/60 + "** hours" +
                    "and " + timeElapsed /1000/60 % 3600 + "minutes!").queue();
        else if(timeElapsed /1000 > 60)
            textChannel.sendMessage("<@" + memberID + ">" +" has studied for **" + timeElapsed /1000/60 + "** minutes!").queue();
        else
            textChannel.sendMessage("<@" + memberID + ">" +" has studied for **" + timeElapsed /1000 + "** seconds!").queue();
    }
}
