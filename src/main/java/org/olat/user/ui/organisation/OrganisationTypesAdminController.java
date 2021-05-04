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
package org.olat.user.ui.organisation;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.OrganisationType;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.user.ui.organisation.OrganisationTypesDataModel.TypeCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationTypesAdminController extends FormBasicController implements Activateable2 {
	
	private FormLink addRootTypeButton;
	private FlexiTableElement tableEl;
	private OrganisationTypesDataModel model;
	
	private CloseableModalController cmc;
	private EditOrganisationTypeController editTypeCtrl;
	private EditOrganisationTypeController rootTypeCtrl;
	
	@Autowired
	private OrganisationService organisationService;
	
	public OrganisationTypesAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "organisation_types");
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		addRootTypeButton = uifactory.addFormLink("add.root.type", formLayout, Link.BUTTON);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, TypeCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TypeCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TypeCols.displayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, TypeCols.externalId));
		DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel("table.header.edit", -1, "edit",
				new StaticFlexiCellRenderer("", "edit", "o_icon o_icon-lg o_icon_edit", translate("edit")));
		editColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(editColumn);
		
		model = new OrganisationTypesDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "types", model, 25, false, getTranslator(), formLayout);
		tableEl.setEmptyTableMessageKey("table.organisation.type.empty");
		tableEl.setAndLoadPersistedPreferences(ureq, "org-types");
	}
	
	private void loadModel() {
		List<OrganisationType> types = organisationService.getOrganisationTypes();
		List<OrganisationTypeRow> rows = new ArrayList<>(types.size());
		for(OrganisationType type:types) {
			OrganisationTypeRow row = new OrganisationTypeRow(type);
			rows.add(row);
		}
		model.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(rootTypeCtrl == source || editTypeCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(rootTypeCtrl);
		removeAsListenerAndDispose(editTypeCtrl);
		removeAsListenerAndDispose(cmc);
		editTypeCtrl = null;
		rootTypeCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addRootTypeButton == source) {
			doAddRootType(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("edit".equals(cmd)) {
					OrganisationTypeRow row = model.getObject(se.getIndex());
					doEditType(ureq, row.getType());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doAddRootType(UserRequest ureq) {
		if(guardModalController(rootTypeCtrl)) return;
		
		rootTypeCtrl = new EditOrganisationTypeController(ureq, getWindowControl(), null);
		listenTo(rootTypeCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", rootTypeCtrl.getInitialComponent(), true, translate("add.root.type"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditType(UserRequest ureq, OrganisationType type) {
		if(guardModalController(editTypeCtrl)) return;
		
		OrganisationType reloadedType = organisationService.getOrganisationType(type);
		editTypeCtrl = new EditOrganisationTypeController(ureq, getWindowControl(), reloadedType);
		listenTo(editTypeCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editTypeCtrl.getInitialComponent(), true, translate("edit.type"));
		listenTo(cmc);
		cmc.activate();
	}
}
