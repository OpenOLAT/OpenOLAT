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
package org.olat.course.area;

import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.ui.NewAreaController;
import org.olat.group.ui.area.BGAreaEditController;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CourseAreasController extends MainLayoutBasicController {
	
	private static final String TABLE_ACTION_EDIT = "tbl_edit";
	private static final String TABLE_ACTION_DELETE = "tbl_del";
	
	private final StackedPanel mainPanel;
	
	private final Link createAreaLink;
	private final VelocityContainer mainVC;
	private final TableController tableCtrl;
	private VelocityContainer createVC;
	private DialogBoxController deleteDialogCtr;
	private BGAreaEditController editController;
	private NewAreaController newAreaController;
	
	@Autowired
	private BGAreaManager areaManager;
	private final BGAreaTableModel areaDataModel;
	
	private final OLATResource resource;
	
	public CourseAreasController(UserRequest ureq, WindowControl wControl, OLATResource resource, boolean readOnly) {
		super(ureq, wControl);
		this.resource = resource;

		Translator resourceTrans = Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator());
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("resources.noresources"), null, "o_icon_courseareas");
		tableCtrl = new TableController(tableConfig, ureq, getWindowControl(), resourceTrans);
		listenTo(tableCtrl);
		
		tableCtrl.addColumnDescriptor(new DefaultColumnDescriptor("table.header.name", 0, null, getLocale()));
		DefaultColumnDescriptor descriptionColDesc = new DefaultColumnDescriptor("table.header.description", 1, null, getLocale());
		descriptionColDesc.setEscapeHtml(EscapeMode.antisamy);
		tableCtrl.addColumnDescriptor(descriptionColDesc);
		if(!readOnly) {
			tableCtrl.addColumnDescriptor(new StaticColumnDescriptor(TABLE_ACTION_EDIT, "action", translate("edit")));
			tableCtrl.addColumnDescriptor(new StaticColumnDescriptor(TABLE_ACTION_DELETE, "action", translate("delete")));
		}

		areaDataModel = new BGAreaTableModel(Collections.<BGArea>emptyList());
		tableCtrl.setTableDataModel(areaDataModel);
		loadModel();
		
		mainVC = createVelocityContainer("area_list");
		mainVC.put("areaList", tableCtrl.getInitialComponent());
		
		createAreaLink = LinkFactory.createButton("create.area", mainVC, this);
		createAreaLink.setVisible(!readOnly);
		mainVC.put("createArea", createAreaLink);
		
		mainPanel = putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	private void loadModel() {
		List<BGArea> areas = areaManager.findBGAreasInContext(resource);
		areaDataModel.setObjects(areas);
		tableCtrl.modelChanged();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == createAreaLink) {
			removeAsListenerAndDispose(newAreaController);
			newAreaController = new NewAreaController(ureq, getWindowControl(), resource, false, null);
			listenTo(newAreaController);
			// wrap in velocity container to add help, title 
			createVC = createVelocityContainer("area_create");
			createVC.put("areaForm", newAreaController.getInitialComponent());
			mainPanel.pushContent(createVC);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(tableCtrl == source) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if(TABLE_ACTION_EDIT.equals(actionid)) {
					BGArea area = areaDataModel.getObject(te.getRowId());
					doEdit(ureq, area);
				} else if (TABLE_ACTION_DELETE.equals(actionid)) {
					BGArea area = areaDataModel.getObject(te.getRowId());
					String text = translate("delete.area.description", new String[]{ area.getName() }); 
					deleteDialogCtr = activateYesNoDialog(ureq, translate("delete.area.title"), text, deleteDialogCtr);
					deleteDialogCtr.setUserObject(area);
				}
			}
		} else if (source == deleteDialogCtr) {
			if (DialogBoxUIFactory.isYesEvent(event)) { // yes case
				BGArea area = (BGArea)deleteDialogCtr.getUserObject();
				doDelete(area);
			}
		} else if (source == newAreaController) {
			if(event == Event.CANCELLED_EVENT ) {
				mainPanel.popContent();
			} else if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				BGArea area = newAreaController.getCreatedArea();
				loadModel();
				mainPanel.popContent();
				doEdit(ureq, area);
				
				removeAsListenerAndDispose(newAreaController);
				newAreaController = null;
				createVC = null;
			}
		} else if (source == editController) {
			if(event == Event.BACK_EVENT) {
				mainPanel.popContent();
				removeAsListenerAndDispose(editController);
				editController = null;
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doDelete(BGArea area) {
		areaManager.deleteBGArea(area);
		loadModel();
	}
	
	private void doEdit(UserRequest ureq, BGArea area) {
		removeAsListenerAndDispose(editController);
		editController = new BGAreaEditController(ureq, getWindowControl(), area, true);
		listenTo(editController);
		mainPanel.pushContent(editController.getInitialComponent());
	}
}
