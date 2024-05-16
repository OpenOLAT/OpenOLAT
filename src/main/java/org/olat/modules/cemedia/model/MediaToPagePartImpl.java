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

import java.io.Serial;
import java.util.Date;
import java.util.Objects;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.modules.ceditor.PagePart;
import org.olat.modules.ceditor.model.jpa.AbstractPart;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaToPagePart;
import org.olat.modules.cemedia.MediaVersion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * Initial date: 2024-04-19<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Entity(name = "mediatopagepart")
@Table(name = "o_media_to_page_part")
public class MediaToPagePartImpl implements MediaToPagePart, Persistable, ModifiedInfo {

	@Serial
	private static final long serialVersionUID = 9043690820171653920L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, insertable = true, updatable = false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creationdate", nullable = false, insertable = true, updatable = false)
	private Date creationDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "lastmodified", nullable = false, insertable = true, updatable = true)
	private Date lastModified;

	@ManyToOne(targetEntity = MediaImpl.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fk_media", nullable = false, insertable = true, updatable = true)
	private Media media;

	@ManyToOne(targetEntity = MediaVersionImpl.class, fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "fk_media_version", nullable = true, insertable = true, updatable = true)
	private MediaVersion mediaVersion;

	@ManyToOne(targetEntity = IdentityImpl.class, fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "fk_identity", nullable = true, insertable = true, updatable = true)
	private Identity identity;

	@ManyToOne(targetEntity = AbstractPart.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fk_page_part", nullable = false, insertable = true, updatable = false)
	private PagePart pagePart;

	/** Used by Hibernate as OrderColumn for GalleryPart.relations */
	@Column(name = "pos", insertable = false, updatable = false)
	private long pos;

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

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public Media getMedia() {
		return media;
	}

	public void setMedia(Media media) {
		this.media = media;
	}

	@Override
	public MediaVersion getMediaVersion() {
		return mediaVersion;
	}

	public void setMediaVersion(MediaVersion mediaVersion) {
		this.mediaVersion = mediaVersion;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public PagePart getPagePart() {
		return pagePart;
	}

	public void setPagePart(PagePart pagePart) {
		this.pagePart = pagePart;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} ;
		if (o == null || getClass() != o.getClass()) {
			return false;
		} ;
		MediaToPagePartImpl that = (MediaToPagePartImpl) o;
		return Objects.equals(key, that.key);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
