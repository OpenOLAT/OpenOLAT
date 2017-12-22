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
package org.olat.core.commons.services.video;

import org.olat.core.commons.services.image.Size;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Initial date: 04.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface MovieService {
	
	/**
	 * Calculate the dimensions of the given movie in terms of width and height
	 * 
	 * @param image
	 * @param suffix
	 * @return
	 */
	public Size getSize(VFSLeaf image, String suffix);

	/**
	 * Calculate the duration of the given movie.
	 * 
	 * @param media
	 * @param suffix
	 * @return long duration in milliseconds
	 */
	public long getDuration(VFSLeaf media, String suffix);
	
	/**
	 * Calculate the number of frames for the given movie.
	 * 
	 * @param media
	 * @param suffix
	 * @return long duration in milliseconds
	 */
	public long getFrameCount(VFSLeaf media, String suffix);

	/**
	 * Checks if a file is really an mp4 file we can handle
	 * @param media
	 * @param fileName
	 * @return 
	 */
	public boolean isMP4(VFSLeaf media, String fileName);
	
}
