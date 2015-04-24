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
package org.olat.modules.webFeed.models;

import java.io.Serializable;
import java.util.Date;

import org.olat.core.commons.controllers.navigation.Dated;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.util.StringHelper;
import org.olat.user.UserManager;

/**
 * This is the feed item class. A feed has many items. Implements Serializable
 * for caching, Dated for ordering.
 * 
 * <P>
 * Initial Date: Feb 16, 2009 <br>
 * 
 * @authorKey Gregor Wassmann
 */
public class Item implements Serializable, Dated {

	private static final long serialVersionUID = 8856863396847065934L;
	private String title;
	private String description, content;
	// The authorKey corresponds to the olat identity key of the user that created
	// this item.
	private long authorKey;
	private long modifierKey;
	// author is used for external sources.
	private String author;
	private String modifier;
	private String guid; // The global unique identifier
	private String externalLink;
	private Date lastModified;
	private Date publishDate;
	private Enclosure enclosure;
	private int width, height;	//fxdiff FXOLAT-118: size for video podcast
	private transient FileElement mediaFile;
	// An item can either be in draft version or it is published
	// -> 'not draft' is equivalent to 'published'
	private boolean draft = false;
	
	public Item(){
		//
	}

	/**
	 * @param title The title to set.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param content The content to set.
	 */
	public void setContent(String content) {
		this.content = content;
	}
	
	/**
	 * @return Returns the content.
	 */
	public String getContent() {
		return content;
	}
	public boolean isAuthorFallbackSet() {
		return StringHelper.containsNonWhitespace(author);
	}

	/**
	 * @param authorKey The authorKey to set. Used for OLAT-made feeds.
	 */
	public void setAuthorKey(long identityKey) {
		this.authorKey = identityKey;
	}

	/**
	 * @return Returns the authorKey
	 */
	public long getAuthorKey() {
		return authorKey;
	}

	/**
	 * @param author The author to set. Used for external feeds.
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return The name of the author
	 */
	public String getAuthor() {
		String authorName = null;
		if(authorKey > 0) {
			authorName = UserManager.getInstance().getUserDisplayName(authorKey);
		}
		if (authorName == null && StringHelper.containsNonWhitespace(author)) {
			authorName = author;
		}
		return authorName;
	}
	
	/**
	 * @return the key of the modifier
	 */
	public long getModifierKey() {
		return modifierKey;
	}
	
	public void setModifierKey(long modifierKey) {
		this.modifierKey = modifierKey;
	}
	
	/**
	 * @return The name of the modifier
	 */
	public String getModifier() {
		String modifierName = null;
		if(modifierKey > 0) {
			modifierName = UserManager.getInstance().getUserDisplayName(modifierKey);
		}
		if (modifierName == null && StringHelper.containsNonWhitespace(modifier)) {
			modifierName = modifier;
		}
		return modifierName;
	}
	
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	/**
	 * @param guid The guid to set.
	 */
	public void setGuid(String guid) {
		this.guid = guid;
	}

	/**
	 * @return Returns the guid.
	 */
	public String getGuid() {
		return guid;
	}

	/**
	 * @param enclosure The enclosure to set.
	 */
	public void setEnclosure(Enclosure enclosure) {
		this.enclosure = enclosure;
	}

	/**
	 * @return Returns the enclosure.
	 */
	public Enclosure getEnclosure() {
		return enclosure;
	}

	/**
	 * @param lastModified The lastModified to set.
	 */
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * @return Returns the lastModified.
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * @param publishDate The publishDate to set.
	 */
	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}

	/**
	 * @return Returns the publishDate.
	 */
	public Date getPublishDate() {
		return publishDate;
	}

	/**
	 * @param externalLink The externalLink to set.
	 */
	public void setExternalLink(String externalLink) {
		this.externalLink = externalLink;
	}

	/**
	 * @return Returns the externalLink.
	 */
	public String getExternalLink() {
		return externalLink;
	}

	/**
	 * @param mediaFile The mediaFile to set.
	 */
	public void setMediaFile(FileElement mediaFile) {
		this.mediaFile = mediaFile;
	}

	/**
	 * @return Returns the mediaFile.
	 */
	public FileElement getMediaFile() {
		return mediaFile;
	}
	//fxdiff FXOLAT-118: size for video podcast
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @param draft The draft to set.
	 */
	public void setDraft(boolean draft) {
		this.draft = draft;
	}

	/**
	 * @return True if this is a draft
	 */
	public boolean isDraft() {
		return draft;
	}

	/**
	 * @return True if this is published
	 */
	public boolean isPublished() {
		Date now = new Date();
		return !draft && publishDate != null && now.after(publishDate);
	}

	/**
	 * @return True if this is publication is scheduled
	 */
	public boolean isScheduled() {
		Date now = new Date();
		return !draft && publishDate != null && now.before(publishDate);
	}

	/**
	 * @return An extra CSS class for drafts and scheduled items
	 */
	public String extraCSSClass() {
		String css = null;
		if (isDraft()) {
			css = "o_draft";
		} else if (isScheduled()) {
			css = "o_scheduled";
		}
		return css;
	}

	/**
	 * This date can be null.
	 * @see org.olat.core.commons.controllers.navigation.Dated#getDate()
	 */
	public Date getDate() {
		return publishDate;
	}
	
	public int hashCode() {
		return guid == null ? 39745 : guid.hashCode();
	}
	
	/**
	 * Overwrite equals method so that different object in the vm that actually
	 * represent the same item are recognized as such. Eg in the remove method of the feed
	 */
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		} else if(obj instanceof Item) {
			Item item = (Item)obj;
			return guid != null && guid.equals(item.guid);
		}
		return false;
		
	}
}
