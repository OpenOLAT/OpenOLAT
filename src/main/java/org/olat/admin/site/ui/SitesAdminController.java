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
package org.olat.admin.site.ui;

import org.olat.core.CoreSpringFactory;
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
import org.olat.core.gui.control.navigation.SiteDefinitions;
import org.olat.course.site.model.CourseSiteConfiguration;
import org.olat.course.site.ui.CourseSiteAdminController;

/**
 * Administration of the sites:
 * <ul>
 * 	<li>Tab with the order, security callbacks and alternative controller for all sites
 *  <li>Configuration of the first info page
 *  <li>Configuration of the second info page
 * </ul>
 * Initial date: 20.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SitesAdminController  extends BasicController  {

	private final SiteDefinitions sitesModule;
	
	private final Link orderLink, courseSite1Link, courseSite2Link, courseSite3Link, courseSite4Link;
	private final SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;
	
	private SitesConfigurationController orderCtrl;
	private CourseSiteAdminController courseSite1Ctrl;
	private CourseSiteAdminController courseSite2Ctrl;
	private CourseSiteAdminController courseSite3Ctrl;
	private CourseSiteAdminController courseSite4Ctrl;
	
	public SitesAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		sitesModule = CoreSpringFactory.getImpl(SiteDefinitions.class);
		
		mainVC = createVelocityContainer("sites_admin");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		orderLink = LinkFactory.createLink("sites.order", mainVC, this);
		segmentView.addSegment(orderLink, true);
		
		courseSite1Link = LinkFactory.createLink("site.courseSite1", mainVC, this);
		segmentView.addSegment(courseSite1Link, false);
		courseSite2Link = LinkFactory.createLink("site.courseSite2", mainVC, this);
		segmentView.addSegment(courseSite2Link, false);
		courseSite3Link = LinkFactory.createLink("site.courseSite3", mainVC, this);
		segmentView.addSegment(courseSite3Link, false);
		courseSite4Link = LinkFactory.createLink("site.courseSite4", mainVC, this);
		segmentView.addSegment(courseSite4Link, false);
		
		doOpenAccountSettings(ureq);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == orderLink) {
					doOpenAccountSettings(ureq);
				} else if (clickedLink == courseSite1Link){
					doCourseSite1Settings(ureq);
				} else if (clickedLink == courseSite2Link){
					doCourseSite2Settings(ureq);
				} else if (clickedLink == courseSite3Link){
					doCourseSite3Settings(ureq);
				} else if (clickedLink == courseSite4Link){
					doCourseSite4Settings(ureq);
				}
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == courseSite1Ctrl) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				sitesModule.setConfigurationCourseSite1(courseSite1Ctrl.saveConfiguration());
			}
		} else if(source == courseSite2Ctrl) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				sitesModule.setConfigurationCourseSite2(courseSite2Ctrl.saveConfiguration());
			}
		} else if(source == courseSite3Ctrl) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				sitesModule.setConfigurationCourseSite3(courseSite3Ctrl.saveConfiguration());
			}
		} else if(source == courseSite4Ctrl) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				sitesModule.setConfigurationCourseSite4(courseSite4Ctrl.saveConfiguration());
			}
		}
		super.event(ureq, source, event);
	}

	private void doOpenAccountSettings(UserRequest ureq) {
		if(orderCtrl == null) {
			orderCtrl = new SitesConfigurationController(ureq, getWindowControl());
			listenTo(orderCtrl);
		} else {
			orderCtrl.reload();
		}
		mainVC.put("segmentCmp", orderCtrl.getInitialComponent());
	}
	
	private void doCourseSite1Settings(UserRequest ureq) {
		if(courseSite1Ctrl == null) {
			CourseSiteConfiguration siteConfiguration = sitesModule.getConfigurationCourseSite1();
			if(siteConfiguration == null) {
				siteConfiguration = new CourseSiteConfiguration();
			}
			courseSite1Ctrl = new CourseSiteAdminController(ureq, getWindowControl(), siteConfiguration);
			listenTo(courseSite1Ctrl);
		}
		mainVC.put("segmentCmp", courseSite1Ctrl.getInitialComponent());
	}
	
	private void doCourseSite2Settings(UserRequest ureq) {
		if(courseSite2Ctrl == null) {
			CourseSiteConfiguration siteConfiguration = sitesModule.getConfigurationCourseSite2();
			if(siteConfiguration == null) {
				siteConfiguration = new CourseSiteConfiguration();
			}
			courseSite2Ctrl = new CourseSiteAdminController(ureq, getWindowControl(), siteConfiguration);
			listenTo(courseSite2Ctrl);
		}
		mainVC.put("segmentCmp", courseSite2Ctrl.getInitialComponent());
	}
	
	private void doCourseSite3Settings(UserRequest ureq) {
		if(courseSite3Ctrl == null) {
			CourseSiteConfiguration siteConfiguration = sitesModule.getConfigurationCourseSite3();
			if(siteConfiguration == null) {
				siteConfiguration = new CourseSiteConfiguration();
			}
			courseSite3Ctrl = new CourseSiteAdminController(ureq, getWindowControl(), siteConfiguration);
			listenTo(courseSite3Ctrl);
		}
		mainVC.put("segmentCmp", courseSite3Ctrl.getInitialComponent());
	}
	
	private void doCourseSite4Settings(UserRequest ureq) {
		if(courseSite4Ctrl == null) {
			CourseSiteConfiguration siteConfiguration = sitesModule.getConfigurationCourseSite4();
			if(siteConfiguration == null) {
				siteConfiguration = new CourseSiteConfiguration();
			}
			courseSite4Ctrl = new CourseSiteAdminController(ureq, getWindowControl(), siteConfiguration);
			listenTo(courseSite4Ctrl);
		}
		mainVC.put("segmentCmp", courseSite4Ctrl.getInitialComponent());
	}
}