import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

public class Main extends Application {
    private Thread serverThread;

    public static int PORT_NUMBER = 4444;

    public static final ArrayList<Role> DEFAULT_ROLES = new ArrayList<>(Arrays.asList(
        Role.WEREWOLF,
        Role.WEREWOLF,
        Role.MINION,
        Role.MASON,
        Role.MASON,
        Role.SEER,
        Role.ROBBER,
        Role.TROUBLEMAKER,
        Role.DRUNK,
        Role.INSOMNIAC
    ));

    private static void handle(WindowEvent event) {
        System.exit(0);
    }

    private void openServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);
        serverThread = new Thread(new Server(serverSocket, DEFAULT_ROLES));
        serverThread.start();
    }


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("One Night Ultimate Werewolf");

        // Create menu buttons
        Button hostButton = new Button("Host");
        Button joinButton = new Button("Join");
        Button changeRoleButton = new Button("Change roles");
        TextField joinIPTextField = new TextField("Enter IP");

        hostButton.setOnAction(event -> {
            try {
                openServer();
                ClientWindow cw = new ClientWindow();
                cw.processClient("localhost", PORT_NUMBER);
                cw.show();
            } catch (BindException e) {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "A server is already open!").showAndWait());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        joinButton.setOnAction(event -> {
            ClientWindow cw = new ClientWindow();
            cw.processClient(joinIPTextField.getText(), PORT_NUMBER);
            cw.show();
        });

        joinIPTextField.setOnAction(event -> {
            ClientWindow cw = new ClientWindow();
            cw.processClient(joinIPTextField.getText(), PORT_NUMBER);
            cw.show();
        });

        changeRoleButton.setOnAction(event -> System.out.println("Change role"));

        primaryStage.setOnCloseRequest(event ->  {
            event.consume();
            if (serverThread != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to close the server?", ButtonType.YES, ButtonType.NO);
                alert.showAndWait().ifPresent(response -> {
                    if (response.equals(ButtonType.YES)) {
                        primaryStage.close();
                        System.exit(0);
                    }
                });
            } else {
                primaryStage.close();
                System.exit(0);
            }
        });

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);

        grid.add(hostButton, 0, 0, 2, 1);
        grid.add(joinIPTextField, 0, 1);
        grid.add(joinButton, 1, 1);
        grid.add(changeRoleButton, 0, 2, 2, 1);

        primaryStage.setScene(new Scene(grid));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
