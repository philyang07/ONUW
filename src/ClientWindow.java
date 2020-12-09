import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ClientWindow extends Stage {
    private final TextArea outputArea;
    private final TextField inputField;
    private final Button enterButton;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Button getEnterButton() {
        return enterButton;
    }

    public TextField getInputField() {
        return inputField;
    }

    public void outputToWindow(String output) {
        outputArea.appendText(output + "\n");
    }

    private void processDisconnection() {
        Platform.runLater(() -> {
            new Alert(Alert.AlertType.ERROR, "Connection error.").showAndWait();
            close();
        });
    }

    private void closeClient() {
        try {
            if (out != null)
                out.close();
            if (in != null)
                in.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        Platform.runLater(super::close);
        closeClient();
    }

    private void enterAction() {
        try {
            out.writeObject(getInputField().getText());
            getInputField().clear();
        } catch (IOException e) {
            e.printStackTrace();
            processDisconnection();
        }
    }

    public void processClient(String hostName, int portNumber) {
        // Continuously output server output
        new Thread(() -> {
            try {
                socket = new Socket(hostName, portNumber);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                // Use enter button/key to send data
                getEnterButton().setOnAction(event -> enterAction());
                getInputField().setOnAction(event -> enterAction());

                while (true)
                    outputToWindow((String) in.readObject());
            } catch (IOException | ClassNotFoundException e) {
                if (e instanceof UnknownHostException) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Invalid IP provided.").showAndWait());
                    close();
                } else if (e instanceof SocketException) {
                    if (e instanceof ConnectException)
                        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Server not open or full.").showAndWait());
                    close();
                } else {
                    processDisconnection();
                }
            }
        }).start();
    }

    public ClientWindow() {
        super();
        outputArea = new TextArea();
        inputField = new TextField();
        enterButton = new Button("Enter");
        GridPane grid = new GridPane();

        outputArea.prefWidth(300);
        outputArea.prefHeight(200);
        inputField.prefWidth(240);

        outputArea.setEditable(false);
        enterButton.setOnAction(event -> {
            outputToWindow(inputField.getText());
            inputField.clear();
        });
        setOnCloseRequest(event -> closeClient());

        grid.add(outputArea, 0, 0, 4, 1);
        grid.add(inputField, 0, 1, 3, 1);
        grid.add(enterButton, 3, 1, 1, 1);

        setScene(new Scene(grid));
    }
}
