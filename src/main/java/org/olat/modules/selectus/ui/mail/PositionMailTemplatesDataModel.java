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
package org.olat.modules.selectus.ui.mail;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 24 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionMailTemplatesDataModel extends DefaultFlexiTableDataModel<PositionMailTemplateRow>
implements SortableFlexiTableDataModel<PositionMailTemplateRow> {
	
	private static final TemplateCols[] COLS = TemplateCols.values();
	
	private final Translator translator;
	
	public PositionMailTemplatesDataModel(FlexiTableColumnModel columnModel, Translator translator) {
		super(columnModel);
		this.translator = translator;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<PositionMailTemplateRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, translator.getLocale()).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		PositionMailTemplateRow templateRow = getObject(row);
		return getValueAt(templateRow, col);
	}

	@Override
	public Object getValueAt(PositionMailTemplateRow row, int col) {
		switch(COLS[col]) {
			case templateId: return row.getId();
			case templateName: return getName(row);
			case letterName: return getLetterName(row);
			case type: return getType(row);
			case recipient: return row.getRecipient();
			case edit:
			case reset: return row;
			default: return "ERROR";
		}
	}
	
	private String getLetterName(PositionMailTemplateRow row) {
		if(!row.isWithLetter()) {
			return translator.translate("not.available");
		}
		return row.getLetterName();
	}
	
	public String getType(PositionMailTemplateRow row) {
		switch(row.getType()) {
			case system:
			case expert:
			case feedback:
			case committeeReminder:
			case confirmationApplication:
			case confirmationApplicationWithRefereeManagement:
			case confirmationApplicationDuplicate:
			case referee: return translator.translate("system.template");
			default: return translator.translate("position.template");
		}
	}
	
	public String getName(PositionMailTemplateRow row) {
		if(row.isSystemTemplate() && StringHelper.containsNonWhitespace(row.getId())) {
			return translator.translate("rejection.template.label." + row.getId().toLowerCase());
		}
		return row.getName();
	}

	public enum TemplateCols implements FlexiSortableColumnDef {
		templateId("table.header.template.id"),
		templateName("table.header.template.name"),
		letterName("table.header.letter.name"),
		type("table.header.template.type"),
		recipient("table.header.template.recipient"),
		edit("table.header.edit"),
		reset("table.header.reset");
		
		private final String i18nKey;
		
		private TemplateCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != edit && this != reset;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
