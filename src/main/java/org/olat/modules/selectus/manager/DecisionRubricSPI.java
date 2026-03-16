/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;

import org.olat.modules.selectus.model.DecisionRubric;

/**
 * 
 * Initial date: 18 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface DecisionRubricSPI {

	public String getKey();
	
	public String getName();

	public FormItem createElement(DecisionRubric rubric, FormItemContainer formLayout, FormUIFactory uifactory);
	
	public void commitValue(DecisionRubric rubric, FormItem item);
	

	public String getValue(DecisionRubric rubric);
	
	/**
	 * Used to calculate the sum
	 * 
	 * @param rubric
	 * @return A double between 0 and 1
	 */
	public double getNumericalNormalizedValue(DecisionRubric rubric);

}
