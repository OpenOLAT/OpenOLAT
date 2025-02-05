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
package org.olat.modules.curriculum;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.id.Roles;

/**
 * 
 * Initial date: 16 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumSecurityCallbackFactory {
	
	private CurriculumSecurityCallbackFactory() {
		//
	}
	
	/**
	 * @return A security callback without any administration permissions.
	 */
	public static final CurriculumSecurityCallback createDefaultCallback() {
		return new DefaultCurriculumSecurityCallback(false, List.of(), List.of());
	}
	
	/**
	 * @return A security callback without any administration permissions
	 * 		but view calendars and lectures.
	 */
	public static final CurriculumSecurityCallback userLookCallback() {
		return new UserLookCurriculumSecurityCallback();
	}
	
	public static final CurriculumSecurityCallback createCallback(Roles roles, List<Curriculum> ownedCurriculums) {
		boolean admin = roles.isCurriculumManager() || roles.isAdministrator();
		return new DefaultCurriculumSecurityCallback(admin, ownedCurriculums, List.of());
	}
	
	private static class UserLookCurriculumSecurityCallback extends DefaultCurriculumSecurityCallback {

		public UserLookCurriculumSecurityCallback() {
			super(false, List.of(), List.of());
		}

		@Override
		public boolean canViewAllCalendars() {
			return true;
		}

		@Override
		public boolean canViewAllLectures() {
			return true;
		}

		@Override
		public boolean canViewAllLearningProgress() {
			return true;
		}
	}
	
	private static class DefaultCurriculumSecurityCallback implements CurriculumSecurityCallback {
		
		private final boolean admin;
		private final Set<Long> ownedCurriculumKeys;
		private final Set<Long> ownedElementKeys;
		
		public DefaultCurriculumSecurityCallback(boolean admin, List<Curriculum> ownedCurriculums, List<CurriculumElementRef> ownedElementRefs) {
			this.admin = admin;
			ownedCurriculumKeys = ownedCurriculums.stream()
					.map(Curriculum::getKey)
					.collect(Collectors.toSet());
			ownedElementKeys = ownedElementRefs.stream()
					.map(CurriculumElementRef::getKey)
					.collect(Collectors.toSet());
		}

		@Override
		public boolean canNewCurriculum() {
			return admin;
		}

		@Override
		public boolean canEditCurriculum() {
			return admin;
		}

		@Override
		public boolean canManagerCurriculumUsers() {
			return admin;
		}

		@Override
		public boolean canNewCurriculumElement() {
			return admin;
		}

		@Override
		public boolean canEditCurriculumElements() {
			return admin || !ownedElementKeys.isEmpty();
		}
		
		@Override
		public boolean canEditCurriculumElement(CurriculumElement element) {
			if(element == null) return false;
			if(admin) return true;
			
			for(CurriculumElement el=element ; el != null; el=el.getParent()) {
				if(ownedElementKeys.contains(el.getKey())) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean canEditCurriculumTree() {
			return admin;
		}

		@Override
		public boolean canManagerCurriculumElementsUsers() {
			return admin;
		}

		@Override
		public boolean canManagerCurriculumElementUsers(CurriculumElement element) {
			return element != null &&  (admin || ownedElementKeys.contains(element.getKey()));
		}

		@Override
		public boolean canManagerCurriculumElementResources(CurriculumElement element) {
			return element != null &&  (admin || ownedElementKeys.contains(element.getKey()));
		}

		@Override
		public boolean canViewAllCalendars() {
			return admin;
		}
		
		@Override
		public boolean canNewLectureBlock() {
			return admin;
		}

		@Override
		public boolean canViewAllLectures() {
			return admin;
		}

		@Override
		public boolean canViewAllLearningProgress() {
			return admin;
		}

		@Override
		public boolean canCurriculumsReports() {
			return admin;
		}

		@Override
		public boolean canCurriculumReports(Curriculum curriculum) {
			return ownedCurriculumKeys.contains(curriculum.getKey());
		}
	}
}
