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
package org.olat.core.gui.components.form.flexible.impl;

import java.util.HashSet;
import java.util.Set;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * Initial Date: 27.11.2006 <br>
 * 
 * @author patrickb
 */
class FormWrapperContainerRenderer implements ComponentRenderer {

	private static final Set<String> acceptedInstructions = new HashSet<>();
	static {
		acceptedInstructions.add("class");
	}

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		FormWrapperContainer formC = (FormWrapperContainer) source;
		ComponentCollection toRender = formC.getFormLayout();

		/*
		 * extract check for render instruction to the form wrapper
		 */
		boolean hasRenderInstr = (args != null && args.length > 0);

		if (toRender != null) {
			AJAXFlags flags = renderer.getGlobalSettings().getAjaxFlags();
			boolean iframePostEnabled = flags.isIframePostEnabled();
			/*
			 * FORM HEADER
			 */
			sb.append("<form ");
			if (hasRenderInstr) {
				// append render instructions if available
				// flexi form supports only class
				FormJSHelper.appendRenderInstructions(sb, args[0], acceptedInstructions);
			}
			
			sb.append(" method=\"post\"");
			// Set encoding to multipart only if multipart data is available to reduce 
			// transfer and parameter extracing overhead
			if (formC.isMultipartEnabled()) {
				sb.append(" enctype=\"multipart/form-data\"");
			}

			sb.append(" id=\"");
			sb.append(formC.getFormName());
			sb.append("\" name=\"");
			sb.append(formC.getFormName());
			sb.append("\" action=\"");
			ubu.buildURI(sb, new String[] { Form.FORMID }, new String[] { Form.FORMCMD },
					null, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL, false);
			sb.append("\" ");
			//check if ready to accept a new request
			if(iframePostEnabled) {
				sb.append(" onsubmit=\"return o_XHRSubmit('").append(formC.getFormName()).append("');\" ");
			} else {
				sb.append(" onsubmit=\"if(o_info.linkbusy) return false; else o_beforeserver(); return true;\" ");
			}
			sb.append(" onkeydown=\"o_submitByEnter(event)\" ");
			sb.append(">");
			// hidden input field for dispatch uri
			sb.append("<input type=\"hidden\" id=\"")
			  .append(formC.getDispatchFieldId())
			  .append("\" name=\"dispatchuri\" value=\"").append(Form.FORM_UNDEFINED).append("\" />")
			  .append("<input type=\"hidden\" id=\"")
			  .append(formC.getEventFieldId())
			  .append("\" name=\"dispatchevent\" value=\"").append(Form.FORM_UNDEFINED).append("\" />");
			if(formC.isCsrfProtected()) {
				sb.append("<input type=\"hidden\"")
				  .append(" name=\"").append(Form.FORM_CSRF).append("\" value=\"").append(renderer.getCsrfToken()).append("\" />");
			}

			/*
			 * FORM CONTAINER
			 */
			renderer.render(sb, toRender, args);
			/*
			 * FORM FOOTER
			 */
			sb.append("</form>");
			/*
			 * FORM SUBMIT on keypress enter
			 */
			sb.append(FormJSHelper.submitOnKeypressEnter(formC.getFormName()));
		}
	}

	@Override
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
		FormWrapperContainer formC = (FormWrapperContainer) source;
		ComponentCollection toRender = formC.getFormLayout();
		if (toRender != null) {
			renderer.renderBodyOnLoadJSFunctionCall(sb, toRender, rstate);
		}
	}

	@Override
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderingState rstate) {
		FormWrapperContainer formC = (FormWrapperContainer) source;
		ComponentCollection toRender = formC.getFormLayout();
		if (toRender != null) {
			renderer.renderHeaderIncludes(sb, toRender, rstate);
		}
	}
}