package events;

import events.ReactionEvent.ReactRoleMessage;
import org.javacord.api.event.message.MessageDeleteEvent;

public class MessageDeleteListener implements org.javacord.api.listener.message.MessageDeleteListener {

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        ReactRoleMessage.loadReactRoleMessage(event.getMessageId()).ifPresent(ReactRoleMessage::deleteRrMessage);
    }
}
