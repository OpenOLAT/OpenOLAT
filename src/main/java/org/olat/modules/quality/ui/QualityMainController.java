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
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.quality.QualitySecurityCallback;

/**
 * 
 * Initial date: 08.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityMainController extends MainLayoutBasicController implements Activateable2 {
	
	private static final String ORES_MY_TYPE = "my";
	private static final String ORES_DATA_COLLECTIONS_TYPE = "datacollections";
	private static final String SEGMENTS_CMP = "segmentCmp";
	
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private Link dataCollectionLink;
	private Link executorParticipationLink;
	
	private TooledStackedPanel stackPanel;
	private DataCollectionListController dataCollectionListCtrl;
	private ExecutorParticipationsListController executorParticipationListCtrl;
	
	private final QualitySecurityCallback secCallback;
	
	public QualityMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		this.secCallback = new QualitySecurityCallbackImpl();
		
		mainVC = createVelocityContainer("main");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		executorParticipationLink = LinkFactory.createLink("segments.executor.participation", mainVC, this);
		segmentView.addSegment(executorParticipationLink, true);
		if (secCallback.canViewDataCollections()) {
			dataCollectionLink = LinkFactory.createLink("segments.data.collection", mainVC, this);
			segmentView.addSegment(dataCollectionLink, false);
		}
		
		doOpenUserParticipations(ureq);
		
		putInitialPanel(mainVC);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) return;
		
		OLATResourceable resource = entries.get(0).getOLATResourceable();
		if (ORES_MY_TYPE.equalsIgnoreCase(resource.getResourceableTypeName())) {
			doOpenUserParticipations(ureq);
			segmentView.select(executorParticipationLink);
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			executorParticipationListCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if (ORES_DATA_COLLECTIONS_TYPE.equalsIgnoreCase(resource.getResourceableTypeName())) {
			if (secCallback.canViewDataCollections()) {
				doOpenDataCollection(ureq);
				segmentView.select(dataCollectionLink);
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				dataCollectionListCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
			} else {
				doOpenUserParticipations(ureq);
				segmentView.select(executorParticipationLink);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView && event instanceof SegmentViewEvent) {
			SegmentViewEvent sve = (SegmentViewEvent) event;
			String segmentCName = sve.getComponentName();
			Component clickedLink = mainVC.getComponent(segmentCName);
			if (clickedLink == executorParticipationLink) {
				doOpenUserParticipations(ureq);
			} else if (clickedLink == dataCollectionLink) {
				doOpenDataCollection(ureq);
			}
		}
	}

	private void doOpenUserParticipations(UserRequest ureq) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ORES_MY_TYPE, 0l);
		WindowControl bwControl = addToHistory(ureq, ores, null);
		executorParticipationListCtrl = new ExecutorParticipationsListController(ureq, bwControl, secCallback);
		mainVC.put(SEGMENTS_CMP, executorParticipationListCtrl.getInitialComponent());
	}

	private void doOpenDataCollection(UserRequest ureq) {
		stackPanel = new TooledStackedPanel("qualitiy.management", getTranslator(), this);
		stackPanel.setInvisibleCrumb(0);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ORES_DATA_COLLECTIONS_TYPE, 0l);
		WindowControl bwControl = addToHistory(ureq, ores, null);
		dataCollectionListCtrl = new DataCollectionListController(ureq, bwControl, stackPanel, secCallback);
		listenTo(dataCollectionListCtrl);
		stackPanel.pushController(translate("data.collections"), dataCollectionListCtrl);
		mainVC.put(SEGMENTS_CMP, stackPanel);
	}

	@Override
	protected void doDispose() {
		//
	}

}
