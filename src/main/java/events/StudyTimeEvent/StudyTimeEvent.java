package events.StudyTimeEvent;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberJoinEvent;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberLeaveEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberJoinListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberLeaveListener;
import org.javacord.api.listener.message.MessageCreateListener;

import exceptions.InvalidDocumentException;

/**
 * Represents a handler for !studytime commands
 */
public class StudyTimeEvent
    implements ServerVoiceChannelMemberJoinListener, ServerVoiceChannelMemberLeaveListener, MessageCreateListener {
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
    Long time = studyTimeLeaderboard.getUserTime(event.getMessageAuthor().getIdAsString());
    if (time > 0) {
      event.getChannel()
          .sendMessage(event.getMessageAuthor().getDisplayName() + " has studied for " + time + " minutes");
    } else {
      event.getChannel().sendMessage(event.getMessageAuthor().getDisplayName() + " has not studied yet.");
    }
  }

  /**
   * Sends a message that tells for how long the given user has studied.
   * 
   * @param memberID    id of the member who just finished their study session.
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

  @Override
  public void onServerVoiceChannelMemberLeave(ServerVoiceChannelMemberLeaveEvent event) {
    if (event.getChannel().getName().equalsIgnoreCase(STUDY_CHANNEL)) {
      User user = event.getUser();
      TextChannel textChannel = event.getServer().getTextChannelsByName("study-records").get(0);
      StudyTimeRecord record;
      try {
        record = StudyTimeRecord.getStudySession(user.getIdAsString());
        long timeElapsed = record.finishSession();
        sendTimeElapsedMessage(textChannel, user.getDisplayName(event.getServer()), timeElapsed);
      } catch (InvalidDocumentException e) {
        textChannel.sendMessage("Something went wrong!");
      }
    }
  }

  @Override
  public void onServerVoiceChannelMemberJoin(ServerVoiceChannelMemberJoinEvent event) {
    if (event.getChannel().getName().matches(STUDY_CHANNEL)) {
      User user = event.getUser();
      TextChannel textChannel = event.getServer().getTextChannelsByName("study-records").get(0);
      StudyTimeRecord record;
      record = new StudyTimeRecord(user.getIdAsString());
      record.trackSession();
      textChannel.sendMessage(user.getDisplayName(event.getServer()) + " has started studying!");
    }
  }

  @Override
  public void onMessageCreate(MessageCreateEvent event) {
    String rawMsg = event.getMessageContent();
    String[] msgLst = rawMsg.split(" ");
    if (msgLst[0].equalsIgnoreCase("!leaderboard")) {
      StudyTimeLeaderboard leaderboard = StudyTimeLeaderboard.loadTimeLeaderboard();
      Server server = event.getServer().get();
      EmbedBuilder eb = leaderboard.getLeaderboardEmbed(server);
      event.getChannel().sendMessage(eb);
    } else if (msgLst[0].equalsIgnoreCase("!studytime")) {
      if (msgLst[1].equalsIgnoreCase("check")) {
        msgStudyTimeForUser(event);
      } else if (msgLst[1].equalsIgnoreCase("sub")) { // IT'S BACK!!
        try {
          long time = Math.abs(Long.parseLong(msgLst[2]));
          StudyTimeRecord.subtractStudyTime(event.getMessageAuthor().getIdAsString(), time * 60);
          event.getChannel().sendMessage("Successfully subtracted " + Long.toString(time) + " minute(s)!");
        } catch (InvalidDocumentException e) {
          event.getChannel().sendMessage("You haven't studied yet >:(");
        }
      }
    }
  }
}
