/**
 * SortFileProcess: This process takes an input file, sorts it
 * and then writes the sorted output to an output file.
 * The motivation behind choosing sorting process is because we can do the following
 * 1) Start a sort operation on node1
 * 2) Suspend it and migrate the process to node2
 * 3) Resume the process on node2
 * 4) Verify the sorted output on node2
 */

package com.company;

import java.io.IOException;
import java.util.Arrays;

public class SortFileProcess extends MigratableProcess {

        private TransactionalFileInputStream inFile;
        private TransactionalFileOutputStream outFile;

        public SortFileProcess(String[] args) throws Exception
        {
                super(args);
                if (args.length != 3) {
                        System.out.println("Synopsis: SortFileProcess <inputFile> <outputFile>");
                        throw new Exception("Invalid Parameters");
                }

                inFile = new TransactionalFileInputStream(args[0]);
                outFile = new TransactionalFileOutputStream(args[1]);
        }

        @Override
        public void run()
        {
                try {
                        while (!suspend_flag && !should_quit) {
                                String line = inFile.readLine();

                                if (line == null)
                                        break;

                                // Store the line in a character array
                                char[] char_arr = line.toCharArray();

                                // Sort the byte array
                                Arrays.sort(char_arr);

                                // Store the sorted array back in a string
                                line = new String(char_arr);

                                // Write the string to the output file
                                outFile.writeString(line);

                                // Make the process run longer
                                try {
                                        Thread.sleep(500);
                                } catch (InterruptedException e) {
                                        // ignore it
                                }
                        }

                        inFile.closeFile();
                        outFile.closeFile();
                        suspend_flag = false;
                } catch (IOException e) {
                        System.out.println("SortProcess Error: " + e);
                } finally {
                        signalListeners();
                }
        }

        public void migrationPreProcess()
        {
                inFile.setFlag(true);
                outFile.setFlag(true);
        }
}