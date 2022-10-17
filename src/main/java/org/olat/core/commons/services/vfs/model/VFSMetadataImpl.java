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
package org.olat.core.commons.services.vfs.model;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.model.LicenseTypeImpl;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;

import jakarta.persistence.Column;
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
import java.util.Date;

/**
 * 
 * Initial date: 11 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="filemetadata")
@Table(name="o_vfs_metadata")
@NamedQuery(name="metadataOnlyByParent", query="select metadata from filemetadata metadata where metadata.parent.key=:parentKey")
public class VFSMetadataImpl implements Persistable, VFSMetadata {

	private static final long serialVersionUID = 1360000029480576628L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="f_uuid", nullable=false, insertable=true, updatable=true)
	private String uuid;
	@Column(name="f_deleted", nullable=false, insertable=true, updatable=true)
	private boolean deleted;
	@Column(name="f_filename", nullable=false, insertable=true, updatable=true)
	private String filename;
	@Column(name="f_relative_path", nullable=false, insertable=true, updatable=true)
	private String relativePath;
	@Column(name="f_directory", nullable=false, insertable=true, updatable=true)
	private boolean directory;
	@Column(name="f_lastmodified", nullable=false, insertable=true, updatable=true)
	private Date fileLastModified;
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_lastmodified_by", nullable=true, insertable=true, updatable=true)
	private Identity fileLastModifiedBy;
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_initialized_by", nullable=true, insertable=true, updatable=true)
	private Identity fileInitializedBy;
	@Column(name="f_size", nullable=false, insertable=true, updatable=true)
	private long fileSize;
	@Column(name="f_transcoding_status", nullable=true, insertable=true, updatable=true)
	private Integer transcodingStatus;
	@Column(name="f_uri", nullable=false, insertable=true, updatable=true)
	private String uri;
	@Column(name="f_uri_protocol", nullable=false, insertable=true, updatable=true)
	private String protocol;

	@Column(name="f_cannot_thumbnails", nullable=true, insertable=true, updatable=true)
	private Boolean cannotGenerateThumbnails;
	@Column(name="f_download_count", nullable=true, insertable=false, updatable=false)
	private int downloadCount;
	
	@Column(name="f_comment", nullable=true, insertable=true, updatable=true)
	private String comment;
	@Column(name="f_title", nullable=true, insertable=true, updatable=true)
	private String title;
	@Column(name="f_publisher", nullable=true, insertable=true, updatable=true)
	private String publisher;
	@Column(name="f_creator", nullable=true, insertable=true, updatable=true)
	private String creator;
	@Column(name="f_source", nullable=true, insertable=true, updatable=true)
	private String source;
	@Column(name="f_city", nullable=true, insertable=true, updatable=true)
	private String city;
	@Column(name="f_pages", nullable=true, insertable=true, updatable=true)
	private String pages;
	@Column(name="f_language", nullable=true, insertable=true, updatable=true)
	private String language;
	@Column(name="f_url", nullable=true, insertable=true, updatable=true)
	private String url;
	@Column(name="f_pub_month", nullable=true, insertable=true, updatable=true)
	private String pubMonth;
	@Column(name="f_pub_year", nullable=true, insertable=true, updatable=true)
	private String pubYear;

	@ManyToOne(targetEntity=LicenseTypeImpl.class, optional=true)
	@JoinColumn(name="fk_license_type", nullable=true, insertable=true, updatable=true)
	private LicenseType licenseType;
	@Column(name="f_license_type_name", nullable=true, insertable=true, updatable=true)
	private String licenseTypeName;
	@Column(name="f_license_text", nullable=true, insertable=true, updatable=true)
	private String licenseText;
	@Column(name="f_licensor", nullable=true, insertable=true, updatable=true)
	private String licensor;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="f_expiration_date", nullable=true, insertable=true, updatable=true)
	private Date expirationDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="f_locked_date", nullable=true, insertable=true, updatable=true)
	private Date lockedDate;
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_locked_identity", nullable=true, insertable=true, updatable=true)
	private Identity lockedBy;
	@Column(name="f_locked", nullable=true, insertable=true, updatable=true)
	private boolean locked;
	
	@Column(name="f_revision_nr", nullable=true, insertable=true, updatable=true)
	private long revisionNr;
	@Column(name="f_revision_temp_nr", nullable=true, insertable=true, updatable=true)
	private Integer revisionTempNr;
	@Column(name="f_revision_comment", nullable=true, insertable=true, updatable=true)
	private String revisionComment;
	
	@Column(name="f_migrated", nullable=true, insertable=true, updatable=true)
	private String migrated;
	
	@Column(name="f_m_path_keys", nullable=true, insertable=true, updatable=true)
	private String materializedPathKeys;
	
	@ManyToOne(targetEntity=VFSMetadataImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_parent", nullable=true, insertable=true, updatable=true)
	private VFSMetadata parent;

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
	
	public void setCreationDate(Date date) {
		creationDate = date;
	}
	
	@Override
	public Date getLastModified() {
		return lastModified;
	}
	
	@Override
	public void setLastModified(Date date) {
		lastModified = date;
	}

	@Override
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public Date getFileLastModified() {
		return fileLastModified;
	}
	
	public void setFileLastModified(Date date) {
		fileLastModified = date;
	}

	@Override
	public Identity getFileLastModifiedBy() {
		return fileLastModifiedBy;
	}

	public void setFileLastModifiedBy(Identity fileLastModifiedBy) {
		this.fileLastModifiedBy = fileLastModifiedBy;
	}

	@Override
	public Identity getFileInitializedBy() {
		return fileInitializedBy;
	}
	
	public void setFileInitializedBy(Identity fileInitializedBy) {
		this.fileInitializedBy = fileInitializedBy;
	}

	@Override
	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	@Override
	public Integer getTranscodingStatus() {
		return transcodingStatus;
	}

	public boolean isInTranscoding() {
		return transcodingStatus != null && transcodingStatus != VFSMetadata.TRANSCODING_STATUS_DONE;
	}

	@Override
	public boolean isTranscoded() {
		return transcodingStatus != null && transcodingStatus == VFSMetadata.TRANSCODING_STATUS_DONE;
	}

	public void setTranscodingStatus(Integer transcodingStatus) {
		this.transcodingStatus = transcodingStatus;
	}

	@Override
	public boolean isDirectory() {
		return directory;
	}

	public void setDirectory(boolean directory) {
		this.directory = directory;
	}

	@Override
	public String getIconCssClass() {
		String cssClass;
		if (isDirectory()) {
			cssClass =  CSSHelper.CSS_CLASS_FILETYPE_FOLDER;
		} else {
			cssClass = CSSHelper.createFiletypeIconCssClassFor(getFilename());
		}
		return cssClass;
	}

	@Override
	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	@Override
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	@Override
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@Override
	public int getDownloadCount() {
		return downloadCount;
	}

	public void setDownloadCount(int downloadCount) {
		this.downloadCount = downloadCount;
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String getCreator() {
		return creator;
	}

	@Override
	public void setCreator(String creator) {
		this.creator = creator;
	}

	@Override
	public String getCity() {
		return city;
	}

	@Override
	public void setCity(String city) {
		this.city = city;
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
	public String getUuid() {
		return uuid;
	}

	@Override
	public void setUuid(String uuid) {
		this.uuid = uuid;
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
	public String getPublisher() {
		return publisher;
	}

	@Override
	public void setPublisher(String publisher) {
		this.publisher = publisher;
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
	public String getPages() {
		return pages;
	}

	@Override
	public void setPages(String pages) {
		this.pages = pages;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public void setUrl(String url) {
		this.url = url;
	}

	public String getPubMonth() {
		return pubMonth;
	}

	public void setPubMonth(String pubMonth) {
		this.pubMonth = pubMonth;
	}

	public String getPubYear() {
		return pubYear;
	}

	public void setPubYear(String pubYear) {
		this.pubYear = pubYear;
	}

	@Override
	public String[] getPublicationDate() {
		return new String[] { pubYear, pubMonth };
	}
	
	@Override
	public void setPublicationDate(String month, String year) {
		setPubMonth(month);
		setPubYear(year);
	}

	@Override
	public LicenseType getLicenseType() {
		return licenseType;
	}

	@Override
	public void setLicenseType(LicenseType licenseType) {
		this.licenseType = licenseType;
	}

	@Override
	public String getLicenseTypeName() {
		return licenseTypeName;
	}

	@Override
	public void setLicenseTypeName(String licenseTypeName) {
		this.licenseTypeName = licenseTypeName;
	}

	@Override
	public String getLicenseText() {
		return licenseText;
	}

	@Override
	public void setLicenseText(String licenseText) {
		this.licenseText = licenseText;
	}

	@Override
	public String getLicensor() {
		return licensor;
	}

	@Override
	public void setLicensor(String licensor) {
		this.licensor = licensor;
	}

	@Override
	public Date getExpirationDate() {
		return expirationDate;
	}

	@Override
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	@Override
	public Date getLockedDate() {
		return lockedDate;
	}

	@Override
	public void setLockedDate(Date lockedDate) {
		this.lockedDate = lockedDate;
	}

	@Override
	public Identity getLockedBy() {
		return lockedBy;
	}

	@Override
	public void setLockedBy(Identity lockedBy) {
		this.lockedBy = lockedBy;
	}

	@Override
	public boolean isLocked() {
		return locked;
	}

	@Override
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	@Override
	public Boolean getCannotGenerateThumbnails() {
		return cannotGenerateThumbnails;
	}

	@Override
	public void setCannotGenerateThumbnails(Boolean cannotGenerateThumbnails) {
		this.cannotGenerateThumbnails = cannotGenerateThumbnails;
	}

	public String getMaterializedPathKeys() {
		return materializedPathKeys;
	}

	public void setMaterializedPathKeys(String materializedPathKeys) {
		this.materializedPathKeys = materializedPathKeys;
	}

	@Override
	public String getMigrated() {
		return migrated;
	}

	public void setMigrated(String migrated) {
		this.migrated = migrated;
	}

	@Override
	public long getRevisionNr() {
		return revisionNr;
	}

	@Override
	public void setRevisionNr(long revisionNr) {
		this.revisionNr = revisionNr;
	}

	@Override
	public Integer getRevisionTempNr() {
		return revisionTempNr;
	}

	@Override
	public void setRevisionTempNr(Integer revisionTempNr) {
		this.revisionTempNr = revisionTempNr;
	}

	@Override
	public String getRevisionComment() {
		return revisionComment;
	}

	@Override
	public void setRevisionComment(String revisionComment) {
		this.revisionComment = revisionComment;
	}

	public VFSMetadata getParent() {
		return parent;
	}

	public void setParent(VFSMetadata parent) {
		this.parent = parent;
	}

	@Override
	public void copyValues(VFSMetadata fromMeta, boolean override) {
		if(override || !StringHelper.containsNonWhitespace(getComment())) {
			setComment(fromMeta.getComment());
		}
		if(override || !StringHelper.containsNonWhitespace(getCity())) {
			setCity(fromMeta.getCity());
		}
		if(override || !StringHelper.containsNonWhitespace(getCreator())) {
			setCreator(fromMeta.getCreator());
		}
		if(override || !StringHelper.containsNonWhitespace(getLanguage())) {
			setLanguage(fromMeta.getLanguage());
		}
		if(override || !StringHelper.containsNonWhitespace(getPages())) {
			setPages(fromMeta.getPages());
		}
		if(override || getPublicationDate() == null) {
			setPublicationDate(fromMeta.getPublicationDate()[1], fromMeta.getPublicationDate()[0]);
		}
		if(override || !StringHelper.containsNonWhitespace(getPublisher())) {
			setPublisher(fromMeta.getPublisher());
		}
		if(override || !StringHelper.containsNonWhitespace(getSource())) {
			setSource(fromMeta.getSource());
		}
		if(override || !StringHelper.containsNonWhitespace(getTitle())) {
			setTitle(fromMeta.getTitle());
		}
		if(override || !StringHelper.containsNonWhitespace(getUrl())) {
			setUrl(fromMeta.getUrl());
		}
		if(override || getLicenseType() == null) {
			setLicenseType(fromMeta.getLicenseType());
			setLicenseTypeName(fromMeta.getLicenseTypeName());
			setLicensor(fromMeta.getLicensor());
			setLicenseText(fromMeta.getLicenseText());
		}
	}
	
	public void copyValues(VFSRevisionImpl fromMeta) {
		setFileInitializedBy(fromMeta.getFileInitializedBy());
		setFileLastModifiedBy(fromMeta.getFileLastModifiedBy());
		setComment(fromMeta.getComment());
		setCity(fromMeta.getCity());
		setCreator(fromMeta.getCreator());
		setLanguage(fromMeta.getLanguage());
		setPages(fromMeta.getPages());
		setPublicationDate(fromMeta.getPubMonth(), fromMeta.getPubYear());
		setPublisher(fromMeta.getPublisher());
		setSource(fromMeta.getSource());
		setTitle(fromMeta.getTitle());
		setUrl(fromMeta.getUrl());
		setLicenseType(fromMeta.getLicenseType());
		setLicenseTypeName(fromMeta.getLicenseTypeName());
		setLicensor(fromMeta.getLicensor());
		setLicenseText(fromMeta.getLicenseText());
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 7386459 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof VFSMetadataImpl) {
			VFSMetadataImpl meta = (VFSMetadataImpl)obj;
			return getKey().equals(meta.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
