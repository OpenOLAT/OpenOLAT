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
package org.olat.modules.portfolio.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.stack.ButtonGroupComponent;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;

/**
 * 
 * Initial date: 07.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderController extends BasicController implements TooledController, Activateable2 {
	
	private Link assessmentLink;
	private final Link overviewLink, entriesLink, publishLink;
	private final ButtonGroupComponent segmentButtonsCmp;
	private final TooledStackedPanel stackPanel;
	private Link editBinderMetadataLink;
	
	private PublishController publishCtrl;
	private TableOfContentController overviewCtrl;
	private BinderPageListController entriesCtrl;
	private BinderAssessmentController assessmentCtrl;
	
	private Binder binder;
	private final BinderConfiguration config;
	private final BinderSecurityCallback secCallback;

	public BinderController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BinderSecurityCallback secCallback, Binder binder, BinderConfiguration config) {
		super(ureq, wControl);
		this.binder = binder;
		this.config = config;
		this.stackPanel = stackPanel;
		this.secCallback = secCallback;
		
		segmentButtonsCmp = new ButtonGroupComponent("segments");
		overviewLink = LinkFactory.createLink("portfolio.overview", getTranslator(), this);
		segmentButtonsCmp.addButton(overviewLink, true);
		entriesLink = LinkFactory.createLink("portfolio.entries", getTranslator(), this);
		segmentButtonsCmp.addButton(entriesLink, false);
		publishLink = LinkFactory.createLink("portfolio.publish", getTranslator(), this);
		segmentButtonsCmp.addButton(publishLink, false);
		if(config.isAssessable()) {
			assessmentLink = LinkFactory.createLink("portfolio.assessment", getTranslator(), this);
			segmentButtonsCmp.addButton(assessmentLink, false);
		}
		
		stackPanel.addListener(this);
		putInitialPanel(new SimpleStackedPanel("portfolioSegments"));
	}

	@Override
	public void initTools() {
		stackPanel.addTool(segmentButtonsCmp, true);
		stackPanel.addTool(editBinderMetadataLink, Align.right);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			doOpenOverview(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(entriesCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				removeAsListenerAndDispose(overviewCtrl);
				overviewCtrl = null;
			}
		} else if(overviewCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				removeAsListenerAndDispose(entriesCtrl);
				entriesCtrl = null;
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (overviewLink == source) {
			doOpenOverview(ureq);
		} else if(entriesLink == source) {
			doOpenEntries(ureq);
		} else if(publishLink == source) {
			doOpenPublish(ureq);
		} else if(assessmentLink == source) {
			doOpenAssessment(ureq);
		} else if(stackPanel == source) {
			if(event instanceof PopEvent) {
				if(stackPanel.getLastController() == this) {
					doOpenOverview(ureq);
				}
			}
		}
	}
	
	private void doOpenEntries(UserRequest ureq) {
		entriesCtrl = new BinderPageListController(ureq, getWindowControl(), stackPanel, secCallback, binder, config);
		listenTo(entriesCtrl);

		stackPanel.popUpToController(this);
		stackPanel.pushController(translate("portfolio.entries"), entriesCtrl);
		segmentButtonsCmp.setSelectedButton(entriesLink);
	}
	
	private void doOpenOverview(UserRequest ureq) {
		overviewCtrl = new TableOfContentController(ureq, getWindowControl(), stackPanel, secCallback, binder, config);
		listenTo(overviewCtrl);

		stackPanel.popUpToController(this);
		stackPanel.pushController(translate("portfolio.overview"), overviewCtrl);
		segmentButtonsCmp.setSelectedButton(overviewLink);
	}
	
	private void doOpenPublish(UserRequest ureq) {
		publishCtrl = new PublishController(ureq, getWindowControl(), stackPanel, secCallback, binder, config);
		listenTo(publishCtrl);
		
		stackPanel.popUpToController(this);
		stackPanel.pushController(translate("portfolio.publish"), publishCtrl);
		segmentButtonsCmp.setSelectedButton(publishLink);
	}
	
	private void doOpenAssessment(UserRequest ureq) {
		assessmentCtrl = new BinderAssessmentController(ureq, getWindowControl(), secCallback, binder, config);
		listenTo(assessmentCtrl);
		
		stackPanel.popUpToController(this);
		stackPanel.pushController(translate("portfolio.assessment"), assessmentCtrl);
		segmentButtonsCmp.setSelectedButton(assessmentLink);
	}
}
