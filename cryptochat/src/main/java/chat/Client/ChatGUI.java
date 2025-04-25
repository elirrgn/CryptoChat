package chat.Client;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Set;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ChatGUI extends Application {

    private static Client client = new Client();

    private static TextArea chatArea;
    private static TextField messageField;
    private static Button sendButton;
    private static ListView<String> onlineUsers;
    private static TextField usernameField;
    private static PasswordField passwordField;
    private static Button loginButton;
    private static Button registerButton;
    private static VBox loginPane;
    private static BorderPane chatPane;
    private static Label loginStatusLabel;

    // Register View
    private static TextField regUsernameField;
    private static PasswordField regPasswordField;
    private static PasswordField regConfirmPasswordField;
    private static Button regSubmitButton;
    private static Button regBackButton;
    private static VBox registerPane;
    private static Label regStatusLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("CryptoChat Client");
        client.connectWithServer();

        // Chat View Layout
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);

        messageField = new TextField();
        messageField.setPromptText("Type your message...");

        sendButton = new Button("Send");
        sendButton.setDefaultButton(true);

        HBox inputBox = new HBox(10, messageField, sendButton);
        inputBox.setPadding(new Insets(10));
        inputBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(messageField, Priority.ALWAYS);

        onlineUsers = new ListView<>();
        onlineUsers.setPrefWidth(150);

        chatPane = new BorderPane();
        chatPane.setPadding(new Insets(10));
        chatPane.setCenter(chatArea);
        chatPane.setBottom(inputBox);
        chatPane.setRight(onlineUsers);

        // Login View Layout
        usernameField = new TextField();
        usernameField.setPromptText("Username");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        loginButton = new Button("Login");
        registerButton = new Button("Go to Register page");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setMaxWidth(Double.MAX_VALUE);

        loginStatusLabel = new Label();
        loginStatusLabel.setStyle("-fx-text-fill: red;");

        VBox loginFields = new VBox(10, usernameField, passwordField, loginButton, registerButton, loginStatusLabel);
        loginFields.setAlignment(Pos.CENTER);
        loginFields.setPadding(new Insets(20));

        loginPane = new VBox(loginFields);
        loginPane.setAlignment(Pos.CENTER);

        // Register View Layout
        regUsernameField = new TextField();
        regUsernameField.setPromptText("Username");

        regPasswordField = new PasswordField();
        regPasswordField.setPromptText("Password");

        regConfirmPasswordField = new PasswordField();
        regConfirmPasswordField.setPromptText("Confirm Password");

        regSubmitButton = new Button("Submit Registration");
        regBackButton = new Button("Back to Login");
        regSubmitButton.setMaxWidth(Double.MAX_VALUE);
        regBackButton.setMaxWidth(Double.MAX_VALUE);

        regStatusLabel = new Label();
        regStatusLabel.setStyle("-fx-text-fill: red;");

        VBox regFields = new VBox(10, regUsernameField, regPasswordField, regConfirmPasswordField, regSubmitButton, regBackButton, regStatusLabel);
        regFields.setAlignment(Pos.CENTER);
        regFields.setPadding(new Insets(20));

        registerPane = new VBox(regFields);
        registerPane.setAlignment(Pos.CENTER);

        // Scene Management
        Scene loginScene = new Scene(loginPane, 600, 400);
        Scene chatScene = new Scene(chatPane, 800, 600);
        Scene registerScene = new Scene(registerPane, 600, 400);

        // Event Handling
        loginButton.setOnAction(e -> handleLogin(primaryStage, chatScene));
        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleLogin(primaryStage, chatScene);
        });
        usernameField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleLogin(primaryStage, chatScene);
        });

        registerButton.setOnAction(e -> primaryStage.setScene(registerScene));

        regBackButton.setOnAction(e -> {
            regStatusLabel.setText("");
            primaryStage.setScene(loginScene);
        });

        regSubmitButton.setOnAction(e -> handleRegister(primaryStage, chatScene));
        regConfirmPasswordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleRegister(primaryStage, chatScene);
        });
        regPasswordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleRegister(primaryStage, chatScene);
        });
        regUsernameField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleRegister(primaryStage, chatScene);
        });

        sendButton.setOnAction(e -> sendMessage());
        messageField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) sendMessage();
        });

        primaryStage.setOnCloseRequest((WindowEvent event) -> {
            client.disconnect();
        });

        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private static void handleLogin(Stage stage, Scene chatScene) {
        String username = usernameField.getText().trim();
        String psw = passwordField.getText().trim();

        if (username.isEmpty() || psw.isEmpty()) {
            loginStatusLabel.setText("Please fill in all fields.");
            return;
        }

        boolean result = client.loginOrRegister("login", username, psw);

        if (result) {
            loginStatusLabel.setText("");
            stage.setScene(chatScene);
        } else {
            loginStatusLabel.setText("Login failed. Please try again.");
        }
    }

    private static void handleRegister(Stage stage, Scene chatScene) {
        String username = regUsernameField.getText().trim();
        String password = regPasswordField.getText().trim();
        String confirm = regConfirmPasswordField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            regStatusLabel.setText("Please fill in all fields.");
            return;
        }

        if (!password.equals(confirm)) {
            regStatusLabel.setText("Passwords do not match.");
            return;
        }

        boolean result = client.loginOrRegister("register", username, password);

        if (result) {
            regStatusLabel.setText("");
            loginStatusLabel.setText("");
            stage.setScene(chatScene);
        } else {
            regStatusLabel.setText("Registration failed. Username may already exist.");
        }
    }

    private static void sendMessage() {
        String msg = messageField.getText().trim();
        if (!msg.isEmpty()) {
            client.sendMsg(msg);
            chatArea.appendText("You: " + msg + "\n");
            messageField.clear();
        }
    }

    public static void appendMessage(String msg) {
        chatArea.appendText(msg+"\n");
    }

    public static void addOnlineUser(String username) {
        if (!onlineUsers.getItems().contains(username)) {
            onlineUsers.getItems().add(username);
        }
    }
    
    public static void removeOnlineUser(String username) {
        onlineUsers.getItems().remove(username);
    }

    public static void loadOnlineClients(Set<String> clientList) {
        // Using forEach to iterate over each clientName in clientList and add the user to the online users list
        clientList.forEach(clientName -> {
            addOnlineUser(clientName);
            System.out.println(clientName);
        });
    }
    public static void main(String[] args) {
        launch(args);
    }
}
