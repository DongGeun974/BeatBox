package project_3;

import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class BeatBoxFinal {

    JFrame theFrame;
    JPanel mainPanel;
    JList incomingList;
    JTextField userMessage;
    ArrayList<JCheckBox> checkboxList;
    int nextNum;
    Vector<String> listVector = new Vector<String>();
    String userName;
    ObjectOutputStream out;
    ObjectInputStream in;
    HashMap<String, boolean[]> otherSeqsMap = new HashMap<String, boolean[]>();

    Sequencer sequencer;
    Sequence sequence;
    Sequence mySequence = null;
    Track track;

    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat",
            "Acoustic Snare", "Crash Cymbal", "Hand Clap", "High Tom", "Hi Bongo",
            "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap",
            "Low-mid Tom", "High Agogo", "Open Hi Conga"};

    int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58,
            47, 67, 63};

    public static void main(String[] args) {
        try {
            System.out.println("황동근_2016250056");
            new BeatBoxFinal().startUp(args[0]);
        }
        catch (ArrayIndexOutOfBoundsException ex){
            System.out.println("Please insert argument in main!");
        }
    }

    public void startUp(String name) {
        userName = name;
        // open connectio to the server
        try {
            Socket sock = new Socket("127.0.0.1", 4242);
            out = new ObjectOutputStream(sock.getOutputStream());
            in = new ObjectInputStream(sock.getInputStream());
            Thread remote = new Thread(new RemoteReader());
            remote.start();
        } catch (Exception ex) {
            System.out.println("couldn't connect - you'll have to play alone.");
        }
        setUpMidi();
        buildGUI();
    } // close startUp

    public void buildGUI() {

        theFrame = new JFrame("Cyber BeatBox");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        checkboxList = new ArrayList<>();

        Box buttonBox = new Box(BoxLayout.Y_AXIS);
        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        JButton sendIt = new JButton("sendIt");
        sendIt.addActionListener(new MySendListener());
        buttonBox.add(sendIt);

        JButton serializeIt = new JButton("serializeIt");
        serializeIt.addActionListener(new serialListener());
        buttonBox.add(serializeIt);

        JButton restore = new JButton("restore");
        restore.addActionListener(new restoreListener());
        buttonBox.add(restore);

        JButton random = new JButton("randomBeats!");
        random.addActionListener(new MyRandomListener());
        buttonBox.add(random);

        userMessage = new JTextField();

        buttonBox.add(userMessage);

        incomingList = new JList();
        incomingList.addListSelectionListener(new MyListSelectionListener());
        incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane theList = new JScrollPane(incomingList);
        buttonBox.add(theList);
        incomingList.setListData(listVector); // no data to start with

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (String instrumentName : instrumentNames) {
            nameBox.add(new Label(instrumentName));
        }

        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);

        theFrame.getContentPane().add(background);
        GridLayout grid = new GridLayout(16, 16);
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);

        for (int i = 0; i < 256; i++) {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkboxList.add(c);
            mainPanel.add(c);
        } // end loop

        theFrame.setBounds(50, 50, 300, 300);
        theFrame.pack();
        theFrame.setVisible(true);
    } // close buildGUI

    public void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        } catch (Exception e) {e.printStackTrace();}
    } // close setUpMidi

    public void buildTrackAndStart() {
        ArrayList<Integer> trackList = null; // this will hold the instruments for each
        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++) {
            trackList = new ArrayList<>();

            for (int j = 0; j < 16; j++) {
                JCheckBox jc = (JCheckBox) checkboxList.get(j + (16 * i));
                if (jc.isSelected()) {
                    int key = instruments[i];
                    trackList.add(new Integer(key));
                } else {
                    trackList.add(null); // because this slot should be empty in the track
                }
            } // close inner loop
            makeTracks(trackList);
        } // close outer loop
        track.add(makeEvent(192, 9, 1, 0, 15)); // - so we always go to full 16 beats

        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch (Exception e) {e.printStackTrace();}
    } // close buildTrackAndStart

    public class MyStartListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            buildTrackAndStart();
        } // close actionPerformed
    } // close inner class

    public class MyStopListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            sequencer.stop();
        } // close actionPerformed
    } // close inner class

    public class MyUpTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 1.03));
        } // close actionPerformed
    } // close inner class

    public class MyDownTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 0.97));
        } // close actionPerformed
    } // close inner class

    public class MySendListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            // make an arraylist of just the STATE of the checkboxes
            boolean[] checkboxState = new boolean[256];
            for (int i = 0; i < 256; i++) {
                checkboxState[i] = checkboxList.get(i).isSelected();
            } // close loop
            String messageToSend = null;
            try {
                out.writeObject(userName + nextNum + ": " + userMessage.getText());
                out.writeObject(checkboxState);
            } catch (Exception ex) {
                System.out.println("Sorry dude. Could not send it to the server");
            }
            userMessage.setText("");
        } // close actionPerformed
    } // close inner class

    public class MyListSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent le) {
            if (!le.getValueIsAdjusting()) {
                int result =  JOptionPane.showConfirmDialog(null,
                        "저장하시겠습니까?");
                if (result == JOptionPane.YES_OPTION)
                {
                    store();
                    String selected = (String) incomingList.getSelectedValue();
                    if (selected != null) {
                        // now go to the map, and change the sequence
                        boolean[] selectedState = (boolean[]) otherSeqsMap.get(selected);
                        changeSequence(selectedState);
                        sequencer.stop();
                        buildTrackAndStart();
                    }
                }else if (result == JOptionPane.NO_OPTION){
                    String selected = (String) incomingList.getSelectedValue();
                    if (selected != null) {
                        // now go to the map, and change the sequence
                        boolean[] selectedState = (boolean[]) otherSeqsMap.get(selected);
                        changeSequence(selectedState);
                        sequencer.stop();
                        buildTrackAndStart();
                    }
                }
            }
        } // close valueChange
    } // close inner class

    public void store(){
        boolean[] checkboxState = new boolean[256];

        for(int i = 0; i < 256; i++)
        {
            JCheckBox check = (JCheckBox) checkboxList.get(i);
            if (check.isSelected())
            {
                checkboxState[i] =true;
            }
        }
        try{
            FileOutputStream fileStream = new FileOutputStream(new File("Checkbox.ser"));
            ObjectOutputStream os = new ObjectOutputStream(fileStream);
            os.writeObject(checkboxState);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public class serialListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            store();
        }
    }

    public class restoreListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            boolean[] checkboxState = null;
            try{
                FileInputStream fileIn = new FileInputStream(new File("Checkbox.ser"));
                ObjectInputStream is = new ObjectInputStream(fileIn);
                checkboxState = (boolean[]) is.readObject();
            }catch (Exception ex){
                ex.printStackTrace();
            }

            for (int i = 0; i < 256; i++)
            {
                JCheckBox check = (JCheckBox) checkboxList.get(i);
                if (checkboxState[i]){
                    check.setSelected(true);
                }else {
                    check.setSelected(false);
                }
            }

            sequencer.stop();
            buildTrackAndStart();
        }
    }

    public class MyRandomListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            Random random = new Random();
            for (int i = 0; i < 256; i++)
            {
                JCheckBox check = (JCheckBox) checkboxList.get(i);
                if (random.nextBoolean()){
                    check.setSelected(true);
                }else {
                    check.setSelected(false);
                }
            }
        }
    }
    public class RemoteReader implements Runnable {
        boolean[] checkboxState = null;
        String nameToShow = null;
        Object obj = null;

        public void run() {
            try {
                while ((obj = in.readObject()) != null) {
                    System.out.println("got an object from server");
                    System.out.println(obj.getClass());
                    String nameToShow = (String) obj;
                    checkboxState  = (boolean[]) in.readObject();
                    otherSeqsMap.put(nameToShow, checkboxState);
                    listVector.add(nameToShow);
                    incomingList.setListData(listVector);
                } // close while
            } catch (Exception ex) {ex.printStackTrace();}
        } // close run
    } // close inner class

    public class MyPlayMineListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            if (mySequence != null) {
                sequence = mySequence; // restore to my original
            }
        } // close actionPerformed
    } // close inner class

    public void changeSequence(boolean[] checkboxState) {
        for (int i = 0; i < 256; i++) {
            checkboxList.get(i).setSelected(checkboxState[i]);
        } // close loop
    } // close changeSequence

    public void makeTracks(ArrayList list) {
        Iterator it = list.iterator();
        for (int i = 0; i < 16; i++) {
            Integer num = (Integer) it.next();
            if (num != null) {
                int numKey = num.intValue();
                track.add(makeEvent(144, 9, numKey, 100, i));
                track.add(makeEvent(128, 9, numKey, 100, i + 1));
            }
        } // close loop
    } // close makeTracks()

    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            event = new MidiEvent(a, tick);
        } catch (Exception e) {}
        return event;
    } // close makeEvent

} // close class