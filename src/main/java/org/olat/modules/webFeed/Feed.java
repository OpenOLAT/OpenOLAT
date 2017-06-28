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
package org.olat.modules.webFeed;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;

/**
 * This is an OpenOLAT feed (or web/news feed) model. It stores all necessary
 * information of a feed including items.
 * 
 * Initial date: 02.05.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface Feed extends OLATResourceable, CreateInfo, ModifiedInfo {

	public Long getKey();
	
	public void setKey(Long key);

	/**
	 * @see org.olat.core.id.OLATResourceable#getResourceableId()
	 */
	@Override
	public Long getResourceableId();
	
	public void setResourceableId(Long resourceableId);

	/**
	 * @see org.olat.core.id.OLATResourceable#getResourceableTypeName()
	 */
	@Override
	public String getResourceableTypeName();

	public void setCreationDate(Date creationDate);

	public String getTitle();

	public void setTitle(String title);

	public String getDescription();

	public void setDescription(String description);

	public String getAuthor();

	public void setAuthor(String initialAuthor);

	public String getImageName();

	public void setImageName(String name);

	public String getExternalImageURL();

	public void setExternalImageURL(String externalImageURL);

	public String getExternalFeedUrl();

	public void setExternalFeedUrl(String externalFeedUrl);

	/**
	 * Set whether the Feed is external (true), internal (false) or undefined (null).
	 * This method should not be called directly, instead use
	 * @see org.olat.modules.webFeed.FeedManager#method(java.lang.Boolean external, org.olat.modules.webFeed.Feed) .
	 * 
	 * @param isExternal
	 */
	public void setExternal(Boolean isExternal);
	
	public Boolean getExternal();
	
	public boolean isExternal();

	public boolean isInternal();

	/**
	 * @return true if it is still undefined whether it is an internal or an external feed.
	 */
	public boolean isUndefined();

	public int getModelVersion();
	
	public void setModelVersion(int version);

}