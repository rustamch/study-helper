package events;

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
import persistence.JSONReader;
import persistence.JSONWriter;

public class StudyTimeEvent extends ListenerAdapter {
    private Map<String, Instant> membersInVC = new HashMap<>();

    TextChannel textChannel;
    Instant finish;
    String memberID;
    long timeStudied;


    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        Member m = event.getMember();
        Instant start = Instant.now();
        textChannel = event.getGuild().getTextChannelsByName("general", true).get(0);
        membersInVC.put(m.getId(), start);
        textChannel.sendMessage(m.getEffectiveName() + " has started studying!").queue();
    }

    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        finish = Instant.now();
        memberID = event.getMember().getId();
        Instant start = membersInVC.get(memberID);
        long timeElapsed = Duration.between(start, finish).toMillis();

        sendTimeElapsedMessage(timeElapsed);
        storeElapsedTime(memberID, timeElapsed);
    }

    private void storeElapsedTime(String memberID, long timeElapsed) {
        JSONReader reader = new JSONReader("./times.json");
        JSONWriter writer = new JSONWriter("./times.json");


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
