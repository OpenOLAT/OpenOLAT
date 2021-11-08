/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.admin.user;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.modules.coach.ui.UserOverviewController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Container for userProperty list, configurable in olat_userconfig.xml.
 * 
 * <P>
 * Initial Date:  15.01.2008 <br>
 * @author Lavinia Dumitrescu
 */
public class UserShortDescription extends BasicController {
	
	private static final String usageIdentifyer = UserShortDescription.class.getCanonicalName();

	private VelocityContainer mainVC;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;

	public UserShortDescription(UserRequest ureq, WindowControl wControl, Identity identity) {
		this(ureq, wControl, identity, Rows.builder().build());
	}
	
	public UserShortDescription(UserRequest ureq, WindowControl wControl, Identity identity, Rows additionalRows) {
		super(ureq, wControl);
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);

		initProperties(usageIdentifyer, identity, isAdministrativeUser,true, additionalRows);
	}
	
	// Pass a custom usage identifier to define which user properties should be shown
	public UserShortDescription(UserRequest ureq, WindowControl wControl, Identity identity, String usageIdentifier) {
		super(ureq, wControl);
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);

		initProperties(usageIdentifier, identity, isAdministrativeUser,true, Rows.builder().build());
	}
	
	// Used in coaching tool -> user relations
	public UserShortDescription(UserRequest ureq, WindowControl wControl, Identity identity, boolean isAdministrativeUser) {
		super(ureq, wControl);

		initProperties(UserOverviewController.usageIdentifier, identity, isAdministrativeUser, true, null);
	}

	private void initProperties(String identifier, Identity identity, boolean isAdministrativeUser, boolean usernameOnTop, Rows additionalRows) {
		String usernameLabel = translate("table.user.login");
		//use the PropertyHandlerTranslator for the velocityContainer
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		mainVC = createVelocityContainer("userShortDescription");
		mainVC.setDomReplacementWrapperRequired(false); // we provide our own DOM replacement ID

		boolean alreadyDefinedUsername = false;
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(identifier, isAdministrativeUser);
		for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
			if(UserConstants.NICKNAME.equals(userPropertyHandler.getName())) {
				alreadyDefinedUsername = true;
			}
		}

		// Show all defined user properties
		mainVC.contextPut("userPropertyHandlers", userPropertyHandlers);
		mainVC.contextPut("user", identity.getUser());
		mainVC.contextPut("locale", getLocale());

		// Show identity key to administrative users
		if (isAdministrativeUser) {
			mainVC.contextPut("identityKey", identity.getKey());
		}

		// Show user name to administrative users if not already in user properties defined
		if(!alreadyDefinedUsername && (getIdentity().equals(identity) || isAdministrativeUser)) {
			mainVC.contextPut("username", identity.getUser().getProperty(UserConstants.NICKNAME, getLocale()));
			mainVC.contextPut("usernameLabel", usernameLabel);
			mainVC.contextPut("usernamePosition", "top");
		}

		if (usernameOnTop) {
			mainVC.contextPut("usernamePosition", "top");
		} else {
			mainVC.contextPut("usernamePosition", "bottom");
		}

		if (additionalRows != null) {
			mainVC.contextPut("additionalRows", additionalRows);
		}

		putInitialPanel(mainVC);
	}
	
	/**
	 * Set the position of the username / identity key if you
	 * have the permission to see them.
	 */
	public void setUsernameAtTop() {
		mainVC.contextPut("usernamePosition", "top");
	}
	
	/**
	 * Set the position of the username / identity key if you
	 * have the permission to see them.
	 */
	public void setUsernameAtBottom() {
		mainVC.contextPut("usernamePosition", "bottom");
	}
	
	public void setAdditionalRows(Rows additionalRows) {
		if (additionalRows != null) {
			mainVC.contextPut("additionalRows", additionalRows);
		} else {
			mainVC.contextRemove("additionalRows");
		}	
	}

	@Override
	protected void doDispose() {
		// nothing to dispose		
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// No event expected		
	}
	
	public static class Rows {
		
		private final List<Row> rows;

		private Rows(Builder builder) {
			this.rows = new ArrayList<>(builder.rows);
		}

		public List<Row> getRows() {
			return rows;
		}

		public static Builder builder() {
			return new Builder();
		}
	}
	
	public static final class Builder {
		
		private List<Row> rows = new ArrayList<>();

		private Builder() {
		}
		
		public Builder addRowBefore(String column1, String column2) {
			rows.add(new Row(column1, column2, true));
			return this;
		}

		public Builder addRow(String column1, String column2) {
			rows.add(new Row(column1, column2, false));
			return this;
		}

		public Rows build() {
			return new Rows(this);
		}
	}
	
	public static class Row {
		
		private final boolean before;
		private final String column1;
		private final String column2;
		
		private Row(String column1, String column2, boolean before) {
			this.column1 = column1;
			this.column2 = column2;
			this.before = before;
		}
		
		public boolean isBefore() {
			return before;
		}

		public String getColumn1() {
			return column1;
		}

		public String getColumn2() {
			return column2;
		}
		
	}
}
