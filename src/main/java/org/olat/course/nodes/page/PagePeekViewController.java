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
package org.olat.course.nodes.page;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.nodes.PageCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PageService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PagePeekViewController extends BasicController {

	private static final Size THUMBNAIL_SIZE = new Size(512, 512, false);
	
	@Autowired
	private PageService pageService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public PagePeekViewController(UserRequest ureq, WindowControl wControl, CourseEnvironment courseEnv, PageCourseNode courseNode) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("peekview");
		Page page = pageService.getPageByKey(courseNode.getPageReferenceKey());
		if(page != null && page.getPreviewMetadata() != null
				&& vfsRepositoryService.isThumbnailAvailable(page.getPreviewMetadata())) {
			VFSMetadata metadata = page.getPreviewMetadata();
			RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
			String mapperId = "/" + courseEntry.getKey() + "_" + courseNode.getIdent()
				+ "_" + metadata.getFileLastModified().getTime();
			String mapperThumbnailUrl = registerCacheableMapper(ureq, "media-thumbnail-" + mapperId,
					new ThumbnailMapper(metadata, vfsRepositoryService));
			mapperThumbnailUrl = mapperThumbnailUrl + "/" + mapperId + "_" + metadata.getFilename();
			mainVC.contextPut("mapperThumbnailUrl", mapperThumbnailUrl);
		}
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private static class ThumbnailMapper implements Mapper {

		private final VFSMetadata metadata;
		private final VFSRepositoryService vfsService;

		public ThumbnailMapper(VFSMetadata metadata, VFSRepositoryService vfsService) {
			this.metadata = metadata;
			this.vfsService = vfsService;
		}

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			VFSLeaf image = (VFSLeaf)vfsService.getItemFor(metadata);
			VFSLeaf thumbnail = vfsService.getThumbnail(image, metadata, THUMBNAIL_SIZE.getWidth(), THUMBNAIL_SIZE.getHeight(), false);
			if(thumbnail != null) {
				return new VFSMediaResource(image);
			}
			return new NotFoundMediaResource();
		}
	}
}
