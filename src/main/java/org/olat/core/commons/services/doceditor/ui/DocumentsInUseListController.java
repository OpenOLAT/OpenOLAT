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
package org.olat.core.commons.services.doceditor.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.AccessSearchParams;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.ui.DocumentsInUseDataModel.DocumentsInUseCols;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Mar 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocumentsInUseListController extends FormBasicController {
	
	private static final String USER_PROPS_ID = DocumentsInUseListController.class.getCanonicalName();
	public static final int USER_PROPS_OFFSET = 500;
	
	private FlexiTableElement tableEl;
	private DocumentsInUseDataModel dataModel;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;

	public DocumentsInUseListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));

		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		int colIndex = USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, null, true, "userProp-" + colIndex));
			colIndex++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DocumentsInUseCols.fileName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DocumentsInUseCols.app));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DocumentsInUseCols.edit));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DocumentsInUseCols.opened));
		
		dataModel = new DocumentsInUseDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "doc-editor-files-in-use-v2");
		
		List<FlexiTableFilter> filters = new ArrayList<>();
		filters.add(new FlexiTableFilter(translate("table.filter.edit"), Mode.EDIT.name()));
		filters.add(new FlexiTableFilter(translate("table.filter.read.only"), Mode.VIEW.name()));
		filters.add(FlexiTableFilter.SPACER);
		filters.add(new FlexiTableFilter(translate("table.filter.show.all"), "showAll", true));
		tableEl.setFilters("", filters, false);
		tableEl.setSelectedFilterKey(Mode.EDIT.name());
	}
	
	void loadModel() {
		AccessSearchParams params = new AccessSearchParams();
		params.setFetch(true);
		List<Access> accesses = docEditorService.getAccesses(params);
		
		List<DocumentsInUseRow> rows = new ArrayList<>(accesses.size());
		for (Access access : accesses) {
			rows.add(new DocumentsInUseRow(access, userPropertyHandlers, getLocale()));
		}
		dataModel.setObjects(rows);
		tableEl.reset(false, false, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
