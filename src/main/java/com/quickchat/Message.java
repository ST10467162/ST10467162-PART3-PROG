package com.quickchat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Message.java
 *
 * Represents one chat message. It also keeps track of information that is
 * shared by ALL messages while the program runs (the list of sent messages,
 * the list of stored messages and the totals). That shared information is
 * declared "static", which means it belongs to the class, not to one message.
 */
public class Message {

    // Maximum number of characters allowed in a message (a constant, so it can never change)
    public static final int MAX_LENGTH = 250;

    // Default name of the file that stored messages are saved to
    public static final String STORE_FILE = "messages.json";

    // The file currently in use (the unit tests point this somewhere else so the real file is not overwritten)
    private static String storeFile = STORE_FILE;

    // Who the messages are sent from (set from the logged-in user in Main)
    private static String sender = "You";

    // ---- static variables: shared by every Message object ----

    // List that holds every message the user chose to SEND
    private static final List<Message> sentMessages = new ArrayList<>();

    // List that holds every message the user chose to STORE for later
    private static final List<Message> storedMessages = new ArrayList<>();

    // Array (list) that holds every message the user chose to DISREGARD
    private static final List<Message> disregardedMessages = new ArrayList<>();

    // Array (list) that holds the hash of every message
    private static final List<String> messageHashes = new ArrayList<>();

    // Array (list) that holds the ID of every message
    private static final List<String> messageIDs = new ArrayList<>();

    // Counts how many messages have actually been sent
    private static int totalSent = 0;

    // Auto-incrementing message number: the first message is 0, then 1, 2 and so on
    private static int numMessages = 0;

    // ---- instance variables: each Message object has its own copy ----

    // The ten-digit random ID used to track the message
    private final String messageId;

    // This message's number (taken from numMessages when it was created)
    private final int messageNumber;

    // The cell phone number the message is being sent to
    private final String recipient;

    // The actual message text
    private final String text;

    // The hash made from the ID, message number and first and last words
    private final String messageHash;

    /**
     * Normal constructor, used by the running program.
     * It generates a random ID and uses the next message number
     * (numMessages++ gives the current value and then adds 1).
     */
    public Message(String recipient, String text) {
        this(generateMessageId(), numMessages++, recipient, text);
    }

    /**
     * Second constructor, which lets the unit tests choose the ID and the
     * message number so that the results are predictable.
     * The hash is created as soon as the message is built.
     */
    public Message(String messageId, int messageNumber, String recipient, String text) {
        this(messageId, messageNumber, recipient, text, null);
    }

    /**
     * Third constructor, used when a message is read back from the JSON file:
     * the hash that was saved is kept. If the hash is null, a new one is created.
     */
    private Message(String messageId, int messageNumber, String recipient, String text, String hash) {
        this.messageId = messageId;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.text = text;
        this.messageHash = (hash != null) ? hash : createMessageHash();
    }

    // ------------------------------------------------------------------
    // Methods required by the task
    // ------------------------------------------------------------------

    /** Checks that the message ID is not more than ten characters long. Returns true or false. */
    public boolean checkMessageID() {
        return messageId != null && messageId.length() <= 10;
    }

    /**
     * Checks the recipient's number. It reuses checkCellPhoneNumber() from the
     * Login class (Part 1), as the brief's hint says, and returns the success
     * or failure message that the brief asks for.
     */
    public String checkRecipientCell() {
        if (new Login().checkCellPhoneNumber(recipient)) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an "
                + "international code. Please correct the number and try again.";
    }

    /**
     * Creates the message hash in the format  ID:NUMBER:FIRSTLAST  in capitals.
     * Example: ID 0012345678, number 0, message "Hi Mike ... tonight?" gives 00:0:HITONIGHT
     */
    public String createMessageHash() {
        // First two characters of the message ID
        String idPart = messageId.length() >= 2 ? messageId.substring(0, 2) : messageId;

        // Split the message into words (\\s+ means "one or more spaces")
        String[] words = text.trim().split("\\s+");

        // First word and last word, with punctuation such as , and ? removed
        String first = stripPunctuation(words[0]);
        String last = stripPunctuation(words[words.length - 1]);

        // Join the parts together and change everything to capital letters
        return (idPart + ":" + messageNumber + ":" + first + last).toUpperCase();
    }

    /**
     * Carries out what the user chose to do with the message:
     * 1 = send it, 2 = disregard it, 3 = store it to send later.
     * Returns the message that must be shown on the screen.
     */
    public String SentMessage(int choice) {
        switch (choice) {
            case 1:
                // Add to the sent list and increase the total
                sentMessages.add(this);
                recordIdentifiers();
                totalSent++;
                return "Message successfully sent.";
            case 2:
                // Kept in the disregarded array; the user must press 0 to delete (handled in Main)
                disregardedMessages.add(this);
                recordIdentifiers();
                return "Press 0 to delete the message.";
            case 3:
                // Add to the stored list and save the list to the JSON file
                storedMessages.add(this);
                recordIdentifiers();
                storeMessage();
                return "Message successfully stored.";
            default:
                return "Invalid option.";
        }
    }

