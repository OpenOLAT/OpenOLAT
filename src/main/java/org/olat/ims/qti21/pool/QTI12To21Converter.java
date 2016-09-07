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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import org.olat.core.helpers.Settings;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti.editor.QTIEditHelper;
import org.olat.ims.qti.editor.QTIEditorPackage;
import org.olat.ims.qti.editor.beecom.objects.Assessment;
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
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.xml.AssessmentHtmlBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.olat.ims.qti21.model.xml.AssessmentTestBuilder;
import org.olat.ims.qti21.model.xml.AssessmentTestFactory;
import org.olat.ims.qti21.model.xml.ManifestBuilder;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;
import org.olat.ims.qti21.model.xml.ModalFeedbackBuilder;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;
import org.olat.ims.qti21.model.xml.interactions.EssayAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.EntryType;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.TextEntry;
import org.olat.ims.qti21.model.xml.interactions.KPrimAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.MultipleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SingleChoiceAssessmentItemBuilder;

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

/**
 * 
 * Initial date: 19.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI12To21Converter {
	
	private static final OLog log = Tracing.createLoggerFor(QTI12To21Converter.class);
	
	private final Locale locale;
	private final File unzippedDirRoot;
	private final QtiSerializer qtiSerializer = new QtiSerializer(null);
	private final AssessmentHtmlBuilder htmlBuilder = new AssessmentHtmlBuilder(qtiSerializer);
	
	private final ManifestBuilder manifest;
	
	public QTI12To21Converter(File unzippedDirRoot, Locale locale) {
		this.locale = locale;
		this.unzippedDirRoot = unzippedDirRoot;
		manifest = ManifestBuilder.createAssessmentTestBuilder();
	}
	
	public AssessmentTest convert(QTIEditorPackage qtiEditorPackage)
	throws URISyntaxException {
		return convert(qtiEditorPackage.getQTIDocument());
	}
	
	public AssessmentTest convert(QTIDocument doc)
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
		if(tmpControl.getFeedback() == Control.CTRL_YES) {
			itemSessionControl.setShowFeedback(Boolean.TRUE);
		}
		if(tmpControl.getSolution() == Control.CTRL_YES) {
			itemSessionControl.setShowSolution(Boolean.TRUE);
		}

		AssessmentTestBuilder assessmentTestBuilder = new AssessmentTestBuilder(assessmentTest);
		assessmentTestBuilder.setExportScore(true);

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

		assessmentTest = assessmentTestBuilder.build();
		persistAssessmentObject(testFile, assessmentTest);
		manifest.write(new File(unzippedDirRoot, "imsmanifest.xml"));
		return assessmentTest;
	}
	
	private void convert(Section section, TestPart testPart)
	throws URISyntaxException {
		AssessmentSection assessmentSection = AssessmentTestFactory.appendAssessmentSection(testPart);
		assessmentSection.setTitle(section.getTitle());
		convertDuration(section.getDuration(), assessmentSection);
		
		RubricBlock rubricBlock = assessmentSection.getRubricBlocks().get(0);
		rubricBlock.getBlocks().clear();
		htmlBuilder.appendHtml(rubricBlock, section.getObjectives());

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
			}
		}
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
		metadata.setQtiMetadata(itemBuilder.getInteractionNames());
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
		SingleChoiceAssessmentItemBuilder itemBuilder = new SingleChoiceAssessmentItemBuilder(qtiSerializer);
		convertItemBasics(item, itemBuilder);
		itemBuilder.clearMapping();
		itemBuilder.clearSimpleChoices();
		itemBuilder.setScoreEvaluationMode(ScoreEvaluation.allCorrectAnswers);
		
		ChoiceInteraction interaction = itemBuilder.getChoiceInteraction();
		
		Question question = item.getQuestion();
		itemBuilder.setShuffle(question.isShuffle());
		
		List<Response> responses = question.getResponses();
		for(Response response:responses) {
			String responseText = response.getContent().renderAsHtmlForEditor();
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
			
			if(response.isCorrect()) {
				itemBuilder.setCorrectAnswer(newChoice.getIdentifier());
			}	
		}
		
		double correctScore = question.getSingleCorrectScore();
		if(correctScore >= 0.0d) {
			itemBuilder.setMinScore(0.0d);
			itemBuilder.setMaxScore(correctScore);
		}
		
		return itemBuilder;
	}
	
	private AssessmentItemBuilder convertMultipleChoice(Item item) {
		MultipleChoiceAssessmentItemBuilder itemBuilder = new MultipleChoiceAssessmentItemBuilder(qtiSerializer);
		convertItemBasics(item, itemBuilder);
		itemBuilder.clearMapping();
		itemBuilder.clearSimpleChoices();
		
		ChoiceInteraction interaction = itemBuilder.getChoiceInteraction();
		
		Question question = item.getQuestion();
		itemBuilder.setShuffle(question.isShuffle());
		
		boolean singleCorrect = question.isSingleCorrect();
		List<Response> responses = question.getResponses();
		for(Response response:responses) {
			String responseText = response.getContent().renderAsHtmlForEditor();

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
			if(response.isCorrect()) {
				itemBuilder.addCorrectAnswer(newChoice.getIdentifier());
			}
			double score = response.getPoints();
			if(singleCorrect) {
				if(score > 0.0f) {
					itemBuilder.setMaxScore(score);
				}
			} else {
				itemBuilder.setMapping(newChoice.getIdentifier(), score);
			}
		}

		if(singleCorrect) {
			itemBuilder.setScoreEvaluationMode(ScoreEvaluation.allCorrectAnswers);
		} else {
			itemBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
		}
		
		return itemBuilder;
	}
	
	private AssessmentItemBuilder convertKPrim(Item item) {
		KPrimAssessmentItemBuilder itemBuilder = new KPrimAssessmentItemBuilder(qtiSerializer);
		convertItemBasics(item, itemBuilder);
		
		Question question = item.getQuestion();
		itemBuilder.setShuffle(question.isShuffle());
		
		List<Response> responses = question.getResponses();
		List<SimpleAssociableChoice> choices = itemBuilder.getKprimChoices();
		for(int i=0; i<4; i++) {
			Response response = responses.get(i);
			SimpleAssociableChoice choice = choices.get(i);
			
			String answer = response.getContent().renderAsHtmlForEditor();
			P firstChoiceText = AssessmentItemFactory.getParagraph(choice, answer);
			choice.getFlowStatics().clear();
			choice.getFlowStatics().add(firstChoiceText);
			
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
		FIBAssessmentItemBuilder itemBuilder = new FIBAssessmentItemBuilder(EntryType.text, qtiSerializer);
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
		EssayAssessmentItemBuilder itemBuilder = new EssayAssessmentItemBuilder(qtiSerializer);
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
		itemBuilder.setQuestion(questionText);
		
		String hintText = question.getHintText();
		if(StringHelper.containsNonWhitespace(hintText)) {
			ModalFeedbackBuilder hint = itemBuilder.createHint();
			hint.setText(hintText);
		}
		
		String feedbackMastery = QTIEditHelper.getFeedbackMasteryText(item);
		if(StringHelper.containsNonWhitespace(feedbackMastery)) {
			ModalFeedbackBuilder feedback = itemBuilder.createCorrectFeedback();
			feedback.setText(feedbackMastery);
		}

		String feedbackFail = QTIEditHelper.getFeedbackFailText(item);
		if(StringHelper.containsNonWhitespace(feedbackFail)) {
			ModalFeedbackBuilder feedback = itemBuilder.createIncorrectFeedback();
			feedback.setText(feedbackFail);
		}
		
	}

	private void convertDuration(Duration duration, ControlObject<?> parent) {
		if(duration != null && duration.isSet()) {
			TimeLimits timeLimits = new TimeLimits(parent);
			double timeInSeconds = (60 * duration.getMin()) + duration.getSec();
			timeLimits.setMaximum(timeInSeconds);
			parent.setTimeLimits(timeLimits);
		}
	}
}
