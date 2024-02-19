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
package org.olat.modules.project.ui;

import java.util.Collection;
import java.util.Date;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjActivity.ActionTarget;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjProject;

/**
 * 
 * Initial date: 17 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjTimelineRow {
	
	private ProjProject project;
	private ProjArtefact artefact;
	private Long businessPathKey;
	private String message;
	private Date date;
	private String formattedDate;
	private boolean today;
	private String doerDisplyName;
	private Collection<Long> identityKeys;
	private String rangeEmpty;
	// Helper value for the filter
	private ActionTarget actionTarget;
	private String iconCssClass;
	private FormItem iconItem;
	private FormItem messageItem;
	private FormLink rangeLink;
	private FormLink showLaterLink;
	
	public ProjTimelineRow() {
		//
	}
	
	public ProjTimelineRow(ProjActivity activity) {
		project = activity.getProject();
		artefact = activity.getArtefact();
	}

	public ProjProject getProject() {
		return project;
	}

	public ProjArtefact getArtefact() {
		return artefact;
	}

	public Long getBusinessPathKey() {
		return businessPathKey;
	}

	public void setBusinessPathKey(Long businessPathKey) {
		this.businessPathKey = businessPathKey;
	}

	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getFormattedDate() {
		return formattedDate;
	}

	public void setFormattedDate(String formattedDate) {
		this.formattedDate = formattedDate;
	}

	public boolean isToday() {
		return today;
	}

	public void setToday(boolean today) {
		this.today = today;
	}

	public String getDoerDisplyName() {
		return doerDisplyName;
	}

	public void setDoerDisplyName(String doerDisplyName) {
		this.doerDisplyName = doerDisplyName;
	}

	public Collection<Long> getIdentityKeys() {
		return identityKeys;
	}

	public void setIdentityKeys(Collection<Long> identityKeys) {
		this.identityKeys = identityKeys;
	}

	public ActionTarget getActionTarget() {
		return actionTarget;
	}

	public void setActionTarget(ActionTarget actionTarget) {
		this.actionTarget = actionTarget;
	}

	public String getRangeEmpty() {
		return rangeEmpty;
	}

	public void setRangeEmpty(String rangeEmpty) {
		this.rangeEmpty = rangeEmpty;
	}

	public String getIconCssClass() {
		return iconCssClass;
	}

	public void setIconCssClass(String iconCssClass) {
		this.iconCssClass = iconCssClass;
	}

	public FormItem getIconItem() {
		return iconItem;
	}

	public String getIconItemName() {
		return iconItem != null? iconItem.getComponent().getComponentName(): null;
	}
	
	public void setIconItem(FormItem iconItem) {
		this.iconItem = iconItem;
	}

	public FormItem getMessageItem() {
		return messageItem;
	}

	public String getMessageItemName() {
		return messageItem != null? messageItem.getComponent().getComponentName(): null;
	}
	
	public void setMessageItem(FormItem messageItem) {
		this.messageItem = messageItem;
	}

	public FormLink getRangeLink() {
		return rangeLink;
	}
	
	public String getRangeLinkName() {
		return rangeLink != null? rangeLink.getComponent().getComponentName(): null;
	}

	public void setRangeLink(FormLink rangeLink) {
		this.rangeLink = rangeLink;
	}

	public FormLink getShowLaterLink() {
		return showLaterLink;
	}

	public void setShowLaterLink(FormLink showLaterLink) {
		this.showLaterLink = showLaterLink;
	}
	
	public String getShowLaterLinkName() {
		return showLaterLink != null? showLaterLink.getComponent().getComponentName(): null;
	}
	
}
