package events.BirthdayEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import events.BotMessageEvent;
import exceptions.IllegalDateException;
import exceptions.InvalidDateFormatException;

/**
 * Represents a handler for !bday commands
 */
public class BirthdayEvent implements BotMessageEvent {

    /**
     * Constructs a new BirthdayEvent and initializes BirthdayReminders.
     */
    public BirthdayEvent() {
        new BirthdayReminder();
    }

    /**
     * Finds the birthday of member mentioned in the message, prints to channel
     * birthday message if found, print "birthday not found" otherwise
     * 
     * @param event message event
     * @param name  name or atMention of the member whose birthday is being looked
     *              up
     */
    private void lookupBDay(MessageCreateEvent event, String name) {
        Server msgServer = event.getServer().orElseThrow(() -> new IllegalStateException("Server not found"));
        Pattern p = Pattern.compile("\\d{18}");
        Matcher matcher = p.matcher(name);
        User user;
        if (matcher.find()) {
            user = msgServer.getMemberById(matcher.group(0))
                    .orElse(event.getApi().getUserById(matcher.group(0)).join());
            event.getServer().ifPresent(actionServer -> {
                LocalDate bday = BirthdayRecord.getDateById(user.getIdAsString());
                if (bday == null) {
                    event.getChannel().sendMessage(
                            "The " + user.getDisplayName(actionServer) + " hasn't saved his birthday yet.");
                } else {
                    event.getChannel().sendMessage(
                            "The " + user.getDisplayName(actionServer) + "'s birthday is " + bday.toString());
                }
            });
        } else {
            List<User> usersWithName = new ArrayList<>(msgServer.getMembersByDisplayName(name));
            if (usersWithName.isEmpty()) {
                event.getChannel().sendMessage("There are no users with a given name on this server");
            } else if (usersWithName.size() == 1) {
                user = usersWithName.get(0);
                LocalDate bday = BirthdayRecord.getDateById(user.getIdAsString());
                if (bday == null) {
                    event.getChannel().sendMessage(name + " hasn't saved their birthday yet.");
                } else {
                    event.getChannel().sendMessage(name + "'s birthday is " + bday.toString());
                }
            } else {
                event.getChannel()
                        .sendMessage("There are multiple users with the name " + name + ". Please be more specific.");
            }
        }
    }

    /**
     * Sets the bday of the member that sent the message according to the message if
     * given date format is correct and date is valid. Sends error message to the
     * channel otherwise
     * 
     * @param event message event
     * @param date  string representation of a date
     */
    private void setBDay(MessageCreateEvent event, String date) {
        try {
            LocalDate bdayDate = getDateFromStr(date);
            BirthdayRecord.recordBDay(event.getMessageAuthor().getIdAsString(), bdayDate);
            event.getChannel().sendMessage(
                    event.getMessageAuthor().getDisplayName() + "'s birthday is set to " + bdayDate.toString());
        } catch (InvalidDateFormatException e) {
            event.getChannel().sendMessage("Sorry, birthday format illegal. Not recorded.");
        } catch (IllegalDateException e) {
            event.getChannel().sendMessage("O.o Wha");
        }
    }

    /**
     * Produce a date object specified by the given string
     * 
     * @param inpt string representation of a date
     * @return a Date object
     * @throws InvalidDateFormatException when given date format is unrecognized
     * @throws IllegalDateException       when given date has illegal year/month/day
     *                                    values
     */
    public static LocalDate getDateFromStr(String inpt) throws InvalidDateFormatException, IllegalDateException {
        String[] lst = inpt.split("[.\\/]");
        return LocalDate.of(Integer.parseInt(lst[0]), Integer.parseInt(lst[1]), Integer.parseInt(lst[2]));
    }

    /**
     * Returns the date of the birthday of the member specified by the given id
     * 
     * @param id id of the member whose birthday is being looked up
     * @return a String representation of a date
     */
    public String getMemberBday(String id) {
        return BirthdayRecord.getDateById(id).toString();
    }

    /**
     * Invoke the event: analyzes the given message to set birthday or lookup a
     * member's birthday
     * 
     * @param event   message event that started with message "!bday"
     * @param content content of the message
     */
    @Override
    public void invoke(MessageCreateEvent event, String[] content) {
        if (content.length == 0) {
            return;
        }
        if (content.length > 1 && content[0].equalsIgnoreCase("set")) {
            setBDay(event, content[1]);
        } else if (content[0].equalsIgnoreCase("check")) {
            if (content.length > 1) {
                lookupBDay(event, content[1]);
            } else {
                lookupBDay(event, event.getMessageAuthor().getDisplayName());
            }

        }
    }

}
