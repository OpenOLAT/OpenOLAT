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
package org.olat.ims.qti21.questionimport;

import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createInlineChoice;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.olat.ims.qti21.model.xml.interactions.EssayAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.EntryType;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.NumericalEntry;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.TextEntry;
import org.olat.ims.qti21.model.xml.interactions.InlineChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.InlineChoiceAssessmentItemBuilder.InlineChoiceInteractionEntry;
import org.olat.ims.qti21.model.xml.interactions.KPrimAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.LobAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.MatchAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.MultipleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;
import org.olat.ims.qti21.model.xml.interactions.SingleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.ui.editor.AssessmentItemEditorController;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.P;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.ToleranceMode;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.InlineChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleAssociableChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.DirectedPairValue;

/**
 * 
 * Initial date: 24.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CSVToAssessmentItemConverter {
	
	private static final Logger log = Tracing.createLoggerFor(CSVToAssessmentItemConverter.class);
	private static final String[] BLOCK_MARKERS = new String[] { "<p", "<P", "<div", "<DIV", "<ul", "<UL", "<ol", "<OL" };

	private int currentLine;
	private int kprimPosition = 0;
	private ImportOptions options;
	private final Locale locale;
	private final QtiSerializer qtiSerializer;
	private List<String> questionFragements;
	private AssessmentItemAndMetadata currentItem;
	private final List<AssessmentItemAndMetadata> items = new ArrayList<>();
	
	public CSVToAssessmentItemConverter(ImportOptions options, Locale locale, QtiSerializer qtiSerializer) {
		this.locale = locale;
		this.options = options;
		this.qtiSerializer = qtiSerializer;
	}
	
	public List<AssessmentItemAndMetadata> getItems() {
		return items;
	}
	
	public int getCurrentLine() {
		return currentLine;
	}
	
	public void parse(String input) {
		List<String[]> lines = getLines(input);
		for(String[] line:lines) {
			currentLine++;
			processLine(line);
		}
		buildCurrentItem();
	}
	
	private List<String[]> getLines(String input) {
		List<String[]> lines = getLines(input, '\t');
		if(lines.isEmpty()) {
			lines = getLines(input, ',');
		}
		return lines;
	}
	
	private List<String[]> getLines(String input, char separator) {
		CSVParser parser = new CSVParserBuilder()
				.withSeparator(separator)
				.build();
		
		List<String[]> lines = new ArrayList<>();
		try(CSVReader reader = new CSVReaderBuilder(new StringReader(input))
					.withCSVParser(parser)
					.build()) {
			
			String [] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				if(nextLine.length > 1) {
					String[] stripedLine = stripLine(nextLine);
					lines.add(stripedLine);
				}
			}
		} catch (IOException | CsvValidationException e) {
			log.error("", e);
		}
		return lines;
	}
	
	private String[] stripLine(String[] line) {
		int lastPos = line.length;
		for(int i=lastPos; i-->0; ) {
			if(StringHelper.containsNonWhitespace(line[i])) {
				lastPos = i;
				break;
			}
		}
		
		if(lastPos < (line.length - 1)) {
			String[] newLine = new String[lastPos + 1];
			System.arraycopy(line, 0, newLine, 0, lastPos + 1);
			line = newLine;
		}
		return line;
	}
	
	private void processLine(String[] parts) {
		String marker = parts[0].toLowerCase();
		switch(marker) {
			case "typ":
			case "type": processType(parts); break;
			case "titel":
			case "title": processTitle(parts); break;
			case "topic": processTopic(parts); break;
			case "beschreibung":
			case "description": processDescription(parts); break;
			case "frage":
			case "question": processQuestion(parts); break;
			case "punkte":
			case "points": processPoints(parts); break;
			case "max. number of possible answers":
			case "max. answers":
			case "max answers": processMaxAnswers(parts); break;
			case "min. number of possible answers":
			case "min. answers":
			case "min answers": processMinAnswers(parts); break;
			case "fachbereich":
			case "subject": processTaxonomyPath(parts); break;
			case "feedback correct answer": processFeedbackCorrectAnswer(parts); break;
			case "feedback wrong answer": processFeedbackWrongAnswer(parts); break;
			case "hint": processHint(parts); break;
			case "correct solution": processCorrectSolution(parts); break;
			case "schlagworte":
			case "keywords": processKeywords(parts); break;
			case "abdeckung":
			case "coverage": processCoverage(parts); break;
			case "level": processLevel(parts); break;
			case "sprache":
			case "language": processLanguage(parts); break;
			case "durchschnittliche bearbeitungszeit":
			case "typical learning time": processTypicalLearningTime(parts); break;
			case "required grading time": processCorrectionTime(parts); break;
			case "itemschwierigkeit":
			case "difficulty index": processDifficultyIndex(parts); break;
			case "standardabweichung itemschwierigkeit":
			case "standard deviation": processStandardDeviation(parts); break;
			case "trennsch\u00E4rfe":
			case "discrimination index": processDiscriminationIndex(parts); break;
			case "anzahl distraktoren":
			case "distractors": processDistractors(parts); break;
			case "editor": processEditor(parts); break;
			case "editor version": processEditorVersion(parts); break;
			case "lizenz":
			case "license": processLicense(parts); break;
			case "min": processMin(parts); break;
			case "max": processMax(parts); break;
			default: processChoice(parts);
		}
	}
	
	private void processDescription(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String description = parts[1];
		if(StringHelper.containsNonWhitespace(description)) {
			currentItem.setDescription(description);
		}
	}

	private void processLevel(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String level = parts[1];
		if(StringHelper.containsNonWhitespace(level)) {
			currentItem.setLevel(level.trim());
		}
	}
	
	private void processTypicalLearningTime(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String time = parts[1];
		if(StringHelper.containsNonWhitespace(time)) {
			currentItem.setTypicalLearningTime(time.trim());
		}
	}
	
	private void processCorrectionTime(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String time = parts[1];
		if(StringHelper.containsNonWhitespace(time) && StringHelper.isLong(time.trim())) {
			try {
				currentItem.setCorrectionTime(Integer.valueOf(time.trim()));
			} catch (NumberFormatException e) {
				log.error("Cannot parse correction time: {}", time, e);
			}
		}
	}
	
	private void processLicense(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String license = parts[1];
		if(StringHelper.containsNonWhitespace(license)) {
			currentItem.setLicense(license.trim());
		}
	}
	
	private void processEditor(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String editor = parts[1];
		if(StringHelper.containsNonWhitespace(editor)) {
			currentItem.setEditor(editor.trim());
		}
	}
	
	private void processEditorVersion(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String editorVersion = parts[1];
		if(StringHelper.containsNonWhitespace(editorVersion)) {
			currentItem.setEditorVersion(editorVersion.trim());
		}
	}
	
	private void processFeedbackCorrectAnswer(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String feedback = parts[1];
		if(StringHelper.containsNonWhitespace(feedback)) {
			AssessmentItemBuilder itemBuilder = currentItem.getItemBuilder();
			itemBuilder.createCorrectFeedback().setText(feedback);
		}
	}
	
	private void processFeedbackWrongAnswer(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String feedback = parts[1];
		if(StringHelper.containsNonWhitespace(feedback)) {
			AssessmentItemBuilder itemBuilder = currentItem.getItemBuilder();
			itemBuilder.createIncorrectFeedback().setText(feedback);
		}
	}
	
	private void processHint(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String feedback = parts[1];
		if(StringHelper.containsNonWhitespace(feedback)) {
			AssessmentItemBuilder itemBuilder = currentItem.getItemBuilder();
			itemBuilder.createHint().setText(feedback);
		}
	}
	
	private void processCorrectSolution(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String feedback = parts[1];
		if(StringHelper.containsNonWhitespace(feedback)) {
			AssessmentItemBuilder itemBuilder = currentItem.getItemBuilder();
			itemBuilder.createCorrectSolutionFeedback().setText(feedback);
		}
	}
	
	private void processDistractors(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String distractors = parts[1];
		if(StringHelper.containsNonWhitespace(distractors)) {
			try {
				currentItem.setNumOfAnswerAlternatives(Integer.parseInt(distractors.trim()));
			} catch (NumberFormatException e) {
				log.warn("", e);
			}
		}
	}
	
	private void processDiscriminationIndex(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String discriminationIndex = parts[1];
		if(StringHelper.containsNonWhitespace(discriminationIndex)) {
			try {
				currentItem.setDifferentiation(new BigDecimal(discriminationIndex.replace(",", ".").trim()));
			} catch (Exception e) {
				log.warn("", e);
			}
		}
	}
	
	private void processDifficultyIndex(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String difficulty = parts[1];
		if(StringHelper.containsNonWhitespace(difficulty)) {
			try {
				BigDecimal dif = new BigDecimal(difficulty.replace(",", ".").trim());
				if(dif.doubleValue() >= 0.0d && dif.doubleValue() <= 1.0d) {
					currentItem.setDifficulty(dif);
				} else {
					currentItem.setHasError(true);
				}
			} catch (Exception e) {
				log.warn("", e);
			}
		}
	}
	
	private void processStandardDeviation(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String stddev = parts[1];
		if(StringHelper.containsNonWhitespace(stddev)) {
			try {
				BigDecimal dev = new BigDecimal(stddev.replace(",", ".").trim());
				if(dev.doubleValue() >= 0.0d && dev.doubleValue() <= 1.0d) {
					currentItem.setStdevDifficulty(dev);
				} else {
					currentItem.setHasError(true);
				}
			} catch (Exception e) {
				log.warn("", e);
			}
		}
	}
	
	private void processMin(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String min = parts[1];
		if(StringHelper.isLong(min) && currentItem.getItemBuilder() instanceof EssayAssessmentItemBuilder) {
			((EssayAssessmentItemBuilder)currentItem.getItemBuilder()).setMinStrings(Integer.valueOf(min));
		}
	}
	
	private void processMax(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String max = parts[1];
		if(StringHelper.isLong(max) && currentItem.getItemBuilder() instanceof EssayAssessmentItemBuilder) {
			((EssayAssessmentItemBuilder)currentItem.getItemBuilder()).setMaxStrings(Integer.valueOf(max));
		}
	}
	
	private void processType(String[] parts) {
		buildCurrentItem();
		
		if(parts.length > 1) {
			String type = parts[1].toLowerCase().replace(" ", "");
			AssessmentItemBuilder itemBuilder;
			switch(type) {
				case "fib": {
					FIBAssessmentItemBuilder fibItemBuilder = new FIBAssessmentItemBuilder("Gap text", EntryType.text, qtiSerializer);
					fibItemBuilder.setQuestionType(QTI21QuestionType.fib);
					itemBuilder = initFIBAssessmentItemBuilder(fibItemBuilder);
					break;
				}
				case "numerical": {
					FIBAssessmentItemBuilder numericalItemBuilder = new FIBAssessmentItemBuilder("Numerical", EntryType.numerical, qtiSerializer);
					numericalItemBuilder.setQuestionType(QTI21QuestionType.numerical);
					itemBuilder = initFIBAssessmentItemBuilder(numericalItemBuilder);
					break;
				}
				case "mc": {
					MultipleChoiceAssessmentItemBuilder mcItemBuilder = new MultipleChoiceAssessmentItemBuilder("Multiple choice", "New answer", qtiSerializer);
					itemBuilder = initMultipleChoiceAssessmentItemBuilder(mcItemBuilder);
					break;
				}
				case "sc": {
					SingleChoiceAssessmentItemBuilder scItemBuilder = new SingleChoiceAssessmentItemBuilder("Single choice", "New answer", qtiSerializer);
					itemBuilder = initSingleChoiceAssessmentItemBuilder(scItemBuilder);
					break;
				}
				case "kprim": {
					kprimPosition = 0;
					itemBuilder = new KPrimAssessmentItemBuilder("Kprim", "New answer", qtiSerializer);
					break;
				}
				case "essay": {
					itemBuilder = new EssayAssessmentItemBuilder("Essay", qtiSerializer);
					break;
				}
				case "matrix": {
					MatchAssessmentItemBuilder matchBuilder = new MatchAssessmentItemBuilder("Matrix", QTI21Constants.CSS_MATCH_MATRIX, qtiSerializer);
					itemBuilder = initMatchAssessmentItemBuilder(matchBuilder);
					break;
				}
				case "drag&drop": {
					MatchAssessmentItemBuilder matchBuilder = new MatchAssessmentItemBuilder("Matrix", QTI21Constants.CSS_MATCH_DRAG_AND_DROP, qtiSerializer);
					itemBuilder = initMatchAssessmentItemBuilder(matchBuilder);
					break;
				}
				case "truefalse": {
					Translator trans = Util.createPackageTranslator(AssessmentItemEditorController.class, locale);
					MatchAssessmentItemBuilder matchBuilder = new MatchAssessmentItemBuilder("Matrix", QTI21Constants.CSS_MATCH_TRUE_FALSE,
							trans.translate("match.unanswered"), trans.translate("match.true"), trans.translate("match.false"), qtiSerializer);
					itemBuilder = initMatchAssessmentItemBuilderForTrueFalse(matchBuilder);
					break;
				} case "inlinechoice": {
					InlineChoiceAssessmentItemBuilder matchBuilder = new InlineChoiceAssessmentItemBuilder("Inline choice", qtiSerializer);
					itemBuilder = initInlineChoiceAssessmentItemBuilder(matchBuilder);
					break;
				}
				default: {
					itemBuilder = null;
				}
			}
			
			if(itemBuilder != null) {
				currentItem = new AssessmentItemAndMetadata(itemBuilder);
			} else {
				log.warn("Question type not supported: {}", type);
				currentItem = null;
			}
		}
	}
	
	private FIBAssessmentItemBuilder initFIBAssessmentItemBuilder(FIBAssessmentItemBuilder fibItemBuilder) {
		fibItemBuilder.setQuestion("");
		fibItemBuilder.clearTextEntries();
		fibItemBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
		return fibItemBuilder;
	}
	
	private SingleChoiceAssessmentItemBuilder initSingleChoiceAssessmentItemBuilder(SingleChoiceAssessmentItemBuilder scItemBuilder) {
		scItemBuilder.clearSimpleChoices();
		scItemBuilder.clearMapping();
		scItemBuilder.setShuffle(options.isShuffle());
		scItemBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
		return scItemBuilder;
	}
	
	private MultipleChoiceAssessmentItemBuilder initMultipleChoiceAssessmentItemBuilder(MultipleChoiceAssessmentItemBuilder mcItemBuilder) {
		mcItemBuilder.clearSimpleChoices();
		mcItemBuilder.clearMapping();
		mcItemBuilder.setShuffle(options.isShuffle());
		mcItemBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
		return mcItemBuilder;
	}
	
	private MatchAssessmentItemBuilder initMatchAssessmentItemBuilder(MatchAssessmentItemBuilder matchBuilder) {
		//reset
		matchBuilder.setQuestion("");
		matchBuilder.clearAssociations();
		matchBuilder.clearMapping();
		matchBuilder.getTargetChoices().clear();
		matchBuilder.getSourceChoices().clear();
		//set default options
		matchBuilder.setShuffle(options.isShuffle());
		matchBuilder.setMultipleChoice(true);
		matchBuilder.addMatchInteractionClass(QTI21Constants.CSS_MATCH_SOURCE_LEFT);
		matchBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
		return matchBuilder;
	}
	
	private MatchAssessmentItemBuilder initMatchAssessmentItemBuilderForTrueFalse(MatchAssessmentItemBuilder matchBuilder) {
		//reset
		matchBuilder.setQuestion("");
		matchBuilder.clearAssociations();
		matchBuilder.clearMapping();
		matchBuilder.getSourceChoices().clear();
		//set default options
		matchBuilder.setShuffle(false);
		matchBuilder.setMultipleChoice(false);
		matchBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
		return matchBuilder;
	}
	
	private InlineChoiceAssessmentItemBuilder initInlineChoiceAssessmentItemBuilder(InlineChoiceAssessmentItemBuilder inlineChoiceBuilder) {
		//reset
		inlineChoiceBuilder.setQuestion("");
		for(InlineChoiceInteractionEntry entry:inlineChoiceBuilder.getInteractions()) {
			inlineChoiceBuilder.removeInteraction(entry);
		}
		//set default options
		inlineChoiceBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
		return inlineChoiceBuilder;
	}
	
	private void buildCurrentItem() {
		if(currentItem != null) {
			try {
				String question = buildQuestion(); 
				currentItem.getItemBuilder().setQuestion(question);
				currentItem.getItemBuilder().postImportProcessing();
				currentItem.getItemBuilder().build();
				items.add(currentItem);
			} catch (Exception e) {
				log.error("", e);
				currentItem.setHasError(true);
			}
		}
		currentItem = null;
		questionFragements = null;
	}
	
	private String buildQuestion() {
		StringBuilder questionBuilder = new StringBuilder(2048);
		if(questionFragements != null) {
			boolean needClosing = false;

			for(String questionFragment:questionFragements) {
				if(isHtmlBlock(questionFragment)) {
					if(needClosing) {
						questionBuilder.append("</p>");
						needClosing = false;
					}
					questionBuilder.append(questionFragment);
				} else {
					if(!needClosing) {
						questionBuilder.append("<p>");
						needClosing = true;
					}
					
					String[] subLines = questionFragment.split("\r?\n");
					if(subLines.length > 1) {
						StringBuilder sb = new StringBuilder(questionFragment.length() + 32);
						for(int i=0; i<subLines.length; i++) {
							if(i>0) {
								sb.append("<br>");
							}
							sb.append(subLines[i]);
						}
						questionFragment = sb.toString();
					}
					questionBuilder.append(questionFragment);
				}
			}
			
			if(needClosing) {
				questionBuilder.append("</p>");
			}
		}

		String question = questionBuilder.toString();
		if(!StringHelper.isHtml(question)) {
			question = "<p>" + question + "</p>";
		}
		return question;
	}
	
	private boolean isHtmlBlock(String block) {
		if(block == null) return false;
		
		for(String marker:BLOCK_MARKERS) {
			if(block.startsWith(marker)) {
				return true;
			}
		}
		return false;
	}
	
	
	private void processCoverage(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String coverage = parts[1];
		if(StringHelper.containsNonWhitespace(coverage)) {
			currentItem.setCoverage(coverage);
		}
	}
	
	private void processKeywords(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String keywords = parts[1];
		if(StringHelper.containsNonWhitespace(keywords)) {
			currentItem.setKeywords(keywords);
		}
	}
	
	private void processTaxonomyPath(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String taxonomyPath = parts[1];
		if(StringHelper.containsNonWhitespace(taxonomyPath)) {
			currentItem.setTaxonomyPath(taxonomyPath);
		}
	}
	
	private void processLanguage(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String language = parts[1];
		if(StringHelper.containsNonWhitespace(language)) {
			currentItem.setLanguage(language);
		}
	}
	
	private void processTitle(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String title = parts[1];
		if(StringHelper.containsNonWhitespace(title)) {
			currentItem.setTitle(title);
		}
	}
	
	private void processTopic(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String topic = parts[1];
		if(StringHelper.containsNonWhitespace(topic)) {
			currentItem.setTopic(topic);
		}
	}
	
	private void processQuestion(String[] parts) {
		if(currentItem == null) return;
		
		String content = parts[1];
		if(StringHelper.containsNonWhitespace(content)) {
			if(questionFragements == null) {
				questionFragements = new ArrayList<>();
			}
			questionFragements.add(content);
		}
	}
	
	private void processPoints(String[] parts) {
		if(currentItem == null) return;
		
		double points = parseDouble(parts[1], 1.0d);
		AssessmentItemBuilder itemBuilder = currentItem.getItemBuilder();
		if (itemBuilder instanceof SimpleChoiceAssessmentItemBuilder
				|| itemBuilder instanceof FIBAssessmentItemBuilder
				|| itemBuilder instanceof KPrimAssessmentItemBuilder
				|| itemBuilder instanceof MatchAssessmentItemBuilder
				|| itemBuilder instanceof LobAssessmentItemBuilder
				|| itemBuilder instanceof InlineChoiceAssessmentItemBuilder) {
			itemBuilder.setMinScore(0.0d);
			itemBuilder.setMaxScore(points);
		}
	}
	
	private void processMaxAnswers(String[] parts) {
		if(currentItem == null) return;
		
		int maxChoices = parseInteger(parts[1], 0);
		AssessmentItemBuilder itemBuilder = currentItem.getItemBuilder();
		if (itemBuilder instanceof MultipleChoiceAssessmentItemBuilder) {
			((MultipleChoiceAssessmentItemBuilder) itemBuilder).setMaxChoices(maxChoices);
		}
	}
	
	private void processMinAnswers(String[] parts) {
		if(currentItem == null) return;
		
		int minChoices = parseInteger(parts[1], 0);
		AssessmentItemBuilder itemBuilder = currentItem.getItemBuilder();
		if (itemBuilder instanceof MultipleChoiceAssessmentItemBuilder) {
			((MultipleChoiceAssessmentItemBuilder) itemBuilder).setMinChoices(minChoices);
		}
	}
	
	private void processChoice(String[] parts) {
		if(currentItem == null || parts.length < 2 || isEmpty(parts)) {
			return;
		}
		
		try {
			AssessmentItemBuilder itemBuilder = currentItem.getItemBuilder();
			if (itemBuilder instanceof SimpleChoiceAssessmentItemBuilder) {
				processChoiceSmc(parts, (SimpleChoiceAssessmentItemBuilder)itemBuilder);
			} else if(itemBuilder instanceof FIBAssessmentItemBuilder) {
				FIBAssessmentItemBuilder fibItemBuilder = (FIBAssessmentItemBuilder)itemBuilder;
				if(fibItemBuilder.getQuestionType() == QTI21QuestionType.fib) {
					processChoiceFib(parts, fibItemBuilder);
				} else if(fibItemBuilder.getQuestionType() == QTI21QuestionType.numerical) {
					processChoiceNumerical(parts, fibItemBuilder);
				}
			} else if(itemBuilder instanceof KPrimAssessmentItemBuilder) {
				processChoiceKprim(parts, (KPrimAssessmentItemBuilder)itemBuilder);
			} else if(itemBuilder instanceof MatchAssessmentItemBuilder) {
				processChoiceMatch(parts, (MatchAssessmentItemBuilder)itemBuilder);
			} else if(itemBuilder instanceof InlineChoiceAssessmentItemBuilder) {
				processInlineChoice(parts, (InlineChoiceAssessmentItemBuilder)itemBuilder);
			}
			
		} catch (NumberFormatException e) {
			log.warn("Cannot parse point for: {} / {}", parts[0], parts[1], e);
		}
	}
	
	private boolean isEmpty(String[] parts) {
		if(parts != null && parts.length > 0) {
			for(int i=parts.length; i-->0; ) {
				if(StringHelper.containsNonWhitespace(parts[i])) {
					return false;
				}
			}
		}
		return true;
	}

	private void processChoiceSmc(String[] parts, SimpleChoiceAssessmentItemBuilder choiceBuilder) {
		if(StringHelper.containsNonWhitespace(parts[0])) {
			double point = parseDouble(parts[0], 1.0d);
			String content = parts[1];
	
			ChoiceInteraction interaction = choiceBuilder.getChoiceInteraction();
			SimpleChoice newChoice = AssessmentItemFactory
					.createSimpleChoice(interaction, content, choiceBuilder.getQuestionType().getPrefix());
			choiceBuilder.addSimpleChoice(newChoice);
			choiceBuilder.setMapping(newChoice.getIdentifier(), point);
	
			if(point > 0.0) {
				if (choiceBuilder instanceof MultipleChoiceAssessmentItemBuilder) {
					((MultipleChoiceAssessmentItemBuilder)choiceBuilder).addCorrectAnswer(newChoice.getIdentifier());
				} else {
					((SingleChoiceAssessmentItemBuilder)choiceBuilder).setCorrectAnswer(newChoice.getIdentifier());
				}
			}
		}
	}
	
	private void processChoiceMatch(String[] parts, MatchAssessmentItemBuilder matchBuilder) {
		List<SimpleAssociableChoice> targetChoices = matchBuilder.getTargetChoices();
		if(targetChoices == null || targetChoices.isEmpty()) {
			for(int i=1;i<parts.length; i++) {
				String answer = parts[i];
				SimpleAssociableChoice choice = AssessmentItemFactory
						.createSimpleAssociableChoice(answer, matchBuilder.getTargetMatchSet());
				targetChoices.add(choice);
			}
		} else {
			String answer = parts[0];
			if(StringHelper.containsNonWhitespace(answer)) {
				SimpleAssociableChoice sourceChoice = AssessmentItemFactory
						.createSimpleAssociableChoice(answer, matchBuilder.getSourceMatchSet());
				matchBuilder.getSourceChoices().add(sourceChoice);
				
				//correct answer and points
				for(int i=1; i<parts.length && i<(targetChoices.size() + 1); i++) {
					double point = parseDouble(parts[i], 0.0d);
					SimpleAssociableChoice targetChoice = targetChoices.get(i - 1);
					DirectedPairValue directedPair = new DirectedPairValue(sourceChoice.getIdentifier(), targetChoice.getIdentifier());
					matchBuilder.addScore(directedPair, Double.valueOf(point));
					if(point > 0.0) {
						matchBuilder.addAssociation(sourceChoice.getIdentifier(), targetChoice.getIdentifier());
					}
				}
			}
		}
	}

	private void processInlineChoice(String[] parts, InlineChoiceAssessmentItemBuilder inlineChoiceBuilder) {
		if(isText(parts)) {
			processText(parts);
		} else if(StringHelper.containsNonWhitespace(parts[0])) {
			double score = parseDouble(parts[0], 1.0d);
			String correctResponse = parts[2];
			
			String responseIdentifier = inlineChoiceBuilder.generateResponseIdentifier().toString();
			InlineChoiceInteractionEntry interactionEntry = inlineChoiceBuilder.createInteraction(responseIdentifier);
			interactionEntry.setShuffle(true);
			
			String[] choices = splitInlineChoices(parts);
			for(String choice:choices) {
				Identifier responseId = IdentifierGenerator.newAsIdentifier("inlinec");
				InlineChoice newChoice = createInlineChoice(interactionEntry.getInteraction(), choice, responseId);
				interactionEntry.getInlineChoices().add(newChoice);
				if(correctResponse != null && correctResponse.equals(choice)) {
					interactionEntry.putScore(responseId, Double.valueOf(score));
					interactionEntry.setCorrectResponseId(responseId);
				} else {
					interactionEntry.putScore(responseId, Double.valueOf(0.0d));
				}
			}
			
			String entry = " <inlineChoiceInteraction responseIdentifier=\"" + responseIdentifier + "\"></inlineChoiceInteraction>";
			if(questionFragements == null) {
				questionFragements = new ArrayList<>();
			}
			questionFragements.add(entry);	
		}
	}
	
	private String[] splitInlineChoices(String[] parts) {
		String choices = parts[1];
		String separator = ";";
		if(parts.length >= 4 && StringHelper.containsNonWhitespace(parts[3]) && parts[3].trim().length() == 1) {
			separator = parts[3].trim();
		}
		return choices.split("[" + separator + "]");
	}

	private void processChoiceFib(String[] parts, FIBAssessmentItemBuilder fibBuilder) {
		if(isText(parts)) {
			processText(parts);
		} else if(StringHelper.containsNonWhitespace(parts[0])) {
			double score = parseDouble(parts[0], 1.0d);
			String correctBlank = parts[1];
			String responseId = fibBuilder.generateResponseIdentifier();
			TextEntry textEntry = fibBuilder.createTextEntry(responseId);
			parseAlternatives(correctBlank, score, textEntry);
			if(parts.length > 2) {
				String sizes = parts[2];
				String[] sizeArr = sizes.split("[,]");
				if(sizeArr.length >= 1) {
					int size = Integer.parseInt(sizeArr[0]);
					textEntry.setExpectedLength(size);
				}	
			}
			
			String entry = "<textEntryInteraction responseIdentifier=\"" + responseId + "\"/>";
			if(questionFragements == null) {
				questionFragements = new ArrayList<>();
			}
			questionFragements.add(entry);	
		}
	}
	
	private boolean isText(String[] parts) {
		if(parts == null || parts.length < 2) return false;
		
		String firstPart = parts[0].toLowerCase();
		return "text".equals(firstPart) || "texte".equals(firstPart);
	}
	
	private void processText(String[] parts) {
		if(parts.length >= 2) {
			String text = parts[1];
			if(questionFragements == null) {
				questionFragements = new ArrayList<>();
			}
			questionFragements.add(text);
		}
	}
	
	private void processChoiceNumerical(String[] parts, FIBAssessmentItemBuilder fibBuilder) {
		String firstPart = parts[0].toLowerCase();
		if("text".equals(firstPart) || "texte".equals(firstPart)) {
			String text = parts[1];
			if(questionFragements == null) {
				questionFragements = new ArrayList<>();
			}
			questionFragements.add(text);	
		} else if(StringHelper.containsNonWhitespace(parts[0])) {
			double score = parseDouble(parts[0], 1.0d);
			String correctBlank = parts[1];
			Double correctNumerical = null;

			String responseId = fibBuilder.generateResponseIdentifier();
			NumericalEntry numericalEntry = fibBuilder.createNumericalEntry(responseId);
			if(correctBlank.indexOf(';') >= 0) {
				String[] values = correctBlank.split("[;]");
				correctNumerical = parseDouble(values[0]);
				
				if(parts.length > 2 && values.length > 2) {
					String toleranceMode = parts[2];
					if(ToleranceMode.ABSOLUTE.name().equalsIgnoreCase(toleranceMode)) {
						BigDecimal solution = new BigDecimal(values[0]);
						BigDecimal lowerBound = new BigDecimal(values[1]);
						BigDecimal upperBound = new BigDecimal(values[2]);
						String upperToleranceString = upperBound.subtract(solution).toString();
						String lowerToleranceString = solution.subtract(lowerBound).toString();
						numericalEntry.setLowerTolerance(Double.parseDouble(lowerToleranceString));
						numericalEntry.setUpperTolerance(Double.parseDouble(upperToleranceString));
						numericalEntry.setToleranceMode(ToleranceMode.ABSOLUTE);
					} else if(ToleranceMode.RELATIVE.name().equalsIgnoreCase(toleranceMode)) {
						Double lowerBound = parseDouble(values[1]);
						Double upperBound = parseDouble(values[2]);
						numericalEntry.setToleranceMode(ToleranceMode.RELATIVE);
						numericalEntry.setLowerTolerance(lowerBound);
						numericalEntry.setUpperTolerance(upperBound);
					}
				}
			} else {
				correctNumerical = parseDouble(correctBlank);
				numericalEntry.setToleranceMode(ToleranceMode.EXACT);
			}
			
			numericalEntry.setSolution(correctNumerical);
			numericalEntry.setScore(score);

			String entry = "<textEntryInteraction responseIdentifier=\"" + responseId + "\"/>";
			if(questionFragements == null) {
				questionFragements = new ArrayList<>();
			}
			questionFragements.add(entry);	
		}
	}
	
	private void processChoiceKprim(String[] parts, KPrimAssessmentItemBuilder kprimBuilder) {
		if(kprimPosition >= 4) {
			return;
		}
		
		String firstPart = parts[0].toLowerCase();
		String answer = parts[1];
		
		Identifier correctIncorrectIdentifier;
		if("+".equals(firstPart)) {
			correctIncorrectIdentifier = QTI21Constants.CORRECT_IDENTIFIER;
		} else {
			correctIncorrectIdentifier = QTI21Constants.WRONG_IDENTIFIER;
		}
		List<SimpleAssociableChoice> choices = kprimBuilder.getKprimChoices();
		
		SimpleAssociableChoice choice = choices.get(kprimPosition);
		
		P choiceText = AssessmentItemFactory.getParagraph(choice, answer);
		choice.getFlowStatics().clear();
		choice.getFlowStatics().add(choiceText);
		
		kprimBuilder.setAssociation(choice.getIdentifier(), correctIncorrectIdentifier);
		kprimPosition++;
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
	
	private Double parseDouble(String value) {
		if(StringHelper.containsNonWhitespace(value)) {
			if(value.indexOf(',') >= 0) {
				value = value.replace(",", ".");
			}
			return Double.valueOf(new BigDecimal(value).doubleValue());
		}
		return null;
	}
	
	private double parseDouble(String value, double defaultValue) {
		BigDecimal bigValue = BigDecimal.valueOf(defaultValue);
		if(StringHelper.containsNonWhitespace(value)) {
			if(value.indexOf(',') >= 0) {
				value = value.replace(",", ".");
			}
			bigValue = new BigDecimal(value);
		}
		return bigValue.doubleValue();
	}
	
	private int parseInteger(String value, int defaultValue) {
		int integerValue = defaultValue;
		if(value != null && StringHelper.isLong(value)) {
			try {
				integerValue = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				log.error("Cannot parse: {}", value, e);
			}
		}
		return integerValue;
	}
}
