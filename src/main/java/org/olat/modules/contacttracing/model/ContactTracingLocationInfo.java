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
package org.olat.modules.contacttracing.model;

/**
 * Initial date: 15.09.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingLocationInfo {
	
	private long registrations;
	private long registrationsWithoutProof;
	private long registrationsWithClaimedProof;
	private long registrationsWithValidatedProof;
	
	public long getRegistrations() {
		return registrations;
	}
	public void setRegistrations(long registrations) {
		this.registrations = registrations;
	}
	public long getRegistrationsWithoutProof() {
		return registrationsWithoutProof;
	}
	public void setRegistrationsWithoutProof(long registrationsWithoutProof) {
		this.registrationsWithoutProof = registrationsWithoutProof;
	}
	public long getRegistrationsWithClaimedProof() {
		return registrationsWithClaimedProof;
	}
	public void setRegistrationsWithClaimedProof(long registrationsWithClaimedProof) {
		this.registrationsWithClaimedProof = registrationsWithClaimedProof;
	}
	public long getRegistrationsWithValidatedProof() {
		return registrationsWithValidatedProof;
	}
	public void setRegistrationsWithValidatedProof(long registrationsWithValidatedProof) {
		this.registrationsWithValidatedProof = registrationsWithValidatedProof;
	}
	
	
}
