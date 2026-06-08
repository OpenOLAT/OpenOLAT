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
package org.olat.modules.selectus.ui.organisation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.OrganisationUnit;

/**
 * 
 * Initial date: 13 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationUnitsAdminDataModel extends DefaultFlexiTableDataModel<OrganisationSettingsRow>
	implements SortableFlexiTableDataModel<OrganisationSettingsRow> {
	
	private final Locale locale;
	private final Translator translator;
	private final RecruitingModule recruitingModule;
	
	public OrganisationUnitsAdminDataModel(FlexiTableColumnModel columnsModel, Translator translator, Locale locale) {
		super(new ArrayList<>(), columnsModel);
		this.locale = locale;
		this.translator = translator;
		recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<OrganisationSettingsRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		OrganisationSettingsRow unit = getObject(row);
		return getValueAt(unit, col);
	}
	
	@Override
	public Object getValueAt(OrganisationSettingsRow row, int col) {
		OrganisationUnit settings = row.setting();
		return switch(OrgUnitCols.values()[col]) {
			case name -> settings == null ? row.organisation().getDisplayName() : settings.getName();
			case nameDe -> settings == null ? null : settings.getNameDe();
			case nameFr -> settings == null ? null : settings.getNameFr();
			case staffMail -> getStaffMail(settings);
			case staffBcc -> getStaffBcc(settings);
			case edit -> Boolean.TRUE;
			default -> "ERROR";
		};
	}
	
	private String getStaffMail(OrganisationUnit settings) {
		if(settings != null && !settings.isSystemConfiguration()
				&& StringHelper.containsNonWhitespace(settings.getStaffMail())) {
			return settings.getStaffMail();
		}
		return recruitingModule.getStaffMail(settings);
	}
	
	private String getStaffBcc(OrganisationUnit setting) {
		String bcc = setting == null || setting.isSystemConfiguration()
				? recruitingModule.getBccStaffMail()
				: setting.getStaffBcc();
		if(StringHelper.containsNonWhitespace(bcc)) {
			return bcc;
		}
		return translator.translate("organisation.staff.bcc.hint.nobbc");
	}
	
	public enum OrgUnitCols implements FlexiSortableColumnDef {
		
		name("table.organisation.name"),
		nameDe("table.organisation.name.de"),
		nameFr("table.organisation.name.fr"),
		staffMail("table.organisation.staff.mail"),
		staffBcc("table.organisation.staff.bcc"),
		edit("table.organisation.staff.bcc");

		private String i18nKey;
		
		private OrgUnitCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
