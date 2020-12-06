import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketException;

public class ServerOutputProcessor extends Thread {
    private ObjectInputStream in;

    public ServerOutputProcessor(ObjectInputStream in) {
        this.in = in;
    }

    @Override
    public void run() {
        // keep printing out server output
        while (true) {
            try {
                System.out.println(in.readObject());
            } catch (SocketException e) {
                System.out.println("Exiting thread because of server closure.");
                break;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
