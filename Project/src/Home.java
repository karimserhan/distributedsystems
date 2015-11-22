import com.sun.tools.javac.comp.Flow;
import javafx.scene.control.ComboBox;

import javax.swing.*;

/**
 * Created by egantoun on 11/21/15.
 */

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

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

    public JFileChooser chooser;

    public Home() {
        Initialize();
    }

    public void Initialize() {
        setBounds(30, 30, 300, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        welcomeText = new JLabel("Java Predicate Control", JLabel.CENTER);
        c.gridy = 1;
        c.gridx = 1;
        getContentPane().add(welcomeText,c);

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
        maxProcessEventst = new JLabel("Minimum Number of Events");
        c.gridx =1;
        c.gridy =6;
        getContentPane().add(maxProcessEventst, c);
        maxNumberOfEvents = new JTextField();
        maxNumberOfEvents.setColumns(2);
        c.gridx = 2;
        c.gridy=6;
        getContentPane().add(maxNumberOfEvents, c);

        //Minimum Number of Messages
        minMsgText = new JLabel("Minimum Number of Events");
        c.gridx =1;
        c.gridy =7;
        getContentPane().add(minMsgText, c);
        minNumberOfMsgs = new JTextField();
        minNumberOfMsgs.setColumns(2);
        c.gridx = 2;
        c.gridy=7;
        getContentPane().add(minNumberOfMsgs, c);

        //Maximum Number of Events
        maxMsgText = new JLabel("Minimum Number of Events");
        c.gridx =1;
        c.gridy =8;
        getContentPane().add(maxMsgText, c);
        maxNumberOfMsgs = new JTextField();
        maxNumberOfMsgs.setColumns(2);
        c.gridx = 2;
        c.gridy=8;
        getContentPane().add(maxNumberOfMsgs, c);



        menu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if(menu.getSelectedIndex() ==1)
                {
                    hideRandomItems();
                    chooser = new JFileChooser();
                    //FileNameExtensionFilter filter = new FileNameExtensionFilter("txt");
                    //chooser.setFileFilter(filter);
                    int returnVal = chooser.showOpenDialog(getParent());
                    if(returnVal == JFileChooser.APPROVE_OPTION) {
                        System.out.println("You chose to open this file: " +
                                chooser.getSelectedFile().getName());
                    }
                }

                if(menu.getSelectedIndex() ==0)
                {
                    showRandomItems();
                    chooser.setVisible(false);
                }

            }
        });



    }

    public void hideRandomItems()
    {

         minProcessText.setVisible(false);
         minNumberOfProcesses.setVisible(false);
         maxProcessText.setVisible(false);
         maxNumberOfProcesses.setVisible(false);
         minProcessEventst.setVisible(false);
         minNumberOfEvents.setVisible(false);
         maxProcessEventst.setVisible(false);
         maxNumberOfEvents.setVisible(false);
         minMsgText.setVisible(false);
         minNumberOfMsgs.setVisible(false);
         maxMsgText.setVisible(false);
         maxNumberOfMsgs.setVisible(false);
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
