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
package org.olat.core.util.vfs;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;

/**
 * Simple mapper for a VFS container
 * 
 * 
 * Initial date: 12.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VFSContainerMapper  implements Mapper {
	private VFSContainer container;
	
	public VFSContainerMapper() {
		//serialization
	}
	
	public VFSContainerMapper(VFSContainer container) {
		this.container = container;
	}
	
	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		VFSItem vfsItem = container.resolve(relPath);
		MediaResource mr;
		if (vfsItem instanceof VFSLeaf) {
			mr = new VFSMediaResource((VFSLeaf) vfsItem);
		} else {
			mr = new NotFoundMediaResource();
		}
		return mr;
	}
}