package com.quickchat;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * StoredMessagesTest.java
 *
 * Part 3 unit tests, using the five test data messages from the brief
 * to populate the arrays.
 */
class StoredMessagesTest {

    private static final String LONGEST = "Where are you? You are late! I have asked you to be on time.";

    private Message m1, m2, m3, m4, m5;

    // Runs before EVERY test: clears the arrays, then fills them with the test data
    @BeforeEach
    void setUp() {
        Message.resetState();

        m1 = new Message("0834557896", 0, "+27834557896", "Did you get the cake?");
        m1.SentMessage(1);                                   // Flag: Sent

        m2 = new Message("0838884568", 1, "+27838884567", LONGEST);
        m2.SentMessage(3);                                   // Flag: Stored

        m3 = new Message("0834484567", 2, "+27834484567", "Yohoooo, I am at your gate.");
        m3.SentMessage(2);                                   // Flag: Disregard

        m4 = new Message("0838884567", 3, "0838884567", "It is dinner time !");
        m4.SentMessage(1);                                   // Flag: Sent (Developer entry)

        m5 = new Message("0838884569", 4, "+27838884567", "Ok, I am leaving without you.");
        m5.SentMessage(3);                                   // Flag: Stored
    }

    // ---------- Arrays ----------
    @Test
    void sentMessagesArray_correctlyPopulated() {
        assertEquals(List.of("Did you get the cake?", "It is dinner time !"),
                Message.getSentMessageTexts());
    }

    @Test
    void disregardedAndStoredArrays_correctlyPopulated() {
        assertEquals(1, Message.getDisregardedMessages().size());
        assertEquals("Yohoooo, I am at your gate.", Message.getDisregardedMessages().get(0).getText());
        assertEquals(2, Message.getStoredMessages().size());
    }

    @Test
    void hashAndIdArrays_correctlyPopulated() {
        assertEquals(5, Message.getMessageHashes().size());
        assertEquals(5, Message.getMessageIDs().size());
        assertTrue(Message.getMessageHashes().contains(m2.getMessageHash()));
        assertTrue(Message.getMessageIDs().contains("0838884567"));
    }

    // ---------- Stored Messages features ----------
    @Test
    void displayLongestMessage() {
        assertEquals(LONGEST, Message.longestStoredMessage());
    }

    @Test
    void searchForMessageID() {
        assertEquals("It is dinner time !", Message.searchByMessageID("0838884567"));
        assertEquals("0838884567", Message.findByMessageID("0838884567").getRecipient());
    }

    @Test
    void searchForMessageID_notFound() {
        assertEquals("No message found with ID 9999999999", Message.searchByMessageID("9999999999"));
    }

    @Test
    void searchAllMessagesForRecipient() {
        assertEquals(List.of(LONGEST, "Ok, I am leaving without you."),
                Message.searchByRecipient("+27838884567"));
    }

    @Test
    void deleteMessageUsingHash() {
        String hash = m2.getMessageHash();
        assertEquals("Message: \"" + LONGEST + "\" successfully deleted.", Message.deleteByHash(hash));
        // It must be gone from the stored array and from the hash and ID arrays
        assertEquals(1, Message.getStoredMessages().size());
        assertFalse(Message.getMessageHashes().contains(hash));
        assertFalse(Message.getMessageIDs().contains("0838884568"));
    }

    @Test
    void deleteMessage_unknownHash() {
        assertEquals("No message found with hash NOPE", Message.deleteByHash("NOPE"));
    }

    @Test
    void displayReport_sentMessages() {
        String report = Message.printMessages();
        assertTrue(report.contains(m1.getMessageHash()));
        assertTrue(report.contains("+27834557896"));
        assertTrue(report.contains("Did you get the cake?"));
    }

    @Test
    void displayReport_storedMessages() {
        String report = Message.displayReport();
        assertTrue(report.contains(m2.getMessageHash()));
        assertTrue(report.contains("+27838884567"));
        assertTrue(report.contains("Ok, I am leaving without you."));
        assertFalse(report.contains("Did you get the cake?"));
    }

    @Test
    void displaySenderAndRecipient() {
        Message.setSender("+27111111111");
        String result = Message.displaySenderAndRecipient();
        assertTrue(result.contains("Sender: +27111111111 | Recipient: +27838884567"));
    }

    // ---------- Reading the JSON file ----------
    @Test
    void loadStoredMessages_readsJsonFile(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("messages.json");
        Files.writeString(file, "[\n  {\n"
                + "    \"messageID\": \"7925947357\",\n"
                + "    \"messageHash\": \"79:0:HITONIGHT\",\n"
                + "    \"recipient\": \"+27718693002\",\n"
                + "    \"message\": \"Hi Mike, can you join us for dinner tonight?\"\n"
                + "  }\n]\n");

        Message.resetState();                       // start with empty arrays
        Message.setStoreFile(file.toString());
        Message.loadStoredMessages();

        assertEquals(1, Message.getStoredMessages().size());
        Message loaded = Message.getStoredMessages().get(0);
        assertEquals("Hi Mike, can you join us for dinner tonight?", loaded.getText());
        assertEquals("79:0:HITONIGHT", loaded.getMessageHash());
        assertTrue(Message.getMessageIDs().contains("7925947357"));

        // Loading again must not add the same message twice
        Message.loadStoredMessages();
        assertEquals(1, Message.getStoredMessages().size());
    }
}