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
package org.olat.modules.bigbluebutton.manager;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.ForbiddenMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;

/**
 * 
 * Initial date: 23 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SlidesContainerMapper implements Mapper {
	
	private static final Logger log = Tracing.createLoggerFor(SlidesContainerMapper.class);
	public static final String DOWNLOAD_PREFIX = "/slides/";
	
	private final VFSContainer container;
	private final VFSContainer tempContainer;
	
	public SlidesContainerMapper(VFSContainer container) {
		this(null, container);
	}
	
	public SlidesContainerMapper(VFSContainer tempContainer, VFSContainer container) {
		this.container = container;
		this.tempContainer = tempContainer;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		MediaResource resource = null;
		if(relPath.startsWith(DOWNLOAD_PREFIX)) {
			String filename = relPath.substring(DOWNLOAD_PREFIX.length());
			VFSItem slide = null;
			if(tempContainer != null) {
				slide = tempContainer.resolve(filename);
			}
			if(slide == null && container != null) {
				slide = container.resolve(filename);
			}
			if(slide instanceof VFSLeaf) {
				resource = new VFSMediaResource((VFSLeaf)slide);
			} else {
				log.warn("Slides not found: {}", relPath);
				resource = new NotFoundMediaResource();
			}	
		} else {
			log.warn("Slides path forbidden: {}", relPath);
			resource = new ForbiddenMediaResource();
		}
		return resource;
	}
}
