import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

public class Server {
    public static final int DISCUSSION_DURATION = 20;
    public static final int VOTING_DURATION = 20;
    public static final int ADDITIONAL_NAME_ENTERING_TIME = 10;
    public static final int NUM_PLAYERS = 5;

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
        StringBuilder playerNames = new StringBuilder();
        for (Player player : players.subList(0, players.size()-1)) {
            playerNames.append(player.getName());
            playerNames.append(", ");
        }
        playerNames.append(players.get(players.size() - 1).getName());
        return playerNames.toString();
    }

    public Server(int portNumber) {
        this.portNumber = portNumber;
        this.players = new ArrayList<>();
        this.centerRoles = new ArrayList<>();

        acceptConnections();
        assignRoles();
        printRoles();
        performNightActions();
        discussion();
        printRoles();

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
        // Assign the roles to the players
        Role[] roles = {Role.WEREWOLF, Role.MINION, Role.INSOMNIAC, Role.ROBBER, Role.DRUNK, Role.TROUBLEMAKER, Role.SEER, Role.VILLAGER};
        List<Role> roleList = Arrays.asList(roles);
        players.get(0).setOldRole(roleList.get(0));
        players.get(1).setOldRole(roleList.get(1));
        players.get(2).setOldRole(roleList.get(2));
        players.get(3).setOldRole(roleList.get(3));
        players.get(4).setOldRole(roleList.get(4));
        centerRoles.add(roleList.get(5));
        centerRoles.add(roleList.get(6));
        centerRoles.add(roleList.get(7));

        // Tell the players what their role is
        for (Player player : players)
            player.printToPlayer("Your role is " + player.getNewRole() + ".");
    }

    public void performNightActions() {
        printToAllExcept(null, "Everyone. Close your eyes.");
        werewolfAction();
        minionAction();
        masonAction();
        seerAction();
        robberAction();
        troublemakerAction();
        drunkAction();
        insomniacAction();
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
        printToAllExcept(null, "Vote for no-one if you think there are no werewolves in the game.");
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

    public void printRoles() {
        printToAllExcept(null, "Everyone's roles are:");
        for (Player player : players)
            printToAllExcept(null, player.getName() + ": " + player.getNewRole().getName());
    }

    public void results() {
        HashMap<Player, Integer> voteCounts = new HashMap<>();
        ArrayList<Player> currentWerewolves = new ArrayList<>();
        for (Player player : players) {
            if (player.getNewRole() == Role.WEREWOLF) {
                currentWerewolves.add(player);
            }
            voteCounts.put(player, 0);
        }
        for (Player player : players) {
            voteCounts.put(player.getVotedPlayer(), voteCounts.get(player.getVotedPlayer())+1);
        }
        int maxVotes = Collections.max(voteCounts.values());
        List<Player> votedOutPlayers = voteCounts.entrySet().stream()
                .filter(i -> i.getValue() == maxVotes)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        ArrayList<Player> deadPlayers = new ArrayList<>(votedOutPlayers);

        if (votedOutPlayers.size() == players.size() || votedOutPlayers.size() == 0) { // No-one was voted out
            printToAllExcept(null, "Either everyone received 1 vote or no-one received any votes.");
            if (currentWerewolves.size() > 0) {
                printToAllExcept(null, "There are werewolves in the game (not in the center) and so the village loses!");
            } else {
                printToAllExcept(null, "There are no werewolves in the game so the village wins!");
            }
        } else {
            // Hunter action (assume swapping into hunter counts)
            if (votedOutPlayers.stream().map(Player::getNewRole).filter(i -> i == Role.HUNTER).count() == 1) {
                Player hunterPlayer = getPlayersByRoleName("Hunter", true).get(0);
                printToAllExcept(null, "The hunter '" + hunterPlayer.getName() + "' was voted out!");
                if (hunterPlayer.getVotedPlayer() != null) {
                    printToAllExcept(null, "Their target " + hunterPlayer.getVotedPlayer() + " will also die.");
                    if (!votedOutPlayers.contains(hunterPlayer.getVotedPlayer())) {
                        deadPlayers.add(hunterPlayer.getVotedPlayer());
                    } else {
                        printToAllExcept(null, "However, they were already voted out.");
                    }
                } else {
                    printToAllExcept(null, "But they didn't target anyone... So no-one else dies.");
                }
            }

            if (currentWerewolves.stream().filter(deadPlayers::contains).count() > 0) {
                printToAllExcept(null, "At least one werewolf died! Villagers win!");
            } else {
                printToAllExcept(null, "No werewolves died! Werewolves win!");
            }

        }


    }

    public void werewolfAction() {
        printToAllExcept(null, "Werewolves, open your eyes.");

        ArrayList<Player> werewolves = getPlayersByRoleName("Werewolf", false);
        if (werewolves.size() == 1) {
            Player werewolfPlayer = werewolves.get(0);
            werewolfPlayer.printToPlayer("You are the lone wolf. Would you like to view a center card? (y/n)");
            String choice = werewolfPlayer.getValidInput(Player::validYesOrNo, "Must be y/n or yes/no");
            if (choice.equals("y") || choice.equals("yes")) {
                werewolfPlayer.printToPlayer("Which card would you like to view (1-3)?");
                // Get a valid selection from the werewolf
                int selection = Integer.parseInt(werewolfPlayer.getValidInput(Player::validIntFromOneToThree, "Must be an integer from 1 to 3."));
                werewolfPlayer.printToPlayer("The role on the card is " + getCenterRoles().get(selection-1).getName());
            }
        } else {
            if (werewolves.size() == 2) {
                for (Player werewolfPlayer : werewolves) {
                    werewolfPlayer.printToPlayer("There are two werewolves. The other werewolf is " + werewolves.get(1-werewolves.indexOf(werewolfPlayer)).getName() + ".");
                }
            }
            delay(Role.WEREWOLF.getTurnTime(), false);
        }

        printToAllExcept(null, "Werewolves, close your eyes.");
    }

    public void minionAction() {
        printToAllExcept(null, "Minion, open your eyes.");

        ArrayList<Player> minions = getPlayersByRoleName("Minion", false);
        ArrayList<Player> werewolves = getPlayersByRoleName("Werewolf", false);

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
        delay(Role.MINION.getTurnTime(), false);
        printToAllExcept(null, "Minion, close your eyes.");
    }

    public void masonAction() {
        printToAllExcept(null, "Masons, open your eyes.");

        ArrayList<Player> masons = getPlayersByRoleName("Mason", false);
        if (masons.size() == 1) {
            Player masonPlayer = masons.get(0);
            masonPlayer.printToPlayer("You are the only mason.");
        } else if (masons.size() == 2) {
            for (Player masonPlayer : masons) {
                masonPlayer.printToPlayer("There are two masons. The other mason is " + masons.get(1-masons.indexOf(masonPlayer)).getName() + ".");
            }
        }
        delay(Role.MASON.getTurnTime(), false);
        printToAllExcept(null, "Masons, close your eyes.");
    }

    public void seerAction() {
        printToAllExcept(null, "Seer. Open your eyes.");
        ArrayList<Player> seers = getPlayersByRoleName("Seer", false);
        if (seers.size() > 0) {
            Player seerPlayer = seers.get(0);
            seerPlayer.printToPlayer("Do you want to view a card from the center or another player's card (center/player)");
            boolean chooseFromCenter = seerPlayer.getValidInput(input -> input.toLowerCase().equals("center") || input.toLowerCase().equals("player"), "Type 'center' or 'player'").equals("center");
            if (chooseFromCenter) {
                seerPlayer.printToPlayer("What card from the center would you like to view? (1-3)");
                int selection = Integer.parseInt(seerPlayer.getValidInput(Player::validIntFromOneToThree, "Must be an integer from 1 to 3."));
                seerPlayer.printToPlayer("The role on the card is " + getCenterRoles().get(selection-1).getName());
            } else {
                seerPlayer.printToPlayer("Who's role do you want revealed? Type /players to see the list of players.");
                Player candidatePlayer = getPlayerFromInputExceptThemselves(seerPlayer);
                seerPlayer.printToPlayer(candidatePlayer.getName() + "'s role is " + candidatePlayer.getNewRole().getName());
            }
        } else {
            delay(Role.SEER.getTurnTime(), false);
        }
        printToAllExcept(null, "Seer. Close your eyes.");
    }

    public void robberAction() {
        printToAllExcept(null, "Robber. Open your eyes.");
        ArrayList<Player> robbers = getPlayersByRoleName("Robber", false);
        if (robbers.size() > 0) {
            Player robberPlayer = robbers.get(0);
            robberPlayer.printToPlayer("Choose a player to swap your role with. Type /players to view who the other players are.");
            Player candidatePlayer = getPlayerFromInputExceptThemselves(robberPlayer);

            // Swap the new roles
            robberPlayer.setNewRole(candidatePlayer.getNewRole());
            candidatePlayer.setNewRole(Role.ROBBER);

            robberPlayer.printToPlayer("Your new role is " + robberPlayer.getNewRole().getName());

        } else {
            delay(Role.ROBBER.getTurnTime(), false);
        }
        printToAllExcept(null, "Robber. Close your eyes.");
    }

    public void troublemakerAction() {
        printToAllExcept(null, "Troublemaker. Open your eyes.");
        ArrayList<Player> troublemakers = getPlayersByRoleName("Troublemaker", false);
        if (troublemakers.size() > 0) {
            Player troublemakerPlayer = troublemakers.get(0);
            // Choose first player
            troublemakerPlayer.printToPlayer("Choose the first player. Type /players to view who the other players are.");
            Player firstPlayer = getPlayerFromInputExceptThemselves(troublemakerPlayer);
            // Choose the second player
            troublemakerPlayer.printToPlayer("Choose the second player. Type /players to view who the other players are.");
            String candidatePlayerName = troublemakerPlayer.getInput();
            while (candidatePlayerName.equals(firstPlayer.getName()) || candidatePlayerName.equals(troublemakerPlayer.getName()) || !validPlayerName(candidatePlayerName)) {
                troublemakerPlayer.printToPlayer("Invalid selection. Type /players to view who the players are.");
                candidatePlayerName = troublemakerPlayer.getInput();
            }
            Player secondPlayer = getPlayer(candidatePlayerName);
            // Swap the players' roles
            Role firstPlayerPreviousRole = firstPlayer.getNewRole();
            firstPlayer.setNewRole(secondPlayer.getNewRole());
            secondPlayer.setNewRole(firstPlayerPreviousRole);
            troublemakerPlayer.printToPlayer("You swapped the roles of " + firstPlayer.getName() + " and " + secondPlayer.getName());
        } else {
            delay(Role.TROUBLEMAKER.getTurnTime(), false);
        }
    }

    public void drunkAction() {
        printToAllExcept(null, "Drunk. Open your eyes.");
        ArrayList<Player> drunks = getPlayersByRoleName("Drunk", false);
        if (drunks.size() > 0) {
            Player drunkPlayer = drunks.get(0);
            drunkPlayer.printToPlayer("What card from the center would you like to swap with? (1-3)");
            int selection = Integer.parseInt(drunkPlayer.getValidInput(Player::validIntFromOneToThree, "Must be an integer from 1 to 3."));
            drunkPlayer.setNewRole(centerRoles.get(selection-1));
            centerRoles.set(selection-1, Role.DRUNK);
            drunkPlayer.printToPlayer("You swapped your role with that of card " + selection + "...");
        }
        delay(Role.DRUNK.getTurnTime(), false);
        printToAllExcept(null, "Drunk. Close your eyes.");
    }

    public void insomniacAction() {
        printToAllExcept(null, "Insomniac. Open your eyes.");
        ArrayList<Player> insomniacs = getPlayersByRoleName("Insomniac", false);
        if (insomniacs.size() > 0) {
            Player insomniacPlayer = insomniacs.get(0);
            insomniacPlayer.printToPlayer("Your current role is " + insomniacPlayer.getNewRole().getName());
        }
        delay(Role.INSOMNIAC.getTurnTime(), false);
        printToAllExcept(null, "Insomniac. Close your eyes.");
    }

    public static void main(String[] args) {
        new Server(3333);
    }
}
