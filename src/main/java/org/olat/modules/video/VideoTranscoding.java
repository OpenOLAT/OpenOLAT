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
 * Initial date: 05.05.2016<br>
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
public interface VideoTranscoding extends CreateInfo {
	public static final int TRANSCODING_STATUS_WAITING = -1;
	public static final int TRANSCODING_STATUS_DONE = 100;
	public static final int TRANSCODING_STATUS_INEFFICIENT = -2;
	public static final int TRANSCODING_STATUS_ERROR = -3;
	public static final int TRANSCODING_STATUS_TIMEOUT = -4;
	public static final String FORMAT_MP4 = "mp4";
	public static final String TRANSCODER_LOCAL = "Local HandBrakeCLI";
	
	/**
	 * @return key, the database identifier
	 */
	public Long getKey();

	/**
	 * @return date when item was last modified
	 */
	public Date getLastModified();

	/**
	 * @return The video resource of the master video
	 */
	public OLATResource getVideoResource();
	
	/**
	 * @return resolution of video in pixel. After transcoding, the video height
	 *         is the same as the resolution. E.g. for 720p video, the
	 *         resolution is 720 and the final height 720.
	 */
	public int getResolution();

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
	public String getFormat();

	/**
	 * @return format the transcoding format, e.g. mp4
	 */
	public void setFormat(String format);

	/**
	 * @return TRANSCODING_STATUS_WAITING: transcoding has not yet started;
	 *         TRANSCODING_STATUS_DONE: transcoding is finished; in between: %
	 *         of transcoding process
	 */
	public int getStatus();
	
	/**
	 * @param status
	 *            TRANSCODING_STATUS_WAITING: transcoding has not yet started;
	 *            TRANSCODING_STATUS_DONE: transcoding is finished; in between:
	 *            % of transcoding process
	 */
	public void setStatus(int status);


	/**
	 * @return String representing the transcoder type. Could be a local process
	 *         or a remote process which updates the database on its own.
	 */
	public String getTranscoder();

	/**
	 * @return transcoder String representing the transcoder type. Could be a
	 *         local process or a remote process which updates the database on
	 *         its own.
	 */
	public void setTranscoder(String transcoder);

}
