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
package org.olat.modules.quality.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.quality.ui.security.MainSecurityCallback;
import org.olat.modules.quality.ui.security.QualitySecurityCallbackFactory;

/**
 * 
 * Initial date: 08.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityMainController extends MainLayoutBasicController implements Activateable2 {
	
	private static final String ORES_TYPE_QUALITY = "quality";
	
	private final TooledStackedPanel stackPanel;
	private final QualityHomeController homeCtrl;
	
	public QualityMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		MainSecurityCallback secCallback = QualitySecurityCallbackFactory.createMainSecurityCallback(ureq.getUserSession().getRoles(), getIdentity());
		
		stackPanel = new TooledStackedPanel("qualitiy.management", getTranslator(), this);
		stackPanel.setToolbarAutoEnabled(true);
		stackPanel.setShowCloseLink(true, false);
		stackPanel.setInvisibleCrumb(0);
		putInitialPanel(stackPanel);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ORES_TYPE_QUALITY, 0l);
		WindowControl swControl = addToHistory(ureq, ores, null, getWindowControl(), true);
		homeCtrl = new QualityHomeController(ureq, swControl, stackPanel, secCallback);
		listenTo(homeCtrl);
		stackPanel.pushController(translate("breadcrumb.root"), homeCtrl);
		stackPanel.setCssClass("o_qual_main");
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries != null && entries.size() > 0) {
			String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if (ORES_TYPE_QUALITY.equalsIgnoreCase(resName)) {
				entries = entries.subList(1, entries.size());
			}
		}
		
		if (homeCtrl != null) {
			homeCtrl.activate(ureq, entries, state);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
