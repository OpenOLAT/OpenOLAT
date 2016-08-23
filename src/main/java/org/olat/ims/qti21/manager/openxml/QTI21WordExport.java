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
package org.olat.ims.qti21.manager.openxml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.openxml.HTMLToOpenXMLHandler;
import org.olat.core.util.openxml.OpenXMLDocument;
import org.olat.core.util.openxml.OpenXMLDocument.Style;
import org.olat.core.util.openxml.OpenXMLDocument.Unit;
import org.olat.core.util.openxml.OpenXMLDocumentWriter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.export.QTIWordExport;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.manager.CorrectResponsesUtil;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentHtmlBuilder;
import org.olat.ims.qti21.model.xml.AssessmentTestBuilder;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.NumericalEntry;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.TextEntry;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.TextEntryAlternative;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.editor.AssessmentTestComposerController;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.content.variable.RubricBlock;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.object.Object;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.DrawingInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.GraphicAssociateInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.GraphicOrderInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HotspotInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.PositionObjectInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.SelectPointInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleAssociableChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleMatchSet;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.MapEntry;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.SectionPart;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeCondition;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeRule;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.DirectedPairValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;

/**
 * 
 * Initial date: 04.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21WordExport implements MediaResource {
	
	private final static OLog log = Tracing.createLoggerFor(QTIWordExport.class);
	
	private String encoding;
	private ResolvedAssessmentTest resolvedAssessmentTest;
	private VFSContainer mediaContainer;
	private Locale locale;
	private final CountDownLatch latch;
	private final AssessmentHtmlBuilder htmlBuilder;
	
	public QTI21WordExport(ResolvedAssessmentTest resolvedAssessmentTest, VFSContainer mediaContainer,
			Locale locale, String encoding, CountDownLatch latch) {
		this.encoding = encoding;
		this.locale = locale;
		this.resolvedAssessmentTest = resolvedAssessmentTest;
		this.latch = latch;
		this.mediaContainer = mediaContainer;
		htmlBuilder = new AssessmentHtmlBuilder();
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

		ZipOutputStream zout = null;
		try {
			AssessmentTest assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();
			
			String label = assessmentTest.getTitle();
			String secureLabel = StringHelper.transformDisplayNameToFileSystemName(label);

			String file = secureLabel + ".zip";
			hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + StringHelper.urlEncodeUTF8(file));			
			hres.setHeader("Content-Description", StringHelper.urlEncodeUTF8(label));
			
			zout = new ZipOutputStream(hres.getOutputStream());
			zout.setLevel(9);

			ZipEntry test = new ZipEntry(secureLabel + ".docx");
			zout.putNextEntry(test);
			exportTest(assessmentTest, label, zout, false);
			zout.closeEntry();
			
			ZipEntry responses = new ZipEntry(secureLabel + "_responses.docx");
			zout.putNextEntry(responses);
			exportTest(assessmentTest, label, zout, true);
			zout.closeEntry();
		} catch (Exception e) {
			log.error("", e);
		} finally {
			latch.countDown();
			IOUtils.closeQuietly(zout);
		}
	}
	
	private void exportTest(AssessmentTest assessmentTest, String header, OutputStream out, boolean withResponses) {
		ZipOutputStream zout = null;
		try {
			OpenXMLDocument document = new OpenXMLDocument();
			document.setMediaContainer(mediaContainer);
			document.setDocumentHeader(header);
			
			Translator translator = Util.createPackageTranslator(AssessmentTestDisplayController.class, locale,
					Util.createPackageTranslator(AssessmentTestComposerController.class, locale));

			renderAssessmentTest(assessmentTest, document, translator);
			
			for(TestPart testPart:assessmentTest.getChildAbstractParts()) {
				List<AssessmentSection> assessmentSections = testPart.getAssessmentSections();
				for(AssessmentSection assessmentSection:assessmentSections) {
					renderAssessmentSection(assessmentSection, document, withResponses, translator);
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
	
	public void renderAssessmentSection(AssessmentSection assessmentSection, OpenXMLDocument document, boolean withResponses, Translator translator) {
		String title = assessmentSection.getTitle();
		document.appendHeading1(title, null);
		List<RubricBlock> rubricBlocks = assessmentSection.getRubricBlocks();
		for(RubricBlock rubricBlock:rubricBlocks) {
			String htmlRubric = htmlBuilder.blocksString(rubricBlock.getBlocks());
			document.appendHtmlText(htmlRubric, true);
		}
		
		for(SectionPart sectionPart:assessmentSection.getChildAbstractParts()) {
			if(sectionPart instanceof AssessmentSection) {
				renderAssessmentSection((AssessmentSection)sectionPart, document, withResponses, translator);
			} else if(sectionPart instanceof AssessmentItemRef) {
				AssessmentItemRef itemRef = (AssessmentItemRef)sectionPart;
				ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
				AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
				renderAssessmentItem(assessmentItem, itemRef, document, withResponses, translator);
				document.appendPageBreak();
			}
		}
	}
	
	public void renderAssessmentTest(AssessmentTest assessmentTest, OpenXMLDocument document, Translator translator) {
		String title = assessmentTest.getTitle();
		document.appendTitle(title);
		
		if(assessmentTest.getOutcomeProcessing() != null) {
			List<OutcomeRule> outcomeRules = assessmentTest.getOutcomeProcessing().getOutcomeRules();
			for(OutcomeRule outcomeRule:outcomeRules) {
				// pass rule
				if(outcomeRule instanceof OutcomeCondition) {
					OutcomeCondition outcomeCondition = (OutcomeCondition)outcomeRule;
					boolean findIf = AssessmentTestBuilder.findSetOutcomeValue(outcomeCondition.getOutcomeIf(), QTI21Constants.PASS_IDENTIFIER);
					boolean findElse = AssessmentTestBuilder.findSetOutcomeValue(outcomeCondition.getOutcomeElse(), QTI21Constants.PASS_IDENTIFIER);
					if(findIf && findElse) {
						Double cutValue = AssessmentTestBuilder.extractCutValue(outcomeCondition.getOutcomeIf());
						String cutValueLabel = translator.translate("cut.value");
						document.appendText(cutValueLabel + ": " + AssessmentHelper.getRoundedScore(cutValue), true);
					}
				}
			}
		}
	}
	
	public void renderAssessmentItem(AssessmentItem item, AssessmentItemRef itemRef, OpenXMLDocument document, boolean withResponses, Translator translator) {
		StringBuilder addText = new StringBuilder();
		
		QTI21QuestionType type = QTI21QuestionType.getType(item);
		String typeDescription = "";
		switch(type) {
			case sc: typeDescription = translator.translate("form.choice"); break;
			case mc: typeDescription = translator.translate("form.choice"); break;
			case fib: typeDescription = translator.translate("form.fib"); break;
			case numerical: typeDescription = translator.translate("form.fib"); break;
			case kprim: typeDescription = translator.translate("form.kprim"); break;
			case hotspot: typeDescription = translator.translate("form.hotspot"); break;
			case essay: typeDescription = translator.translate("form.essay"); break;
			default: typeDescription = null; break;
		}
		
		Double maxScore = QtiNodesExtractor.extractMaxScore(item);
		
		if(StringHelper.containsNonWhitespace(typeDescription) || maxScore != null) {
			if(StringHelper.containsNonWhitespace(typeDescription)) {
				addText.append("(").append(typeDescription).append(")");
			}
			if(maxScore != null) {
				addText.append(" - ").append(AssessmentHelper.getRoundedScore(maxScore));
			}
		}
		
		String title = item.getTitle();
		document.appendHeading1(title, addText.toString());
		
		URI itemUri = resolvedAssessmentTest.getSystemIdByItemRefMap().get(itemRef);
		File itemFile = new File(itemUri);
		
		List<Block> itemBodyBlocks = item.getItemBody().getBlocks();
		String html = htmlBuilder.blocksString(itemBodyBlocks);
		document.appendHtmlText(html, true, new QTI21AndHTMLToOpenXMLHandler(document, item, itemFile, withResponses));
	}
	
	private class QTI21AndHTMLToOpenXMLHandler extends HTMLToOpenXMLHandler {
		
		private final File itemFile;
		private final AssessmentItem assessmentItem;
		private final boolean withResponses;
		
		private String simpleChoiceIdentifier;
		private String responseIdentifier;
		private boolean renderElement = true;
		
		public QTI21AndHTMLToOpenXMLHandler(OpenXMLDocument document, AssessmentItem assessmentItem, File itemFile, boolean withResponses) {
			super(document);
			this.itemFile = itemFile;
			this.withResponses = withResponses;
			this.assessmentItem = assessmentItem;
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {

			String tag = localName.toLowerCase();
			switch(tag) {
				case "choiceinteraction":
					responseIdentifier = attributes.getValue("responseidentifier");
					break;
				case "simplechoice":
					if(currentTable == null) {
						startTable();
					}
					currentTable.addRowEl();
					currentTable.addCellEl(factory.createTableCell("E9EAF2", 4560, Unit.pct), 1);
					simpleChoiceIdentifier = attributes.getValue("identifier");
					break;
				case "textentryinteraction":
					startTextEntryInteraction(tag, attributes);
					break;
				case "extendedtextinteraction":
					startExtendedTextInteraction(attributes);
					break;
				case "hotspotinteraction":
					startHotspotInteraction(attributes);
					break;
				case "inlinechoiceinteraction":
				case "hottextinteraction":
				case "hottext":
					break;
				case "matchinteraction":
					renderElement = false;
					
					Interaction interaction = getInteractionByResponseIdentifier(attributes);
					if(interaction instanceof MatchInteraction) {
						MatchInteraction matchInteraction = (MatchInteraction)interaction;
						QTI21QuestionType type = QTI21QuestionType.getTypeOfMatch(assessmentItem, matchInteraction);
						if(type == QTI21QuestionType.kprim) {
							startKPrim(matchInteraction);
						}
					}
					break;
				case "gapmatchinteraction":
					break;
				case "selectpointinteraction":
					startSelectPointInteraction(attributes);
					break;
				case "graphicassociateinteraction":
					startGraphicAssociateInteraction(attributes);
					break;
				case "graphicorderinteraction":
					startGraphicOrderInteraction(attributes);
					break;
				case "graphicgapmatchinteraction":
				case "associateinteraction":
				case "uploadinteraction":
					break;
				case "positionobjectinteraction":
					startPositionObjectInteraction(attributes);
					break;
				case "sliderinteraction":
					break;
				case "drawinginteraction":
					startDrawingInteraction(attributes);
					break;
				case "simplematchset":
				case "simpleassociablechoice":
					//do nothing
					break;
				default: {
					if(renderElement) {
						super.startElement(uri, localName, qName, attributes);
					}
				}
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			if(renderElement) {
				super.characters(ch, start, length);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
			String tag = localName.toLowerCase();
			switch(tag) {
				case "choiceinteraction":
					endTable();
					break;
				case "simplechoice":
					Element checkboxCell = factory.createTableCell(null, 369, Unit.pct);
					Node checkboxNode = currentTable.addCellEl(checkboxCell, 1);
					
					boolean checked = false;
					if(withResponses) {
						Identifier identifier = Identifier.assumedLegal(simpleChoiceIdentifier);
						List<Identifier> correctAnswers = CorrectResponsesUtil
								.getCorrectIdentifierResponses(assessmentItem, Identifier.assumedLegal(responseIdentifier));
						checked = correctAnswers.contains(identifier);	
					}
					
					Node responseEl = factory.createCheckbox(checked);
					Node wrapEl = factory.wrapInParagraph(responseEl);
					checkboxNode.appendChild(wrapEl);
					closeCurrentTableRow();
					break;
				case "textentryinteraction":
					//auto closing tag
				case "extendedtextinteraction":
					//auto closing tag
				case "hotspotinteraction":
					//all work done during start
					break;
				case "matchinteraction":
					renderElement = true;
					break;
				default: {
					if(renderElement) {
						super.endElement(uri, localName, qName);
					}
				}
			}
		}
		
		private void startDrawingInteraction(Attributes attributes) {
			Interaction interaction = getInteractionByResponseIdentifier(attributes);
			if(interaction instanceof DrawingInteraction) {
				DrawingInteraction drawingInteraction = (DrawingInteraction)interaction;
				setObject(drawingInteraction.getObject());
			}
		}
		
		private void startSelectPointInteraction(Attributes attributes) {
			Interaction interaction = getInteractionByResponseIdentifier(attributes);
			if(interaction instanceof SelectPointInteraction) {
				SelectPointInteraction selectPointInteraction = (SelectPointInteraction)interaction;
				setObject(selectPointInteraction.getObject());
			}
		}
		
		private void startGraphicAssociateInteraction(Attributes attributes) {
			Interaction interaction = getInteractionByResponseIdentifier(attributes);
			if(interaction instanceof GraphicAssociateInteraction) {
				GraphicAssociateInteraction associateInteraction = (GraphicAssociateInteraction)interaction;
				setObject(associateInteraction.getObject());
			}
		}
		
		private void startGraphicOrderInteraction(Attributes attributes) {
			Interaction interaction = getInteractionByResponseIdentifier(attributes);
			if(interaction instanceof GraphicOrderInteraction) {
				GraphicOrderInteraction orderInteraction = (GraphicOrderInteraction)interaction;
				setObject(orderInteraction.getObject());
			}
		}
		
		private void startPositionObjectInteraction(Attributes attributes) {
			Interaction interaction = getInteractionByResponseIdentifier(attributes);
			if(interaction instanceof PositionObjectInteraction) {
				PositionObjectInteraction positionObject = (PositionObjectInteraction)interaction;
				setObject(positionObject.getObject());
			}
		}
		
		private void startHotspotInteraction(Attributes attributes) {
			Interaction interaction = getInteractionByResponseIdentifier(attributes);
			if(interaction instanceof HotspotInteraction) {
				HotspotInteraction hotspotInteraction = (HotspotInteraction)interaction;
				setObject(hotspotInteraction.getObject());
			}
		}
		
		private void startKPrim(MatchInteraction matchInteraction) {
			SimpleMatchSet questionMatchSet = matchInteraction.getSimpleMatchSets().get(0);

			//open a table with 3 columns
			startTable(new Integer[]{9062, 1116, 1116});

			currentTable.addRowEl();
			//draw header with +/-
			Node emptyCell = currentTable.addCellEl(factory.createTableCell(null, 9062, Unit.dxa), 1);
			emptyCell.appendChild(factory.createParagraphEl(""));
			
			Node plusCell = currentTable.addCellEl(factory.createTableCell(null, 1116, Unit.dxa), 1);
			plusCell.appendChild(factory.createParagraphEl("+"));
			Node minusCell = currentTable.addCellEl(factory.createTableCell(null, 1116, Unit.dxa), 1);
			minusCell.appendChild(factory.createParagraphEl("-"));

			currentTable.closeRow();
			
			for(SimpleAssociableChoice choice:questionMatchSet.getSimpleAssociableChoices()) {
				currentTable.addRowEl();
	
				//answer
				Node answerCell = currentTable.addCellEl(factory.createTableCell("E9EAF2", 4120, Unit.pct), 1);

				String html = htmlBuilder.flowStaticString(choice.getFlowStatics());
				String text = FilterFactory.getHtmlTagAndDescapingFilter().filter(html);
				Element responseEl = factory.createTextEl(text);
				Node wrapEl = factory.wrapInParagraph(responseEl);
				answerCell.appendChild(wrapEl);
				
				//checkbox
				boolean correct = isCorrectKPrimResponse(choice.getIdentifier(), "correct", matchInteraction);
				appendKPrimCheckBox(correct, factory);
				boolean wrong = isCorrectKPrimResponse(choice.getIdentifier(), "wrong", matchInteraction);
				appendKPrimCheckBox(wrong, factory);

				currentTable.closeRow();
			}
			
			endTable();
		}
		
		private boolean isCorrectKPrimResponse(Identifier choiceIdentifier, String id, MatchInteraction interaction) {
			if(!withResponses) return false;
			String choiceId = choiceIdentifier.toString();
			
			ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(interaction.getResponseIdentifier());
			List<MapEntry> mapEntries = responseDeclaration.getMapping().getMapEntries();
			for(MapEntry mapEntry:mapEntries) {
				SingleValue mapKey = mapEntry.getMapKey();
				if(mapKey instanceof DirectedPairValue) {
					DirectedPairValue pairValue = (DirectedPairValue)mapKey;
					String source = pairValue.sourceValue().toString();
					String destination = pairValue.destValue().toString();
					if(source.equals(choiceId) && destination.equals(id)) {
						return true;
					}
				}
			}
			return false;
		}
		
		private void appendKPrimCheckBox(boolean checked, OpenXMLDocument document) {
			Node checkboxCell = currentTable.addCellEl(document.createTableCell(null, 369, Unit.pct), 1);
			Node responseEl = document.createCheckbox(checked);
			Node wrapEl = document.wrapInParagraph(responseEl);
			checkboxCell.appendChild(wrapEl);
		}
		
		private void setObject(Object object) {
			if(object != null && StringHelper.containsNonWhitespace(object.getData())) {
				setImage(new File(itemFile.getParentFile(), object.getData()));
			}
		}
		
		private Interaction getInteractionByResponseIdentifier(Attributes attributes) {
			String identifier = attributes.getValue("responseidentifier");
			if(StringHelper.containsNonWhitespace(identifier)) {
				Identifier rIdentifier = Identifier.assumedLegal(identifier);
				List<Interaction> interactions = assessmentItem.getItemBody().findInteractions();
				for(Interaction interaction:interactions) {
					if(rIdentifier.equals(interaction.getResponseIdentifier())) {
						return interaction;
					}
				}
			}
			return null;
		}
		
		private void startExtendedTextInteraction(Attributes attributes) {
			closeParagraph();

			int expectedLines = 5;
			String length = attributes.getValue("expectedlines");
			try {
				expectedLines = Integer.parseInt(length) + 1;
			} catch (NumberFormatException e) {
				//
			}
			
			for(int i=expectedLines; i-->0; ) {
				Element paragraphEl = factory.createFillInBlanckWholeLine();
				currentParagraph = addContent(paragraphEl);
			}
		}
		
		private void startTextEntryInteraction(String tag, Attributes attributes) {
			flushText();

			Style[] styles = setTextPreferences(Style.italic);
			styleStack.add(new StyleStatus(tag, true, styles));
			pNeedNewParagraph = false;
			
			if(withResponses) {
				String response = "";
				Identifier responseId = Identifier.assumedLegal(attributes.getValue("responseidentifier"));
				ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(responseId);
				if(responseDeclaration != null) {
					if(responseDeclaration.hasBaseType(BaseType.STRING) && responseDeclaration.hasCardinality(Cardinality.SINGLE)) {
						TextEntry textEntry = new TextEntry(responseId);
						FIBAssessmentItemBuilder.extractTextEntrySettingsFromResponseDeclaration(textEntry, responseDeclaration, new AtomicInteger(), new DoubleAdder());
						
						StringBuilder sb = new StringBuilder();
						if(StringHelper.containsNonWhitespace(textEntry.getSolution())) {
							sb.append(textEntry.getSolution());
						}
						if(textEntry.getAlternatives() != null) {
							for(TextEntryAlternative alt:textEntry.getAlternatives()) {
								if(StringHelper.containsNonWhitespace(alt.getAlternative())) {
									if(sb.length() > 0) sb.append(", ");
									sb.append(alt.getAlternative());
								}
							}
						}
						response = sb.toString();
					} else if(responseDeclaration.hasBaseType(BaseType.FLOAT) && responseDeclaration.hasCardinality(Cardinality.SINGLE)) {
						NumericalEntry numericalEntry = new NumericalEntry(responseId);
						FIBAssessmentItemBuilder.extractNumericalEntrySettings(assessmentItem, numericalEntry, responseDeclaration, new AtomicInteger(), new DoubleAdder());
						if(numericalEntry.getSolution() != null) {
							response = numericalEntry.getSolution().toString();
						}
					}
				}
				characters(response.toCharArray(), 0, response.length());
			} else {
				int expectedLength = 20;
				String length = attributes.getValue("expectedlength");
				try {
					expectedLength = Integer.parseInt(length);
				} catch (NumberFormatException e) {
					//
				}
				Element blanckEl = factory.createFillInBlanck(expectedLength);
				getCurrentParagraph(false).appendChild(blanckEl);
			}
			
			flushText();
			popStyle(tag);
		}
	}
}