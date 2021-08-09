package events.StudyTimeEvent;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import events.BotMessageEvent;
import exceptions.InvalidDocumentException;

/**
 * Represents a handler for !studytime commands
 */
public class StudyTimeEvent implements BotMessageEvent {
  public static final String STUDY_CHANNEL = "silent study";
  public static final int NUMBER_OF_USERS_ON_LEADERBOARD = 5;

  /**
   * Constructs and sends a message that tells user for how long has he studied
   * already.
   * 
   * @param event a JDA event
   */
  private void msgStudyTimeForUser(MessageCreateEvent event) {
    StudyTimeLeaderboard studyTimeLeaderboard = StudyTimeLeaderboard.loadTimeLeaderboard();
    long time = studyTimeLeaderboard.getUserTime(event.getMessageAuthor().getIdAsString()) / 60;
    if (time > 0) {
      event.getChannel()
          .sendMessage(event.getMessageAuthor().getDisplayName() + " has studied for "  + time / 60 + " hour(s) " + time % 60 + " minutes");
    } else {
      event.getChannel().sendMessage(event.getMessageAuthor().getDisplayName() + " has not studied yet.");
    }
  }

  @Override
  public void invoke(MessageCreateEvent event, String[] content) {
    String command = content[0];
    switch (command) {
      case "check":
        msgStudyTimeForUser(event);
        break;
      case "leaderboard":
        if (content[1].equals("reset")) {
          StudyTimeLeaderboard.loadTimeLeaderboard().resetLeaderboard();
        } else {
          StudyTimeLeaderboard studyTimeLeaderboard = StudyTimeLeaderboard.loadTimeLeaderboard();
          event.getServer().ifPresent(server -> {
            EmbedBuilder eb = studyTimeLeaderboard.getLeaderboardEmbed(server);
            event.getChannel().sendMessage(eb);
          });
        }
        break;
      case "sub":
        if (content.length > 1) {
          try {
            long time = Math.abs(Long.parseLong(content[1]));
            StudyTimeRecord.subtractStudyTime(event.getMessageAuthor().getIdAsString(), time * 60);
            event.getChannel().sendMessage("Successfully subtracted " + time + " minute(s)!");
          } catch (InvalidDocumentException e) {
            event.getChannel().sendMessage("You haven't studied yet >:(");
          }
        } else {
          event.getChannel().sendMessage("You need to specify how much you want to subtract!");
        }
        break;
    }
  }
}
