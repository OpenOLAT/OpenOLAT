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
package org.olat.ims.qti21.model.xml;

import java.util.List;

import uk.ac.ed.ph.jqtiplus.node.expression.Expression;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.item.template.declaration.TemplateDeclaration;
import uk.ac.ed.ph.jqtiplus.node.item.template.processing.SetCorrectResponse;
import uk.ac.ed.ph.jqtiplus.types.ComplexReferenceIdentifier;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.utils.QueryUtils;
import uk.ac.ed.ph.jqtiplus.value.BaseType;

/**
 * 
 * 
 * 
 * Initial date: 5 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemChecker {
	
	/**
	 * 
	 * The method check and correct the type of some variables.
	 * 
	 * @param item
	 * @return
	 */
	public static boolean checkAndCorrect(AssessmentItem item) {
		boolean allOk = true;
		allOk &= checkSetCorrectResponse(item);	
		return allOk;
	}
	
	/**
	 * responseDeclaration -> float
	 * 
	 * templateVraiable -> integer
	 * 
	 * setCorrectResponse
	 *   -> variable -> integer doesn't match float -> issue
	 * @param item
	 * @return
	 */
	private static boolean checkSetCorrectResponse(AssessmentItem item) {
		boolean allOk = true;
		
		List<SetCorrectResponse> setCorrectResponses = QueryUtils.search(SetCorrectResponse.class, item);
		for(SetCorrectResponse setCorrectResponse:setCorrectResponses) {
			Identifier responseIdentifier = setCorrectResponse.getIdentifier();
			ResponseDeclaration responseDeclaration = item.getResponseDeclaration(responseIdentifier);
			BaseType baseType = responseDeclaration.getBaseType();
			Expression expression = setCorrectResponse.getExpression();
			if(expression instanceof Variable) {
				Variable variable = (Variable)expression;
				ComplexReferenceIdentifier cpxVariableIdentifier = variable.getIdentifier();
				Identifier variableIdentifier = Identifier.assumedLegal(cpxVariableIdentifier.toString());
				TemplateDeclaration templateDeclaration = item.getTemplateDeclaration(variableIdentifier);
				if(templateDeclaration != null && !templateDeclaration.hasBaseType(baseType)) {
					templateDeclaration.setBaseType(baseType);
					allOk &= false;
				}
			}
		}
		
		return allOk;
	}

}
