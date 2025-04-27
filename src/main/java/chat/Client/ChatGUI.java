package chat.Client;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Handles the graphical user interface (GUI) for the CryptoChat client.
 * Provides login, registration, and chat functionalities, including sending messages and managing online users.
 * It interacts with the server for authentication and message handling.
 */
public class ChatGUI extends Application {

    private static Client client = new Client();

    private static Stage primaryStage;

    private static TextFlow chatFlow;
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
    private static ProgressIndicator loginLoadingIndicator;

    // Register View
    private static TextField regUsernameField;
    private static PasswordField regPasswordField;
    private static PasswordField regConfirmPasswordField;
    private static Button regSubmitButton;
    private static Button regBackButton;
    private static VBox registerPane;
    private static Label regStatusLabel;
    private static ProgressIndicator registerLoadingIndicator;

    /**
     * Initializes and starts the CryptoChat client GUI.
     * Sets up the various UI elements, including the login screen, registration screen, and chat interface.
     * Handles user interactions and transitions between the login, registration, and chat scenes.
     * Initializes the connection with the server and sets up event handlers for buttons and text fields.
     *
     * @param primaryStage The primary stage for the JavaFX application.
     */
    @Override
    public void start(Stage primaryStage) {
        ChatGUI.primaryStage = primaryStage;
        primaryStage.setTitle("CryptoChat Client");
        client.connectWithServer();

        // Chat View Layout
        chatFlow = new TextFlow();
        chatFlow.setPadding(new Insets(10));
        chatFlow.setLineSpacing(5.0);
        chatFlow.setPrefWidth(600);  // Set fixed width for chat area

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

        // On double-click set up DM message to selected user
        onlineUsers.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // double-click
                String selectedUser = onlineUsers.getSelectionModel().getSelectedItem();
                if (selectedUser != null) {
                    messageField.setText("/DM;;" + selectedUser + ";;");
                    messageField.requestFocus();
                    messageField.positionCaret(messageField.getText().length()); // put cursor at end
                }
            }
        });

        Label onlineUsersLabel = new Label("Online Users:");
        onlineUsersLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 5 0 5 0;");

        VBox onlineUsersBox = new VBox(5, onlineUsersLabel, onlineUsers);
        onlineUsersBox.setPadding(new Insets(10));
        onlineUsersBox.setAlignment(Pos.TOP_CENTER);

        chatPane = new BorderPane();
        chatPane.setPadding(new Insets(10));
        chatPane.setCenter(new ScrollPane(chatFlow));  // Set chatFlow inside ScrollPane
        chatPane.setBottom(inputBox);
        chatPane.setRight(onlineUsersBox);


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

        loginLoadingIndicator = new ProgressIndicator();
        loginLoadingIndicator.setVisible(false);
        loginLoadingIndicator.setPrefSize(30, 30);

        VBox loginFields = new VBox(10, usernameField, passwordField, loginButton, registerButton, loginLoadingIndicator, loginStatusLabel);
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

        registerLoadingIndicator = new ProgressIndicator();
        registerLoadingIndicator.setVisible(false);
        registerLoadingIndicator.setPrefSize(30, 30);

        VBox regFields = new VBox(10, regUsernameField, regPasswordField, regConfirmPasswordField, regSubmitButton, regBackButton, registerLoadingIndicator, regStatusLabel);
        regFields.setAlignment(Pos.CENTER);
        regFields.setPadding(new Insets(20));

        registerPane = new VBox(regFields);
        registerPane.setAlignment(Pos.CENTER);

        // Scene Management
        Scene loginScene = new Scene(loginPane, 600, 400);
        Scene chatScene = new Scene(chatPane, 800, 600);
        Scene registerScene = new Scene(registerPane, 600, 400);

        // Event Handling
        loginButton.setOnAction(e -> handleLogin(primaryStage, chatScene)); //Login handling
        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleLogin(primaryStage, chatScene); // Submit on Enter key pressed
        });
        usernameField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleLogin(primaryStage, chatScene); // Submit on Enter key pressed
        });

        registerButton.setOnAction(e -> primaryStage.setScene(registerScene)); // Redirect to register screen

        regBackButton.setOnAction(e -> { // Go back to login screen
            regStatusLabel.setText("");
            primaryStage.setScene(loginScene);
        });

        regSubmitButton.setOnAction(e -> handleRegister(primaryStage, chatScene)); //Registration handling
        regConfirmPasswordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleRegister(primaryStage, chatScene); // Submit on Enter key pressed
        });
        regPasswordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleRegister(primaryStage, chatScene); // Submit on Enter key pressed
        });
        regUsernameField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleRegister(primaryStage, chatScene); // Submit on Enter key pressed
        });

        sendButton.setOnAction(e -> sendMessage());
        messageField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) sendMessage(); // Send message on Enter key pressed
        });

        primaryStage.setOnCloseRequest((WindowEvent event) -> {
            event.consume(); // Doesn't make the window close

            // Show Alert to confirm 
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Disconnect");
            alert.setHeaderText("You are about to disconnect");
            alert.setContentText("Are you sure you want to close the application?");

            // Wait for the user's response
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    client.disconnect();
                    Platform.exit();
                    System.exit(0);
                }
            });
        });

        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    /**
     * Handles the login process.
     * 
     * This method validates the user's credentials, initiates a login task in the background, and updates the UI 
     * based on the result (success or failure).
     *
     * @param stage The current stage of the application.
     * @param chatScene The scene to switch to upon successful login.
     */
    private static void handleLogin(Stage stage, Scene chatScene) {
        loginButton.setDisable(true);
        String username = usernameField.getText().trim();
        String psw = passwordField.getText().trim();
        
        if (username.isEmpty() || psw.isEmpty()) {
            loginStatusLabel.setText("Please fill in all fields.");
            loginButton.setDisable(false);
            return;
        }

        loginLoadingIndicator.setVisible(true);
        loginStatusLabel.setText("");

        Task<Boolean> loginTask = new Task<>() {
            @Override
            protected Boolean call() {
                return client.loginOrRegister("login", username, psw);
            }
        };

        loginTask.setOnSucceeded(e -> {
            loginLoadingIndicator.setVisible(false);
            if (loginTask.getValue()) {
                loginStatusLabel.setText("");
                stage.setScene(chatScene);
            } else {
                loginStatusLabel.setText("Login failed. Please try again.");
                loginButton.setDisable(false);
            }
        });

        loginTask.setOnFailed(e -> {
            loginLoadingIndicator.setVisible(false);
            loginStatusLabel.setText("An error occurred during login.");
            loginButton.setDisable(false);
        });

        new Thread(loginTask).start();
    }

    /**
     * Handles the registration process.
     * 
     * This method validates the registration inputs, performs the registration in the background, and updates the UI 
     * based on the result (success or failure).
     *
     * @param stage The current stage of the application.
     * @param chatScene The scene to switch to upon successful registration.
     */
    private static void handleRegister(Stage stage, Scene chatScene) {
        regSubmitButton.setDisable(true);
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

        registerLoadingIndicator.setVisible(true);
        regStatusLabel.setText("");

        Task<Boolean> registerTask = new Task<>() {
            @Override
            protected Boolean call() {
                return client.loginOrRegister("register", username, password);
            }
        };

        registerTask.setOnSucceeded(e -> {
            registerLoadingIndicator.setVisible(false);
            if (registerTask.getValue()) {
                regStatusLabel.setText("");
                loginStatusLabel.setText("");
                stage.setScene(chatPane.getScene());
            } else {
                regStatusLabel.setText("Registration failed. Username may already exist.");
            }
        });

        registerTask.setOnFailed(e -> {
            registerLoadingIndicator.setVisible(false);
            regStatusLabel.setText("An error occurred during registration.");
            regSubmitButton.setDisable(false);
        });

        new Thread(registerTask).start();
    }

    /**
     * Sends a message typed by the user.
     * 
     * The method retrieves the message from the input field and sends it to the server. It also updates the chat with 
     * the message, indicating whether it was successfully sent or if there was an error.
     */
    private static void sendMessage() {
        String msg = messageField.getText().trim();
        if (!msg.isEmpty()) {
            String errorMsg = client.sendMsg(msg);
            if(errorMsg == null){
                if(msg.startsWith("/DM")) {
                    String[] splitMsg = msg.split(";;");
                    appendMessage("You (to "+splitMsg[1]+"): "+splitMsg[2], "#3498DB");
                }else {
                    appendMessage("You: " + msg, "#3498DB");
                }
            }else {
                appendMessage("You: " + msg, "#696969");
                appendMessage(errorMsg, "#E74C3C");
            }
            messageField.clear();
        }
    }

    /**
     * Appends a message to the chat window with a default color (black).
     * 
     * This method adds the given message to the chat area with a timestamp, formatted with the current time.
     *
     * @param msg The message to display in the chat.
     */
    public static void appendMessage(String msg) {
        appendMessage(msg, "black");
    }

    /**
     * Appends a message to the chat window with a custom color.
     * 
     * This method adds the given message to the chat area with a timestamp, formatted with the current time. The 
     * message is displayed in the specified color.
     *
     * @param msg The message to display in the chat.
     * @param color The color in which the message will be displayed (as a string or hexadecimal string).
     */
    public static void appendMessage(String msg, String color) {
        Platform.runLater(() -> {
            // Get current time in the format hh:mm:ss
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

            // Construct the formatted message with time, sender, and message
            String formattedMsg = String.format("[%s] %s", time, msg);

            Text text = new Text(formattedMsg + "\n");
            text.setStyle("-fx-fill: " + color + ";");
            chatFlow.getChildren().add(text);
        });
    }

    /**
     * Handles the server disconnection event.
     * 
     * This method displays a message indicating the server is offline and disables chat functionality.
     */
    public static void serverDisconnected() {
        appendMessage("Server offline, close the window", "red");
        messageField.clear();
        messageField.setDisable(true);
        sendButton.setDisable(true);
    }

    public static void clientProcessKilled() {
        appendMessage("Client Process Killed!", "red");
        messageField.clear();
        messageField.setDisable(true);
        sendButton.setDisable(true);
    }
    
    /**
     * Adds an online user to the online users list.
     * 
     * This method adds the specified username to the list of online users in the GUI, ensuring thread-safety.
     *
     * @param username The username of the online user to add.
     */
    public synchronized static void addOnlineUser(String username) {
        Platform.runLater(() -> {
            if (!onlineUsers.getItems().contains(username)) {
                onlineUsers.getItems().add(username);
            }
        });
    }

    /**
     * Removes an online user from the online users list.
     * 
     * This method removes the specified username from the list of online users in the GUI, ensuring thread-safety.
     *
     * @param username The username of the online user to remove.
     */
    public synchronized static void removeOnlineUser(String username) {
        Platform.runLater(() -> { // If not Exception
            onlineUsers.getItems().remove(username);
        });
    }

    /**
     * Loads a set of online users into the online users list.
     * 
     * This method populates the online users list with a set of usernames provided by the server.
     *
     * @param clientList A set containing the usernames of all online clients.
     */
    public synchronized static void loadOnlineClients(Set<String> clientList) {
        clientList.forEach(ChatGUI::addOnlineUser);
    }

    /**
     * Updates the title of the primary stage.
     * 
     * This method updates the window title to the given string in a thread-safe manner.
     *
     * @param newTitle The new title for the primary stage.
     */
    public static void setPrimaryStageTitle(String newTitle) {
        Platform.runLater(() -> {
            primaryStage.setTitle(newTitle);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
