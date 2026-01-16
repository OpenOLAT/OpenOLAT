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
package org.olat.modules.lecture.ui;

import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumRef;

/**
 * Very simple security callback for the principals
 * 
 * 
 * Initial date: 20 juil. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface LecturesSecurityCallback {
	
	boolean canNewLectureBlock(CurriculumElementRef element, CurriculumRef curriculum);
	
	boolean canEditLectureBlocks();
	
	boolean canEditLectureBlock(CurriculumElementRef element, CurriculumRef curriculum);
	
	boolean canChangeRates();
	
	boolean canSeeAppeals();
	
	boolean canSeeAbsencesInDailyOverview();
	
	boolean canSelectCoursesInDailyOverview();
	
	boolean canSeeStatisticsInDailyOverview();
	
	boolean canSeeWarningsAndAlertsInDailyOverview();
	
	boolean canApproveAppeal();
	
	boolean canEditConfiguration();
	
	boolean canAuthorizeAbsence();
	
	boolean canReopenLectureBlock();
	
	boolean canAddAbsences();
	
	boolean canAddNoticeOfAbsences();
	
	boolean canAddDispensations();
	
	boolean canViewLog();
	
	boolean canViewList();
	
	boolean canAssessmentMode();
	
	/**
	 * @return true if a E-mail to the teaches after creating a notice is mandatory
	 */
	boolean needToInformTeacher();

	boolean canEditAbsenceNotices();
	
	boolean canDeleteAbsenceNotices();
	
	LectureRoles viewAs();
	
	boolean isOnlineMeetingModerator();
	
	boolean isOnlineMeetingAdministrator();
	
	LecturesSecurityCallback readOnlyCopy();

}
