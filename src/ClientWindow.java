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
import java.net.Socket;

public class ClientWindow extends Stage {
    private final TextArea outputArea;
    private final TextField inputField;
    private final Button enterButton;

    public Button getEnterButton() {
        return enterButton;
    }

    public TextField getInputField() {
        return inputField;
    }

    public void outputToWindow(String output) {
        outputArea.appendText(output + "\n");
    }

    public void processClient(String hostName, int portNumber) {
        // Continuously output server output
        new Thread(() -> {
            try (
                Socket socket = new Socket(hostName, portNumber);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
            ) {
                // Use enter button/key to send data
                getEnterButton().setOnAction(event -> {
                    try {
                        out.writeObject(getInputField().getText());
                    } catch (IOException e) {
                        close();
                        new Alert(Alert.AlertType.ERROR, "Connection error.").showAndWait();
                    }
                });
                getInputField().setOnAction(event -> {
                    try {
                        out.writeObject(getInputField().getText());
                    } catch (IOException e) {
                        close();
                        new Alert(Alert.AlertType.ERROR, "Connection error.").showAndWait();
                    }
                });

                        while (true)
                            outputToWindow((String) in.readObject());
                    } catch (IOException | ClassNotFoundException e) {
                        Platform.runLater(() -> {
                            close();
                            new Alert(Alert.AlertType.ERROR, "Connection error.").showAndWait();
                        });


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

        grid.add(outputArea, 0, 0, 4, 1);
        grid.add(inputField, 0, 1, 3, 1);
        grid.add(enterButton, 3, 1, 1, 1);

        setScene(new Scene(grid));
    }
}
