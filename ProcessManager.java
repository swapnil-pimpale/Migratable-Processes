/**
 * ProcessManager:
 * The process manager handles the following tasks:
 * 1) Add/Remove hosts to/from the cluster
 * 2) Launch processes
 * 3) Migrate processes
 * 4) Remove processes
 * 5) List processes running on all nodes in the cluster
 * 6) List all the nodes in the cluster
 */

package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ProcessManager implements ThreadCompletionListeners {

	private int processIDCounter;
	private ConcurrentLinkedQueue<MigratableProcess> processQueue;
	private Server mServer;
	private String hostName;
	private List<String> hostList;
	private int port = 2048;
	
	
	private ProcessManager() {
		processIDCounter = 0;
		processQueue = new ConcurrentLinkedQueue<MigratableProcess>();
		mServer = new Server(port, this);
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			System.err.println("Could not get host name.");
			e.printStackTrace();
		}
		hostList = new ArrayList<String>();
		hostList.add(hostName);
	}
	
	public static void main(String[] args) {
		ProcessManager pManager = new ProcessManager();

                // start the server
		pManager.startMigrationServer();

                // start the commandline
		pManager.acceptCommands();	
	}
	
	private void startMigrationServer() {
		new Thread(mServer).start();
	}
	
	public Object[] getAllProcesses() {
		return processQueue.toArray();
	}

        // ProcessManager commandline
	private void acceptCommands() {
		String[] command;
		String s = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		while(true) {
			System.out.print("$$ ");
			try {
				s = in.readLine();
			} catch (IOException e) {
				System.err.println("Error: Could not read from stdin!");
				e.printStackTrace();
			}
			
			command = s.split("\\s+");
			
			if(command[0].equals("quit")) {
				System.exit(0);
			} else if(command[0].equals("ps")) {
				printProcesses();
			} else if(command[0].equals("help")) {
				printHelpPrompt();			
			} else if(command[0].equals("migrate")) {
				if(command.length == 4) {
					try {
						int processID = Integer.parseInt(command[1]);
					
						if(hostName.equals(command[2])) { //send from this host
							migrateProcess(processID, command[3]);		
						} else {
							Request request = new MigrateRequest(processID, command[3]);
							sendRequest(request, command[2]);
						}
					} catch(NumberFormatException e) {
						System.out.println("Invalid processID specified. " +
                                                        "Type help to see the format of the migrate command.");
					}
				} else {
					System.out.println("Invalid migrate command." +
                                                " Type help to see the format of the migrate command.");
					
				}
			} else if(command[0].equals("kill")) {
				if(command.length == 3) {
					try {
						int processID = Integer.parseInt(command[1]);
					
						if(hostName.equals(command[2])) { //kill from this host
							killProcess(processID);
						} else {
							Request request = new KillRequest(processID);
							sendRequest(request, command[2]);
						}
					} catch(NumberFormatException e) {
						System.out.println("Invalid processID specified." +
                                                        " Type help to see the format of the kill command.");
					}
				} else {
					System.out.println("Invalid kill command." +
                                                " Type help to see the format of the kill command.");
				}
			} else if(command[0].equals("addhost")) {
					if(command.length == 2) {
						hostList.add(command[1]);	
						Iterator<String> itr = hostList.iterator();
						
						while(itr.hasNext()) {
							String host = itr.next();
							sendRequest(new AddHostRequest(hostList), host);						
						}
					} else {
						System.out.println("Invalid addhost command. "
                                                + "Type help to see the format of the addhost command.");
					}				
			} else if(command[0].equals("listhosts")) {
				Iterator<String> itr = hostList.iterator();
				
				System.out.println("The cluster has the following hosts:");
				while(itr.hasNext()) {
					String hostName = itr.next();
					System.out.println(hostName);
				}
			} else if (command[0].equals("removehost")) {
                                if(command.length == 2) {
                                        Iterator<String> itr = hostList.iterator();

                                        while(itr.hasNext()) {
                                                String host = itr.next();
                                                if (!hostName.equals(host)) {
                                                        sendRequest(new RemoveHostRequest(command[1]), host);
                                                }
                                        }
                                        removeHost(command[1]);
                                } else {
                                        System.out.println("Invalid removehost command. " +
                                                "Type help to see the format of the removehost command.");
                                }
                        } else { //check if its a request to launch a process
				constructProcess(command);
			}
		}
	}

        /**
         * sendRequest: send a request to a host over socket
         * @param request: The request to be sent
         * @param hostTo: The host to which the request is to be sent
         */
        private void sendRequest(Request request, String hostTo) {
			
        	try {
                        Socket sock = new Socket(hostTo, port);
                        ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
                        ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
                        out.writeObject(request);
                        Object obj = in.readObject();

                        out.close();
                        in.close();

                        // check what kind of response we received
                        if(obj instanceof ProcessListResponse) {
                                ProcessListResponse resp = (ProcessListResponse) obj;
                                List<MigratableProcess> list = resp.getList();
                                Iterator<MigratableProcess> itr = list.iterator();

                                if (list.isEmpty()) {
                                        System.out.println(hostTo + " - no processes running");
                                } else {
                                        System.out.println(hostTo + " - has following processes running");
                                }

                                while(itr.hasNext()) {
                                        System.out.println(itr.next().toString());
                                }
                        } else if(obj instanceof Response) {
                                Response resp = (Response) obj;
                                if(resp.getErrorCode()==0) {
                                        //System.out.println(sock.getInetAddress().getHostName()+ ": Command succeeded.");
                                }
                                else{
                                        System.out.println(sock.getInetAddress().getHostName()+ ": Command failed.");
                                }
                        }
                } catch (UnknownHostException e) {
                        System.out.println("Could not find the specified host.");
                } catch (IOException e) {
                        System.out.println("An I/O Exception occured while creating the socket. "
                                + "Perhaps " + hostTo + " is down.");
                } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                }
        }

	public int killProcess(int processID) {
                Iterator<MigratableProcess> itr = processQueue.iterator();
                boolean found = false;
                int ret = 0;

                while (itr.hasNext()) {
                        MigratableProcess process = itr.next();

                        if (processID == process.getProcID()) {
                                found = true;
                                processQueue.remove(process);
                                try {
                                        process.stop();
                                } catch (InterruptedException e) {
                                        e.printStackTrace();
                                        ret = -1;
                                }
                        }
                }

                if (!found) {
                        ret = -1;
                        System.out.println("The process specified was not found. "
                                + "Type ps to see a list of all running processes.");
                }
                
                return ret;
        }
		
	public int addHosts(List<String> hosts) {
		
		Iterator<String> itr = hosts.iterator();
		while(itr.hasNext()) {
			String host = itr.next();
			if(!hostList.contains(host)) {
				hostList.add(host);
			}
		}
		return 0;
	}
		
	public int migrateProcess(int processID, String host) {
		Iterator<MigratableProcess> itr = processQueue.iterator();
		boolean found = false;
		int ret = 0;
		
		while(itr.hasNext()) {
			MigratableProcess process = itr.next();
			
			if(processID == process.getProcID()) {
				found = true;
				processQueue.remove(process);
				//Suspend, serialize the process and send it over.
				try {
					process.suspend();
					process.migrationPreProcess();
					Socket sock = new Socket(host, port);
					ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
					out.writeObject(process);
					
				} catch (UnknownHostException e) {
					ret = -1;
					System.out.println("Could not find the specified host.");
				} catch (IOException e) {
					ret = -1;
					System.out.println("An I/O Exception occured while creating the socket.");
				} catch (InterruptedException e) {
					ret = -1;
					e.printStackTrace();
				}
			}	
		}
		
		if(!found) {
			ret = -1;
			System.out.println("The process specified was not found. "
                                + "Type ps to see a list of all running processes.");
		}
		
		return ret;
	}

	//Process names are case-sensitive;
	private <T> void constructProcess(String[] command) {
		try {
			@SuppressWarnings("unchecked")
			Class<T> c = (Class<T>) Class.forName(getPackageName() + command[0]);
			if(MigratableProcess.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers()) &&
					!Modifier.isInterface((c.getModifiers())) ) { 
				Constructor<T>[] cons = (Constructor<T>[]) c.getConstructors();//returns all constructors!
				String[] array = Arrays.copyOfRange(command, 1, (command.length + 1));
				array[command.length - 1] = Integer.toString(generateProcessID());
				MigratableProcess newProcess = (MigratableProcess) cons[0].newInstance(new Object[]{array});
				
				//we now have our process object, add it to the queue and start it
				addProcessToQueue(newProcess);

                                // add ourself as the listener
                                newProcess.addListener(this);

                                // Start the thread
				new Thread(newProcess).start();
			} else {
				System.out.println("You have not specified a valid process. Please try again.");
			}
		} catch (ClassNotFoundException e) {
			System.out.println("You have entered an invalid command. Please type 'help' for more "
				+ "information.\n");
		} catch (InstantiationException e) {
			System.err.println("Failed to instantiate the specified process.");
		} catch (IllegalAccessException e) {
			System.err.println("Failed to acess the specified constructor.");
		} catch (InvocationTargetException e) {
			System.out.println("The object constructor threw an exception. Please make sure"
					+ " the arguments are correctly specified.\n");
		}
		  catch(NoClassDefFoundError e) {
			  System.out.println("Processes are case-sensitive. Please try again.");
		}
	}

	private String getPackageName() {
		if(this.getClass().getPackage()==null) {
			return "";
		}
		String name =  this.getClass().getPackage().getName();
		name += ".";
		return name;
	}

	private void printHelpPrompt() {
		System.out.println("Commands:");
                System.out.println("<processname> [arg0] [arg1] ... - Start the specified process with the"
                        + " given arguments.");
                System.out.println("");
		System.out.println("ps - Displays a list of running processes on all nodes of the cluster");
                System.out.println("Synopsis: ps");
                System.out.println("");
                System.out.println("migrate - Migrate a process specified by processID from one machine in the cluster" +
                " to other");
		System.out.println("Synopsis: migrate processID fromHost toHost");
                System.out.println("");
		System.out.println("kill - Kill the desired process from a particular host.");
                System.out.println("Synopsis: kill processID fromHost");
                System.out.println("");
                System.out.println("addhost - Add a host to the cluster.");
                System.out.println("Synopsis - addHost hostName");
                System.out.println("");
                System.out.println("removehost - Remove a host from the cluster.");
                System.out.println("Synopsis - removehost hostName");
                System.out.println("");
                System.out.println("listhosts - List all hosts in the cluster.");
                System.out.println("Synopsis - listhosts");
                System.out.println("");
		System.out.println("quit - Terminate the ProcessManager");
                System.out.println("Synopsis: quit");
                System.out.println("");
		System.out.println("Note that process names are case-sensitive.\n");
	}

	private void printProcesses() {
		Iterator<String> hostItr = hostList.iterator();
		while(hostItr.hasNext()) {
			String host = hostItr.next();
                        sendRequest(new ProcessListRequest(), host);
		}
	}

	public int generateProcessID() {
		processIDCounter++;
		return processIDCounter;
	}

        public void addProcessToQueue(MigratableProcess proc) {
                processQueue.add(proc);
        }

        public void removeProcessFromQueue(MigratableProcess proc) {
                processQueue.remove(proc);
        }

        @Override
        public void notifyOfThreadCompletion(MigratableProcess process) {
                System.out.println("Processing of processID [" + process.getProcID() + "] on this node has finished");
                 // remove the listener
                process.removeListener(this);
                
                // remove the process from the queue
                removeProcessFromQueue(process);
        }

        public int removeHost(String host) {
                int ret = 0;
                        if(!hostList.contains(host)) {
                                System.out.println("Host not found!");
                                ret = -1;
                        } else if (hostName.equals(host)) {
                                /*
                                we want to delete localhost from the cluster.
                                Delete all other hosts from the list of nodes
                                 */
                                hostList.clear();
                                hostList.add(hostName);
                                ret = 0;
                        } else {
                                hostList.remove(host);
                                ret = 0;
                        }

                return 0;
        }
}