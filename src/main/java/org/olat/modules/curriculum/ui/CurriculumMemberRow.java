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
package org.olat.modules.curriculum.ui;

import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 9 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumMemberRow extends UserPropertiesRow {
	
	private final String role;
	private final GroupMembershipInheritance inheritanceMode;
	
	public CurriculumMemberRow(CurriculumMember member, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(member.getIdentity(), userPropertyHandlers, locale);
		role = member.getRole();
		inheritanceMode = member.getInheritanceMode();
	}
	
	public String getRole() {
		return role;
	}
	
	public GroupMembershipInheritance getInheritanceMode() {
		return inheritanceMode;
	}


}
