/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager.spi;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.DecisionRubric;

/**
 * 
 * Initial date: 27 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MinusPlusDecisionRubricProvider extends AbstractSelectionDecisionRubricProvider {

	@Override
	public String getKey() {
		return "-1-0-+1";
	}

	@Override
	public String getName() {
		return "- 0 +";
	}

	@Override
	public double getNumericalNormalizedValue(DecisionRubric rubric) {
		if(rubric != null && rubric.getIntegerValue() != null) {
			switch(rubric.getIntegerValue().intValue()) {
				case 1: return 1.0d;
				case 0: return 0.0d;
				case -1: return -1.0d;
			}
		}
		return 0;
	}

	@Override
	public FormItem createElement(DecisionRubric rubric, FormItemContainer formLayout, FormUIFactory uifactory) {
		String[] theKeys = new String[]{ "-1", "0", "1"};
		String[] theValues = new String[]{ "-", "0", "+"};
		return super.createElement(rubric, theKeys, theValues, formLayout, uifactory);
	}
}
