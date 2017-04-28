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
package org.olat.course.archiver;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.course.nodes.CourseNode;

/**
 * wrapper for GenericArchiveController to handle individual export configuration 
 * 
 * Initial Date: 19.04.2017
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
public class TestArchiveController extends BasicController {
	
	private Link downloadOptionsEl;
	
	private GenericArchiveController genericArchiveController;
	private CloseableModalController cmc;
	private ExportOptionsController exportOptionsCtrl;
	
	protected TestArchiveController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, CourseNode... nodeTypes) {
		super(ureq, wControl);
		
		VelocityContainer nodeChoose = createVelocityContainer("testarchive");
		nodeChoose.contextPut("nodeType", nodeTypes[0].getType());

		downloadOptionsEl = LinkFactory.createButton("download.options", nodeChoose, this);
		downloadOptionsEl.setIconLeftCSS("o_icon o_icon_tools");
		
		genericArchiveController = new GenericArchiveController(ureq, wControl, ores, nodeTypes);
		genericArchiveController.setHideTitle(true);
		genericArchiveController.setOptions(FormatConfigHelper.getArchiveOptions(ureq));
		listenTo(genericArchiveController);
		
		nodeChoose.put("genericArchiveController", genericArchiveController.getInitialComponent());

		putInitialPanel(nodeChoose);	
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == downloadOptionsEl) {
			doOpenExportOptios(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == exportOptionsCtrl) {
			if (event == Event.DONE_EVENT) {
				genericArchiveController.setOptions(FormatConfigHelper.getArchiveOptions(ureq));
			}
			cmc.deactivate();
			cleanUpPopups();
		}
	}
	
	/**
	 * Aggressive clean up all popup controllers
	 */
	protected void cleanUpPopups() {
		removeAsListenerAndDispose(exportOptionsCtrl);
		removeAsListenerAndDispose(cmc);
		exportOptionsCtrl = null;
		cmc = null;
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}

	private void doOpenExportOptios(UserRequest ureq) {
		exportOptionsCtrl = new ExportOptionsController(ureq, getWindowControl());
		listenTo(exportOptionsCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), exportOptionsCtrl.getInitialComponent(),
				true, translate("download.options"));
		cmc.activate();
		listenTo(cmc);
	}
}
