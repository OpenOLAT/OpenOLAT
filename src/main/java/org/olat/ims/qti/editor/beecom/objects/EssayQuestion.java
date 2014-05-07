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

import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.olat.ims.qti.editor.QTIEditHelper;
import org.olat.ims.qti.editor.beecom.parser.ParserManager;

/**
 * Initial Date:  Oct 10, 2003
 *
 * @author gnaegi
 * 
 * Comment:  
 * Representatio of an essay question type. An essay consists of a question
 * (material) and a response (response_str) element.
 * The essay question type has no respconditions at all, it can only be used
 * in the context of a survey.
 * </pre>
 */
public class EssayQuestion extends Question implements QTIObject {
	
	private static ParserManager parserManager = new ParserManager();

	public EssayQuestion() {
		super();
		setType(Question.TYPE_ESSAY);
	}
	
	/**
	 * Called by ItemParser
	 * 
	 * @param item
	 * @return
	 */
	public static EssayQuestion getInstance(Element item) {
		
		EssayQuestion instance = new EssayQuestion();

		// Parsing presentation element
		Element presentationXML = item.element("presentation");

		// Question
		Element material = (Element)presentationXML.selectSingleNode(".//material"); // export uses flow
		Material matQuestion = (Material)parserManager.parse(material);
		if (matQuestion != null) 
			instance.setQuestion(matQuestion);
	
		// Response
		List<Response> responses = instance.getResponses();
		EssayResponse response = new EssayResponse();
		Element response_str = (Element)presentationXML.selectSingleNode(".//response_str"); // export uses flow

		String ident = response_str.attribute("ident").getValue();
		response.setIdent(ident);
		
		Element render_fib = response_str.element("render_fib");
		response.setColumns(render_fib.attribute("columns"));
		response.setRows(render_fib.attribute("rows"));

		responses.add(response);
		instance.setResponses(responses);

		Element resprocessingXML = item.element("resprocessing");
		if (resprocessingXML != null) {
			// set min/max score
			Element decvar = (Element)resprocessingXML.selectSingleNode(".//decvar");
			if(decvar != null) {
				String min = decvar.attributeValue("minvalue");
				if (min != null) {
					instance.setMinValue(min);
				}
				String max = decvar.attributeValue("maxvalue");
				if (max != null) {
					if(instance.isSingleCorrect()) {
						instance.setSingleCorrectScore(max);
					}
					instance.setMaxValue(max);
				}
			}
		}
		return instance;
	}
	
	/**
	 * Render XML
	 */
	public void addToElement(Element root) {
		// Add presentation element
		Element presentationXML = root.addElement("presentation");
		presentationXML.addAttribute("label", getLable());

		// Question
		getQuestion().addToElement(presentationXML);

		EssayResponse response = this.getEssayResponse();

		Element response_str = presentationXML.addElement("response_str");
		response_str.addAttribute("ident", response.getIdent());

		Element render_fib = response_str.addElement("render_fib");
		render_fib.addAttribute("columns", String.valueOf(response.getColumns()));
		render_fib.addAttribute("rows", String.valueOf(response.getRows()));

		Element response_label = render_fib.addElement("response_label");
		response_label.addAttribute("ident", response.getIdent());
		response_label.addAttribute("rshuffle", "Yes"); // QTI default

		// No resprocessing since only used in survey mode
		
		Element resprocessingXML = root.addElement("resprocessing");
		resprocessingXML.addAttribute("scoremodel","HumanRater");

		Element outcomes = resprocessingXML.addElement("outcomes");
		Element decvar = outcomes.addElement("decvar");
		decvar.addAttribute("varname", "SCORE");
		decvar.addAttribute("vartype", "Decimal");
		decvar.addAttribute("defaultval", "0");
		decvar.addAttribute("minvalue", "" + getMinValue());
		float maxScore = QTIEditHelper.calculateMaxScore(this);
		float maxValue = getMaxValue();
		float max = maxScore > maxValue ? maxValue : maxScore;
		decvar.addAttribute("maxvalue", "" + max);
		decvar.addAttribute("cutvalue", "" + max);
		
		resprocessingXML.addElement("itemproc_extension");
		
		// hint
		if (getHintText() != null) {
			QTIEditHelper.addHintElement(root, getHintText());
		}

		// solution
		if (getSolutionText() != null) {
			QTIEditHelper.addSolutionElement(root, getSolutionText());
		}
	}
	

	/**
	 * Returns the essay response. In the QTI editor it is only possible to have one 
	 * response in the essay question type
	 * @return the essay response
	 */
	public EssayResponse getEssayResponse(){
		EssayResponse response = null;
		for (Iterator<Response> iter = getResponses().iterator(); iter.hasNext();){
			response = (EssayResponse) iter.next();
		} 
		return response;
	}

}