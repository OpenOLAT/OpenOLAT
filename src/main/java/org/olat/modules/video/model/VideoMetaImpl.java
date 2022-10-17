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
package org.olat.modules.video.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.modules.video.VideoFormat;
import org.olat.modules.video.VideoMeta;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;

/**
 * The Class VideoMetaImpl.
 * Initial Date: January 2017
 * @author fkiefer fabian.kiefer@frentix.com
 */
@Entity(name="videometadata")
@Table(name="o_vid_metadata")
public class VideoMetaImpl implements VideoMeta, Persistable, ModifiedInfo {

	private static final long serialVersionUID = 8360426958L;

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
	
	@OneToOne(targetEntity=OLATResourceImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_resource_id", nullable=false, insertable=true, updatable=false)
	private OLATResource videoResource;
	
	@Column(name="vid_url", nullable=true, insertable=true, updatable=true)
	private String url;
	@Column(name="vid_width", nullable=true, insertable=true, updatable=true)
	private int width;
	@Column(name="vid_height", nullable=true, insertable=true, updatable=true)
	private int height;
	@Column(name="vid_size", nullable=true, insertable=true, updatable=true)
	private long size;
	@Column(name="vid_format", nullable=true, insertable=true, updatable=true)
	private String format;
	@Column(name="vid_length", nullable=true, insertable=true, updatable=true)
	private String length;	
	@Column(name="vid_download_enabled", nullable=false, insertable=true, updatable=true)
	private boolean downloadEnabled;	
	
	
	public VideoMetaImpl(int width, int height, long size) {
		this.width = width;
		this.height = height;
		this.size = size;
	}	
	
	public VideoMetaImpl() {
		// make JAXB happy
	}
	
	@Override
	public void setVideoResource(OLATResource videoResource) {
		this.videoResource = videoResource;
	}

	@Override 
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public void setLastModified(Date date) {
		this.lastModified = date;
	}

	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public OLATResource getVideoResource() {
		return videoResource;
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
	public int getWidth() {
		return width;
	}

	@Override
	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public void setSize(long size) {
		this.size = size;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	public VideoFormat getVideoFormat() {
		if(StringHelper.containsNonWhitespace(format)) {
			return VideoFormat.secureValueOf(format);
		}
		return null;
	}

	@Override
	public void setVideoFormat(VideoFormat format) {
		if(format != null) {
			this.format = format.name();
		} else {
			this.format = null;
		}
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
	public int hashCode() {
		return key == null ? 237865 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof VideoMetaImpl) {
			VideoMetaImpl meta = (VideoMetaImpl)obj;
			return key != null && key.equals(meta.key);
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
	
	@Override
	public boolean isDownloadEnabled() {
		return downloadEnabled;
	}
	
	@Override
	public void setDownloadEnabled(boolean downloadEnabled) {
		this.downloadEnabled = downloadEnabled;
	}
}