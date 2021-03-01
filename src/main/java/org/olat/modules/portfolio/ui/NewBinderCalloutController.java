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
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.portfolio.ui.event.NewBinderEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NewBinderCalloutController extends BasicController {
	
	private Link createBinderLink;
	private Link createBinderFromTemplateLink;
	private Link createBinderFromCourseLink;
	private Link createBinderFromEntriesLink;
	
	@Autowired
	private PortfolioV2Module portfolioModule;
	
	public NewBinderCalloutController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("new_binder_callout");
		if(portfolioModule.isLearnerCanCreateBinders()) {
			createBinderLink = LinkFactory.createLink("empty", "create.empty.binder", "new.empty", mainVC, this);
			mainVC.put("empty", createBinderLink);
		}
		if(portfolioModule.isCanCreateBindersFromTemplate()) {
			createBinderFromTemplateLink = LinkFactory.createLink("template", "create.empty.binder.from.template", "new.template", mainVC, this);
			mainVC.put("template", createBinderFromTemplateLink);
		}
		if(portfolioModule.isCanCreateBindersFromCourse()) {
			createBinderFromCourseLink = LinkFactory.createLink("course", "create.empty.binder.from.course", "new.course", mainVC, this);
			mainVC.put("course", createBinderFromCourseLink);
		}
		if(portfolioModule.isLearnerCanCreateBinders()) {
			createBinderFromEntriesLink = LinkFactory.createLink("entries", "create.binder.from.entries", "new.entries", mainVC, this);
			mainVC.put("entries", createBinderFromEntriesLink);
		}
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(createBinderLink == source) {
			fireEvent(ureq, new NewBinderEvent(NewBinderEvent.NEW_EMPTY));
		} else if(createBinderFromTemplateLink == source) {
			fireEvent(ureq, new NewBinderEvent(NewBinderEvent.NEW_EMPTY_FROM_TEMPLATE));
		} else if(createBinderFromCourseLink == source) {
			fireEvent(ureq, new NewBinderEvent(NewBinderEvent.NEW_EMPTY_FROM_COURSE));
		} else if(createBinderFromEntriesLink == source) {
			fireEvent(ureq, new NewBinderEvent(NewBinderEvent.NEW_FROM_ENTRIES));
		}
	}
}
