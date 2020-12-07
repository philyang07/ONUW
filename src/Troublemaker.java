import java.util.ArrayList;

public class Troublemaker extends Role {
    public Troublemaker(Server server) {
        super(server);
        this.name = "Troublemaker";
    }

    @Override
    public void dayAction() {

    }

    @Override
    public void nightAction() {
        server.printToAllExcept(null, "Troublemaker. Open your eyes.");
        ArrayList<Player> troublemakers = server.getPlayersByRoleName("troublemaker", false);
        if (troublemakers.size() > 0) {
            Player troublemakerPlayer = troublemakers.get(0);
            // Choose first player
            troublemakerPlayer.printToPlayer("Choose the first player. Type /players to view who the other players are.");
            Player firstPlayer = server.getPlayerFromInputExceptThemselves(troublemakerPlayer);
            // Choose the second player
            troublemakerPlayer.printToPlayer("Choose the second player. Type /players to view who the other players are.");
            String candidatePlayerName = troublemakerPlayer.getInput();
            while (candidatePlayerName.equals(firstPlayer.getName()) || candidatePlayerName.equals(troublemakerPlayer.getName()) || !server.validPlayerName(candidatePlayerName)) {
                troublemakerPlayer.printToPlayer("Invalid selection. Type /players to view who the players are.");
                candidatePlayerName = troublemakerPlayer.getInput();
            }
            Player secondPlayer = server.getPlayer(candidatePlayerName);
            // Swap the players' roles
            firstPlayer.setNewRole(secondPlayer.getNewRole());
            secondPlayer.setNewRole(firstPlayer.getNewRole());
            troublemakerPlayer.printToPlayer("You swapped the roles of " + firstPlayer.getName() + " and " + secondPlayer.getName());
        } else {
            server.delay(turnTime, false);
        }
    }
}
