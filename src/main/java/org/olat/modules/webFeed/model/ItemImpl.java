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
package org.olat.modules.webFeed.model;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.Target;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.util.StringHelper;
import org.olat.modules.webFeed.Enclosure;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.Item;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 02.05.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="item")
@Table(name="o_feed_item")
@NamedQuery(name="loadItemByGuid",
		query="select data from item data where data.feed.key=:feedKey and data.guid=:guid")
@NamedQuery(name="loadItemByGuidWithoutFeed",
		query="select data from item data where data.guid=:guid")
@NamedQuery(name="loadItemsByFeed",
	query="select data from item data where data.feed=:feed")
@NamedQuery(name="loadItemsByAuthorWithFeed",
	query="select data from item data inner join fetch data.feed as feed where data.authorKey=:authorKey")
@NamedQuery(name="loadItemsGuidByFeed",
	query="select guid from item data where data.feed=:feed")
@NamedQuery(name="removeItem",
	query="delete from item data where data.key=:key")
@NamedQuery(name="removeItemsForFeed",
	query="delete from item data where data.feed.key=:feedKey")
public class ItemImpl implements Item, Serializable {

	private static final long serialVersionUID = 4504634251127072211L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=false, updatable=false)
	private Long key;
	
	@ManyToOne(targetEntity=FeedImpl.class, fetch=FetchType.EAGER)
	@JoinColumn(name="fk_feed_id", nullable=false, insertable=true, updatable=false)
	private Feed feed;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="f_title", nullable=true, insertable=true, updatable=true)
	private String title;
	@Column(name="f_description", nullable=true, insertable=true, updatable=true)
	private String description;
	@Column(name="f_content", nullable=true, insertable=true, updatable=true)
	private String content;
	
	@Column(name="fk_identity_author_id", nullable=true, insertable=true, updatable=false)
	private Long authorKey;
	@Column(name="fk_identity_modified_id", nullable=true, insertable=true, updatable=true)
	private Long modifierKey;
	@Column(name="f_author", nullable=true, insertable=true, updatable=true)
	private String author;
	
	@Column(name="f_guid", nullable=true, insertable=true, updatable=true)
	private String guid;
	@Column(name="f_external_link", nullable=true, insertable=true, updatable=true)
	private String externalLink;
	
	@Column(name="f_draft", nullable=true, insertable=true, updatable=true)
	private boolean draft = false;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="f_publish_date", nullable=true, insertable=true, updatable=true)
	private Date publishDate;
	
	private transient FileElement mediaFile;
	@Embedded
	@Target(EnclosureImpl.class)
    @AttributeOverrides( {
    	@AttributeOverride(name="fileName", column = @Column(name="f_filename") ),
    	@AttributeOverride(name="type", column = @Column(name="f_type") ),
    	@AttributeOverride(name="length", column = @Column(name="f_length") ),
    	@AttributeOverride(name="externalUrl", column = @Column(name="f_external_url") )
    })
	private Enclosure enclosure;
	@Column(name="f_width", nullable=true, insertable=true, updatable=true)
	private Integer width;
	@Column(name="f_height", nullable=true, insertable=true, updatable=true)
	private Integer	height;

	public ItemImpl(Feed feed) {
		this.feed = feed;
	}
	
	@SuppressWarnings("unused")
	private ItemImpl() {
		// make Hibernate happy
	}

	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public void setContent(String content) {
		this.content = content;
	}
	
	@Override
	public boolean isAuthorFallbackSet() {
		return StringHelper.containsNonWhitespace(author);
	}

	@Override
	public Long getAuthorKey() {
		return authorKey;
	}

	@Override
	public void setAuthorKey(Long identityKey) {
		this.authorKey = identityKey;
	}

	@Override
	public String getAuthor() {
		String authorName = null;
		if(authorKey != null) {
			authorName = UserManager.getInstance().getUserDisplayName(authorKey);
		}
		if (authorName == null && StringHelper.containsNonWhitespace(author)) {
			authorName = author;
		}
		return authorName;
	}

	@Override
	public void setAuthor(String author) {
		this.author = author;
	}

	@Override
	public Long getModifierKey() {
		return modifierKey;
	}
	
	@Override
	public void setModifierKey(Long modifierKey) {
		this.modifierKey = modifierKey;
	}
	
	@Override
	public String getModifier() {
		String modifierName = null;
		if(modifierKey != null) {
			modifierName = UserManager.getInstance().getUserDisplayName(modifierKey);
		}
		return modifierName;
	}

	@Override
	public String getGuid() {
		return guid;
	}

	@Override
	public void setGuid(String guid) {
		this.guid = guid;
	}

	@Override
	public Date getPublishDate() {
		return publishDate;
	}

	@Override
	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}

	@Override
	public void setDraft(boolean draft) {
		this.draft = draft;
	}

	@Override
	public boolean isDraft() {
		return draft;
	}

	@Override
	public boolean isScheduled() {
		Date now = new Date();
		return !draft && publishDate != null && now.before(publishDate);
	}

	@Override
	public boolean isPublished() {
		Date now = new Date();
		return !draft && publishDate != null && now.after(publishDate);
	}

	@Override
	public void setExternalLink(String externalLink) {
		this.externalLink = externalLink;
	}

	@Override
	public String getExternalLink() {
		return externalLink;
	}

	@Override
	public Enclosure getEnclosure() {
		return enclosure;
	}

	@Override
	public void setEnclosure(Enclosure enclosure) {
		this.enclosure = enclosure;
	}

	@Override
	public void setMediaFile(FileElement mediaFile) {
		this.mediaFile = mediaFile;
	}

	@Override
	public FileElement getMediaFile() {
		return mediaFile;
	}

	@Override
	public Integer getWidth() {
		return width;
	}

	@Override
	public void setWidth(Integer width) {
		this.width = width;
	}

	@Override
	public Integer getHeight() {
		return height;
	}

	@Override
	public void setHeight(Integer height) {
		this.height = height;
	}

	@Override
	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}

	@Override
	public String extraCSSClass() {
		String css = null;
		if (isDraft()) {
			css = "o_draft";
		} else if (isScheduled()) {
			css = "o_scheduled";
		}
		return css;
	}

	@Override
	public Date getDate() {
		return publishDate;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 39745 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		} else if(obj instanceof ItemImpl) {
			ItemImpl item = (ItemImpl)obj;
			return getKey() != null && getKey().equals(item.getKey());
		}
		return false;
	}

}
