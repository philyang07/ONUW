import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;

public class Main extends Application {
    private Thread serverThread;
    private ArrayList<Role> roleList;
    private Text numPlayersText;

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

    public static final ArrayList<Role> ALL_ROLES = new ArrayList<>(Arrays.asList(
            Role.WEREWOLF,
            Role.MINION,
            Role.MASON,
            Role.SEER,
            Role.ROBBER,
            Role.TROUBLEMAKER,
            Role.DRUNK,
            Role.INSOMNIAC,
            Role.DOPPELGANGER,
            Role.TANNER,
            Role.HUNTER,
            Role.VILLAGER
    ));

    private void openServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);
        serverThread = new Thread(new Server(serverSocket, new ArrayList<>(roleList)));
        serverThread.start();
    }

    private void updateNumPlayersText() {
        numPlayersText.setText((roleList.size()-3) + " players");
    }

    public Stage roleListWindow() {
        Stage rlw = new Stage();

        ListView<Role> roleListView = new ListView<>();
        ListView<Role> allRolesListView = new ListView<>();
        Button addButton = new Button("Add");
        Button removeButton = new Button("Remove");

        addButton.setOnAction(event -> {
            Role selectedRole = allRolesListView.getSelectionModel().getSelectedItem();
            if (selectedRole == Role.DOPPELGANGER && roleList.contains(Role.DOPPELGANGER)) {
                new Alert(Alert.AlertType.ERROR, "Can only have 1 doppelganger!").showAndWait();
            } else if (selectedRole != null) {
                roleListView.getItems().add(selectedRole);
                roleList.add(selectedRole);
                updateNumPlayersText();
            }
        });

        removeButton.setOnAction(event -> {
            Role selectedRole = roleListView.getSelectionModel().getSelectedItem();
            if (selectedRole != null)
                if (roleList.size() <= 4) {
                    new Alert(Alert.AlertType.ERROR, "Minimum of 4 roles must be in the game!").showAndWait();
                } else {
                    roleListView.getItems().remove(selectedRole);
                    roleList.remove(selectedRole);
                    updateNumPlayersText();
                }
        });

        roleListView.setItems(FXCollections.observableArrayList(roleList));
        allRolesListView.setItems(FXCollections.observableArrayList(ALL_ROLES));

        GridPane grid = new GridPane();

        grid.add(new Text("Current roles"), 0, 0);
        grid.add(new Text("All roles"), 1, 0);
        grid.add(roleListView, 0, 1);
        grid.add(allRolesListView, 1, 1);
        grid.add(addButton, 1, 2);
        grid.add(removeButton, 0, 2);

        rlw.setScene(new Scene(grid));
        return rlw;
    }

    @Override
    public void start(Stage primaryStage) {
        roleList = new ArrayList<>(DEFAULT_ROLES);

        primaryStage.setTitle("One Night Ultimate Werewolf");

        // Create menu buttons
        Button hostButton = new Button("Host");
        Button joinButton = new Button("Join");
        Button changeRoleButton = new Button("Change roles");
        TextField joinIPTextField = new TextField("Enter IP");
        numPlayersText = new Text();
        updateNumPlayersText();

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

        changeRoleButton.setOnAction(event -> roleListWindow().show());

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

        grid.add(hostButton, 0, 0);
        grid.add(numPlayersText, 1, 0);
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
