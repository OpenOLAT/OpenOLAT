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
package org.olat.course.wizard.provider.exam;

/**
 * 
 * Initial date: 11 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ExamCourseSteps {
	
	private boolean retest;
	private boolean certificate;
	private boolean disclaimer;
	private boolean coaches;
	private boolean participants;

	public boolean isRetest() {
		return retest;
	}

	public void setRetest(boolean retest) {
		this.retest = retest;
	}

	public boolean isCertificate() {
		return certificate;
	}

	public void setCertificate(boolean certificate) {
		this.certificate = certificate;
	}
	
	public boolean isDisclaimer() {
		return disclaimer;
	}
	
	public void setDisclaimer(boolean disclaimer) {
		this.disclaimer = disclaimer;
	}

	public boolean isCoaches() {
		return coaches;
	}

	public void setCoaches(boolean coaches) {
		this.coaches = coaches;
	}

	public boolean isParticipants() {
		return participants;
	}

	public void setParticipants(boolean participants) {
		this.participants = participants;
	}
	
}
