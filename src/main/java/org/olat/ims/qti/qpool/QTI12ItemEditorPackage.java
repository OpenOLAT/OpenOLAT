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
package org.olat.ims.qti.qpool;

import java.io.IOException;
import java.io.OutputStream;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.ims.qti.QTIConstants;
import org.olat.ims.qti.editor.QTIEditorPackage;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.QTIDocument;

/**
 * 
 * Initial date: 12.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI12ItemEditorPackage implements QTIEditorPackage {
	
	private static final OLog log = Tracing.createLoggerFor(QTI12ItemEditorPackage.class);
	
	private Item item;
	private VFSLeaf itemLeaf;
	private final String mediaUrl;
	private final QTIDocument document;
	private final VFSContainer directory;
	
	private static OutputFormat outformat;
	static {
		outformat = OutputFormat.createPrettyPrint();
		outformat.setEncoding("UTF-8");
	}
	
	public QTI12ItemEditorPackage(Item item, QTIDocument document, String mediaUrl, VFSLeaf itemLeaf, VFSContainer directory) {
		this.item = item;
		this.document = document;
		this.directory = directory;
		this.mediaUrl = mediaUrl;
		this.itemLeaf = itemLeaf;
	}

	@Override
	public VFSContainer getBaseDir() {
		return directory;
	}

	@Override
	public String getMediaBaseURL() {
		return mediaUrl;
	}

	@Override
	public QTIDocument getQTIDocument() {
		return document;
	}

	@Override
	public void serializeQTIDocument() {
		try {
			OutputStream out = itemLeaf.getOutputStream(false);
			XMLWriter writer = new XMLWriter(out, outformat);
			DocumentFactory df = DocumentFactory.getInstance();
			Document doc = df.createDocument();
			doc.addDocType(QTIConstants.XML_DOCUMENT_ROOT, null, QTIConstants.XML_DOCUMENT_DTD);
			Element questestinteropEl = df.createElement(QTIDocument.DOCUMENT_ROOT);
			doc.setRootElement(questestinteropEl);
			item.addToElement(questestinteropEl);
			writer.write(doc);
			writer.close();
		} catch (IOException e) {
			log.error("", e);
		}
	}
}
