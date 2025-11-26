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
package org.olat.course.certificate.ui;

import java.util.Date;

import org.olat.core.util.DateUtils;
import org.olat.course.certificate.Certificate;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 22 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public record RecertificationInDays(Date nextRecertificationDate, Long days,
		Date startDateRecertification, Long startDays,
		Date endDateOfRecertificationWindow, Long windowDays, Boolean windowOpen,
		boolean isCourse) {
	
	public boolean isBeforeRecertification(Date referenceDate) {
		return nextRecertificationDate() != null && nextRecertificationDate().compareTo(referenceDate) > 0;
	}
	
	public boolean isRecertificationOpen(Date referenceDate) {
		return nextRecertificationDate() != null && nextRecertificationDate().compareTo(referenceDate) <= 0
				&& (endDateOfRecertificationWindow() == null || (endDateOfRecertificationWindow().compareTo(referenceDate) >= 0));
	}
	
	public boolean isRecertificationWindowClosed(Date referenceDate) {
		return nextRecertificationDate() != null && nextRecertificationDate().compareTo(referenceDate) > 0
				&& (endDateOfRecertificationWindow() != null && (endDateOfRecertificationWindow().compareTo(referenceDate) < 0));
	}
	
	public boolean isWarning() {
		if(days() != null && days().longValue() > 0) {
			return false;
		}
		if(endDateOfRecertificationWindow() == null) {
			return days() <= 0 && !isCourse;
		}
		return windowDays() != null && windowDays().longValue() >= 0l;
	}
	
	public boolean isDanger() {
		if(days() != null && days().longValue() > 0) {
			return false;
		}
		return windowDays() != null && windowDays().longValue() < 0l;
	}
	
	public boolean isRecertificationWindowOpen() {
		return windowOpen() != null && windowOpen().booleanValue();
	}
	
	public boolean isRecertificationWindowClosed() {
		return windowOpen() != null && !windowOpen().booleanValue();
	}

	public static RecertificationInDays valueOf(Date nextRecertificationDate, Date startRecertificationDate, RepositoryEntry entry, Date referenceDate) {
		// Eternal
		if(nextRecertificationDate == null
				|| entry == null) {
			return null;
		}
		
		Long days = DateUtils.countDays(referenceDate, nextRecertificationDate);
		Long startDays = DateUtils.countDays(referenceDate, startRecertificationDate);
		return new RecertificationInDays(nextRecertificationDate, days, startRecertificationDate, startDays, null, null, null, true);
	}
	
	public static RecertificationInDays valueOf(Certificate certificate, CertificationProgram program, Date referenceDate) {
		// Eternal
		if(certificate.getNextRecertificationDate() == null
				|| program == null
				|| !program.isValidityEnabled()) {
			return null;
		}
		
		Long days = null;
		Long windowDays = null;
		Date endOfWindow = null;
		Boolean windowOpen = null;
		if(program.isRecertificationEnabled()) {
			if(program.getRecertificationWindowUnit() != null && program.getRecertificationWindow() > 0) {
				// Check if the certificate can be renewed
				endOfWindow = program.getRecertificationWindowUnit().toDate(certificate.getNextRecertificationDate(), program.getRecertificationWindow());
				windowDays = DateUtils.countDays(referenceDate, endOfWindow);
				if(certificate.getNextRecertificationDate().compareTo(referenceDate) <= 0 && endOfWindow.compareTo(referenceDate) >= 0) {
					windowOpen = Boolean.TRUE;
				} else if(certificate.getNextRecertificationDate().compareTo(referenceDate) <= 0 && endOfWindow.compareTo(referenceDate) < 0) {
					windowOpen = Boolean.FALSE;
				}
						
				if(endOfWindow.compareTo(referenceDate) > 0) {
					days = DateUtils.countDays(referenceDate, certificate.getNextRecertificationDate());
				}
			} else {
				days = DateUtils.countDays(referenceDate, certificate.getNextRecertificationDate());
			}
		}
		
		Date nextRecertificationDate = certificate.getNextRecertificationDate();
		return new RecertificationInDays(nextRecertificationDate, days,
				nextRecertificationDate, days, endOfWindow, windowDays, windowOpen, false);
	}
}
