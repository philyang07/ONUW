import java.util.ArrayList;

public class Insomniac extends Role {
    public Insomniac(Server server) {
        super(server);
        turnTime = 5;
    }

    @Override
    public void dayAction() {

    }

    @Override
    public void nightAction() {
        server.printToAllExcept(null, "Insomniac. Open your eyes.");
        ArrayList<Player> insomniacs = server.getPlayersByRoleName("Insomniac", false);
        if (insomniacs.size() > 0) {
            Player insomniacPlayer = insomniacs.get(0);
            insomniacPlayer.printToPlayer("Your current role is " + insomniacPlayer.getNewRole().getName());
        }
        server.delay(turnTime, false);
        server.printToAllExcept(null, "Insomniac. Close your eyes.");
    }
}
