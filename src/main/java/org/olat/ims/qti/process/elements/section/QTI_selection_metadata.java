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

package org.olat.ims.qti.process.elements.section;

import org.dom4j.Element;
import org.olat.ims.qti.process.elements.ExpressionBuilder;

/**
 * @author Felix Jost
 */
public class QTI_selection_metadata implements ExpressionBuilder {

	/**
	 * Constructor for QTI_and_selection.
	 */
	public QTI_selection_metadata() {
		super();
	}

	/**
	 * <!ELEMENT selection_metadata (#PCDATA)>
	 * <!ATTLIST selection_metadata  %I_Mdname; %I_Mdoperator; >
	 * <!ENTITY % I_Mdoperator " mdoperator  (EQ | NEQ | LT | LTE | GT | GTE )  #REQUIRED">
	 * <!ENTITY % I_Mdname " mdname CDATA  #REQUIRED">
	 * 
	 * e.g itemmetadata/qtimetadata/qtimetadatafield[fieldlabel[text()='qmd_dificulty'] and fieldentry[text()='4']]
	 * 
	 * @see org.olat.qti.process.elements.ExpressionBuilder#buildXPathExpression(org.dom4j.Element, java.lang.StringBuilder)
	 */
	public void buildXPathExpression(Element el_selectionElement, StringBuilder expr,boolean not_switch,boolean use_switch) {
		/* Attributes:
		mdname (mandatory). Identifies the IMS QTI-specific or IMS Meta-data field that is to be used for the selection rule. No validation check is made in the instance on the existence or otherwise of this field.
		Data-type = string (1-64 chars).
		mdoperator (mandatory with selection from the enumerated list of: EQ, NEQ, LT, LTE, GT, GTE). Identifies the nature of the meta-data field comparison that is to be applied.
		Data-type = string (1-16 chars). 
		Elements: None.
		Example:
		<selection> 
			<selection_metadata mdname="qmd_timelimit" mdoperator="LTE">5</selection_metadata>
		</selection>
		*/
		String mdName= el_selectionElement.attributeValue("mdname");
		String mdOperator= el_selectionElement.attributeValue("mdoperator");
		String val= el_selectionElement.getText();
		expr.append("itemmetadata/qtimetadata/qtimetadatafield");

		/* 
		 * 
		 * // xpath =  "//item [
		 * 	---->					itemmetadata/qtimetadata/qtimetadatafield 
		 * 	---->						[fieldlabel [text()='qmd_dificulty']	and fieldentry[text()='4'] ]
		 * 							 or 
		 * 						itemmetadata/qtimetadata/qtimetadatafield
		 * 							[fieldlabel [text()='qmd_author'] 		and fieldentry[text()='felix'] ] 
		 * 						]"
		 */

		// if the not was transmitted till here, switch the operators now: we need to do this since xpath does not know "not" in the logic sense.
		if (not_switch) {
			if (mdOperator.equals("GTE"))
				mdOperator= "LT"; // greater or equals becomes less than
			if (mdOperator.equals("LT"))
				mdOperator= "GTE";
			if (mdOperator.equals("GT"))
				mdOperator= "LTE";
			if (mdOperator.equals("LTE"))
				mdOperator= "GT";
			if (mdOperator.equals("EQ"))
				mdOperator= "NEQ";

		}
		if (mdOperator.equals("EQ")) { // equals
			expr.append(
				"[fieldlabel[text()='" + mdName + "'] and fieldentry[text()='" + val + "']]");
		}
		else if (mdOperator.equals("NEQ")) { // not equals
			expr.append(
				"[fieldlabel[text()='" + mdName + "'] and fieldentry[text()!='" + val + "']]");
		}
		else if (mdOperator.equals("GT")) { // greater than, assuming int values -> round(text())
			expr.append(
				"[fieldlabel[text()='"
					+ mdName
					+ "'] and fieldentry[number(text()) >'"
					+ val
					+ "']]");
		}
		else if (mdOperator.equals("GTE")) { // greater than, assuming int values -> round(text())
			expr.append(
				"[fieldlabel[text()='"
					+ mdName
					+ "'] and fieldentry[number(text()) >='"
					+ val
					+ "']]");
		}
		else if (mdOperator.equals("LT")) { // greater than, assuming int values -> round(text())
			expr.append(
				"[fieldlabel[text()='"
					+ mdName
					+ "'] and fieldentry[number(text()) <'"
					+ val
					+ "']]");
		}
		else if (mdOperator.equals("LTE")) { // greater than, assuming int values -> round(text())
			expr.append(
				"[fieldlabel[text()='"
					+ mdName
					+ "'] and fieldentry[number(text()) <='"
					+ val
					+ "']]");
		}
		//else // no else because of xml validation with dtd
	}

}
