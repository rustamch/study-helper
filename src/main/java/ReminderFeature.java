import exception.DuplicateReminderException;
import model.ReminderManager;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReminderFeature extends ListenerAdapter {

    public ReminderFeature() {}

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        MessageChannel channel = event.getChannel();

        String messageStringRaw = message.getContentRaw();

        if (messageStringRaw.equals("!reminder")) {
            ReminderManager reminderManager = new ReminderManager();
            try {
                reminderManager.addReminder(event);
            } catch (DuplicateReminderException e) {
                // TODO add valid reminder message
                System.out.println("Please type in a valid reminder.");
            }
        }

    }
}
