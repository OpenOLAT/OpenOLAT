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
package org.olat.core.gui.components.htmlsite;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.SimpleHtmlParser;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;

/**
 * 
 * Initial date: 22.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HtmlStaticMapper implements Mapper {
	
	private static final Logger log = Tracing.createLoggerFor(HtmlStaticMapper.class);
	
	private VFSContainer mapperRootContainer;
	
	public HtmlStaticMapper() {
		//for serializable/deserializable
	}
	
	public HtmlStaticMapper(VFSContainer mapperRootContainer) {
		this.mapperRootContainer = mapperRootContainer;
	}
	
	public MediaResource handle(String relPath, HttpServletRequest request) {
		if(log.isDebugEnabled()) log.debug("CPComponent Mapper relPath=" + relPath);
		
		VFSItem currentItem = mapperRootContainer.resolve(relPath);
		if (currentItem == null || (currentItem instanceof VFSContainer)) {
			return new NotFoundMediaResource();
		}
		VFSMediaResource vmr = new VFSMediaResource((VFSLeaf)currentItem);
		String encoding = SimpleHtmlParser.extractHTMLCharset(((VFSLeaf)currentItem));
		if(log.isDebugEnabled()) log.debug("CPComponent Mapper set encoding=" + encoding);
		vmr.setEncoding(encoding);// 
		return vmr;
	}
}
