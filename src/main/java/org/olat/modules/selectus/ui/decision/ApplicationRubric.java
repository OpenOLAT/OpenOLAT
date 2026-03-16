/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
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
