/**
 * GrepProcess: This is modified version of the grep process provided with the assignment.
 * We have modified it to be compatible with our framework.
 * It takes a pattern and searches for it in an input file and when found writes the line
 * into an output file
 */

package com.company;
import java.io.EOFException;
import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;

@SuppressWarnings("serial")
public class GrepProcess extends MigratableProcess {
	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	private String query;

	public GrepProcess(String args[]) throws Exception {
		super(args);
		
		if(args.length!=4) {
			System.out.println("Synopsis: GrepProcess [pattern] [inputFile] [outputFile]");
			throw new Exception("Invalid Arguments");
		}
		
		query = args[0];
		inFile = new TransactionalFileInputStream(args[1]);
		outFile = new TransactionalFileOutputStream(args[2]);
	}

	@Override
	public void run() {
		try {
			while (!suspend_flag && !should_quit) {
				String line = inFile.readLine();
				if (line == null) break;
				
				if (line.contains(query)) {
					outFile.writeString(line);
				}
				
				// Make grep take longer so that we don't require extremely large files for interesting results
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// ignore it
				}
			}
			
			inFile.closeFile();
			outFile.closeFile();
                        suspend_flag = false;
		} catch (EOFException e) {
			//End of File
		} catch (IOException e) {
			System.out.println ("GrepProcess: Error: " + e);
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