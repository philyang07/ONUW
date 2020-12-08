import java.util.function.Predicate;

public class Player {
    private String name;
    private Player votedPlayer;
    private Role oldRole;
    private boolean isDoppelgangerRobber; // Very special instance, to make sure doesn't wake up during mason/werewolf times
    private Role newRole;

    private final ClientThread clientThread;

    public Role getOldRole() {
        return oldRole;
    }

    // Returns new role if they have a new role, otherwise, return old role
    public Role getNewRole() {
        if (newRole == null)
            return oldRole;
        return newRole;
    }

    public boolean hasNoDoppelgangerRobber() {
        return !isDoppelgangerRobber;
    }

    public void setDoppelgangerRobber(boolean doppelgangerRobber) {
        isDoppelgangerRobber = doppelgangerRobber;
    }

    public String getName() {
        return name;
    }

    public Player getVotedPlayer() {
        return votedPlayer;
    }

    public void setOldRole(Role oldRole) {
        this.oldRole = oldRole;
    }

    public void setNewRole(Role newRole) {
        this.newRole = newRole;
    }

    public Player(ClientThread ct) {
        clientThread = ct;
        name = null;
        votedPlayer = null;
        oldRole = null;
        newRole = null;
        isDoppelgangerRobber = false;
    }

    public void startVoting() {
        clientThread.setInVoting(true);
    }

    public void stopEnteringNames() {
        clientThread.setStillEnteringNames(false);
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
        while (this.clientThread.getCurrentInput().equals(oldInput) || this.clientThread.getCurrentInput().charAt(0) == '/') { // Ignore inputs that begin with a '/'
            // Just do something while waiting...
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return this.clientThread.getCurrentInput();
    }

    // Keep requesting the player for valid input
    public String getValidInput(Predicate<String> validator, String errorMessage) {
        String input = getInput();
        while (!validator.test(input)) {
            printToPlayer(errorMessage);
            input = getInput();
        }
        return input;
    }

    public static boolean validIntFromOneToThree(String input) {
        try {
            int choice = Integer.parseInt(input);
            return choice >= 1 && choice <= 3;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static boolean validYesOrNo(String input) {
        return input.toLowerCase().equals("y") || input.toLowerCase().equals("yes") || input.toLowerCase().equals("n") || input.toLowerCase().equals("no");
    }

}
