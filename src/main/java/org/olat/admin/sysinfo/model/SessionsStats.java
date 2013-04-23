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
