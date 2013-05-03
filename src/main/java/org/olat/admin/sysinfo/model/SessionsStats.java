/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.admin.sysinfo.model;

public class SessionsStats {

	private long requests = 0l;
	private long authenticatedClickCalls = 0l;
	private long authenticatedPollerCalls = 0l;
	
	public SessionsStats() {
		//
	}

	
	public long getRequests() {
		return requests;
	}
	
	public void setRequests(long requests) {
		this.requests = requests;
	}
	
	public long getAuthenticatedClickCalls() {
		return authenticatedClickCalls;
	}
	
	public void setAuthenticatedClickCalls(long authenticatedClickCalls) {
		this.authenticatedClickCalls = authenticatedClickCalls;
	}
	
	public long getAuthenticatedPollerCalls() {
		return authenticatedPollerCalls;
	}
	
	public void setAuthenticatedPollerCalls(long authenticatedPollerCalls) {
		this.authenticatedPollerCalls = authenticatedPollerCalls;
	}
	
	

}
