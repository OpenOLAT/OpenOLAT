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
package org.olat.modules.video.managers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.olat.core.commons.services.image.Size;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.resource.OLATResource;

/**
 * The <code>VideoManager</code> singleton is responsible for dealing with video
 * resources.
 *
 * Initial date: 01.04.2015<br>
 * @author Dirk Furrer, dirk.furrer@frentix.com, http://www.frentix.com
 */
public abstract class VideoManager extends BasicManager {

	protected static VideoManager INSTANCE;

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

	public abstract void setCommentsEnabled(OLATResource video, boolean isEnabled);

	public abstract boolean getCommentsEnabled(OLATResource video);

	public abstract void setRatingEnabled(OLATResource video, boolean isEnabled);

	public abstract boolean getRatingEnabled(OLATResource video);

	public abstract boolean getFrame(OLATResource video, int frameNumber, VFSLeaf frame) throws IOException;

	public abstract void setDescription(OLATResource video, String text);

	public abstract String getDescription(OLATResource video);

	public abstract boolean optimizeVideoRessource(OLATResource video);

}