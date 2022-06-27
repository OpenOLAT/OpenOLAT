/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
* Initial code contributed and copyrighted by<br>
* JGS goodsolutions GmbH, http://www.goodsolutions.ch
* <p>
*/
package org.olat.core.gui.dev.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.Container;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.util.ComponentUtil;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.floatingresizabledialog.FloatingResizableDialogController;
import org.olat.core.gui.control.generic.spacesaver.ExpColController;
import org.olat.core.gui.control.winmgr.WindowBackOfficeImpl;
import org.olat.core.gui.control.winmgr.WindowManagerImpl;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.intercept.DebugHelper;
import org.olat.core.gui.util.bandwidth.SlowBandWidthSimulator;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  20.05.2006 <br>
 *
 * @author Felix Jost
 */
public class DevelopmentController extends BasicController {
	private VelocityContainer myContent;
	private Panel mainpanel;
	
	private final WindowBackOfficeImpl wboImpl;
	private ExpColController spacesaverController;
	private Component mainComp;
	
	private Link web10Link;
	private Link web20Link;
	private Link showComponentTree;
	
	private List<Link> modes = new ArrayList<>();
	
	private Link chosenMode;
	private WindowManagerImpl winMgrImpl;
	private Link debugLink;
	private boolean treeShown = false;

	private int pageCnt =0; // only for visual indication
	
	// fast polling while developing: no more need to push browser reload! 
	private Link toggleAutorefresh;
	private boolean autorefresh = false;
	private Controller floatCtr;
	private Controller bandwithController;
	private Link devToolLink;

	/**
	 * @param ureq
	 * @param wControl
	 * @param navElem
	 */
	public DevelopmentController(UserRequest ureq, WindowControl wControl, WindowBackOfficeImpl wboImpl) {
		super(ureq, wControl);
		this.wboImpl = wboImpl;
		this.winMgrImpl = wboImpl.getWindowManager();

		// set up the main layout
		myContent = createVelocityContainer("index");
		
		// create four links to switch between modes.
		// a special case here: these link must work in regular mode (normal uri with full screen refresh (as 
		// opposed to partial page refresh )in order to switch modes correctly.
		// (grouping only needed for coloring)
		web10Link = LinkFactory.createLink("web10", myContent, this);
		web10Link.setAjaxEnabled(false);
		web20Link =LinkFactory.createLink("web20", myContent, this);
		web20Link.setAjaxEnabled(false);
		debugLink = LinkFactory.createLink("debug", myContent, this);
		debugLink.setAjaxEnabled(false);
		
		modes.add(web10Link);
		modes.add(web20Link);
		modes.add(debugLink);
		if (winMgrImpl.isAjaxEnabled()) {
			chosenMode = web20Link;
		} else {
			chosenMode = web10Link;
		}
		updateUI();
		
		// commands
		showComponentTree = LinkFactory.createButton("showComponentTree", myContent, this);
		showComponentTree.setAjaxEnabled(false);
		myContent.contextPut("compdump", "");
		myContent.contextPut("sys", this);
		
		toggleAutorefresh = LinkFactory.createButtonSmall("toggleAutorefresh", myContent, this);
		// do it with web 1.0 full page reload timer
		myContent.contextPut("autorefresh", "false");
		
		// slow bandwidth simulation
		SlowBandWidthSimulator sbs = Windows.getWindows(ureq).getSlowBandWidthSimulator();
		bandwithController = sbs.createAdminGUI().createController(ureq, getWindowControl());
		myContent.put("bandwidth",bandwithController.getInitialComponent());

		mainpanel = new Panel("developermainpanel");
		
		devToolLink = LinkFactory.createCustomLink("devTool", "devTool", "", Link.NONTRANSLATED, myContent, this);
		devToolLink.setIconLeftCSS("o_icon o_icon_dev o_icon-fw");
		devToolLink.setCustomEnabledLinkCSS("o_dev hidden-print");
		devToolLink.setTitle(translate("devTool"));
		devToolLink.setAriaLabel(translate("devTool"));

		Component protectedMainPanel = DebugHelper.createDebugProtectedWrapper(mainpanel);
		spacesaverController = new ExpColController(ureq, getWindowControl(), false, protectedMainPanel, devToolLink);
		mainComp = DebugHelper.createDebugProtectedWrapper(spacesaverController.getInitialComponent());
		putInitialPanel(mainComp);
	}
	