    /** Returns the details of all the messages that have been sent while the program is running. */
    public static String printMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent yet.";
        }
        // StringBuilder is used to join many pieces of text efficiently
        StringBuilder sb = new StringBuilder();
        for (Message m : sentMessages) {
            sb.append(m.getDetails()).append(System.lineSeparator());
        }
        return sb.toString();
    }

    /** Returns the total number of messages that have been sent. */
    public static int returnTotalMessages() {
        return totalSent;
    }

    /**
     * Saves every stored message to messages.json.
     * JSON is a text format made of "name": "value" pairs. The whole file is
     * rewritten each time so that it is always valid JSON.
     */
    public void storeMessage() {
        saveStoredMessages();
    }

    /** Does the actual saving. It is static because it only uses the shared stored list. */
    private static void saveStoredMessages() {
        StringBuilder json = new StringBuilder("[\n");
        // Loop through the stored messages and write each one as a JSON object
        for (int i = 0; i < storedMessages.size(); i++) {
            Message m = storedMessages.get(i);
            json.append("  {\n")
                .append("    \"messageID\": \"").append(escape(m.messageId)).append("\",\n")
                .append("    \"messageHash\": \"").append(escape(m.messageHash)).append("\",\n")
                .append("    \"recipient\": \"").append(escape(m.recipient)).append("\",\n")
                .append("    \"message\": \"").append(escape(m.text)).append("\"\n")
                // No comma after the last object, otherwise the JSON would be invalid
                .append("  }").append(i < storedMessages.size() - 1 ? "," : "").append("\n");
        }
        json.append("]\n");
        // try-with-resources closes the file automatically, even if an error happens
        try (FileWriter fw = new FileWriter(storeFile)) {
            fw.write(json.toString());
        } catch (IOException e) {
            System.out.println("Could not write to " + storeFile + ": " + e.getMessage());
        }
    }


    // ------------------------------------------------------------------
    // Part 3: arrays, JSON reading and the "Stored Messages" features
    // ------------------------------------------------------------------

    /** Adds this message's hash and ID to the message hash and message ID arrays. */
    private void recordIdentifiers() {
        messageHashes.add(messageHash);
        messageIDs.add(messageId);
    }

    /** Matches one object in messages.json; the field names must match the names in the file. */
    private static class StoredRecord {
        String messageID;
        String messageHash;
        String recipient;
        String message;
    }

    /**
     * Reads messages.json into the stored messages array.
     * Gson turns the JSON text into a list of StoredRecord objects. Messages whose
     * ID is already in the arrays are skipped, so calling this twice adds nothing twice.
     */
    public static void loadStoredMessages() {
        File file = new File(storeFile);
        if (!file.exists()) {
            return;   // nothing has been stored yet
        }
        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<StoredRecord>>() { }.getType();
            List<StoredRecord> records = new Gson().fromJson(reader, listType);
            if (records == null) {
                return;   // empty file
            }
            for (int i = 0; i < records.size(); i++) {
                StoredRecord r = records.get(i);
                if (r == null || r.message == null || r.messageID == null || messageIDs.contains(r.messageID)) {
                    continue;
                }
                // The message number is the middle part of the hash (e.g. 79:0:HITONIGHT gives 0)
                int number = i;
                if (r.messageHash != null) {
                    String[] parts = r.messageHash.split(":");
                    if (parts.length >= 3) {
                        try { number = Integer.parseInt(parts[1]); } catch (NumberFormatException ignored) { }
                    }
                }
                Message m = new Message(r.messageID, number, r.recipient, r.message, r.messageHash);
                storedMessages.add(m);
                m.recordIdentifiers();
                // Make sure new messages never reuse a message number
                if (number >= numMessages) {
                    numMessages = number + 1;
                }
            }
        } catch (Exception e) {
            System.out.println("Could not read " + storeFile + ": " + e.getMessage());
        }
    }

    /** a) The sender and recipient of every stored message. */
    public static String displaySenderAndRecipient() {
        if (storedMessages.isEmpty()) {
            return "No stored messages.";
        }
        StringBuilder sb = new StringBuilder();
        for (Message m : storedMessages) {
            sb.append("Sender: ").append(sender)
              .append(" | Recipient: ").append(m.recipient)
              .append(System.lineSeparator());
        }
        return sb.toString();
    }

    /** b) The longest stored message (the one with the most characters). */
    public static String longestStoredMessage() {
        String longest = "";
        for (Message m : storedMessages) {
            if (m.text.length() > longest.length()) {
                longest = m.text;
            }
        }
        return longest.isEmpty() ? "No stored messages." : longest;
    }

    /** Finds a sent or stored message by its ID. Returns null if there is no match. */
    public static Message findByMessageID(String id) {
        for (Message m : sentMessages) {
            if (m.messageId.equals(id)) return m;
        }
        for (Message m : storedMessages) {
            if (m.messageId.equals(id)) return m;
        }
        return null;
    }

    /** c) Searches for a message ID and returns the message text. */
    public static String searchByMessageID(String id) {
        Message m = findByMessageID(id);
        return (m == null) ? "No message found with ID " + id : m.text;
    }

    /** d) Every sent or stored message for one recipient. */
    public static List<String> searchByRecipient(String recipient) {
        List<String> found = new ArrayList<>();
        for (Message m : sentMessages) {
            if (m.recipient.equals(recipient)) found.add(m.text);
        }
        for (Message m : storedMessages) {
            if (m.recipient.equals(recipient)) found.add(m.text);
        }
        return found;
    }

    /** e) Deletes a message using its hash and returns the confirmation message. */
    public static String deleteByHash(String hash) {
        for (int i = 0; i < storedMessages.size(); i++) {
            if (storedMessages.get(i).messageHash.equalsIgnoreCase(hash)) {
                Message removed = storedMessages.remove(i);
                forget(removed);
                saveStoredMessages();
                return "Message: \"" + removed.text + "\" successfully deleted.";
            }
        }
        for (int i = 0; i < sentMessages.size(); i++) {
            if (sentMessages.get(i).messageHash.equalsIgnoreCase(hash)) {
                Message removed = sentMessages.remove(i);
                forget(removed);
                return "Message: \"" + removed.text + "\" successfully deleted.";
            }
        }
        return "No message found with hash " + hash;
    }

    /** Removes a deleted message's hash and ID from the hash and ID arrays. */
    private static void forget(Message m) {
        messageHashes.remove(m.messageHash);
        messageIDs.remove(m.messageId);
    }

    /** f) A report with the full details of all the stored messages. */
    public static String displayReport() {
        if (storedMessages.isEmpty()) {
            return "No stored messages.";
        }
        StringBuilder sb = new StringBuilder("===== STORED MESSAGES REPORT =====").append(System.lineSeparator());
        for (Message m : storedMessages) {
            sb.append(m.getDetails()).append(System.lineSeparator());
        }
        return sb.toString();
    }

    // Setters and getters for the shared arrays (copies are returned so they cannot be changed from outside)
    public static void setSender(String newSender) { sender = newSender; }
    public static void setStoreFile(String path) { storeFile = path; }
    public static List<Message> getSentMessages() { return new ArrayList<>(sentMessages); }
    public static List<Message> getDisregardedMessages() { return new ArrayList<>(disregardedMessages); }
    public static List<Message> getStoredMessages() { return new ArrayList<>(storedMessages); }
    public static List<String> getMessageHashes() { return new ArrayList<>(messageHashes); }
    public static List<String> getMessageIDs() { return new ArrayList<>(messageIDs); }

    /** The text of every sent message, in the order they were sent. */
    public static List<String> getSentMessageTexts() {
        List<String> texts = new ArrayList<>();
        for (Message m : sentMessages) texts.add(m.text);
        return texts;
    }

    // ------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------

    /** Checks the 250-character rule and returns the success or failure message. */
    public static String checkMessageLength(String text) {
        if (text != null && text.length() <= MAX_LENGTH) {
            return "Message ready to send.";
        }
        // Work out by how many characters the message is too long
        int over = (text == null ? 0 : text.length()) - MAX_LENGTH;
        return "Message exceeds 250 characters by " + over + "; please reduce the size.";
    }

    /** Returns the details in the order the brief asks for: ID, Hash, Recipient, Message. */
    public String getDetails() {
        return "Message ID: " + messageId
                + " | Message Hash: " + messageHash
                + " | Recipient: " + recipient
                + " | Message: " + text;
    }

    /** Generates a random ten-digit ID. %010d pads with zeros so it is always ten digits. */
    public static String generateMessageId() {
        long n = ThreadLocalRandom.current().nextLong(10_000_000_000L);
        return String.format("%010d", n);
    }

    /** Clears all the shared information. Only the unit tests use this, so each test starts fresh. */
    public static void resetState() {
        sentMessages.clear();
        storedMessages.clear();
        disregardedMessages.clear();
        messageHashes.clear();
        messageIDs.clear();
        totalSent = 0;
        numMessages = 0;
        sender = "You";
        storeFile = "target/test-messages.json";   // keeps the tests away from the real messages.json
    }

    // Getters: let other classes read the private variables
    public String getMessageId() { return messageId; }
    public String getMessageHash() { return messageHash; }
    public String getRecipient() { return recipient; }
    public String getText() { return text; }
    public int getMessageNumber() { return messageNumber; }

    /** Removes everything that is not a letter or a number (e.g. the comma in "Mike,"). */
    private static String stripPunctuation(String word) {
        return word.replaceAll("[^A-Za-z0-9]", "");
    }

    /** Makes text safe to place inside JSON by escaping quotes, backslashes and new lines. */
    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}