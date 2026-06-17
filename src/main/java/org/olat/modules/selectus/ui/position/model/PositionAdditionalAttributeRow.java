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
package org.olat.modules.selectus.ui.position.model;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;

import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.PositionAttributeDefinitionTypeEnum;

/**
 * 
 * Initial date: 6 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionAdditionalAttributeRow {
	
	private MultipleSelectionElement mandatoryEl;
	private TextElement labelEl;
	private TextElement placeholderEl;
	
	private FormLink upButton;
	private FormLink downButton;
	private FormLink editButton;
	private FormLink deleteButton;
	private FormLink editLabelButton;
	private FormLink editPlaceholderButton;
	
	private int position;
	private PositionAttributeDefinition attributeDefinition;
	
	public PositionAdditionalAttributeRow(PositionAttributeDefinition attributeDefinition, MultipleSelectionElement mandatoryEl,
			TextElement nameEl, TextElement placeholderEl) {
		this.labelEl = nameEl;
		this.mandatoryEl = mandatoryEl;
		this.placeholderEl = placeholderEl;
		this.attributeDefinition = attributeDefinition;
	}
	
	public MultipleSelectionElement getMandatoryEl() {
		return mandatoryEl;
	}

	public TextElement getLabelEl() {
		return labelEl;
	}

	public TextElement getPlaceholderEl() {
		return placeholderEl;
	}
	
	public void setPlaceholderEl(TextElement placeholderEl) {
		this.placeholderEl = placeholderEl;
	}

	public PositionAttributeDefinitionTypeEnum getType() {
		return attributeDefinition == null ? null : attributeDefinition.getTypeEnum();
	}

	public PositionAttributeDefinition getAttributeDefinition() {
		return attributeDefinition;
	}

	public void setAttributeDefinition(PositionAttributeDefinition attributeDefinition) {
		this.attributeDefinition = attributeDefinition;
	}

	public FormLink getUpButton() {
		return upButton;
	}

	public void setUpButton(FormLink upButton) {
		this.upButton = upButton;
	}

	public FormLink getDownButton() {
		return downButton;
	}

	public void setDownButton(FormLink downButton) {
		this.downButton = downButton;
	}
	
	public FormLink getEditButton() {
		return editButton;
	}

	public void setEditButton(FormLink editButton) {
		this.editButton = editButton;
	}

	public FormLink getEditLabelButton() {
		return editLabelButton;
	}

	public void setEditLabelButton(FormLink editLabelButton) {
		this.editLabelButton = editLabelButton;
	}

	public FormLink getEditPlaceholderButton() {
		return editPlaceholderButton;
	}

	public void setEditPlaceholderButton(FormLink editPlaceholderButton) {
		this.editPlaceholderButton = editPlaceholderButton;
	}

	public FormLink getDeleteButton() {
		return deleteButton;
	}

	public void setDeleteButton(FormLink deleteButton) {
		this.deleteButton = deleteButton;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
}
