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
package org.olat.ims.qti21.manager;

import java.util.Collections;
import java.util.Map;

import org.olat.ims.qti21.manager.extensions.MaximaOperator;

import uk.ac.ed.ph.jqtiplus.ExtensionNamespaceInfo;
import uk.ac.ed.ph.jqtiplus.JqtiExtensionPackage;
import uk.ac.ed.ph.jqtiplus.JqtiLifecycleEventType;
import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.expression.ExpressionParent;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.CustomOperator;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.CustomInteraction;

/**
 * 
 * Initial date: 3 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenOLATExtensionPackage implements JqtiExtensionPackage<OpenOLATExtensionPackage> {

	public OpenOLATExtensionPackage() {
		//
	}

	@Override
	public String getDisplayName() {
		return "MAXIMA Extension pack";
	}

	@Override
	public Map<String, ExtensionNamespaceInfo> getNamespaceInfoMap() {
		return Collections.emptyMap();
	}

	@Override
	public boolean implementsCustomOperator(String operatorClassName) {
		return MaximaOperator.class.getName().equals(operatorClassName);
	}
	
	@Override
	public CustomOperator<OpenOLATExtensionPackage> createCustomOperator(ExpressionParent expressionParent, String operatorClassName) {
		return new MaximaOperator(expressionParent);
	}

	@Override
	public boolean implementsCustomInteraction(String interactionClassName) {
		return false;
	}

	@Override
	public CustomInteraction<OpenOLATExtensionPackage> createCustomInteraction(QtiNode parentObject, String interactionClassName) {
		return null;
	}
	
    //------------------------------------------------------------------------

    @Override
    public void lifecycleEvent(final Object source, final JqtiLifecycleEventType eventType) {
        //
    }
}
