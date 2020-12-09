import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

public class Server implements Runnable {
    public static final int DISCUSSION_DURATION = 20;
    public static final int VOTING_DURATION = 20;
    public static final int ADDITIONAL_NAME_ENTERING_TIME = 10;

    private final ArrayList<Player> players;
    private final ArrayList<Role> roleList;
    private ArrayList<Role> centerRoles;
    private final ServerSocket serverSocket;

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

    public void removePlayer(Player player) {
        players.remove(player);
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

    public Server(ServerSocket serverSocket, ArrayList<Role> roleList) {
        this.players = new ArrayList<>();
        this.roleList = roleList;
        this.serverSocket = serverSocket;
    }

    private void closeSockets() {
        for (Player player : players) {
            player.closePlayerClient();
        }
    }

    @Override
    public void run() {
        try {
            acceptConnections();
//        assignRoles();
//        printRoles();
//        performNightActions();
//        printRoles();
            printToAllExcept(null, "The server will close in 10 seconds.");
            delay(10, false);
            closeSockets();
        } catch (IOException e) { // Close the server socket if any errors occur
            if (!serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }

    public void acceptConnections() throws IOException {
        Player player;
        ClientThread ct;
        while (players.size() < roleList.size()-3) { // Number of players is 3 less than the number of roles
            Socket clientSocket = serverSocket.accept();
            ct = new ClientThread(this, clientSocket);
            player = new Player(ct);
            ct.setPlayer(player); // so the ClientThread can access Player for the discussion part
            player.setName("Player " + (players.size()+1));
            players.add(player);
            ct.start();

            // Allow players to enter their names until 10 seconds after the voting period
            ct.setStillEnteringNames(true);
            player.printToPlayer("Welcome " + player.getName() + ". You can change your name if you like via /name <name>");
        }
        serverSocket.close();
        printToAllExcept(null, "You have " + ADDITIONAL_NAME_ENTERING_TIME + " seconds left to change your name");
        delay(ADDITIONAL_NAME_ENTERING_TIME, true);
        for (Player p : players) {
            p.stopEnteringNames();
        }
    }

    public void assignRoles() {
        // Assign to the players based on the role list
        Collections.shuffle(roleList);
        int i = 0;
        for (Player player : players) {
            player.setOldRole(roleList.get(i++));
        }
        centerRoles = new ArrayList<>(roleList.subList(players.size(), roleList.size()));

        // Tell the players what their role is
        for (Player player : players)
            player.printToPlayer("Your role is " + player.getNewRole() + ".");
    }

    public void performNightActions() {
        printToAllExcept(null, "Everyone. Close your eyes.");
        doppelgangerAction();
        werewolfAction();
        minionAction(false);
        masonAction();
        seerAction(false);
        robberAction(false);
        troublemakerAction(false);
        drunkAction(false);
        insomniacAction(false);
        // Add doppelganger-insomniac if playing with doppelganger
        if (getPlayersByRoleName("Doppelganger", false).size() > 0 || centerRoles.contains(Role.DOPPELGANGER)) {
            printToAllExcept(null, "Doppelganger-insomniac. Open your eyes.");
            insomniacAction(true);
            printToAllExcept(null, "Doppelganger-insomniac. Close your eyes.");
        }
        printToAllExcept(null, "Everyone. Open your eyes.");
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
        printToAllExcept(null, "The center roles are:");
        for (Role role : centerRoles)
            printToAllExcept(null, role.getName());
    }

    public void results() {
        HashMap<Player, Integer> voteCounts = new HashMap<>();
        ArrayList<Player> currentWerewolves = new ArrayList<>();
        ArrayList<Player> tanners = getPlayersByRoleName("Tanner", true);

        for (Player player : players) {
            if (player.getNewRole() == Role.WEREWOLF) {
                currentWerewolves.add(player);
            }
            voteCounts.put(player, 0);
        }
        for (Player player : players) {
            if (player.getVotedPlayer() != null)
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
            if (tanners.size() > 0)
                for (Player player : tanners) // Accounting for potential doppelganger-tanner
                    player.printToPlayer("You didn't die and so you didn't win...");
        } else {
            // Hunter action (assume swapping into hunter counts)
            for (Player hunterPlayer : votedOutPlayers) {
                if (hunterPlayer.getNewRole() == Role.HUNTER) {
                    if (hunterPlayer.getOldRole() == Role.DOPPELGANGER) {
                        printToAllExcept(null, "The hunter (doppelganger)'" + hunterPlayer.getName() + "' was voted out!");
                    } else {
                        printToAllExcept(null, "The hunter '" + hunterPlayer.getName() + "' was voted out!");
                    }
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
            }

            if (currentWerewolves.stream().anyMatch(deadPlayers::contains)) {
                printToAllExcept(null, "At least one werewolf died! Villagers win!");
            } else {
                printToAllExcept(null, "No werewolves died! Werewolves win!");
            }

            // Tanner action
            if (tanners.size() > 0 && deadPlayers.contains(tanners.get(0)))
                for (Player player : deadPlayers) {
                    if (player.getNewRole() == Role.TANNER) {
                        if (player.getOldRole() == Role.DOPPELGANGER) {
                            printToAllExcept(null, "The tanner (originally doppelganger)'" + tanners.get(0).getName() + "' died, so they win!");
                        } else {
                            printToAllExcept(null, "The tanner '" + tanners.get(0).getName() + "' died, so they win!");
                        }
                    }
                }
        }
    }

    public void doppelgangerAction() {
        printToAllExcept(null, "Doppelganger. Open your eyes.");
        ArrayList<Player> doppelgangers = getPlayersByRoleName("Doppelganger", false);
        if (doppelgangers.size() > 0) {
            Player doppelgangerPlayer = doppelgangers.get(0);
            doppelgangerPlayer.printToPlayer("Choose someone whose role you would like to copy. Type /players to get the list of players' names.");
            Player candidatePlayer = getPlayerFromInputExceptThemselves(doppelgangerPlayer);
            doppelgangerPlayer.printToPlayer("You chose " + candidatePlayer.getName() + " whose role was " + candidatePlayer.getOldRole().getName() + ".");
            doppelgangerPlayer.setNewRole(candidatePlayer.getOldRole());
            switch (candidatePlayer.getOldRole()) {
                case MINION:
                    minionAction(true);
                    break;
                case SEER:
                    seerAction(true);
                    break;
                case ROBBER:
                    doppelgangerPlayer.setDoppelgangerRobber(true);
                    robberAction(true);
                    break;
                case TROUBLEMAKER:
                    troublemakerAction(true);
                    break;
                case DRUNK:
                    drunkAction(true);
                    break;
            }
        } else {
            delay(Role.DOPPELGANGER.getTurnTime(), false);
        }

        printToAllExcept(null, "Doppelganger. Close your eyes.");
    }

    public void werewolfAction() {
        printToAllExcept(null, "Werewolves. Open your eyes.");

        ArrayList<Player> werewolves = getPlayersByRoleName("Werewolf", false);
        ArrayList<Player> doppelgangers = getPlayersByRoleName("Doppelganger", false);
        if (doppelgangers.size() > 0 && doppelgangers.get(0).getNewRole() == Role.WEREWOLF && doppelgangers.get(0).hasNoDoppelgangerRobber()) { // We don't want doppelganger-robber to wake up
            werewolves.add(doppelgangers.get(0));
        }
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
            } else if (werewolves.size() == 3) { // Account for doppelganger-werewolf
                for (Player werewolfPlayer : werewolves) {
                    werewolfPlayer.printToPlayer("There are three werewolves (including a doppelganger-werewolf). The other werewolves are:");
                    for (Player otherWerewolf : werewolves) {
                        if (otherWerewolf != werewolfPlayer)
                            werewolfPlayer.printToPlayer(otherWerewolf.getName());
                    }
                }
            }
            delay(Role.WEREWOLF.getTurnTime(), false);
        }

        printToAllExcept(null, "Werewolves. Close your eyes.");
    }

    public void minionAction(boolean doppelgangerTurn) {
        if (!doppelgangerTurn)
            printToAllExcept(null, "Minion, open your eyes.");
        ArrayList<Player> minions = getPlayersByRoleName("Minion", false);
        ArrayList<Player> werewolves = getPlayersByRoleName("Werewolf", false);
        ArrayList<Player> doppelgangers = getPlayersByRoleName("Doppelganger", false);
        // Check for doppelganger-werewolf (NOT DOPPELGANGER-ROBBER!)
        if (doppelgangers.size() > 0 && doppelgangers.get(0).getNewRole() == Role.WEREWOLF && doppelgangers.get(0).hasNoDoppelgangerRobber()) {
            werewolves.add(doppelgangers.get(0));
        }

        if (minions.size() > 0) {
            Player minionPlayer = minions.get(0);
            if (doppelgangerTurn) // If doppelganger-minion's turn
                minionPlayer = getPlayersByRoleName("Doppelganger", false).get(0);
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
        if (!doppelgangerTurn)
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
        } else if (masons.size() == 3) { // Account for doppelganger-mason
            for (Player masonPlayer : masons) {
                masonPlayer.printToPlayer("There are three masons (including a doppelganger-mason). The other masons are:");
                for (Player otherMason : masons) {
                    if (otherMason != masonPlayer)
                        masonPlayer.printToPlayer(otherMason.getName());
                }
            }
        }

        delay(Role.MASON.getTurnTime(), false);
        printToAllExcept(null, "Masons, close your eyes.");
    }

    public void seerAction(boolean doppelgangerTurn) {
        if (!doppelgangerTurn)
            printToAllExcept(null, "Seer. Open your eyes.");
        ArrayList<Player> seers = getPlayersByRoleName("Seer", false);
        if (seers.size() > 0) {
            Player seerPlayer = seers.get(0);
            if (doppelgangerTurn) // Doppelganger-seer's turn
                seerPlayer = getPlayersByRoleName("Doppelganger", false).get(0);
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
        if (!doppelgangerTurn)
            printToAllExcept(null, "Seer. Close your eyes.");
    }

    public void robberAction(boolean doppelgangerTurn) {
        if (!doppelgangerTurn)
            printToAllExcept(null, "Robber. Open your eyes.");
        ArrayList<Player> robbers = getPlayersByRoleName("Robber", false);
        if (robbers.size() > 0) {
            Player robberPlayer = robbers.get(0);
            if (doppelgangerTurn) // Doppelganger-robber's turn
                robberPlayer = getPlayersByRoleName("Doppelganger", false).get(0);
            robberPlayer.printToPlayer("Choose a player to swap your role with. Type /players to view who the other players are.");
            Player candidatePlayer = getPlayerFromInputExceptThemselves(robberPlayer);

            // Swap the new roles
            robberPlayer.setNewRole(candidatePlayer.getNewRole());
            candidatePlayer.setNewRole(Role.ROBBER);

            robberPlayer.printToPlayer("Your new role is " + robberPlayer.getNewRole().getName());

        } else {
            delay(Role.ROBBER.getTurnTime(), false);
        }
        if (!doppelgangerTurn)
            printToAllExcept(null, "Robber. Close your eyes.");
    }

    public void troublemakerAction(boolean doppelgangerTurn) {
        if (!doppelgangerTurn)
            printToAllExcept(null, "Troublemaker. Open your eyes.");
        ArrayList<Player> troublemakers = getPlayersByRoleName("Troublemaker", false);
        if (troublemakers.size() > 0) {
            Player troublemakerPlayer = troublemakers.get(0);
            if (doppelgangerTurn) // Account for doppelganger-troublemaker
                troublemakerPlayer = getPlayersByRoleName("Doppelganger", false).get(0);
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
        if (!doppelgangerTurn)
            printToAllExcept(null, "Troublemaker. Close your eyes.");
    }

    public void drunkAction(boolean doppelgangerTurn) {
        if (!doppelgangerTurn)
            printToAllExcept(null, "Drunk. Open your eyes.");
        ArrayList<Player> drunks = getPlayersByRoleName("Drunk", false);
        if (drunks.size() > 0) {
            Player drunkPlayer = drunks.get(0);
            if (doppelgangerTurn) // Account for doppelganger-drunk
                drunkPlayer = getPlayersByRoleName("Doppelganger", false).get(0);
            drunkPlayer.printToPlayer("What card from the center would you like to swap with? (1-3)");
            int selection = Integer.parseInt(drunkPlayer.getValidInput(Player::validIntFromOneToThree, "Must be an integer from 1 to 3."));
            Role previousRole = drunkPlayer.getNewRole();
            drunkPlayer.setNewRole(centerRoles.get(selection-1));
            centerRoles.set(selection-1, previousRole);
            drunkPlayer.printToPlayer("You swapped your role with that of card " + selection + "...");
        } else {
            delay(Role.DRUNK.getTurnTime(), false);
        }
        if (!doppelgangerTurn)
            printToAllExcept(null, "Drunk. Close your eyes.");
    }

    public void insomniacAction(boolean doppelgangerTurn) {
        if (!doppelgangerTurn)
            printToAllExcept(null, "Insomniac. Open your eyes.");
        ArrayList<Player> insomniacs = getPlayersByRoleName("Insomniac", false);
        if (insomniacs.size() > 0) {
            Player insomniacPlayer = insomniacs.get(0);
            if (doppelgangerTurn) // Account for doppelganger-insomniac
                insomniacPlayer = getPlayersByRoleName("Doppelganger", false).get(0);
            insomniacPlayer.printToPlayer("Your current role is " + insomniacPlayer.getNewRole().getName());
        }
        delay(Role.INSOMNIAC.getTurnTime(), false);
        if (!doppelgangerTurn)
            printToAllExcept(null, "Insomniac. Close your eyes.");
    }

    public static void main(String[] args) {
//        new Server(3333);
    }
}
