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
package org.olat.ims.qti.export;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.OpenXMLDocument;
import org.olat.core.util.openxml.OpenXMLDocumentWriter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti.container.qtielements.RenderInstructions;
import org.olat.ims.qti.editor.beecom.objects.Assessment;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.Section;
import org.olat.ims.qti.editor.tree.AssessmentNode;

/**
 * 
 * Initial date: 02.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTIWordExport implements MediaResource {
	
	private final static OLog log = Tracing.createLoggerFor(QTIWordExport.class);
	
	private String encoding;
	private AssessmentNode rootNode;
	private VFSContainer mediaContainer;
	private Locale locale;
	
	public QTIWordExport(AssessmentNode rootNode, VFSContainer mediaContainer, Locale locale, String encoding) {
		this.encoding = encoding;
		this.locale = locale;
		this.rootNode = rootNode;
		this.mediaContainer = mediaContainer;
	}
	
	@Override
	public String getContentType() {
		return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	}

	@Override
	public Long getSize() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public void release() {
		//
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		try {
			hres.setCharacterEncoding(encoding);
		} catch (Exception e) {
			log.error("", e);
		}
		
		String label = rootNode.getTitle();
		String file = StringHelper.transformDisplayNameToFileSystemName(label) + ".docx";
		hres.setHeader("Content-Disposition","attachment; filename=\"" + StringHelper.urlEncodeISO88591(file) + "\"");			
		hres.setHeader("Content-Description",StringHelper.urlEncodeISO88591(label));
		
		String mediaBaseUrl = "";
		ZipOutputStream zout = null;
		try {
			OpenXMLDocument document = new OpenXMLDocument();
			document.setMediaContainer(mediaContainer);

			Assessment assessment = rootNode.getAssessment();
			renderAssessment(assessment, document);

			for(Section section:assessment.getSections()) {
				renderSection(section, document);
				List<Item> items = section.getItems();
				for(Item item:items) {
					renderItem(item, document, mediaBaseUrl);
					document.appendPageBreak();
				}
			}
			
			zout = new ZipOutputStream(hres.getOutputStream());
			zout.setLevel(9);
			
			OpenXMLDocumentWriter writer = new OpenXMLDocumentWriter();
			writer.createDocument(zout, document);
		} catch (Exception e) {
			log.error("", e);
		} finally {
			IOUtils.closeQuietly(zout);
		}
	}
	
	private void renderItem(Item item, OpenXMLDocument document, String mediaBaseUrl) {
		Element el = DocumentFactory.getInstance().createElement("dummy");
		item.addToElement(el);
		org.olat.ims.qti.container.qtielements.Item foo
			= new org.olat.ims.qti.container.qtielements.Item((Element)el.elements().get(0));
		
		RenderInstructions renderInstructions = new RenderInstructions();
		renderInstructions.put(RenderInstructions.KEY_STATICS_PATH, mediaBaseUrl + "/");
		renderInstructions.put(RenderInstructions.KEY_LOCALE, locale);
		renderInstructions.put(RenderInstructions.KEY_RENDER_TITLE, Boolean.TRUE);
		
		foo.renderOpenXML(document, renderInstructions);
	}
	
	private void renderSection(Section section, OpenXMLDocument document) {
		String title = section.getTitle();
		document.appendHeading1(title);
		String objectives = section.getObjectives();
		document.appendTextParagraph(objectives);
	}
	
	private void renderAssessment(Assessment assessment, OpenXMLDocument document) {
		String title = assessment.getTitle();
		document.appendTitle(title);
		String objectives = assessment.getObjectives();
		document.appendTextParagraph(objectives);
	}
}
