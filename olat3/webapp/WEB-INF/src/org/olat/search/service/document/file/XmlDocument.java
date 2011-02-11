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
* <p>
*/ 

package org.olat.search.service.document.file;

import java.io.BufferedInputStream;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.search.service.SearchResourceContext;

/**
 * Lucene document mapper.
 * @author Christian Guretzki
 */
public class XmlDocument extends FileDocument {
	private static final OLog log = Tracing.createLoggerFor(XmlDocument.class);

	public static final String FILE_TYPE = "type.file.html";

	public XmlDocument() {
		super();
	}
	
	public static Document createDocument(SearchResourceContext leafResourceContext, VFSLeaf leaf) throws IOException,DocumentException,DocumentAccessException {
    XmlDocument htmlDocument = new XmlDocument();
    htmlDocument.init(leafResourceContext,leaf);
    htmlDocument.setFileType(FILE_TYPE);
		htmlDocument.setCssIcon("b_filetype_xml");
		if (log.isDebug() ) log.debug(htmlDocument.toString());
		return htmlDocument.getLuceneDocument();
	}
	
	protected String readContent(VFSLeaf leaf) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(leaf.getInputStream());
    String inputString = FileUtils.load(bis, "utf-8");
    // Remove all HTML and &nbsp; Tags
    if (log.isDebug() ) log.debug("HTML content with tags :" + inputString);
    String output = FilterFactory.getHtmlTagsFilter().filter(inputString);
    if (log.isDebug() ) log.debug("HTML content without tags :" + output);
  	bis.close();
		return output;
	}

}
