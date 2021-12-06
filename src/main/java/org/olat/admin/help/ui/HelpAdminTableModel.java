/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.admin.help.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nModule;

/* 
 * Initial date: 1 Apr 2020<br>
 * @author Alexander Boeckle, alexander.boeckle@frentix.com
 */
public class HelpAdminTableModel extends DefaultFlexiTableDataModel<HelpAdminTableContentRow> {

	private final Translator translator;
	private final Translator adminTranslator;
	
	public HelpAdminTableModel(FlexiTableColumnModel columnModel, UserRequest ureq) {
		super(columnModel);
		
		adminTranslator = Util.createPackageTranslator(HelpAdminController.class, ureq.getLocale());
		translator = Util.createPackageTranslator(HelpAdminController.class, ureq.getLocale());
	}
	
	// Only for createCopyWithEmptyList
	private HelpAdminTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
		
		adminTranslator = Util.createPackageTranslator(HelpAdminController.class, I18nModule.getDefaultLocale());
		translator = Util.createPackageTranslator(HelpAdminController.class, I18nModule.getDefaultLocale());
	}

	@Override
	public Object getValueAt(int row, int col) {
		HelpAdminTableContentRow helpAdminRow = getObject(row);
		return getValueAt(helpAdminRow, col);
	}

	public Object getValueAt(HelpAdminTableContentRow row, int col) {
		switch (HelpAdminTableColumn.values()[col]) {
		case label:
			return translator.translate("help." + row.getHelpPlugin());
		case icon: 
			return row.getIcon();
		case action: 
			return adminTranslator.translate("help.admin." + row.getHelpPlugin());
		case usertool:
			return row.isUsertoolSet();
		case authoring:
			return row.isAuthoringSet();
		case dmz:
			return row.isLoginSet();
		case edit:
			return row.getEditLink();
		case delete:
			return row.getDeleteLink();
		case position:
			return getObjects().indexOf(row);
		case upDown: 
			return row.getUpDown();
		default:
			return "ERROR";
		}
	}
	
	public enum HelpAdminTableColumn implements FlexiColumnDef {
		label("help.admin.label"),
		icon("help.admin.icon"),
		action("help.admin.action"),
		usertool("help.admin.usertool"),
		authoring("help.admin.authorsite"),
		dmz("help.admin.login"),
		edit("help.admin.edit"),
		delete("help.admin.delete"),
		plugin("help.admin.plugin"),
		position("help.admin.position"),
		upDown("help.admin.up.down");
		
		private final String i18nHeaderKey;
		
		private HelpAdminTableColumn(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}
		
		@Override 
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
}
