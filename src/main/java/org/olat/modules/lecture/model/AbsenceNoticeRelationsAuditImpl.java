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
package org.olat.modules.lecture.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.modules.lecture.AbsenceNoticeToLectureBlock;
import org.olat.modules.lecture.AbsenceNoticeToRepositoryEntry;
import org.olat.modules.lecture.LectureBlockRollCall;

/**
 * 
 * Initial date: 20 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbsenceNoticeRelationsAuditImpl {
	
	private List<LectureBlockRollCall> rollCalls;
	private List<AbsenceNoticeToLectureBlock> noticeToBlocks;
	private List<AbsenceNoticeToRepositoryEntry> noticeToEntries;
	
	public List<AbsenceNoticeToLectureBlock> getNoticeToBlocks() {
		return noticeToBlocks;
	}
	
	public void setNoticeToBlocks(List<AbsenceNoticeToLectureBlock> noticeToBlocks) {
		this.noticeToBlocks = noticeToBlocks == null ? new ArrayList<>(1) : new ArrayList<>(noticeToBlocks);
	}
	
	public List<AbsenceNoticeToRepositoryEntry> getNoticeToEntries() {
		return noticeToEntries;
	}
	
	public void setNoticeToEntries(List<AbsenceNoticeToRepositoryEntry> noticeToEntries) {
		this.noticeToEntries = noticeToEntries == null ? new ArrayList<>(1) : new ArrayList<>(noticeToEntries);
	}

	public List<LectureBlockRollCall> getRollCalls() {
		return rollCalls;
	}

	public void setRollCalls(List<LectureBlockRollCall> rollCalls) {
		this.rollCalls = rollCalls == null ? new ArrayList<>(1) : new ArrayList<>(rollCalls);
	}
	
	
}
