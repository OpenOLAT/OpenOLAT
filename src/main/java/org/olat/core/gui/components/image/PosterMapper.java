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
package org.olat.core.gui.components.image;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;

/**
 * 
 * Initial date: 17 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PosterMapper implements Mapper {

	private Size size;
	private VFSLeaf poster;
	private VFSLeaf media;

	public VFSLeaf getPoster() {
		return poster;
	}

	public void setPoster(VFSLeaf poster) {
		this.poster = poster;
	}

	public void setThumbnailWithSize(VFSLeaf media, Size size) {
		this.size = size;
		this.media = media;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		if(poster != null) {
			return new VFSMediaResource(poster);
		}
		
		if(size != null && media != null && media.canMeta() == VFSStatus.YES) {
			VFSLeaf thumbnail = CoreSpringFactory.getImpl(VFSRepositoryService.class)
					.getThumbnail(media, size.getWidth(), size.getHeight(), true);
			if(thumbnail != null) {
				return new VFSMediaResource(thumbnail);
			}
		}
		return null;
	}
}
