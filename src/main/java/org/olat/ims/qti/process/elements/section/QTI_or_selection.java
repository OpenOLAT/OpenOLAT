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

import java.util.List;

import org.dom4j.Element;
import org.olat.ims.qti.process.QTIHelper;
import org.olat.ims.qti.process.elements.ExpressionBuilder;

/**
 * enclosing_type Description:<br>
 * @author Felix Jost
 */
public class QTI_or_selection implements ExpressionBuilder {

	/**
	 * Constructor for QTI_and_selection.
	 */
	public QTI_or_selection() {
		super();
	}

	/**
	 * @see org.olat.qti.process.elements.ExpressionBuilder#buildXPathExpression(org.dom4j.Element, java.lang.StringBuilder)
	 */
	public void buildXPathExpression(Element selectionElement, StringBuilder expr, boolean not_switch, boolean use_switch) {
		if (use_switch && not_switch) { // treat this and node as an "or
			ExpressionBuilder eb = QTIHelper.getExpressionBuilder("and_selection");
			eb.buildXPathExpression(selectionElement, expr, not_switch, false);
		}
		else {
			List elems = selectionElement.elements();
			int size = elems.size(); // dtd: >0
			expr.append("(");
			for (int i = 0; i < size; i++) {
				Element child = (Element)elems.get(i);
				String name = child.getName();
				ExpressionBuilder eb = QTIHelper.getExpressionBuilder(name);
				eb.buildXPathExpression(child, expr, not_switch, true);
				if (i < size -1) expr.append(" or ");
			}
			expr.append(")");
		}
	}

}
