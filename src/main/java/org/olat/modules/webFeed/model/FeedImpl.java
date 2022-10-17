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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.olat.core.id.OLATResourceable;
import org.olat.modules.webFeed.Feed;

/**
 *
 * Initial date: 02.05.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="feed")
@Table(name="o_feed")
@NamedQuery(name="loadFeedByRessourceable",
		query="select data from feed as data where data.resourceableId=:key and data.resourceableType=:name")
public class FeedImpl implements Feed, Serializable {

	private static final long serialVersionUID = 6005283969959964489L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=false, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="f_resourceable_id", nullable=true, insertable=true, updatable=true)
	private Long resourceableId;
	@Column(name="f_resourceable_type", nullable=true, insertable=true, updatable=true)
	private String resourceableType;
	
	@Column(name="f_title", nullable=true, insertable=true, updatable=true)
	private String title;
	@Column(name="f_description", nullable=true, insertable=true, updatable=true)
	private String description;
	@Column(name="f_author", nullable=true, insertable=true, updatable=true)
	private String author;
	@Column(name="f_image_name", nullable=true, insertable=true, updatable=true)
	private String imageName;
	@Column(name="f_external_feed_url", nullable=true, insertable=true, updatable=true)
	private String externalFeedURL;
	@Column(name="f_external_image_url", nullable=true, insertable=true, updatable=true)
	private String externalImageURL;
	
	/**
	 * A feed can either be internal, external or unspecified.
	 * Internal means that items are created within OLAT.
	 * External implies that the feed is read from an external URL.
	 * In some situations the state can't be determined:
	 * - it has just been created
	 * - all items have been removed
	 * - the feed url of an external feed has been set empty
	 */
	@Column(name="f_external", nullable=true, insertable=true, updatable=true)
	private Boolean isExternal;

	// Data model versioning
	public static final int CURRENT_MODEL_VERSION = 3;
	// Default is that model version is set to the initial value 1. This is 
	// necessary to detect models previous to the introduction of this model
	// version flag which was with version 2.
	// Save it in the XML file but not in the database.
	@Transient
	private int modelVersion = 0; 

	public FeedImpl(OLATResourceable ores) {
		this.resourceableId = ores.getResourceableId();
		this.resourceableType = ores.getResourceableTypeName();

		// new model constructor, set to current version
		this.modelVersion = CURRENT_MODEL_VERSION;
	}
	
	private FeedImpl() {
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
	public Long getResourceableId() {
		return resourceableId;
	}

	@Override
	public void setResourceableId(Long resourceableId) {
		this.resourceableId = resourceableId;
	}

	@Override
	public String getResourceableTypeName() {
		return resourceableType;
	}

	public void setResourceableType(String resourceableType) {
		this.resourceableType = resourceableType;
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
	public String getAuthor() {
		return author;
	}

	@Override
	public void setAuthor(String author) {
		this.author = author;
	}

	@Override
	public String getImageName() {
		return imageName;
	}

	@Override
	public void setImageName(String name) {
		this.imageName = name;
	}

	@Override
	public String getExternalImageURL() {
		return externalImageURL;
	}

	@Override
	public void setExternalImageURL(String externalImageURL) {
		this.externalImageURL = externalImageURL;
	}

	@Override
	public String getExternalFeedUrl() {
		return externalFeedURL;
	}

	@Override
	public void setExternalFeedUrl(String externalFeedURL) {
		this.externalFeedURL = externalFeedURL;
	}

	@Override
	public Boolean getExternal() {
		return isExternal;
	}

	@Override
	public void setExternal(Boolean isExternal) {
		this.isExternal = isExternal;
	}

	@Override
	public boolean isExternal() {
		return isExternal != null && isExternal.booleanValue();
	}

	@Override
	public boolean isInternal() {
		return isExternal != null && !this.isExternal.booleanValue();
	}

	@Override
	public boolean isUndefined() {
		return isExternal == null;
	}

	@Override
	public void setModelVersion(int modelVersion) {
		this.modelVersion = modelVersion;
	}

	@Override
	public int getModelVersion() {
		return modelVersion;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 43254 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		} else if(obj instanceof FeedImpl) {
			FeedImpl feed = (FeedImpl)obj;
			return getKey() != null && getKey().equals(feed.getKey());
		}
		return false;
	}
	
}
