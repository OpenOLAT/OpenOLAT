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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.EntryType;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.TextEntry;
import org.olat.ims.qti21.model.xml.interactions.MultipleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SingleChoiceAssessmentItemBuilder;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;

/**
 * 
 * Initial date: 24.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CSVToAssessmentItemConverter {
	
	private static final OLog log = Tracing.createLoggerFor(CSVToAssessmentItemConverter.class);

	private ImportOptions options;
	private final QtiSerializer qtiSerializer;
	private AssessmentItemAndMetadata currentItem;
	private final List<AssessmentItemAndMetadata> items = new ArrayList<>();
	
	public CSVToAssessmentItemConverter(ImportOptions options, QtiSerializer qtiSerializer) {
		this.options = options;
		this.qtiSerializer = qtiSerializer;
	}
	
	public List<AssessmentItemAndMetadata> getItems() {
		return items;
	}
	
	public void parse(String input) {
		String[] lines = input.split("\r?\n");
		
		for (int i = 0; i<lines.length; i++) {
			String line = lines[i];
			if (line.equals("")) {
				continue;
			}
		
			String delimiter = "\t";
			// use comma as fallback delimiter, e.g. for better testing
			if (line.indexOf(delimiter) == -1) {
				delimiter = ",";
			}
			String[] parts = line.split(delimiter);
			if(parts.length > 1) {
				processLine(parts);
			}	
		}
		
		if(currentItem != null) {
			build();
			items.add(currentItem);
			currentItem = null;
		}
	}
	
	private void processLine(String[] parts) {
		String marker = parts[0].toLowerCase();
		switch(marker) {
			case "typ":
			case "type": processType(parts); break;
			case "titel":
			case "title": processTitle(parts); break;
			case "beschreibung":
			case "description": processDescription(parts); break;
			case "frage":
			case "question": processQuestion(parts); break;
			case "punkte":
			case "points": processPoints(parts); break;
			case "fachbereich":
			case "subject": processTaxonomyPath(parts); break;
			case "feedback correct answer": processFeedbackCorrectAnswer(parts); break;
			case "feedback wrong answer": processFeedbackWrongAnswer(parts); break;
			case "schlagworte":
			case "keywords": processKeywords(parts); break;
			case "abdeckung":
			case "coverage": processCoverage(parts); break;
			case "level": processLevel(parts); break;
			case "sprache":
			case "language": processLanguage(parts); break;
			case "durchschnittliche bearbeitungszeit":
			case "typical learning time": processTypicalLearningTime(parts); break;
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
				currentItem.setDifferentiation(new BigDecimal(discriminationIndex.trim()));
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
				BigDecimal dif = new BigDecimal(difficulty.trim());
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
				BigDecimal dev = new BigDecimal(stddev.trim());
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
	
	private void processType(String[] parts) {
		if(currentItem != null) {
			build();
			items.add(currentItem);
			currentItem = null;
		}
		
		if(parts.length > 1) {
			String type = parts[1].toLowerCase();
			AssessmentItemBuilder itemBuilder;
			switch(type) {
				case "fib": {
					FIBAssessmentItemBuilder fibItemBuilder = new FIBAssessmentItemBuilder(EntryType.text, qtiSerializer);
					fibItemBuilder.setQuestion("");
					fibItemBuilder.clearTextEntries();
					fibItemBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
					itemBuilder = fibItemBuilder;
					break;
				}
				case "mc": {
					MultipleChoiceAssessmentItemBuilder mcItemBuilder = new MultipleChoiceAssessmentItemBuilder(qtiSerializer);
					mcItemBuilder.clearSimpleChoices();
					mcItemBuilder.clearMapping();
					mcItemBuilder.setShuffle(options.isShuffle());
					mcItemBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
					itemBuilder = mcItemBuilder;
					break;
				}
				case "sc": {
					SingleChoiceAssessmentItemBuilder scItemBuilder = new SingleChoiceAssessmentItemBuilder(qtiSerializer);
					scItemBuilder.clearSimpleChoices();
					scItemBuilder.clearMapping();
					scItemBuilder.setShuffle(options.isShuffle());
					scItemBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
					itemBuilder = scItemBuilder;
					break;
				}
				default: {
					itemBuilder = null;
				}
			}
			
			if(itemBuilder != null) {
				currentItem = new AssessmentItemAndMetadata(itemBuilder);
			} else {
				log.warn("Question type not supported: " + type);
				currentItem = null;
			}
		}
	}
	
	private void build() {
		if(currentItem != null) {
			String question = currentItem.getItemBuilder().getQuestion();
			if(!StringHelper.isHtml(question)) {
				question = "<p>" + question + "</p>";
			}
			currentItem.getItemBuilder().setQuestion(question);
			currentItem.getItemBuilder().build();
		}
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
	
	private void processQuestion(String[] parts) {
		if(currentItem == null) return;
		
		String content = parts[1];
		if(StringHelper.containsNonWhitespace(content)) {
			AssessmentItemBuilder itemBuilder = currentItem.getItemBuilder();
			itemBuilder.setQuestion("<p>" + content + "</p>");
		}
	}
	
	private void processPoints(String[] parts) {
		if(currentItem == null) return;
		
		double points = parseFloat(parts[1], 1.0f);
		AssessmentItemBuilder itemBuilder = currentItem.getItemBuilder();
		if (itemBuilder instanceof SimpleChoiceAssessmentItemBuilder) {
			itemBuilder.setMinScore(0.0d);
			itemBuilder.setMaxScore(points);
		} else if(itemBuilder instanceof FIBAssessmentItemBuilder) {
			itemBuilder.setMinScore(0.0d);
			itemBuilder.setMaxScore(points);
		}
	}
	
	private void processChoice(String[] parts) {
		if(currentItem == null || parts.length < 2) {
			return;
		}
		
		try {
			AssessmentItemBuilder itemBuilder = currentItem.getItemBuilder();
			if (itemBuilder instanceof SimpleChoiceAssessmentItemBuilder) {
				double point = parseFloat(parts[0], 1.0f);
				String content = parts[1];

				SimpleChoiceAssessmentItemBuilder choiceBuilder = (SimpleChoiceAssessmentItemBuilder)itemBuilder;
				ChoiceInteraction interaction = choiceBuilder.getChoiceInteraction();
				SimpleChoice newChoice = AssessmentItemFactory
						.createSimpleChoice(interaction, content, choiceBuilder.getQuestionType().getPrefix());
				choiceBuilder.addSimpleChoice(newChoice);
				choiceBuilder.setMapping(newChoice.getIdentifier(), point);

				if(point > 0.0) {
					if (itemBuilder instanceof MultipleChoiceAssessmentItemBuilder) {
						((MultipleChoiceAssessmentItemBuilder)itemBuilder).addCorrectAnswer(newChoice.getIdentifier());
					} else if (itemBuilder instanceof SingleChoiceAssessmentItemBuilder) {
						((SingleChoiceAssessmentItemBuilder)itemBuilder).setCorrectAnswer(newChoice.getIdentifier());
					}
				}
			} else if(itemBuilder instanceof FIBAssessmentItemBuilder) {
				String firstPart = parts[0].toLowerCase();
				FIBAssessmentItemBuilder fibBuilder = (FIBAssessmentItemBuilder)itemBuilder;
				if("text".equals(firstPart) || "texte".equals(firstPart)) {
					String text = parts[1];
					if(StringHelper.containsNonWhitespace(fibBuilder.getQuestion())) {
						fibBuilder.setQuestion(fibBuilder.getQuestion() + " " + text);
					} else {
						fibBuilder.setQuestion(text);
					}	
				} else {
					double score = parseFloat(parts[0], 1.0f);
					String correctBlank = parts[1];
					String responseId = fibBuilder.generateResponseIdentifier();
					TextEntry textEntry = fibBuilder.createTextEntry(responseId);
					parseAlternatives(correctBlank, score, textEntry);
					if(parts.length > 2) {
						String sizes = parts[2];
						String[] sizeArr = sizes.split(",");
						if(sizeArr.length >= 2) {
							int size = Integer.parseInt(sizeArr[0]);
							//int maxLength = Integer.parseInt(sizeArr[1]);
							textEntry.setExpectedLength(size);
						}	
					}
					
					String entry = " <textEntryInteraction responseIdentifier=\"" + responseId + "\"/>";
					fibBuilder.setQuestion(fibBuilder.getQuestion() + " " + entry);
				}
			}
		} catch (NumberFormatException e) {
			log.warn("Cannot parse point for: " + parts[0] + " / " + parts[1], e);
		}
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
	
	private float parseFloat(String value, float defaultValue) {
		float floatValue = defaultValue;
		
		if(value != null) {
			if(value.indexOf(",") >= 0) {
				value = value.replace(",", ".");
			}
			floatValue = Float.parseFloat(value);
		}
		return floatValue;
	}
}
