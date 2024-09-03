/**
 * Name: Anna Gan, Student ID: 1579818
 * This class creates a fixed pool of worker threads and manages them, it accepts client request from the ClientHandler class and puts them in a queue for the worker thread to execute   
 */
package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;


public class WorkerPool {
    private final int poolSize;
    private final LinkedList<Runnable> taskQueue; 
    private static int idleWorkers;
    private DataInputStream input;
    private DataOutputStream output;
    
    /**
     * Constructs a WorkerPool with a specified number of worker threads.
     * @param poolSize the number of worker threads in the pool
     */
    public WorkerPool(int poolSize) {
        this.poolSize = poolSize;
        this.taskQueue = new LinkedList<>();
        idleWorkers = poolSize;
        initialiseThreadPool();
    }
	
    /**
     * Sets up the worker pool by creating a fixed number of threads.
     */
	private void initialiseThreadPool() {
		for (int i = 0; i < poolSize; i++) {
			Thread worker = new Thread(() -> {
				try {
					run();
				} catch (IOException e) {
					DictionaryServerGUI.logToOutputPane("IOException occurred: "+ e.getMessage());
				}
				
			});
 			worker.start(); //start executing each thread
		}
	}
	
	/**
     * The method that each worker thread runs. Continuously processes tasks from the queue.
     * @throws IOException if an error occurs during task execution
     */
	private void run() throws IOException {
		while (true) {
			ThreadHandler task;
			// Synchronised access to the task queue
			synchronized (taskQueue) {
				// Wait for tasks to be available in the queue
				while (taskQueue.isEmpty()) {
					try {
						taskQueue.wait();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						return; //Exit if the thread is interrupted
					}
				}
				task = (ThreadHandler) taskQueue.poll();
			}

			try {
	            idleWorkers--; 	//Decrease the number of available threads	            
				task.run(); //Execute the task
			} catch (Exception e) {
				DictionaryServerGUI.logToOutputPane(e.getMessage());
			} finally {
                idleWorkers++;
                DictionaryServer.promptClient(task.getClientNumber());
			}
		}
	}
	
	/**
     * Submits a task to the worker pool.
     * @param task the task to be submitted
     * @throws IOException if an error occurs while submitting the task
     */
	synchronized void submitTask(ThreadHandler task) throws IOException {
		//Synchronise access to the task queue
	    synchronized (taskQueue) {
	        taskQueue.addLast(task); //add the task to the end of the queue
	        taskQueue.notify(); //Notify a waiting thread in the pool that a task is available
	    }
	    if (idleWorkers==0) {
	    	//Log message if all threads are busy
	    	DictionaryServerGUI.logToOutputPane("Client " + task.getClientNumber() +" waiting for available threads...");
	    	DictionaryServerGUI.logToOutputPane("------------------------------------------");
	    }

	}
	
	/**
     * Gets the number of available (idle) worker threads.
     * @return the number of available worker threads
     */
	public static int getNumberOfAvailableThreads() {
        return idleWorkers;
	}
	
	
}