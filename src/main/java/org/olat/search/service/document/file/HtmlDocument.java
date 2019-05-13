/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.search.service.document.file;

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.logging.Tracing;
import org.olat.core.util.filter.impl.NekoHTMLFilter;
import org.olat.core.util.filter.impl.NekoHTMLFilter.NekoContent;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.search.service.SearchResourceContext;

/**
 * Lucene document mapper.
 * @author Christian Guretzki
 */
public class HtmlDocument extends FileDocument {
	private static final long serialVersionUID = 4816587780227792396L;
	private static final Logger log = Tracing.createLoggerFor(HtmlDocument.class);

	public static final String FILE_TYPE = "type.file.html";

	public HtmlDocument() {
		//
	}
	
	public static Document createDocument(SearchResourceContext leafResourceContext, VFSLeaf leaf) throws IOException,DocumentException,DocumentAccessException {
		HtmlDocument htmlDocument = new HtmlDocument();
		htmlDocument.init(leafResourceContext, leaf);
		htmlDocument.setFileType(FILE_TYPE);
		htmlDocument.setCssIcon(CSSHelper.createFiletypeIconCssClassFor(leaf.getName()));
		if (log.isDebugEnabled() ) log.debug(htmlDocument.toString());
		return htmlDocument.getLuceneDocument();
	}
	
	@Override
	protected FileContent readContent(VFSLeaf leaf) throws DocumentException {
		try(InputStream is = leaf.getInputStream()) {
			// Remove all HTML and &nbsp; Tags
			NekoContent output = new NekoHTMLFilter().filter(is);
			if (log.isDebugEnabled())
				log.debug("HTML content without tags :" + output);
	
			return new FileContent(output.getTitle(), output.getContent());
		} catch(Exception e) {
			throw new DocumentException(e.getMessage()); 
		}
	}
}
