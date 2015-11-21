import com.sun.deploy.trace.Trace;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class TraceGenerator {

    private List<List<Integer>> trace;
    private List<Message> messages;

    int minNbrProcessors;
    int maxNbrProcessors;
    int minEventsPerProcess;
    int maxEventsPerProcess;
    int minNbrMsgs;
    int maxNbrMsgs;

    public TraceGenerator(int minNbrProcessors,
                          int maxNbrProcessors,
                          int minEventsPerProcess,
                          int maxEventsPerProcess,
                          int minNbrMsgs,
                          int maxNbrMsgs) {
        this.trace = new LinkedList<>();
        this.messages = new LinkedList<>();
        this.minNbrProcessors = minNbrProcessors;
        this.maxNbrProcessors = maxNbrProcessors;
        this.minEventsPerProcess = minEventsPerProcess;
        this.maxEventsPerProcess = maxEventsPerProcess;
        this.minNbrMsgs = minNbrMsgs;
        this.maxNbrMsgs = maxNbrMsgs;
    }

    public void generatreTraceFile(String outputFileName) throws FileNotFoundException {
        String trace_str = "";

        int nbrProcessors = minNbrProcessors + (int)((maxNbrProcessors-minNbrProcessors)*Math.random());
        int eventID = 0;
        for (int i = 0; i < nbrProcessors; i++) {
            String localTrace_str = "P" + i + ": ";
            List<Integer> localTrace = new LinkedList<>();

            int nbrEvents = minEventsPerProcess
                    + (int)((maxEventsPerProcess-minEventsPerProcess)*Math.random());
            int maxEventID = eventID + nbrEvents - 1;
            String delim = "";
            while (eventID <= maxEventID) {
                String predValue = (Math.random() < 0.5) ? "T" : "F";
                localTrace_str += delim + eventID + "(" + predValue + ")";
                delim = ", ";
                localTrace.add(eventID);
                eventID++;
            }
            trace_str += localTrace_str + "\n";
            trace.add(localTrace);
        }

        String messages_str = "";

        int nbrMsgs = minNbrMsgs + (int)((maxNbrMsgs-minNbrMsgs)*Math.random());
        int[] minSendEventPerProcess = new int[nbrProcessors];
        int msgCount = 0;
        while (true) {
            // choose processes
            int sender = (int)(Math.random()*nbrProcessors);
            int receiver = (int)(Math.random()*nbrProcessors);
            if (sender == receiver) { continue; }

            // choose events
            int sendEvt = trace.get(sender).get(0) + (int)(Math.random()*trace.get(sender).size());
            int receiveEvt = trace.get(receiver).get(0) + (int)(Math.random()*trace.get(receiver).size());

            // add message
            if (checkValidity(new Message(sendEvt, receiveEvt))) {
                messages.add(new Message(sendEvt, receiveEvt));
                messages_str += sendEvt + "," + receiveEvt + "\n";
                msgCount++;
                if (msgCount == nbrMsgs) { break; }
            }
        }

        // write to file
        PrintWriter writer = new PrintWriter(outputFileName);
        writer.println(trace_str.trim() + "\n" + messages_str.trim());
        writer.close();
    }

    /**
     * Checks whether msg can be added without creating any cycles
     */
    private boolean checkValidity(Message msg) {
        return !isReachable(msg.receiveEvt, msg.sendEvt);
    }


    private boolean isReachable(int start, int end) {
        List<Integer> allNextEvts = findAllNextEvts(start);
        for (int nextEvt : allNextEvts) {
            if (nextEvt == end) {
                return true;
            }
            else if (isReachable(nextEvt, end)) {
                return true;
            }
        }
        return false;
    }

    private List<Integer> findAllNextEvts(int evt) {
        List<Integer> allNextEvts = new LinkedList<>();
        // search in trace
        boolean stop = false;
        for (int i = 0; i < trace.size(); i++) {
            List<Integer> localTrace = trace.get(i);
            for (int j = 0; j < localTrace.size(); j++) {
                if (localTrace.get(j) == evt && j != (localTrace.size()-1)) {
                    allNextEvts.add(localTrace.get(j+1));
                    stop = true;
                    break;
                }
            }
            if (stop) { break; }
        }
        // search in messages
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).sendEvt == evt) {
                allNextEvts.add(messages.get(i).receiveEvt);
            }
        }

        return allNextEvts;
    }

    public class Message {
        int sendEvt;
        int receiveEvt;

        public Message (int sendEvt, int receiveEvt) {
            this.sendEvt = sendEvt;
            this.receiveEvt = receiveEvt;
        }
    }
}
