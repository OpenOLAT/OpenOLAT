/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.ui;

import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramRef;
import org.olat.modules.certificationprogram.CertificationProgramStatusEnum;
import org.olat.modules.certificationprogram.RecertificationMode;
import org.olat.modules.certificationprogram.ui.component.Duration;
import org.olat.modules.creditpoint.CreditPointSystem;

/**
 * 
 * Initial date: 26 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramRow implements CertificationProgramRef {

	private final long activeUsers;
	private final long candidates;
	private final long removedUsers;
	private final String creditPoints;
	private final CreditPointSystem creditPointSystem;
	private final CertificationProgram certificationProgram;
	
	public CertificationProgramRow(CertificationProgram certificationProgram,
			long activeUsers, long candidates, long removedUsers, String creditPoints) {
		creditPointSystem = certificationProgram.getCreditPointSystem();
		this.certificationProgram = certificationProgram;
		this.creditPoints = creditPoints;
		this.candidates = candidates;
		this.activeUsers = activeUsers;
		this.removedUsers = removedUsers;
	}
	
	@Override
	public Long getKey() {
		return certificationProgram.getKey();
	}
	
	public String getDisplayName() {
		return certificationProgram.getDisplayName();
	}
	
	public String getIdentifier() {
		return certificationProgram.getIdentifier();
	}
	
	public RecertificationMode getRecertificationMode() {
		return certificationProgram.getRecertificationMode();
	}
	
	public Duration getValidityPeriod() {
		return certificationProgram.isValidityEnabled()
				? new Duration(certificationProgram.getValidityTimelapse(), certificationProgram.getValidityTimelapseUnit())
				: null;
	}
	
	public CertificationProgramStatusEnum getStatus() {
		return certificationProgram.getStatus();
	}
	
	public long getActiveUsers() {
		return activeUsers;
	}
	
	public long getCandidates() {
		return candidates;
	}

	public long getRemovedUsers() {
		return removedUsers;
	}
	
	public String getCreditPoints() {
		return creditPoints;
	}
	
	public CreditPointSystem getCreditPointSystem() {
		return creditPointSystem;
	}

	public CertificationProgram getCertificationProgram() {
		return certificationProgram;
	}

}
