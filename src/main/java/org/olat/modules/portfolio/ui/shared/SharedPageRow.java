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
package org.olat.modules.portfolio.ui.shared;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PageUserStatus;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 11 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharedPageRow extends UserPropertiesRow  {
	
	private Long pageKey;
	private String pageTitle;
	private Date lastChanges;
	private PageStatus status;

	private boolean mark;
	private PageUserStatus userStatus;
	
	private FormLink bookmarkLink;
	
	public SharedPageRow(Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(identity, userPropertyHandlers, locale);
	}

	public Long getPageKey() {
		return pageKey;
	}

	public void setPageKey(Long pageKey) {
		this.pageKey = pageKey;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	public PageStatus getStatus() {
		return status == null ? PageStatus.draft : status;
	}

	public void setStatus(PageStatus status) {
		this.status = status;
	}

	public boolean isMark() {
		return mark;
	}

	public void setMark(boolean mark) {
		this.mark = mark;
	}

	public PageUserStatus getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(PageUserStatus userStatus) {
		this.userStatus = userStatus;
	}

	public Date getLastChanges() {
		return lastChanges;
	}

	public void setLastChanges(Date lastChanges) {
		this.lastChanges = lastChanges;
	}

	public FormLink getBookmarkLink() {
		return bookmarkLink;
	}

	public void setBookmarkLink(FormLink bookmarkLink) {
		this.bookmarkLink = bookmarkLink;
	}
}
