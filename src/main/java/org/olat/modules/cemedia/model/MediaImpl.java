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
package org.olat.modules.cemedia.model;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaVersion;

/**
 * 
 * Initial date: 17.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="mmedia")
@Table(name="o_media")
public class MediaImpl implements Persistable, CreateInfo, Media  {

	private static final long serialVersionUID = -8066676191014353560L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="p_collection_date", nullable=false, insertable=true, updatable=false)
	private Date collectionDate;
	@Column(name="p_type", nullable=false, insertable=true, updatable=false)
	private String type;

	@Column(name="p_title", nullable=false, insertable=true, updatable=true)
	private String title;
	@Column(name="p_description", nullable=true, insertable=true, updatable=true)
	private String description;
	@Column(name="p_alt_text", nullable=true, insertable=true, updatable=true)
	private String altText;
	@Column(name="p_signature", nullable=false, insertable=true, updatable=true)
	private int signature;
	@Column(name="p_business_path", nullable=true, insertable=true, updatable=true)
	private String businessPath;
	@Column(name="p_reference_id", nullable=true, insertable=true, updatable=true)
	private String referenceId;
	@Column(name="p_uuid", insertable=true, updatable=true)
	private String uuid;

	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_author_id", nullable=false, insertable=true, updatable=false)
	private Identity author;
	
	//Dublin core
	@Column(name="p_creators", nullable=true, insertable=true, updatable=true)
	private String creators;// Dublin core
	@Column(name="p_place", nullable=true, insertable=true, updatable=true)
	private String place; // Zot + City of MetaInfo
	@Column(name="p_publisher", nullable=true, insertable=true, updatable=true)
	private String publisher;// Zot + Dublin core
	@Column(name="p_publication_date", nullable=true, insertable=true, updatable=true)
	private Date publicationDate;
	@Column(name="p_date", nullable=true, insertable=true, updatable=true)
	private String date;// Zot + Dublin core
	@Column(name="p_url", nullable=true, insertable=true, updatable=true)
	private String url;// Zot + Dublin core
	@Column(name="p_source", nullable=true, insertable=true, updatable=true)
	private String source;// Dublin core
	@Column(name="p_language", nullable=true, insertable=true, updatable=true)
	private String language;// Dublin core
	
	@Column(name="p_metadata_xml", nullable=true, insertable=true, updatable=true)
	private String metadataXml;// Dublin core
	
	@OneToMany(targetEntity=MediaVersionImpl.class, mappedBy="media", fetch=FetchType.LAZY,
			orphanRemoval=true, cascade={CascadeType.REMOVE})
	@OrderColumn(name="pos")
	private List<MediaVersion> versions;
	
	public MediaImpl() {
		//
	}

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getCollectionDate() {
		return collectionDate;
	}

	public void setCollectionDate(Date collectionDate) {
		this.collectionDate = collectionDate;
	}

	@Override
	public String getResourceableTypeName() {
		return MEDIA_RESOURCE_TYPE;
	}

	@Override
	public Long getResourceableId() {
		return getKey();
	}

	@Override
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
	public String getAltText() {
		return altText;
	}

	@Override
	public void setAltText(String altText) {
		this.altText = altText;
	}

	public int getSignature() {
		return signature;
	}

	public void setSignature(int signature) {
		this.signature = signature;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	@Override
	public String getBusinessPath() {
		return businessPath;
	}

	public void setBusinessPath(String businessPath) {
		this.businessPath = businessPath;
	}

	@Override
	public String getCreators() {
		return creators;
	}

	@Override
	public void setCreators(String creators) {
		this.creators = creators;
	}

	@Override
	public String getPlace() {
		return place;
	}

	@Override
	public void setPlace(String place) {
		this.place = place;
	}

	@Override
	public String getPublisher() {
		return publisher;
	}

	@Override
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	@Override
	public Date getPublicationDate() {
		return publicationDate;
	}

	@Override
	public void setPublicationDate(Date publicationDate) {
		this.publicationDate = publicationDate;
	}

	@Override
	public String getDate() {
		return date;
	}

	@Override
	public void setDate(String date) {
		this.date = date;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String getSource() {
		return source;
	}

	@Override
	public void setSource(String source) {
		this.source = source;
	}

	@Override
	public String getLanguage() {
		return language;
	}

	@Override
	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public String getMetadataXml() {
		return metadataXml;
	}

	@Override
	public void setMetadataXml(String metadataXml) {
		this.metadataXml = metadataXml;
	}

	@Override
	public Identity getAuthor() {
		return author;
	}

	public void setAuthor(Identity author) {
		this.author = author;
	}

	@Override
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public List<MediaVersion> getVersions() {
		return versions;
	}

	public void setVersions(List<MediaVersion> versions) {
		this.versions = versions;
	}

	@Override
	public int hashCode() {
		return key == null ? 459537 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof MediaImpl media) {
			return key != null && key.equals(media.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