	/**
	 * [used by velocity]
	 * @return
	 */
	public String time() {
		return ""+System.currentTimeMillis();
	}
	
	/**
	 * [used by velocity]
	 * @return a hex color
	 */
	
	public String modthree() {
		int n = ++pageCnt % 3;
		switch (n) {
			case 0: return "FF0000";
			case 1: return "00FF00";
			case 2: return "0000FF";
			default: return "n/a"; // cannot happen
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == devToolLink) {
			removeAsListenerAndDispose(floatCtr);
			floatCtr = new FloatingResizableDialogController(ureq, getWindowControl(), myContent, "Brasato Development Tool", 1000, 200, 10, 60, true, true);
			listenTo(floatCtr);
			mainpanel.setContent(floatCtr.getInitialComponent());			
		} else if (source == web10Link) {
			// choose regular mode
			winMgrImpl.setShowDebugInfo(false);
			winMgrImpl.setAjaxEnabled(false);
			winMgrImpl.setIdDivsForced(false);
			chosenMode = web10Link;
			updateUI();
		} else if (source == web20Link) {
			// enable ajax / generic-dom-replacement GDR mode
			winMgrImpl.setShowDebugInfo(false);
			winMgrImpl.setAjaxEnabled(true);
			winMgrImpl.setIdDivsForced(false);
			chosenMode = web20Link;
			updateUI();
		} else if (source == debugLink) {
			// debug mode requires web 1.0 mode at the moment
			winMgrImpl.setShowDebugInfo(true);
			winMgrImpl.setAjaxEnabled(false);
			winMgrImpl.setIdDivsForced(false);
			chosenMode = debugLink;
			updateUI();
		} else if (source == showComponentTree) {
			if (treeShown) {
				// hide component tree
				myContent.contextPut("compdump", "");
				winMgrImpl.setIdDivsForced(false);
			} else {
				winMgrImpl.setIdDivsForced(true);
				updateComponentTree();				
			}
			treeShown = !treeShown;
		} else if (source == toggleAutorefresh) {
			autorefresh = !autorefresh;
			if (autorefresh) {
				myContent.contextPut("autorefresh", "true");
			} else {
				myContent.contextPut("autorefresh", "false");
			}
		} else if (event == ComponentUtil.VALIDATE_EVENT) {
			// todo update mode
			if (treeShown) {
				updateComponentTree();
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == floatCtr) {
			if (event.equals(Event.DONE_EVENT)) {
				spacesaverController.toggleUi();
			}
		}
	}

	private void updateComponentTree() {
		Window win = wboImpl.getWindow();
		try(StringOutput sb = new StringOutput()) {
			renderDebugInfo(win.getContentPane(), sb);
			myContent.contextPut("compdump", sb.toString());
		} catch(IOException e) {
			logError("", e);
		}
	}
	
	private void updateUI() {
		// update mode.
		for (Link li : modes) {
			myContent.contextPut(li.getComponentName() + "Active", (li == chosenMode ? Boolean.TRUE : Boolean.FALSE));
		}
		myContent.contextPut("compdump", "");
		
	}

	@Override
	protected void doDispose() {
		// floatCtr auto disposed by basic controller
		if (spacesaverController != null) spacesaverController.dispose();
		if (bandwithController != null) bandwithController.dispose();
        super.doDispose();
	}

	/**
	 * used by velocityrenderdecorator
	 * @param target
	 */
	private void renderDebugInfo(Component root, StringOutput target) {
		target.append("<div>");
		int cnt = cntTree(root);
		int size = DefaultController.getControllerCount();
		target.append("<strong>Component Tree:</strong> count: " + cnt + "&nbsp;&nbsp;|&nbsp;&nbsp;Controllers (global: active and not disposed): <strong>"+size+"</strong>");
		target.append("</div><div>");
		Map<Controller, List<Component>> controllerInfos = new HashMap<>(); 
		dumpTree(target, root, 0, controllerInfos);
		target.append("</div>");
		// now dump the controller info
		for (Controller controller : controllerInfos.keySet()) {
			try {
				Component initComp = controller.getInitialComponent();
				target.append("<div style=\"padding-bottom:2px; \"><strong>Controller "+controller.getClass().getName()+" :"+controller.hashCode());
				appendDivCodeForComponent("<i>Initial Component:</i> ",target, initComp, 20);
				List<Component> listenTo = controllerInfos.get(controller);
				for (Component component : listenTo) {
					appendDivCodeForComponent("", target, component, 20);
				}
				target.append("</strong></div><br />");
			} catch (Exception e) {
				// some components like window dont like being called for the initialcomponent
				// -> ignore
			}
		}
	}
	
	private void appendDivCodeForComponent(String pre, StringOutput sb, Component current, int marginLeft) {
		String pcid = Renderer.getComponentPrefix(current);
		sb.append("<div");
		if (current.isVisible() && current.isDomReplaceable()) {
			sb.append(" onMouseOver=\"jQuery(this).css('background-color','#f3feff');o_dbg_mark('").append(pcid)
			.append("')\" onMouseOut=\"jQuery(this).css('background-color','');o_dbg_unmark('").append(pcid).append("')\"");
		}
		sb.append(" style=\"color:blue; padding-bottom:2px; font-size:10px\"><div style=\"margin-left:"+marginLeft+"px\">");
		
		String cname = current.getClass().getName();
		cname = cname.substring(cname.lastIndexOf('.') + 1);
		sb.append(pre+"<b>" + cname  + "</b> (" + current.getComponentName() + " id "+current.getDispatchID()+") "); 
		sb.append((current.isVisible()? "":"INVISIBLE ")+(current.isEnabled()? "":" NOT ENABLED ")+current.getExtendedDebugInfo()+", "+current.getListenerInfo()+"<br />");
		sb.append("</div></div>");
	}

	private void dumpTree(StringOutput sb, Component current, int indent, Map<Controller, List<Component>> controllerInfos) {
		// add infos, 
		Controller lController = org.olat.core.gui.dev.Util.getListeningControllerFor(current);
		if (lController != null) {
			List<Component> lcomps = controllerInfos.get(lController);
			if (lcomps == null) {
				// first entry
				lcomps = new ArrayList<>();
				controllerInfos.put(lController, lcomps);
			}
			lcomps.add(current);
		}
		
		int pxInd = indent * 25;
		String pcid = Renderer.getComponentPrefix(current);
		sb.append("<div id='dmt_").append(pcid).append("' ");
		if (current.isVisible() && current.isDomReplaceable()) {
			sb.append(" onMouseOver=\"jQuery(this).css('background-color','#f3feff');o_dbg_mark('").append(pcid)
			.append("')\" onMouseOut=\"jQuery(this).css('background-color','');o_dbg_unmark('").append(pcid).append("')\"");
		}
		sb.append(" style=\"color:blue; padding-bottom:2px; font-size:10px\"><div style=\"margin-left:"+pxInd+"px\">");
		
		String cname = current.getClass().getName();
		cname = cname.substring(cname.lastIndexOf('.') + 1);
		
		sb.append("<b>" + cname  + "</b> (" + current.getComponentName() + " id "+current.getDispatchID()+") ");
		if (current == mainComp) { // suppress detail and subtree for our controller here
			sb.append(" --suppressing output, since developmentcontroller --</div></div>");
		} else {
				sb.append((current.isVisible()? "":"INVISIBLE ")+(current.isEnabled()? "":" NOT ENABLED ")+current.getExtendedDebugInfo()+", "+current.getListenerInfo()+"<br />");
			sb.append("</div></div>");
			if (current instanceof Container) {
				Container co = (Container) current;
				for (Component child:co.getComponents()) {
					dumpTree(sb, child, indent + 1, controllerInfos);
				}
			}
		}
	}
	
	private int cntTree(Component current) {
		int cnt = 1;
		if (current instanceof ComponentCollection) {
			ComponentCollection co = (ComponentCollection) current;
			for (Component child:co.getComponents()) {
				cnt += cntTree(child);
			}
		}
		return cnt;
	}
}