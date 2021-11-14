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
package org.olat.admin.sysinfo;

import org.hibernate.stat.Statistics;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 03.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DatabaseController extends BasicController {
	
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link enableLink, disableLink, clearLink, statisticsLink, queriesLink, entitiesLink;
	
	private HibernateQueriesController queriesCtrl;
	private HibernateEntitiesController entitiesCtrl;
	private HibernateStatisticsController statisticsCtrl;
	
	@Autowired
	private DB dbInstance;
	
	public DatabaseController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("hibernate_segments");
		enableLink = LinkFactory.createButton("enable.hibernate.statistics", mainVC, this);
		disableLink = LinkFactory.createButton("disable.hibernate.statistics", mainVC, this);
		clearLink = LinkFactory.createButton("clear.hibernate.statistics", mainVC, this);
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		statisticsLink = LinkFactory.createLink("hibernate.statistics", mainVC, this);
		segmentView.addSegment(statisticsLink, true);
		doOpenStatistics(ureq);
		queriesLink = LinkFactory.createLink("hibernate.queries", mainVC, this);
		segmentView.addSegment(queriesLink, false);
		entitiesLink = LinkFactory.createLink("hibernate.entities", mainVC, this);
		segmentView.addSegment(entitiesLink, false);
		
		Statistics statistics = dbInstance.getStatistics();
		mainVC.contextPut("isStatisticsEnabled", statistics.isStatisticsEnabled());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		Statistics statistics = dbInstance.getStatistics();
		if (source == enableLink){
			statistics.setStatisticsEnabled(true);
			mainVC.contextPut("isStatisticsEnabled",statistics.isStatisticsEnabled());
			getWindowControl().setInfo("Hibernate statistics enabled.");
		} else if (source == disableLink){
			statistics.setStatisticsEnabled(false);
			mainVC.contextPut("isStatisticsEnabled", statistics.isStatisticsEnabled());
			getWindowControl().setInfo("Hibernate statistics disabled.");
		} else if (source == clearLink){
			statistics.clear();
			getWindowControl().setInfo("Hibernate statistics clear done.");
		} else if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == statisticsLink) {
					doOpenStatistics(ureq);
				} else if (clickedLink == queriesLink) {
					doOpenQueries(ureq);
				} else if (clickedLink == entitiesLink) {
					doOpenEntities(ureq);
				}
			}
		} 
	}
	
	private void doOpenStatistics(UserRequest ureq) {
		if(statisticsCtrl == null) {
			statisticsCtrl = new HibernateStatisticsController(ureq, getWindowControl());
			listenTo(statisticsCtrl);
		} else {
			statisticsCtrl.loadModel();
		}
		mainVC.put("segmentCmp", statisticsCtrl.getInitialComponent());
	}
	
	private void doOpenQueries(UserRequest ureq) {
		if(queriesCtrl == null) {
			queriesCtrl = new HibernateQueriesController(ureq, getWindowControl());
			listenTo(queriesCtrl);
		} else {
			queriesCtrl.loadModel();
		}
		mainVC.put("segmentCmp", queriesCtrl.getInitialComponent());
	}
	
	private void doOpenEntities(UserRequest ureq) {
		if(entitiesCtrl == null) {
			entitiesCtrl = new HibernateEntitiesController(ureq, getWindowControl());
			listenTo(entitiesCtrl);
		} else {
			entitiesCtrl.loadModel();
		}
		mainVC.put("segmentCmp", entitiesCtrl.getInitialComponent());
	}
}
