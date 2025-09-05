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
package org.olat.resource.accesscontrol.ui;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;

/**
 * Initial date: 2025-09-05<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class PendingMembershipRow {

	private final String title;
	private final String extRef;
	private final Date begin;
	private final Date end;
	private final String type;
	private final Date confirmationUntil;

	private FormLink toolsLink;

	public PendingMembershipRow(String title, String extRef, Date begin, Date end, String type,
								Date confirmationUntil) {
		this.title = title;
		this.extRef = extRef;
		this.begin = begin;
		this.end = end;
		this.type = type;
		this.confirmationUntil = confirmationUntil;
	}

	public String getTitle() {
		return title;
	}

	public String getExtRef() {
		return extRef;
	}

	public Date getBegin() {
		return begin;
	}

	public Date getEnd() {
		return end;
	}

	public String getType() {
		return type;
	}

	public Date getConfirmationUntil() {
		return confirmationUntil;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
}
