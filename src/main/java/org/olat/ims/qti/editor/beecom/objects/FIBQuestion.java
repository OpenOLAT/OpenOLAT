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

package org.olat.ims.qti.editor.beecom.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.olat.ims.qti.editor.QTIEditHelper;
import org.olat.ims.qti.editor.beecom.parser.ParserManager;

/**
 * @author rkulow
 *
 */
public class FIBQuestion extends Question implements QTIObject {
	
	private static ParserManager parserManager = new ParserManager();

	public FIBQuestion() {
		super();
		setType(Question.TYPE_FIB);
	}
	
	/**
	 * Called by ItemParser
	 * 
	 * @param item
	 * @return
	 */
	public static FIBQuestion getInstance(Element item) {
		FIBQuestion instance = new FIBQuestion();

		Element presentationXML = item.element("presentation");
		List elementsXML =  presentationXML.element("flow").elements();
		List<Response> responses = instance.getResponses();
		Element el_resprocessing = item.element("resprocessing");
	
		for(Iterator i = elementsXML.iterator(); i.hasNext();) {
			Element content = (Element)i.next();	
			FIBResponse fibresponse = new FIBResponse();
			String name = content.getName();
			if(name.equalsIgnoreCase("material")) {
				Material mat = (Material)parserManager.parse(content);
				if (mat != null)
				fibresponse.setContent(mat);
				fibresponse.setType(FIBResponse.TYPE_CONTENT);
				responses.add(fibresponse);
			} else if (name.equalsIgnoreCase("response_str")) {
				String ident = content.attribute("ident").getValue();
				Element render_fib = content.element("render_fib");
				content.element("render_fib").element("flow_label");
				fibresponse.setType(FIBResponse.TYPE_BLANK);
				fibresponse.setIdent(ident);
				fibresponse.setSizeFromColumns(render_fib.attribute("columns"));
				fibresponse.setMaxLengthFromMaxChar(render_fib.attribute("maxchars"));
				List el_varequals = el_resprocessing.selectNodes(".//varequal[@respident='" + ident + "']");
				List<String> processedSolutions = new ArrayList<>(); // list of already process strings
				if (el_varequals != null) {
					String correctBlank = "";
					String correctBlankCaseAttribute = "No";
					for (Iterator iter = el_varequals.iterator(); iter.hasNext();) {
						Element el_varequal = (Element)iter.next();
						String solution = el_varequal.getTextTrim();
						if (!processedSolutions.contains(solution)) {
							// Solutions are there twice because of the mastery feedback.
							// Only process solutions once.
							correctBlank = correctBlank + solution;
							if (iter.hasNext()) correctBlank = correctBlank + ";";
							correctBlankCaseAttribute = el_varequal.attributeValue("case");
							processedSolutions.add(solution);
						}
					}
					if (correctBlank.endsWith(";")) correctBlank = correctBlank.substring(0, correctBlank.length() - 1);
					fibresponse.setCorrectBlank(correctBlank);
					fibresponse.setCaseSensitive(correctBlankCaseAttribute);
				}
				responses.add(fibresponse);
			}
		}
		
		Element resprocessingXML = item.element("resprocessing");
		if (resprocessingXML != null) {
			
			List respconditions = resprocessingXML.elements("respcondition");
			Map<String,Float> points = QTIEditHelper.fetchPoints(respconditions, instance.getType());
		
			// postprocessing choices
			for(Iterator<Response> i = responses.iterator(); i.hasNext();) {
				FIBResponse fibResp = (FIBResponse)i.next();		
				Float fPoints = points.get(fibResp.getIdent());
				if (fPoints != null) {
					fibResp.setPoints(fPoints.floatValue());
					fibResp.setCorrect(true);
				} 
			}	
	
			// if does not contain any ANDs, assume only one combination
			// of answers is possible (which sets points by a setvar action="Set")
			if (resprocessingXML.selectNodes(".//setvar[@action='Add']").size() == 0) {
				instance.setSingleCorrect(true);
				Collection<Float> values = points.values();
				if (values.size() > 0)
					instance.setSingleCorrectScore((values.iterator().next()).floatValue());
			} else {
				instance.setSingleCorrect(false);
			}
	
			// set min/max score
			QTIEditHelper.configureMinMaxScore(instance,
			 (Element)resprocessingXML.selectSingleNode(".//decvar"));
		}		
		return instance;
	}
	
