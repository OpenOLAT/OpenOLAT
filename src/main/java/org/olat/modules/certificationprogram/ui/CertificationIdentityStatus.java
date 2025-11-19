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

import java.util.Date;

import org.olat.course.certificate.Certificate;

/**
 * 
 * Initial date: 14 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public enum CertificationIdentityStatus {
	
	CERTIFIED,
	RECERTIFYING,
	REMOVED;
	
	public static final CertificationIdentityStatus valueOf(CertificationStatus status) {
		return switch(status) {
			case VALID -> CertificationIdentityStatus.CERTIFIED;
			case EXPIRED_RENEWABLE -> CertificationIdentityStatus.RECERTIFYING;
			case EXPIRED, REVOKED, ARCHIVED -> CertificationIdentityStatus.REMOVED;
			default -> null;
		};
	}
	
	public static CertificationIdentityStatus evaluate(Certificate certificate, Date referenceDate) {
		if(certificate.isLast()) {
			Date nextRecertificationDate = certificate.getNextRecertificationDate();
			Date recertificationWindowDate = certificate.getRecertificationWindowDate();
			if(nextRecertificationDate == null || nextRecertificationDate.compareTo(referenceDate) >= 0) {
				return CERTIFIED;
			}
			if(recertificationWindowDate != null && recertificationWindowDate.compareTo(referenceDate) >= 0) {
				return RECERTIFYING;
			}
			return REMOVED;
		}
		return REMOVED;
	}
}
