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
package org.olat.modules.lecture;

import java.util.Date;

/**
 * 
 * Initial date: 11 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface LectureBlockAuditLog {
	
	public Date getCreationDate();
	
	public String getAction();
	
	public String getBefore();

	public String getAfter();
	
	public String getMessage();
	
	public Long getLectureBlockKey();
	
	public Long getRollCallKey();
	
	public Long getEntryKey();
	
	public Long getAbsenceNoticeKey();

	public Long getIdentityKey();

	public Long getAuthorKey();
	
	public enum Action {
		autoclose,
		saveLectureBlock,
		createLectureBlock,
		updateLectureBlock,
		cancelLectureBlock,
		closeLectureBlock,
		reopenLectureBlock,

		createRollCall,
		addToRollCall,
		removeFromRollCall,
		updateAuthorizedAbsence,
		updateRollCall,
		adaptRollCall,
		
		updateSummary,
		removeCustomRate,
		
		sendAppeal,
		
		createAbsenceNotice,
		updateAbsenceNotice,
		deleteAbsenceNotice,
		createAbsenceNoticeRelations,
		updateAbsenceNoticeRelations,
		
	}

}
