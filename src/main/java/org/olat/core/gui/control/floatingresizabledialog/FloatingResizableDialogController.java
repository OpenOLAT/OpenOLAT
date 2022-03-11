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
*/ 
package org.olat.core.gui.control.floatingresizabledialog;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.StringTokenizer;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.util.ConsumableBoolean;
import org.olat.core.util.StringHelper;

/**
 * Description:<br>
 * Controller for the floating resizable inline panels which are javascript based and
 * done by the extjs library see webapp/static/js/ext*
 * 
 * <P>
 * Initial Date:  05.06.2007 <br>
 * @author guido
 */
public class FloatingResizableDialogController extends BasicController {
	
	private VelocityContainer wrapper;
	private String panelName;
	
	private int offsetX = -1;
	private int offsetY = -1;
	private int width   = -1;
	private int height  = -1;
	
	/**
	 * creates a panel with a single content component
	 * @param ureq
	 * @param wControl
	 * @param content
	 * @param title
	 * @param initialWidth
	 * @param initialHeight
	 * @param offsetX
	 * @param offsetY
	 * @param resizable
	 */
	public FloatingResizableDialogController( UserRequest ureq, WindowControl wControl, Component content, String title, int initialWidth, int initialHeight,
			int offsetX, int offsetY, boolean resizable, boolean autoScroll) {
		this(ureq, wControl, content, title, null, initialWidth, initialHeight, offsetX, offsetY, resizable, autoScroll, false, null);
	}
	
	public FloatingResizableDialogController(UserRequest ureq, WindowControl wControl,
			Component content, String title, String cssClass, int initialWidth, int initialHeight, int offsetX, int offsetY,
			boolean resizable, boolean autoScroll, boolean constrain, String uniquePanelName) {
		
		super(ureq, wControl);
		
		this.width   = initialWidth;
		this.height  = initialHeight;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		
		wrapper = createVelocityContainer("index");
		wrapper.put("panelContent", content);

		String escapedTitle = StringHelper.escapeHtml(title);
		escapedTitle = StringHelper.escapeJavaScript(title);
		
		panelName = "o_extjsPanel_" + (uniquePanelName == null ? hashCode() : uniquePanelName);
		wrapper.contextPut("panelName", panelName);
		wrapper.contextPut("width", this.width);
		wrapper.contextPut("height", this.height);
		wrapper.contextPut("offsetX", this.offsetX);
		wrapper.contextPut("offsetY", this.offsetY);
		wrapper.contextPut("title", escapedTitle);
		wrapper.contextPut("cssClass", cssClass);
		wrapper.contextPut("resizable", resizable);
		wrapper.contextPut("constrain", constrain);
		wrapper.contextPut("scroll", Boolean.toString(autoScroll));
		if (wControl.getWindowBackOffice().getGlobalSettings().getAjaxFlags().isIframePostEnabled()) {
			wrapper.contextPut("renderOnce", new ConsumableBoolean(true));//panels should only be rendered once in ajax mode, otherwise they get doubled (e.g. switching tabs between open courses)
			wrapper.contextPut("renderAlways", Boolean.FALSE);
		} else {
			wrapper.contextPut("renderAlways", Boolean.TRUE);//render each time in non ajax mode
			wrapper.contextPut("renderOnce", new ConsumableBoolean(true));
		}
		
		wrapper.contextPut("ajaxFlags", wControl.getWindowBackOffice().getGlobalSettings().getAjaxFlags());
		
		//add the dialog javascript
		JSAndCSSComponent js = new JSAndCSSComponent("js", new String[] { "js/jquery/ui/jquery-ui-1.11.4.custom.dialog.min.js" }, null);
		wrapper.put("jsAdder", js);
		
		putInitialPanel(wrapper);
	}
	
	public void setElementCSSClass(String cssClass) {
		if(StringHelper.containsNonWhitespace(cssClass)) {
			wrapper.contextPut("cssClass", cssClass);
		} else {
			wrapper.contextRemove("cssClass");
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ("geometry".equals(event.getCommand())) {
			String p = ureq.getParameter("p");	
			if (p != null) {
				try {
					p = URLDecoder.decode(p, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					logError("", e);
				}
				
				boolean dirt = wrapper.isDirty();
				StringTokenizer tokens = new StringTokenizer(p, ",:");
				if(tokens.hasMoreTokens()) {
					offsetX = (int)Double.parseDouble(tokens.nextToken());
					wrapper.contextPut("offsetX", offsetX);
				}
				if(tokens.hasMoreTokens()) {
					offsetY = (int)Double.parseDouble(tokens.nextToken());
					wrapper.contextPut("offsetY", offsetY);
				}
				if(tokens.hasMoreTokens()) {
					width = (int)Double.parseDouble(tokens.nextToken());
					wrapper.contextPut("width"  , width);
				}
				if(tokens.hasMoreTokens()) {
					height = (int)Double.parseDouble(tokens.nextToken());
					wrapper.contextPut("height" , height);
				}	
				wrapper.setDirty(dirt);
			}		
		} else if (source == wrapper) {
			if ("close".equals(event.getCommand())) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
	}

	public String getPanelName() {
		return panelName;
	}
	
	public JSCommand getCloseCommand () {
		StringBuilder sb = new StringBuilder();
		sb.append("try{")
		  .append(" jQuery('#").append(getPanelName()).append("').dialog('destroy').remove();")
		  .append("}catch(e){}");
		return new JSCommand(sb.toString());
	}
	
	public void executeCloseCommand() {
		getWindowControl().getWindowBackOffice().sendCommandTo(getCloseCommand());
	}
}
