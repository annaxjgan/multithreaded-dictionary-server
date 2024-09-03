/**
 * Name: Anna Gan, Student ID: 1579818
 * This class is the main entry point of the server application. It creates a server socket and actively listens for client connection. 
 */
package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.Scanner;

import javax.swing.SwingUtilities;

public class DictionaryServer {

    private final int port;
    private final WorkerPool workerPool;
    private static final List<Socket> clientSockets = new ArrayList<>();
    private static ServerSocket serverSocket;
    private static DictionaryHandler dictionary;
    private static DictionaryServerGUI serverGui;
    private static int poolSize;
    
    /**
     * Constructor to initialise the DictionaryServer with the given port, thread pool size, and dictionary file.
     * @param port the port number on which the server listens
     * @param threadPoolSize the number of threads in the worker pool
     * @param fileName the name of the dictionary file
     * @throws IOException if an I/O error occurs
     */
    public DictionaryServer(int port, int threadPoolSize, String fileName) throws IOException {
    	this.port = port;
    	this.workerPool = new WorkerPool(threadPoolSize);
    	dictionary = new DictionaryHandler(fileName);
    	serverGui = new DictionaryServerGUI(this);
        SwingUtilities.invokeLater(() -> serverGui.setVisible(true));
    }
    
    /**
     * Starts the server, accepts client connections, and processes requests using the worker pool.
     * @throws IOException if an I/O error occurs
     */
    void start() throws IOException {
    	System.out.println("Server started");
    	InetAddress ip = InetAddress.getLocalHost(); //get the local IP address
    	serverSocket = new ServerSocket(port);
    	logToGui("Server running on port " + port);
    	logToGui("Current IP address : " + ip.getHostAddress());
    	logToGui("Port : " + port);	
    	logToGui("Number of threads available : " + poolSize);	
    	logToGui("Waiting for client connection...\n------------------------------------------");
    	int clientCount = 0;

    	while (true) {
    		//Accepts client connection
    		Socket clientSocket = serverSocket.accept();
    		clientSockets.add(clientSocket);
    		clientCount++;
    		logToGui("Client " + clientCount +" connected.");
    		clientSocket.getOutputStream().write(WorkerPool.getNumberOfAvailableThreads());

    		//Encapsulate a client connection as a task
    		ThreadHandler clientHandler = new ThreadHandler(clientSocket, clientCount,dictionary);
    		//Add the client task to the queue
    		workerPool.submitTask(clientHandler);
    		}

    }
    
    /**
     * Stops the server and closes all client connections.
     * @throws IOException if an I/O error occurs
     */
    void stop() throws IOException {
    	System.out.println("Server terminated.");
    	if (serverSocket!=null) {
	    	serverSocket.close();
	    	for (Socket eachSocket : clientSockets) {eachSocket.close();}
    	}
        System.exit(0);
    }
    
    /**
     * Sends a prompt to a specific client.
     * @param clientNumber the index of the client in the clientSockets list
     * @throws IOException if an I/O error occurs
     */
    static void promptClient(int clientNumber) throws IOException {
    	if (clientNumber+1<= clientSockets.size()) {
        	Socket client = clientSockets.get(clientNumber);
        	client.getOutputStream().write(WorkerPool.getNumberOfAvailableThreads());
    	}

    }
    
    /**
     * Logs messages to the GUI's output pane.
     * @param message the message to log
     */
    public static void logToGui(String message) {
    	DictionaryServerGUI.logToOutputPane(message);
    }

    /**
     * Main method to start the server application.
     * @param args command-line arguments: <port> <pool-size> <dictionary-file>
     */
    public static void main(String[] args) {
    	try {
    		if (args.length<3) {
    			System.err.println("Lack of Parameters. Usage example: \"java - jar DictServer.jar <port> <pool-size> <dictionary-file>\"");
    			System.exit(1);
    		}
            int port = Integer.parseInt(args[0]); // Example port number
            poolSize = Integer.parseInt(args[1]);
            String fileName = args[2];
            DictionaryServer server = new DictionaryServer(port,poolSize, fileName);
            
    	}catch (NumberFormatException e) {
    		System.err.println("Error: Invalid format for port number or worker pool size");
            System.exit(1);
        } catch (UnknownHostException e) {
        	System.err.println("Unknown Host. Please try again");
            System.exit(1);
        } catch (IOException e) {
        	System.err.println("IOException occurred: "+ e.getMessage());
            System.exit(1);
        }

    }

   

}