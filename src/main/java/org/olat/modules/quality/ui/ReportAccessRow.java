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
package org.olat.modules.quality.ui;

import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.modules.quality.QualityReportAccess.Type;

/**
 * 
 * Initial date: 29.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ReportAccessRow {

	private final String name;
	private final Type type;
	private final String role;
	private MultipleSelectionElement onlineEl;
	private SingleSelection emailTriggerEl;

	public ReportAccessRow(String name, Type type, String role) {
		this.name = name;
		this.type = type;
		this.role = role;
	}

	public void setOnlineEl(MultipleSelectionElement onlineEl) {
		this.onlineEl = onlineEl;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	public String getRole() {
		return role;
	}

	public MultipleSelectionElement getOnlineEl() {
		return onlineEl;
	}

	public SingleSelection getEmailTriggerEl() {
		return emailTriggerEl;
	}

	public void setEmailTriggerEl(SingleSelection emailTriggerEl) {
		this.emailTriggerEl = emailTriggerEl;
	}

}