	/**
	 * Render XML
	 */
	public void addToElement(Element root) {
		Element presentationXML = root.addElement("presentation");
		presentationXML.addAttribute("label", "notset");
		// presentation
		Element flowXML = presentationXML.addElement("flow");
		for (Iterator<Response> i = getResponses().iterator(); i.hasNext();) {
			FIBResponse fibcontent = (FIBResponse) i.next();
			if (fibcontent.getType().equals(FIBResponse.TYPE_CONTENT)) {
				Material mat = fibcontent.getContent();
				if(mat.getElements().isEmpty()) {
					//the flow cannot be empty -> add dummy material element
					flowXML.addElement("material").addElement("mattext").addCDATA("");
				} else {
					mat.addToElement(flowXML);
				}
			} else if (fibcontent.getType().equals(FIBResponse.TYPE_BLANK)) {
				Element response_str = flowXML.addElement("response_str");
				response_str.addAttribute("ident", fibcontent.getIdent());
				response_str.addAttribute("rcardinality", "Single");

				Element render_fib = response_str.addElement("render_fib");
				render_fib.addAttribute("columns", String.valueOf(fibcontent.getSize()));
				render_fib.addAttribute("maxchars", String.valueOf(fibcontent.getMaxLength()));

				Element flow_label = render_fib.addElement("flow_label");
				flow_label.addAttribute("class", "Block");
				Element response_lable = flow_label.addElement("response_label");
				response_lable.addAttribute("ident", fibcontent.getIdent());
				response_lable.addAttribute("rshuffle", "Yes");
			}
		}

		// resprocessing
		Element resprocessingXML = root.addElement("resprocessing");

		// outcomes
		Element decvar =
			resprocessingXML.addElement("outcomes").addElement("decvar");
		decvar.addAttribute("varname", "SCORE");
		decvar.addAttribute("vartype", "Decimal");
		decvar.addAttribute("defaultval", "0");
		decvar.addAttribute("minvalue", "" + getMinValue());
		float maxScore = QTIEditHelper.calculateMaxScore(this);
		maxScore = maxScore > getMaxValue() ? getMaxValue() : maxScore;
		decvar.addAttribute("maxvalue", "" + maxScore);
		decvar.addAttribute("cutvalue", "" + maxScore);
		
		//respcondition
		// correct
	
		if (isSingleCorrect()){
			buildRespconditionFIBSingle(resprocessingXML);
			buildRespcondition_fail(resprocessingXML, true);
		} else {
			buildRespconditionFIBMulti(resprocessingXML);
			buildRespcondition_fail(resprocessingXML, false);
		}
		
		// hint
		if (getHintText() != null)
			QTIEditHelper.addHintElement(root, getHintText());
		
		// solution
		if (getSolutionText() != null)
			QTIEditHelper.addSolutionElement(root, getSolutionText());

		// Feedback for all other cases eg. none has been answered at all
		Element incorrect = resprocessingXML.addElement("respcondition");
		incorrect.addAttribute("title", "Fail");
		incorrect.addAttribute("continue", "Yes");
		incorrect.addElement("conditionvar").addElement("other");
		Element setvar = incorrect.addElement("setvar");
		setvar.addAttribute("varname", "SCORE");
		setvar.addAttribute("action", "Set");
		setvar.setText("0");
		QTIEditHelper.addFeedbackFail(incorrect);
		QTIEditHelper.addFeedbackHint(incorrect);
		QTIEditHelper.addFeedbackSolution(incorrect);
	}
	
	/**
	 * Build mastery respcondition for FIB in all-banks-must-be-correct mode. Adds
	 * one respcondition in which all blanks must be answered correctly. This 
	 * respcondition uses the mastery feedback.
	 * @param resprocessingXML
	 */
	private void buildRespconditionFIBSingle(Element resprocessingXML) {
		Element correct = resprocessingXML.addElement("respcondition");
		correct.addAttribute("title", "Mastery");
		correct.addAttribute("continue", "Yes");

		Element conditionvar = correct.addElement("conditionvar");
		Element and = conditionvar.addElement("and");
		for (Iterator<Response> i = getResponses().iterator(); i.hasNext();) {
			FIBResponse fib = (FIBResponse) i.next();
			if (fib.getType().equals(FIBResponse.TYPE_BLANK)) {
				String[] correctFIBs = fib.getCorrectBlank().split(";");
				Element or = and.addElement("or");
				for (int j = 0; j < correctFIBs.length; j++) {
					Element varequal = or.addElement("varequal");
					varequal.addAttribute("respident", fib.getIdent());
					varequal.addAttribute("case", fib.getCaseSensitive());
					if (correctFIBs[j].length() > 0)
						varequal.addCDATA(correctFIBs[j]);
				}
			}
		}
		
		Element setvar = correct.addElement("setvar");
		setvar.addAttribute("varname", "SCORE");
		setvar.addAttribute("action", "Set");
		setvar.setText("" + getSingleCorrectScore());

		// Use mastery feedback
		QTIEditHelper.addFeedbackMastery(correct);

		// remove whole respcondition if empty
		if (and.element("or") == null)
			resprocessingXML.remove(correct);
	}		


