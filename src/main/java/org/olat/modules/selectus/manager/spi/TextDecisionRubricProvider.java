/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager.spi;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.util.CodeHelper;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.manager.DecisionRubricSPI;
import org.olat.modules.selectus.model.DecisionRubric;

/**
 * 
 * Initial date: 18 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TextDecisionRubricProvider implements DecisionRubricSPI {

	@Override
	public String getKey() {
		return "text";
	}

	@Override
	public String getName() {
		return "Text";
	}

	@Override
	public String getValue(DecisionRubric rubric) {
		return rubric == null ? null : rubric.getStringValue();
	}

	@Override
	public double getNumericalNormalizedValue(DecisionRubric rubric) {
		return 0.0d;
	}

	@Override
	public FormItem createElement(DecisionRubric rubric, FormItemContainer formLayout, FormUIFactory uifactory) {
		String val = rubric == null ? "" : rubric.getStringValue();
		return uifactory.addTextElement("rub_" + CodeHelper.getRAMUniqueID(), null, 256, val, formLayout);
	}
	
	@Override
	public void commitValue(DecisionRubric rubric, FormItem item) {
		if(item instanceof TextElement) {
			TextElement textEl = (TextElement)item;
			rubric.setStringValue(textEl.getValue());
		}
	}
}
