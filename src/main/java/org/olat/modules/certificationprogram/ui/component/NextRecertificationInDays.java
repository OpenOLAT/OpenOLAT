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
package org.olat.modules.certificationprogram.ui.component;

import java.util.Date;

import org.olat.core.util.DateUtils;
import org.olat.course.certificate.Certificate;

/**
 * 
 * Initial date: 22 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public record NextRecertificationInDays(Long days, Long overdueDays) {
	
	public boolean isNotRenewable() {
		return days != null && overdueDays != null && overdueDays.longValue() < 0l;
	}
	
	public static NextRecertificationInDays valueOf(Certificate certificate, Date referenceDate) {
		Long days = certificate.getNextRecertificationDate() == null
				? null
				: DateUtils.countDays(referenceDate, certificate.getNextRecertificationDate());
		
		Long overdueDays = null;
		if(certificate.getNextRecertificationDate() != null && certificate.getRecertificationWindowDate() != null
				&& certificate.getNextRecertificationDate().compareTo(referenceDate) < 0) {
			overdueDays = DateUtils.countDays(referenceDate, certificate.getRecertificationWindowDate());
		}
		
		return new NextRecertificationInDays(days, overdueDays);
	}

}
