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
package org.olat.course.assessment.ui.mode;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Status;

/**
 * 
 * Initial date: 13 juin 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeHelper {
	
	private final Translator translator;
	
	public AssessmentModeHelper(Translator translator) {
		this.translator = translator;
	}
	
	public String getCssClass(AssessmentMode mode) {
		return getStatus(mode).cssClass();
	}
	
	public String getStatusLabel(AssessmentMode mode) {
		Status status = getStatus(mode);
		return getStatusLabel(status);
	}
	
	public String getStatusLabel(Status status) {
		return translator.translate("assessment.mode.status.".concat(status.name()));
	}
	
	public Status getStatus(AssessmentMode mode) {
		return mode != null && mode.getStatus() != null ? mode.getStatus() : Status.none;
	}
	
	public String getBeginEndDate(AssessmentMode mode) {
		Date begin = mode.getBegin();
		Date end = mode.getEnd();
		Formatter formatter = Formatter.getInstance(translator.getLocale());

		String[] args = new String[] {
			formatter.formatDate(begin),				// 0
			formatter.formatTimeShort(begin),			// 1
			formatter.formatDate(end),				// 0
			formatter.formatTimeShort(end),				// 2
			Integer.toString(mode.getLeadTime()),		// 3
			Integer.toString(mode.getFollowupTime())	// 4
		};
		
		String i18nKey;
		if(DateUtils.isSameDay(begin, end)) {
			i18nKey = "date.and.time.text.same.day";
		} else {
			i18nKey = "date.and.time.text";
		}
		return translator.translate(i18nKey, args);
	}
}
