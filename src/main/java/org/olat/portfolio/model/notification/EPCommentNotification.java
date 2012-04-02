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
package org.olat.portfolio.model.notification;

import java.util.Date;

import org.olat.basesecurity.IdentityShort;
import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.util.StringHelper;

/**
 * Map the notification for comment element
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EPCommentNotification extends PersistentObject implements EPNotification {

	private static final long serialVersionUID = -1065069940086963966L;
	
	private Date lastModified;
	private String type;
	
	private Long mapKey;
	private String mapTitle;
	private Long pageKey;
	private String pageTitle;
	
	private IdentityShort author;

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date date) {
		this.lastModified = date;
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

	public Long getMapKey() {
		return mapKey;
	}

	public void setMapKey(Long mapKey) {
		this.mapKey = mapKey;
	}

	public String getMapTitle() {
		return mapTitle;
	}

	public void setMapTitle(String mapTitle) {
		this.mapTitle = mapTitle;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getTitle() {
		return StringHelper.containsNonWhitespace(pageTitle) ? pageTitle : mapTitle;
	}

	@Override
	public IdentityShort getAuthor() {
		return author;
	}

	public void setAuthor(IdentityShort author) {
		this.author = author;
	}
	
	@Override
	public String toString() {
		return "CommentNotification[" + super.toString() + ";pageKey=" + pageKey + "]";
	}
	
	@Override
	public int hashCode() {
		return getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof EPCommentNotification) {
			EPCommentNotification notification = (EPCommentNotification)obj;
			return getKey().equals(notification.getKey());
		}
		return false;
	}
}