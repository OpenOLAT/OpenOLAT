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
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.MediaVersionMetadata;

/**
 * 
 * Initial date: 15 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="mediaversion")
@Table(name="o_media_version")
public class MediaVersionImpl implements Persistable, MediaVersion {
	
	private static final long serialVersionUID = -7573741424138051540L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	/** Only used for order by */
	@Column(name="pos", insertable=false, updatable=false)//order hack
	private long pos;
	
	@Column(name="p_version", insertable=true, updatable=false)
	private String versionName;
	@Column(name="p_version_uuid", insertable=true, updatable=true)
	private String versionUuid;
	@Column(name="p_version_checksum", insertable=true, updatable=true)
	private String versionChecksum;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="p_collection_date", nullable=false, insertable=true, updatable=true)
	private Date collectionDate;
	
	@Column(name="p_storage_path", nullable=true, insertable=true, updatable=true)
	private String storagePath;
	@Column(name="p_root_filename", nullable=true, insertable=true, updatable=true)
	private String rootFilename;
	@Column(name="p_content", nullable=true, insertable=true, updatable=true)
	private String content;

	@ManyToOne(targetEntity=MediaImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_media", nullable=false, insertable=true, updatable=false)
	private Media media;
	
	@ManyToOne(targetEntity=VFSMetadataImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_metadata", nullable=true, insertable=true, updatable=true)
	private VFSMetadata metadata;

	@OneToOne(targetEntity=MediaVersionMetadataImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_version_metadata", nullable=true, insertable=true, updatable=true)
	private MediaVersionMetadata versionMetadata;

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
	
	public long getPos() {
		return pos;
	}
	
	public void setPos(long pos) {
		this.pos = pos;
	}

	@Override
	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	@Override
	public String getVersionUuid() {
		return versionUuid;
	}

	public void setVersionUuid(String versionUuid) {
		this.versionUuid = versionUuid;
	}

	@Override
	public String getVersionChecksum() {
		return versionChecksum;
	}

	public void setVersionChecksum(String versionChecksum) {
		this.versionChecksum = versionChecksum;
	}

	@Override
	public Date getCollectionDate() {
		return collectionDate;
	}
	
	public void setCollectionDate(Date collectionDate) {
		this.collectionDate = collectionDate;
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
	public String getStoragePath() {
		return storagePath;
	}

	@Override
	public void setStoragePath(String storagePath) {
		this.storagePath = storagePath;
	}

	@Override
	public String getRootFilename() {
		return rootFilename;
	}

	@Override
	public void setRootFilename(String rootFilename) {
		this.rootFilename = rootFilename;
	}

	@Override
	public Media getMedia() {
		return media;
	}

	public void setMedia(Media media) {
		this.media = media;
	}
	
	@Override
	public VFSMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(VFSMetadata metadata) {
		this.metadata = metadata;
	}

	@Override
	public MediaVersionMetadata getVersionMetadata() {
		return versionMetadata;
	}

	@Override
	public void setVersionMetadata(MediaVersionMetadata versionMetadata) {
		this.versionMetadata = versionMetadata;
	}

	@Override
	public boolean hasUrl() {
		return versionMetadata != null && StringHelper.containsNonWhitespace(versionMetadata.getUrl());
	}

	@Override
	public boolean sameAs(MediaVersion version) {
		return Objects.equals(content, version.getContent())
				&& Objects.equals(storagePath, version.getStoragePath())
				&& Objects.equals(rootFilename, version.getRootFilename());
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 459537 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof MediaVersionImpl mediaVersion) {
			return getKey() != null && getKey().equals(mediaVersion.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
