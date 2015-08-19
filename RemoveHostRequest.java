/**
 * RemoveHostRequest: Service a request to remove a particular host
 * from the hostlist
 */
package com.company;

import java.util.List;

public class RemoveHostRequest extends Request {

        private String host;

        public RemoveHostRequest(String host) {
                this.host = host;
        }

        public String getHost() {
                return host;
        }
}
