package events.studysession;

import events.BotMessageEvent;
import events.StudyTimeEvent.StudyTimeRecord;
import org.javacord.api.event.message.MessageCreateEvent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.TimerTask;

import static events.StudyTimeEvent.StudyTimeRecord.getStudySession;

public class StudySessionEvent extends TimerTask implements BotMessageEvent {

    public StudySessionEvent() {
        Timer timer = new Timer(60000, e -> {
            run();
        });
    }

    public void run() {
        StudyTimeRecord.getDueStudySessions().forEach(record -> {
            record.finishSession();
            record.save();
        });
    }

    @Override
    public void invoke(MessageCreateEvent event, String[] content) {
        getEndOfSession(content).ifPresentOrElse(endEpoch -> {
            String memberId = event.getMessageAuthor().getIdAsString();
            StudyTimeRecord record = StudyTimeRecord.getStudySession(memberId);
            if (record.inProgress()) {
                record.finishSession();
            }
            record.setEndTime(endEpoch);
            record.trackSession();
        }, () -> event.getChannel().sendMessage("The amount of time you specified isn't valid!"));
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
                if (content[1].matches("min|m")) {
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


}
