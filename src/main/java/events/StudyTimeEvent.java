package events;

import java.time.Instant;
import java.time.Duration;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class StudyTimeEvent extends ListenerAdapter {

    TextChannel textChannel;
    Instant start;
    Instant finish;
    String memberID;
    String nickname;
    long timeStudied;

    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        start = Instant.now();
        textChannel = event.getGuild().getTextChannelsByName("general", true).get(0);
        nickname = event.getMember().getEffectiveName();
        textChannel.sendMessage(nickname + " has started studying!").queue();
    }

    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        memberID = event.getMember().getId();
        if(timeElapsed/1000 > 3600)
            textChannel.sendMessage("<@" + memberID + ">" +" has studied for **" + timeElapsed/1000/60/60 + "** hours" +
                    "and " + timeElapsed/1000/60 % 3600 + "minutes!").queue();
        else if(timeElapsed/1000 > 60)
            textChannel.sendMessage("<@" + memberID + ">" +" has studied for **" + timeElapsed/1000/60 + "** minutes!").queue();
        else
            textChannel.sendMessage("<@" + memberID + ">" +" has studied for **" + timeElapsed/1000 + "** seconds!").queue();
        timeStudied = timeElapsed;
    }
}
