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
package org.olat.portfolio.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.MainPanel;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.portfolio.manager.EPFrontendManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EPOverviewController extends BasicController implements Activateable2 {
	
	private EPMapRunController myMapsCtrl;
	private EPMapRunController myTasksCtrl;
	private EPMapRunController publicMapsCtrl;
	private EPArtefactPoolRunController artefactsCtrl;
	
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link myArtefactLink, myMapLink, myTaskLink, publicMapLink;
	
	@Autowired
	private EPFrontendManager ePFMgr;
	
	public EPOverviewController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("segments");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		myArtefactLink = LinkFactory.createLink("myartefacts.menu.title", mainVC, this);
		myArtefactLink.setElementCssClass("o_sel_ep_my_artfeacts");
		segmentView.addSegment(myArtefactLink, true);
		myMapLink = LinkFactory.createLink("mymaps.menu.title", mainVC, this);
		myMapLink.setElementCssClass("o_sel_ep_my_maps");
		segmentView.addSegment(myMapLink, false);
		myTaskLink = LinkFactory.createLink("mystructuredmaps.menu.title", mainVC, this);
		myTaskLink.setElementCssClass("o_sel_ep_my_tasks");
		segmentView.addSegment(myTaskLink, false);
		publicMapLink = LinkFactory.createLink("othermaps.menu.title", mainVC, this);
		publicMapLink.setElementCssClass("o_sel_ep_public_maps");
		segmentView.addSegment(publicMapLink, false);
		
		doOpenMyArtefacts(ureq);

		MainPanel panel = new MainPanel("portfolio");
		panel.setContent(mainVC);
		putInitialPanel(panel);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == myArtefactLink) {
					doOpenMyArtefacts(ureq);
				} else if (clickedLink == myMapLink) {
					doOpenMyMaps(ureq);
				} else if (clickedLink == myTaskLink) {
					doOpenMyTasks(ureq);
				} else if (clickedLink == publicMapLink) {
					doOpenPublicMaps(ureq);
				}
			}
		}
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("EPDefaultMap".equalsIgnoreCase(type)) {
			Long mapKey = entries.get(0).getOLATResourceable().getResourceableId();
			if(mapKey == 0l) {
				doOpenMyMaps(ureq).activate(ureq, entries, state);
				segmentView.select(myMapLink);
			} else {
				boolean owner = ePFMgr.isMapOwner(getIdentity(), mapKey);
				if(owner) {
					doOpenMyMaps(ureq).activate(ureq, entries, state);
					segmentView.select(myMapLink);
				} else {
					doOpenPublicMaps(ureq).activate(ureq, entries, state);
					segmentView.select(publicMapLink);
				}
			}
		} else if("EPStructuredMap".equalsIgnoreCase(type)) {
			doOpenMyTasks(ureq).activate(ureq, entries, state);
			segmentView.select(myTaskLink);
		} else if("Artefact".equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenMyArtefacts(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
			segmentView.select(myArtefactLink);
		}
	}

	private EPArtefactPoolRunController doOpenMyArtefacts(UserRequest ureq) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Artefact", 0l);
		addToHistory(ureq, ores, null);// pool run controller set its own business path after
		if(artefactsCtrl == null) {
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			artefactsCtrl =  new EPArtefactPoolRunController(ureq, addToHistory(ureq, bwControl));
			listenTo(artefactsCtrl);
		}
		mainVC.put("segmentCmp", artefactsCtrl.getInitialComponent());
		return artefactsCtrl;
	}
	
	private EPMapRunController doOpenMyMaps(UserRequest ureq) {
		if(myMapsCtrl == null) {
			myMapsCtrl = new EPMapRunController(ureq, getWindowControl(), true, EPMapRunViewOption.MY_DEFAULTS_MAPS, null);
			listenTo(myMapsCtrl);
		}
		mainVC.put("segmentCmp", myMapsCtrl.getInitialComponent());
		addToHistory(ureq, OresHelper.createOLATResourceableType("EPDefaultMap"), null);
		return myMapsCtrl;
	}
	
	private EPMapRunController doOpenMyTasks(UserRequest ureq) {
		if(myTasksCtrl == null) {
			myTasksCtrl = new EPMapRunController(ureq, getWindowControl(), false, EPMapRunViewOption.MY_EXERCISES_MAPS, null);
			listenTo(myTasksCtrl);
		}
		mainVC.put("segmentCmp", myTasksCtrl.getInitialComponent());
		addToHistory(ureq, OresHelper.createOLATResourceableType("EPStructuredMap"), null);
		return myTasksCtrl;
	}
	
	private EPMapRunController doOpenPublicMaps(UserRequest ureq) {
		if(publicMapsCtrl == null) {
			publicMapsCtrl = new EPMapRunController(ureq, getWindowControl(), false, EPMapRunViewOption.OTHERS_MAPS, null);
			listenTo(publicMapsCtrl);
		}
		mainVC.put("segmentCmp", publicMapsCtrl.getInitialComponent());
		addToHistory(ureq, OresHelper.createOLATResourceableInstance("EPDefaultMap", 0l), null);
		return publicMapsCtrl;
	}
}
