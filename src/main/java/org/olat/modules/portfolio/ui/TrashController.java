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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.ui.event.DeleteBinderEvent;
import org.olat.modules.portfolio.ui.event.PageDeletedEvent;
import org.olat.modules.portfolio.ui.event.RestoreBinderEvent;

/**
 * 
 * Initial date: 3 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TrashController extends BasicController {
	
	private final VelocityContainer mainVC;
	private final Link pagesLink, bindersLink;
	private final SegmentViewComponent segmentView;
	
	private DeletedPageListController pagesCtrl;
	private DeletedBinderController bindersCtrl;
	
	private final TooledStackedPanel stackPanel;
	private final BinderSecurityCallback secCallback;
	
	public TrashController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BinderSecurityCallback secCallback) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		this.secCallback = secCallback;
		
		mainVC = createVelocityContainer("trash");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		pagesLink = LinkFactory.createLink("pages", mainVC, this);
		segmentView.addSegment(pagesLink, true);
		bindersLink = LinkFactory.createLink("binders", mainVC, this);
		segmentView.addSegment(bindersLink, false);
		
		doOpenPages(ureq);
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == pagesLink) {
					doOpenPages(ureq);
				} else if (clickedLink == bindersLink) {
					doOpenBinders(ureq);
				}
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof RestoreBinderEvent
				|| event instanceof DeleteBinderEvent
				|| event instanceof PageDeletedEvent) {
			stackPanel.popUpToController(this);
		} 
		super.event(ureq, source, event);
	}

	private void doOpenPages(UserRequest ureq) {
		if(pagesCtrl == null) {
			pagesCtrl = new DeletedPageListController(ureq, getWindowControl(), stackPanel, secCallback);
			listenTo(pagesCtrl);
		}
		mainVC.put("segmentCmp", pagesCtrl.getInitialComponent());	
	}
	
	private void doOpenBinders(UserRequest ureq) {
		if(bindersCtrl == null) {
			bindersCtrl = new DeletedBinderController(ureq, this.getWindowControl(), stackPanel);
			listenTo(bindersCtrl);
		}
		mainVC.put("segmentCmp", bindersCtrl.getInitialComponent());
	}
}
