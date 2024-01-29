/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.todo.manager;

import org.olat.modules.todo.ToDoMailRule;

/**
 * 
 * Initial date: 29 Jan 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ToDoMailRules {
	
	public static final class ToDoMailDefaultRule implements ToDoMailRule {
		
		@Override
		public boolean isSendAssignmentEmail(boolean byMyself, boolean isAssignedOrDelegated, boolean wasAssignedOrDelegated) {
			// Send the email if the identity has now one of the two roles and has had none of the two roles.
			// Switching from one role to the other does not trigger an email.
			// And: Do not send an email to yourself
			return isAssignedOrDelegated && !wasAssignedOrDelegated && !byMyself;
		}

		@Override
		public boolean isSendDoneEmail() {
			return true;
		}

	}
	
	public static final class ToDoMailNoRule implements ToDoMailRule {
		
		@Override
		public boolean isSendAssignmentEmail(boolean byMyself, boolean isAssignedOrDelegated, boolean wasAssignedOrDelegated) {
			return false;
		}

		@Override
		public boolean isSendDoneEmail() {
			return false;
		}

	}


}
