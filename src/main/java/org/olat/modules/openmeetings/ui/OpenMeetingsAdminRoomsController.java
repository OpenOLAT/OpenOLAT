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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.openmeetings.ui;

import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.openmeetings.manager.OpenMeetingsException;
import org.olat.modules.openmeetings.manager.OpenMeetingsManager;
import org.olat.modules.openmeetings.model.OpenMeetingsRoom;
import org.olat.modules.openmeetings.model.OpenMeetingsRoomReference;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * 
 * Initial date: 08.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenMeetingsAdminRoomsController extends BasicController {
	
	private final OpenMeetingsManager openMeetingsManager;
	
	private Controller infoController;
	private DialogBoxController dialogCtr;
	private final TableController tableCtr;
	private CloseableModalController cmc;
	
	public OpenMeetingsAdminRoomsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		openMeetingsManager = CoreSpringFactory.getImpl(OpenMeetingsManager.class);
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("table.empty"), null, "o_cal_icon");
		tableConfig.setDownloadOffered(true);
		tableConfig.setSortingEnabled(true);
		tableConfig.setDisplayTableHeader(true);
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);
		
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(tableCtr);

		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("room.name", OpenMeetingsRoomsDataModel.Column.name.ordinal(), null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("room.size", OpenMeetingsRoomsDataModel.Column.roomSize.ordinal(), null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("room.numOfUsers", OpenMeetingsRoomsDataModel.Column.numOfUsers.ordinal(), null, getLocale()));
		
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("room.resource", OpenMeetingsRoomsDataModel.Column.resource.ordinal(), "resource", getLocale()));

		tableCtr.addColumnDescriptor(new StaticColumnDescriptor("infos", "room.infos", translate("room.infos")));
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor("delete", "delete", translate("delete")));
		
		tableCtr.setSortColumn(0, false);

		reloadModel();
		
		putInitialPanel(tableCtr.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//nothing to do
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == tableCtr) {
			if(event instanceof TableEvent) {
				TableEvent e = (TableEvent)event;
				int row = e.getRowId();
				OpenMeetingsRoom room = (OpenMeetingsRoom)tableCtr.getTableDataModel().getObject(row);
				if("delete".equals(e.getActionId())) {
					confirmDelete(ureq, room);
				} else if("infos".equals(e.getActionId())) {
					openInfoBox(ureq, room);
				} else if("resource".equals(e.getActionId())) {
					openResource(ureq, room);
				}
			}
		} else if(source == dialogCtr) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				OpenMeetingsRoom room = (OpenMeetingsRoom)dialogCtr.getUserObject();
				doDelete(room);
			}
		} else if (source == cmc ) {
			removeAsListenerAndDispose(infoController);
			removeAsListenerAndDispose(cmc);
		} else if (source == infoController) {
			cmc.deactivate();
			removeAsListenerAndDispose(infoController);
			removeAsListenerAndDispose(cmc);
			reloadModel();
		}
	}
	
	protected void openResource(UserRequest ureq, OpenMeetingsRoom room) {
		OpenMeetingsRoomReference prop = room.getReference();
		if(prop != null) {
			String url;
			if(prop.getGroup() != null) {
				url = "[BusinessGroup:" + prop.getGroup().getKey() + "]";
			} else {
				OLATResourceable ores = OresHelper.createOLATResourceableInstance(prop.getResourceTypeName(), prop.getResourceTypeId());
				RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(ores, false);
				if(re != null) {
					url = "[RepositoryEntry:" + re.getKey() + "]";
					if(StringHelper.containsNonWhitespace(prop.getSubIdentifier()) && "CourseModule".equals(ores.getResourceableTypeName())) {
						url += "[CourseNode:" + prop.getSubIdentifier() + "]";
					}	
				} else {
					showWarning("resource.dont.exist");
					return;
				}
			}
			BusinessControl bc = BusinessControlFactory.getInstance().createFromString(url);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
			NewControllerFactory.getInstance().launch(ureq, bwControl);
		}
	}
	
	protected void openInfoBox(UserRequest ureq, OpenMeetingsRoom room) {
		removeAsListenerAndDispose(infoController);
		removeAsListenerAndDispose(cmc);
		
		try {
			infoController = new OpenMeetingsAdminRoomInfosController(ureq, getWindowControl(), room);
			listenTo(infoController);

			cmc = new CloseableModalController(getWindowControl(), translate("close"), infoController.getInitialComponent(), true, translate("room.raw.title"));
			listenTo(cmc);
			cmc.activate();
		} catch (Exception e) {
			showError(OpenMeetingsException.SERVER_NOT_I18N_KEY);
		}
	}
	
	protected void doDelete(OpenMeetingsRoom room) {
		try {
			if(openMeetingsManager.deleteRoom(room)) {
				showInfo("delete.ok");
			} else {
				showError("delete.nok");
			}
			reloadModel();
		} catch (Exception e) {
			showError(OpenMeetingsException.SERVER_NOT_I18N_KEY);
		}
	}
	
	protected void confirmDelete(UserRequest ureq, OpenMeetingsRoom room) {
		String title = translate("delete");
		String text = translate("delete.confirm", new String[]{ StringHelper.escapeHtml(room.getName()) });
		dialogCtr = activateOkCancelDialog(ureq, title, text, dialogCtr);
		dialogCtr.setUserObject(room);
	}
	
	protected void reloadModel() {
		try {
			List<OpenMeetingsRoom> rooms = openMeetingsManager.getOpenOLATRooms();
			OpenMeetingsRoomsDataModel tableModel = new OpenMeetingsRoomsDataModel(rooms);
			tableCtr.setTableDataModel(tableModel);
		} catch (Exception e) {
			showError(OpenMeetingsException.SERVER_NOT_I18N_KEY);
		}
	}
}