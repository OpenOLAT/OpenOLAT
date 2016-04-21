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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.olat.core.commons.services.image.Size;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.video.model.VideoQualityVersion;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;

/**
 * The <code>VideoManager</code> singleton is responsible for dealing with video
 * resources.
 *
 * Initial date: 01.04.2015<br>
 * @author Dirk Furrer, dirk.furrer@frentix.com, http://www.frentix.com
 */
public interface VideoManager {

	public abstract File getVideoFile(OLATResource video);

	public abstract Size getVideoSize(OLATResource video);

	public abstract void setVideoSize(OLATResource video, Size size);

	public abstract VFSLeaf getPosterframe(OLATResource video);

	public abstract void setPosterframe(OLATResource video, VFSLeaf posterframe);

	public abstract void setTitle(OLATResource video, String title);

	public abstract String getTitle(OLATResource video);

	public abstract HashMap<String, VFSLeaf> getAllTracks(OLATResource video);

	public abstract void addTrack(OLATResource video, String lang, VFSLeaf trackFile);

	public abstract VFSLeaf getTrack(OLATResource video, String lang);

	public abstract void removeTrack(OLATResource video, String lang);

	public abstract boolean getFrame(OLATResource video, int frameNumber, VFSLeaf frame) throws IOException;

	public abstract void setDescription(OLATResource video, String text);

	public abstract String getDescription(OLATResource video);

	public abstract boolean optimizeVideoRessource(OLATResource video);
	
	public abstract List<VideoQualityVersion> getQualityVersions(OLATResource video);

	/**
	 * Get a human readable aspect ratio from the given video size. Recognizes
	 * the most common aspect ratios
	 * 
	 * @param videoSize
	 *            Must not be NULL
	 * @return String containing a displayable aspect ratio
	 */
	public abstract String getAspectRatio(Size videoSize);

	/**
	 * Get the media container for this resource where the actual video is stored
	 * @param videoResource
	 * @return VFSContainer
	 */
	public abstract VFSContainer getMediaContainer(OLATResource videoResource);
	
	/**
	 * Get the container where all the transcoded videos are stored
	 * @param videoResource
	 * @return VFSContainer
	 */
	public abstract VFSContainer getOptimizedDataContainer(OLATResource videoResource);
	
	/**
	 * Get the master video file 
	 * @param videoRepoEntry
	 * @return VFSLeaf or NULL if it does not exist
	 */
	public abstract VFSLeaf getMasterVideoFile(RepositoryEntry videoRepoEntry);

}