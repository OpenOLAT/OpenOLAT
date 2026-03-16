/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.reference;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;

/**
 * 
 * Initial date: 24 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MyApplicationsTableModel extends DefaultFlexiTableDataModel<Application>
implements SortableFlexiTableDataModel<Application> {
	
	private static final MyAppsCols[] COLS = MyAppsCols.values();
	
	private final Locale locale;
	
	public MyApplicationsTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey sortKey) {
		//
	}

	@Override
	public Object getValueAt(int row, int col) {
		Application app = getObject(row);
		return getValueAt(app, col);
	}

	@Override
	public Object getValueAt(Application row, int col) {
		Position position = row.getPosition();
		switch(COLS[col]) {
			case positionTitle: return position.getMLTitle(locale);
			case refereeMgmtDeadline: return position.getApplicantRefereeManagementDeadline();
			default: return "ERROR";
		}
	}
	
	public Application getApplicationByKey(Long key) {
		List<Application> apps = getObjects();
		for(Application app:apps) {
			if(app.getKey().equals(key)) {
				return app;
			}
		}
		return null;
	}
	
	public enum MyAppsCols implements FlexiSortableColumnDef {
		positionTitle("position.list.title", true),
		refereeMgmtDeadline("table.header.reference.applicant.deadline", true);
		
		private final String i18nKey;
		private final boolean sortable;
		
		private MyAppsCols(String i18nKey, boolean sortable) {
			this.i18nKey = i18nKey;
			this.sortable = sortable;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return sortable;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}

}
