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

import org.olat.core.commons.controllers.navigation.Dated;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 02.05.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface Item extends Dated, CreateInfo, ModifiedInfo {
	
	public Long getKey();
	
	public void setKey(Long key);

	@Override
	public Date getDate();

	public void setCreationDate(Date date);

	public String getTitle();
	
	public void setTitle(String title);

	public String getDescription();
	
	public void setDescription(String description);
	
	public String getContent();
	
	public void setContent(String string);

	/**
	 * The author key corresponds to the identity key of the user that created
	 * this item. External items have no author key.
	 * @return
	 */
	public Long getAuthorKey();
	
	public void setAuthorKey(Long key);

	/**
	 * The modifier key corresponds to the identity key of the user that
	 * modified this item. External items have no modifier key.
	 * @return
	 */
	public Long getModifierKey();
	
	public void setModifierKey(Long key);

	/**
	 * This is the display name of the author.
	 * @return
	 */
	public String getAuthor();
	
	public void setAuthor(String author);

	/**
	 * This is the display name of the modifier.
	 * External items have no modifier name.
	 * @return
	 */
	public String getModifier();
	
	public String getGuid();
	
	public void setGuid(String uri);

	/**
	 * This is the link of an external item.
	 * Internal items return null.
	 * @return
	 */
	public String getExternalLink();
	
	public void setExternalLink(String link);

	public void setDraft(boolean b);

	/**
	 * An item can either be in draft version or it is published
	 * -> 'not draft' is equivalent to 'published'
	 * @return
	 */
	public boolean isDraft();

	public boolean isScheduled();

	public boolean isPublished();

	public Date getPublishDate();
	
	public void setPublishDate(Date date);

	public Enclosure getEnclosure();
	
	public void setEnclosure(Enclosure media);

	/**
	 * Width for video podcast.
	 * @return
	 */
	public Integer getWidth();
	
	public void setWidth(Integer width);

	/**
	 * Height for video podcast.
	 * @return
	 */
	public Integer getHeight();
	
	public void setHeight(Integer height);

	public FileElement getMediaFile();
	
	public void setMediaFile(FileElement file);

	public boolean isAuthorFallbackSet();

	public Feed getFeed();

	/**
	 * @return An extra CSS class for drafts and scheduled items
	 */
	public String extraCSSClass();
	
}
