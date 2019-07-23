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
package org.olat.ims.qti.questionimport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.ims.qti.editor.QTIEditHelper;
import org.olat.ims.qti.editor.beecom.objects.ChoiceQuestion;
import org.olat.ims.qti.editor.beecom.objects.ChoiceResponse;
import org.olat.ims.qti.editor.beecom.objects.Control;
import org.olat.ims.qti.editor.beecom.objects.FIBQuestion;
import org.olat.ims.qti.editor.beecom.objects.FIBResponse;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.Material;
import org.olat.ims.qti.editor.beecom.objects.Mattext;
import org.olat.ims.qti.editor.beecom.objects.QTIObject;
import org.olat.ims.qti.editor.beecom.objects.Question;
import org.olat.ims.qti.editor.beecom.objects.Response;

/**
 * 
 * Initial date: 24.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CSVToQuestionConverter {
	
	private static final Logger log = Tracing.createLoggerFor(CSVToQuestionConverter.class);

	private ItemAndMetadata currentItem;
	private final List<ItemAndMetadata> items = new ArrayList<>();
	private Translator translator;
	private ImportOptions options;
	
	public CSVToQuestionConverter(Translator translator, ImportOptions options) {
		this.translator = translator;
		this.options = options;
	}
	
	public List<ItemAndMetadata> getItems() {
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
			Item item = currentItem.getItem();
			Control control = QTIEditHelper.getControl(item);
			if(control.getFeedback() != 1) {
				control.setFeedback(1);
			}
			QTIEditHelper.setFeedbackMastery(item, feedback);
		}
	}
	
	private void processFeedbackWrongAnswer(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String feedback = parts[1];
		if(StringHelper.containsNonWhitespace(feedback)) {
			Item item = currentItem.getItem();
			Control control = QTIEditHelper.getControl(item);
			if(control.getFeedback() != 1) {
				control.setFeedback(1);
			}
			QTIEditHelper.setFeedbackFail(item, feedback);
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
			items.add(currentItem);
		}
		
		if(parts.length > 1) {
			String type = parts[1].toLowerCase();
			switch(type) {
				case "fib": {
					currentItem = new ItemAndMetadata(QTIEditHelper.createFIBItem(translator));
					((FIBQuestion)currentItem.getItem().getQuestion()).getResponses().clear();
					break;
				}
				case "mc": {
					currentItem = new ItemAndMetadata(QTIEditHelper.createMCItem(translator));
					((ChoiceQuestion)currentItem.getItem().getQuestion()).getResponses().clear();
					((ChoiceQuestion)currentItem.getItem().getQuestion()).setShuffle(options.isShuffle());
					break;
				}
				case "sc": {
					currentItem = new ItemAndMetadata(QTIEditHelper.createSCItem(translator));
					((ChoiceQuestion)currentItem.getItem().getQuestion()).getResponses().clear();
					((ChoiceQuestion)currentItem.getItem().getQuestion()).setShuffle(options.isShuffle());
					break;
				}
				default: {
					log.warn("Question type not supported: " + type);
					currentItem = null;
				}
			}
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
	
	private void processDescription(String[] parts) {
		if(currentItem == null || parts.length < 2) return;
		
		String description = parts[1];
		if(StringHelper.containsNonWhitespace(description)) {
			currentItem.setDescription(description);
		}
	}
	
	private void processQuestion(String[] parts) {
		if(currentItem == null) return;
		
		Question question = currentItem.getItem().getQuestion();
		Material mat = question.getQuestion();
		
		String content = parts[1];
		Mattext matText = new Mattext(content);
		List<QTIObject> elements = new ArrayList<>(1);
		elements.add(matText);
		mat.setElements(elements);
	}
	
	private void processPoints(String[] parts) {
		if(currentItem == null) return;
		
		float points = parseFloat(parts[1], 1.0f);

		Question question = currentItem.getItem().getQuestion();
		int type = question.getType();
		if (type == Question.TYPE_MC) {
			question.setMinValue(0.0f);
			question.setMaxValue(points);
			question.setSingleCorrect(false);
			question.setSingleCorrectScore(0.0f);
		} else if (type == Question.TYPE_SC) {
			question.setSingleCorrect(true);
			question.setSingleCorrectScore(points);
		} else if(type == Question.TYPE_FIB) {
			question.setMinValue(0.0f);
			question.setMaxValue(points);
			question.setSingleCorrect(false);
			question.setSingleCorrectScore(0.0f);
		}
	}
	
	private void processChoice(String[] parts) {
		if(currentItem == null || parts.length < 2) {
			return;
		}
		
		try {
			Question question = currentItem.getItem().getQuestion();
			int type = question.getType();
			if (type == Question.TYPE_MC || type == Question.TYPE_SC) {
				float point = parseFloat(parts[0], 1.0f);
				String content = parts[1];

				ChoiceQuestion choice = (ChoiceQuestion)question;
				List<Response> choices = choice.getResponses();
				ChoiceResponse newChoice = new ChoiceResponse();
				newChoice.getContent().add(createMattext(content));
				newChoice.setCorrect(point > 0.0f);
				newChoice.setPoints(point);
				choices.add(newChoice);
			} else if(type == Question.TYPE_FIB) {
				String firstPart = parts[0].toLowerCase();
				FIBQuestion fib = (FIBQuestion)question;
				if("text".equals(firstPart) || "texte".equals(firstPart)) {
					String text = parts[1];
					
					FIBResponse response = new FIBResponse();
					response.setType(FIBResponse.TYPE_CONTENT);
					Material mat = createMaterialWithText(text);
					response.setContent(mat);
					fib.getResponses().add(response);
				} else {
					float point = parseFloat(parts[0], 1.0f);
					String correctBlank = parts[1];

					FIBResponse response = new FIBResponse();
					response.setType(FIBResponse.TYPE_BLANK);
					response.setCorrectBlank(correctBlank);
					response.setPoints(point);
					
					if(parts.length > 2) {
						String sizes = parts[2];
						String[] sizeArr = sizes.split(",");
						if(sizeArr.length >= 2) {
							int size = Integer.parseInt(sizeArr[0]);
							int maxLength = Integer.parseInt(sizeArr[1]);
							response.setSize(size);
							response.setMaxLength(maxLength);
						}	
					}
					
					fib.getResponses().add(response);
				}
			}
		} catch (NumberFormatException e) {
			log.warn("Cannot parse point for: " + parts[0] + " / " + parts[1], e);
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
	
	private Material createMaterialWithText(String text) {
		Material material = new Material();
		material.add(createMattext(text));
		return material;
	}
	
	private Mattext createMattext(String text) {
		//text is already in a CDATA
		text = text.replace("// <![CDATA[", "").replace("// ]]>", "");
		// Strip unnecessary BR tags at the beginning and the end which are added
		// automaticall by mysterious tiny code and cause problems in FIB questions. (OLAT-4363)
		// Use explicit return which create a P tag if you want a line break.
		if (text.startsWith("<br />") && text.length() > 6) text = text.substring(6);
		if (text.endsWith("<br />") && text.length() > 6) text = text.substring(0, text.length()-6);
		// Remove any conditional comments due to strange behavior in test (OLAT-4518)
		Filter conditionalCommentFilter = FilterFactory.getConditionalHtmlCommentsFilter();
		text = conditionalCommentFilter.filter(text);

		Mattext mattext = new Mattext(text);
		return mattext;
	}
}
