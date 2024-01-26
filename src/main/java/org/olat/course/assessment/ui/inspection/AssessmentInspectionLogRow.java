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
package org.olat.course.assessment.ui.inspection;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentInspection;
import org.olat.course.assessment.AssessmentInspectionLog;
import org.olat.course.assessment.AssessmentInspectionLog.Action;
import org.olat.course.assessment.manager.AssessmentInspectionXStream;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 8 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionLogRow extends UserPropertiesRow {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentInspectionLogRow.class);
	
	private final AssessmentInspectionLog inspectionLog;
	
	private AssessmentInspection inspectionBefore;
	private AssessmentInspection inpsectionAfter;
	
	public AssessmentInspectionLogRow(AssessmentInspectionLog inspectionLog,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(inspectionLog.getDoer(), userPropertyHandlers, locale);
		this.inspectionLog = inspectionLog;
		
		if(StringHelper.containsNonWhitespace(inspectionLog.getBefore())) {
			try {
				inspectionBefore = AssessmentInspectionXStream
						.fromXml(inspectionLog.getBefore(), AssessmentInspection.class);
			} catch (Exception e) {
				log.debug("", e);
			}
		}
		
		if(StringHelper.containsNonWhitespace(inspectionLog.getAfter())) {
			try {
				inpsectionAfter = AssessmentInspectionXStream
						.fromXml(inspectionLog.getAfter(), AssessmentInspection.class);
			} catch (Exception e) {
				log.debug("", e);
			}
		}
	}
	
	public Date getCreationDate() {
		return inspectionLog.getCreationDate();
	}
	
	public Action getAction() {
		return inspectionLog.getAction();
	}
	
	public String getRawBefore() {
		return inspectionLog.getBefore();
	}
	
	public String getRawAfter() {
		return inspectionLog.getAfter();
	}
	
	public AssessmentInspection getInspectionBefore() {
		return inspectionBefore;
	}
	
	public AssessmentInspection getInspectionAfter() {
		return inpsectionAfter;
	}
}
