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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Description:<br>
 * Simple controller that provides a tabbedPane for the UserProperty-Config GUI
 * 
 * 
 * <P>
 * Initial Date: 26.08.2011 <br>
 * 
 * @author strentini, sergio.trentini@frentix.com, http://www.frentix.com
 */
public class UsrPropCfgController extends BasicController {

	private TabbedPane tabbedPane;
	private UsrPropCfgTableController userPropTblCtr; 
	private UsrPropContextCfgTableController userPropContextTblCtr; 
	private UsrPropCfgResetController userPropResetCtrl; 

	public UsrPropCfgController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		userPropTblCtr = new UsrPropCfgTableController(ureq, wControl);
		userPropContextTblCtr = new UsrPropContextCfgTableController(ureq, wControl);
		userPropResetCtrl = new UsrPropCfgResetController(ureq, wControl);
		listenTo(userPropResetCtrl);

		tabbedPane = new TabbedPane("tabbedPane", ureq.getLocale());
		tabbedPane.addTab(translate("upc.properties"), userPropTblCtr.getInitialComponent());
		tabbedPane.addTab(translate("upc.contexts"), userPropContextTblCtr.getInitialComponent());
		tabbedPane.addTab(translate("upc.reset"), userPropResetCtrl.getInitialComponent());

		putInitialPanel(tabbedPane);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to handle
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source.equals(userPropResetCtrl) && event.equals(Event.DONE_EVENT)){
			userPropTblCtr.refresh();
			getWindowControl().setInfo(translate("upc.reset.config.done"));
		}
	}
}