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

import org.olat.core.id.Persistable;
import org.olat.modules.cemedia.MediaVersionMetadata;
import org.olat.modules.video.VideoFormatExtended;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * Initial date: 2023-11-20<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Entity(name= "mediaversionmetadata")
@Table(name="o_media_version_metadata")
public class MediaVersionMetadataImpl implements Persistable, MediaVersionMetadata {

	@Serial
	private static final long serialVersionUID = 5913273399997953714L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@Column(name="p_url", nullable=true, insertable=true, updatable=true)
	private String url;

	@Column(name="p_width", nullable=true, insertable=true, updatable=true)
	private Integer width;

	@Column(name="p_height", nullable=true, insertable=true, updatable=true)
	private Integer height;

	@Column(name="p_length", nullable=true, insertable=true, updatable=true)
	private String length;

	@Column(name="p_format", nullable=true, insertable=true, updatable=true)
	private String format;

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
	public String getUrl() {
		return url;
	}

	@Override
	public void setUrl(String url) {
		this.url = url;
		VideoFormatExtended videoFormat = VideoFormatExtended.valueOfUrl(url);
		if (videoFormat != null) {
			setFormat(videoFormat.name());
		}
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
	public String getLength() {
		return length;
	}

	@Override
	public void setLength(String length) {
		this.length = length;
	}

	@Override
	public String getFormat() {
		return format;
	}

	@Override
	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 28681 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof MediaVersionMetadataImpl mediaVersionStreamingUrl) {
			return getKey() != null && getKey().equals(mediaVersionStreamingUrl.getKey());
		}
		return false;
	}
}
