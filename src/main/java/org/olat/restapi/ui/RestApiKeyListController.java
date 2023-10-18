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
package org.olat.restapi.ui;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.restapi.RestModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * 
 * Initial date: 18 oct. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RestApiKeyListController extends FormBasicController {
	
	private FormLink addApiKeyLink;
	private FlexiTableElement tableEl;
	private RestApiKeyTableDataModel tableModel;
	
	private CloseableModalController cmc;
	private RestApiKeyController addApiKeyCtrl;
	private ConfirmDeleteRestApiKeyController confirmDeleteCtrl;
	
	@Autowired
	private BaseSecurity securityManager;
	
	public RestApiKeyListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "apikey_list");
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addApiKeyLink = uifactory.addFormLink("add.api.key", formLayout, Link.BUTTON);
		addApiKeyLink.setIconLeftCSS("o_icon o_ac_token_icon");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RestCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RestCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RestCols.secret));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
		
		tableModel = new RestApiKeyTableDataModel(columnsModel);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "keys", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		
	}
	
	private void loadModel() {
		List<Authentication> authentications = securityManager.getAuthentications(getIdentity());
		List<Authentication> rows = authentications.stream()
				.filter(auth -> RestModule.RESTAPI_AUTH.equals(auth.getProvider()))
				.collect(Collectors.toList());
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addApiKeyCtrl == source || confirmDeleteCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
				fireEvent(ureq, Event.DONE_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(addApiKeyCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeleteCtrl = null;
		addApiKeyCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == addApiKeyLink) {
			doAddApiKey(ureq);
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent se) {
				Authentication auth = tableModel.getObject(se.getIndex());
				if("delete".equals(se.getCommand())) {
					doConfirmDelete(ureq, auth);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doAddApiKey(UserRequest ureq) {
		addApiKeyCtrl = new RestApiKeyController(ureq, getWindowControl(), getIdentity());
		listenTo(addApiKeyCtrl);
		
		String title = addApiKeyCtrl.getAndRemoveFormTitle();
		if(title == null) {
			title = translate("add.api.key");
		}	
		cmc = new CloseableModalController(getWindowControl(), translate("close"), addApiKeyCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDelete(UserRequest ureq, Authentication authentication) {
		confirmDeleteCtrl = new ConfirmDeleteRestApiKeyController(ureq, getWindowControl(), authentication);
		listenTo(confirmDeleteCtrl);
		
		String title = translate("delete.api.key");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDeleteCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private static class RestApiKeyTableDataModel extends DefaultFlexiTableDataModel<Authentication>
	implements SortableFlexiTableDataModel<Authentication> {
		
		private static final RestCols[] COLS = RestCols.values();
		
		public RestApiKeyTableDataModel(FlexiTableColumnModel columnsModel) {
			super(columnsModel);
		}

		@Override
		public void sort(SortKey sortKey) {
			//
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			Authentication auth = getObject(row);
			return getValueAt(auth, col);
		}

		@Override
		public Object getValueAt(Authentication row, int col) {
			switch(COLS[col]) {
				case id: return row.getAuthusername();
				case creationDate: return row.getCreationDate();
				case secret: return "***";
				default: return "ERROR";
			}
		}
	}

	public enum RestCols implements FlexiSortableColumnDef {

		id("table.header.id"),
		creationDate("table.header.creation.date"),
		secret("table.header.secret");

		private final String i18nKey;
		
		private RestCols(String i18nKey) {
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
