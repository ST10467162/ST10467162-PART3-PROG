# ST10467162-PART3-PROG

![Java CI with Maven](https://github.com/ST10467162/ST10467162-PART3-PROG/actions/workflows/maven.yml/badge.svg)

QuickChat is a console-based chat application written in Java. Users register and log in, then send, disregard or store messages. Part 3 adds arrays that hold the message data, reading stored messages back from a JSON file, a "Stored Messages" menu, and automated unit tests that run on GitHub.

The application is console only, with no GUIs and no `JOptionPane`.

## Features

**Part 1: Registration and login**
- Username, password and cell phone number are validated.
- Messaging is only available after a successful login.

**Part 2: Messaging**
- The recipient's number must include the international code (e.g. `+27718693002`).
- Messages are limited to 250 characters.
- Each message gets a 10-digit random message ID and a message hash in the format `ID:NUMBER:FIRSTLAST` (e.g. `79:0:HITONIGHT`).
- The user chooses to **send**, **disregard** or **store** each message.

**Part 3: Store data and display**
- Five arrays are filled while the program runs (nothing is hard-coded): sent messages, disregarded messages, stored messages, message hashes and message IDs.
- Stored messages are saved to `messages.json` and read back into the stored messages array when the program starts, using the Gson library.
- A fourth main menu option, **Stored Messages**, lets the user:
  1. Display the sender and recipient of all stored messages
  2. Display the longest stored message
  3. Search for a message ID and display the recipient and message
  4. Search for all messages stored or sent for a recipient
  5. Delete a message using its message hash
  6. Display a report with the full details of all stored messages
- Main menu option 2 displays a report of the sent messages (ID, hash, recipient and message).

## Project structure

```
pom.xml
.github/workflows/maven.yml          GitHub Actions workflow (runs the unit tests)
src/main/java/com/quickchat/
    Main.java                        Console menus and program flow
    Login.java                       Registration and login (Part 1)
    Message.java                     Message data, arrays, JSON and stored-message features
    QuickChat.java                   NetBeans template class
src/test/java/com/quickchat/
    MessageTest.java                 Unit tests for Part 2
    StoredMessagesTest.java          Unit tests for Part 3
```

## How to run

**Requirements:** Java 17 and Maven (NetBeans includes both).

**In NetBeans**
1. Open the project (File > Open Project).
2. Right-click the project, choose **Clean and Build** (Maven downloads Gson and JUnit).
3. Right-click `Main.java` and choose **Run File**.
4. Type in the Output window at the bottom.

**From the command line**
```
mvn clean compile
mvn exec:java -Dexec.mainClass=com.quickchat.Main
```

## Unit tests

The tests use JUnit 5 and `assertEquals`. `StoredMessagesTest` is built from the five test data messages in the brief:

| Message | Recipient | Message text | Flag |
|---|---|---|---|
| 1 | +27834557896 | Did you get the cake? | Sent |
| 2 | +27838884567 | Where are you? You are late! I have asked you to be on time. | Stored |
| 3 | +27834484567 | Yohoooo, I am at your gate. | Disregard |
| 4 | 0838884567 (developer) | It is dinner time ! | Sent |
| 5 | +27838884567 | Ok, I am leaving without you. | Stored |

The tests check that:
- the sent messages array is correctly populated
- the disregarded, stored, hash and ID arrays are correctly populated
- the longest message is displayed
- searching by message ID returns the right message
- searching by recipient returns all matching messages
- a message can be deleted using its hash
- the sent and stored reports contain the hash, recipient and message
- stored messages are read correctly from a JSON file

To run the tests locally: `mvn test` (in NetBeans, right-click the project and choose **Test**).

## Automated testing on GitHub

The workflow in `.github/workflows/maven.yml` runs automatically on every push and pull request to the `main` branch. It sets up Java 17, then runs `mvn -B verify`, which compiles the project and runs all the unit tests. The result is shown in the **Actions** tab and in the badge at the top of this README.

## Libraries used

- [Gson](https://github.com/google/gson) 2.10.1 for reading the JSON file
- [JUnit 5](https://junit.org/junit5/) 5.10.2 for unit testing

## Author

ST10467162
