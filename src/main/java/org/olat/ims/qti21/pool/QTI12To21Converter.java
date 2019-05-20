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
package org.olat.ims.qti21.pool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.DoubleAdder;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.nodes.iq.QTIResourceTypeModule;
import org.olat.ims.qti.QTIModule;
import org.olat.ims.qti.editor.QTIEditHelper;
import org.olat.ims.qti.editor.QTIEditorPackage;
import org.olat.ims.qti.editor.beecom.objects.Assessment;
import org.olat.ims.qti.editor.beecom.objects.ChoiceQuestion;
import org.olat.ims.qti.editor.beecom.objects.ChoiceResponse;
import org.olat.ims.qti.editor.beecom.objects.Control;
import org.olat.ims.qti.editor.beecom.objects.Duration;
import org.olat.ims.qti.editor.beecom.objects.EssayQuestion;
import org.olat.ims.qti.editor.beecom.objects.EssayResponse;
import org.olat.ims.qti.editor.beecom.objects.FIBResponse;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.Material;
import org.olat.ims.qti.editor.beecom.objects.OutcomesProcessing;
import org.olat.ims.qti.editor.beecom.objects.QTIDocument;
import org.olat.ims.qti.editor.beecom.objects.Question;
import org.olat.ims.qti.editor.beecom.objects.Response;
import org.olat.ims.qti.editor.beecom.objects.Section;
import org.olat.ims.qti.editor.beecom.objects.SelectionOrdering;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.ims.qti.qpool.QTI12HtmlHandler;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.xml.AssessmentHtmlBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.olat.ims.qti21.model.xml.AssessmentTestBuilder;
import org.olat.ims.qti21.model.xml.AssessmentTestFactory;
import org.olat.ims.qti21.model.xml.ManifestBuilder;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;
import org.olat.ims.qti21.model.xml.ModalFeedbackBuilder;
import org.olat.ims.qti21.model.xml.ModalFeedbackBuilder.ModalFeedbackType;
import org.olat.ims.qti21.model.xml.ModalFeedbackCondition;
import org.olat.ims.qti21.model.xml.ModalFeedbackCondition.Operator;
import org.olat.ims.qti21.model.xml.ModalFeedbackCondition.Variable;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.model.xml.interactions.EssayAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.EntryType;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.TextEntry;
import org.olat.ims.qti21.model.xml.interactions.KPrimAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.MultipleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;
import org.olat.ims.qti21.model.xml.interactions.SingleChoiceAssessmentItemBuilder;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.manager.QItemTypeDAO;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.resource.OLATResource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import uk.ac.ed.ph.jqtiplus.node.AssessmentObject;
import uk.ac.ed.ph.jqtiplus.node.content.variable.RubricBlock;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.P;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleAssociableChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.ControlObject;
import uk.ac.ed.ph.jqtiplus.node.test.ItemSessionControl;
import uk.ac.ed.ph.jqtiplus.node.test.Selection;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.node.test.TimeLimits;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.Orientation;

