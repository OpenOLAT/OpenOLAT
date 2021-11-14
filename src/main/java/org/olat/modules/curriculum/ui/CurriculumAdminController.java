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
package org.olat.modules.curriculum.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.CurriculumModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumAdminController extends BasicController {
	
	private final Link configurationLink;
	private final Link curriculumElementTypeListLink;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	
	private CurriculumAdminConfigurationController configurationCtrl;
	private CurriculumElementTypesEditController elementTypeListCtrl;
	
	@Autowired
	private CurriculumModule curriculumModule;
	
	public CurriculumAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("curriculum_admin");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		configurationLink = LinkFactory.createLink("curriculum.configuration", mainVC, this);
		segmentView.addSegment(configurationLink, true);
		curriculumElementTypeListLink = LinkFactory.createLink("curriculum.element.types", mainVC, this);
		doOpenConfiguration(ureq);
		if(curriculumModule.isEnabled()) {
			segmentView.addSegment(curriculumElementTypeListLink, false);
		}
		
		mainVC.put("segmentCmp", configurationCtrl.getInitialComponent());
		putInitialPanel(mainVC);

	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(configurationCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				segmentView.removeSegment(curriculumElementTypeListLink);
				if(curriculumModule.isEnabled()) {
					segmentView.addSegment(curriculumElementTypeListLink, false);
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == configurationLink) {
					doOpenConfiguration(ureq);
				} else if (clickedLink == curriculumElementTypeListLink){
					doOpenCurriculumElementTypes(ureq);
				}
			}
		}
	}
	
	private void doOpenConfiguration(UserRequest ureq) {
		if(configurationCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Configuration"), null);
			configurationCtrl = new CurriculumAdminConfigurationController(ureq, bwControl);
			listenTo(configurationCtrl);
		}
		addToHistory(ureq, configurationCtrl);
		mainVC.put("segmentCmp", configurationCtrl.getInitialComponent());
	}
	
	private void doOpenCurriculumElementTypes(UserRequest ureq) {
		if(elementTypeListCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Types"), null);
			elementTypeListCtrl = new CurriculumElementTypesEditController(ureq, bwControl);
			listenTo(elementTypeListCtrl);
		}
		addToHistory(ureq, elementTypeListCtrl);
		mainVC.put("segmentCmp", elementTypeListCtrl.getInitialComponent());
	}

}
