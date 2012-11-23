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

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Initial date: 16.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class HibernateStatisticsController extends BasicController {
	
	private Link enableLink;
	private Link disableLink;
	private Link clearLink;
	private VelocityContainer mainVC;

	public HibernateStatisticsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("hibernateinfo");
		enableLink = LinkFactory.createButton("enable.hibernate.statistics", mainVC, this);
		disableLink = LinkFactory.createButton("disable.hibernate.statistics", mainVC, this);
		clearLink = LinkFactory.createButton("clear.hibernate.statistics", mainVC, this);
		
		loadModel();
		putInitialPanel(mainVC);
	}
	
	public void loadModel() {
		mainVC.contextPut("isStatisticsEnabled", DBFactory.getInstance(false).getStatistics().isStatisticsEnabled());
		mainVC.contextPut("hibernateStatistics", DBFactory.getInstance(false).getStatistics());
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == enableLink){
			DBFactory.getInstance(false).getStatistics().setStatisticsEnabled(true);
			mainVC.contextPut("isStatisticsEnabled", DBFactory.getInstance(false).getStatistics().isStatisticsEnabled());
			getWindowControl().setInfo("Hibernate statistics enabled.");
			loadModel();
		} else if (source == disableLink){
			DBFactory.getInstance(false).getStatistics().setStatisticsEnabled(false);
			mainVC.contextPut("isStatisticsEnabled", DBFactory.getInstance(false).getStatistics().isStatisticsEnabled());
			getWindowControl().setInfo("Hibernate statistics disabled.");
			loadModel();
		} else if (source == clearLink){
			DBFactory.getInstance(false).getStatistics().clear();
			getWindowControl().setInfo("Hibernate statistics clear done.");
			loadModel();
		}
	}
	
	


	
	

}
