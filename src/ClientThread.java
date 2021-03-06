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
    private final Socket clientSocket;

    private boolean inDiscussion;
    private boolean inVoting;
    private boolean stillEnteringNames;

    public void setStillEnteringNames(boolean stillEnteringNames) {
        this.stillEnteringNames = stillEnteringNames;
    }

    public void setInDiscussion(Boolean inDiscussion) {
        this.inDiscussion = inDiscussion;
    }

    public void setInVoting(Boolean inVoting) {
        this.inVoting = inVoting;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void closeClientThread() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClientThread(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
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
//            e.printStackTrace();
        }
    }

    // Sends a message to everyone (including the current player)
    private void chat(String msg) {
        server.printToAllExcept(null, player.getName() + ": " + msg);
    }

    private void processInput(String input) {
        // Process commands first
        if (input.equals("/players")) {
            player.printToPlayer("The players are: " + server.getAllPlayerNames());
        } else if (inDiscussion) {
            chat(input);
        } else if (inVoting) {
            if (server.validPlayerName(input) && server.getPlayer(input) != player) {
                player.setVotedPlayer(server.getPlayer(input));
                player.printToPlayer("You voted for " + input + ". Type /unvote to remove your vote.");
            } else if (input.equals("/unvote") && player.getVotedPlayer() != null) {
                player.printToPlayer("You removed your vote for " + player.getVotedPlayer().getName() + ".");
                player.setVotedPlayer(null);
            } else {
                player.printToPlayer("Invalid name provided.");
            }
        } else if (stillEnteringNames) {
            // Check if input matches "/name <name>"
            if (input.matches("/name [a-zA-z0-9][a-zA-Z0-9 ]*")) {
                String nameCandidate = input.substring(6).stripTrailing();
                if (nameCandidate.equals(player.getName())) {
                    player.printToPlayer("Name provided is same as original.");
                } else if (server.validPlayerName(nameCandidate)) {
                    player.printToPlayer("Name already taken.");
                } else {
                    player.setName(nameCandidate);
                    player.printToPlayer("You changed your name to " + player.getName());
                }
            // Otherwise can just chat as per normal
            } else {
                chat(input);
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                currentInput = (String) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                if (stillEnteringNames)
                    server.removePlayer(player);
                server.printToAllExcept(player, player.getName() + " left the game. Restart the server if game has started.");
                break;
            }
            processInput(currentInput);
        }
    }
}
