/**
 * AddHostRequest: Service a request to add a new host in the cluster
 */
package com.company;

import java.util.List;

public class AddHostRequest extends Request {

	private List<String> hosts;
	
	public AddHostRequest(List<String> hostList) {
		hosts = hostList;
	}
	
	public List<String> getHostList() {
		return hosts;
	}
}
