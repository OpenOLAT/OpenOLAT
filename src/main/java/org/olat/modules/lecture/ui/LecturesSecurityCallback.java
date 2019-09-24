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

/**
 * Very simple security callback for the principals
 * 
 * 
 * Initial date: 20 juil. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface LecturesSecurityCallback {
	
	public boolean canNewLectureBlock();
	
	public boolean canChangeRates();
	
	public boolean canSeeAppeals();
	
	public boolean canApproveAppeal();
	
	public boolean canEditConfiguration();
	
	public boolean canAuthorizeAbsence();
	
	public boolean canAddAbsences();
	
	public boolean canAddNoticeOfAbsences();
	
	public boolean canAddDispensations();

	public boolean canEditAbsenceNotices();
	
	public boolean canDeleteAbsenceNotices();
	
	public LectureRoles viewAs();

}
