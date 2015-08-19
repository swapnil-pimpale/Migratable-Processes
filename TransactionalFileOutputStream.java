/**
 * TransactionalFileOutputStream
 * Output part of the transactional I/O library
 * It implements Serializable and extends OutputStream
 * We implement the write() and writeString() functions. We use the
 * RandomAccessFile object which helps seeking to a particular file offset
 * and getting the current file offset.
 */
package com.company;
import java.io.*;


public class TransactionalFileOutputStream extends OutputStream implements Serializable{

        // Output File path
        private String outputFilePath;

        // Random Access File handler. Transient because we don't want to serialize
	private transient RandomAccessFile raHandler;

        // We store the IO state here
	private long fileOffset;

        // Migrated flag - to check if this object has been migrated
        private boolean migrated;
	
	public TransactionalFileOutputStream(String outFile) {
                this.outputFilePath = outFile;
                this.fileOffset = 0;
                this.migrated = false;
	}
	
	public void setFlag(boolean flag) {
		migrated = flag;
	}
	
	@Override
	public void write(int b) throws IOException {
                if (migrated || raHandler == null) {
                        try {
                                raHandler = new RandomAccessFile(outputFilePath, "rw");

                                // Seek to the fileOffset
                                raHandler.seek(fileOffset);

                                // Set migrated to false
                                migrated = false;
                        } catch (FileNotFoundException e) {
                                System.err.println("Error: Could not find or create "
                                + "new file named " +outputFilePath+ ".");
                                e.printStackTrace();
                        }
                }
		raHandler.write(b);
		fileOffset = raHandler.getFilePointer();
	}
	
	//Character encoding is a concern..
	public void writeString(String s) throws IOException {
                if (migrated || raHandler == null) {
                        try {
                                raHandler = new RandomAccessFile(outputFilePath, "rw");

                                // Seek to the fileOffset
                                raHandler.seek(fileOffset);

                                // Set migrated to false
                                migrated = false;
                        } catch (FileNotFoundException e) {
                                System.err.println("Error: Could not find or create "
                                + "new file named " +outputFilePath+ ".");
                                e.printStackTrace();
                        }
                }

		if(s != null) {
			s += System.getProperty("line.separator");
			raHandler.writeBytes(s);
			fileOffset = raHandler.getFilePointer();
		}
	}

	public void closeFile() {
		try {
			if(raHandler!=null) {
				raHandler.close();
			}
			this.close();
		} catch (IOException e) {
			System.err.println("Failed to close output file.");
			e.printStackTrace();
		}
	}
}
