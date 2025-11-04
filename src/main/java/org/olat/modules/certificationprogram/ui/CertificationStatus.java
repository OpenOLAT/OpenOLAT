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

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.DateUtils;
import org.olat.course.certificate.Certificate;

/**
 * 
 * Initial date: 3 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum CertificationStatus {
	
	/** Flag last=true on certificate */
	VALID,
	/** Flag last=true on certificate but next certification date */
	EXPIRED,
	/** Flag last=true on certificate but next certification date */
	EXPIRED_RENEWABLE,
	/** Flag paused on certificate */
	PAUSED,
	/** Flag last=true on certificate but next certification date, and window for recertification */
	NOT_RENEWABLE,
	/** Only flag last=false */
	REVOKED,
	
	ARCHIVED;

	public static CertificationStatus evaluate(Certificate certificate, Date referenceDate) {
		if(certificate.isLast()) {
			if(certificate.isRecertificationPaused()) {
				return PAUSED;
			}
			
			Date nextRecertificationDate = certificate.getNextRecertificationDate();
			Date recertificationWindowDate = certificate.getRecertificationWindowDate();
			if(nextRecertificationDate == null || nextRecertificationDate.compareTo(referenceDate) >= 0) {
				return VALID;
			}
			
			if(recertificationWindowDate != null) {
				if(recertificationWindowDate.compareTo(referenceDate) < 0) {
					return NOT_RENEWABLE;
				}
				return EXPIRED_RENEWABLE;
			}
			return EXPIRED;
		}
		return REVOKED;
	}
	
	public String asLabelExplained(Certificate certificate, Date referenceDate, Translator translator) {
		if(this == CertificationStatus.EXPIRED) {
			if(certificate.getNextRecertificationDate() != null
					&& certificate.getNextRecertificationDate().compareTo(referenceDate) < 0) {
				long overdueDays = DateUtils.countDays(certificate.getNextRecertificationDate(), referenceDate);
				return translator.translate("certification.status.expired.days", Long.toString(Math.abs(overdueDays)));
			}
			return translator.translate("certification.status.".concat(name().toLowerCase()));
		}
		return translator.translate("certification.status.".concat(name().toLowerCase()));
	}
}
