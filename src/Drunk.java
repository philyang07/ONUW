import java.util.ArrayList;

public class Drunk extends Role {
    public Drunk(Server server) {
        super(server);
        this.name = "Drunk";
    }

    @Override
    public void dayAction() {

    }

    @Override
    public void nightAction() {
        server.printToAllExcept(null, "Drunk. Open your eyes.");
        ArrayList<Player> drunks = server.getPlayersByRoleName("Drunk", false);
        if (drunks.size() > 0) {
            Player drunkPlayer = drunks.get(0);
            drunkPlayer.printToPlayer("What card from the center would you like to view? (1-3)");
            int selection = Integer.parseInt(drunkPlayer.getValidInput(Player::validIntFromOneToThree, "Must be an integer from 1 to 3."));
            System.out.println("The role on the card is " + server.getCenterRoles().get(selection-1).getName());

        }
        server.delay(turnTime, false);
        server.printToAllExcept(null, "Drunk. Close your eyes.");
    }
}
