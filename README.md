# CryptoChat 🛡️💬

**CryptoChat** is a secure, encrypted multi-client chat application built in **Java** using **JavaFX** for the GUI and **Maven** for project management.

It supports:
* Secure login and registration
* Encrypted messaging (global and private/direct)
* Online users list
* User-friendly chat interface with timestamps
* Graceful handling of server disconnection

## 🚀 Features
* 🔒 **Secure Authentication** (Login/Register)
* 💬 **Global Chat** and **Private Messaging** (`/DM`)
* 🧑‍💻 **List of Online Users**
* 🕐 **Message Timestamps** (hh:mm:ss)
* 🛑 **Server Disconnection Detection**
* 📜 **Command-based Controls**

## 📦 Installation
1. **Clone the Repository**
   ```bash
   git clone https://github.com/elirrgn/CryptoChat.git
   cd cryptochat
   ```

2. **Install Java and Maven**
   Make sure you have:
   * Java 17+ installed (`java -version`)
   * Maven installed (`mvn -v`)

   If you don't have them installed:
   * Download Java
   * Download Maven

3. **Build the Project**
   ```bash
   mvn clean install
   ```

4. **Run the Client**
   ```bash
   mvn javafx:run
   ```

**Note:** This project assumes you already have a **CryptoChat Server** running separately. The client will attempt to connect to it on startup.

## 🖥️ Usage Guide
After launching the client, you will be prompted to **Login** or **Register**.

Inside the chat, you can use the following commands:

| Command | Description |
| --- | --- |
| Simply type a message | Send a global message to all users |
| `/DM;;<username>;;<message>` | Send a private (direct) message to a user |
| `/help` | Show the help menu |
| (Close Window) | Disconnect safely from the server |

📌 Double-click on a username in the "Online Users" list to automatically start a `/DM` message!

Each message sent or received will display:
```
[hh:mm:ss] Sender: Message
```

## 📁 Project Structure
```
cryptochat/
├── pom.xml           # Maven Project file
├── src/main/java/chat/
│   ├── Client/       # Client-side JavaFX GUI and Client logic
│   ├── Server/       # (Optional) Server logic if included
│   ├── Shared/       # Shared classes (models, utils)
├── resources/        # Config files (log4j2.xml etc.)
└── README.md
```

## 🛠️ Technologies Used
* Java 17
* JavaFX
* Maven
* AES/RSA Encryption (custom or BouncyCastle)
* TCP Sockets
* Log4j2 (for logging)

## 📜 License
This project is licensed under the MIT License.

## ✨ Acknowledgments
Thanks to the Open Source community for JavaFX, BouncyCastle, and many other libraries that made secure chat possible! 💖