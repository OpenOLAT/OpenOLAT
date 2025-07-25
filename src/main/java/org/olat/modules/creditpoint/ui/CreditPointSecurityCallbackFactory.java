/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.creditpoint.ui;

import org.olat.core.id.Roles;

/**
 * 
 * Initial date: 7 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointSecurityCallbackFactory {
	
	public static final CreditPointSecurityCallback userToolSecurityCallback() {
		return new CreditPointSecurityCallbackImpl(false);
	}
	
	public static final CreditPointSecurityCallback createSecurityCallback(Roles roles) {
		return new CreditPointSecurityCallbackImpl(roles.isAdministrator());
	}
	
	private static class CreditPointSecurityCallbackImpl implements CreditPointSecurityCallback {
		
		private final boolean admin;
		
		public CreditPointSecurityCallbackImpl(boolean admin) {
			this.admin = admin;
		}

		@Override
		public boolean canAddTransaction() {
			return admin;
		}

		@Override
		public boolean canRemoveTransaction() {
			return admin;
		}

		@Override
		public boolean canCancelTransaction() {
			return admin;
		}
	}
}
