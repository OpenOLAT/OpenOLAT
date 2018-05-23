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
package org.olat.modules.forms;

import java.util.Date;

/**
 * 
 * Initial date: 23.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormStatistic {
	
	private long numOfDoneSessions;
	private Date firstSubmission;
	private Date lastSubmission;
	private long averageDuration;
	private long[] durations;
	
	public long getNumOfDoneSessions() {
		return numOfDoneSessions;
	}
	
	public void setNumOfDoneSessions(long numOfDoneSessions) {
		this.numOfDoneSessions = numOfDoneSessions;
	}
	
	public Date getFirstSubmission() {
		return firstSubmission;
	}

	public void setFirstSubmission(Date firstSubmission) {
		this.firstSubmission = firstSubmission;
	}

	public Date getLastSubmission() {
		return lastSubmission;
	}

	public void setLastSubmission(Date lastSubmission) {
		this.lastSubmission = lastSubmission;
	}

	public long getAverageDuration() {
		return averageDuration;
	}
	
	public void setAverageDuration(long averageDuration) {
		this.averageDuration = averageDuration;
	}
	
	public long[] getDurations() {
		return durations;
	}
	
	public void setDurations(long[] durations) {
		this.durations = durations;
	}

}
