/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
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
public class OrganisationUnitsAdminDataModel extends DefaultFlexiTableDataModel<OrganisationUnit>
	implements SortableFlexiTableDataModel<OrganisationUnit> {
	
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
			List<OrganisationUnit> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		OrganisationUnit unit = getObject(row);
		return getValueAt(unit, col);
	}
	
	@Override
	public Object getValueAt(OrganisationUnit row, int col) {
		switch(OrgUnitCols.values()[col]) {
			case name: return row.getName();
			case nameDe: return row.getNameDe();
			case nameFr: return row.getNameFr();
			case staffMail: {
				String mail = row.getStaffMail();
				if(!row.isSystemConfiguration() && StringHelper.containsNonWhitespace(mail)) {
					return mail;
				}
				return recruitingModule.getStaffMail(row);
			}
			case staffBcc: {
				String bcc = row.isSystemConfiguration() ? recruitingModule.getBccStaffMail() : row.getStaffBcc();
				if(StringHelper.containsNonWhitespace(bcc)) {
					return bcc;
				}
				return translator.translate("organisation.staff.bcc.hint.nobbc");
			}
			default: return "ERROR";
		}
	}
	
	public enum OrgUnitCols implements FlexiSortableColumnDef {
		
		name("table.organisation.name"),
		nameDe("table.organisation.name.de"),
		nameFr("table.organisation.name.fr"),
		staffMail("table.organisation.staff.mail"),
		staffBcc("table.organisation.staff.bcc");

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
