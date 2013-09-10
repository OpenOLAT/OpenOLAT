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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
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
import org.olat.ims.qti.editor.QTIEditHelper;
import org.olat.ims.qti.editor.beecom.objects.Assessment;
import org.olat.ims.qti.editor.beecom.objects.ChoiceQuestion;
import org.olat.ims.qti.editor.beecom.objects.FIBQuestion;
import org.olat.ims.qti.editor.beecom.objects.FIBResponse;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.Question;
import org.olat.ims.qti.editor.beecom.objects.Response;
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
		return "application/zip";
		//return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
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

		ZipOutputStream zout = null;
		try {
			String label = rootNode.getTitle();
			String secureLabel = StringHelper.transformDisplayNameToFileSystemName(label);

			String file = secureLabel + ".zip";
			hres.setHeader("Content-Disposition","attachment; filename=\"" + StringHelper.urlEncodeISO88591(file) + "\"");			
			hres.setHeader("Content-Description",StringHelper.urlEncodeISO88591(label));
			
			zout = new ZipOutputStream(hres.getOutputStream());
			zout.setLevel(9);

			ZipEntry test = new ZipEntry(secureLabel + ".docx");
			zout.putNextEntry(test);
			exportTest(zout, false);
			zout.closeEntry();
			
			ZipEntry responses = new ZipEntry(secureLabel + "_responses.docx");
			zout.putNextEntry(responses);
			exportTest(zout, true);
			zout.closeEntry();
		} catch (Exception e) {
			log.error("", e);
		} finally {
			IOUtils.closeQuietly(zout);
		}
	}
	
	private void exportTest(OutputStream out, boolean withResponses) {
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
					renderItem(item, document, mediaBaseUrl, withResponses);
					document.appendPageBreak();
				}
			}
			
			zout = new ZipOutputStream(out);
			zout.setLevel(9);
			
			OpenXMLDocumentWriter writer = new OpenXMLDocumentWriter();
			writer.createDocument(zout, document);
		} catch (Exception e) {
			log.error("", e);
		} finally {
			if(zout != null) {
				try {
					zout.finish();
				} catch (IOException e) {
					log.error("", e);
				}
			}
		}
	}
	
	private void renderItem(Item item, OpenXMLDocument document, String mediaBaseUrl, boolean withResponses) {
		Element el = DocumentFactory.getInstance().createElement("dummy");
		item.addToElement(el);
		Element itemEl = (Element)el.elements().get(0);
		org.olat.ims.qti.container.qtielements.Item foo
			= new org.olat.ims.qti.container.qtielements.Item(itemEl);
	
		RenderInstructions renderInstructions = new RenderInstructions();
		renderInstructions.put(RenderInstructions.KEY_STATICS_PATH, mediaBaseUrl + "/");
		renderInstructions.put(RenderInstructions.KEY_LOCALE, locale);
		renderInstructions.put(RenderInstructions.KEY_RENDER_TITLE, Boolean.TRUE);
		if(withResponses) {
			
			Map<String,String> iinput = new HashMap<String,String>();
			
			Question question = item.getQuestion();
			if(question instanceof ChoiceQuestion) {
				ChoiceQuestion choice = (ChoiceQuestion)question;
				Element resprocessingXML = itemEl.element("resprocessing");
				if(resprocessingXML != null) {
					List<?> respconditions = resprocessingXML.elements("respcondition");
					Map<String,Float> points = QTIEditHelper.fetchPoints(respconditions, choice.getType());
					for(String point:points.keySet()) {
						iinput.put(point, point);
					}
				}
			} else if(question instanceof FIBQuestion) {
				for (Response response: question.getResponses()) {
					FIBResponse fibResponse = (FIBResponse)response;
					if("BLANK".equals(fibResponse.getType())) {
						iinput.put(fibResponse.getIdent(), fibResponse.getCorrectBlank());
					}
				}
			}

			renderInstructions.put(RenderInstructions.KEY_RENDER_CORRECT_RESPONSES, Boolean.TRUE);
			renderInstructions.put(RenderInstructions.KEY_CORRECT_RESPONSES_MAP, iinput);
		}
		
		foo.renderOpenXML(document, renderInstructions);
	}
	

	
	private void renderSection(Section section, OpenXMLDocument document) {
		String title = section.getTitle();
		document.appendHeading1(title);
		String objectives = section.getObjectives();
		document.appendText(objectives, true);
	}
	
	private void renderAssessment(Assessment assessment, OpenXMLDocument document) {
		String title = assessment.getTitle();
		document.appendTitle(title);
		String objectives = assessment.getObjectives();
		document.appendText(objectives, true);
	}
}
