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

package org.olat.core.gui.components.panel;

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.control.dragdrop.DragAndDropImpl;
import org.olat.core.gui.control.dragdrop.Draggable;
import org.olat.core.gui.control.dragdrop.DroppableImpl;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class PanelRenderer implements ComponentRenderer {

	/**
	 * 
	 */
	public PanelRenderer() {
	//
	}

	/**
	 * @see org.olat.core.gui.render.ui.ComponentRenderer#render(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator,
	 *      org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		Panel panel = (Panel) source;
		
		Component toRender = panel.getContent();
		
		//fxdiff alpha-quality for drag and drop
		if (renderer.getGlobalSettings().getAjaxFlags().isIframePostEnabled()) {
			DragAndDropImpl dndi = panel.doGetDragAndDrop();
			if (dndi != null) {
				DroppableImpl di = dndi.getDroppableImpl();
				if (di != null) {
					String urivar = Renderer.getComponentPrefix(panel) + "_dropurl";
					sb.append("<script type=\"text/javascript\">var ").append(urivar).append(" = \"");
					boolean iframePostEnabled = renderer.getGlobalSettings().getAjaxFlags().isIframePostEnabled();
					ubu.buildURI(sb, null, null, iframePostEnabled? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
					sb.append("\";</script>");
				}
			}
		}
		
		
		if (toRender != null) {
			//FIXME:fj: replace , args with , null ?
			renderer.render(sb, toRender, args);
		}
	}

	/**
	 * @see org.olat.core.gui.render.ui.ComponentRenderer#renderHeaderIncludes(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator)
	 */
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
		Panel panel = (Panel) source;
		Component toRender = panel.getContent();
		if (toRender != null) {
			// delegate header rendering to the content
			renderer.renderHeaderIncludes(sb, toRender, rstate);
		}
	}

	/**
	 * @see org.olat.core.gui.render.ui.ComponentRenderer#renderBodyOnLoadJSFunctionCall(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component)
	 */
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
		Panel panel = (Panel) source;
		Component toRender = panel.getContent();
		
		if (toRender != null) {
			// delegate header rendering to the content
			renderer.renderBodyOnLoadJSFunctionCall(sb, toRender, rstate);
		}
		
		//fxdiff
		if (renderer.getGlobalSettings().getAjaxFlags().isIframePostEnabled()
				&& panel.doGetDragAndDrop() != null) {
			//renderer.getGlobalSettings().getAjaxFlags().isDragAndDropEnabled()
			// first activate the drag and drop
			// drag and drop to look at
			DragAndDropImpl dndi = panel.doGetDragAndDrop();
			if (dndi != null) {
				DroppableImpl di = dndi.getDroppableImpl();
				if (di != null) {
					boolean iframePostEnabled = renderer.getGlobalSettings().getAjaxFlags().isIframePostEnabled();					
					String compPrefix = Renderer.getComponentPrefix(panel);
					String urivar = Renderer.getComponentPrefix(panel) + "_dropurl";
					
					// we have a droppable
					for (Draggable draga: di.getAccepted()) {
						List<String> cids = draga.getContainerIds();
						sb.append("Droppables.add('").append(compPrefix).append("',{containment:[");
						int clen = cids.size();
						for (int i = 0; i < clen; i++) {
							sb.append("\"").append(cids.get(i)).append("\"");
							if (i < clen-1) sb.append(",");
						}
						sb.append("]");
						sb.append(",onDrop:function(el){o_info.drop=true;");
						if (iframePostEnabled) {
							sb.append("var f = $('o_oaap'); f.v.value=el.id; f.action = ").append(urivar).append("; f.submit();");
						} else {
							//TODO: also use the global post form, but the form must have a different target(self)
							sb.append("document.location.href = ").append(urivar).append(" + \"?v=\"+el.id;");
						}
						sb.append("}});\n");
					}
				}
				
				if (toRender != null) {
					// only offer drag code if there is content.
					// render code for - draggable -
					Draggable drag = dndi.getDraggable();
					if (drag != null) {
						String id = Renderer.getComponentPrefix(toRender);
						sb.append("new Draggable('").append(id).append("',{handle:'handle',revert:function(el){if (o_info.drop) {o_info.drop = false; return false;} else return true;}});\n");
					}
				}
			}
		}
	}

}