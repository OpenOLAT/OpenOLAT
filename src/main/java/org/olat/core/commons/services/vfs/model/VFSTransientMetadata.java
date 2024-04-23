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

import java.util.Date;

import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 23 April 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class VFSTransientMetadata implements VFSMetadata {

	private Long key;
	private Date creationDate;
	private Date lastModified;
	private String uuid;
	private boolean deleted;
	private Identity deletedBy;
	private Date deletedDate;
	private String filename;
	private String relativePath;
	private boolean directory;
	private Date fileLastModified;
	private Identity fileLastModifiedBy;
	private Identity fileInitializedBy;
	private long fileSize;
	private Integer transcodingStatus;
	private String uri;
	private String protocol;
	private Boolean cannotGenerateThumbnails;
	private int downloadCount;
	private String comment;
	private String title;
	private String publisher;
	private String creator;
	private String source;
	private String city;
	private String pages;
	private String language;
	private String url;
	private String pubMonth;
	private String pubYear;
	private LicenseType licenseType;
	private String licenseTypeName;
	private String licenseText;
	private String licensor;
	private Date expirationDate;
	private Date lockedDate;
	private Identity lockedBy;
	private boolean locked;
	private long revisionNr;
	private Integer revisionTempNr;
	private String revisionComment;
	private String migrated;
	private String materializedPathKeys;
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
	public Date getDeletedDate() {
		return deletedDate;
	}

	public void setDeletedDate(Date deletedDate) {
		this.deletedDate = deletedDate;
	}

	@Override
	public Identity getDeletedBy() {
		return deletedBy;
	}

	public void setDeletedBy(Identity deletedBy) {
		this.deletedBy = deletedBy;
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
	
	@Override
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

	@Override
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
}
