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
package org.olat.group.ui.main;

/**
 * 
 * Initial date: 20 juil. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberListSecurityCallbackFactory {
	
	public static MemberListSecurityCallback adminRights() {
		return new MemberListSecurityCallbackImpl(false, true);
	}
	
	public static MemberListSecurityCallback getSecurityCallback(boolean readOnly, boolean canRemoveMembers) {
		return new MemberListSecurityCallbackImpl(readOnly, canRemoveMembers);
	}
	
	private static class MemberListSecurityCallbackImpl implements MemberListSecurityCallback {
		
		private final boolean readOnly;
		private final boolean canRemoveMembers;
		
		public MemberListSecurityCallbackImpl(boolean readOnly, boolean canRemoveMembers) {
			this.readOnly = readOnly;
			this.canRemoveMembers = canRemoveMembers;
		}

		@Override
		public boolean isReadonly() {
			return readOnly;
		}

		@Override
		public boolean canRemoveMembers() {
			return canRemoveMembers;
		}
	}
}
