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
package org.olat.modules.library.ui;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.ForbiddenMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 03.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LibraryThumbnailMapper implements Mapper {
	
	private final String basePath;
	
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public LibraryThumbnailMapper(String basePath) {
		CoreSpringFactory.autowireObject(this);
		this.basePath = basePath;
	}
	
	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		if (relPath.startsWith(basePath)) {
			VFSLeaf file = VFSManager.olatRootLeaf(relPath);
			VFSLeaf thumbnail = vfsRepositoryService.getThumbnail(file, 200, 200, false);
			if(thumbnail != null) {
				return new VFSMediaResource(thumbnail);
			}
			return new NotFoundMediaResource(false);
		}
		return new ForbiddenMediaResource();
	}
}
