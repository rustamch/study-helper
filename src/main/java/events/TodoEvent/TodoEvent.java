package events.TodoEvent;

import exceptions.MissingElementException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TodoEvent extends ListenerAdapter {
    private TodoManager manager;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String rawMsg = event.getMessage().getContentRaw();
        String[] msgLst = rawMsg.split(" ");
        if (msgLst[0].equalsIgnoreCase("!todo")) {
            User m = event.getAuthor();
            manager = new TodoManager(m.getName());
            if (! (msgLst.length < 2)) {
                if (msgLst[1].equalsIgnoreCase("add")) {
                    addTodo(event, m, rawMsg, msgLst);
                    messageTodoList(event);
                } else if (msgLst[1].equalsIgnoreCase("check")) {
                    messageTodoList(event);
                }
            }
        }
    }

    private void messageTodoList(MessageReceivedEvent event) {
        event.getChannel().sendMessage(manager.getTodoMessage()).queue();
    }

    private void addTodo(@NotNull MessageReceivedEvent event, User m, String rawMsg, String[] msgLst) {
        try {
            if (msgLst.length == 3) {
                setTodayGoal(msgLst[2]);
            } else {
                addLongTodo(rawMsg);
            }
        } catch (MissingElementException e) {
            event.getChannel().sendMessage(e.getMessage()).queue();
        }
    }

    private void addLongTodo(String rawMsg) throws MissingElementException {
        String course = findCourse(rawMsg);
        String description = findDescription(rawMsg);
        LocalDate due = findDue(rawMsg);
        manager.addTodo(course, description, due);
    }

    private void setTodayGoal(String rawMsg) throws MissingElementException {
        LocalDate due = LocalDate.now();
        String course = findCourse(rawMsg);
        String description = findDescription(rawMsg);
        manager.addTodo(course, description, due);
    }

    private LocalDate findDue(String rawMsg) throws MissingElementException {
        String[] lst = rawMsg.split("due ");
        if (lst.length > 1) {
            String[] date = lst[1].split("[\\/\\.-]");
            if (date.length != 2 && date.length != 3) {
                throw new MissingElementException("Date incomplete!");
            }
            if (date.length == 2) {
                int year = LocalDate.now().getYear();
                int month = Integer.parseInt(date[0]);
                int day = Integer.parseInt(date[1]);
                return LocalDate.of(year, month, day);
            } else {
                int year = Integer.parseInt(date[0]);
                int month = Integer.parseInt(date[1]);
                int day = Integer.parseInt(date[2]);
                return LocalDate.of(year, month, day);
            }
        }
        return LocalDate.now();
    }

    private String findDescription(String rawMsg) throws MissingElementException {
        try {
            return rawMsg.split("\"")[1];
        } catch (IndexOutOfBoundsException e) {
            throw new MissingElementException("Missing description!");
        }
    }

    private String findCourse(String rawMsg) {
        Pattern p = Pattern.compile("[QWERTYUIOPASDFGHJKLZXCVBNM]{4}\\s\\d\\d\\d");
        Matcher m = p.matcher(rawMsg);
        if (m.find()) {
            return m.group();
        }
        return null;
    }
}
