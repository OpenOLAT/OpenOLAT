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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.model.LicenseTypeImpl;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 18 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="vfsrevision")
@Table(name="o_vfs_revision")
public class VFSRevisionImpl implements Persistable, VFSRevision {

	private static final long serialVersionUID = 2868142296811338251L;

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

	@Column(name="f_revision_nr", nullable=false, insertable=true, updatable=true)
	private int revisionNr;
	@Column(name="f_revision_filename", nullable=false, insertable=true, updatable=true)
	private String filename;
	@Column(name="f_revision_size", nullable=false, insertable=true, updatable=true)
	private long size;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="f_revision_lastmodified", nullable=false, insertable=true, updatable=true)
	private Date fileLastModified;
	@Column(name="f_revision_comment", nullable=true, insertable=true, updatable=true)
	private String revisionComment;
	
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
	
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_lastmodified_by", nullable=true, insertable=true, updatable=true)
	private Identity fileLastModifiedBy;
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_initialized_by", nullable=true, insertable=true, updatable=true)
	private Identity fileInitializedBy;
	@ManyToOne(targetEntity=VFSMetadataImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_metadata", nullable=false, insertable=true, updatable=false)
	private VFSMetadata metadata;
	
	
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

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
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
	public Date getFileLastModified() {
		return fileLastModified;
	}
	
	public void setFileLastModified(Date date) {
		this.fileLastModified = date;
	}
	
	@Override
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public int getRevisionNr() {
		return revisionNr;
	}
	
	public void setRevisionNr(int revision) {
		this.revisionNr = revision;
	}
	
	@Override
	public String getRevisionComment() {
		return revisionComment;
	}
	
	public void setRevisionComment(String text) {
		revisionComment = text;
	}
	
	@Override
	public long getSize() {
		return size;
	}
	
	public void setSize(long size) {
		this.size = size;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPages() {
		return pages;
	}

	public void setPages(String pages) {
		this.pages = pages;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getUrl() {
		return url;
	}

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

	public LicenseType getLicenseType() {
		return licenseType;
	}

	public void setLicenseType(LicenseType licenseType) {
		this.licenseType = licenseType;
	}

	public String getLicenseTypeName() {
		return licenseTypeName;
	}

	public void setLicenseTypeName(String licenseTypeName) {
		this.licenseTypeName = licenseTypeName;
	}

	public String getLicenseText() {
		return licenseText;
	}

	public void setLicenseText(String licenseText) {
		this.licenseText = licenseText;
	}

	public String getLicensor() {
		return licensor;
	}

	public void setLicensor(String licensor) {
		this.licensor = licensor;
	}

	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public VFSMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(VFSMetadata metadata) {
		this.metadata = metadata;
	}
	
	public void copyValues(VFSMetadata fromMeta) {
		setComment(fromMeta.getComment());
		setCity(fromMeta.getCity());
		setCreator(fromMeta.getCreator());
		setLanguage(fromMeta.getLanguage());
		setPages(fromMeta.getPages());
		String[] pubDates = fromMeta.getPublicationDate();
		setPubYear(pubDates[0]);
		setPubMonth(pubDates[1]);
		setPublisher(fromMeta.getPublisher());
		setSource(fromMeta.getSource());
		setTitle(fromMeta.getTitle());
		setUrl(fromMeta.getUrl());
		setLicenseType(fromMeta.getLicenseType());
		setLicenseTypeName(fromMeta.getLicenseTypeName());
		setLicensor(fromMeta.getLicensor());
		setLicenseText(fromMeta.getLicenseText());
	}
	
	public void copyValues(VFSRevisionImpl fromMeta) {
		setComment(fromMeta.getComment());
		setCity(fromMeta.getCity());
		setCreator(fromMeta.getCreator());
		setLanguage(fromMeta.getLanguage());
		setPages(fromMeta.getPages());
		setPubYear(fromMeta.getPubYear());
		setPubMonth(fromMeta.getPubMonth());
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
		return getKey() == null ? 221879 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof VFSRevisionImpl) {
			VFSRevisionImpl rev = (VFSRevisionImpl)obj;
			return getKey() != null && getKey().equals(rev.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
