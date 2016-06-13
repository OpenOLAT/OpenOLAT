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

/**
 * 
 * Initial date: 06.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PortfolioHomeController extends BasicController implements Activateable2 {
	
	private Link myBindersLink, myEntriesLink;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackPanel;
	
	private MyPageListController myPageListCtrl;
	private BinderListController myPortfolioListCtrl;
	
	public PortfolioHomeController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;

		mainVC = createVelocityContainer("home");
		myBindersLink = LinkFactory.createLink("goto.my.binders", mainVC, this);
		myBindersLink.setIconRightCSS("o_icon o_icon-lg o_icon_start");
		
		myEntriesLink = LinkFactory.createLink("goto.my.pages", mainVC, this);
		myEntriesLink.setIconRightCSS("o_icon o_icon-lg o_icon_start");

		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(myBindersLink == source) {
			doOpenMyBinders(ureq);
		} else if(myEntriesLink == source) {
			doOpenMyPages(ureq);
		}
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("MyBinders".equalsIgnoreCase(resName)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenMyBinders(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
		}
	}
	
	private MyPageListController doOpenMyPages(UserRequest ureq) {
		removeAsListenerAndDispose(myPageListCtrl);
		
		OLATResourceable pagesOres = OresHelper.createOLATResourceableInstance("MyPages", 0l);
		WindowControl swControl = addToHistory(ureq, pagesOres, null);
		myPageListCtrl = new MyPageListController(ureq, swControl, stackPanel);
		listenTo(myPageListCtrl);
		stackPanel.pushController(translate("my.portfolio.pages.breadcrump"), myPageListCtrl);
		return myPageListCtrl;
	}
	
	private BinderListController doOpenMyBinders(UserRequest ureq) {
		removeAsListenerAndDispose(myPortfolioListCtrl);
		
		OLATResourceable bindersOres = OresHelper.createOLATResourceableInstance("MyBinders", 0l);
		WindowControl swControl = addToHistory(ureq, bindersOres, null);
		myPortfolioListCtrl = new BinderListController(ureq, swControl, stackPanel);
		listenTo(myPortfolioListCtrl);
		stackPanel.pushController(translate("my.portfolio.binders.breadcrump"), myPortfolioListCtrl);
		return myPortfolioListCtrl;
	}
}