	/**
	 * Build mastery respconditions for FIB in points-per-blank mode. Adds respconditions
	 * for every single blank and a respcondition in case of all blanks answered 
	 * correctly that uses the mastery feedback.
	 * @param resprocessingXML
	 */
	private void buildRespconditionFIBMulti(Element resprocessingXML) {
		for (Iterator<Response> i = getResponses().iterator(); i.hasNext();) {
			FIBResponse fib = (FIBResponse) i.next();
			if (!fib.getType().equals(FIBResponse.TYPE_BLANK)) continue;
			float points = fib.getPoints();
			if (points == 0) continue;
			
			Element correct = resprocessingXML.addElement("respcondition");
			correct.addAttribute("continue", "Yes");
			if (points > 0)
				correct.addAttribute("title", "Mastery");
			else
				//doesn't make much sense, but maybe the user has some fancy
				// ideas...
				correct.addAttribute("title", "Fail");  

			Element or = correct.addElement("conditionvar").addElement("or");
			String[] correctFIBs = fib.getCorrectBlank().split(";");
			for (int j = 0; j < correctFIBs.length; j++) {
				Element varequal = or.addElement("varequal");
				varequal.addAttribute("respident", fib.getIdent());
				varequal.addAttribute("case", fib.getCaseSensitive());
				if (correctFIBs[j].length() > 0)
					varequal.addCDATA(correctFIBs[j]);
			}
			Element setvar = correct.addElement("setvar");
			setvar.addAttribute("varname", "SCORE");
			setvar.addAttribute("action", "Add");
			setvar.setText("" + points);
		}
		
		// Resp condition for feedback mastery:
		// all response with points>0 must be selected
		Element respcondition_correct = resprocessingXML.addElement("respcondition");
		respcondition_correct.addAttribute("title", "Mastery");
		respcondition_correct.addAttribute("continue", "Yes");		
		Element conditionvar = respcondition_correct.addElement("conditionvar");
		Element and = conditionvar.addElement("and");

		for (Iterator<Response> i = getResponses().iterator(); i.hasNext();) {
			FIBResponse tmpResponse = (FIBResponse) i.next();
			if (!tmpResponse.getType().equals(FIBResponse.TYPE_BLANK)) continue;
			String[] correctFIBs = tmpResponse.getCorrectBlank().split(";");
			Element or = and.addElement("or");
			for (int j = 0; j < correctFIBs.length; j++) {
				Element varequal = or.addElement("varequal");
				varequal.addAttribute("respident", tmpResponse.getIdent());
				varequal.addAttribute("case", tmpResponse.getCaseSensitive());
				if (correctFIBs[j].length() > 0)
					varequal.addCDATA(correctFIBs[j]);
			} // for loop
		} // for loop

		// Use mastery feedback
		QTIEditHelper.addFeedbackMastery(respcondition_correct);

		// remove whole respcondition if empty
		if (and.element("or") == null)
			resprocessingXML.remove(respcondition_correct);
	}		


	/**
	 * Build fail resprocessing: Adjust score to 0 (if single correct mode) 
	 * and set hints, solutions and fail feedback when any blank is answered 
	 * wrong
	 * 
	 * @param resprocessingXML
	 * @param isSingleCorrect
	 */
	private void buildRespcondition_fail(Element resprocessingXML, boolean isSingleCorrect) {
		// build 
		Element respcondition_fail = resprocessingXML.addElement("respcondition");
		respcondition_fail.addAttribute("title", "Fail");
		respcondition_fail.addAttribute("continue", "Yes");		
		Element conditionvar = respcondition_fail.addElement("conditionvar");
		Element or = conditionvar.addElement("or");

		for (Iterator<Response> i = getResponses().iterator(); i.hasNext();) {
			FIBResponse tmpResponse = (FIBResponse) i.next();
			if (!tmpResponse.getType().equals(FIBResponse.TYPE_BLANK)) {
				continue;
			}
			
			String[] correctFIBs = tmpResponse.getCorrectBlank().split(";");
			if(correctFIBs.length > 1) {
				Element not = or.addElement("not");
				Element orVal = not.addElement("or");
				for (int j = 0; j < correctFIBs.length; j++) {
					String correctFIB = correctFIBs[j];
					if (correctFIB.length() > 0) {
						Element varequal = orVal.addElement("varequal");
						varequal.addAttribute("respident", tmpResponse.getIdent());
						varequal.addAttribute("case", tmpResponse.getCaseSensitive());
						varequal.addCDATA(correctFIB);
					}
				} // for loop correct FIB
			}
		} // for loop

		if (isSingleCorrect){
			Element setvar = respcondition_fail.addElement("setvar");
			setvar.addAttribute("varname", "SCORE");
			setvar.addAttribute("action", "Set");
			setvar.addText("0");
		}

		// Use fail feedback, hints and solutions
		QTIEditHelper.addFeedbackFail(respcondition_fail);
		QTIEditHelper.addFeedbackHint(respcondition_fail);
		QTIEditHelper.addFeedbackSolution(respcondition_fail);
		
		// remove whole respcondition if empty
		if (or.element("not") == null)
			resprocessingXML.remove(respcondition_fail);
	}
}