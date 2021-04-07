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
package org.olat.ims.lti13.ui;

import java.util.List;

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
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.ims.lti13.LTI13Platform;
import org.olat.ims.lti13.LTI13PlatformScope;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.model.LTI13PlatformWithInfos;
import org.olat.ims.lti13.ui.LTI13AdminPlatformsTableModel.PlatformsCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 avr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13AdminPlatformsController extends FormBasicController {
	
	private FormLink addPlatformButton;
	private FlexiTableElement tableEl;
	private LTI13AdminPlatformsTableModel tableModel;
	
	private CloseableModalController cmc;
	private LTI13EditPlatformController editPlatformCtrl;
	
	@Autowired
	private LTI13Service lti13Service;
	
	public LTI13AdminPlatformsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin_platforms");
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("access.lti13.title");
		
		addPlatformButton = uifactory.addFormLink("add.platform", formLayout, Link.BUTTON);
		addPlatformButton.setIconLeftCSS("o_icon o_icon_add");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PlatformsCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PlatformsCols.issuer, new ServerCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PlatformsCols.clientId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PlatformsCols.deployments));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
		
		tableModel = new LTI13AdminPlatformsTableModel(columnsModel, getLocale());
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmtpyTableMessageKey("platforms.empty");
		tableEl.setAndLoadPersistedPreferences(ureq, "lti13-platforms-admin");
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void loadModel() {
		List<LTI13PlatformWithInfos> rows = lti13Service.getPlatformWithInfos();
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editPlatformCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
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
		removeAsListenerAndDispose(editPlatformCtrl);
		removeAsListenerAndDispose(cmc);
		editPlatformCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addPlatformButton == source) {
			doAddPlatform(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("edit".equals(se.getCommand())) {
					doEditPlatform(ureq, tableModel.getObject(se.getIndex()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doAddPlatform(UserRequest ureq) {
		if(guardModalController(editPlatformCtrl)) return;

		LTI13Platform sharedTool = lti13Service.createTransientPlatform(LTI13PlatformScope.SHARED);
		editPlatformCtrl = new LTI13EditPlatformController(ureq, getWindowControl(), sharedTool);
		listenTo(editPlatformCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editPlatformCtrl.getInitialComponent(),
				true, translate("add.platform"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doEditPlatform(UserRequest ureq, LTI13PlatformWithInfos row) {
		if(guardModalController(editPlatformCtrl)) return;

		LTI13Platform platform = lti13Service.getPlatformByKey(row.getPlatform().getKey());
		editPlatformCtrl = new LTI13EditPlatformController(ureq, getWindowControl(), platform);
		listenTo(editPlatformCtrl);
		
		String title = translate("edit.platform", new String[] { row.getPlatform().getIssuer() });
		cmc = new CloseableModalController(getWindowControl(), "close", editPlatformCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
}
