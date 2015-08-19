/**
 * MigratableProcess: Abstract class for migratable processes
 *
 * It implements Runnable such that the processes can be run in a thread.
 * It also implements Serializable such that it can be serialized and written
 * to and read from a stream
 */
package com.company;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * Each MigratableProcess should satisfy the following:
 * 1) The class should have only 1 constructor, that takes in an array of strings for an argument.
 * 2) Each MigratableProcess should store it's arguments, which include a processID passed in 
 * by the ProcessManager.
 */
@SuppressWarnings("serial")
public abstract class MigratableProcess implements Serializable, Runnable {

        // Process ID
        public int procID;

        // Set of arguments to the process
        protected List<String> procArgs;

        // Suspend flag - set when the process is suspended
        public volatile boolean suspend_flag;

        // Quit flag for when we want to stop the thread
        public volatile boolean should_quit;

        // To keep track of the parent process which should be notified after thread death
        protected transient ThreadCompletionListeners listener;

        // Add a listener
        public void addListener(ThreadCompletionListeners listener)
        {
                this.listener = listener;
        }

        // Remove a listener
        public void removeListener(ThreadCompletionListeners listener)
        {
                this.listener = null;
        }

        // Signal a listener of thread death
        public void signalListeners()
        {
              listener.notifyOfThreadCompletion(this);
        }

        // Constructor
        public MigratableProcess(String[] args)
        {
                // Store arguments
                this.procArgs = new ArrayList<String>(Arrays.asList(args));
                this.procID = Integer.parseInt(args[args.length - 1]);
                this.suspend_flag = false;
                this.should_quit = false;
                this.listener = null;
        }

        // Run method for the process
        public abstract void run();

        // Pre-processing required before migration
        public abstract void migrationPreProcess();

        // Suspend method should allow the object to enter a safe state so that it can be serialized
	public void suspend() throws InterruptedException
        {
                suspend_flag = true;
                while (suspend_flag)
                        Thread.sleep(10);
        }

        // Stop method sets the should_quit flag
        public void stop() throws InterruptedException
        {
                should_quit = true;
        }

        // Get process Id
        public int getProcID()
        {
                return procID;
        }

        // Set process ID
        public void setProcID(int procID)
        {
                this.procID = procID;
        }

        /**
         * The toString method should print out the process name, it's arguments, and it's processID.
         * Process name is obtained via reflection, and arguments and processID are stored during construction.
         */
	public String toString()
        {
                StringBuffer buf = new StringBuffer();
                buf.append(this.getClass().getSimpleName());
                buf.append("(PID: " + procID + ") - ");
                for(int i = 0; i < procArgs.size(); i++) {
                        if(i > 0){
                                buf.append(", ");
                        }

                        if(i == (procArgs.size() - 1)) {
                        	buf.append("(Old pid: " + procArgs.get(i) + ")");
                        } else {
                        	buf.append(procArgs.get(i));
                        }
                }
                return buf.toString();
        }
}