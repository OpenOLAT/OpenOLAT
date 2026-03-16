/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.app_wizard;

import org.olat.core.gui.components.form.flexible.FormItem;

import org.olat.modules.selectus.model.ApplicationAttribute;
import org.olat.modules.selectus.model.PositionAttributeDefinition;

/**
 * 
 * Initial date: 10 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationAttributeWithDefinition {
	
	private final PositionAttributeDefinition definition;
	
	private ApplicationAttribute value;
	
	private FormItem primaryItem;
	private FormItem SecondaryItem;
	
	public ApplicationAttributeWithDefinition(PositionAttributeDefinition definition, ApplicationAttribute value) {
		this.definition = definition;
		this.value = value;
	}
	
	public PositionAttributeDefinition getDefinition() {
		return definition;
	}

	public ApplicationAttribute getValue() {
		return value;
	}

	public void setValue(ApplicationAttribute value) {
		this.value = value;
	}

	public FormItem getPrimaryItem() {
		return primaryItem;
	}

	public void setPrimaryItem(FormItem primaryItem) {
		this.primaryItem = primaryItem;
	}

	public FormItem getSecondaryItem() {
		return SecondaryItem;
	}

	public void setSecondaryItem(FormItem secondaryItem) {
		SecondaryItem = secondaryItem;
	}
}
