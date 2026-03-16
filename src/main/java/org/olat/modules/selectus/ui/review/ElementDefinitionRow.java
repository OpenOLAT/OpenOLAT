/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.review;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;

import org.olat.modules.selectus.model.review.ReviewElementDefinition;
import org.olat.modules.selectus.model.review.ReviewElementType;

/**
 * 
 * Initial date: 4 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ElementDefinitionRow {
	
	private FormLink upLink;
	private FormLink downLink;
	private FormLink deleteLink;
	private final TextElement labelEl;
	
	private ReviewElementDefinition element;
	
	public ElementDefinitionRow(ReviewElementDefinition element, TextElement labelEl,
			FormLink upButton, FormLink downButton, FormLink deleteButton) {
		this.element = element;
		this.labelEl = labelEl;
		this.upLink = upButton;
		this.downLink = downButton;
		this.deleteLink = deleteButton;
	}

	public ReviewElementDefinition getElement() {
		return element;
	}
	
	public void setElement(ReviewElementDefinition element) {
		this.element = element;
	}
	
	public String getLabel() {
		return element.getLabel();
	}
	
	public void setLabel(String label) {
		element.setLabel(label);
	}
	
	public TextElement getLabelEl() {
		return labelEl;
	}
	
	public ReviewElementType getType() {
		return element.getType();
	}

	public FormLink getUpLink() {
		return upLink;
	}

	public FormLink getDownLink() {
		return downLink;
	}

	public FormLink getDeleteLink() {
		return deleteLink;
	}

	@Override
	public int hashCode() {
		return element.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof ElementDefinitionRow) {
			ElementDefinitionRow row = (ElementDefinitionRow)obj;
			return element != null && row.element != null && element.equals(row.element);	
		}
		return false;
	}
}
