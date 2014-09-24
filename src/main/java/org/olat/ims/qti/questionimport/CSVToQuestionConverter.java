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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.ims.qti.editor.QTIEditHelper;
import org.olat.ims.qti.editor.beecom.objects.ChoiceQuestion;
import org.olat.ims.qti.editor.beecom.objects.ChoiceResponse;
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
	
	private static final OLog log = Tracing.createLoggerFor(CSVToQuestionConverter.class);

	private Item currentItem;
	private final List<Item> items = new ArrayList<>();
	private Translator translator;
	
	public CSVToQuestionConverter(Translator translator) {
		this.translator = translator;
	}
	
	public List<Item> getItems() {
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
			default: processChoice(parts);
		}
	}
	
	private void processType(String[] parts) {
		if(currentItem != null) {
			items.add(currentItem);
		}
		
		String type = parts[1].toLowerCase();
		switch(type) {
			case "fib": {
				currentItem = QTIEditHelper.createFIBItem(translator);
				((FIBQuestion)currentItem.getQuestion()).getResponses().clear();
				break;
			}
			case "mc": {
				currentItem = QTIEditHelper.createMCItem(translator);
				((ChoiceQuestion)currentItem.getQuestion()).getResponses().clear();
				break;
			}
			case "sc": {
				currentItem = QTIEditHelper.createSCItem(translator);
				((ChoiceQuestion)currentItem.getQuestion()).getResponses().clear();
				break;
			}
			default: {
				log.warn("Question type not supported: " + type);
				currentItem = null;
			}
		}
	}
	
	private void processTitle(String[] parts) {
		if(currentItem == null) return;
		
		String title = parts[1];
		currentItem.setTitle(title);
	}
	
	private void processDescription(String[] parts) {
		String objectives = parts[1];
		currentItem.setObjectives(objectives);
	}
	
	private void processQuestion(String[] parts) {
		Question question = currentItem.getQuestion();
		Material mat = question.getQuestion();
		
		String content = parts[1];
		Mattext matText = new Mattext(content);
		List<QTIObject> elements = new ArrayList<QTIObject>(1);
		elements.add(matText);
		mat.setElements(elements);
	}
	
	private void processPoints(String[] parts) {
		String pointsStr = parts[1];
		float points = Float.parseFloat(pointsStr);
		
		int type = currentItem.getQuestion().getType();
		Question question = currentItem.getQuestion();
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
			int type = currentItem.getQuestion().getType();
			if (type == Question.TYPE_MC || type == Question.TYPE_SC) {
				float point = Float.parseFloat(parts[0]);
				String content = parts[1];

				ChoiceQuestion question = (ChoiceQuestion)currentItem.getQuestion();
				List<Response> choices = question.getResponses();
				ChoiceResponse newChoice = new ChoiceResponse();
				newChoice.getContent().add(createMattext(content));
				newChoice.setCorrect(point > 0.0f);
				newChoice.setPoints(point);
				choices.add(newChoice);
			} else if(type == Question.TYPE_FIB) {
				String firstPart = parts[0].toLowerCase();
				FIBQuestion fib = (FIBQuestion)currentItem.getQuestion();
				if("text".equals(firstPart) || "texte".equals(firstPart)) {
					String text = parts[1];
					
					FIBResponse response = new FIBResponse();
					response.setType(FIBResponse.TYPE_CONTENT);
					Material mat = createMaterialWithText(text);
					response.setContent(mat);
					fib.getResponses().add(response);
				} else {
					float point = Float.parseFloat(parts[0]);
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
