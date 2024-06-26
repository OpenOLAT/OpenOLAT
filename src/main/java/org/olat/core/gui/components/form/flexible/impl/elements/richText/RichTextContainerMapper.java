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
package org.olat.core.gui.components.form.flexible.impl.elements.richText;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;

/**
 * 
 * Initial date: 18 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RichTextContainerMapper implements Mapper {
	
	private String path;
	private final VFSContainer container;
	
	public RichTextContainerMapper(VFSContainer container, String relFilePath) {
		this.container = container;
		path = relFilePath;
		if(!path.startsWith("/")) {
			path = "/" + path;
		}
		if(!path.endsWith("/")) {
			path += "/";
		}
	}
	
	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		VFSItem vfsItem = container.resolve(relPath);
		if(vfsItem == null && relPath.startsWith(path)) {
			String fallback = relPath.substring(path.length(), relPath.length());
			vfsItem = container.resolve(fallback);
		}
		
		MediaResource mr;
		if (vfsItem instanceof VFSLeaf) {
			mr = new VFSMediaResource((VFSLeaf) vfsItem);
		} else {
			mr = new NotFoundMediaResource();
		}
		return mr;
	}
}
