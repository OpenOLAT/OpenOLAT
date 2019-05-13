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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLDocument;
import org.olat.core.util.openxml.OpenXMLDocument.Style;
import org.olat.core.util.openxml.OpenXMLDocumentWriter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.ims.qti.container.qtielements.RenderInstructions;
import org.olat.ims.qti.editor.QTIEditHelper;
import org.olat.ims.qti.editor.QTIEditorMainController;
import org.olat.ims.qti.editor.beecom.objects.Assessment;
import org.olat.ims.qti.editor.beecom.objects.ChoiceQuestion;
import org.olat.ims.qti.editor.beecom.objects.EssayQuestion;
import org.olat.ims.qti.editor.beecom.objects.FIBQuestion;
import org.olat.ims.qti.editor.beecom.objects.FIBResponse;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.OutcomesProcessing;
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
	
	private static final Logger log = Tracing.createLoggerFor(QTIWordExport.class);
	
	private String encoding;
	private AssessmentNode rootNode;
	private VFSContainer mediaContainer;
	private Locale locale;
	private final CountDownLatch latch;
	
	public QTIWordExport(AssessmentNode rootNode, VFSContainer mediaContainer,
			Locale locale, String encoding, CountDownLatch latch) {
		this.encoding = encoding;
		this.locale = locale;
		this.rootNode = rootNode;
		this.latch = latch;
		this.mediaContainer = mediaContainer;
	}
	
	@Override
	public long getCacheControlDuration() {
		return 0;
	}

	@Override
	public boolean acceptRanges() {
		return false;
	}
	
	@Override
	public String getContentType() {
		return "application/zip";
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

		try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			zout.setLevel(9);
			
			String label = rootNode.getTitle();
			String secureLabel = StringHelper.transformDisplayNameToFileSystemName(label);

			String file = secureLabel + ".zip";
			hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + StringHelper.urlEncodeUTF8(file));			
			hres.setHeader("Content-Description", StringHelper.urlEncodeUTF8(label));
			

			ZipEntry test = new ZipEntry(secureLabel + ".docx");
			zout.putNextEntry(test);
			exportTest(label, zout, false);
			zout.closeEntry();
			
			ZipEntry responses = new ZipEntry(secureLabel + "_responses.docx");
			zout.putNextEntry(responses);
			exportTest(label, zout, true);
			zout.closeEntry();
		} catch (Exception e) {
			log.error("", e);
		} finally {
			latch.countDown();
		}
	}
	
	private void exportTest(String header, OutputStream out, boolean withResponses) {
		ZipOutputStream zout = null;
		try {
			OpenXMLDocument document = new OpenXMLDocument();
			document.setMediaContainer(mediaContainer);
			document.setDocumentHeader(header);
			
			Translator translator = Util.createPackageTranslator(QTIWordExport.class, locale,
					Util.createPackageTranslator(QTIEditorMainController.class, locale));

			Assessment assessment = rootNode.getAssessment();
			renderAssessment(assessment, document, translator);

			for(Section section:assessment.getSections()) {
				renderSection(section, document);
				List<Item> items = section.getItems();
				for(Iterator<Item> itemIt=items.iterator(); itemIt.hasNext(); ) {
					Item item = itemIt.next();
					if(item.isAlient()) {
						renderAlienItem(item, document, translator);
					} else {
						renderItem(item, document, withResponses, translator);
					}
					if(itemIt.hasNext()) {
						document.appendPageBreak();
					}
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

	public static void renderAlienItem(Item item, OpenXMLDocument document, Translator translator) {
		String title = item.getTitle();
		if(!StringHelper.containsNonWhitespace(title)) {
			title = item.getLabel();
		}
		document.appendHeading1(title, null);
		String notSupported = translator.translate("info.alienitem");
		document.appendText(notSupported, true, Style.bold);
	}
	
	public static void renderItem(Item item, OpenXMLDocument document, boolean withResponses, Translator translator) {
		Element el = DocumentFactory.getInstance().createElement("dummy");
		item.addToElement(el);
		Element itemEl = (Element)el.elements().get(0);
		org.olat.ims.qti.container.qtielements.Item foo
			= new org.olat.ims.qti.container.qtielements.Item(itemEl);
	
		RenderInstructions renderInstructions = new RenderInstructions();
		renderInstructions.put(RenderInstructions.KEY_STATICS_PATH, "/");
		renderInstructions.put(RenderInstructions.KEY_LOCALE, translator.getLocale());
		renderInstructions.put(RenderInstructions.KEY_RENDER_TITLE, Boolean.TRUE);
		if(item.getQuestion() != null) {

			Map<String,String> iinput = new HashMap<String,String>();
			
			String questionType = null;
			String questionScore = null;
			Question question = item.getQuestion();
			if(question instanceof ChoiceQuestion) {
				ChoiceQuestion choice = (ChoiceQuestion)question;
				if(question.getType() == Question.TYPE_SC) {
					questionType = translator.translate("item.type.sc");
					fetchPointsOfMultipleChoices(itemEl, choice, iinput);
				} else if(question.getType() == Question.TYPE_MC) {
					questionType = translator.translate("item.type.mc");
					fetchPointsOfMultipleChoices(itemEl, choice, iinput);
				} else if (question.getType() == Question.TYPE_KPRIM) {
					questionType = translator.translate("item.type.kprim");
					fetchPointsOfKPrim(itemEl, choice, iinput);
				}
				
			} else if(question instanceof FIBQuestion) {
				questionType = translator.translate("item.type.sc");
				for (Response response: question.getResponses()) {
					FIBResponse fibResponse = (FIBResponse)response;
					if("BLANK".equals(fibResponse.getType())) {
						iinput.put(fibResponse.getIdent(), fibResponse.getCorrectBlank());
					}
				}
			} else if(question instanceof EssayQuestion) {
				questionType = translator.translate("item.type.essay");
			}
			
			if(question != null && question.getMaxValue() > 0.0f) {
				questionScore = AssessmentHelper.getRoundedScore(question.getMaxValue());
				questionScore = translator.translate("item.score.long", new String[]{ questionScore });
			}

			renderInstructions.put(RenderInstructions.KEY_RENDER_CORRECT_RESPONSES, new Boolean(withResponses));
			renderInstructions.put(RenderInstructions.KEY_CORRECT_RESPONSES_MAP, iinput);
			renderInstructions.put(RenderInstructions.KEY_QUESTION_TYPE, questionType);
			renderInstructions.put(RenderInstructions.KEY_QUESTION_SCORE, questionScore);
			renderInstructions.put(RenderInstructions.KEY_QUESTION_OO_TYPE, new Integer(question.getType()));
		}
		
		foo.renderOpenXML(document, renderInstructions);
	}
	
	private static void fetchPointsOfKPrim(Element itemEl, ChoiceQuestion choice, Map<String,String> iinput) {
		Element resprocessingXML = itemEl.element("resprocessing");
		if(resprocessingXML != null) {
			List<?> respconditions = resprocessingXML.elements("respcondition");
			Map<String,Float> points = QTIEditHelper.fetchPoints(respconditions, choice.getType());
			for(Map.Entry<String,Float> entryPoint:points.entrySet()) {
				Float val = entryPoint.getValue();
				if(val != null) {
					iinput.put(entryPoint.getKey(), entryPoint.getKey());
				}
			}
		}
	}
	
	private static void fetchPointsOfMultipleChoices(Element itemEl, ChoiceQuestion choice, Map<String,String> iinput) {
		Element resprocessingXML = itemEl.element("resprocessing");
		if(resprocessingXML != null) {
			List<?> respconditions = resprocessingXML.elements("respcondition");
			Map<String,Float> points = QTIEditHelper.fetchPoints(respconditions, choice.getType());
			for(Map.Entry<String,Float> entryPoint:points.entrySet()) {
				Float val = entryPoint.getValue();
				if(val != null && val.floatValue() > 0.0f) {
					iinput.put(entryPoint.getKey(), entryPoint.getKey());
				}
			}
		}
	}
	
	public static void renderSection(Section section, OpenXMLDocument document) {
		String title = section.getTitle();
		document.appendHeading1(title, null);
		String objectives = section.getObjectives();
		document.appendHtmlText(objectives, true);
	}
	
	public static void renderAssessment(Assessment assessment, OpenXMLDocument document, Translator translator) {
		String title = assessment.getTitle();
		document.appendTitle(title);
		
		OutcomesProcessing outcomesProcessing = assessment.getOutcomes_processing();
		if (outcomesProcessing != null) {
			String cutValue = outcomesProcessing.getField(OutcomesProcessing.CUTVALUE);
			String cutValueLabel = translator.translate("cut_value");
			document.appendText(cutValueLabel + ": " + cutValue, true);
		}	

		String objectives = assessment.getObjectives();
		document.appendText(objectives, true);
	}
}
