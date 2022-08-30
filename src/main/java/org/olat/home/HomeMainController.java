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
package org.olat.home;

import java.util.List;

import org.olat.core.extensions.ExtManager;
import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.resource.OresHelper;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * Initial date: 27.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HomeMainController extends MainLayoutBasicController implements Activateable2 {

	private BreadcrumbedStackedPanel stackPanel;
	private Controller currentCtr;

	public HomeMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		stackPanel = new BreadcrumbedStackedPanel("homeStackPanel", getTranslator(), this);
		stackPanel.setInvisibleCrumb(1);
		putInitialPanel(stackPanel);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private String currentNavKey;

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String navKey = entry.getOLATResourceable().getResourceableTypeName();
		if("HomeSite".equals(navKey)) {
			entries = entries.subList(1, entries.size());
			if(!entries.isEmpty()) {
				entry = entries.get(0);
				navKey = entry.getOLATResourceable().getResourceableTypeName();
			}
		}
		
		if(navKey.equals(currentNavKey) && currentCtr instanceof ReusableHomeController) {
			if (currentCtr instanceof Activateable2) {
				((Activateable2) currentCtr).activate(ureq, entries, entry.getTransientState());
			}
		} else {
			GenericActionExtension gAE = ExtManager.getInstance()
					.getActionExtensionByNavigationKey(HomeMainController.class.getName(), navKey);
			if (gAE != null) {
				currentNavKey = navKey;
				stackPanel.popUpToRootController(ureq);
				
				removeAsListenerAndDispose(currentCtr);
				currentCtr = createController(gAE, ureq);
				listenTo(currentCtr);
				if (!entries.isEmpty()) {
					entries = entries.subList(1, entries.size());
				}

				String actionText = gAE.getActionText(getLocale());
				stackPanel.rootController(actionText, currentCtr);
				
				if (currentCtr instanceof Activateable2) {
					((Activateable2) currentCtr).activate(ureq, entries, entry.getTransientState());
				}
			}
		}
	}
	
	protected Controller createController(GenericActionExtension ae, UserRequest ureq) {
		// get our ores for the extension
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ae.getNavigationKey(), 0L);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);

		Controller ctrl = ae.createController(ureq, bwControl, null);
		if(ctrl instanceof BreadcrumbPanelAware) {
			((BreadcrumbPanelAware)ctrl).setBreadcrumbPanel(stackPanel);
		}
		return ctrl;
	}
}