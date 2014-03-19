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
package org.olat.home;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.extensions.ExtManager;
import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.StackedController;
import org.olat.core.gui.components.stack.StackedControllerAware;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * Initial date: 27.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HomeMainController extends MainLayoutBasicController implements StackedController, Activateable2 {

	private Link backLink;
	private Panel content;
	private LayoutMain3ColsController contentCtr;
	private VelocityContainer stackVC;
	private final List<Link> stack = new ArrayList<Link>(3);
	
	public HomeMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		init();
	}
	
	private void init() {
		content = new Panel("content");
		
		//create the stack
		String stackPage = Util.getPackageVelocityRoot(StackedController.class) + "/stack.html";
		stackVC = new VelocityContainer(null, "vc_stack", stackPage, getTranslator(), this);
		//back link
		backLink = LinkFactory.createCustomLink("back", "back", null, Link.NONTRANSLATED + Link.LINK_CUSTOM_CSS, stackVC, this);
		backLink.setCustomEnabledLinkCSS("b_breadcumb_back");
		backLink.setCustomDisplayText("\u25C4"); // unicode back arrow (black left pointer symbol)
		backLink.setTitle(translate("back"));
		backLink.setAccessKey("b"); // allow navigation using keyboard
		stackVC.put("back", backLink);
		//add the root
		Link link = LinkFactory.createLink("gcrumb_root", stackVC, this);
		link.setCustomDisplayText("");
		link.setUserObject(this);
		stack.add(link);
		stackVC.contextPut("breadCrumbs", stack);
		
		putInitialPanel(stackVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void popUpToRootController(UserRequest ureq) {
		if(stack.size() > 1) {
			Controller popedCtrl = null;
			for(int i=stack.size(); i-->1; ) {
				Link link = stack.remove(i);
				popedCtrl = (Controller)link.getUserObject();
				popedCtrl.dispose();
			}
			
			//set the root controller
			Link rootLink = stack.get(0);
			Controller rootController  = (Controller)rootLink.getUserObject();
			if(rootController == this) {
				content.setContent(contentCtr.getInitialComponent());
			} else {
				content.setContent(rootController.getInitialComponent());
			}
		}
	}

	@Override
	public void pushController(String displayName, Controller controller) {
		Link link = LinkFactory.createLink("gcrumb_" + stack.size(), stackVC, this);
		link.setCustomDisplayText(displayName);
		link.setUserObject(controller);
		stack.add(link);
		content.setContent(controller.getInitialComponent());
	}

	@Override
	public void popController(Controller controller) {
		popController(controller.getInitialComponent());
	}
	
	private void popController(Component source) {
		int index = stack.indexOf(source);
		if(index < (stack.size() - 1)) {
			Controller popedCtrl = null;
			for(int i=stack.size(); i-->(index+1); ) {
				Link link = stack.remove(i);
				popedCtrl = (Controller)link.getUserObject();
				popedCtrl.dispose();
			}

			Link currentLink = stack.get(index);
			Controller currentCtrl  = (Controller)currentLink.getUserObject();
			if(currentCtrl == this) {
				content.setContent(contentCtr.getInitialComponent());
			} else {
				content.setContent(currentCtrl.getInitialComponent());
			}
			stackVC.setDirty(true);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source.equals(backLink)) {
			if (stack.size() > 1) {
				// back means to one level down, change source to the stack item one below current
				source = stack.get(stack.size()-2);
				// now continue as if user manually pressed a stack item in the list
			}
		}
		if(stack.contains(source)) {
			popController(source);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String navKey = entry.getOLATResourceable().getResourceableTypeName();
		if("HomeSite".equals(navKey)) {
			entries = entries.subList(1, entries.size());
			if(entries.size() > 0) {
				entry = entries.get(0);
				navKey = entry.getOLATResourceable().getResourceableTypeName();
			}
		}
		
		GenericActionExtension gAE = ExtManager.getInstance()
				.getActionExtensioByNavigationKey(HomeMainController.class.getName(), navKey);
		if (gAE != null) {
			popUpToRootController(ureq);

			Controller innerContentCtr = createController(gAE, ureq);
			contentCtr = new LayoutMain3ColsController(ureq, getWindowControl(), innerContentCtr);
			listenTo(contentCtr);
			if (entries.size() >= 1) {
				entries = entries.subList(1, entries.size());
			}
			if (innerContentCtr instanceof Activateable2) {
				((Activateable2) innerContentCtr).activate(ureq, entries, entry.getTransientState());
			}
			stackVC.put("content", contentCtr.getInitialComponent());
		}
	}
	
	protected Controller createController(GenericActionExtension ae, UserRequest ureq) {
		WindowControl bwControl = getWindowControl();

		// get our ores for the extension
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ae.getNavigationKey(), 0L);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		bwControl = addToHistory(ureq, ores, null);

		Controller ctrl = ae.createController(ureq, bwControl, null);
		if(ctrl instanceof StackedControllerAware) {
			((StackedControllerAware)ctrl).setStackedController(this);
		}
		return ctrl;
	}
}
