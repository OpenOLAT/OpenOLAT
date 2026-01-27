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
		return new DefaultCurriculumSecurityCallback(false, false, List.of(), List.of());
	}
	
	/**
	 * @return A security callback without any administration permissions
	 * 		but view calendars and lectures.
	 */
	public static final CurriculumSecurityCallback userLookCallback() {
		return new UserLookCurriculumSecurityCallback();
	}
	
	public static final CurriculumSecurityCallback createCallback(Roles roles, List<Curriculum> ownedCurriculums,
			List<CurriculumElementRef> ownedElements) {
		boolean admin = roles.isCurriculumManager() || roles.isAdministrator();
		boolean principal = roles.isPrincipal();
		return new DefaultCurriculumSecurityCallback(admin, principal, ownedCurriculums, ownedElements);
	}
	
	private static class UserLookCurriculumSecurityCallback extends DefaultCurriculumSecurityCallback {

		public UserLookCurriculumSecurityCallback() {
			super(false, false, List.of(), List.of());
		}

		@Override
		public boolean canViewAllCalendars() {
			return true;
		}

		@Override
		public boolean canViewAllLectures(Curriculum curriculum) {
			return true;
		}

		@Override
		public boolean canViewAllLearningProgress() {
			return true;
		}
	}
	
	private static class DefaultCurriculumSecurityCallback implements CurriculumSecurityCallback {
		
		private final boolean admin;
		private final boolean principal;
		private final Set<Long> ownedCurriculumKeys;
		private final Set<Long> ownedElementsKeys;
		
		public DefaultCurriculumSecurityCallback(boolean admin, boolean principal, List<Curriculum> ownedCurriculums, List<CurriculumElementRef> ownedElements) {
			this.admin = admin;
			this.principal = principal;
			ownedCurriculumKeys = ownedCurriculums.stream()
					.map(Curriculum::getKey)
					.collect(Collectors.toSet());
			ownedElementsKeys = ownedElements.stream()
					.map(CurriculumElementRef::getKey)
					.collect(Collectors.toSet());
		}

		@Override
		public boolean canViewCurriculums() {
			return admin || principal || !ownedCurriculumKeys.isEmpty() || !ownedElementsKeys.isEmpty();
		}

		@Override
		public boolean canViewImplementations() {
			return admin || principal || !ownedCurriculumKeys.isEmpty() || !ownedElementsKeys.isEmpty();
		}

		@Override
		public boolean canNewCurriculum() {
			return admin;
		}

		@Override
		public boolean canEditCurriculum(Curriculum curriculum) {
			return admin
					|| (curriculum != null && ownedCurriculumKeys.contains(curriculum.getKey()));
		}
		
		@Override
		public boolean canDeleteCurriculum() {
			return admin;
		}

		@Override
		public boolean canManagerCurriculumUsers() {
			return admin || !ownedCurriculumKeys.isEmpty();
		}

		@Override
		public boolean canNewCurriculumElement(Curriculum curriculum) {
			return admin
					|| (curriculum != null && ownedCurriculumKeys.contains(curriculum.getKey()));
		}
		
		@Override
		public boolean canDeleteCurriculumElement(CurriculumElement curriculumElement) {
			return admin
					|| (curriculumElement != null && ownedCurriculumKeys.contains(curriculumElement.getCurriculum().getKey()));
		}

		@Override
		public boolean canEditCurriculumElements(Curriculum curriculum) {
			return admin
					|| (curriculum != null && ownedCurriculumKeys.contains(curriculum.getKey()))
					|| !ownedElementsKeys.isEmpty();
		}
		
		@Override
		public boolean canEditCurriculumElement(CurriculumElement element) {
			if(element == null || element.getCurriculum() == null) {
				return false;
			}
			return admin
					|| ownedCurriculumKeys.contains(element.getCurriculum().getKey())
					|| ownedElementsKeys.contains(element.getKey());
		}
		
		@Override
		public boolean canViewCurriculumElement(CurriculumElement element) {
			return principal || canEditCurriculumElement(element);
		}
		
		@Override
		public boolean canMoveCurriculumElement(CurriculumElement element) {
			if(element == null || element.getCurriculum() == null) {
				return false;
			}
			if(admin || ownedCurriculumKeys.contains(element.getCurriculum().getKey())) {
				return true;
			}
			return false;
		}
		
		@Override
		public boolean canEditCurriculumElementSettings(CurriculumElement element) {
			if(element == null || element.getCurriculum() == null) {
				return false;
			}
			if(admin || ownedCurriculumKeys.contains(element.getCurriculum().getKey())) {
				return true;
			}
			return false;
		}

		@Override
		public boolean canEditCurriculumTree() {
			return admin;
		}

		@Override
		public boolean canManageCurriculumElementsUsers(Curriculum curriculum) {
			return admin
					|| (curriculum != null && ownedCurriculumKeys.contains(curriculum.getKey()));
		}

		@Override
		public boolean canManageCurriculumElementUsers(CurriculumElement element) {
			return element != null && element.getCurriculum() != null && (
					admin
					|| ownedCurriculumKeys.contains(element.getCurriculum().getKey()));
		}
		
		@Override
		public boolean canViewCurriculumElementResources(CurriculumElement element) {
			return principal || canManageCurriculumElementResources(element);
		}

		@Override
		public boolean canManageCurriculumElementResources(CurriculumElement element) {
			return element != null && element.getCurriculum() != null && (
					admin
					|| ownedCurriculumKeys.contains(element.getCurriculum().getKey())
					|| ownedElementsKeys.contains(element.getKey()));
		}

		@Override
		public boolean canViewCatalogSettings(CurriculumElement element) {
			return element != null && element.getCurriculum() != null
					&& (admin || principal || ownedCurriculumKeys.contains(element.getCurriculum().getKey()));
		}

		@Override
		public boolean canViewAllCalendars() {
			return admin || principal;
		}
		
		@Override
		public boolean canNewLectureBlock() {
			return admin;
		}

		@Override
		public boolean canViewAllLectures(Curriculum curriculum) {
			return admin || principal
					|| (curriculum != null && ownedCurriculumKeys.contains(curriculum.getKey()));
		}

		@Override
		public boolean canViewAllLearningProgress() {
			return admin || principal;
		}

		@Override
		public boolean canCurriculumsReports() {
			return admin || principal || !ownedCurriculumKeys.isEmpty();
		}

		@Override
		public boolean canCurriculumReports(Curriculum curriculum) {
			return admin || principal
					|| (curriculum != null && ownedCurriculumKeys.contains(curriculum.getKey()));
		}
	}
}
