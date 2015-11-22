import java.util.LinkedList;
import java.util.List;

public class Event {
    private int id;
    private int pid;
    private boolean predValue;
    private List<Integer> incomingEvents;
    private int xCoord;
    private int yCoord;

    public Event(int id, int pid, boolean predValue) {
        this.id = id;
        this.pid = pid;
        this.predValue = predValue;
        this.incomingEvents = new LinkedList<>();
    }

    public void addIncomingEvent(int evt) {
        this.incomingEvents.add(evt);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public boolean isPredValue() {
        return predValue;
    }

    public void setPredValue(boolean predValue) {
        this.predValue = predValue;
    }

    public List<Integer> getIncomingEvents() {
        return incomingEvents;
    }

    public void setIncomingEvents(List<Integer> incomingEvents) {
        this.incomingEvents = incomingEvents;
    }

    public int getXCoord() {
        return xCoord;
    }

    public void setXCoord(int xCoord) {
        this.xCoord = xCoord;
    }

    public int getYCoord() {
        return yCoord;
    }

    public void setYCoord(int yCoord) {
        this.yCoord = yCoord;
    }
}
