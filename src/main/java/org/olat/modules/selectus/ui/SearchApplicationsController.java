/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Roles;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.ui.events.SelectApplicationEvent;

/**
 * 
 * Initial date: 18.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchApplicationsController extends FormBasicController {
	
	private static final String SELECT_APP = "select-app";
	
	private TextElement searchTextEl;
	private FlexiTableElement tableEl;
	private ApplicationResultsDataModel resultsModel;
	
	private final List<PositionStatus> filters = new ArrayList<>(6);

	@Autowired
	private RecruitingService erFrontendManager;
	
	public SearchApplicationsController(UserRequest ureq, WindowControl wControl,
			RecruitingSecurityCallback secCallback) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		
		if(secCallback.canAddPosition()) {
			filters.add(PositionStatus.preparation);
			filters.add(PositionStatus.published);
			filters.add(PositionStatus.closed);
		}
		filters.add(PositionStatus.publishedAndInScreening);
		filters.add(PositionStatus.closedAndInScreening);
		filters.add(PositionStatus.closedAndNoRating);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer searchFieldsLayout = FormLayoutContainer.createDefaultFormLayout("searchFields", getTranslator());
		formLayout.add(searchFieldsLayout);
		searchFieldsLayout.setRootForm(mainForm);
		
		searchTextEl = uifactory.addTextElement("search.text", "search.for", 255, "", searchFieldsLayout);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ResultsField.firstName, SELECT_APP));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ResultsField.lastName,  SELECT_APP));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ResultsField.mail, SELECT_APP));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ResultsField.positionTitle, SELECT_APP));
		
		resultsModel = new ApplicationResultsDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "positions", resultsModel, 25, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(false);
		tableEl.setElementCssClass("o_sel_search_app_list");
		tableEl.setPageSize(40);
		tableEl.setCustomizeColumns(false);
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(ResultsField.lastName.name(), true));
		tableEl.setSortSettings(sortOptions);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				Application app = resultsModel.getObject(se.getIndex());
				if(SELECT_APP.equals(cmd)) {
					fireEvent(ureq, new SelectApplicationEvent(app));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String text = searchTextEl.getValue();
		Roles roles = ureq.getUserSession().getRoles();
		List<Application> results = erFrontendManager.searchApplications(text, getIdentity(), roles, filters);
		resultsModel.setObjects(results);
		tableEl.reset();
		tableEl.reloadData();
	}
	
	private class ApplicationResultsDataModel extends DefaultFlexiTableDataModel<Application>
	implements SortableFlexiTableDataModel<Application> {

		public ApplicationResultsDataModel(FlexiTableColumnModel columnsModel) {
			super(columnsModel);
		}

		@Override
		public void sort(SortKey orderBy) {
			if(orderBy != null) {
				List<Application> views = new SortableFlexiTableModelDelegate<>(orderBy, this, getLocale()).sort();
				super.setObjects(views);
			}
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			Application app = getObject(row);
			return getValueAt(app, col);
		}

		@Override
		public Object getValueAt(Application app, int col) {
			ResultsField field = ResultsField.values()[col];
			switch(field) {
				case firstName: return app.getPerson().getFirstName();
				case lastName: return app.getPerson().getLastName();
				case mail: return app.getPerson().getMail();
				case positionTitle: return app.getPosition().getMLTitle(getLocale());
				default: return null;
			}
		}
	}
	
	public enum ResultsField implements FlexiSortableColumnDef {
		firstName("edit.application.firstName"),
		lastName("edit.application.lastName"),
		mail("edit.application.mail"),
		positionTitle("edit.position_title");

		private final String i18nKey;
		
		private ResultsField(String key) {
			this.i18nKey = key;
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
