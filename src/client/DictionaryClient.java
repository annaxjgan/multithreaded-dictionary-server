/**
 * Name: Anna Gan, Student ID: 1579818
 * The DictionaryClient class connects to a dictionary server to perform word retrieval, addition, removal, and updates via a GUI.
 */

package client;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class DictionaryClient {
    private String ip;
    private int port;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private DictionaryClientGUI gui;
    
    /**
     * Constructs a DictionaryClient instance with the specified server IP and port.
     *
     * @param ip The IP address of the server to connect to.
     * @param port The port number on which the server is listening.
     * @throws InterruptedException If the thread is interrupted while waiting for a connection.
     */
    public DictionaryClient(String ip, int port) throws InterruptedException{
        this.ip = ip;
        this.port = port;
        this.gui = new DictionaryClientGUI(this);
        connectToServer();
    }
    
    /**
     * Attempts to connect to the server using the provided IP address and port.
     * Displays messages about the connection status and initializes the GUI once connected.
     *
     * @throws InterruptedException If the thread is interrupted while waiting for a connection.
     */
    void connectToServer() throws InterruptedException {
        try {
            socket = new Socket(ip, port);
            System.out.println("Connection established with the server.");
            
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            boolean hasPrintedWaitMessage = false;
            while (true) {
                int availableThread = input.read(); // Check if there are available threads
                if (availableThread > 0) {
                    System.out.println("Loading the application...");
                    gui.initialiseGUI(); //Initialise the GUI once task is picked up by a thread
                    System.out.println("Application loaded.");
                    break;
                } else {
                    if (!hasPrintedWaitMessage) { 
                    	//Inform client that there are no available threads to run task
                        System.out.println("Server is busy. Please wait while we process your request...");
                        hasPrintedWaitMessage = true;
                    }
                }
            }

        } catch (UnknownHostException e) {
            System.err.println("Unknown host due to incorrect IP address.");
            System.exit(1);
        } catch (SocketTimeoutException e) {
        	System.err.println("Timeout occurred. Please try connecting to the server again.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage() + ". No server listening on host " + ip + " and port " + port);
            System.exit(1);
        }
    }
    
    /**
     * Sends a request to the server to retrieve the meaning of a specified word.
     *
     * @param word The word for which the meaning is to be retrieved.
     */
    synchronized void getMeaning(String word){
        try {
            JSONObject request = new JSONObject(); //Create a JSON object to represent the request
            request.put("command", "getMeaning");
            request.put("word", word);


            output.writeUTF(request.toJSONString());
            output.flush();
            getServerResponse();


        } catch (IOException | ParseException e) {
        	handleDisconnection(e);
        }
        
    }
    
    /**
     * Sends a request to the server to add a new word to the dictionary.
     *
     * @param word The word to be added to the dictionary.
     * @param meaning The meaning of the word to be added.
     */
    synchronized void addNewWord(String word, String meaning)  {
        try {
            JSONObject request = new JSONObject();
            request.put("command", "addNewWord");
            request.put("word", word);
            request.put("meaning", meaning);
         
            output.writeUTF(request.toJSONString());
            output.flush();
            getServerResponse();


        }  catch (IOException | ParseException e) {
        	handleDisconnection(e);
        }
    }
    
    /**
     * Sends a request to the server to remove a word from the dictionary.
     *
     * @param word The word to be removed from the dictionary.
     */
    synchronized void removeWord(String word) {
        try {
            JSONObject request = new JSONObject();
            request.put("command", "removeWord");
            request.put("word", word);

            output.writeUTF(request.toJSONString());
            output.flush();
            getServerResponse();

        } catch (IOException | ParseException e) {
        	handleDisconnection(e);
        }
    }
    
    /**
     * Sends a request to the server to add a new meaning to an existing word.
     *
     * @param word The word to which the new meaning will be added.
     * @param newMeaning The new meaning to be added to the word.
     */
    synchronized void addNewMeaning(String word, String newMeaning){
        try {

            JSONObject request = new JSONObject();
            request.put("command", "addNewMeaning");
            request.put("word", word);
            request.put("newMeaning", newMeaning);
            

            output.writeUTF(request.toJSONString());
            output.flush();
            getServerResponse();
            
        }  catch (IOException | ParseException e) {
        	handleDisconnection(e);
        }
    }
    
    /**
     * Sends a request to the server to update the meaning of an existing word.
     *
     * @param word The word whose meaning is to be updated.
     * @param existingMeaning The current meaning of the word.
     * @param newMeaning The new meaning to replace the existing meaning.
     */
    synchronized void updateMeaning(String word, String existingMeaning, String newMeaning)  {
        try {
            JSONObject request = new JSONObject();
            request.put("command", "updateMeaning");
            request.put("word", word);
            request.put("existingMeaning", existingMeaning);
            request.put("newMeaning", newMeaning);

            output.writeUTF(request.toJSONString());
            output.flush();
            getServerResponse();
           

        } catch (IOException | ParseException e) {
        	handleDisconnection(e);
        }
    }

    /**
     * Handles disconnection from the server and updates the GUI to reflect the disconnection.
     *
     * @param e The exception that caused the disconnection.
     */
    void handleDisconnection(Exception e) {

        // Close socket
        if (socket != null && !socket.isClosed()) {
            try {
				socket.close();
				gui.printDisconnectionMessage();
			} catch (IOException e1) {
			}
        }
        
        // Update UI to reflect disconnection
        gui.updateOutputArea("", Color.red);
        gui.updateConnectionStatus("Disconnected", Color.red);

    }
    

    /**
     * Reads the server's response and updates the GUI accordingly.
     *
     * @throws ParseException If there is an error parsing the server response.
     * @throws IOException If there is an error reading from the server.
     */
    private void getServerResponse() throws ParseException, IOException {
        String serverResponse = input.readUTF(); // Read the server's response as a UTF-encoded string
        JSONParser parser = new JSONParser();
        JSONObject response = (JSONObject) parser.parse(serverResponse);
        String outputText = response.get("output").toString();
        Color color;
        
        //Determine the colour for the output based on the response text
        if (outputText.startsWith("ERROR")) {
            color = Color.red;
        } else if (outputText.startsWith("SUCCESS")) {
            color = new Color(0,150,0);
        } else {
            color = Color.black; 
        }        
        gui.updateOutputArea(outputText, color);
    }
    
    /**
     * Closes the socket connection to the server and exits the application.
     * Updates the GUI to reflect the disconnection.
     */
    void closeSocket() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close(); //Close the socket connection
                System.out.println("Socket closed.");
            } catch (IOException ex) {
                System.out.println("Error closing socket: " + ex.getMessage());
            }
        }
        
        System.out.println("Closing the GUI...");
        System.exit(0); 
    }
   

    public static void main(String[] args) throws IOException {
    	try {
	        if (args.length != 2) {
    			System.err.println("Lack of Parameters. Usage example: \"java - jar DictServer.jar <ip address> <port>\"");
	            System.exit(1);
	        }
	        
	        
	        String ip = args[0];
	        int port = Integer.parseInt(args[1]);

	        SwingUtilities.invokeLater(()  -> {
	        		try {
						new DictionaryClient(ip, port);
					} catch (InterruptedException e) {
						System.out.println("Connection failed. Please check the server address and try again.");
					}
 	        });
	        
    	}
        catch (NumberFormatException e) {
        	System.out.println("Incorrect hostname or port address format.");
        	System.exit(1);
        }
    }
    
}