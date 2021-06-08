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
package org.olat.ims.qti21.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.RepositoryEntrySettingsController;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * Initial date: 30 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21SettingsController extends RepositoryEntrySettingsController {
	
	private Link qtiOptionsLink;
	
	private QTI21DeliveryOptionsController deliveryOptionsCtrl;
	
	public QTI21SettingsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, RepositoryEntry entry) {
		super(ureq, wControl, stackPanel, entry);
	}
	
	@Override
	protected void initOptions() {
		qtiOptionsLink = LinkFactory.createToolLink("options", translate("tab.options"), this);
		qtiOptionsLink.setElementCssClass("o_sel_qti_resource_options");
		buttonsGroup.addButton(qtiOptionsLink, false);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		super.activate(ureq, entries, state);
		
		if(entries != null && !entries.isEmpty()) {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Options".equalsIgnoreCase(type)) {
				doDeliveryOptions(ureq);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(deliveryOptionsCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				doDeliveryOptions(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(qtiOptionsLink == source) {
			doDeliveryOptions(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void cleanUp() {
		super.cleanUp();
		
		removeAsListenerAndDispose(deliveryOptionsCtrl);
		deliveryOptionsCtrl = null;
	}
	
	private Activateable2 doDeliveryOptions(UserRequest ureq) {
		OLATResourceable ores = OresHelper.createOLATResourceableType("Options");
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl swControl = addToHistory(ureq, ores, null);
		
		deliveryOptionsCtrl = new QTI21DeliveryOptionsController(ureq, swControl, entry, readOnly);
		listenTo(deliveryOptionsCtrl);
		mainPanel.setContent(deliveryOptionsCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(qtiOptionsLink);
		return deliveryOptionsCtrl;
	}
}
