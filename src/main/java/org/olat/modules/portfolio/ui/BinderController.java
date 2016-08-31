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

import java.util.ArrayList;
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
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.ui.event.SectionSelectionEvent;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * Initial date: 07.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderController extends BasicController implements TooledController, Activateable2 {
	
	private Link assessmentLink, publishLink, optionsLink;
	private final Link overviewLink, entriesLink, historyLink;
	private final ButtonGroupComponent segmentButtonsCmp;
	private final TooledStackedPanel stackPanel;
	private Link editBinderMetadataLink;
	private StackedPanel mainPanel;
	
	private HistoryController historyCtrl;
	private PublishController publishCtrl;
	private BinderDeliveryOptionsController optionsCtrl;
	private BinderPageListController entriesCtrl;
	private TableOfContentController overviewCtrl;
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

		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(binder));

		segmentButtonsCmp = new ButtonGroupComponent("segments");
		segmentButtonsCmp.setElementCssClass("o_sel_pf_binder_navigation");
		overviewLink = LinkFactory.createLink("portfolio.overview", getTranslator(), this);
		overviewLink.setElementCssClass("o_sel_pf_toc");
		segmentButtonsCmp.addButton(overviewLink, false);
		entriesLink = LinkFactory.createLink("portfolio.entries", getTranslator(), this);
		entriesLink.setElementCssClass("o_sel_pf_entries");
		segmentButtonsCmp.addButton(entriesLink, false);
		historyLink = LinkFactory.createLink("portfolio.history", getTranslator(), this);
		historyLink.setElementCssClass("o_sel_pf_history");
		segmentButtonsCmp.addButton(historyLink, false);
		if(config.isShareable()) {
			publishLink = LinkFactory.createLink("portfolio.publish", getTranslator(), this);
			publishLink.setElementCssClass("o_sel_pf_publication");
			segmentButtonsCmp.addButton(publishLink, false);
		}
		if(config.isAssessable()) {
			assessmentLink = LinkFactory.createLink("portfolio.assessment", getTranslator(), this);
			publishLink.setElementCssClass("o_sel_pf_assessment");
			segmentButtonsCmp.addButton(assessmentLink, false);
		}
		if(config.isOptions()) {
			optionsLink = LinkFactory.createLink("portfolio.template.options", getTranslator(), this);
			publishLink.setElementCssClass("o_sel_pf_options");
			segmentButtonsCmp.addButton(optionsLink, false);
		}
		
		mainPanel = putInitialPanel(new SimpleStackedPanel("portfolioSegments"));
		mainPanel.setContent(new Panel("empty"));
		stackPanel.addListener(this);
	}

	@Override
	public void initTools() {
		stackPanel.addTool(segmentButtonsCmp, true);
		stackPanel.addTool(editBinderMetadataLink, Align.right);
		
		if(segmentButtonsCmp.getSelectedButton() == overviewLink) {
			if(overviewCtrl != null) {
				overviewCtrl.initTools();
			}
		}
	}
	
	protected void setSegmentButtonsVisible(boolean enabled) {
		if(segmentButtonsCmp != null) {
			segmentButtonsCmp.setVisible(enabled);
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			doOpenEntries(ureq);
			return;
		}
		
		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Page".equalsIgnoreCase(resName) || "Entry".equalsIgnoreCase(resName) || "Section".equalsIgnoreCase(resName)) {
			doOpenOverview(ureq).activate(ureq, entries, state);
		} else if("Entries".equalsIgnoreCase(resName)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenEntries(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if("Publish".equalsIgnoreCase(resName)) {
			if(config.isShareable()) {
				doOpenPublish(ureq);
			}
		} else if("Assessment".equalsIgnoreCase(resName)) {
			if(config.isAssessable()) {
				doOpenAssessment(ureq);
			}
		} else if("History".equalsIgnoreCase(resName)) {
			doOpenHistory(ureq);
		} else if("Toc".equalsIgnoreCase(resName)) {
			doOpenOverview(ureq);
		} else if("Options".equalsIgnoreCase(resName)) {
			doOpenOptions(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(entriesCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				if(overviewCtrl != null) {
					overviewCtrl.loadModel();
				}
			}
		} else if(overviewCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				removeAsListenerAndDispose(entriesCtrl);
				entriesCtrl = null;
			} else if(event instanceof SectionSelectionEvent) {
				SectionSelectionEvent sse = (SectionSelectionEvent)event;
				List<ContextEntry> entries = new ArrayList<>();
				entries.add(BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableInstance("Section", sse.getSection().getKey())));
				doOpenEntries(ureq).activate(ureq, entries, null);
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
		} else if(historyLink == source) {
			doOpenHistory(ureq);
		} else if(optionsLink == source) {
			doOpenOptions(ureq);
		} else if(stackPanel == source) {
			if(event instanceof PopEvent) {
				if(stackPanel.getLastController() == this) {
					doOpenOverview(ureq);
				}
			}
		}
	}
	
	private void popUpToBinderController(UserRequest ureq) {
		if(stackPanel.getRootController() == this) {
			stackPanel.popUpToRootController(ureq);
		} else {
			stackPanel.popUpToController(this);
		}
	}
	
	private TableOfContentController doOpenOverview(UserRequest ureq) {
		OLATResourceable bindersOres = OresHelper.createOLATResourceableInstance("Toc", 0l);
		WindowControl swControl = addToHistory(ureq, bindersOres, null);
		overviewCtrl = new TableOfContentController(ureq, swControl, stackPanel, secCallback, binder, config);
		listenTo(overviewCtrl);
		
		popUpToBinderController(ureq);
		segmentButtonsCmp.setSelectedButton(overviewLink);
		mainPanel.setContent(overviewCtrl.getInitialComponent());
		return overviewCtrl;
	}
	
	private BinderPageListController doOpenEntries(UserRequest ureq) {
		OLATResourceable bindersOres = OresHelper.createOLATResourceableInstance("Entries", 0l);
		WindowControl swControl = addToHistory(ureq, bindersOres, null);
		entriesCtrl = new BinderPageListController(ureq, swControl, stackPanel, secCallback, binder, config);
		listenTo(entriesCtrl);

		popUpToBinderController(ureq);
		stackPanel.pushController(translate("portfolio.entries"), entriesCtrl);
		segmentButtonsCmp.setSelectedButton(entriesLink);
		return entriesCtrl;
	}
	
	private PublishController doOpenPublish(UserRequest ureq) {
		OLATResourceable bindersOres = OresHelper.createOLATResourceableInstance("Publish", 0l);
		WindowControl swControl = addToHistory(ureq, bindersOres, null);
		publishCtrl = new PublishController(ureq, swControl, stackPanel, secCallback, binder, config);
		listenTo(publishCtrl);

		popUpToBinderController(ureq);
		stackPanel.pushController(translate("portfolio.publish"), publishCtrl);
		segmentButtonsCmp.setSelectedButton(publishLink);
		return publishCtrl;
	}
	
	private BinderAssessmentController doOpenAssessment(UserRequest ureq) {
		OLATResourceable bindersOres = OresHelper.createOLATResourceableInstance("Assessment", 0l);
		WindowControl swControl = addToHistory(ureq, bindersOres, null);
		assessmentCtrl = new BinderAssessmentController(ureq, swControl, secCallback, binder, config);
		listenTo(assessmentCtrl);

		popUpToBinderController(ureq);
		stackPanel.pushController(translate("portfolio.assessment"), assessmentCtrl);
		segmentButtonsCmp.setSelectedButton(assessmentLink);
		return assessmentCtrl;
	}
	
	private HistoryController doOpenHistory(UserRequest ureq) {
		OLATResourceable bindersOres = OresHelper.createOLATResourceableInstance("History", 0l);
		WindowControl swControl = addToHistory(ureq, bindersOres, null);
		historyCtrl = new HistoryController(ureq, swControl, binder);
		listenTo(historyCtrl);
		
		popUpToBinderController(ureq);
		stackPanel.pushController(translate("portfolio.history"), historyCtrl);
		segmentButtonsCmp.setSelectedButton(historyLink);
		return historyCtrl;
	}
	
	private void doOpenOptions(UserRequest ureq) {
		OLATResourceable bindersOres = OresHelper.createOLATResourceableInstance("Options", 0l);
		WindowControl swControl = addToHistory(ureq, bindersOres, null);
		optionsCtrl = new BinderDeliveryOptionsController(ureq, swControl, binder);
		listenTo(optionsCtrl);
		
		popUpToBinderController(ureq);
		stackPanel.pushController(translate("portfolio.template.options"), optionsCtrl);
		segmentButtonsCmp.setSelectedButton(optionsLink);
	}
}