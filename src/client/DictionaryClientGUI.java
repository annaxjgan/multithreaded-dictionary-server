/**
 * Name: Anna Gan, Student ID: 1579818
 * The DictionaryClientGUI class provides a graphical user interface for interacting with the DictionaryClient, allowing users to perform word-related operations and view server responses.
 */

package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class DictionaryClientGUI extends JFrame {

    private JTextField wordField;
    private JTextField existingMeaningField;
    private JTextField newMeaningField;
    private JTextArea outputArea;
    private JLabel connectionStatus;
    private DictionaryClient client;
    
    /**
     * Constructs a DictionaryClientGUI instance with the specified DictionaryClient.
     * @param client The DictionaryClient instance that this GUI will interact with.
     */
    public DictionaryClientGUI(DictionaryClient client) {
        this.client = client;
 
    }
    
    /**
     * Initialises the GUI components and layout for the application.
     */
    void initialiseGUI() {
        setVisible(true);
        setTitle("Fruit Dictionary");
        setSize(750, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(0, 1));
        setBackground(Color.white);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 4));

        inputPanel.add(new JLabel());
        inputPanel.add(new JLabel());

        inputPanel.add(new JLabel("Word:"));
        wordField = new JTextField();
        inputPanel.add(wordField);

        inputPanel.add(new JLabel("Connection status:"));
        connectionStatus = new JLabel();
        connectionStatus.setForeground(new Color(0,150,0));
        connectionStatus.setText("Connected");
        inputPanel.add(connectionStatus);

        inputPanel.add(new JLabel("Existing Meaning:"));
        existingMeaningField = new JTextField();
        inputPanel.add(existingMeaningField);

        inputPanel.add(new JLabel());
        inputPanel.add(new JLabel());

        inputPanel.add(new JLabel("New Meaning:"));
        newMeaningField = new JTextField();
        inputPanel.add(newMeaningField);

        add(inputPanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); 
        
        //Create and add buttons for various operations
        JButton getMeaningButton = new JButton("Get Meaning");
        getMeaningButton.setBackground(Color.GRAY);
        getMeaningButton.setPreferredSize(new Dimension(140, 50));
        getMeaningButton.addActionListener(e -> {
        	
        	client.getMeaning(wordField.getText());

        });
        buttonPanel.add(getMeaningButton);

        JButton addNewWordButton = new JButton("Add New Word");
        addNewWordButton.setBackground(Color.GRAY);
        addNewWordButton.setPreferredSize(new Dimension(140, 50));
        addNewWordButton.addActionListener(e -> {

        	client.addNewWord(wordField.getText(), newMeaningField.getText());

        });
        buttonPanel.add(addNewWordButton);

        JButton removeWordButton = new JButton("Remove Word");
        removeWordButton.setBackground(Color.black);
        removeWordButton.setPreferredSize(new Dimension(140, 50));
        removeWordButton.addActionListener(e -> {

        	client.removeWord(wordField.getText());

        });
        buttonPanel.add(removeWordButton);

        JButton addNewMeaningButton = new JButton("Add New Meaning");
        addNewMeaningButton.setBackground(Color.GRAY);
        addNewMeaningButton.setPreferredSize(new Dimension(140, 50));
        addNewMeaningButton.addActionListener(e -> {

        	client.addNewMeaning(wordField.getText(), newMeaningField.getText());

        });
        buttonPanel.add(addNewMeaningButton);

        JButton updateMeaningButton = new JButton("Update Meaning");
        updateMeaningButton.setBackground(Color.GRAY);
        updateMeaningButton.setPreferredSize(new Dimension(140, 50));
        updateMeaningButton.addActionListener(e -> {
        	
        	client.updateMeaning(wordField.getText(), existingMeaningField.getText(), newMeaningField.getText());

        });
        buttonPanel.add(updateMeaningButton);

        add(buttonPanel);
        
        //Add window listener to handle closing the application and printing exit message
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            	client.closeSocket();
            }
        });
        
        // Set up the output area to display server responses
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setPreferredSize(new Dimension(700, 250)); 
        add(new JScrollPane(outputArea));
    }
 

    /**
     * Updates the output area with the specified text and colour.
     * @param text The text to be displayed in the output area.
     * @param colour The colour to be used for the text.
     */
    void updateOutputArea(String text, Color color) {
        outputArea.setForeground(color);
        outputArea.setText(text);
    }
    
    /**
     * Updates the connection status label with the specified status and colour.
     * @param status The connection status text to be displayed.
     * @param colour The colour to be used for the status text.
     */
    void updateConnectionStatus(String status, Color color) {
        connectionStatus.setText(status);
        connectionStatus.setForeground(color);
    }
    
    /**
     * Displays a message dialog indicating that the server has disconnected and closes the application.
     */
    void printDisconnectionMessage() {

    	SwingUtilities.invokeLater(() ->{
    		JOptionPane.showMessageDialog(this, "Server disconnected. The application will now close.","Connection Lost",JOptionPane.ERROR_MESSAGE);
    		System.out.println("Server disconnected. Closing the Application...");
    		System.exit(0);
    	});

    }
}