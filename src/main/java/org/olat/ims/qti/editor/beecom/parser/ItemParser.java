/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.ims.qti.editor.beecom.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.olat.core.util.CodeHelper;
import org.olat.ims.qti.editor.beecom.objects.ChoiceQuestion;
import org.olat.ims.qti.editor.beecom.objects.Control;
import org.olat.ims.qti.editor.beecom.objects.Duration;
import org.olat.ims.qti.editor.beecom.objects.EssayQuestion;
import org.olat.ims.qti.editor.beecom.objects.FIBQuestion;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.QTIObject;
import org.olat.ims.qti.editor.beecom.objects.QTIXMLWrapper;
import org.olat.ims.qti.editor.beecom.objects.Question;

/**
 * @author rkulow
 *
 */
public class ItemParser implements IParser {

	public static final String ITEM_PREFIX_SCQ = "QTIEDIT:SCQ:";
	public static final String ITEM_PREFIX_MCQ = "QTIEDIT:MCQ:";
	public static final String ITEM_PREFIX_FIB = "QTIEDIT:FIB:";
	public static final String ITEM_PREFIX_ESSAY = "QTIEDIT:ESSAY:";
	public static final String ITEM_PREFIX_KPRIM = "QTIEDIT:KPRIM:";
	
	public static final List<String> OO_ITEM_PREFIX = List.of(ITEM_PREFIX_SCQ, ITEM_PREFIX_MCQ, ITEM_PREFIX_FIB, ITEM_PREFIX_ESSAY, ITEM_PREFIX_KPRIM);
	
	private ParserManager parserManager = new ParserManager();

	@Override
	public Object parse(Element element) {
		Item item = new Item();
		Attribute tmp = element.attribute("ident");
		if (tmp != null)
			item.setIdent(tmp.getValue());
		else
			item.setIdent("" + CodeHelper.getRAMUniqueID());
		
		tmp = element.attribute("title");
		if (tmp != null)
			item.setTitle(tmp.getValue());
		
		tmp = element.attribute("label");
		if (tmp != null)
			item.setLabel(tmp.getValue());
		
		tmp = element.attribute("maxattempts");
		if (tmp != null) {
			try {
				item.setMaxattempts(Integer.parseInt(tmp.getValue()));
			} catch (NumberFormatException nfe) {
				item.setMaxattempts(0);
			}
		}

		// if editor can't handle type of item, just keep raw XML
		if (!(item.getIdent().startsWith(ITEM_PREFIX_SCQ)
		|| item.getIdent().startsWith(ITEM_PREFIX_MCQ)
		|| item.getIdent().startsWith(ITEM_PREFIX_FIB)
		|| item.getIdent().startsWith(ITEM_PREFIX_ESSAY)
		|| item.getIdent().startsWith(ITEM_PREFIX_KPRIM))) {
			item.setRawXML(new QTIXMLWrapper(element));
			return item;
		}
		
		// exported olat surveys don't have the correct essay prefix. Search
		// for render_fib that contains rows attribute and convert them to essay
		if (item.getIdent().startsWith(ITEM_PREFIX_FIB)
			&& element.selectNodes(".//render_fib[@rows]").size() > 0){
			item.setIdent(item.getIdent().replaceFirst("FIB", "ESSAY"));
		}

		
		// DURATION
		Duration duration =
			(Duration) parserManager.parse(element.element("duration"));
		item.setDuration(duration);

		// CONTROLS
		List itemcontrolsXML = element.elements("itemcontrol");
		List itemcontrols = new ArrayList();
		for (Iterator i = itemcontrolsXML.iterator(); i.hasNext();) {
			itemcontrols.add(parserManager.parse((Element) i.next()));
		}
		if (itemcontrols.size() == 0) {
			itemcontrols.add(new Control());
		}
		item.setItemcontrols(itemcontrols);

		//OBJECTIVES
		Element mattext = (Element)element.selectSingleNode("./objectives/material/mattext");
		if (mattext != null)
			item.setObjectives(mattext.getTextTrim());
		
		// QUESTIONS
		if (item.getIdent().startsWith(ITEM_PREFIX_SCQ))
			item.setQuestion(ChoiceQuestion.getInstance(element));
		else if (item.getIdent().startsWith(ITEM_PREFIX_MCQ))
			item.setQuestion(ChoiceQuestion.getInstance(element));
		else if (item.getIdent().startsWith(ITEM_PREFIX_FIB))
			item.setQuestion(FIBQuestion.getInstance(element));
		else if (item.getIdent().startsWith(ITEM_PREFIX_ESSAY))
			item.setQuestion(EssayQuestion.getInstance(element));
		else if (item.getIdent().startsWith(ITEM_PREFIX_KPRIM))
			item.setQuestion(ChoiceQuestion.getInstance(element));
		
		// FEEDBACKS
		List feedbacksXML = element.elements("itemfeedback");
		List feedbacks = new ArrayList();
		item.setItemfeedbacks(feedbacks);
		Question question = item.getQuestion();
		for (Iterator i = feedbacksXML.iterator(); i.hasNext();) {
			Element el_feedback = (Element)i.next();
			if (el_feedback.element("solution") != null) { // fetch solution
				Element el_solution = el_feedback.element("solution");
				question.setSolutionText(getMaterialAsString(el_solution));
			} else if (el_feedback.element("hint") != null) { // fetch hint
				Element el_hint = el_feedback.element("hint");
				question.setHintText(getMaterialAsString(el_hint));
			} else {
				QTIObject tmpObj = (QTIObject) parserManager.parse(el_feedback);
				if (tmpObj != null) feedbacks.add(tmpObj);
			}
		}

		
		return item;
	}

	private String getMaterialAsString(Element el_root) {
		StringBuilder result = new StringBuilder();
		List materials = el_root.selectNodes(".//mattext");
		for (Iterator iter = materials.iterator(); iter.hasNext();) {
			Element el_mattext = (Element) iter.next();
			result.append(el_mattext.getTextTrim() + "\n");
		}
		return result.toString();
	}
}
