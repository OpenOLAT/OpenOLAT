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

package org.olat.ims.qti.process.elements;

import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.olat.ims.qti.container.ItemContext;
import org.olat.ims.qti.container.Output;
import org.olat.ims.qti.container.Variable;
import org.olat.ims.qti.container.Variables;
import org.olat.ims.qti.container.qtielements.Hint;
import org.olat.ims.qti.container.qtielements.Solution;
import org.olat.ims.qti.process.QTIHelper;

/**
 *
 */
public class QTI_respcondition {

	/**
	 * Response Condition
	 * 
	 * ims qti 1.2.1 respcondition
	 * <!ELEMENT respcondition (qticomment? , conditionvar , setvar* , displayfeedback*)>
	 * <!ELEMENT conditionvar (not | and | or | unanswered | other | varequal | varlt | varlte | 
	 *      vargt | vargte | varsubset | varinside | varsubstring | durequal | durlt | durlte | durgt | durgte)+>
	 * 
	 * <!ELEMENT setvar (#PCDATA)>
	 * <!ATTLIST setvar  %I_VarName; action     (Set | Add | Subtract | Multiply | Divide )  'Set' >
	 * mit I_VarName = varname CDATA  'SCORE'
	 * <setvar action="Set" varname="SCORE">10</setvar>
	 * 
	 * <!ELEMENT displayfeedback (#PCDATA)>
	 * <!ATTLIST displayfeedback  feedbacktype  (Response | Solution | Hint )  'Response'
	                        %I_LinkRefId; >
	 * mit I_LinkRefId = linkrefid CDATA  #REQUIRED"
	 * e.g. <displayfeedback feedbacktype = "Solution" linkrefid = "CorrectSoln"/>
	 * 
	 * ??? should be ? ? <conditionvar><or>...</or><varequal>...</varequal></conditionvar> does not make sense
	 * but 	<conditionvar>  
					<varequal respident = "Word-1">KITTENS</varequal>  
					<varequal respident = "Word-2">HATS</varequal>
				</conditionvar> makes sense, so treat members of conditionvar as children of an "and" element
	 * @param node_respcond
	 */
	public boolean process(Element el_respcond, ItemContext itc, EvalContext ect) {
		// 1. evaluate conditionvar
		// 2. if true, set variables
		//    and setCurrentDisplayFeedback (TODO: assuming there is only one displayfeedback in a respcondition)
		Variables vars;
		Element el_condVar = (Element) el_respcond.selectSingleNode("conditionvar");
		String respcondtitle = el_respcond.attributeValue("title");
		QTI_and qtiAnd = QTIHelper.getQTI_and();
		boolean fulfilled = qtiAnd.eval(el_condVar, itc, ect);
		// continue to set variables and display feedback if question was answered correctly
		if (fulfilled) {
			vars = itc.getVariables();
			List setvars = el_respcond.selectNodes("setvar");
			for (Iterator iter = setvars.iterator(); iter.hasNext();) {
				Element element = (Element) iter.next();
				String action = element.attributeValue("action");
				String varName = element.attributeValue("varname");
				if (varName == null) varName = "SCORE";
				varName.trim();
				String varVal = element.getText();
				Variable var = vars.getVariable(varName);
				if (var == null) throw new RuntimeException("var "+varName+" is in setvar, but was not declared ");
				if (action.equals("Set")) {
					var.setValue(varVal);
				} else {
					// we are doing Integer or float arithmetic
					// Add | Subtract | Multiply | Divide
					if (action.equals("Add")) {
						var.add(varVal);
					} else if (action.equals("Subtract")) {
						var.subtract(varVal);
					} else if (action.equals("Multiply")) {
						var.multiply(varVal);
					} else if (action.equals("Divide")) {
						var.divide(varVal);
					}
				}
			}
			// set displayfeedback
			//<displayfeedback feedbacktype = "Response" linkrefid = "Correct"/>
			//<!ATTLIST displayfeedback  feedbacktype  (Response | Solution | Hint )  'Response' %I_LinkRefId; >
			Output output = itc.getOutput();
			List fbs = el_respcond.selectNodes("displayfeedback");
			for (Iterator it_fbs = fbs.iterator(); it_fbs.hasNext();) {
				Element el_dispfb = (Element) it_fbs.next();
				String linkRefId = el_dispfb.attributeValue("linkrefid"); // must exist (dtd)
				String feedbacktype = el_dispfb.attributeValue("feedbacktype"); // must exist (dtd)
				Element el_resolved =
					(Element) itc.getEl_item().selectSingleNode(".//itemfeedback[@ident='" + linkRefId + "']");
				if (el_resolved == null) continue;
				if (feedbacktype.equals("Response")) {
					// additional (olat) rule:
					// we want to render the original answer again in the simple case where the respcondition was generated
					// by the olat export and contains only one varequal.
					/*
					  
					 	 <response_label ident = "2">
			     			<flow_mat>
								<material>
										<mattext texttype="text/html">...</mattext>
								</material>
                  			</flow_mat>
                		</response_label> 
					  ...
					
					 	<respcondition title="_olat_resp_feedback" continue="Yes">
							<conditionvar>
								<varequal respident="Frage6549" case="Yes">2</varequal>
							</conditionvar>
            				<displayfeedback linkrefid="2"/>
						</respcondition>
						
						In this case, it is possible (and wished) to trace the feedback back to the answer which triggered this feedback.
						Such a respcondition is identified by the title which is exactly "_olat_resp_feedback".
						
						
					 */
					Element el_chosenanswer = null;
					if (respcondtitle != null && respcondtitle.equals("_olat_resp_feedback")) {
						Element el_vareq = (Element) el_respcond.selectSingleNode(".//varequal");
						String answerident = el_vareq.getText();
						el_chosenanswer = (Element) itc.getEl_item().selectSingleNode(".//response_label[@ident='" + answerident + "']//material");
					}
					output.addItem_El_response(el_chosenanswer,el_resolved); // give the whole itemfeedback to render
				} else if (feedbacktype.equals("Solution")) {
					Element el_solution = (Element) el_resolved.selectSingleNode(".//solution");
					if (el_solution != null) output.setSolution(new Solution(el_solution));
				} else if (feedbacktype.equals("Hint")) {
					//<!ENTITY % I_FeedbackStyle " feedbackstyle  (Complete | Incremental | Multilevel | Proprietary )  'Complete'">
					Element el_hint = (Element) el_resolved.selectSingleNode(".//hint");
					output.setHint(new Hint(el_hint));
				}
			}
		}
		return fulfilled;

	}


}
