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
package org.olat.modules.selectus.ui.position;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.position.model.EditVisibilityStepSettings;
import org.olat.modules.selectus.ui.position.model.PositionAdditionalAttributeRow;

/**
 * 
 * Initial date: 6 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditAdditionalAttributesDataModel extends DefaultFlexiTableDataModel<PositionAdditionalAttributeRow>  {
	
	private static final AttributeCols[] COLS = AttributeCols.values();

	private final Tab tab;
	private Boolean globalVisibility;
	private EditVisibilityStepSettings visibilityStepSettings;
	
	public PositionEditAdditionalAttributesDataModel(FlexiTableColumnModel columnsModel,
			EditVisibilityStepSettings visibilityStepSettings, Tab tab) {
		super(columnsModel);
		this.tab = tab;
		this.visibilityStepSettings = visibilityStepSettings;
		globalVisibility = visibilityStepSettings == null ? null : visibilityStepSettings.getGlobalVisibility(tab);
	}
	
	public void updateVisibilityStepSettings(EditVisibilityStepSettings settings) {
		this.visibilityStepSettings = settings;
		globalVisibility = visibilityStepSettings == null ? null : visibilityStepSettings.getGlobalVisibility(tab);
	}

	@Override
	public Object getValueAt(int row, int col) {
		PositionAdditionalAttributeRow attribute = getObject(row);
		if(col >= 0 && col < COLS.length) {
			switch(COLS[col]) {
				case mandatory: return attribute.getMandatoryEl();
				case label: return attribute.getLabelEl();
				case editLabel: return attribute.getEditLabelButton();
				case placeholder: return attribute.getPlaceholderEl();
				case editPlaceholder: return attribute.getEditPlaceholderButton();
				case type: return attribute.getType();
				case visibilityReferees:
					return visibilityStepSettings != null && isFieldVisibleTo(attribute, visibilityStepSettings.getRefereesFields());
				case visibilityExperts:
					return visibilityStepSettings != null && isFieldVisibleTo(attribute, visibilityStepSettings.getExpertsFields());
				case visibilityComparativeAssessment:
					return visibilityStepSettings != null && isFieldVisibleTo(attribute, visibilityStepSettings.getComparativeExpertFields());
				case edit: return attribute.getEditButton();
				case up: return attribute.getUpButton();
				case down: return attribute.getDownButton();
				case delete: return attribute.getDeleteButton();
				default: return "ERROR";
			}
		} else if(col >= PositionEditAdditionalAttributesController.CHECKBOX_OFFSET) {
			int configIndex = col - PositionEditAdditionalAttributesController.CHECKBOX_OFFSET;
			if(globalVisibility != null) {
				return globalVisibility.booleanValue();
			} else if(visibilityStepSettings != null) {
				List<String> visibleFields = visibilityStepSettings.getFacultyMembersFields(configIndex);
				return isFieldVisibleTo(attribute, visibleFields);
			}
			return false;
		}
		return "ERROR";
	}
	
	private boolean isFieldVisibleTo(PositionAdditionalAttributeRow row, List<String> visibleFields) {
		if(globalVisibility != null) {
			return globalVisibility.booleanValue();
		}
		
		PositionAttributeDefinition definition = row.getAttributeDefinition();
		if( definition != null) {
			String field = RecruitingModule.APP_CUSTOM_FIELD_PREFIX + definition.getKey();
			return visibleFields.contains(field);
		}
		return false;
	}
	
	public enum AttributeCols implements FlexiColumnDef {
		mandatory("table.custom.attribute.header.mandatory"),
		label("table.custom.attribute.header.name"),
		editLabel("table.header.ml"),
		placeholder("table.custom.attribute.header.placeholder"),
		editPlaceholder("table.header.ml"),
		type("table.custom.attribute.header.type"),
		visibilityReferees("table.document.header.referees"),
		visibilityExperts("table.document.header.experts"),
		visibilityComparativeAssessment("table.document.header.comparative.experts"),
		edit("edit"),
		up("table.custom.attribute.header.up"),
		down("table.custom.attribute.header.down"),
		delete("delete");
		
		private String i18nKey;
		
		private AttributeCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
