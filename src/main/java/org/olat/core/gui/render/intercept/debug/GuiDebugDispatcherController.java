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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.olat.core.commons.editor.plaintexteditor.PlainTextEditorController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.delegating.DelegatingComponent;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
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
import org.olat.core.helpers.Settings;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;

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
	private Map<String, Component> idToComponent = new HashMap<String, Component>();
	private PlainTextEditorController vcEditorController;
	private Panel mainP;

	/**
	 * @param ureq
	 * @param wControl needed for subsequent debug-actions e.g. on a modal screen
	 */
	public GuiDebugDispatcherController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		dc = new DelegatingComponent("deleg", new ComponentRenderer() {

			public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
					RenderResult renderResult, String[] args) {
				// save urlbuilder for later use (valid only for one request scope thus
				// transient, normally you may not save the url builder for later usage)
				debugURLBuilder = ubu;
			}

			public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
					RenderingState rstate) {
			// void
			}

			public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
			// void
			}
		}); 
		/*{
			@Override
			/*public boolean isDirty() {
				return true;
			}
		};
		*/
		dc.addListener(this);
		dc.setDomReplaceable(false);
		mainP = putInitialPanel(dc);
		mainP.setDomReplaceable(false);
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == dc) {
			String cid = ureq.getParameter("cid");
			Component infoComponent = idToComponent.get(cid);

			
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
			} else	if (com.equals("vc")) {
				// ------- open velocity container for editing -------
				VelocityContainer vc = (VelocityContainer) infoComponent;
				String velocityTemplatePath  = WebappHelper.getSourcePath()+"/"+vc.getPage();
				VFSLeaf vcContentFile = new LocalFileImpl(new File(velocityTemplatePath));
				boolean readOnly = Settings.isReadOnlyDebug();
				vcEditorController = new PlainTextEditorController(ureq, getWindowControl(), vcContentFile, "utf-8", true, true, null);
				vcEditorController.setReadOnly(readOnly);
				vcEditorController.addControllerListener(this);
				VelocityContainer vcWrap = createVelocityContainer("vcWrapper");
				if (readOnly) vcWrap.contextPut("readOnly", Boolean.TRUE);
				vcWrap.put("editor", vcEditorController.getInitialComponent());
				getWindowControl().pushAsModalDialog(DebugHelper.createDebugProtectedWrapper(vcWrap));
			} 
		}
	}

	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == vcEditorController) {
			// saving was already done by editor, just pop
			getWindowControl().pop();
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
	//
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.olat.core.gui.render.debug.DebugHandler#createDebugComponentRenderer(org.olat.core.gui.components.ComponentRenderer)
	 */
	public ComponentRenderer createInterceptComponentRenderer(final ComponentRenderer originalRenderer) {
		return new ComponentRenderer() {

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

					sb.append("<div class=\"oocgrid_d1\">");
					sb
							.append(""
									+ "<div>"
									+ "  <span id=\"o_guidebugst"
									+ did
									+ "\" onmouseover=\"o_debu_show(this.parentNode.parentNode, $('o_guidebugtt"
									+ did
									+ "'))\" "
									+
									// " onmouseout=\"o_debu_hide(this.parentNode.parentNode,
									// $('o_guidebugtt"+did+"'))\" "+
									">"
									+ source.getComponentName()
									+ " ("
									+ cnameShort
									+ ")"
									+ "&nbsp;&nbsp;&nbsp;"
									+ "</span>"
									+ "</div>"
									+ "<div style=\"position:relative\">"
									+ "	<div id=\"o_guidebugtt"
									+ did
									+ "\" style=\"position:absolute; top:0px; left:24px; height:auto; width:auto; display:none; padding:5px; border: 1px solid black; margin: 0px; z-index:999; font-size:11px; background-color: #BBF;\" "+
									// does not work as it should?  " onmouseout=\"o_debu_hide($('o_guidebugst"+did+"'),$('o_guidebugtt"+did+"'))\" "+
									">");

					sb.append("Info: <b>").append(source.getComponentName()).append("</b> ("+cnameShort+") id:"+
							String.valueOf(source.getDispatchID()+"&nbsp; level:"+lev));

					// offer velocity editor if appropriate.
					// todo: let component provide component-specific editors
					if (source instanceof VelocityContainer) {
						VelocityContainer vcc = (VelocityContainer) source;
						sb.append("<br />velocity: <a href=\"");
						debugURLBuilder.buildURI(sb, new String[] { "cid", "com" }, new String[] { String.valueOf(did), "vc" });
						sb.append("\">").append("page:").append(vcc.getPage()+"</a>");						
					}
					
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

			public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
					RenderingState rstate) {
				originalRenderer.renderHeaderIncludes(renderer, sb, source, ubu, translator, rstate);
			}

			public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
				originalRenderer.renderBodyOnLoadJSFunctionCall(renderer, sb, source, rstate);
			}
		};
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.olat.core.gui.render.debug.DebugHandler#createDebugHandlerRenderInstance()
	 */
	public InterceptHandlerInstance createInterceptHandlerInstance() {
		// clear all previous data and return this.
		// otherwise this map would collect all components from all clicks, but we
		// need only one click
		debugURLBuilder = null;
		idToComponent.clear();
		return this;
	}


	/**
	 * @param showDebugInfo
	 */
	public void setShowDebugInfo(boolean showDebugInfo) {
		if (showDebugInfo) {
			mainP.setContent(dc);
		} else {
			mainP.setContent(null);
		}
	}

}
