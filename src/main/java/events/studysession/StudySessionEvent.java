package events.studysession;

import events.BotMessageEvent;
import events.ServerConfig;
import events.StudyTimeEvent.StudyTimeRecord;
import model.Bot;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

import javax.swing.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;


public class StudySessionEvent implements BotMessageEvent {

    public StudySessionEvent() {
        Timer timer = new Timer(60000, e -> run());
        timer.setRepeats(true);
        timer.start();
    }

    public void run() {
        StudyTimeRecord.getDueStudySessions().forEach(record -> {
            long timeElapsed = record.finishSession();
            Bot.API.getServersByName("Studium Praetorium").forEach(server ->
                    server.getMemberById(record.getMemberId()).ifPresent(user ->
                            ServerConfig.getRecordsChannelForServer(server).ifPresent(records ->
                                    sendTimeElapsedMessage(records, user.getDisplayName(server), timeElapsed))
                    ));
            record.save();
        });
    }

    @Override
    public void invoke(MessageCreateEvent event, String[] content) {
        if (content[0] != null) {
            if (content[0].matches("\\d.|\\d.\\w.")) {
                startStudySession(event, content);
            } else if (content[0].equals("leave")) {
                logOffUser(event);
            }
        }

    }

    private void startStudySession(MessageCreateEvent event, String[] content) {
        getEndOfSession(content).ifPresentOrElse(endEpoch -> event.getServer().ifPresent(server ->
                event.getMessageAuthor().asUser().ifPresent(user -> {
                    if (memberIsInStudyMode(user.getRoles(server), server)) {
                        String memberId = event.getMessageAuthor().getIdAsString();
                        StudyTimeRecord record = StudyTimeRecord.getStudySession(memberId);
                        ServerConfig.getRecordsChannelForServer(server).ifPresentOrElse(recordsChannel -> {
                            if (record.inProgress()) {
                                long timeElapsed = record.finishSession();
                                String displayName = event.getMessageAuthor().getDisplayName();
                                sendTimeElapsedMessage(recordsChannel, displayName, timeElapsed);
                            }
                            record.setEndTime(endEpoch);
                            record.trackSession();
                            recordsChannel.sendMessage(user.getDisplayName(server) +
                                    " has started studying!");
                        }, () -> server.getOwner().ifPresent(owner ->
                                owner.sendMessage("Please setup a records channel on your server " +
                                        "using `!config study-records <textChannelId>`!")));
                        event.getChannel().sendMessage(user.getMentionTag() +
                                ", you’ve started a study session. Be productive!");
                    } else {
                        event.getChannel().sendMessage("You need to be in study mode to start a study session!");
                    }
                })), () -> event.getChannel().sendMessage("The amount of time you specified isn't valid!"));
    }

    private void logOffUser(MessageCreateEvent event) {
        event.getMessageAuthor().asUser().ifPresent(user ->
                event.getServer().ifPresent(server -> {
                    StudyTimeRecord record = StudyTimeRecord.getStudySession(user.getIdAsString());
                    try {
                        long timeElapsed = record.finishSession();
                        record.save();
                        ServerConfig.getRecordsChannelForServer(server).ifPresent(textChannel ->
                            sendTimeElapsedMessage(textChannel,user.getDisplayName(server), timeElapsed));
                        event.getChannel().sendMessage(user.getMentionTag() + " you have just ended your " +
                                "study session and you have studied for " + timeElapsed / 60 + " minutes! Good job!");
                    } catch (IllegalStateException e) {
                        event.getChannel().sendMessage("You aren't in active study session right now!");
                    }
                }));
    }

    private boolean memberIsInStudyMode(List<Role> roles, Server server) {
        if (ServerConfig.getStudyRoleForServer(server).isPresent()) {
            Role studyRole = ServerConfig.getStudyRoleForServer(server).get();
            return roles.contains(studyRole);
        } else {
            return false;
        }
    }


    /**
     * Produce an instance that points to the time when the study session should end.
     *
     * @param content An array of strings that contains words from the user's message.
     * @return returns an Optional that could contain an Instance.
     */
    private Optional<Long> getEndOfSession(String[] content) {
        if (content[0] != null && content[0].matches("\\d*")) {
            long numTemp = Long.parseLong(content[0]);
            if (content[1] != null) {
                Instant now = Instant.now();
                if (content[1].matches("min|m|mins")) {
                    return Optional.of(now.plus(numTemp, ChronoUnit.MINUTES).getEpochSecond());
                } else if (content[1].matches("hr|hours|hour")) {
                    return Optional.of(now.plus(numTemp, ChronoUnit.HOURS).getEpochSecond());
                } else {
                    return Optional.empty();
                }
            }
        } else {
            return Optional.empty();
        }
        return Optional.empty();
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
