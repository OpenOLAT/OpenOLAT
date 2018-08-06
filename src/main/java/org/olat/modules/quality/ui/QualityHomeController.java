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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.quality.QualitySecurityCallback;
import org.olat.modules.quality.generator.ui.GeneratorListController;

/**
 * 
 * Initial date: 07.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityHomeController extends BasicController implements Activateable2{

	private static final String ORES_MY_TYPE = "my";
	private static final String ORES_DATA_COLLECTIONS_TYPE = "datacollections";
	private static final String ORES_GENERATORS_TYPE = "generators";
	
	private final VelocityContainer mainVC;
	private Link dataCollectionLink;
	private Link executorParticipationLink;
	private Link generatorsLink;
	
	private final TooledStackedPanel stackPanel;
	private DataCollectionListController dataCollectionListCtrl;
	private ExecutorParticipationsListController executorParticipationListCtrl;
	private GeneratorListController generatorsListCtrl;
	
	private final QualitySecurityCallback secCallback;
	
	public QualityHomeController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, QualitySecurityCallback secCallback) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		this.stackPanel.setToolbarAutoEnabled(true);
		this.secCallback = secCallback;
		
		mainVC = createVelocityContainer("home");

		List<PanelWrapper> wrappers = new ArrayList<>(6);
		executorParticipationLink = LinkFactory.createLink("goto.executor.participation.link", mainVC, this);
		executorParticipationLink.setIconRightCSS("o_icon o_icon_start");
		wrappers.add(new PanelWrapper(translate("goto.executor.participation.title"),
				translate("goto.executor.participation.help"), executorParticipationLink));
		
		if (secCallback.canViewDataCollections()) {
			dataCollectionLink = LinkFactory.createLink("goto.data.collection.link", mainVC, this);
			dataCollectionLink.setIconRightCSS("o_icon o_icon_start");
			wrappers.add(new PanelWrapper(translate("goto.data.collection.title"),
					translate("goto.data.collection.help"), dataCollectionLink));
		}
		
		if (secCallback.canViewGenerators()) {
			generatorsLink = LinkFactory.createLink("goto.generator.link", mainVC, this);
			generatorsLink.setIconRightCSS("o_icon o_icon_start");
			wrappers.add(new PanelWrapper(translate("goto.generator.title"),
					translate("goto.generator.help"), generatorsLink));
		}
		
		mainVC.contextPut("panels", wrappers);
		
		putInitialPanel(mainVC);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) {
			if (canOnlyExecute()) {
				doOpenUserParticipations(ureq);
			}
			return;
		}
		
		OLATResourceable resource = entries.get(0).getOLATResourceable();
		if (ORES_MY_TYPE.equalsIgnoreCase(resource.getResourceableTypeName())) {
			doOpenUserParticipations(ureq);
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			executorParticipationListCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if (ORES_DATA_COLLECTIONS_TYPE.equalsIgnoreCase(resource.getResourceableTypeName())
				&& secCallback.canViewDataCollections()) {
			doOpenDataCollection(ureq);
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			dataCollectionListCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if (ORES_GENERATORS_TYPE.equalsIgnoreCase(resource.getResourceableTypeName())
				&& secCallback.canViewGenerators()) {
			doOpenGenerators(ureq);
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			generatorsListCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if (canOnlyExecute()) {
			doOpenUserParticipations(ureq);
		}
	}

	private boolean canOnlyExecute() {
		return !secCallback.canViewDataCollections();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (executorParticipationLink == source) {
			doOpenUserParticipations(ureq);
		} else if (dataCollectionLink == source) {
			doOpenDataCollection(ureq);
		} else if (generatorsLink == source) {
			doOpenGenerators(ureq);
		}
	}

	private void doOpenUserParticipations(UserRequest ureq) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ORES_MY_TYPE, 0l);
		WindowControl bwControl = addToHistory(ureq, ores, null);
		executorParticipationListCtrl = new ExecutorParticipationsListController(ureq, bwControl, stackPanel, secCallback);
		listenTo(executorParticipationListCtrl);
		stackPanel.pushController(translate("breadcrumb.executor.participations"), executorParticipationListCtrl);
	}

	private void doOpenDataCollection(UserRequest ureq) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ORES_DATA_COLLECTIONS_TYPE, 0l);
		WindowControl bwControl = addToHistory(ureq, ores, null);
		dataCollectionListCtrl = new DataCollectionListController(ureq, bwControl, stackPanel, secCallback);
		listenTo(dataCollectionListCtrl);
		stackPanel.pushController(translate("breadcrumb.data.collections"), dataCollectionListCtrl);
	}

	private void doOpenGenerators(UserRequest ureq) {
		stackPanel.popUpToRootController(ureq);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ORES_GENERATORS_TYPE, 0l);
		WindowControl bwControl = addToHistory(ureq, ores, null);
		generatorsListCtrl = new GeneratorListController(ureq, bwControl, stackPanel, secCallback);
		listenTo(generatorsListCtrl);
		stackPanel.pushController(translate("breadcrumb.generators"), generatorsListCtrl);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public static class PanelWrapper {
		
		private final String title;
		private final String helpText;
		private final Component link;
		
		public PanelWrapper(String title, String helpText, Component link) {
			this.title = title;
			this.helpText = helpText;
			this.link = link;
		}

		public String getTitle() {
			return title;
		}

		public String getHelpText() {
			return helpText;
		}

		public Component getLink() {
			return link;
		}
		
	}
}
