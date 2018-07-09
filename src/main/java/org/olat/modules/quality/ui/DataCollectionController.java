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
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.stack.ButtonGroupComponent;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualitySecurityCallback;
import org.olat.modules.quality.ui.event.DataCollectionEvent;
import org.olat.modules.quality.ui.event.DataCollectionEvent.Action;

/**
 * 
 * Initial date: 11.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DataCollectionController extends BasicController implements TooledController, Activateable2 {

	private Link configurationLink;
	private Link participantsLink;
	private Link reminderLink;
	private final ButtonGroupComponent segmentButtonsCmp;
	private final TooledStackedPanel stackPanel;
	private final StackedPanel mainPanel;
	
	private DataCollectionConfigurationController configurationCtrl;
	private ParticipationListController participationsCtrl;
	private ReminderListController reminderCtrl;
	
	private final QualitySecurityCallback secCallback;
	private QualityDataCollectionLight dataCollection;
	
	protected DataCollectionController(UserRequest ureq, WindowControl wControl, QualitySecurityCallback secCallback,
			TooledStackedPanel stackPanel, QualityDataCollectionLight dataCollection) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		this.dataCollection = dataCollection;
		
		segmentButtonsCmp = new ButtonGroupComponent("segments");
		configurationLink = LinkFactory.createLink("data.collection.configuration", getTranslator(), this);
		segmentButtonsCmp.addButton(configurationLink, false);
		participantsLink = LinkFactory.createLink("data.collection.participations", getTranslator(), this);
		segmentButtonsCmp.addButton(participantsLink, false);
		reminderLink = LinkFactory.createLink("data.collection.reminders", getTranslator(), this);
		segmentButtonsCmp.addButton(reminderLink, false);
		
		mainPanel = putInitialPanel(new SimpleStackedPanel("dataCollectionSegments"));
		mainPanel.setContent(new Panel("empty"));
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		doOpenConfiguration(ureq);
	}

	@Override
	public void initTools() {
		stackPanel.addTool(segmentButtonsCmp, true);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == configurationCtrl) {
			if (event instanceof DataCollectionEvent) {
				DataCollectionEvent dccEvent = (DataCollectionEvent) event;
				if (dccEvent.getAction().equals(Action.CHANGED)) {
					dataCollection = dccEvent.getDataCollection();
					stackPanel.changeDisplayname(dataCollection.getTitle(), null, this);
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (configurationLink == source) {
			doOpenConfiguration(ureq);
		} else if (participantsLink == source) {
			doOpenParticipants(ureq);
		} else if (reminderLink == source) {
			doOpenReminders(ureq);
		} else if (stackPanel == source) {
			if (event instanceof PopEvent) {
				if (stackPanel.getLastController() == this) {
					doOpenConfiguration(ureq);
				}
			}
		}
	}

	private void doOpenConfiguration(UserRequest ureq) {
		stackPanel.popUpToController(this);	
		configurationCtrl = new DataCollectionConfigurationController(ureq, getWindowControl(), secCallback, stackPanel,
				dataCollection);
		listenTo(configurationCtrl);
		stackPanel.pushController(translate("data.collection.configuration"), configurationCtrl);
		segmentButtonsCmp.setSelectedButton(configurationLink);
	}
	
	private void doOpenParticipants(UserRequest ureq) {
		stackPanel.popUpToController(this);
		participationsCtrl = new ParticipationListController(ureq, getWindowControl(), secCallback, stackPanel,
				dataCollection);
		stackPanel.pushController(translate("data.collection.participations"), participationsCtrl);
		segmentButtonsCmp.setSelectedButton(participantsLink);
	}

	private void doOpenReminders(UserRequest ureq) {
		stackPanel.popUpToController(this);
		reminderCtrl = new ReminderListController(ureq, getWindowControl(), secCallback, stackPanel,
				dataCollection);
		stackPanel.pushController(translate("data.collection.reminders"), reminderCtrl);
		segmentButtonsCmp.setSelectedButton(reminderLink);
	}

	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
	}

}
