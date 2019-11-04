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

import org.olat.core.CoreSpringFactory;
import org.olat.modules.lecture.LectureModule;

/**
 * 
 * Initial date: 20 juil. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesSecurityCallbackFactory {
	
	public static LecturesSecurityCallback getSecurityCallback(boolean adminRole, boolean masterCoachRole, boolean teacherRole, boolean readOnly) {
		LectureRoles viewAs;
		if(adminRole) {
			viewAs = LectureRoles.lecturemanager;
		} else if(masterCoachRole) {
			viewAs = LectureRoles.mastercoach;
		} else if(teacherRole) {
			viewAs = LectureRoles.teacher;
		} else {
			viewAs = LectureRoles.participant;
		}
		return new LecturesSecurityCallbackImpl(adminRole, masterCoachRole, teacherRole, viewAs, readOnly);
	}
	
	public static LecturesSecurityCallback getSecurityCallback(boolean adminRole, boolean masterCoachRole, boolean teacherRole, LectureRoles viewAs, boolean readOnly) {
		return new LecturesSecurityCallbackImpl(adminRole, masterCoachRole, teacherRole, viewAs, readOnly);
	}
	
	private static class LecturesSecurityCallbackImpl implements LecturesSecurityCallback {
		
		private final boolean adminRole;
		private final boolean masterCoachRole;
		private final boolean teacherRole;
		private final boolean readOnly;
		
		private final LectureRoles viewAs;
		private LectureModule lectureModule;
		
		public LecturesSecurityCallbackImpl(boolean adminRole, boolean masterCoachRole, boolean teacherRole, LectureRoles viewAs, boolean readOnly) {
			this.adminRole = adminRole;
			this.masterCoachRole = masterCoachRole;
			this.teacherRole = teacherRole;
			this.viewAs = viewAs;
			this.readOnly = readOnly;
			lectureModule = CoreSpringFactory.getImpl(LectureModule.class);
		}

		@Override
		public boolean canNewLectureBlock() {
			if(readOnly) return false;
			
			return adminRole;
		}

		@Override
		public boolean canReopenLectureBlock() {
			if(readOnly) return false;
			
			return adminRole;
		}

		@Override
		public boolean canChangeRates() {
			if(readOnly) return false;
			
			return adminRole;
		}

		@Override
		public boolean canSeeAppeals() {
			if(!lectureModule.isAbsenceAppealEnabled()) {
				return false;
			}
			
			if(viewAs == LectureRoles.teacher) {
				return teacherRole && lectureModule.isTeacherCanSeeAppeal();
			}
			if(viewAs == LectureRoles.mastercoach) {
				return masterCoachRole && lectureModule.isMasterCoachCanSeeAppeal();
			}
			if(viewAs == LectureRoles.lecturemanager) {
				return adminRole && lectureModule.isAbsenceAppealEnabled();
			}
			return false;
		}

		@Override
		public boolean canApproveAppeal() {
			if(readOnly) return false;
			
			if(adminRole) {
				return true;
			}
			
			if(viewAs == LectureRoles.teacher) {
				return teacherRole && lectureModule.isTeacherCanAuthorizedAppeal();
			}
			if(viewAs == LectureRoles.mastercoach) {
				return masterCoachRole && lectureModule.isMasterCoachCanAuthorizedAppeal();
			}
			return false;
		}

		@Override
		public boolean canEditConfiguration() {
			if(readOnly) return false;
			
			return adminRole;
		}

		@Override
		public boolean canAuthorizeAbsence() {
			if(readOnly) return false;
			
			if(adminRole) {
				return true;
			}
			return (masterCoachRole && lectureModule.isMasterCoachCanAuthorizedAbsence())
					|| (teacherRole && lectureModule.isTeacherCanAuthorizedAbsence());
		}
		
		@Override
		public boolean canAddAbsences() {
			if(!lectureModule.isAbsenceNoticeEnabled() || readOnly) {
				return false;
			}
			
			// same permissions as dispensations
			if(viewAs == LectureRoles.participant) {
				return false;
			} else if(teacherRole || adminRole) {
				return true;
			} else if(masterCoachRole) {
				return lectureModule.isMasterCoachCanRecordNotice();
			}
			return false;
		}
		
		@Override
		public boolean canAddDispensations() {
			return canAddAbsences();
		}

		@Override
		public boolean canAddNoticeOfAbsences() {
			if(!lectureModule.isAbsenceNoticeEnabled() || readOnly) {
				return false;
			}
			
			if(viewAs == LectureRoles.participant) {
				return lectureModule.isParticipantCanNotice();
			}
			
			if(teacherRole || adminRole) {
				return true;
			} else if(masterCoachRole) {
				return lectureModule.isMasterCoachCanRecordNotice();
			}
			return false;
		}

		@Override
		public boolean canEditAbsenceNotices() {
			if(readOnly) return false;
			
			return viewAs == LectureRoles.teacher || viewAs == LectureRoles.lecturemanager || viewAs == LectureRoles.mastercoach;
		}

		@Override
		public boolean canDeleteAbsenceNotices() {
			if(readOnly) return false;
			
			return viewAs == LectureRoles.lecturemanager || viewAs == LectureRoles.mastercoach;
		}

		@Override
		public LectureRoles viewAs() {
			return viewAs;
		}
	}
}
