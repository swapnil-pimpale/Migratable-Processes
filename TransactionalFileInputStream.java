/**
 * TransactionalFileInputStream
 * Input part of the transactional I/O library
 * It implements Seriablizable and extends InputStream
 * We implement the read() and readLine() functions. We use the
 * RandomAccessFile object which helps seeking to a particular file offset
 * and getting the current file offset.
 */
package com.company;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;


public class TransactionalFileInputStream extends InputStream implements Serializable {

        // Input File path
        private String inputFile;

        // Random Access File Handler. Transient because we don't want to serialize
	private transient RandomAccessFile raHandler;

        // We store the IO state here
	private long fileOffset;

        // Migrated flag - to check if this object has been migrated
        private boolean migrated;
	
	public TransactionalFileInputStream(String inFile) {
                this.inputFile = inFile;
                this.fileOffset = 0;
                this.migrated = false;
	}
	
	public void setFlag(boolean flag) {
		migrated = flag;
	}

	@Override
	public int read() throws IOException {
                int byteRead = -1;

                try {
                        if (migrated || raHandler == null) {
                                raHandler = new RandomAccessFile(inputFile, "r");
                                raHandler.seek(fileOffset);
                                migrated = false;
                        }

                        byteRead = raHandler.read();
                        fileOffset = raHandler.getFilePointer();
                } catch (FileNotFoundException e) {
                        System.err.println("Error: Input file '" + inputFile
                                + "' could not be found.");
                }
		
		return byteRead;
	}
	
	//Character encoding is a concern..
	public String readLine() throws IOException {            
		String line = null;
		
		try {
			if(migrated || raHandler == null) {
				raHandler = new RandomAccessFile(inputFile, "r");
				raHandler.seek(fileOffset);
				migrated = false;
			}
			
			line = raHandler.readLine();
			fileOffset = raHandler.getFilePointer();
			
		} catch (FileNotFoundException e) {
                        System.err.println("Error: Input file " +inputFile+ " could not be found.");
		}
		
		return line;
	}

	public void closeFile() {
		try {
			if(raHandler!=null) {
				raHandler.close();
			}
			this.close();
		} catch (IOException e) {
			System.err.println("Failed to close input file.");
			e.printStackTrace();
		}
	}
}
