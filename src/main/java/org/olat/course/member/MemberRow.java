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
package org.olat.course.member;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.group.ui.main.CourseMembership;
import org.olat.user.PortraitUser;
import org.olat.user.UserPortraitComponent;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 29 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class MemberRow extends UserPropertiesRow {
	
	private final Date creationDate;
	private final CourseMembership membership;
	
	private PortraitUser portraitUser;
	private UserPortraitComponent userPortraitComponent;
	
	public MemberRow(Identity identity, List<UserPropertyHandler> userPropertyHandlers, CourseMembership membership,
			Date creationDate, Locale locale) {
		super(identity, userPropertyHandlers, locale);
		this.membership = membership;
		this.creationDate = creationDate;
	}

	public MemberRow(Long identityKey, List<UserPropertyHandler> userPropertyHandlers, String[] identityProps, CourseMembership membership,
			Date creationDate, Locale locale) {
		super(identityKey, userPropertyHandlers, identityProps, locale);
		this.membership = membership;
		this.creationDate = creationDate;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public CourseMembership getMembership() {
		return membership;
	}

	public PortraitUser getPortraitUser() {
		return portraitUser;
	}

	public void setPortraitUser(PortraitUser portraitUser) {
		this.portraitUser = portraitUser;
	}

	public UserPortraitComponent getPortraitComponent() {
		return userPortraitComponent;
	}

	public void setPortraitComponent(UserPortraitComponent userPortraitComponent) {
		this.userPortraitComponent = userPortraitComponent;
	}
}
