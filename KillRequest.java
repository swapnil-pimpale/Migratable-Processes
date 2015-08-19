/**
 * KillRequest: Service a request to kill a process
 */

package com.company;

public class KillRequest extends Request {

	private int processID;
	
	public KillRequest(int processID) {
		this.processID = processID;
	}

	public int getProcessID() {
		return processID;
	}
}
