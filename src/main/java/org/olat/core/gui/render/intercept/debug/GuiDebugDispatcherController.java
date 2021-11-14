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
package org.olat.core.gui.render.intercept.debug;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.olat.core.commons.editor.plaintexteditor.TextEditorController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.delegating.DelegatingComponent;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.dev.Util;
import org.olat.core.gui.dev.controller.SourceViewController;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.render.intercept.DebugHelper;
import org.olat.core.gui.render.intercept.InterceptHandler;
import org.olat.core.gui.render.intercept.InterceptHandlerInstance;
import org.olat.core.gui.translator.Translator;

/**
 * Description:<br>
 * <P>
 * Initial Date: 20.05.2006 <br>
 * 
 * @author Felix Jost
 */
public class GuiDebugDispatcherController extends BasicController implements InterceptHandler, InterceptHandlerInstance {
	
	private URLBuilder debugURLBuilder;
	private DelegatingComponent dc;
	private Map<String, Component> idToComponent = new HashMap<>();
	private TextEditorController vcEditorController;
	private StackedPanel mainP;

	public GuiDebugDispatcherController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		dc = new DelegatingComponent("deleg", new ComponentRenderer() {
			@Override
			public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
					RenderResult renderResult, String[] args) {
				// save urlbuilder for later use (valid only for one request scope thus
				// transient, normally you may not save the url builder for later usage)
				debugURLBuilder = ubu;
			}

			@Override
			public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
					RenderingState rstate) {
			// void
			}

			@Override
			public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
			// void
			}
		}); 
		
		dc.addListener(this);
		dc.setDomReplaceable(false);
		mainP = putInitialPanel(dc);
		mainP.setDomReplaceable(false);
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == dc) {
			String com = ureq.getParameter("com");
			// ------- open java IDE -------
			if (com.equals("ojava")) {
				String cl = ureq.getParameter("class");
				// cl e.g. org.olat.core.MyClass
				//ide does not work yet, just show sourcecode in new browser window
				try {
					ureq.getDispatchResult().setResultingMediaResource(SourceViewController.showjavaSource(cl));
				} catch (IOException e) {
					getWindowControl().setError("Could not render java source code. Make sure you have set the source path (olat and olatcore) in the config (olat.properties) and have the source files there available");
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == vcEditorController) {
			if (event == Event.DONE_EVENT) {
				// saving was already done by editor, just pop
				getWindowControl().pop();
			}
		}
	}

	@Override
	public ComponentRenderer createInterceptComponentRenderer(final ComponentRenderer originalRenderer) {
		return new ComponentRenderer() {

			@Override
			public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
					RenderResult renderResult, String[] args) {
				if (debugURLBuilder != null && !DebugHelper.isProtected(source)) {
					// remember source for later debug info access
					String did = source.getDispatchID();
					String didS = String.valueOf(did);
					idToComponent.put(didS, source);
					int lev = renderResult.getNestedLevel();

					String cname = source.getClass().getName();
					String cnameShort = cname.substring(cname.lastIndexOf('.') + 1);
					// header before component

					sb.append("<div class='o_dev_w'>");
					sb.append("<div class='o_dev_h'><span id='o_guidebugst").append(did).append("' onmouseover=\"o_debu_show(this.parentNode.parentNode, jQuery('#o_guidebugtt").append(did).append("'))\">");
					sb.append(source.getComponentName()).append(" (").append(cnameShort).append(")");
					sb.append("</span></div>");
					sb.append("<div class='o_dev_c'><div id='o_guidebugtt").append(did).append("' class='o_dev_i'>");
					sb.append("Info: <b>").append(source.getComponentName()).append("</b> ("+cnameShort+") id:");
					sb.append(String.valueOf(source.getDispatchID())).append("&nbsp; level:").append(lev);

					Controller listC = Util.getListeningControllerFor(source);
					if (listC != null) {
						sb.append("<br /><b>controller:</b> <a  target=\"_blank\" href=\"");
						String controllerClassName = listC.getClass().getName();
						debugURLBuilder.buildURI(sb, new String[] { "cid", "com", "class" }, new String[] { String.valueOf(did), "ojava",  controllerClassName});
						sb.append("\">");
						sb.append(controllerClassName);
						sb.append("</a>");
					}
					
					sb.append("<br /><i>listeners</i>: ");
					if (!source.isEnabled()) {
						sb.append(" NOT ENABLED");
					}
					String listeners = source.getListenerInfo();
					sb.append(listeners);
					if (!source.isVisible()) {
						sb.append("<br />INVISIBLE");
					}
					sb.append("<br />");
					
					// we must let the original renderer do its work so that the collecting translator is callbacked.
					// we save the result in a new var since it is too early to append it to the 'stream' right now.
					StringOutput sbOrig = new StringOutput();
					try {
						originalRenderer.render(renderer, sbOrig, source, ubu, translator, renderResult, args);
					} catch (Exception e) {
						String emsg = "exception while rendering component '" + source.getComponentName() + "' ("
						+ source.getClass().getName() + ") " + source.getListenerInfo() + "<br />Message of exception: " + e.getMessage();
						sbOrig.append("<span style=\"color:red\">Exception</span><br /><pre>"+emsg+"</pre>");
					}
					
					sb.append("</div>");
							
					// add original component
					sb.append(sbOrig); 
					sb.append("</div></div>");
				} else {
					// e.g. when the render process take place before the delegating
					// component of this controller here was rendered.
					// the delegating component should be placed near the <html> tag in
					// order to be rendered first.
					// the contentpane of the window and the first implementing container
					// will not be provided with debug info, which is on purpose,
					// since those are contents from the chiefcontroller which control the
					// window.

					// render original component
					originalRenderer.render(renderer, sb, source, ubu, translator, renderResult, args);
				}
			}

			@Override
			public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
					RenderingState rstate) {
				originalRenderer.renderHeaderIncludes(renderer, sb, source, ubu, translator, rstate);
			}

			@Override
			public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
				originalRenderer.renderBodyOnLoadJSFunctionCall(renderer, sb, source, rstate);
			}
		};
	}


	@Override
	public InterceptHandlerInstance createInterceptHandlerInstance() {
		// clear all previous data and return this.
		// otherwise this map would collect all components from all clicks, but we
		// need only one click
		debugURLBuilder = null;
		idToComponent.clear();
		return this;
	}

	public void setShowDebugInfo(boolean showDebugInfo) {
		if (showDebugInfo) {
			mainP.setContent(dc);
		} else {
			mainP.setContent(null);
		}
	}
}
