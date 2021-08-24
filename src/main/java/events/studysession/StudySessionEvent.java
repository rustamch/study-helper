package events.studysession;

import events.BotMessageEvent;
import org.javacord.api.event.message.MessageCreateEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class StudySessionEvent implements BotMessageEvent {
    @Override
    public void invoke(MessageCreateEvent event, String[] content) {
        getEndOfSession(content).ifPresentOrElse(instant -> event.getMessageAuthor().getIdAsString(), () ->
                event.getChannel().sendMessage("The amount of time you specified isn't valid!"));
    }

    /**
     * Produce an instance that points to the time when the study session should end.
     *
     * @param content An array of strings that contains words from the user's message.
     * @return returns an Optional that could contain an Instance.
     */
    private Optional<Instant> getEndOfSession(String[] content) {
        if (content[0] != null && content[0].matches("\\d*")) {
            long numTemp = Long.parseLong(content[0]);
            if (content[1] != null) {
                if (content[1].matches("min|m")) {
                    return Optional.of(Instant.now().plus(numTemp, ChronoUnit.MINUTES));
                } else if (content[1].matches("hr|hours|hour")) {
                    return Optional.of(Instant.now().plus(numTemp, ChronoUnit.HOURS));
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
