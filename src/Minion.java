import java.util.ArrayList;

public class Minion extends Role {

    public Minion(Server server) {
        super(server);
        this.name = "Minion";
        this.turnTime = 5;
    }

    @Override
    public void dayAction() {

    }

    @Override
    public void nightAction() {
        server.printToAllExcept(null, "Minion, open your eyes.");

        ArrayList<Player> minions = server.getPlayersByRoleName("Minions", false);
        ArrayList<Player> werewolves = server.getPlayersByRoleName("Werewolf", false);

        if (minions.size() > 0) {
            Player minionPlayer = minions.get(0);
            if (werewolves.size() > 0) {
                minionPlayer.printToPlayer("The werewolves are:");
                for (Player player : werewolves) {
                    minionPlayer.printToPlayer((werewolves.indexOf(player)+1) + ". " + player.getName());
                }
            } else {
                minionPlayer.printToPlayer("There are no werewolves in the game. You win if a villager dies.");
            }
        }
        server.delay(turnTime, false);
        server.printToAllExcept(null, "Minion, close your eyes.");
    }
}
