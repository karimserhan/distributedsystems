import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;

public class Home extends JFrame implements ActionListener {

    public JLabel welcomeText;
    public  JComboBox menu;
    public JLabel minProcessText;
    public JTextField minNumberOfProcesses;
    public JLabel maxProcessText;
    public JTextField maxNumberOfProcesses;
    public JLabel minProcessEventst;
    public JTextField minNumberOfEvents;
    public JLabel maxProcessEventst;
    public JTextField maxNumberOfEvents;
    public JLabel minMsgText;
    public JTextField minNumberOfMsgs;
    public JLabel maxMsgText;
    public JTextField maxNumberOfMsgs;
    public JButton generateButton;

    public JFileChooser chooser;

    public Home() {
        initialize();
    }

    public void initialize() {
        setBounds(30, 30, 300, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        welcomeText = new JLabel("Java Predicate Control", JLabel.CENTER);
        c.gridy = 1;
        c.gridx = 1;
        getContentPane().add(welcomeText, c);

        String[] comboBoxItems = new String[]{"Random Trace", "Trace from File"};
        c.gridx = 1;
        c.gridy =2;
        menu = new JComboBox(comboBoxItems);
        getContentPane().add(menu,c);

        //Random Menu
        c.anchor = GridBagConstraints.WEST;
        //Minimum Number of Processes
        minProcessText = new JLabel("Minimum Number of Processes");
        c.gridx =1;
        c.gridy =3;
        getContentPane().add(minProcessText, c);
        minNumberOfProcesses = new JTextField();
        minNumberOfProcesses.setColumns(2);
        c.gridx = 2;
        c.gridy=3;
        getContentPane().add(minNumberOfProcesses, c);

        //Maximum Number of Processes
        maxProcessText = new JLabel("Maximum Number of Processes");
        c.gridx =1;
        c.gridy =4;
        getContentPane().add(maxProcessText, c);
        maxNumberOfProcesses = new JTextField();
        maxNumberOfProcesses.setColumns(2);
        c.gridx = 2;
        c.gridy=4;
        getContentPane().add(maxNumberOfProcesses, c);

        //Minimum Number of Events
        minProcessEventst = new JLabel("Minimum Number of Events");
        c.gridx =1;
        c.gridy =5;
        getContentPane().add(minProcessEventst, c);
        minNumberOfEvents = new JTextField();
        minNumberOfEvents.setColumns(2);
        c.gridx = 2;
        c.gridy=5;
        getContentPane().add(minNumberOfEvents, c);

        //Maximum Number of Events
        maxProcessEventst = new JLabel("Maximum Number of Events");
        c.gridx =1;
        c.gridy =6;
        getContentPane().add(maxProcessEventst, c);
        maxNumberOfEvents = new JTextField();
        maxNumberOfEvents.setColumns(2);
        c.gridx = 2;
        c.gridy=6;
        getContentPane().add(maxNumberOfEvents, c);

        //Minimum Number of Messages
        minMsgText = new JLabel("Minimum Number of Messages");
        c.gridx =1;
        c.gridy =7;
        getContentPane().add(minMsgText, c);
        minNumberOfMsgs = new JTextField();
        minNumberOfMsgs.setColumns(2);
        c.gridx = 2;
        c.gridy=7;
        getContentPane().add(minNumberOfMsgs, c);

        //Maximum Number of Events
        maxMsgText = new JLabel("Maximum Number of Messages");
        c.gridx =1;
        c.gridy =8;
        getContentPane().add(maxMsgText, c);
        maxNumberOfMsgs = new JTextField();
        maxNumberOfMsgs.setColumns(2);
        c.gridx = 2;
        c.gridy=8;
        getContentPane().add(maxNumberOfMsgs, c);

        generateButton = new JButton("Generate random trace");
        c.gridx = 1;
        c.gridy = 9;
        getContentPane().add(generateButton, c);

        ((PlainDocument)minNumberOfProcesses.getDocument()).setDocumentFilter(new MyIntFilter());
        ((PlainDocument)maxNumberOfProcesses.getDocument()).setDocumentFilter(new MyIntFilter());
        ((PlainDocument)minNumberOfEvents.getDocument()).setDocumentFilter(new MyIntFilter());
        ((PlainDocument)maxNumberOfEvents.getDocument()).setDocumentFilter(new MyIntFilter());
        ((PlainDocument)minNumberOfMsgs.getDocument()).setDocumentFilter(new MyIntFilter());
        ((PlainDocument)maxNumberOfMsgs.getDocument()).setDocumentFilter(new MyIntFilter());

        final JFrame thisFrame = this;
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int[] vals = null;//getRandomParams();
                    if (vals == null) {
                        JOptionPane.showMessageDialog(thisFrame, "Please fill out all of the fields with positive numbers",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    TraceGenerator generator = new TraceGenerator(vals[0], vals[1], vals[2], vals[3], vals[4], vals[5]);
                    generator.generatreTraceFile("random_trace.txt");
                    MainPanel.createAndShowUI("random_trace.txt");
                } catch (FileNotFoundException exp) {
                    JOptionPane.showMessageDialog(thisFrame, "Unexpected error occured",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    System.out.println("Error writing to file");
                }
            }
        });

        menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if(menu.getSelectedIndex() ==1)
                {
                    enableRandomItems(false);
                    chooser = new JFileChooser();
                    //FileNameExtensionFilter filter = new FileNameExtensionFilter("txt");
                    //chooser.setFileFilter(filter);
                    int returnVal = chooser.showOpenDialog(getParent());
                    if(returnVal == JFileChooser.APPROVE_OPTION) {
                        MainPanel.createAndShowUI(chooser.getSelectedFile().getAbsolutePath());
                    } else {
                        menu.setSelectedIndex(0);
                        enableRandomItems(true);
                    }
                } else {
                    enableRandomItems(true);
                }

                if(menu.getSelectedIndex() ==0)
                {
                    showRandomItems();
                    chooser.setVisible(false);
                }

            }
        });
    }

    public void enableRandomItems(boolean enabled)
    {
         minProcessText.setEnabled(enabled);
         minNumberOfProcesses.setEnabled(enabled);
         maxProcessText.setEnabled(enabled);
         maxNumberOfProcesses.setEnabled(enabled);
         minProcessEventst.setEnabled(enabled);
         minNumberOfEvents.setEnabled(enabled);
         maxProcessEventst.setEnabled(enabled);
         maxNumberOfEvents.setEnabled(enabled);
         minMsgText.setEnabled(enabled);
         minNumberOfMsgs.setEnabled(enabled);
         maxMsgText.setEnabled(enabled);
         maxNumberOfMsgs.setEnabled(enabled);
        generateButton.setEnabled(enabled);
    }

    public void showRandomItems()
    {
        minProcessText.setVisible(true);
        minNumberOfProcesses.setVisible(true);
        maxProcessText.setVisible(true);
        maxNumberOfProcesses.setVisible(true);
        minProcessEventst.setVisible(true);
        minNumberOfEvents.setVisible(true);
        maxProcessEventst.setVisible(true);
        maxNumberOfEvents.setVisible(true);
        minMsgText.setVisible(true);
        minNumberOfMsgs.setVisible(true);
        maxMsgText.setVisible(true);
        maxNumberOfMsgs.setVisible(true);
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Home().setVisible(true);
            }
        });
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        System.out.println("Test");

    }
}

class MyIntFilter extends DocumentFilter {
    @Override
    public void insertString(FilterBypass fb, int offset, String string,
                             AttributeSet attr) throws BadLocationException {

        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.insert(offset, string);

        if (test(sb.toString())) {
            super.insertString(fb, offset, string, attr);
        } else {
            // warn the user and don't allow the insert
        }
    }

    private boolean test(String text) {
        try {
            if (text.equals("")) { return true; }
            int x = Integer.parseInt(text);
            return x >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text,
                        AttributeSet attrs) throws BadLocationException {

        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.replace(offset, offset + length, text);

        if (test(sb.toString())) {
            super.replace(fb, offset, length, text, attrs);
        } else {
            // warn the user and don't allow the insert
        }
    }

    @Override
    public void remove(DocumentFilter.FilterBypass fb, int offset, int length)
            throws BadLocationException {
        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.delete(offset, offset + length);

        if (test(sb.toString())) {
            super.remove(fb, offset, length);
        } else {// warn the user and don't allow the insert
        }

    }
}