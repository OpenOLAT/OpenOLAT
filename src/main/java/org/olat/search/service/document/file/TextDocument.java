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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.logging.Tracing;
import org.olat.core.util.io.LimitedContentWriter;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.search.service.SearchResourceContext;

/**
 * Lucene document mapper.
 * 
 * @author Christian Guretzki
 */
public class TextDocument extends FileDocument {
	private static final long serialVersionUID = 9188038452431819507L;
	private static final Logger log = Tracing.createLoggerFor(TextDocument.class);

	public final static String FILE_TYPE = "type.file.text";

	public TextDocument() {
		//
	}

	public static Document createDocument(SearchResourceContext leafResourceContext, VFSLeaf leaf)
	throws IOException, DocumentException, DocumentAccessException {
		
		TextDocument textDocument = new TextDocument();
		textDocument.init(leafResourceContext, leaf);
		textDocument.setFileType(FILE_TYPE);
		textDocument.setCssIcon(CSSHelper.createFiletypeIconCssClassFor(leaf
				.getName()));
		if (log.isDebugEnabled())
			log.debug(textDocument.toString());
		return textDocument.getLuceneDocument();
	}

	@Override
	protected FileContent readContent(VFSLeaf leaf) throws IOException {
		InputStreamReader in = new InputStreamReader(leaf.getInputStream());
		LimitedContentWriter out = new LimitedContentWriter((int)leaf.getSize(), FileDocumentFactory.getMaxFileSize());
		try {
			copy(in, out);
		} catch (Exception e) {
			log.error("", e);
		}
		return new FileContent(out.toString());
	}

	private void copy(Reader input, Writer output) throws IOException {
		char[] buffer = new char[4096];

		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
		}
	}
}
