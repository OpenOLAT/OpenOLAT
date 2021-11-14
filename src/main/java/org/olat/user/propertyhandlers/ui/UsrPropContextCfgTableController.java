/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */

package org.olat.user.propertyhandlers.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.user.propertyhandlers.UserPropertyUsageContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * this class displays a table with all UserPropertyUsageContexts and the number
 * of PropertyHandlers in each Context
 * 
 * <P>
 * Initial Date: 29.08.2011 <br>
 * 
 * @author strentini
 */
public class UsrPropContextCfgTableController extends BasicController {

	private TableController contextTableCtr;
	private TableDataModel<Entry<String, UserPropertyUsageContext>> contTblModel;

	private static final String CMD_EDITCONTEXT = "edit.context";

	private UsrPropContextEditController contextEditCtr;
	private CloseableModalController editBox;
	
	@Autowired
	private UsrPropCfgManager usrPropCfgMng;

	public UsrPropContextCfgTableController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		// the context table
		TableGuiConfiguration contTableConfig = new TableGuiConfiguration();
		contTableConfig.setPageingEnabled(false);
		contextTableCtr = new TableController(contTableConfig, ureq, wControl, getTranslator());
		contextTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("upc.name", 0, null, ureq.getLocale()));
		contextTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("upc.description", 1, null, ureq.getLocale()));
		contextTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("upc.context.propcount", 2, null, ureq.getLocale()));
		contextTableCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_EDITCONTEXT, "upc.edit", translate("upc.edit")));

		listenTo(contextTableCtr);

		Map<String, UserPropertyUsageContext> contexts = usrPropCfgMng.getUserPropertiesConfigObject().getUsageContexts();
		List<Entry<String, UserPropertyUsageContext>> contextsList = new ArrayList<>(contexts.entrySet());

		contTblModel = new UsrPropContextCfgTableModel(contextsList);
		contextTableCtr.setTableDataModel(contTblModel);
		putInitialPanel(contextTableCtr.getInitialComponent());
	}
	
	/**
	 * 
	 */
	private void refreshView(){
		Map<String, UserPropertyUsageContext> contexts = usrPropCfgMng.getUserPropertiesConfigObject().getUsageContexts();
		List<Entry<String, UserPropertyUsageContext>> contextsList = new ArrayList<>(contexts.entrySet());

		contTblModel = new UsrPropContextCfgTableModel(contextsList);
		contextTableCtr.setTableDataModel(contTblModel);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source.equals(contextTableCtr)) {
			TableEvent tblEvent = (TableEvent) event;
			Entry<String, UserPropertyUsageContext> contextRow = contTblModel.getObject(tblEvent.getRowId());
			
			removeAsListenerAndDispose(editBox);
			removeAsListenerAndDispose(contextEditCtr);

			contextEditCtr = new UsrPropContextEditController(ureq, getWindowControl(), contextRow.getValue(), contextRow.getKey());
			editBox = new CloseableModalController(getWindowControl(), translate("close"), contextEditCtr.getInitialComponent(),
					true, translate("upc.context.edit") + " :: " + contextRow.getKey(), true);

			listenTo(editBox);
			editBox.activate();
		} else if(source.equals(contextEditCtr)){
			//nothing to do
		} else if(source.equals(editBox)){
			if(event.equals(CloseableModalController.CLOSE_MODAL_EVENT)){
				// edit-popup was closed, refresh our table
				refreshView();
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to handle
	}
}
