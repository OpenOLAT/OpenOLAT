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
package org.olat.repository.ui;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryRefImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryImageMapper implements Mapper {
	
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public RepositoryEntryImageMapper() {
		CoreSpringFactory.autowireObject(this);
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		if(relPath.startsWith("/")) {
			relPath = relPath.substring(1, relPath.length());
		}
		
		int lastIndex = relPath.lastIndexOf('.');
		if(lastIndex >= 0) {
			relPath = relPath.substring(0, lastIndex);
		}
		
		MediaResource resource = null;
		if(StringHelper.isLong(relPath)) {
			RepositoryEntryRef re = new RepositoryEntryRefImpl(Long.valueOf(relPath));
			VFSItem image = repositoryService.getIntroductionImage(re);
			if(image instanceof VFSLeaf) {
				//121 is needed to fill the div
				VFSLeaf thumbnail = vfsRepositoryService.getThumbnail((VFSLeaf)image, 180, 120, true);
				if(thumbnail != null) {
					resource = new VFSMediaResource(thumbnail);
				}
				
				if(resource == null) {
					resource = new VFSMediaResource((VFSLeaf)image);
				}
			} else {
				resource = new NotFoundMediaResource();
			}
		} else {
			resource = new NotFoundMediaResource();
		}
		return resource;
	}
}
