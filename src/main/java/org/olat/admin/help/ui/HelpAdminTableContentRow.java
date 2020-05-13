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
package org.olat.admin.help.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.updown.UpDown;

/* 
 * Initial date: 6 Apr 2020<br>
 * @author Alexander Boeckle, alexander.boeckle@frentix.com
 */
public class HelpAdminTableContentRow {

	private String helpPlugin;
	private String icon;
	private boolean usertool;
	private boolean authoring;
	private boolean login;
	private FormLink editLink;
	private FormLink deleteLink;
	private UpDown upDown;
	
	public HelpAdminTableContentRow() {
		// Nothing to do here
	}
	
	public HelpAdminTableContentRow(String helpPlugin, String icon, boolean usertool, boolean authoring, boolean login) {
		this.helpPlugin = helpPlugin;
		this.icon = icon;
		this.usertool = usertool;
		this.authoring = authoring;
		this.login = login;
	}

	// Getters and setters
	public void setUpDown(UpDown upDown) {
		this.upDown = upDown;
	}
	
	public UpDown getUpDown() {
		return upDown;
	}
	
	public String getIcon() {
		return icon;
	}

	public String getHelpPlugin() {
		return helpPlugin;
	}

	public boolean isUsertoolSet() {
		return usertool;
	}

	public boolean isAuthoringSet() {
		return authoring;
	}
	
	public boolean isLoginSet() {
		return login;
	}
	
	public FormLink getEditLink() {
		return editLink;
	}
	
	public FormLink getDeleteLink() {
		return deleteLink;
	}
	
	public void setEditLink(FormLink editLink) {
		this.editLink = editLink;
	}
	
	public void setDeleteLink(FormLink deleteLink) {
		this.deleteLink = deleteLink;
	}
}
