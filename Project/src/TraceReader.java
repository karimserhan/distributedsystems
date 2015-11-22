import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class TraceReader {
    private List<List<Integer>> trace;
    private HashMap<Integer, List<Integer>> messages;
    private List<Integer> trueEvents;
    private List<Integer> trueInitials;
    private List<Integer> trueFinals;
    private HashMap<Integer, Event> eventDetails;

    public TraceReader() {
        trace = new LinkedList<>();
        messages = new HashMap<>();
        trueEvents = new LinkedList<>();
        trueInitials = new LinkedList<>();
        trueFinals = new LinkedList<>();
        eventDetails = new HashMap<>();
    }

    public void readTrace(String fileName) throws
            IOException, NumberFormatException {

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line = reader.readLine();
        // read the lines that contain the list of events on each processor
        while (line != null) {
            if (line.trim().equals("")) {
                continue;
            }
            if (line.charAt(0) != 'P') {
                break;
            }
            List<Integer> localTrace = new LinkedList<>();
            String process = line.substring(0, line.indexOf(":"));
            int processID = Integer.parseInt(process.substring(1));
            String events = line.substring(line.indexOf(":") + 1);
            String[] eventArr = events.split(",");
            for (int i = 0; i < eventArr.length; i++) {
                String event = eventArr[i];
                String eventIdAsStr = event.substring(0, event.indexOf("(")).trim();
                String predicateAsStr = event.substring(event.indexOf("(") + 1, event.indexOf(")")).trim();
                int eventID = Integer.parseInt(eventIdAsStr);
                if (predicateAsStr.equals("T")) {
                    trueEvents.add(eventID);
                    if (i == 0) { trueInitials.add(eventID); }
                    if (i == eventArr.length-1) { trueFinals.add(eventID); }
                }
                localTrace.add(eventID);
                eventDetails.put(eventID, new Event(eventID, processID, predicateAsStr.equals("T")));
            }
            trace.add(localTrace);
            line = reader.readLine();
        }

        // read the lines that contain the message information
        while (line != null) {
            if (line.equals("")) {continue;}
            String[] evts = line.split(",");
            int sendEvt = Integer.parseInt(evts[0].trim());
            int rcvEvt = Integer.parseInt(evts[1].trim());
            if (messages.containsKey(rcvEvt)) {
                messages.get(rcvEvt).add(sendEvt);
            } else {
                List<Integer> evtList = new LinkedList<>();
                evtList.add(sendEvt);
                messages.put(rcvEvt, evtList);
            }
            eventDetails.get(rcvEvt).addIncomingEvent(sendEvt);
            line = reader.readLine();
        }
    }

    public boolean[][] getGraph() {
        // Construct the true event graph
        int n = getNbrOfEvents();
        boolean[][] graph = new boolean[n][n];
        for (int e = 0; e < n; e++) {
            for (int f = 0; f < n; f++) {
                if (trueEvents.contains(e) && trueEvents.contains(f)) {
                    int e_next = getNext(e);
                    graph[e][f] = e_next == -1 || !happensBefore(e_next, f);
                }
                if (!trueEvents.contains(e)) { isolateNode(graph, e); }
                if (!trueEvents.contains(f)) { isolateNode(graph, f); }
            }
        }
        return graph;
    }

    public List<Integer> getInitialTrueEvents() {
        return trueInitials;
    }
    
    public List<Integer> getFinalTrueEvents() {
        return trueFinals;
    }

    public HashMap<Integer, Event> getEventDetails() {
        return eventDetails;
    }

    private void isolateNode(boolean[][] graph, int e) {
        int n = graph.length;
        for (int i = 0; i < n; i++) {
            graph[i][e] = false;
            graph[e][i] = false;
        }
    }

    private int getNbrOfEvents() {
        int sum = 0;
        for (int i = 0; i < trace.size(); i++) {
            sum += trace.get(i).size();
        }
        return sum;
    }

    private boolean locallyPrecedes(int e, int f) {
        for (int i = 0; i < trace.size(); i++) {
            List<Integer> localTrace = trace.get(i);
            boolean foundE = false;
            for (int j = 0; j < localTrace.size(); j++) {
                if (localTrace.get(j) == f) { return foundE; }
                if (localTrace.get(j) == e) { foundE = true; }
            }
            if (foundE) { return false; }
        }
        return false;
    }

    private int getNext(int evtID) {
        for (int i = 0; i < trace.size(); i++) {
            List<Integer> localTrace = trace.get(i);
            for (int j = 0; j < localTrace.size(); j++) {
                if (localTrace.get(j) == evtID && j != (localTrace.size()-1)) {
                    return localTrace.get(j+1);
                }
            }
        }
        return -1;
    }

    private int getPrev(int evtID) {
        for (int i = 0; i < trace.size(); i++) {
            List<Integer> localTrace = trace.get(i);
            for (int j = 0; j < localTrace.size(); j++) {
                if (localTrace.get(j) == evtID && j != 0) {
                    return localTrace.get(j-1);
                }
            }
        }
        return -1;
    }

    private boolean happensBefore(int e, int f) {
        if (f == -1) {
            return false;
        }

        if (e == f || locallyPrecedes(e, f)) {
            return true;
        }
        int localPrev = getPrev(f);
        List<Integer> remotePrevs = new LinkedList<>();
        if (messages.containsKey(f)) { remotePrevs = messages.get(f); }

        if (happensBefore(e, localPrev)) { return true; }

        for (int remotePrev : remotePrevs){
            if (happensBefore(e, remotePrev)) {
                return true;
            }
        }

        return false;
    }

    public HashMap<Integer,List<Integer>> getMessages() {
        return messages;
    }

    public List<List<Integer>> getTrace() {
        return trace;
    }
}
