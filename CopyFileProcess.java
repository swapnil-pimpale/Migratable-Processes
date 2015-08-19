/**
 * CopyFileProcess: This process reads from an input file and writes the
 * content to an output file
 * The motivation behind choosing a copy process is because we can do the following:
 * 1) Start a copy operation on node1
 * 2) Suspend it and migrate the process to node2
 * 3) Resume the process on node2
 * 4) Verify the correctness of the copied file on node2
 */

package com.company;

import java.io.IOException;

public class CopyFileProcess extends MigratableProcess {

	private TransactionalFileInputStream inFile;
	private TransactionalFileOutputStream outFile;
	
	public CopyFileProcess(String[] args) throws Exception {
		super(args);
		
		if(args.length != 3) {
			System.out.println("Synopsis: CopyProcess <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		
		inFile = new TransactionalFileInputStream(args[0]);
		outFile = new TransactionalFileOutputStream(args[1]);
	}

	@Override
	public void run() {
		try {
			while (!suspend_flag && !should_quit) {

                                // Read a byte from the input file
				int read = inFile.read();
				
				if(read == -1)
					break;

                                // write it to the output file
				outFile.write(read);
				
				// Make copy take longer so that we don't require extremely large files for interesting results
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore it
				}
			}
			
			inFile.closeFile();
			outFile.closeFile();
                        suspend_flag = false;
		} catch (IOException e) {
			System.out.println("CopyFileProcess: Error: " + e);
		} finally {
                        signalListeners();
                }
	}

	@Override
	public void migrationPreProcess() {
		inFile.setFlag(true);
		outFile.setFlag(true);
	}
}
