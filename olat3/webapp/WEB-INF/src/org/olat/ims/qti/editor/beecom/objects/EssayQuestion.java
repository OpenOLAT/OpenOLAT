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
* <p>
*/ 

package org.olat.ims.qti.editor.beecom.objects;

import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
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
		List responses = instance.getResponses();
		EssayResponse response = new EssayResponse();
		Element response_str = (Element)presentationXML.selectSingleNode(".//response_str"); // export uses flow

		String ident = response_str.attribute("ident").getValue();
		response.setIdent(ident);
		
		Element render_fib = response_str.element("render_fib");
		response.setColumns(render_fib.attribute("columns"));
		response.setRows(render_fib.attribute("rows"));

		responses.add(response);
		instance.setResponses(responses);

		// No resprocessing since used only in survey mode

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
	}
	

	/**
	 * Returns the essay response. In the QTI editor it is only possible to have one 
	 * response in the essay question type
	 * @return the essay response
	 */
	public EssayResponse getEssayResponse(){
		EssayResponse response = null;
		for (Iterator iter = getResponses().iterator(); iter.hasNext();){
			response = (EssayResponse) iter.next();
		} 
		return response;
	}

}