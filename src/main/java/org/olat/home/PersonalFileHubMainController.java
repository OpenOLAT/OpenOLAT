/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.home;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;

/**
 * 
 * Initial date: 21 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class PersonalFileHubMainController extends BasicController implements Activateable2 {

	private PersonalFileHubMountPointsController vfsSourcesCtrl;
	private PersonalFileHubLibrariesController storageesCtrl;

	public PersonalFileHubMainController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackedPanel) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("file_hub");
		putInitialPanel(mainVC);
		
		mainVC.contextPut("storageTitle", translate("file.hub.storage"));
		vfsSourcesCtrl = new PersonalFileHubMountPointsController(ureq, wControl, stackedPanel, translate("file.hub"));
		listenTo(vfsSourcesCtrl);
		mainVC.put("vfsSources", vfsSourcesCtrl.getInitialComponent());
		
		mainVC.contextPut("librariesTitle", translate("file.hub.libraries"));
		storageesCtrl = new PersonalFileHubLibrariesController(ureq, wControl, stackedPanel);
		listenTo(storageesCtrl);
		mainVC.put("libraries", storageesCtrl.getInitialComponent());
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("MediaCenter".equalsIgnoreCase(resName)) {
			storageesCtrl.activate(ureq, entries, state);
		} else if("Media".equalsIgnoreCase(resName)) {
			storageesCtrl.activate(ureq, entries, state);
		} else {
			vfsSourcesCtrl.activate(ureq, entries, state);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
