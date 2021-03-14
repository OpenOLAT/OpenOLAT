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

import java.util.ArrayList;
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
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13SharedTool;
import org.olat.ims.lti13.model.LTI13SharedToolWithInfos;
import org.olat.ims.lti13.ui.LTI13SharedToolsTableModel.SharedToolsCols;
import org.olat.ims.lti13.ui.events.AddDeploymentEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13ResourceAccessController extends FormBasicController {
	
	private FormLink shareToolButton;
	private FlexiTableElement tableEl;
	private LTI13SharedToolsTableModel tableModel;
	
	private int count = 0;
	private RepositoryEntry entry;
	private BusinessGroup businessGroup;

	private CloseableModalController cmc;
	private LTI13EditSharedToolController shareToolCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private LTI13EditSharedToolDeploymentController addDeploymentCtrl;
	private LTI13SharedToolDeploymentCalloutController deploymentListCtrl;
	
	@Autowired
	private LTI13Service lti13Service;

	public LTI13ResourceAccessController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, "access_resource");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;

		initForm(ureq);
		loadModel();
	}
	
	public LTI13ResourceAccessController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
		super(ureq, wControl, "access_resource");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.businessGroup = businessGroup;

		initForm(ureq);
		loadModel();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("access.lti13.title");
		
		shareToolButton = uifactory.addFormLink("share.tool", formLayout, Link.BUTTON);
		shareToolButton.setIconLeftCSS("o_icon o_icon_add");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SharedToolsCols.issuer, new ServerCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SharedToolsCols.clientId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SharedToolsCols.deployments));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
		
		tableModel = new LTI13SharedToolsTableModel(columnsModel, getLocale());
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmtpyTableMessageKey("tools.empty");
		tableEl.setAndLoadPersistedPreferences(ureq, "lti13-tools-admin");
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	private void loadModel() {
		List<LTI13SharedToolWithInfos> infos;
		if(entry != null) {
			infos = lti13Service.getSharedToolsWithInfos(entry);
		} else if(businessGroup != null) {
			infos = lti13Service.getSharedToolsWithInfos(businessGroup);
		} else {
			infos = List.of();
		}

		List<SharedToolRow> rows = new ArrayList<>(infos.size());
		for(LTI13SharedToolWithInfos info:infos) {
			LTI13SharedTool tool = info.getSharedTool();
			FormLink deploymentLink = uifactory.addFormLink("deployments_" + (++count), "deployments", String.valueOf(info.getNumOfDeployments()), null,
					flc, Link.LINK | Link.NONTRANSLATED);
			SharedToolRow row = new SharedToolRow(tool.getKey(), tool.getClientId(), tool.getIssuer(),
					info.getNumOfDeployments());
			row.setDeploymentLink(deploymentLink);
			deploymentLink.setUserObject(row);
			rows.add(row);
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(shareToolCtrl == source || addDeploymentCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(deploymentListCtrl == source) {
			calloutCtrl.deactivate();
			cleanUp();
			if(event instanceof AddDeploymentEvent) {
				doAddDeployment(ureq, ((AddDeploymentEvent)event).getSharedTool());
			}
		} else if(cmc == source || calloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(deploymentListCtrl);
		removeAsListenerAndDispose(addDeploymentCtrl);
		removeAsListenerAndDispose(shareToolCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(cmc);
		deploymentListCtrl = null;
		addDeploymentCtrl = null;
		shareToolCtrl = null;
		calloutCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(shareToolButton == source) {
			doAddShareTool(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("edit".equals(se.getCommand())) {
					doEditSharedTool(ureq, tableModel.getObject(se.getIndex()));
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("deployments".equals(link.getCmd()) && link.getUserObject() instanceof SharedToolRow) {
				doOpenDeploymentsList(ureq, (FormLink)source, (SharedToolRow)link.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenDeploymentsList(UserRequest ureq, FormLink link, SharedToolRow row) {
		LTI13SharedTool sharedTool = lti13Service.getSharedToolByKey(row.getKey());
		deploymentListCtrl = new LTI13SharedToolDeploymentCalloutController(ureq, getWindowControl(), sharedTool);
		listenTo(deploymentListCtrl);
		
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), deploymentListCtrl.getInitialComponent(),
				link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doAddShareTool(UserRequest ureq) {
		if(guardModalController(shareToolCtrl)) return;

		LTI13SharedTool sharedTool;
		if(entry != null) {
			sharedTool = lti13Service.createTransientSharedTool(entry);
		} else if(businessGroup != null) {
			sharedTool = lti13Service.createTransientSharedTool(businessGroup);
		} else {
			return;
		}
		
		shareToolCtrl = new LTI13EditSharedToolController(ureq, getWindowControl(), sharedTool);
		listenTo(shareToolCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", shareToolCtrl.getInitialComponent(),
				true, translate("add.tool"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doEditSharedTool(UserRequest ureq, SharedToolRow row) {
		if(guardModalController(shareToolCtrl)) return;

		LTI13SharedTool sharedTool = lti13Service.getSharedToolByKey(row.getKey());
		shareToolCtrl = new LTI13EditSharedToolController(ureq, getWindowControl(), sharedTool);
		listenTo(shareToolCtrl);
		
		String title = translate("edit.tool", new String[] { row.getIssuer() });
		cmc = new CloseableModalController(getWindowControl(), "close", shareToolCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doAddDeployment(UserRequest ureq, LTI13SharedTool sharedTool) {
		addDeploymentCtrl = new LTI13EditSharedToolDeploymentController(ureq, getWindowControl(), sharedTool);
		listenTo(addDeploymentCtrl);
		
		String title = translate("add.deployment");
		cmc = new CloseableModalController(getWindowControl(), "close", addDeploymentCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
}