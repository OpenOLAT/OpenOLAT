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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Iterator;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.poi.hwpf.HWPFOldDocument;
import org.apache.poi.hwpf.OldWordFileFormatException;
import org.apache.poi.hwpf.extractor.Word6Extractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
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
public class WordDocument extends FileDocument {
	private static final long serialVersionUID = 1827194935338994490L;
	private static final Logger log = Tracing.createLoggerFor(WordDocument.class);

	public static final String FILE_TYPE = "type.file.word";

	public WordDocument() {
		//
	}

	public static Document createDocument(SearchResourceContext leafResourceContext, VFSLeaf leaf)
	throws IOException, DocumentException, DocumentAccessException {
		WordDocument wordDocument = new WordDocument();
		wordDocument.init(leafResourceContext, leaf);
		wordDocument.setFileType(FILE_TYPE);
		wordDocument.setCssIcon(CSSHelper.createFiletypeIconCssClassFor(leaf.getName()));
		if (log.isDebugEnabled())
			log.debug(wordDocument.toString());
		return wordDocument.getLuceneDocument();
	}

	@Override
	protected FileContent readContent(VFSLeaf leaf)
	throws IOException, DocumentException {
		try(LimitedContentWriter sb = new LimitedContentWriter((int)leaf.getSize(), FileDocumentFactory.getMaxFileSize());
				InputStream bis = new BufferedInputStream(leaf.getInputStream())) {
			POIFSFileSystem filesystem = new POIFSFileSystem(bis);
			Iterator<?> entries = filesystem.getRoot().getEntries();
			while (entries.hasNext()) {
				Entry entry = (Entry) entries.next();
				String name = entry.getName();
				if (!(entry instanceof DocumentEntry)) {
					// Skip directory entries
				} else if ("WordDocument".equals(name)) {
					collectWordDocument(leaf, filesystem, sb);
				}
			}
			return new FileContent(sb.toString());
		} catch (Exception e) {
			log.warn("could not read in word document: {} please check, that this is not an docx/rtf/html file!", leaf);
			throw new DocumentException(e.getMessage());
		}
	}

	private void collectWordDocument(VFSLeaf leaf, POIFSFileSystem filesystem, Writer sb) throws IOException {
		try(WordExtractor extractor = new WordExtractor(filesystem)) {
			addTextIfAny(sb, extractor.getTextFromPieces());
		} catch(OldWordFileFormatException ex) {
			collectOldWordDocument(leaf, sb);
		} catch(Exception e) {
			log.error("Cannot read word document: {}", leaf, e);
		}
	}
	
	private void collectOldWordDocument(VFSLeaf leaf, Writer sb) throws IOException {
		try(InputStream bis = new BufferedInputStream(leaf.getInputStream())) {
            POIFSFileSystem pfs = new POIFSFileSystem(bis);
            HWPFOldDocument doc = new HWPFOldDocument(pfs);
            Word6Extractor docExtractor = new Word6Extractor(doc);
            addTextIfAny(sb, docExtractor.getText());
		} catch(Exception e) {
			log.error("Cannot read old word document: {}", leaf, e);
		}
	}

	private void addTextIfAny(Writer sb, String text) throws IOException {
		if (text != null && text.length() > 0) {
			sb.append(text).append(' ');
		}
	}
}
