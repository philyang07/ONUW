public enum Role {
    WEREWOLF ("Werewolf", 8),
    MINION ("Minion", 5),
    MASON ("Mason", 5),
    SEER ("Seer", 8),
    ROBBER ("Robber", 8),
    TROUBLEMAKER ("Troublemaker", 8),
    DRUNK ("Drunk", 5),
    INSOMNIAC ("Insomniac", 5),
    VILLAGER ("Villager", 0),
    TANNER ("Tanner", 0),
    HUNTER ("Hunter", 0),
    DOPPELGANGER("Doppelganger", 0);

    private final String name;
    private final int turnTime;

    Role(String name, int turnTime) {
        this.name = name;
        this.turnTime = turnTime;
    }

    public int getTurnTime() {
        return turnTime;
    }

    public String getName() {
        return name;
    }
}
