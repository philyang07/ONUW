public class Player {
    private String name;
    private Player votedPlayer;
    private final ClientThread clientThread;


    public String getName() {
        return name;
    }

    public Player getVotedPlayer() {
        return votedPlayer;
    }

    public Player(ClientThread ct) {
        clientThread = ct;
        name = null;
        votedPlayer = null;
    }

    public void startVoting() {
        clientThread.setInVoting(true);
    }

    public void endVoting() {
        clientThread.setInVoting(false);
    }

    public void startDiscussion() {
        clientThread.setInDiscussion(true);
    }

    public void endDiscussion() {
        clientThread.setInDiscussion(false);
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setVotedPlayer(Player votedPlayer) {
        this.votedPlayer = votedPlayer;
    }

    public void printToPlayer(String input) {
        clientThread.sendToClient(input);
    }

    // Blocking method that gets input from a player client
    public String getInput() {
        String oldInput = this.clientThread.getCurrentInput();
        // Wait until found
        while (this.clientThread.getCurrentInput().equals(oldInput)) {
            // Just do something while waiting...
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return this.clientThread.getCurrentInput();
    }


}
