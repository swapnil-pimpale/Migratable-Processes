/**
 * Respnse: The response class is used to send a response (errorCode) back over the socket
 * This way we can find out whether a command on the other node succeeded or not
 */

package com.company;

import java.io.Serializable;

public class Response implements Serializable {

	private int errorCode;
	
	public Response(int errorCode) {
		this.errorCode = errorCode;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
}
