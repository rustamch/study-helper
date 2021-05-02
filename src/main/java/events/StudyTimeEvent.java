package events;

import java.time.Instant;
import java.time.Duration;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class StudyTimeEvent extends ListenerAdapter {

    TextChannel textChannel;
    Instant start;
    Instant finish;

    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        start = Instant.now();

        textChannel = event.getGuild().getTextChannelsByName("general", true).get(0);
        textChannel.sendMessage("User has joined vc!").queue();
    }

    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        textChannel.sendMessage("User has left vc after " + timeElapsed/1000 + " seconds!").queue();
        textChannel.sendMessage("User has left vc!").queue();
    }
}
