/**
 * MigrateRequest: Service a request to migrate a process
 */

package com.company;

public class MigrateRequest extends Request {
	private int processID;
	private String hostTo;

	public MigrateRequest(int processID, String hostTo) {
		this.processID = processID;
		this.hostTo = hostTo;
	}
	
	public int getProcessID() {
		return processID;
	}
	
	public String getHostTo() {
		return hostTo;
	}
}
