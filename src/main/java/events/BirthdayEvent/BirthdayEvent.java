package events.BirthdayEvent;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import events.BotEvent;
import exceptions.IllegalDateException;
import exceptions.InvalidDateFormatException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Represents a handler for !bday commands
 */
public class BirthdayEvent implements BotEvent{

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
    private void lookupBDay(MessageReceivedEvent event, String name) {
        event.getChannel().sendMessage(name).queue();
        Pattern p = Pattern.compile("\\d{18}");
        Matcher matcher = p.matcher(name);
        String id;
        if (matcher.find()) {
            id = matcher.group(0);
        } else {
            List<Member> membersWithName = event.getGuild().getMembersByEffectiveName(name, true);
            if (membersWithName.isEmpty()) {
                event.getChannel().sendMessage("I wasn't able to find any members with given name.").queue();
                return;
            } else if (membersWithName.size() > 1) {
                event.getChannel().sendMessage("I found multiple members with given name, please be more specific.").queue();
                return;
            }
            id = membersWithName.get(0).getId();
        }

        LocalDate date = BirthdayRecord.getDateById(id);
        if (date != null) {
            event.getChannel().sendMessage("I couldn't find any birthday records for " + name + ".").queue();
        } else {
            event.getChannel().sendMessage("The birthday of " + name + " is " + BirthdayRecord.getDateById(id) + ".").queue();
        }
    }

    /**
     * Sets the bday of the member that sent the message according to the message if
     * given date format is correct and date is valid. Sends error message to the
     * channel otherwise
     * 
     * @param event message event
     * @param date  string representation of a date
     * @param id    name of the member
     */
    private void setBDay(MessageReceivedEvent event, String date) {
        try {
            LocalDate bdayDate = getDateFromStr(date);
            String userId = event.getAuthor().getId();
            BirthdayRecord.recordBDay(userId, bdayDate);
            event.getChannel()
            .sendMessage(event.getGuild().getMemberById(userId).getEffectiveName()+ "'s birthday is set to " + bdayDate.toString())
            .queue();
        } catch (InvalidDateFormatException e) {
            event.getChannel().sendMessage("Sorry, birthday format illegal. Not recorded.").queue();
        } catch (IllegalDateException e) {
            event.getChannel().sendMessage("O.o Wha").queue();
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
     * @param id id of the member whose birthday is being looked up
     * @return a String representation of a date
     */
    public String getMemberBday(String id) {
        return BirthdayRecord.getDateById(id).toString();
    }


        /**
     * Invoke the event: analyzes the given message to set birthday or lookup a member's birthday
     * @param event message event that started with message "!bday"
     * @param content content of the message
     */
    @Override
    public void invoke(MessageReceivedEvent event, String[] content) {
        if (content.length == 0) {
            return;
        }
        if (content.length > 1 && content[0].equalsIgnoreCase("set")) {
            setBDay(event, content[1]);
        } else if (content[0].equalsIgnoreCase("check")) {
            if (content.length > 1) {
                lookupBDay(event, content[1]);
            } else {
                lookupBDay(event, event.getMember().getEffectiveName());
            }

        }
    }
}
