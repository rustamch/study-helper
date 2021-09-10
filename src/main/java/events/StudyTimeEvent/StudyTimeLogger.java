package events.StudyTimeEvent;

import events.ServerConfig;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberJoinEvent;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberLeaveEvent;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberJoinListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberLeaveListener;

public class StudyTimeLogger implements ServerVoiceChannelMemberJoinListener, ServerVoiceChannelMemberLeaveListener {
    public static final String STUDY_CHANNEL = ".*study.*";


    @Override
    public void onServerVoiceChannelMemberLeave(ServerVoiceChannelMemberLeaveEvent event) {
        if (event.getChannel().getName().matches(STUDY_CHANNEL)) {
            User user = event.getUser();
            TextChannel textChannel = ServerConfig.getRecordsChannelForServer(event.getServer())
                    .orElse(event.getServer().getSystemChannel().orElseThrow());
            StudyTimeRecord record = StudyTimeRecord.getStudySession(user.getIdAsString());
            try {
                long timeElapsed = record.finishSession();
                record.save();
                sendTimeElapsedMessage(textChannel, user.getDisplayName(event.getServer()), timeElapsed);
            } catch (IllegalStateException e) {
                textChannel.sendMessage("Something went wrong!");
            }
        }
    }

    @Override
    public void onServerVoiceChannelMemberJoin(ServerVoiceChannelMemberJoinEvent event) {
        if (event.getChannel().getName().matches(STUDY_CHANNEL)) {
            User user = event.getUser();
            ServerConfig.getRecordsChannelForServer(event.getServer()).ifPresent(recordsChannel -> {
                StudyTimeRecord record = StudyTimeRecord.getStudySession(event.getUser().getIdAsString());
                if (record.inProgress()) {
                    record.finishSession();
                }
                record.trackSession();
                recordsChannel.sendMessage(user.getDisplayName(event.getServer()) + " has started studying!");
            });
        }
    }

    /**
     * Sends a message that tells for how long the given user has studied.
     *
     * @param timeElapsed amount of time in miliseconds.
     */
    private void sendTimeElapsedMessage(TextChannel records, String name, long timeElapsed) {
        String msg;
        if (timeElapsed / 1000 > 3600)
            msg = name + " has studied for **" + timeElapsed / 60 / 60 + "** hours" + " and " + timeElapsed / 60 % 60
                    + " minutes!";
        else if (timeElapsed > 60)
            msg = name + " has studied for **" + timeElapsed / 60 + "** minutes!";
        else
            msg = name + " has studied for **" + timeElapsed + "** seconds!";
        records.sendMessage(msg);
    }

}
