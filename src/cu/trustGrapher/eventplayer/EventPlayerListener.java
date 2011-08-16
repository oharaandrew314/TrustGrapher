package cu.trustGrapher.eventplayer;

public interface EventPlayerListener {
    
    public void addEventPlayer(EventPlayer eventThread);

    public void goToIndex(int i);
    
    public EventPlayer getEventPlayer();
}
