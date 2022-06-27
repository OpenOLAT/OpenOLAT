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

import java.util.List;

import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.RollCallSecurityCallback;

/**
 * 
 * Initial date: 11 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RollCallSecurityCallbackImpl implements RollCallSecurityCallback {

	private final boolean repoAdmin;
	private final boolean masterCoach;
	private final boolean teacher;
	private LectureBlock lectureBlock;
	private final LectureModule lectureModule;
	
	public RollCallSecurityCallbackImpl(boolean repoAdmin, boolean masterCoach, boolean teacher,
			LectureBlock lectureBlock, LectureModule lectureModule) {
		this.repoAdmin = repoAdmin;
		this.teacher = teacher;
		this.masterCoach = masterCoach;
		this.lectureBlock = lectureBlock;
		this.lectureModule = lectureModule;
	}
	
	@Override
	public void updateLectureBlock(LectureBlock block) {
		lectureBlock = block;
	}

	@Override
	public boolean canEdit() {
		if(isClosed() || isCancelled()) return false;
		return (repoAdmin || teacher) && isBlockEditable();
	}

	@Override
	public boolean canExport() {
		return repoAdmin || teacher || masterCoach;
	}

	@Override
	public boolean canViewDetails() {
		return repoAdmin || teacher || masterCoach;
	}

	@Override
	public boolean canClose(List<LectureBlock> blocks) {
		if(blocks == null || blocks.isEmpty()) return false;
		
		boolean allOk = true;
		if(repoAdmin || teacher) {
			for(LectureBlock block:blocks) {
				if(isClosed(block) || isCancelled(block) || !isBlockEditable(block)) {
					allOk &= false;
				} 
			}
		}
		return allOk;
	}

	@Override
	public boolean canViewAuthorizedAbsences() {
		boolean autorizedAbsenceAllowed = lectureModule.isAuthorizedAbsenceEnabled();
		if(autorizedAbsenceAllowed) {
			if(repoAdmin) {
				return true;
			}
			return !isBlockEditable() && lectureModule.isTeacherCanAuthorizedAbsence();
		}
		return false;
	}

	@Override
	public boolean canEditAuthorizedAbsences() {
		if(isClosed() || isCancelled()) return false;
		boolean autorizedAbsenceAllowed = lectureModule.isAuthorizedAbsenceEnabled();
		if(autorizedAbsenceAllowed) {
			if(repoAdmin) {
				return isBlockEditable();
			}
			return isBlockEditable() && lectureModule.isTeacherCanAuthorizedAbsence();
		}
		return false;
	}

	@Override
	public boolean canEditAbsences() {
		if(isClosed() || isCancelled()) return false;
		return repoAdmin || (teacher && isBlockEditable());
	}
	
	@Override
	public boolean canReopen() {
		return (repoAdmin || masterCoach) && (isClosed() || isCancelled());
	}

	private boolean isBlockEditable() {
		return isBlockEditable(lectureBlock);
	}
	
	private boolean isBlockEditable(LectureBlock block) {
		return block.getRollCallStatus() == LectureRollCallStatus.open
			|| block.getRollCallStatus() == LectureRollCallStatus.reopen;
	}
	
	private boolean isCancelled() {
		return isCancelled(lectureBlock);
	}
	
	private boolean isCancelled(LectureBlock block) {
		return block.getStatus() == LectureBlockStatus.cancelled;
	}
	
	private boolean isClosed() {
		return isClosed(lectureBlock);
	}
	
	private boolean isClosed(LectureBlock block) {
		return block.getRollCallStatus() == LectureRollCallStatus.closed
			|| block.getRollCallStatus() == LectureRollCallStatus.autoclosed;
	}
}
