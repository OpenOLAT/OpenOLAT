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
package org.olat.modules.portfolio.model;

import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PageUserStatus;

/**
 * 
 * Initial date: 11 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedPage {
	
	private Long binderKey;
	private Date sectionEndDate;
	private Long pageKey;
	private String pageTitle;
	private PageStatus pageStatus;
	private Date lastModified;
	
	private final Identity owner;
	
	private boolean mark;
	private PageUserStatus userStatus;
	
	public AssessedPage(Long binderKey, Date sectionEndDate, Long pageKey,
			String pageTitle, PageStatus pageStatus, Date lastModified,
			Boolean mark, PageUserStatus userStatus, Identity owner) {
		this.binderKey = binderKey;
		this.sectionEndDate = sectionEndDate;
		this.pageKey = pageKey;
		this.pageTitle = pageTitle;
		this.pageStatus = pageStatus;
		this.lastModified = lastModified;
		this.owner = owner;
		this.userStatus = userStatus;
		this.mark = mark == null ? false : mark.booleanValue();
	}

	public Long getBinderKey() {
		return binderKey;
	}
	
	public Date getSectionEndDate() {
		return sectionEndDate;
	}

	public Long getPageKey() {
		return pageKey;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public PageStatus getPageStatus() {
		return pageStatus;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public boolean isMarked() {
		return mark;
	}

	public PageUserStatus getUserStatus() {
		return userStatus;
	}
	
	public Identity getOwner() {
		return owner;
	}
}
