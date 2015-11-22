import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainPanel extends JPanel {
    private final int lineGap = 100;
    private final int padding = 60;
    private final int windowWidth = 1500;
    private final int defaultStroke = 2;

    private boolean isControllable;
    private boolean alreadySatisfied;

    private List<List<Integer>> trace;
    private List<Integer> syncSequence;
    private HashMap<Integer, List<Integer>> messages;
    private HashMap<Integer, Event> eventDetails;

    public MainPanel(List<List<Integer>> trace, HashMap<Integer, List<Integer>> messages,
                     HashMap<Integer, Event> eventDetails, List<Integer> syncSequence) {
        this.trace = trace;
        this.messages = messages;
        this.eventDetails = eventDetails;
        this.syncSequence = syncSequence;
    }

    @Override
    public Dimension getPreferredSize() {
        int size = (trace.size() + 1) * lineGap;
        return new Dimension(windowWidth, size);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        writeResult(g);
        drawTrace(g);
    }

    private static MainPanel generatePanel(String fileName) {
        try {
            // read the file
            TraceReader traceReader = new TraceReader();
            traceReader.readTrace(fileName);

            // get the object model
            boolean[][] trueEvtGraph = traceReader.getGraph();
            List<Integer> trueInitials = traceReader.getInitialTrueStates();
            List<Integer> trueFinals = traceReader.getFinalTrueStates();

            // call algorithm
            ArrayList<Integer> shortestSequence = PredicateControl
                    .getShortestSequence(trueInitials, trueFinals, trueEvtGraph);

            MainPanel panel = new MainPanel(
                    traceReader.getTrace(),
                    traceReader.getMessages(),
                    traceReader.getEventDetails(),
                    shortestSequence);

            panel.isControllable = shortestSequence != null;
            panel.alreadySatisfied = shortestSequence != null && shortestSequence.size() == 0;
            return panel;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void writeResult(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setFont(new Font(null, Font.BOLD, 30));
        String message = "Computation controlled with (red) synchronization messages";
        if (!isControllable) {
            message = "The predicate cannot controllable in this computation";
        } else if (alreadySatisfied) {
            message = "The predicate is already satisfied in this computation";
        }
        g2.drawString(message, 20, 50);
    }

    private void drawTrace(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        int width = getWidth() - 2*padding;
        int height = getHeight();

        //  Draw lines starting from left to bottom
        int x = padding;
        int y = lineGap;

        g2.setFont(new Font(null, Font.BOLD, 30));
        g2.setStroke(new BasicStroke(defaultStroke));
        for (int i = 0; i < trace.size(); i++)
        {
            g2.drawString("P" + i, x-50, y+10);
            g2.drawLine(x, y, x+width, y);
            List<Integer> localTrace = trace.get(i);
            int separation = width / (localTrace.size()+1);
            int point_x = x + separation;
            g2.setStroke(new BasicStroke(10));
            for (int j = 0; j < localTrace.size(); j++) {
                Event evt = eventDetails.get(localTrace.get(j));
                if (evt.isPredValue()) {
                    g2.setColor(new Color(34,139,34));
                } else {
                    g2.setColor(Color.BLACK);
                }
                g2.drawLine(point_x, y, point_x, y);
                evt.setXCoord(point_x);
                evt.setYCoord(y);
                point_x += separation;
            }
            g2.setColor(Color.BLACK);

            y += lineGap;
            g2.setStroke(new BasicStroke(defaultStroke));
        }

        for (Map.Entry<Integer, List<Integer>> msg : messages.entrySet()) {
            for (int sendEvt : msg.getValue()) {
                drawMessage(g, sendEvt, msg.getKey(), Color.BLACK);
            }
        }

        if (syncSequence != null) {
            for (int i = 1; i < syncSequence.size(); i++) {
                drawMessage(g, syncSequence.get(i - 1), syncSequence.get(i), Color.RED);
            }
        }
    }

    private void drawMessage(Graphics g, int e, int f, Color color) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(color);
        g2.setStroke(new BasicStroke(defaultStroke));
        Event sendEvt = eventDetails.get(e);
        Event recvEvt = eventDetails.get(f);
        int x1 = sendEvt.getXCoord();
        int y1 = sendEvt.getYCoord();
        int x2 = recvEvt.getXCoord();
        int y2 = recvEvt.getYCoord();

        // draw arrow
        double theta = Math.atan2(y2 - y1, x2 - x1);

        int barb = 20;
        double phi = Math.PI/6;
        double x = x2 - barb * Math.cos(theta + phi);
        double y = y2 - barb * Math.sin(theta + phi);
        g2.draw(new Line2D.Double(x2, y2, x, y));
        x = x2 - barb * Math.cos(theta - phi);
        y = y2 - barb * Math.sin(theta - phi);
        g2.draw(new Line2D.Double(x2, y2, x, y));
        g2.drawLine(x1, y1, x2, y2);
    }

    public static void createAndShowUI() {
        try {
            TraceGenerator generator = new TraceGenerator(2, 4, 4, 5, 2, 2);
            generator.generatreTraceFile("trace.txt");
        } catch (FileNotFoundException e) {
            System.out.println("Error writing to file");
        }

        JDialog dialog = new JDialog(new JFrame(), "Trace");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        MainPanel panel = generatePanel("trace.txt");
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationByPlatform(true);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                createAndShowUI();
            }
        });
    }
}
