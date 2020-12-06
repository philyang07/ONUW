import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientThread extends Thread {
    private Player player;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String currentInput;
    private final Server server;
    private boolean inDiscussion;
    private boolean inVoting;

    public void setInDiscussion(Boolean inDiscussion) {
        this.inDiscussion = inDiscussion;
    }

    public void setInVoting(Boolean inVoting) {
        this.inVoting = inVoting;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public ClientThread(Server server, Socket clientSocket) {
        this.server = server;
        this.currentInput = "";
        try {
            in = new ObjectInputStream(clientSocket.getInputStream());
            out = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        inDiscussion = false;
    }

    public String getCurrentInput() {
        return currentInput;
    }

    public void sendToClient(String message) {
        try {
            out.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processInput(String input) {
        if (inDiscussion) {
            server.printToAllExcept(player, player.getName() + ": " + input);
        } else if (inVoting) {
            if (server.validPlayerName(input) && server.getPlayer(input) != player) {
                player.setVotedPlayer(server.getPlayer(input));
                player.printToPlayer("You voted for " + input);
            } else {
                player.printToPlayer("Invalid name provided.");
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                currentInput = (String) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            processInput(currentInput);
        }
    }
}
