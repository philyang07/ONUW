import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    public static int DISCUSSION_DURATION = 20;
    public static int VOTING_DURATION = 20;

    private final int portNumber;
    private final ArrayList<Player> players;

    public void printToAllExcept(Player p, String message) {
        for (Player player : players) {
            if (player != p)
                player.printToPlayer(message);
        }
    }

    public boolean validPlayerName(String name) {
        for (Player player : players) {
            if (player.getName().equals(name))
                return true;
        }
        return false;
    }

    public Player getPlayer(String name) {
        for (Player player : players) {
            if (player.getName().equals(name))
                return player;
        }
        return null;
    }

    public Server(int portNumber) {
        this.portNumber = portNumber;
        this.players = new ArrayList<>();
        acceptConnections();
        for (Player player : players) {
            player.printToPlayer("What is your name?");
            String name = player.getInput();
            player.setName(name);
        }
        voting();
        while (true) {}
    }

    public void acceptConnections() {
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            Player player;
            ClientThread ct;
            for (int i = 0; i < 3; i++) {
                Socket clientSocket = serverSocket.accept();
                ct = new ClientThread(this, clientSocket);
                player = new Player(ct);
                ct.setPlayer(player); // so the ClientThread can access Player for the discussion part
                player.setName("Player " + (i+1));
                players.add(player);
                ct.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void assignRoles() {

    }

    public void performNightActions() {

    }

    public void performDayActions() {

    }

    public void discussion() {
        for (Player player : players) {
            player.startDiscussion();
        }
        printToAllExcept(null, "Started discussion.");
        try {
            Thread.sleep(DISCUSSION_DURATION*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        printToAllExcept(null, "Ended discussion.");
        for (Player player : players) {
            player.endDiscussion();
        }
    }

    public void voting() {
        for (Player player : players) {
            player.startVoting();
        }
        printToAllExcept(null, "Started voting.");
        try {
            Thread.sleep(VOTING_DURATION*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (Player player : players) {
            player.endVoting();
        }
        printToAllExcept(null, "Ended voting.");
        for (Player player : players) {
            if (player.getVotedPlayer() != null) {
                printToAllExcept(null, player.getName() + " voted for " + player.getVotedPlayer().getName() + ".");
            } else {
                printToAllExcept(null, player.getName() + " didn't for anyone.");
            }
        }
    }

    public void results() {

    }

    public static void main(String[] args) {
        new Server(3333);
    }
}
