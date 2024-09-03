/**
 * Name: Anna Gan, Student ID: 1579818
 * This class is responsible for handling the communications between the server and the client, it executes methods like returning the meaning of a query.
 */
package server;

import java.awt.Color;

import java.io.DataInputStream;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

 // Inner class to handle client communication
 public class ThreadHandler implements Runnable {
	 
    private final Socket clientSocket;
    private int clientNum;
    private DataInputStream input;
    private DataOutputStream output;
    private DictionaryHandler dict;
    
    /**
     * Constructor initializes the ThreadHandler with a client socket, client number, and dictionary handler.
     * @param socket the socket associated with the client
     * @param client_num the number assigned to the client
     * @param dict the DictionaryHandler instance for handling dictionary operations
     * @throws IOException if an I/O error occurs
     */
    public ThreadHandler(Socket socket, int client_num, DictionaryHandler dict) throws IOException {
        this.clientSocket = socket;
        this.clientNum = client_num;
        this.dict = dict;
        this.input = new DataInputStream(clientSocket.getInputStream());
        this.output = new DataOutputStream(clientSocket.getOutputStream());

    }
    
    /**
     * The run method processes client requests in a loop, handling various dictionary commands.
     */
    @Override
    public void run() {
    	DictionaryServerGUI.logToOutputPane("Thread allocated for client " + clientNum);
    	DictionaryServerGUI.logToOutputPane("Running application for client " + clientNum);
    	DictionaryServerGUI.logToOutputPane("------------------------------------------");
        JSONParser parser = new JSONParser();
        JSONObject jsonResponse = new JSONObject(); 


		String clientMsg;
		while (true) {
		    try {
		    	//Read the client's message
		        clientMsg = input.readUTF();
		        JSONObject command = (JSONObject) parser.parse(clientMsg);
		        String commandName = (String) command.get("command");
		        String result = "";
		        
                // Handle different commands from the client
		        // Synchronize on the dictionary to ensure thread safety
		        synchronized (dict) {
			        switch (commandName) {
			            case "getMeaning":
					        jsonResponse.put("output",  dict.getMeaning(command));
				            break;
	
			            case "addNewWord":
					        jsonResponse.put("output",  dict.addNewWord(command));
	
			                break;
	
			            case "removeWord":
					        jsonResponse.put("output",  dict.removeWord(command));
	
			                break;
	
			            case "addNewMeaning":
					        jsonResponse.put("output",  dict.addNewMeaning(command));
	
			                break;
	
			            case "updateMeaning":
					        jsonResponse.put("output",  dict.updateMeaning(command));
	
			                break;
			            default:
			            	//Handle unknown commands
			            	result= "Unknown command";
			            	break;
			        }
		        }
		        //Send response back to the client
		        String serverResponse = jsonResponse.toString();
		        output.writeUTF(serverResponse);
		        output.flush();
		        // Save the dictionary data to file after every operation
		        DictionaryHandler.saveDataToFile();
		        

		    } catch (IOException | ParseException e) {
		    	// Log disconnection when an client disconnects
		    	DictionaryServerGUI.logToOutputPane("Client " + clientNum + " disconnected.");
		        break;
		    }
		}
    }
    
    /**
     * Returns the client socket associated with this handler.
     * @return the client socket
     */
    public Socket getClientSocket() {
    	return this.clientSocket;
    }
    
    /**
     * Returns the client number associated with this handler.
     * @return the client number
     */
    public int getClientNumber() {
    	return this.clientNum;
    }

}
