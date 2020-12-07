public abstract class Role {
    protected final Server server;
    protected String name;
    protected int turnTime;

    public String getName() {
        return name;
    }

    public Role(Server server) {
        this.server = server;
    }

    public abstract void nightAction();

    public abstract void dayAction();

}
