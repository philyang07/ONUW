import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Client {
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Scanner scanner;
    private ServerOutputProcessor sop;

    public Client(String hostName, int portNumber) {
        Socket socket = null;
        try {
            socket = new Socket(hostName, portNumber);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (SocketException e) {
            System.out.println("Couldn't connect to server.");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        scanner = new Scanner(System.in);
        sop = new ServerOutputProcessor(in);
        sop.start();

        // Send any input from the client to the server
        while (true) {
            try {
                out.writeObject(scanner.nextLine());
            } catch (SocketException e) {
                System.out.println("Server closed.");
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new Client("localhost", 3333);
    }
}
