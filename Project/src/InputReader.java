import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class InputReader {
    private static List<List<Integer>> trace;
    private static HashMap<Integer, Integer> messages;
    private static List<Integer> trueEvents;

    public static boolean[][] readTrace(String fileName) throws
            IOException, NumberFormatException{
        trace = new LinkedList<>();
        messages = new HashMap<>();
        trueEvents = new LinkedList<>();

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line = reader.readLine();
        // read the lines that contain the list of events on each processor
        while (line != null) {
            if (line.charAt(0) != 'P') {
                break;
            }
            List<Integer> localTrace = new LinkedList<>();
            String process = line.substring(0, line.indexOf(":"));
            int processID = Integer.parseInt(process.substring(1));
            String events = line.substring(line.indexOf(":")+1);
            for (String event : events.split(",")) {
                String eventIdAsStr = event.substring(0, event.indexOf("(")).trim();
                String predicateAsStr = event.substring(event.indexOf("(") + 1, event.indexOf(")")).trim();
                int eventID = Integer.parseInt(eventIdAsStr);
                if (predicateAsStr.equals("T")) {
                    trueEvents.add(eventID);
                }
                localTrace.add(eventID);
            }
            trace.add(localTrace);
            line = reader.readLine();
        }

        // read the lines that contain the message information
        while (line != null) {
            String[] evts = line.split(",");
            int sendEvt = Integer.parseInt(evts[0].trim());
            int rcvEvt = Integer.parseInt(evts[1].trim());
            messages.put(rcvEvt, sendEvt);
            line = reader.readLine();
        }

        return constructGraph();
    }

    private static boolean[][] constructGraph() {
        // Construct the true event graph
        int n = getNbrOfEvents();
        boolean[][] graph = new boolean[n][n];
        for (int e = 0; e < n; e++) {
            for (int f = 0; f < n; f++) {
                if (trueEvents.contains(e) && trueEvents.contains(f)) {
                    int e_next = getNext(e);
                    graph[e][f] = !happensBefore(e_next, f);
                }
                if (!trueEvents.contains(e)) { isolateNode(graph, e); }
                if (!trueEvents.contains(f)) { isolateNode(graph, f); }
            }
        }
        return graph;
    }

    private static void isolateNode(boolean[][] graph, int e) {
        int n = graph.length;
        for (int i = 0; i < n; i++) {
            graph[i][e] = false;
            graph[e][i] = false;
        }
    }

    private static int getNbrOfEvents() {
        int sum = 0;
        for (int i = 0; i < trace.size(); i++) {
            sum += trace.get(i).size();
        }
        return sum;
    }

    private static boolean locallyPrecedesOrSame(int e, int f) {
        for (int i = 0; i < trace.size(); i++) {
            List<Integer> localTrace = trace.get(i);
            boolean foundE = false;
            for (int j = 0; j < localTrace.size(); j++) {
                if (localTrace.get(j) == e) { foundE = true; }
                if (localTrace.get(j) == f && foundE) {
                    return true;
                }
            }
            if (!foundE) { return false; }
        }
        return false;
    }

    private static int getNext(int evtID) {
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

    private static int getPrev(int evtID) {
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

    private static boolean happensBefore(int e, int f) {
        if (f == -1) {
            return false;
        }

        if (locallyPrecedesOrSame(e, f)) {
            return true;
        }
        int localPrev = getPrev(f);
        int remotePrev = -1;
        if (messages.containsKey(f)) { remotePrev = messages.get(f); }

        return happensBefore(e, localPrev) || happensBefore(e, remotePrev);
    }
}
