package events.ReactionEvent;

import exceptions.InvalidEmojiException;
import exceptions.InvalidDocumentException;
import org.bson.Document;
import org.javacord.api.entity.emoji.Emoji;
import persistence.DBReader;
import persistence.DBWriter;
import persistence.SaveOption;
import persistence.Writable;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 *
 */
public class ReactRoleMessage implements Writable {
    private static final String COLLECTION_NAME = "reaction_role_msgs";
    private static final DBReader reader = new DBReader(COLLECTION_NAME);
    private static final DBWriter writer = new DBWriter(COLLECTION_NAME);

    private final long msgId;
    private final Document reactRoleDoc;


    /**
     * Constructs a new ReactionRoleMessage from the data retrieved from the DB
     *
     * @param id          the snowflake ID of the message.
     * @param reactRoleDoc the BSON document that contains all the reactRole entries.
     */
    public ReactRoleMessage(long id, Document reactRoleDoc) {
        this.reactRoleDoc = reactRoleDoc;
        this.msgId = id;
    }

    /**
     * Constructs a new ReactionRoleMessage
     *
     * @param id the snowflake ID of the message.
     */
    public ReactRoleMessage(long id) {
        this.msgId = id;
        this.reactRoleDoc = new Document();
        this.reactRoleDoc.put(ACCESS_KEY, id);
    }

    /**
     * Loads a ReactRoleMessage with given ID from the database
     *
     * @param messageID the snowflake ID of the message
     * @return an Optional that contains ReactRoleMessage if it was retrieved from the database,
     * or an empty Optional otherwise
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

    public static void addRoleToMsg(long messageId, Emoji emoji, long roleId)
            throws InvalidEmojiException {
        ReactRoleMessage reactRoleMessage = loadReactRoleMessage(messageId)
                .orElse(new ReactRoleMessage(messageId));
        reactRoleMessage.addRoleEmojiPair(emoji, roleId);
        reactRoleMessage.save();
    }

    /**
     * Removes the emoji-role pair from reactRoleMessage
     * @param messageId the snowflake ID of the reactRoleMessage
     * @param emoji the emoji that should be removed from react role message.
     * @throws NoSuchElementException thrown if reactRoleMessage with given ID doesn't exist
     */
    public static void rmRoleFromMsg(long messageId, Emoji emoji)
            throws NoSuchElementException, InvalidEmojiException {
        ReactRoleMessage reactRoleMessage = loadReactRoleMessage(messageId).orElseThrow(NoSuchElementException::new);
        reactRoleMessage.rmRoleEmojiPair(emoji);
    }

    private void rmRoleEmojiPair(Emoji emoji) throws InvalidEmojiException {
        String emojiId = getEmojiId(emoji);
        if (reactRoleDoc.containsKey(emojiId)) {
            this.reactRoleDoc.remove(emojiId);
        } else {
            throw new InvalidEmojiException();
        }
    }

    private static String getEmojiId(Emoji userReaction) {
        if (userReaction.asCustomEmoji().isPresent()) {
            return userReaction.asCustomEmoji().get().getIdAsString();
        } else {
            return userReaction.asUnicodeEmoji().get();
        }
    }

    private void save() {
        writer.saveObject(this, SaveOption.DEFAULT);
    }

    @Override
    public Document toDoc() {
        return reactRoleDoc;
    }

    public void addRoleEmojiPair(Emoji userReaction, long roleId) throws InvalidEmojiException {
        String emojiId = getEmojiId(userReaction);
        if (reactRoleDoc.containsKey(emojiId)) {
            throw new InvalidEmojiException();
        }
        this.reactRoleDoc.put(emojiId, roleId);
    }

    public Optional<Long> getRoleIdByEmoji(Emoji userReaction) {
        String emojiId = getEmojiId(userReaction);
        if (reactRoleDoc.containsKey(emojiId)) {
            return Optional.of(reactRoleDoc.getLong(emojiId));
        } else {
            return Optional.empty();
        }
    }
}
