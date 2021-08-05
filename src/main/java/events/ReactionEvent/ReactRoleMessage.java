package events.ReactionEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.server.Server;

import exceptions.EmojiAlreadyAssociatedWithRoleException;
import exceptions.InvalidDocumentException;
import exceptions.InvalidMessageIdException;
import io.vavr.control.Option;
import model.Bot;
import persistence.DBReader;
import persistence.Writable;

/**
 *      
 */
public class ReactRoleMessage extends Writable {
    private static String COLLECTION_NAME = "reaction_role_msgs";
    private static DBReader reader = new DBReader(COLLECTION_NAME);
    private long msgId;
    private Document reactRoleDoc;
    

    /**
     * Constructs a new ReactionRoleMessage from the data retrieved from the DB
     * @param id the snowflake ID of the message.
     * @param reacRoleDoc the BSON document that contains all the react role entries.
     */
    public ReactRoleMessage(long id, Document reacRoleDoc) {
        this.reactRoleDoc = reacRoleDoc;
        this.msgId = id;
    }

    /**
     * Constructs a new ReactionRoleMessage
     * @param id the snowflake ID of the message.
     */
    public ReactRoleMessage(long id) {
        this.msgId = id;
        this.reactRoleDoc = new Document();
    }


    /**
     * Loads a ReactRoleMessage with given ID from the database
     * @param messageID the snoflake ID of the message
     * @return an Optional that contains ReactRoleMessage if it was retrieved from the database,
     *          or an empty Optional otherwise
     */
    public static Optional<ReactRoleMessage> loadReactRoleMessage(long messageID) {
        try {
            Document readDoc = reader.loadObject(messageID);
            ReactRoleMessage rrMsg = new ReactRoleMessage(messageID, readDoc);
            return Optional.of(rrMsg);
        } catch (InvalidDocumentException e) {
            return Optional.empty();
        }
    }

    public static void addRoleToMsg(long messageId, Emoji emoji, long roleId) throws EmojiAlreadyAssociatedWithRoleException {
        String emojiId = getEmojiId(emoji);
        ReactRoleMessage rrMsg = loadReactRoleMessage(messageId).orElseThrow();
        t
    }


    // public static void checkAndRemoveRole(long messageID, Emoji emoji, Server server, long userID) {
    //     long serverID = server.getId();
    //     String emojiId = getEmojiId(emoji);
    //     getRoleID(messageID, emojiId, serverID).ifPresent(roleID -> 
    //         server.getRoleById(roleID).ifPresent(role -> 
    //             Bot.API.getUserById(userID).thenAccept(user ->
    //                 role.removeUser(user))));
    // }

    // public static boolean checkAndAddRole(long messageID, Emoji emoji, Server server, long userID) {
    //     long serverID = server.getId();
    //     String emojiID = getEmojiId(emoji);
    //     getRoleID(messageID, emojiID, serverID).ifPresent(roleID -> 
    //         server.getRoleById(roleID).ifPresent(role -> 
    //             Bot.API.getUserById(userID).thenAccept(user ->
    //                 role.addUser(user))));
    // }


    // public static Optional<Long> getRoleID(long messageId, String emojiId, long serverId) {
    //     try {
    //         Document reactionRoleMsg = reader.loadObject(messageId);
    //         Long roleID = reactionRoleMsg.getLong(emojiId);
    //         if (roleID != null) {
    //             return Optional.of(roleID);
    //         } else {
    //             return Optionall.empty();
    //     }.empty();
    //         }
    //     } catch (InvalidDocumentException e) {
    //         return Optiona
    // }
    
    // public static Optional<Document> loadReactRoleMsgDoc(long messageId, long severId) {
    //     try {
    //         Document reactionRoleMsg = reader.loadObject(messageId);
    //         reactionRoleMsg.
    //         if (roleId != null) {
    //             return Optional.of(roleId);
    //         } else {
    //             return Optional.empty();
    //         }
    //     } catch (InvalidDocumentException e) {
    //         return Optional.empty();
    //     }
    // }

    @Override
    public Document toDoc() {
        if (this.reactRoleDoc == null) {
            Document retDoc = new Document();
            retDoc.put(ACCESS_KEY, msgId);
            return retDoc;
        } else {
            return this.reactRoleDoc;
        }
    }


    private static String getEmojiId(Emoji userReaction) {
        if (userReaction.asCustomEmoji().isPresent()) {
            return userReaction.asCustomEmoji().get().getIdAsString();
        } else {
            return userReaction.asUnicodeEmoji().get();
        }
    }

    public void trackMsg(long id) {
        if (reactRoleDoc)
    }

    public Optional<String> getRoleIdByEmoji(Emoji userReaction) {
        String emojiId = getEmojiId(userReaction);
        if (reactRoleDoc.containsKey(emojiId)) {
            return Optional.of(reactRoleDoc.getString(emojiId));
        } else {
            return Optional.empty();
        }
    }


}
