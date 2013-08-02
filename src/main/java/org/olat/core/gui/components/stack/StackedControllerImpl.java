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
package org.olat.core.gui.components.stack;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class StackedControllerImpl extends DefaultController implements StackedController {
	
	private final List<Link> stack = new ArrayList<Link>(3);
	private final VelocityContainer mainVC;
	private final Link backLink;
	
	public StackedControllerImpl(WindowControl wControl, Translator trans, String mainCssClass) {
		super(wControl);

		String path = Util.getPackageVelocityRoot(StackedController.class) + "/stack.html";
		mainVC = new VelocityContainer("vc_stacked", path, trans,  this);
		mainVC.contextPut("breadCrumbs", stack);
		if(StringHelper.containsNonWhitespace(mainCssClass)) {
			mainVC.contextPut("mainCssClass", mainCssClass);
		}
		// Add back link before the bread crumbs, when pressed delegates click to current bread-crumb - 1
		backLink = LinkFactory.createCustomLink("back", "back", null, Link.NONTRANSLATED + Link.LINK_CUSTOM_CSS, mainVC, this);
		backLink.setCustomEnabledLinkCSS("b_breadcumb_back");
		backLink.setCustomDisplayText("&#x25C4;"); // unicode back arrow (black left pointer symbol)
		backLink.setTitle(trans.translate("back"));
		backLink.setAccessKey("b"); // allow navigation using keyboard
		
		setInitialComponent(mainVC);
	}
	
	public void popController() {
		if(stack.size() > 1) {
			Link link = stack.remove(stack.size() - 1);
			Controller ctrl = (Controller)link.getUserObject();
			ctrl.dispose();
		}
	}
	
	@Override
	public void popController(Controller controller) {
		for(Link link:stack) {
			Controller popCtrl = (Controller)link.getUserObject();
			if(popCtrl == controller) {
				popController(link);
			}
		}
	}
	
	private Controller popController(Component source) {
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
			setContent(currentCtrl);
			return popedCtrl;
		}
		return null;
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
			setContent(rootController); 
			fireEvent(ureq, new PopEvent(popedCtrl));
		}
	}

	@Override
	public void pushController(String displayName, Controller controller) {
		Link link = LinkFactory.createLink("crumb_" + stack.size(), mainVC, this);
		link.setCustomDisplayText(StringHelper.escapeHtml(displayName));
		link.setUserObject(controller);
		stack.add(link);
		setContent(controller);
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
			Controller popedCtrl = popController(source);
			if(popedCtrl != null) {
				fireEvent(ureq, new PopEvent(popedCtrl));
			}
		}
	}
	
	private void setContent(Controller ctrl) {
		mainVC.put("content", ctrl.getInitialComponent());
	}

	@Override
	protected void doDispose() {
		//
	}
}