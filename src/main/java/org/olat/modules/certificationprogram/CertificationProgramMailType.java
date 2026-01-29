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
package org.olat.modules.certificationprogram;

import java.util.EnumSet;
import java.util.Set;

/**
 * 
 * Initial date: 11 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public enum CertificationProgramMailType {
	
	certificate_issued(CertificationProgramLogAction.send_notification_certificate_issued),
	certificate_renewed(CertificationProgramLogAction.send_notification_certificate_renewed),
	certificate_expired(CertificationProgramLogAction.send_notification_certificate_expired),
	certificate_revoked(CertificationProgramLogAction.send_notification_certificate_revoked),
	program_removed(CertificationProgramLogAction.send_notification_program_removed),
	reminder_upcoming(CertificationProgramLogAction.send_reminder_upcoming),
	reminder_overdue(CertificationProgramLogAction.send_reminder_overdue);
	
	private final CertificationProgramLogAction logAction;
	
	private CertificationProgramMailType(CertificationProgramLogAction logAction) {
		this.logAction = logAction;
	}
	
	public CertificationProgramLogAction logAction() {
		return logAction;
	}
	
	public static final CertificationProgramMailType[] notifications() {
		return new CertificationProgramMailType[]{ certificate_issued, certificate_renewed, certificate_expired, certificate_revoked, program_removed };
	}
	
	public static final Set<CertificationProgramMailType> notificationsSet() {
		return EnumSet.of(certificate_issued, certificate_renewed, certificate_expired, certificate_revoked, program_removed);
	}
	
	public static final Set<CertificationProgramMailType> notificationsSet(CertificationProgram program) {
		EnumSet<CertificationProgramMailType> set = EnumSet.noneOf(CertificationProgramMailType.class);
		set.add(certificate_issued);
		if(program.isRecertificationEnabled()) {
			set.add(certificate_renewed);
		}
		if(program.isValidityEnabled()) {
			set.add(certificate_expired);
		}
		set.add(certificate_revoked);
		set.add(program_removed);
		return set;
	}
}
