/**
 * ProcessListResponse: Response for a 'ps' request and send a list of all
 * processes running on a node
 */
package com.company;

import java.util.ArrayList;
import java.util.List;

public class ProcessListResponse extends Response {
	
	private List<MigratableProcess> list = new ArrayList<MigratableProcess>();
	
	public ProcessListResponse(int errorCode, Object[] processes) {
		super(errorCode);
		populateList(processes);
	}

	private void populateList(Object[] processes) {
		for(int i = 0; i < processes.length; i++) {
			list.add((MigratableProcess) processes[i]);
		}
	}

	public List<MigratableProcess> getList() {
		return list;
	}
}
