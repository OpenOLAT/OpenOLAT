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

import java.util.Map;

/**
 * Represents the metadata of a master video file 
 * 
 * Initial date: 05.05.2016<br>
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
public interface VideoMetadata {

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
	 * @return Map of caption tracks
	 */
	public Map<String, String> getAllTracks();

	/**
	 * Add a caption track for a specific language
	 * @param lang the lang key
	 * @param trackFile the srt track filename
	 */
	public void addTrack(String lang, String trackFile);

	/**
	 * Get the caption track file name for a specific language
	 * @param lang
	 * @return track file name
	 */
	public String getTrack(String lang);

	/**
	 * Remove the caption track for the given lang key
	 * @param lang
	 */
	public void removeTrack(String lang);

}
