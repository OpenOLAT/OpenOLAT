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
		return new DefaultCurriculumSecurityCallback(false);
	}
	
	public static final CurriculumSecurityCallback createCallback(Roles roles) {
		boolean admin = roles.isCurriculumManager() || roles.isAdministrator();
		return new DefaultCurriculumSecurityCallback(admin);
	}
	
	public static final CurriculumSecurityCallback createCallback(boolean canManage) {
		return new DefaultCurriculumSecurityCallback(canManage);
	}
	
	private static class DefaultCurriculumSecurityCallback implements CurriculumSecurityCallback {
		
		private final boolean admin;
		
		public DefaultCurriculumSecurityCallback(boolean admin) {
			this.admin = admin;
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
		public boolean canEditCurriculumElement() {
			return admin;
		}

		@Override
		public boolean canManagerCurriculumElementUsers() {
			return admin;
		}

		@Override
		public boolean canManagerCurriculumElementResources() {
			return admin;
		}

		@Override
		public boolean canViewAllCalendars() {
			return admin;
		}

		@Override
		public boolean canViewAllLectures() {
			return admin;
		}
	}
}
