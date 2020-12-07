import java.util.ArrayList;

public class Werewolf extends Role {
    public Werewolf(Server server) {
        super(server);
        this.name = "Werewolf";
        turnTime = 8;
    }

    @Override
    public void dayAction() {

    }

    @Override
    public void nightAction() {
        server.printToAllExcept(null, "Werewolves, open your eyes.");

        ArrayList<Player> werewolves = server.getPlayersByRoleName("Werewolf", false);
        if (werewolves.size() == 1) {
            Player werewolfPlayer = werewolves.get(0);
            werewolfPlayer.printToPlayer("You are the lone wolf. Would you like to view a center card? (y/n)");
            if (werewolfPlayer.getInput().toLowerCase().equals("y")) {
                werewolfPlayer.printToPlayer("Which card would you like to view (1-3)?");
                // Get a valid selection from the werewolf
                int selection = Integer.parseInt(werewolfPlayer.getValidInput(Player::validIntFromOneToThree, "Must be an integer from 1 to 3."));
                System.out.println("The role on the card is " + server.getCenterRoles().get(selection-1).getName());
            }
        } else {
            if (werewolves.size() == 2) {
                for (Player werewolfPlayer : werewolves) {
                    werewolfPlayer.printToPlayer("There are two werewolves. The other werewolf is " + werewolves.get(1-werewolves.indexOf(werewolfPlayer)).getName() + ".");
                }
            }
            server.delay(turnTime, false);
        }

        server.printToAllExcept(null, "Werewolves, close your eyes.");
    }

}
