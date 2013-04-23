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
package org.olat.admin.sysinfo.manager;

import java.util.ArrayList;
import java.util.List;

import org.olat.admin.sysinfo.model.SessionStatsSample;
import org.olat.admin.sysinfo.model.SessionsStats;
import org.olat.core.util.UserSession;
import org.olat.core.util.session.UserSessionManager;
import org.olat.restapi.system.Sampler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * <h3>Description:</h3>
 * 
 * Initial Date:  21 juin 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 *
 */
@Service
public class SessionStatsManager implements Sampler {
	
	@Autowired
	private	UserSessionManager sessionManager;
	
	private SessionStatsSample currentSample;
	private List<SessionStatsSample> sessionStatsSamples = new ArrayList<SessionStatsSample>();

	public List<SessionStatsSample> getSessionViews() {
		return sessionStatsSamples;
	}
	
	public synchronized void incrementAuthenticatedClick() {
		if(currentSample == null) {
			currentSample = new SessionStatsSample(sessionManager.getNumberOfAuthenticatedUserSessions());
		}
		currentSample.incrementAuthenticatedClick();
	}
	
	public synchronized void incrementAuthenticatedPollerClick() {
		if(currentSample == null) {
			currentSample = new SessionStatsSample(sessionManager.getNumberOfAuthenticatedUserSessions());
		}
		currentSample.incrementAuthenticatedPollerCalls();
	}
	
	public synchronized void incrementRequest() {
		if(currentSample == null) {
			currentSample = new SessionStatsSample(sessionManager.getNumberOfAuthenticatedUserSessions());
		}
		currentSample.incrementRequest();
	}
	
	public synchronized long getNumOfSessions() {
		if(currentSample != null) {
			return currentSample.getNumOfSessions();
		}
		return 0l;
	}
	
	public long getActiveSessions(int numOfSeconds) {
		long diff = numOfSeconds * 1000;
		
		List<UserSession> authUserSessions = new ArrayList<UserSession>(sessionManager.getAuthenticatedUserSessions());
		long now = System.currentTimeMillis();
		long counter = 0;
		for (UserSession usess : authUserSessions) {
			long lastklick = usess.getSessionInfo() == null ? -1 : usess.getSessionInfo().getLastClickTime();
			if ((now - lastklick) <= diff) {
				counter++;
			}
		}
		return counter;
	}
	
	public SessionsStats getSessionsStatsLast(int numOfSeconds) {
		if(currentSample == null) {
			return new SessionsStats();
		}

		double polls = 0l;
		double clicks = 0l;
		double requests = 0l;

		long lastTime = 0l;
		long fromTimestamp = 0l;
		
		synchronized(this) {
			if(sessionStatsSamples.isEmpty()) {
				return new SessionsStats();
			}
		
			polls = currentSample.getAuthenticatedPollerCalls();
			clicks = currentSample.getAuthenticatedClick();
			requests = currentSample.getRequests();
			fromTimestamp = lastTime = System.currentTimeMillis();
			
			double toTimestamp = fromTimestamp - (numOfSeconds * 1000.0d);
			for(int i=sessionStatsSamples.size(); i-->0 && lastTime > toTimestamp; ) {
				SessionStatsSample lastSample = sessionStatsSamples.get(i);
				polls += lastSample.getAuthenticatedPollerCalls();
				clicks += lastSample.getAuthenticatedClick();
				requests += lastSample.getRequests();
				lastTime = lastSample.getTimestamp();
			}	
		}
		
		double duration = (fromTimestamp - lastTime) / 1000;
		double pollPerSlot = (polls / duration) * (double)numOfSeconds;
		double clickPerSlot = (clicks / duration) * (double)numOfSeconds;
		double requestPerSlot = (requests / duration) * (double)numOfSeconds;
		
		SessionsStats stats = new SessionsStats();
		stats.setAuthenticatedClickCalls(Math.round(clickPerSlot));
		stats.setAuthenticatedPollerCalls(Math.round(pollPerSlot));
		stats.setRequests(Math.round(requestPerSlot));
		return stats;
	}
	
	public synchronized SessionStatsSample getLastSample() {
		return sessionStatsSamples.isEmpty() ? null : sessionStatsSamples.get(sessionStatsSamples.size() - 1);
	}

	@Override
	public synchronized void takeSample() {
		if(currentSample != null) {
			sessionStatsSamples.add(currentSample);
		}
		currentSample = new SessionStatsSample(sessionManager.getNumberOfAuthenticatedUserSessions());
		while(sessionStatsSamples.size() >= 1000) {
			sessionStatsSamples.remove(0);
		}
	}
	

	

}
