import java.util.ArrayList;

public class Robber extends Role {
    public Robber(Server server) {
        super(server);
        this.name = "Robber";
        turnTime = 7;
    }

    @Override
    public void dayAction() {

    }

    @Override
    public void nightAction() {
        server.printToAllExcept(null, "Robber. Open your eyes.");
        ArrayList<Player> robbers = server.getPlayersByRoleName("Robber", false);
        if (robbers.size() > 0) {
            Player robberPlayer = robbers.get(0);
            robberPlayer.printToPlayer("Choose a player to swap your role with. Type /players to view who the other players are.");
            Player candidatePlayer = server.getPlayerFromInputExceptThemselves(robberPlayer);

            // Swap the new roles
            robberPlayer.setNewRole(candidatePlayer.getNewRole());
            candidatePlayer.setNewRole(robberPlayer.getNewRole());

            robberPlayer.printToPlayer("Your new role is " + robberPlayer.getNewRole().getName());

        } else {
            server.delay(turnTime, false);
        }
        server.printToAllExcept(null, "Robber. Close your eyes.");
    }
}
