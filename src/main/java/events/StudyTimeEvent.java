package events;

import java.io.FileNotFoundException;
import java.time.Instant;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import persistence.JSONReader;
import persistence.JSONWriter;

public class StudyTimeEvent extends ListenerAdapter {
    public static final String STUDY_CHANNEL = "silent study";
    private static final String COLLECTION_NAME = "times";
    private final Map<String, Instant> membersInVC = new HashMap<>();
    private String docName;
    TextChannel textChannel;
    Instant finish;
    String memberID;

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        if (event.getChannelLeft().getName().equalsIgnoreCase(STUDY_CHANNEL)) {
            endAndRecord(event);
        } else if (event.getChannelJoined().getName().equalsIgnoreCase(STUDY_CHANNEL)) {
            trackStartTime(event);
        }
    }

    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        if (event.getChannelJoined().getName().equalsIgnoreCase(STUDY_CHANNEL)) {
            trackStartTime(event);
        }
    }

    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (event.getChannelLeft().getName().equalsIgnoreCase(STUDY_CHANNEL)) {
            endAndRecord(event);
        }
    }

    private void trackStartTime(GenericGuildVoiceEvent event) {
        Member m = event.getMember();
        Instant start = Instant.now();
        textChannel = event.getGuild().getTextChannelsByName("study-records", true).get(0);
        membersInVC.put(m.getId(), start);
        textChannel.sendMessage(m.getEffectiveName() + " has started studying!").queue();
    }


    private void endAndRecord(GenericGuildVoiceEvent event) {
        finish = Instant.now();
        memberID = event.getMember().getId();
        Instant start = membersInVC.get(memberID);
        long timeElapsed = Duration.between(start, finish).toMillis();

        sendTimeElapsedMessage(timeElapsed);
        storeElapsedTime(memberID, timeElapsed);
    }

    private void storeElapsedTime(String memberID, long timeElapsed) {
        docName = memberID;
        JSONReader reader = new JSONReader(COLLECTION_NAME,docName);
        JSONWriter writer = new JSONWriter(COLLECTION_NAME,docName);
        JSONObject jobj = reader.getStoredTimes();
        jobj.put("save_key","study_times");

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
                    " and " + timeElapsed /1000/60 % 60 + " minutes!").queue();
        else if(timeElapsed /1000 > 60)
            textChannel.sendMessage("<@" + memberID + ">" +" has studied for **" + timeElapsed /1000/60 + "** minutes!").queue();
        else
            textChannel.sendMessage("<@" + memberID + ">" +" has studied for **" + timeElapsed /1000 + "** seconds!").queue();
    }
}
