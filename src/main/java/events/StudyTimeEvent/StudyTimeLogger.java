package events.StudyTimeEvent;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberJoinEvent;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberLeaveEvent;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberJoinListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberLeaveListener;

import exceptions.InvalidDocumentException;

public class StudyTimeLogger implements ServerVoiceChannelMemberJoinListener, ServerVoiceChannelMemberLeaveListener {
    public static final String STUDY_CHANNEL = ".*study.*";


    @Override
    public void onServerVoiceChannelMemberLeave(ServerVoiceChannelMemberLeaveEvent event) {
        if (event.getChannel().getName().equalsIgnoreCase(STUDY_CHANNEL)) {
            User user = event.getUser();
            TextChannel textChannel = event.getServer().getTextChannelsByName("study-records").get(0);
            StudyTimeRecord record;
            try {
                record = StudyTimeRecord.getStudySession(user.getIdAsString());
                long timeElapsed = record.finishSession();
                sendTimeElapsedMessage(textChannel, user.getDisplayName(event.getServer()), timeElapsed);
            } catch (InvalidDocumentException e) {
                textChannel.sendMessage("Something went wrong!");
            }
        }
    }

    @Override
    public void onServerVoiceChannelMemberJoin(ServerVoiceChannelMemberJoinEvent event) {
        if (event.getChannel().getName().matches(STUDY_CHANNEL)) {
            User user = event.getUser();
            TextChannel textChannel = event.getServer().getTextChannelsByName("study-records").get(0);
            StudyTimeRecord record;
            try {
                record = StudyTimeRecord.getStudySession(event.getUser().getIdAsString());
            } catch (InvalidDocumentException e) {
                record = new StudyTimeRecord(user.getIdAsString());
            }
            record.trackSession();
            textChannel.sendMessage(user.getDisplayName(event.getServer()) + " has started studying!");
        }
    }

    /**
     * Sends a message that tells for how long the given user has studied.
     * 
     * @param memberID    id of the member who just finished their study session.
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
