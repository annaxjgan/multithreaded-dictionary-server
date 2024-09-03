/**
 * Name: Anna Gan, Student ID: 1579818
 * A GUI for starting, stopping, and monitoring the DictionaryServer, featuring buttons and a text area for server logs.
 */

package server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DictionaryServerGUI extends JFrame {

    private static JTextArea outputPane;
    private static JButton runButton;
    private static JButton stopButton;

    private DictionaryServer server;
    private boolean serverRunning = false;
    
    /**
     * Constructs a DictionaryServerGUI with the specified DictionaryServer.
     * @param server the DictionaryServer instance to control
     */
    public DictionaryServerGUI(DictionaryServer server) {
    	this.server = server;
        setTitle("Server Control Panel");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        outputPane = new JTextArea();
        outputPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputPane);

        runButton = new JButton("Run");
        stopButton = new JButton("Stop");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(runButton);
        buttonPanel.add(stopButton);
        stopButton.setEnabled(false);


        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Add action listeners for buttons
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
					startServer();
				} catch (IOException e1) {
					System.out.println("IOException occurred. "+ e1.getMessage());
				}
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
					stopServer();
				} catch (IOException e1) {
					System.out.println("IOException occurred. "+ e1.getMessage());
				}
            }
        });
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            	try {
					server.stop();
				} catch (IOException e1) {
					System.out.println(e1.getMessage());
					System.exit(1);
				}
            }
        });
    }
    
    /**
     * Starts the server and updates the UI components.
     * @throws IOException if an error occurs while starting the server
     */
    private void startServer() throws IOException {
        if (serverRunning) {
            logToOutputPane("Server is already running.");
            return;
        }

        new Thread(() -> {
            try {
            	//Prints messages when server is started
                logToOutputPane("Starting server...");
                runButton.setEnabled(false);
                stopButton.setEnabled(true);
                serverRunning = true;
                server.start();

            } catch (IOException e) {
                logToOutputPane("IOException occurred: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Stops the server and updates the UI components.
     * @throws IOException if an error occurs while stopping the server
     */
    private void stopServer() throws IOException {
        if (!serverRunning) {
            logToOutputPane("Server is not running.");
            return;
        }

        new Thread(() -> {
            try {
            	//Prints messages when server is stopped
                logToOutputPane("Terminating server...");
                runButton.setEnabled(true);
                stopButton.setEnabled(false);
                serverRunning = false;
                server.stop();
   
             
            } catch (IOException e) {
                logToOutputPane("IOException occurred: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Logs a message to the output pane.
     * @param message the message to log
     */
    static void logToOutputPane(String message) {
        SwingUtilities.invokeLater(() -> {
            outputPane.append(message + "\n"); // Append to existing text
        });
    }

}