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
package org.olat.modules.video;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.resource.OLATResource;

/**
 * Represents the metadata of a transcoded video file 
 * 
 * Initial date: 19.01.2017<br>
 * @author fkiefer, fabian.kiefer@frentix.com, http://www.frentix.com
 *
 */
public interface VideoMeta extends CreateInfo {
	public static final String FORMAT_MP4 = "mp4";
	
	/**
	 * @return key, the database identifier
	 */
	public Long getKey();

	/**
	 * @return The video resource of the master video
	 */
	public OLATResource getVideoResource();
	
	/**
	 * @return The URL of an external video
	 */
	public String getUrl();
	
	public void setUrl(String url);
	
	/**
	 * @return width of transcoded video in pixel
	 */
	public int getWidth();

	/**
	 * @param width of video in pixel after transcoding
	 */
	public void setWidth(int width);

	/**
	 * @return height of transcoded video in pixel
	 */
	public int getHeight();

	/**
	 * @param height of video in pixel after transcoding
	 */
	public void setHeight(int height);

	/**
	 * @return the video file size in bytes
	 */
	public long getSize();

	/**
	 * @param size the file size (bytes) of the transcoded video
	 */
	public void setSize(long size);

	/**
	 * @return the transcoding format, e.g. mp4
	 */
	public VideoFormat getVideoFormat();

	/**
	 * @return format the transcoding format, e.g. mp4
	 */
	public void setVideoFormat(VideoFormat format);

	
	/**
	 * Gets the length of the video as string.
	 *
	 * @return the length
	 */
	public String getLength();
	
	/**
	 * Sets the length.
	 *
	 * @param length the new length
	 */
	public void setLength(String length);

	/**
	 * Sets the creation date.
	 *
	 * @param creationdate
	 */
	public void setCreationDate(Date creationdate);

	/**
	 * Sets the video resource.
	 *
	 * @param videoResource the new video resource
	 */
	public void setVideoResource(OLATResource videoResource);
	
	public boolean isDownloadEnabled();
	
	public void setDownloadEnabled(boolean downloadEnabled);
	
}
