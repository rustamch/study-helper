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
    private Map<String,String> reactRoles;
    
    public ReactRoleMessage(long id) {
        this.msgId = id;
        this.reactRoles = new HashMap<>();
    }

    public static void checkAndRemoveRole(long messageID, Emoji emoji, Server server, long userID) {
        long serverID = server.getId();
        String emojiId = getEmojiId(emoji);
        getRoleID(messageID, emojiId, serverID).ifPresent(roleID -> 
            server.getRoleById(roleID).ifPresent(role -> 
                Bot.API.getUserById(userID).thenAccept(user ->
                    role.removeUser(user))));
    }

    public static boolean checkAndAddRole(long messageID, Emoji emoji, Server server, long userID) {
        long serverID = server.getId();
        String emojiID = getEmojiId(emoji);
        getRoleID(messageID, emojiID, serverID).ifPresent(roleID -> 
            server.getRoleById(roleID).ifPresent(role -> 
                Bot.API.getUserById(userID).thenAccept(user ->
                    role.addUser(user))));
            
    }


    public static Optional<Long> getRoleID(long messageId, String emojiId, long serverId) {
        try {
            Document reactionRoleMsg = reader.loadObject(messageId);
            Long roleID = reactionRoleMsg.getLong(emojiId);
            if (roleID != null) {
                return Optional.of(roleID);
            } else {
                return Optionall.empty();
        }.empty();
            }
        } catch (InvalidDocumentException e) {
            return Optiona
    }
    
    public static Optional<Document> loadReactRoleMsgDoc(long messageId, long severId) {
        try {
            Document reactionRoleMsg = reader.loadObject(messageId);
            reactionRoleMsg.
            if (roleId != null) {
                return Optional.of(roleId);
            } else {
                return Optional.empty();
            }
        } catch (InvalidDocumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public Document toDoc() {
        Document retDoc = new Document();
        retDoc.put(ACCESS_KEY, msgId);
        for (Map.Entry<String, String> entry : reactRoles.entrySet()) {
            retDoc.put(entry.getKey(), entry.getValue());
        }
        return retDoc;
    }


    private static String getEmojiId(Emoji userReaction) {
        if (userReaction.asCustomEmoji().isPresent()) {
            return userReaction.asCustomEmoji().get().getIdAsString();
        } else {
            return userReaction.asUnicodeEmoji().get();
        }
    }

    public Object trackMsg(long id) {
        return null;
    }

    public static void addRoleToMsg(long messageId, Emoji emoji, long roleId) throws EmojiAlreadyAssociatedWithRoleException, InvalidMessageIdException {
        String emojiId = getEmojiId(emoji);
        try {
            Document readDoc = reader.loadObject(messageId);
            if (readDoc.containsKey(emojiId)) {
                throw new EmojiAlreadyAssociatedWithRoleException();
            }
        } catch (InvalidDocumentException e) {
            throw new InvalidMessageIdException();
        }
    }

}
