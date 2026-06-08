/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.decision;

import org.olat.core.gui.components.form.flexible.FormItem;

import org.olat.modules.selectus.manager.DecisionRubricSPI;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.DecisionRubric;
import org.olat.modules.selectus.model.DecisionRubricDefinition;

/**
 * 
 * Initial date: 18 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationRubric {
	
	private DecisionRubric rubric;
	private final ApplicationLight application;
	private final DecisionRubricDefinition definition;
	private final DecisionRubricSPI decisionRubricSpi;
	private final ApplicationRubricsRow applicationRubricsRow;
	
	private FormItem element;

	public ApplicationRubric(DecisionRubricDefinition definition, ApplicationLight application,
			ApplicationRubricsRow applicationRubricsRow, DecisionRubricSPI decisionRubricSpi) {
		this.definition = definition;
		this.application = application;
		this.decisionRubricSpi = decisionRubricSpi;
		this.applicationRubricsRow = applicationRubricsRow;
	}

	public DecisionRubric getRubric() {
		return rubric;
	}

	public void setRubric(DecisionRubric rubric) {
		this.rubric = rubric;
	}
	
	public ApplicationLight getApplication() {
		return application;
	}
	
	public DecisionRubricDefinition getDefinition() {
		return definition;
	}
	
	public DecisionRubricSPI getDecisionRubricSpi() {
		return decisionRubricSpi;
	}
	
	public ApplicationRubricsRow getApplicationRubricsRow() {
		return applicationRubricsRow;
	}
	
	public String getValue() {
		return decisionRubricSpi.getValue(rubric);
	}
	
	public double getNumericalNormalizedValue() {
		return decisionRubricSpi.getNumericalNormalizedValue(rubric);
	}

	public FormItem getFormItem() {
		return element;
	}

	public void setFormItem(FormItem element) {
		this.element = element;
	}
}
