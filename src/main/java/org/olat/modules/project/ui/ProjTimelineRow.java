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

/**
 * 
 * Initial date: 17 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjTimelineRow {
	
	private String message;
	private Date date;
	private String doerDisplyName;
	private Collection<Long> identityKeys;
	private String rangeEmpty;
	private FormItem iconItem;
	private FormItem messageItem;
	private FormLink rangeLink;
	private FormLink moreLink;
	
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

	public String getRangeEmpty() {
		return rangeEmpty;
	}

	public void setRangeEmpty(String rangeEmpty) {
		this.rangeEmpty = rangeEmpty;
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
	
	public String getMoreLinkName() {
		return moreLink != null? moreLink.getComponent().getComponentName(): null;
	}

	public FormLink getMoreLink() {
		return moreLink;
	}

	public void setMoreLink(FormLink moreLink) {
		this.moreLink = moreLink;
	}
	
}
