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
package org.olat.course.nodes.form;

import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Initial date: 17 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FormSecurityCallbackFactory {
	
	private static final FormSecurityCallback READ_ONLY = new ReadOnlyCFormSecurityallback();
	
	public static FormSecurityCallback createSecurityCallback(UserCourseEnvironment userCourseEnv) {
		
		if (userCourseEnv.isCourseReadOnly()) {
			return READ_ONLY;
		}
		
		return new FormSecurityCallbackImpl(userCourseEnv.isAdmin());
	}
	
	private static final class FormSecurityCallbackImpl implements FormSecurityCallback {
		
		private final boolean admin;

		public FormSecurityCallbackImpl(boolean admin) {
			super();
			this.admin = admin;
		}

		@Override
		public boolean canResetAll() {
			return admin;
		}

		@Override
		public boolean canReset() {
			return true;
		}

		@Override
		public boolean canReopen() {
			return true;
		}
		
	}
	
	private static final class ReadOnlyCFormSecurityallback implements FormSecurityCallback {

		@Override
		public boolean canResetAll() {
			return false;
		}

		@Override
		public boolean canReset() {
			return false;
		}

		@Override
		public boolean canReopen() {
			return false;
		}
	}

}