/**
 * 
 * Initial date: 19.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI12To21Converter {
	
	private static final Logger log = Tracing.createLoggerFor(QTI12To21Converter.class);
	
	private final Locale locale;
	private final File unzippedDirRoot;
	private final QtiSerializer qtiSerializer = new QtiSerializer(null);
	private final AssessmentHtmlBuilder htmlBuilder = new AssessmentHtmlBuilder(qtiSerializer);
	
	private final ManifestBuilder manifest;
	private List<String> materialPath = new ArrayList<>();
	private Map<String,String> materialMappings = new HashMap<>();
	private List<String> errors = new ArrayList<>();
	private final DoubleAdder atomicMaxScore = new DoubleAdder();
	
	public QTI12To21Converter(File unzippedDirRoot, Locale locale) {
		this.locale = locale;
		this.unzippedDirRoot = unzippedDirRoot;
		manifest = ManifestBuilder.createAssessmentTestBuilder();
	}
	
	public AssessmentTest convert(QTIEditorPackage qtiEditorPackage, QTI21DeliveryOptions qti21Options)
	throws URISyntaxException {
		return convert(qtiEditorPackage.getBaseDir(), qtiEditorPackage.getQTIDocument(), qti21Options);
	}
	
	public AssessmentTest convert(VFSContainer originalContainer, QTIDocument doc, QTI21DeliveryOptions qti21Options)
	throws URISyntaxException {
		Assessment assessment = doc.getAssessment();

		AssessmentTest assessmentTest = new AssessmentTest();
		String assessmentTestIdentifier = IdentifierGenerator.newAssessmentTestFilename();
		File testFile = new File(unzippedDirRoot, assessmentTestIdentifier + ".xml");
		manifest.appendAssessmentTest(testFile.getName());
		
		assessmentTest.setIdentifier(assessmentTestIdentifier);
		assessmentTest.setTitle(assessment.getTitle());
		assessmentTest.setToolName(QTI21Constants.TOOLNAME);
		assessmentTest.setToolVersion(Settings.getVersion());
		convertDuration((Duration)assessment.getDuration(), assessmentTest);
		
		TestPart testPart = AssessmentTestFactory.createTestPart(assessmentTest);
		ItemSessionControl itemSessionControl = testPart.getItemSessionControl();
		Control tmpControl = QTIEditHelper.getControl(assessment);
		if(tmpControl.getSolution() == Control.CTRL_YES) {
			itemSessionControl.setShowSolution(Boolean.TRUE);
		}
		
		if(qti21Options != null) {
			qti21Options.setHideFeedbacks(false);
			
			if(assessment.isInheritControls() && tmpControl.getFeedback() != Control.CTRL_YES) {
				qti21Options.setHideFeedbacks(true);
			}
		}

		AssessmentTestBuilder assessmentTestBuilder = new AssessmentTestBuilder(assessmentTest);

		//root
		List<Section> sections = assessment.getSections();
		for(Section section:sections) {
			convert(section, testPart);
		}
		
		//this are lost in QTI 2.1
		//assessment.getSelection_ordering().getOrderType();
		//assessment.getSelection_ordering().getSelectionNumber();
		OutcomesProcessing outcomesProcessing = assessment.getOutcomes_processing();
		if(outcomesProcessing != null) {
			String cutValue = outcomesProcessing.getField(OutcomesProcessing.CUTVALUE);
			if(StringHelper.containsNonWhitespace(cutValue)) {
				try {
					assessmentTestBuilder.setCutValue(Double.valueOf(cutValue));
				} catch (NumberFormatException e) {
					log.error("Cannot parse cut value: " + cutValue, e);
				}
			}
		}
		
		assessmentTestBuilder.setMaxScore(atomicMaxScore.doubleValue());

		assessmentTest = assessmentTestBuilder.build();
		persistAssessmentObject(testFile, assessmentTest);
		manifest.write(new File(unzippedDirRoot, "imsmanifest.xml"));
		copyMaterial(originalContainer);
		return assessmentTest;
	}
	
	private void convert(Section section, TestPart testPart)
	throws URISyntaxException {
		AssessmentSection assessmentSection = AssessmentTestFactory.appendAssessmentSection("Section", testPart);
		assessmentSection.setTitle(section.getTitle());
		convertDuration(section.getDuration(), assessmentSection);
		
		RubricBlock rubricBlock = assessmentSection.getRubricBlocks().get(0);
		rubricBlock.getBlocks().clear();
		String objectives = section.getObjectives();
		htmlBuilder.appendHtml(rubricBlock, blockedHtml(objectives));

		boolean shuffle = SelectionOrdering.RANDOM.equals(section.getSelection_ordering().getOrderType());
		assessmentSection.getOrdering().setShuffle(shuffle);
		
		int selectionNum = section.getSelection_ordering().getSelectionNumber();
		if(selectionNum > 0) {
			Selection selection = new Selection(assessmentSection);
			selection.setSelect(selectionNum);
			assessmentSection.setSelection(selection);
		}
		
		List<Item> items = section.getItems();
		for(Item item:items) {
			AssessmentItemBuilder itemBuilder = null;
			if(item != null && item.getQuestion() != null) {
				int questionType = item.getQuestion().getType();
				switch (questionType) {
					case Question.TYPE_SC:
						itemBuilder = convertSingleChoice(item);
						break;
					case Question.TYPE_MC:
						itemBuilder = convertMultipleChoice(item);
						break;
					case Question.TYPE_KPRIM:
						itemBuilder = convertKPrim(item);
						break;
					case Question.TYPE_FIB:
						itemBuilder = convertFIB(item);
						break;
					case Question.TYPE_ESSAY:
						itemBuilder = convertEssay(item);
						break;
				}
			} else {
				errors.add(item.getTitle());
				log.error("Item without question: " + item);
			}

			if(itemBuilder != null) {
				itemBuilder.build();
				
				AssessmentItem assessmentItem = itemBuilder.getAssessmentItem();
				
				AssessmentItemRef itemRef = new AssessmentItemRef(assessmentSection);
				String itemId = IdentifierGenerator.newAsString(itemBuilder.getQuestionType().getPrefix());
				itemRef.setIdentifier(Identifier.parseString(itemId));
				convertItemBasics(item, itemRef);
				File itemFile = new File(unzippedDirRoot, itemId + ".xml");
				itemRef.setHref(new URI(itemFile.getName()));
				assessmentSection.getSectionParts().add(itemRef);
				persistAssessmentObject(itemFile, assessmentItem);
				appendResourceAndMetadata(item, itemBuilder, itemFile);
				
				//collect max score
				Double maxScore = QtiNodesExtractor.extractMaxScore(assessmentItem);
				if(maxScore != null && maxScore.doubleValue() > 0.0d) {
					atomicMaxScore.add(maxScore.doubleValue());
				}
			}
		}
	}
	
	public boolean convert(QuestionItemImpl convertedItem, Item item, VFSContainer originalContainer) {
		if(convertItem(convertedItem, item)) {
			copyMaterial(originalContainer);
			manifest.appendAssessmentItem(convertedItem.getRootFilename());
			manifest.write(new File(unzippedDirRoot, "imsmanifest.xml"));
			return true;
		}
		return false;
	}
	
	private void copyMaterial(VFSContainer originalContainer) {
		Set<String> materialSet = new HashSet<>(materialPath);
		for(String material:materialSet) {
			if(StringHelper.containsNonWhitespace(material)
					&& !material.startsWith("http://") && !material.startsWith("https://")) {
				VFSItem materialItem = originalContainer.resolve(material);
				if(materialItem instanceof VFSLeaf) {
					try(InputStream in = ((VFSLeaf) materialItem).getInputStream()) {
						if(materialMappings.containsKey(material)) {
							material = materialMappings.get(material);
						}
						File dest = new File(unzippedDirRoot, material);
						FileUtils.copyToFile(in, dest, "");
					} catch(Exception e) {
						log.error("Cannot copy: " + material, e);
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param item
	 * @return The name of the assesssmentItem file
	 */
	private boolean convertItem(QuestionItemImpl convertedQuestion, Item item) {
		QItemTypeDAO qItemTypeDao = CoreSpringFactory.getImpl(QItemTypeDAO.class);
		
		AssessmentItemBuilder itemBuilder = null;
		int questionType = item.getQuestion().getType();
		switch (questionType) {
			case Question.TYPE_SC:
				itemBuilder = convertSingleChoice(item);
				convertedQuestion.setType(qItemTypeDao.loadByType(QuestionType.SC.name()));
				break;
			case Question.TYPE_MC:
				itemBuilder = convertMultipleChoice(item);
				convertedQuestion.setType(qItemTypeDao.loadByType(QuestionType.MC.name()));
				break;
			case Question.TYPE_KPRIM:
				itemBuilder = convertKPrim(item);
				convertedQuestion.setType(qItemTypeDao.loadByType(QuestionType.KPRIM.name()));
				break;
			case Question.TYPE_FIB:
				itemBuilder = convertFIB(item);
				convertedQuestion.setType(qItemTypeDao.loadByType(QuestionType.FIB.name()));
				break;
			case Question.TYPE_ESSAY:
				itemBuilder = convertEssay(item);
				convertedQuestion.setType(qItemTypeDao.loadByType(QuestionType.ESSAY.name()));
				break;
		}

		if(itemBuilder != null) {
			itemBuilder.build();

			AssessmentItem assessmentItem = itemBuilder.getAssessmentItem();
			String itemId = IdentifierGenerator.newAsString(itemBuilder.getQuestionType().getPrefix());
			File itemFile = new File(unzippedDirRoot, itemId + ".xml");
			persistAssessmentObject(itemFile, assessmentItem);
			appendResourceAndMetadata(item, itemBuilder, itemFile);
			convertedQuestion.setRootFilename(itemFile.getName());
			return true;
		}
		return false;
	}
	
	private void convertItemBasics(Item item, AssessmentItemRef itemRef) {
		if(item.getMaxattempts() > 0) {
			ItemSessionControl itemSessionControl = itemRef.getItemSessionControl();
			if(itemSessionControl == null) {
				itemSessionControl = new ItemSessionControl(itemRef);
				itemRef.setItemSessionControl(itemSessionControl);
			}
			
			itemSessionControl.setMaxAttempts(item.getMaxattempts());
		}
		if(item.getDuration() != null && item.getDuration().isSet()) {
			TimeLimits timeLimits = itemRef.getTimeLimits();
			if(timeLimits == null) {
				timeLimits = new TimeLimits(itemRef);
				itemRef.setTimeLimits(timeLimits);
			}
			
			timeLimits.setMinimum(0.0d);
			
			double max = 0.0d;
			if(item.getDuration().getMin() > 0) {
				max += item.getDuration().getMin() * 60d;
			}
			if(item.getDuration().getSec() > 0) {
				max += item.getDuration().getSec();
			}
			timeLimits.setMaximum(max);
		}
	}
	
	private void appendResourceAndMetadata(Item item, AssessmentItemBuilder itemBuilder, File itemFile) {
		manifest.appendAssessmentItem(itemFile.getName());
		ManifestMetadataBuilder metadata = manifest.getResourceBuilderByHref(itemFile.getName());
		metadata.setTechnicalFormat(ManifestBuilder.ASSESSMENTITEM_MIMETYPE);
		metadata.setQtiMetadataInteractionTypes(itemBuilder.getInteractionNames());
		metadata.setOpenOLATMetadataQuestionType(itemBuilder.getQuestionType().getPrefix());
		metadata.setTitle(item.getTitle(), locale.getLanguage());
		metadata.setDescription(item.getObjectives(), locale.getLanguage());
	}
	
	public boolean persistAssessmentObject(File resourceFile, AssessmentObject assessmentObject) {
		try(FileOutputStream out = new FileOutputStream(resourceFile)) {
			qtiSerializer.serializeJqtiObject(assessmentObject, out);
			return true;
		} catch(Exception e) {
			log.error("", e);
			return false;
		}
	}
	
	private AssessmentItemBuilder convertSingleChoice(Item item) {
		SingleChoiceAssessmentItemBuilder itemBuilder = new SingleChoiceAssessmentItemBuilder("Single choice", "New answer", qtiSerializer);
		convertItemBasics(item, itemBuilder);
		itemBuilder.clearMapping();
		itemBuilder.clearSimpleChoices();
		itemBuilder.setScoreEvaluationMode(ScoreEvaluation.allCorrectAnswers);
		
		ChoiceInteraction interaction = itemBuilder.getChoiceInteraction();
		
		Question question = item.getQuestion();
		itemBuilder.setShuffle(question.isShuffle());
		convertOrientation(question, itemBuilder);
		
		List<Response> responses = question.getResponses();
		Map<String,Identifier> identToIdentifier = new HashMap<>();
		for(Response response:responses) {
			String responseText = response.getContent().renderAsHtmlForEditor();
			responseText = blockedHtml(responseText);
			SimpleChoice newChoice;
			if(StringHelper.isHtml(responseText)) {
				newChoice = AssessmentItemFactory
						.createSimpleChoice(interaction, "", itemBuilder.getQuestionType().getPrefix());
				htmlBuilder.appendHtml(newChoice, responseText);
			} else {
				newChoice = AssessmentItemFactory
					.createSimpleChoice(interaction, responseText, itemBuilder.getQuestionType().getPrefix());
			}
			itemBuilder.addSimpleChoice(newChoice);
			identToIdentifier.put(response.getIdent(), newChoice.getIdentifier());
			
			if(response.isCorrect()) {
				itemBuilder.setCorrectAnswer(newChoice.getIdentifier());
			}	
		}
		
		convertFeedbackPerAnswers(item, itemBuilder, identToIdentifier);
		
		double correctScore = question.getSingleCorrectScore();
		if(correctScore >= 0.0d) {
			itemBuilder.setMinScore(0.0d);
			itemBuilder.setMaxScore(correctScore);
		}
		
		return itemBuilder;
	}
	
	private AssessmentItemBuilder convertMultipleChoice(Item item) {
		MultipleChoiceAssessmentItemBuilder itemBuilder = new MultipleChoiceAssessmentItemBuilder("Multiple choice", "New answer", qtiSerializer);
		convertItemBasics(item, itemBuilder);
		itemBuilder.clearMapping();
		itemBuilder.clearSimpleChoices();
		
		ChoiceInteraction interaction = itemBuilder.getChoiceInteraction();
		
		Question question = item.getQuestion();
		itemBuilder.setShuffle(question.isShuffle());
		convertOrientation(question, itemBuilder);
		
		boolean hasNegative = false;
		List<Response> responses = question.getResponses();
		for(Response response:responses) {
			if(response.getPoints() < 0.0f) {
				hasNegative = true;
			}
		}
		
		boolean singleCorrect = question.isSingleCorrect();
		Map<String, Identifier> identToIdentifier = new HashMap<>();
		for(Response response:responses) {
			String responseText = response.getContent().renderAsHtmlForEditor();
			responseText = blockedHtml(responseText);

			SimpleChoice newChoice;
			if(StringHelper.isHtml(responseText)) {
				newChoice = AssessmentItemFactory
						.createSimpleChoice(interaction, "", itemBuilder.getQuestionType().getPrefix());
				htmlBuilder.appendHtml(newChoice, responseText);
			} else {
				newChoice = AssessmentItemFactory
					.createSimpleChoice(interaction, responseText, itemBuilder.getQuestionType().getPrefix());
			}
			
			itemBuilder.addSimpleChoice(newChoice);
			identToIdentifier.put(response.getIdent(), newChoice.getIdentifier());
			
			double score = response.getPoints();
			if(singleCorrect) {
				if(response.isCorrect()) {
					itemBuilder.addCorrectAnswer(newChoice.getIdentifier());
				}
				if(score > 0.0f) {
					itemBuilder.setMaxScore(score);
				}
			} else {
				if((hasNegative && response.getPoints() >= 0.0f) || (!hasNegative && response.getPoints() > 0.0f)) {
					itemBuilder.addCorrectAnswer(newChoice.getIdentifier());
				}
				itemBuilder.setMapping(newChoice.getIdentifier(), score);
			}
		}

		convertFeedbackPerAnswers(item, itemBuilder, identToIdentifier);

		if(singleCorrect) {
			itemBuilder.setScoreEvaluationMode(ScoreEvaluation.allCorrectAnswers);
		} else {
			itemBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
			if(question instanceof ChoiceQuestion) {
				ChoiceQuestion choice = (ChoiceQuestion)question;
				itemBuilder.setMinScore(new Double(choice.getMinValue()));
				itemBuilder.setMaxScore(new Double(choice.getMaxValue()));
			}
		}
		
		return itemBuilder;
	}
	
	private void convertOrientation(Question question, SimpleChoiceAssessmentItemBuilder itemBuilder) {
		if (question instanceof ChoiceQuestion) {
			String flowLabel = ((ChoiceQuestion)question).getFlowLabelClass();
			if(StringHelper.containsNonWhitespace(flowLabel)) {
				if(ChoiceQuestion.BLOCK.equals(flowLabel)) {
					itemBuilder.setOrientation(Orientation.HORIZONTAL);
				} else if(ChoiceQuestion.LIST.equals(flowLabel)) {
					itemBuilder.setOrientation(Orientation.VERTICAL);
				}
			}
		}
	}
	
	private AssessmentItemBuilder convertKPrim(Item item) {
		KPrimAssessmentItemBuilder itemBuilder = new KPrimAssessmentItemBuilder("Kprim", "New answer", qtiSerializer);
		convertItemBasics(item, itemBuilder);
		
		Question question = item.getQuestion();
		itemBuilder.setShuffle(question.isShuffle());
		
		List<Response> responses = question.getResponses();
		List<SimpleAssociableChoice> choices = itemBuilder.getKprimChoices();
		for(int i=0; i<4; i++) {
			Response response = responses.get(i);
			SimpleAssociableChoice choice = choices.get(i);
			
			String answer = response.getContent().renderAsHtmlForEditor();
			answer = blockedHtml(answer);
			if(StringHelper.isHtml(answer)) {
				htmlBuilder.appendHtml(choice, answer);
			} else {
				P firstChoiceText = AssessmentItemFactory.getParagraph(choice, answer);
				choice.getFlowStatics().clear();
				choice.getFlowStatics().add(firstChoiceText);
			}
			
			if(response.isCorrect()) {
				itemBuilder.setAssociation(choice.getIdentifier(), QTI21Constants.CORRECT_IDENTIFIER);
			} else {
				itemBuilder.setAssociation(choice.getIdentifier(), QTI21Constants.WRONG_IDENTIFIER);	
			}
		}
		
		double score = question.getMaxValue();
		itemBuilder.setMinScore(0.0d);
		itemBuilder.setMaxScore(score);
		return itemBuilder;
	}
	
	private AssessmentItemBuilder convertFIB(Item item) {
		FIBAssessmentItemBuilder itemBuilder = new FIBAssessmentItemBuilder("Gap text", EntryType.text, qtiSerializer);
		itemBuilder.setQuestion("");
		itemBuilder.clearTextEntries();
		convertItemBasics(item, itemBuilder);
		
		Question question = item.getQuestion();
		boolean singleCorrect = question.isSingleCorrect();
		if(singleCorrect) {
			itemBuilder.setScoreEvaluationMode(ScoreEvaluation.allCorrectAnswers);
		} else {
			itemBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
		}
		itemBuilder.getMinScoreBuilder().setScore(new Double(question.getMinValue()));
		itemBuilder.getMaxScoreBuilder().setScore(new Double(question.getMaxValue()));

		List<Response> responses = question.getResponses();
		StringBuilder sb = new StringBuilder();
		for(Response response:responses) {
			if(response instanceof FIBResponse) {
				FIBResponse gap = (FIBResponse)response;
				if(FIBResponse.TYPE_BLANK.equals(gap.getType())) {
					String responseId = itemBuilder.generateResponseIdentifier();
					
					StringBuilder entryString = new StringBuilder();
					entryString.append(" <textentryinteraction responseidentifier=\"").append(responseId).append("\"");
					
					TextEntry entry = itemBuilder.createTextEntry(responseId);
					entry.setCaseSensitive("Yes".equals(gap.getCaseSensitive()));
					if(gap.getMaxLength() > 0) {
						entry.setExpectedLength(gap.getMaxLength());
						entryString.append(" expectedlength=\"").append(gap.getMaxLength()).append("\"");
					} else if(gap.getSize() > 0) {
						entry.setExpectedLength(gap.getSize());
						entryString.append(" expectedlength=\"").append(gap.getSize()).append("\"");
					}
					parseAlternatives(gap.getCorrectBlank(), gap.getPoints(), entry);
					entryString.append("></textentryinteraction>");
					sb.append(entryString);
				} else if(FIBResponse.TYPE_CONTENT.equals(gap.getType())) {
					Material text = gap.getContent();
					String htmltext = text.renderAsHtmlForEditor();
					htmltext = blockedHtml(htmltext);
					sb.append(htmltext);
				}
			}
		}
		
		String fib = "<div>" + sb.toString() + "</div>";
		itemBuilder.setQuestion(fib);
		return itemBuilder;
	}
	
	private void parseAlternatives(String value, double score, TextEntry textEntry) {
		String[] values = value.split(";");
		if(values.length > 0) {
			textEntry.setSolution(values[0]);
			textEntry.setScore(score);
		}
		if(values.length > 1) {
			for(int i=1; i<values.length; i++) {
				textEntry.addAlternative(values[i], score);
			}
		}
	}
	
	private AssessmentItemBuilder convertEssay(Item item) {
		EssayAssessmentItemBuilder itemBuilder = new EssayAssessmentItemBuilder("Essay", qtiSerializer);
		convertItemBasics(item, itemBuilder);
		
		EssayQuestion question = (EssayQuestion)item.getQuestion();
		EssayResponse response = question.getEssayResponse();
		int cols = response.getColumns();
		int rows = response.getRows();
		itemBuilder.setExpectedLength(cols * rows);
		itemBuilder.setExpectedLines(rows);

		double score = question.getMaxValue();
		itemBuilder.setMinScore(0.0d);
		itemBuilder.setMaxScore(score);

		return itemBuilder;
	}
	
	private void convertItemBasics(Item item, AssessmentItemBuilder itemBuilder) {
		AssessmentItem assessmentItem = itemBuilder.getAssessmentItem();
		if(StringHelper.containsNonWhitespace(item.getTitle())) {
			assessmentItem.setTitle(item.getTitle());
		}
		if(StringHelper.containsNonWhitespace(item.getLabel())) {
			assessmentItem.setLabel(item.getLabel());
		}
		
		Question question = item.getQuestion();
		String questionText = question.getQuestion().renderAsHtmlForEditor();
		questionText = blockedHtml(questionText);
		if(StringHelper.isHtml(questionText)) {
			itemBuilder.setQuestion(questionText);
		} else {
			itemBuilder.setQuestion("<p>" + questionText + "</p>");
		}
		
		String hintText = question.getHintText();
		if(StringHelper.containsNonWhitespace(hintText)) {
			ModalFeedbackBuilder hint = itemBuilder.createHint();
			Translator translator = Util.createPackageTranslator(QTIModule.class, locale);
			hint.setTitle(translator.translate("render.hint"));
			hint.setText(hintText);
		}
		
		String solutionText = question.getSolutionText();
		if(StringHelper.containsNonWhitespace(solutionText)) {
			ModalFeedbackBuilder solution = itemBuilder.createCorrectSolutionFeedback();
			solutionText = blockedHtml(solutionText);
			solution.setText(solutionText);
		}
		
		String feedbackMastery = QTIEditHelper.getFeedbackMasteryText(item);
		if(StringHelper.containsNonWhitespace(feedbackMastery)) {
			ModalFeedbackBuilder feedback = itemBuilder.createCorrectFeedback();
			feedbackMastery = blockedHtml(feedbackMastery);
			feedback.setText(feedbackMastery);
		}

		String feedbackFail = QTIEditHelper.getFeedbackFailText(item);
		if(StringHelper.containsNonWhitespace(feedbackFail)) {
			ModalFeedbackBuilder feedback = itemBuilder.createIncorrectFeedback();
			feedbackFail = blockedHtml(feedbackFail);
			feedback.setText(feedbackFail);
		}
		

	}
	
	private void convertFeedbackPerAnswers(Item item, AssessmentItemBuilder itemBuilder, Map<String,Identifier> identToIdentifier) {
		Question question = item.getQuestion();
		
		List<ModalFeedbackBuilder> additionalFeedbacks = new ArrayList<>();
		for (Response response : question.getResponses()) {
			if(response instanceof ChoiceResponse) {
				Material responseFeedbackMat = QTIEditHelper.getFeedbackOlatRespMaterial(item, response.getIdent());
				if(responseFeedbackMat != null) {
					String feedbackCondition = responseFeedbackMat.renderAsHtmlForEditor();
					feedbackCondition = blockedHtml(feedbackCondition);
					
					ModalFeedbackCondition condition = new ModalFeedbackCondition();
					condition.setVariable(Variable.response);
					condition.setOperator(Operator.equals);
					condition.setValue(identToIdentifier.get(response.getIdent()).toString());
					List<ModalFeedbackCondition> conditions = new ArrayList<>(1);
					conditions.add(condition);
					
					ModalFeedbackBuilder feedback = new ModalFeedbackBuilder(itemBuilder.getAssessmentItem(), ModalFeedbackType.additional);
					feedback.setFeedbackConditions(conditions);
					feedback.setText(feedbackCondition);
					additionalFeedbacks.add(feedback);
				}
			}
		}
		itemBuilder.setAdditionalFeedbackBuilders(additionalFeedbacks);
	}
	
	private void convertDuration(Duration duration, ControlObject<?> parent) {
		if(duration != null && duration.isSet()) {
			TimeLimits timeLimits = new TimeLimits(parent);
			double timeInSeconds = (60 * duration.getMin()) + duration.getSec();
			timeLimits.setMaximum(timeInSeconds);
			parent.setTimeLimits(timeLimits);
		}
	}
	
	/**
	 * Make sure the HTML content is in block elements. Simple text
	 * are returned as is.
	 * 
	 * @param text
	 * @return
	 */
	protected final String blockedHtml(String text) {
		if(StringHelper.containsNonWhitespace(text)) {
			collectMaterial(text);
			if(StringHelper.isHtml(text)) {
				String trimmedText = text.trim();
				trimmedText = trimmedText.replace("<hr />", "<hr></hr>");
				try {
					Writer out = new StringWriter();
					XMLOutputFactory xof = XMLOutputFactory.newInstance();
					XMLStreamWriter xtw = xof.createXMLStreamWriter(out);
						
					HtmlParser parser = new HtmlParser();
					QTI12To21HtmlHandler handler = new QTI12To21HtmlHandler(xtw);
					parser.setContentHandler(handler);
					parser.parse(new InputSource(new StringReader(trimmedText)));
					String blockedHtml = out.toString();
					text = blockedHtml.replace("<start>", "").replace("</start>", "");
					materialMappings.putAll(handler.getMaterialsMapping());
				} catch (FactoryConfigurationError | XMLStreamException | SAXException | IOException e) {
					log.error("", e);
				}
				
			} else {
				text = StringEscapeUtils.unescapeHtml(text);
			}
		}
		return text;
	}
	
	private void collectMaterial(String content) {
		try {
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
			QTI12HtmlHandler contentHandler = new QTI12HtmlHandler(materialPath);
			parser.setContentHandler(contentHandler);
			parser.parse(new InputSource(new StringReader(content)));
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public static boolean isConvertible(OLATResource resource) {
		if(TestFileResource.TYPE_NAME.equals(resource.getResourceableTypeName())) {
			if(QTIResourceTypeModule.isOnyxTest(resource)) {
				return true;
			}
			
			QTIDocument doc = TestFileResource.getQTIDocument(resource);
			if(doc == null) {
				return false;
			}

			boolean alien = false;
			@SuppressWarnings("unchecked")
			List<Item> items = doc.getAssessment().getItems();
			for(int i=0; i<items.size(); i++) {
				Item item = items.get(i);
				alien |= item.isAlient();
			}
			return !alien;

		}
		return false;
	}
}
