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
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

import org.olat.modules.selectus.model.Application;

/**
 * 
 * Initial date: 15.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceApplicationsListDataModel extends DefaultFlexiTableDataModel<Application>
	implements SortableFlexiTableDataModel<Application> {
	
	private static final AppCols[] COLS = AppCols.values();
	
	private final Locale locale;
	
	public ReferenceApplicationsListDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<Application> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		Application ref = getObject(row);
		return getValueAt(ref, col);
	}

	@Override
	public Object getValueAt(Application app, int col) {
		switch(COLS[col]) {
			case id: return app.getId();
			case title: return app.getPerson().getTitle();
			case firstName: return app.getPerson().getFirstName();
			case lastName: return app.getPerson().getLastName();
			case mail: return app.getPerson().getMail();
			case projectTitle: return app.getProject().getTitle();
			default: return "ERROR";
		}
	}
	
	public enum AppCols implements FlexiSortableColumnDef {
		id("edit.application.id"),
		title("edit.application.title"),
		firstName("edit.application.firstName"),
		lastName("edit.application.lastName"),
		mail("edit.application.mail"),
		//project
		projectTitle("table.header.project.title"),
		;
		
		private final String i18nKey;
		
		private AppCols(String i18nKey) {
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
