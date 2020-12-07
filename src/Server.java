import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    public static final int DISCUSSION_DURATION = 20;
    public static final int VOTING_DURATION = 20;
    public static final int ADDITIONAL_NAME_ENTERING_TIME = 10;
    public static final int NUM_PLAYERS = 3;

    private final int portNumber;
    private final ArrayList<Player> players;
    private final ArrayList<Role> centerRoles;

    public void delay(int secs, boolean showHalfTime) {
        try {
            Thread.sleep(secs*500);
            if (showHalfTime)
                printToAllExcept(null, secs/2 + " seconds remaining.");
            Thread.sleep(secs*500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void printToAllExcept(Player p, String message) {
        for (Player player : players) {
            if (player != p)
                player.printToPlayer(message);
        }
    }

    public ArrayList<Role> getCenterRoles() {
        return centerRoles;
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

    // Return an ArrayList of players by a given role depending on if their new or old rule is wanted
    public ArrayList<Player> getPlayersByRoleName(String roleName, boolean newRole) {
        ArrayList<Player> playersWithRole = new ArrayList<>();
        for (Player player : players) {
            if (newRole && player.getNewRole().getName().equals(roleName)) {
                playersWithRole.add(player);
            } else if (!newRole && player.getOldRole().getName().equals(roleName)) {
                playersWithRole.add(player);
            }
        }
        return playersWithRole;
    }

    // Get a player to input a valid player name that isn't their own
    public Player getPlayerFromInputExceptThemselves(Player player) {
        String input = player.getInput();
        Player candidatePlayer = getPlayer(input);
        while (candidatePlayer == null || candidatePlayer == player) {
            player.printToPlayer("Invalid selection. Type /players to view who the players are.");
            input = player.getInput();
            candidatePlayer = getPlayer(input);
        }
        return candidatePlayer;
    }

    public String getAllPlayerNames() {
        String playerNames = "";
        for (Player player : players.subList(0, players.size()-1)) {
            playerNames += player.getName();
            playerNames += ", ";
        }
        playerNames += players.get(players.size()-1).getName();
        return playerNames;
    }

    public Server(int portNumber) {
        this.portNumber = portNumber;
        this.players = new ArrayList<>();
        this.centerRoles = new ArrayList<>();

        acceptConnections();
        voting();
        while (true) {}
    }

    public void acceptConnections() {
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            Player player;
            ClientThread ct;
            for (int i = 0; i < NUM_PLAYERS; i++) {
                Socket clientSocket = serverSocket.accept();
                ct = new ClientThread(this, clientSocket);
                player = new Player(ct);
                ct.setPlayer(player); // so the ClientThread can access Player for the discussion part
                player.setName("Player " + (i+1));
                players.add(player);
                ct.start();

                // Allow players to enter their names until 10 seconds after the voting period
                ct.setStillEnteringNames(true);
                player.printToPlayer("Welcome " + player.getName() + ". You can change your name if you like via /name <name>");
            }
            printToAllExcept(null, "You have " + ADDITIONAL_NAME_ENTERING_TIME + " seconds left to change your name");
            delay(ADDITIONAL_NAME_ENTERING_TIME, true);
            for (Player p : players) {
                p.stopEnteringNames();
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
        printToAllExcept(null, "Started discussion. " + DISCUSSION_DURATION + " seconds remaining.");
        delay(DISCUSSION_DURATION, true);
        printToAllExcept(null, "Ended discussion.");
        for (Player player : players) {
            player.endDiscussion();
        }
    }

    public void voting() {
        for (Player player : players) {
            player.startVoting();
        }
        printToAllExcept(null, "Started voting. " + VOTING_DURATION + " seconds remaining.");
        delay(VOTING_DURATION, true);
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
