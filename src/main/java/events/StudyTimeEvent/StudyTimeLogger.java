package events.StudyTimeEvent;

import events.ServerConfig;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
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
            StudyTimeRecord record = StudyTimeRecord.getStudySession(event.getUser().getIdAsString());
                long timeElapsed = record.finishSession();
                String displayName = event.getUser().getDisplayName(event.getServer());
                ServerConfig.getRecordsChannelForServer(event.getServer()).ifPresentOrElse(recordsChannel -> {
                    sendTimeElapsedMessage(recordsChannel, displayName, timeElapsed);
                    record.save();
                }, () -> {
                    Server server = event.getServer();
                    server.getOwner().ifPresent(owner ->
                            owner.sendMessage("Please setup a records channel on your server using " +
                                    "`!config study-records <textChannelId>`!"));
                });
        }
    }

    @Override
    public void onServerVoiceChannelMemberJoin(ServerVoiceChannelMemberJoinEvent event) {
        if (event.getChannel().getName().matches(STUDY_CHANNEL)) {
            User user = event.getUser();
            StudyTimeRecord record = StudyTimeRecord.getStudySession(event.getUser().getIdAsString());
            ServerConfig.getRecordsChannelForServer(event.getServer()).ifPresentOrElse(recordsChannel -> {
                String displayName = user.getDisplayName(event.getServer());
                if (record.inProgress()) {
                    long timeElapsed = record.finishSession();
                    sendTimeElapsedMessage(recordsChannel, displayName, timeElapsed);
                }
                record.trackSession();
                recordsChannel.sendMessage(displayName + " has started studying!");
            }, () -> {
                Server server = event.getServer();
                server.getOwner().ifPresent(owner ->
                        owner.sendMessage("Please setup a records channel on your server using " +
                                "`!config study-records <textChannelId>`!"));
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
