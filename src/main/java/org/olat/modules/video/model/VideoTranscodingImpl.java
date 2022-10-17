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
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.modules.video.VideoTranscoding;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;

/**
 * Represents the metadata of a transcoded video file 
 * 
 * Initial date: 05.05.2016<br>
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="videotranscoding")
@Table(name="o_vid_transcoding")
public class VideoTranscodingImpl implements VideoTranscoding, Persistable, ModifiedInfo {

	private static final long serialVersionUID = 8360999803434426958L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@ManyToOne(targetEntity=OLATResourceImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_resource_id", nullable=false, insertable=true, updatable=false)
	private OLATResource videoResource;
	
	@Column(name="vid_resolution", nullable=true, insertable=true, updatable=true)
	private int resolution;
	@Column(name="vid_width", nullable=true, insertable=true, updatable=true)
	private int width;
	@Column(name="vid_height", nullable=true, insertable=true, updatable=true)
	private int height;
	@Column(name="vid_size", nullable=true, insertable=true, updatable=true)
	private long size;
	@Column(name="vid_format", nullable=true, insertable=true, updatable=true)
	private String format;
	@Column(name="vid_status", nullable=true, insertable=true, updatable=true)
	private int status;
	@Column(name="vid_transcoder", nullable=true, insertable=true, updatable=true)
	private String transcoder;

	
	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date date) {
		this.lastModified = date;
	}

	@Override
	public OLATResource getVideoResource() {
		return videoResource;
	}
	
	public void setVideoResource(OLATResource videoResource) {
		this.videoResource = videoResource;
	}

	@Override
	public int getResolution() {
		return resolution;
	}
	
	public void setResolution(int resolution) {
		this.resolution = resolution;
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

	@Override
	public String getFormat() {
		return format;
	}

	@Override
	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public String getTranscoder() {
		return transcoder;
	}

	@Override
	public void setTranscoder(String transcoder) {
		this.transcoder = transcoder;
	}

	@Override
	public int hashCode() {
		return key == null ? -827346537 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof VideoTranscodingImpl) {
			VideoTranscodingImpl videoTranscoding = (VideoTranscodingImpl)obj;
			return key != null && key.equals(videoTranscoding.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
