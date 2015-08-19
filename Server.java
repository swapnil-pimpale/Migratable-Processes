/**
 * Server:
 * Started by the ProcessManager.
 * It Serves multiple requests for adding/removing hosts, ps, migrate and kill
 */

package com.company;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server implements Runnable {

        private int Port;
        private ServerSocket serverSocket;
        private boolean running;
        private ProcessManager pm;

        public Server(int port, ProcessManager pm) {
                this.Port = port;
                this.pm = pm;
        }

        public void run() {
                running = true;
                bind();
                while (running)
                        accept();
        }

        public void stop() {
                if (running) {
                        running = false;
                        try {
                                serverSocket.close();
                        } catch (IOException e) {
                                System.err.println("Could not close server socket");
                        }
                }
        }

        public void bind() {
                try {
                        // Bind
                        serverSocket = new ServerSocket(Port);
                } catch (SocketException e) {
                        System.err.println("Could not create server socket");
                        System.err.println("Caught SocketException: " + e.getMessage());
                        System.exit(1);
                } catch (IOException e) {
                        System.err.println("Caught IOException: " + e.getMessage());
                        System.exit(1);
                }
        }

        public void accept()
        {
                Socket clientSocket;

                try {
                        // Accept
                        clientSocket = serverSocket.accept();
                        // Receive a request over the socket
                        RequestReceive(clientSocket);
                } catch (SocketException e) {
                        System.err.println("Socket accept error");
                        System.err.println("Caught SocketException: " + e.getMessage());
                        System.exit(1);
                } catch (IOException e) {
                        System.err.println("Caught IOException: " + e.getMessage());
                        System.exit(1);
                }
        }

        // Receive requests from other nodes
        public void RequestReceive(Socket clientSocket)
        {
                try {
                        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                        // To Send status to the other node
                        ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());

                        Object obj = in.readObject();
                        if (obj instanceof MigratableProcess) {
                                MigratableProcess process = null;
                                process = (MigratableProcess)obj;
                                //Start the new process from here
                                System.out.println("Restarting migrated process.");
                                start_migrated_process(process);
                        } else if(obj instanceof MigrateRequest){
                        	MigrateRequest request = (MigrateRequest) obj;
                        	Response resp = new Response(pm.migrateProcess(request.getProcessID(), request.getHostTo()));
                        	out.writeObject(resp);
                        } else if(obj instanceof KillRequest){
                                KillRequest request = (KillRequest) obj;
                                Response resp = new Response(pm.killProcess(request.getProcessID()));
                                out.writeObject(resp);
                        } else if(obj instanceof AddHostRequest){
                        	AddHostRequest request = (AddHostRequest) obj;
                        	Response resp = new Response(pm.addHosts(request.getHostList()));
                        	out.writeObject(resp);
                        } else if (obj instanceof RemoveHostRequest) {
                                RemoveHostRequest request = (RemoveHostRequest) obj;
                                Response resp = new Response(pm.removeHost(request.getHost()));
                                out.writeObject(resp);
                        } else if(obj instanceof ProcessListRequest) {
                        	ProcessListResponse resp = new ProcessListResponse(0, pm.getAllProcesses());
                        	out.writeObject(resp);
                        }

                        // Close the streams and the socket
                        in.close();
                        out.close();
                        clientSocket.close();
                } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        public void start_migrated_process(MigratableProcess process)
        {
                // Get and set procID
                process.setProcID(pm.generateProcessID());

                // Add process to this node's queue
                pm.addProcessToQueue(process);

                // Add ourself as the listener
                process.addListener(pm);
                
                // Start the process
                new Thread(process).start();
        }
}
