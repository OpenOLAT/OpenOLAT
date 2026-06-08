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

import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.model.StandardAttributeRow;
import org.olat.modules.selectus.ui.position.model.EditVisibilityStepSettings;

/**
 * 
 * Initial date: 23 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditStandardAttributesDataModel extends DefaultFlexiTableDataModel<StandardAttributeRow>  {
	
	private static final StandardAttributeCols[] COLS = StandardAttributeCols.values();

	private final Tab tab;
	private Boolean globalVisibility;
	private EditVisibilityStepSettings visibilityStepSettings;
	
	public PositionEditStandardAttributesDataModel(FlexiTableColumnModel columnsModel, EditVisibilityStepSettings visibilityStepSettings, Tab tab) {
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
		StandardAttributeRow attribute = getObject(row);
		if(col >= 0 && col < COLS.length) {
			switch(COLS[col]) {
				case mandatory: return attribute.isMandatory();
				case labels: return attribute.getLabels();
				case heading: return attribute.getHeading();
				case type: return attribute.getType();
				case visibilityReferees:
					return visibilityStepSettings != null && isFieldVisibleTo(attribute, visibilityStepSettings.getRefereesFields());
				case visibilityExperts:
					return visibilityStepSettings != null && isFieldVisibleTo(attribute, visibilityStepSettings.getExpertsFields());
				case visibilityComparativeAssessment:
					return visibilityStepSettings != null && isFieldVisibleTo(attribute, visibilityStepSettings.getComparativeExpertFields());
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
	
	private boolean isFieldVisibleTo(StandardAttributeRow row, List<String> visibleFields) {
		if(globalVisibility != null) {
			return globalVisibility.booleanValue();
		}
		
		List<String> fields = row.getFields();
		if(fields != null && !fields.isEmpty()) {
			for(String field:fields) {
				if(visibleFields.contains(field)) {
					return true;
				}
			}
			
		}
		return false;
	}
	
	public enum StandardAttributeCols implements FlexiColumnDef {
		
		type("table.standard.attribute.header.type"),
		heading("table.standard.attribute.header.heading"),
		labels("table.standard.attribute.header.labels"),
		mandatory("table.standard.attribute.header.mandatory"),
		visibilityReferees("table.document.header.referees"),
		visibilityExperts("table.document.header.experts"),
		visibilityComparativeAssessment("table.document.header.comparative.experts");
		
		private String i18nKey;
		
		private StandardAttributeCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
