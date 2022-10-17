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
package org.olat.modules.video.manager;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.ForbiddenMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;

public class VideoMediaMapper implements Mapper  {
	
	private final VFSContainer mediaBase;
	
	public VideoMediaMapper(VFSContainer mediaBase) {
		this.mediaBase = mediaBase;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		if(relPath != null && relPath.contains("..")) { 
			return new ForbiddenMediaResource();
		}
		VFSItem mediaFile = mediaBase.resolve(relPath);
		if (mediaFile instanceof VFSLeaf && !relPath.endsWith("xml")){
			VFSMediaResource res =  new VFSMediaResource((VFSLeaf)mediaFile);
			if (relPath.toLowerCase().endsWith("srt") && relPath.toLowerCase().endsWith("vtt")) {
				// SRT caption files are supposed to be UTF-8, see
				// https://en.wikipedia.org/wiki/SubRip#Text_encoding
				res.setEncoding("utf-8");				
			}
			return res;
		} else {
 			return new NotFoundMediaResource();
		}
	}
}
