package events.TodoEvent;

import exception.IllegalDateException;
import exceptions.MissingElementException;
import net.dv8tion.jda.api.entities.*;
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
            manager = new TodoManager(m);
            if (! (msgLst.length < 2)) {
                if (msgLst[1].equalsIgnoreCase("add")) {
                    addTodo(event, rawMsg, msgLst);
                } else if (msgLst[1].equalsIgnoreCase("check")) {
                    messageTodoList(event.getChannel());
                } else if (msgLst[1].equalsIgnoreCase("rm") && msgLst.length > 2) {
                    if (msgLst[2].equalsIgnoreCase("all")) {
                        manager.clearTodo();
                        event.getChannel().sendMessage("Your todo list is cleared!").queue();
                    } else {
                        removeTodo(event, msgLst);
                    }
                } else if (msgLst[1].equalsIgnoreCase("done") && msgLst.length > 2) {
                    if (msgLst[2].equalsIgnoreCase("all")) {
                        manager.clearTodo();
                        event.getChannel().sendMessage("List completed, hooray!").queue();
                    } else {
                        setComplete(event, msgLst);
                    }
                } else if (msgLst[1].equalsIgnoreCase("post")) {
                    TextChannel textChannel = event.getGuild()
                            .getTextChannelsByName("todos", true).get(0);
                    messageTodoList(textChannel);
                }
            }
        }
    }

    private void setComplete(MessageReceivedEvent event, String[] msgLst) {
        StringBuilder builder = new StringBuilder();
        int numCompleted = 0;
        try {
            for (int i = 2; i < msgLst.length; i++) {
                manager.setTodoAsComplete(Integer.parseInt(msgLst[i]) - 1 - numCompleted);
                builder.append(Integer.parseInt(msgLst[i])).append(" ");
                numCompleted++;
            }
        } catch (NumberFormatException e) {

        } finally {
            if (manager.listIsCleared()) {
                event.getChannel().sendMessage("List completed, hooray!").queue();
            } else {
                builder.append("completed!");
                event.getChannel().sendMessage(builder.toString()).queue();
            }
        }
    }

    private void messageTodoList(MessageChannel channel) {
        channel.sendMessage(manager.getTodoMessage()).queue();
    }

    private void addTodo(@NotNull MessageReceivedEvent event, String rawMsg, String[] msgLst) {
        try {
            if (msgLst.length == 3) {
                setTodayGoal(msgLst[2]);
            } else {
                addLongTodo(rawMsg);
            }
            messageTodoList(event.getChannel());
        } catch (MissingElementException | IllegalDateException e) {
            event.getChannel().sendMessage(e.getMessage()).queue();
        }
    }

    private void removeTodo(MessageReceivedEvent event, String[] msgLst) {
        StringBuilder builder = new StringBuilder();
        int numRemoved = 0;
        try {
            for (int i = 2; i < msgLst.length; i++) {
                manager.removeTodoByNumber(Integer.parseInt(msgLst[i]) - 1 - numRemoved);
                builder.append(Integer.parseInt(msgLst[i])).append(" ");
                numRemoved++;
            }
        } catch (NumberFormatException e) {

        } finally {
            builder.append("removed!");
            event.getChannel().sendMessage(builder.toString()).queue();
        }
    }

    private void addLongTodo(String rawMsg) throws MissingElementException, IllegalDateException {
        String course = findCourse(rawMsg);
        String description = findDescription(rawMsg);
        LocalDate due = findDue(rawMsg);
        manager.addTodo(course, description, due);
    }

    private void setTodayGoal(String rawMsg) throws MissingElementException, IllegalDateException {
        LocalDate due = LocalDate.now();
        String description = findDescription(rawMsg);
        manager.addTodo(null, description, due);
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
            return rawMsg.split("[\"“”]")[1];
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
