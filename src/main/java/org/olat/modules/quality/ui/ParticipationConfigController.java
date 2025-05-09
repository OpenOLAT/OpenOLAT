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
package org.olat.modules.quality.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.ui.security.DataCollectionSecurityCallback;

/**
 * 
 * Initial date: May 2, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ParticipationConfigController extends BasicController implements TooledController {

	private final ParticipationPublicLinkController publicLinkCtrl;
	private final ParticipationListController participationsCtrl;

	public ParticipationConfigController(UserRequest ureq, WindowControl wControl,
			DataCollectionSecurityCallback secCallback, TooledStackedPanel stackPanel,
			QualityDataCollection dataCollection) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("participation_config");
		putInitialPanel(mainVC);
		
		publicLinkCtrl = new ParticipationPublicLinkController(ureq, wControl, secCallback, dataCollection);
		listenTo(publicLinkCtrl);
		mainVC.put("public", publicLinkCtrl.getInitialComponent());
		
		participationsCtrl = new ParticipationListController(ureq, getWindowControl(),
				secCallback, stackPanel, dataCollection);
		listenTo(participationsCtrl);
		mainVC.put("participations", participationsCtrl.getInitialComponent());
	}
	
	public void onChanged(QualityDataCollection dataCollection, DataCollectionSecurityCallback secCallback) {
		publicLinkCtrl.onChanged(dataCollection, secCallback);
		participationsCtrl.onChanged(dataCollection, secCallback);
	}
	
	@Override
	public void initTools() {
		participationsCtrl.initTools(this);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
