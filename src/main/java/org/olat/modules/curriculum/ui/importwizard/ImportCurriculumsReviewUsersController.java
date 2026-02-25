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
package org.olat.modules.curriculum.ui.importwizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSort;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.ui.CurriculumExport;
import org.olat.modules.curriculum.ui.importwizard.ImportCurriculumsReviewTableModel.ImportCurriculumsCols;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportCurriculumsReviewUsersController extends AbstractImportListController {
	
	protected static final int USER_PROPS_OFFSET = 500;
	
	protected static final String SORT_RELEVANCE_KEY = "relevance";
	
	private ImportCurriculumsReviewUsersTableModel tableModel;
	
	private final boolean importUsersPasswords;
	private final List<UserPropertyHandler> userPropertyHandlers;

	@Autowired
	private BaseSecurityModule securityModule;
	
	public ImportCurriculumsReviewUsersController(UserRequest ureq, WindowControl wControl, Form rootForm,
			ImportCurriculumsContext context, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, "import_review_users", context, runContext, "Users", true, false);
		
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		setTranslator(Util.createPackageTranslator(ImportCurriculumsReviewUsersController.class, getLocale(), getTranslator()));

		importUsersPasswords = context.hasImportedUsersPasswords();
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(CurriculumExport.usageIdentifyer, isAdministrativeUser)
				.stream()
				.filter(handler -> userManager.isMandatoryUserProperty(CurriculumExport.usageIdentifyer, handler))
				.toList();
		
		initForm(ureq);
		// Load data for filters
		loadModel();
		// Initialize filters
		initFilterTabs();
		initFilters();
		tableEl.setSelectedFilterTab(ureq, allTab);
	}

	@Override
	protected void initColumns(FlexiTableColumnModel columnsModel) {
		DefaultFlexiColumnModel statusCol = new DefaultFlexiColumnModel(ImportCurriculumsCols.status,
				new ImportStatusCellRenderer(getTranslator()));
		columnsModel.addFlexiColumnModel(statusCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.infosWarnings));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.ignore));
		
		int i=0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			int colIndex = USER_PROPS_OFFSET + i++;
			String propName = userPropertyHandler.getName();
			FlexiColumnModel col = new DefaultFlexiColumnModel(true, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, true, propName);
			col.setCellRenderer(new ImportValueCellRenderer(colIndex, propName, getLocale()));
			columnsModel.addFlexiColumnModel(col);
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.organisationIdentifier,
				new ImportValueCellRenderer(ImportCurriculumsCols.organisationIdentifier, getLocale())));
		
		if(importUsersPasswords) {
			ImportValueCellRenderer renderer = new ImportValueCellRenderer(ImportCurriculumsCols.password, getLocale());
			renderer.setObsfuscate(true);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImportCurriculumsCols.password, renderer));
		}
	}

	@Override
	protected DefaultFlexiTableDataModel<? extends AbstractImportRow> initTableModel(FlexiTableColumnModel columnsModel) {
		tableModel = new ImportCurriculumsReviewUsersTableModel(columnsModel, getLocale());
		return tableModel;
	}

	@Override
	protected void initFilters(List<FlexiTableExtendedFilter> filters) {
		// Status
		SelectionValues statusKV = new SelectionValues();
		statusKV.add(SelectionValues.entry(STATUS_NEW, translate("search.status.new")));
		statusKV.add(SelectionValues.entry(STATUS_WITH_ERRORS, translate("search.status.errors")));
		statusKV.add(SelectionValues.entry(STATUS_WITH_WARNINGS, translate("search.status.warnings")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("search.import.object.status"),
				STATUS_KEY, statusKV, true));
		
		// User names
		SelectionValues usernamesKV = new SelectionValues();
		List<String> usernames = tableModel.getUsernames();
		for(String username:usernames) {
			usernamesKV.add(SelectionValues.entry(username, username));
		}

		filters.add(new FlexiTableMultiSelectionFilter(translate("search.import.usernames"),
				USERNAME_KEY, usernamesKV, true));
		
		// Organisation
		SelectionValues organisationsKV = new SelectionValues();
		List<Organisation> organisations = tableModel.getOrganisations();
		for(Organisation organisation:organisations) {
			StringBuilder sb = new StringBuilder();
			if(StringHelper.containsNonWhitespace(organisation.getDisplayName())) {
				sb.append(organisation.getDisplayName());
			}
			if(StringHelper.containsNonWhitespace(organisation.getIdentifier())) {
				sb.append(" \u00B7 ").append(organisation.getIdentifier());
			}
			organisationsKV.add(SelectionValues.entry(organisation.getKey().toString(), sb.toString()));
		}

		filters.add(new FlexiTableMultiSelectionFilter(translate("search.import.organisations"),
				ORGANISATION_KEY, organisationsKV, true));
	}
	
	@Override
	protected void sortOptions() {
		List<FlexiTableSort> sorters = new ArrayList<>();
		sorters.add(new FlexiTableSort(translate("sort.relevance"), SORT_RELEVANCE_KEY));
		FlexiTableSortOptions options = new FlexiTableSortOptions(sorters);
		tableEl.setSortSettings(options);
	}

	private void loadModel() {
		List<ImportedUserRow> rows = context.getImportedUsersRows();
		if(rows == null) {
			rows = List.of();
		} else {
			ImportCurriculumsObjectsLoader loader = context.getLoader();
			loader.loadUsers(rows);
			
			ImportCurriculumsValidator validator = context.getValidator();
			for(ImportedUserRow row:rows) {
				if(importUsersPasswords) {
					validator.validatePassword(row);
				}
				validator.validate(row);
			}
			validator.validateUsersUniqueUsernames(rows);
			
			SelectionValues ignorePK = new SelectionValues();
			ignorePK.add(SelectionValues.entry(IGNORE, ""));
			
			for(ImportedUserRow row:rows) {
				forgeRow(row, ignorePK);
			}
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		
		loadErrorMessage(rows, "error.users");
	}
	
	@Override
	protected final void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				if(ImportValueCellRenderer.CMD_ACTIONS.equals(se.getCommand())) {
					ImportedUserRow selectedRow = tableModel.getObject(se.getIndex());
					int colIndex = ImportValueCellRenderer.getColIndex(ureq);
					if(colIndex >= USER_PROPS_OFFSET) {
						String targetId = ImportValueCellRenderer.getId(se.getIndex(), colIndex);
						if(targetId != null) {
							doOpenValidationCallout(ureq, selectedRow, colIndex, targetId);
						}
					}
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formNext(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private void doOpenValidationCallout(UserRequest ureq, ImportedUserRow row, int colIndex, String targetId) {
		removeAsListenerAndDispose(validationCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		
		int handlerIndex = colIndex - USER_PROPS_OFFSET;
		if(handlerIndex >= 0 && handlerIndex < userPropertyHandlers.size()) {
			UserPropertyHandler handler = userPropertyHandlers.get(handlerIndex);
			CurriculumImportedValue value = row.getHandlerValidation(handler.getName());
			validationCtrl = new ValidationResultController(ureq, getWindowControl(), value);
			listenTo(validationCtrl);
		
			calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					validationCtrl.getInitialComponent(), targetId, "", true, "");
			listenTo(calloutCtrl);
			calloutCtrl.activate();
		}
	}
}
