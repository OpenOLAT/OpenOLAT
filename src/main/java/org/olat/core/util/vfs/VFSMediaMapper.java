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

import java.io.File;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;

/**
 * 
 * Initial date: 06.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class VFSMediaMapper implements Mapper {
	
	private VFSLeaf vfsLeaf;
	private boolean useMaster = false;
	
	public VFSMediaMapper() {
	}

	public VFSMediaMapper(VFSLeaf vfsLeaf) {
		this.vfsLeaf = vfsLeaf;
	}

	public void setUseMaster(boolean useMaster) {
		this.useMaster = useMaster;
	}

	public VFSMediaMapper(File file) {
		this.vfsLeaf = new LocalFileImpl(file);
	}

	public void setMediaFile(VFSLeaf vfsLeaf) {
		this.vfsLeaf = vfsLeaf;
	}

	public VFSLeaf getVfsLeaf() {
		return vfsLeaf;
	}

	public void setVfsLeaf(VFSLeaf vfsLeaf) {
		this.vfsLeaf = vfsLeaf;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		VFSMediaResource mediaResource = new VFSMediaResource(vfsLeaf);
		mediaResource.setUseMaster(useMaster);
		return mediaResource;
	}
}
