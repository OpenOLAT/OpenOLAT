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

package org.olat.core.gui.components.form.flexible.impl.elements.richText;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.GUIInterna;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormBaseComponentIdProvider;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;

/**
 * 
 * Description:<br>
 * This class render the rich text element. It uses the TinyMCE javascript
 * library
 * 
 * <P>
 * Initial Date: 21.04.2009 <br>
 * 
 * @author gnaegi
 */
class RichTextElementRenderer implements ComponentRenderer {

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#render(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder,
	 *      org.olat.core.gui.translator.Translator,
	 *      org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
	@SuppressWarnings("unused")
	public void render(
	Renderer renderer, StringOutput sb, Component source,
			URLBuilder ubu, Translator translator, RenderResult renderResult,
			String[] args) {
		//
		RichTextElementComponent teC = (RichTextElementComponent) source;
		RichTextElementImpl te = teC.getRichTextElementImpl();
		int cols = teC.getCols();
		int rows = teC.getRows();
		// DOM ID used to identify the rich text element in the browser DOM
		String domID;
        if (GUIInterna.isLoadPerformanceMode()) {
                domID = FormBaseComponentIdProvider.DISPPREFIX+te.getRootForm().getReplayableDispatchID(teC);
        } else {
                domID = teC.getFormDispatchId();
        }
		// Use an empty string as default value
		String value = te.getRawValue();
		if (value == null) {
			value = "";
		}

		if (!source.isEnabled()) {
			// Read only view
			sb.append("<div ");
			sb.append(FormJSHelper.getRawJSFor(te.getRootForm(), domID, te
					.getAction()));
			sb.append(" id=\"");
			sb.append(domID);
			sb
					.append("_disabled\" readonly class=\"b_form_element_disabled\" style=\"");
			if (cols != -1) {
				sb.append(" width:");
				sb.append(cols);
				sb.append("em;");
			}
			if (rows != -1) {
				sb.append(" min-height:");
				sb.append(rows);
				sb.append("em;");
			}
			sb.append("\" >");
			sb.append(Formatter.formatLatexFormulas(value));
			sb.append("</div>");

		} else {
			// Read write view
			sb.append("<textarea id=\"");
			sb.append(domID);
			sb.append("\" name=\"");
			sb.append(domID);
			sb.append("\" ");
			StringBuilder rawData = FormJSHelper.getRawJSFor(te.getRootForm(), domID, te.getAction());
			sb.append(rawData.toString());
			sb.append(" style=\"");
			sb.append(" width:");
			if (cols == -1) {
				sb.append("100%;");
			} else {
				sb.append(cols);
				sb.append("em;");
			}
			sb.append("height:");
			if (rows == -1) {
				sb.append("100%;");
			} else {
				sb.append(rows);
				sb.append("em;");
			}
			sb.append("\" class=\"BGlossarIgnore\">");
			// The value needs to be encoded when loading into the editor to properly display < > etc values. 
			// See http://tinymce.moxiecode.com/punbb/viewtopic.php?id=1846
			sb.append(StringEscapeUtils.escapeHtml(value));
			sb.append("</textarea>");

			// Load TinyMCE code. 
			sb.append("<script type='text/javascript'>/* <![CDATA[ */ ");
			// Execute code within an anonymous function (closure) to not leak
			// variables to global scope (OLAT-5755)
			if(teC.isExtDelay()) sb.append("setTimeout(function(){");
			else sb.append("(function(){");
			// Stop existing form dirty observers first
			sb.append("BTinyHelper.stopFormDirtyObserver('" + te.getRootForm().getDispatchFieldId() + "','" + domID + "');");
			// Now add component dispatch URL as a tiny helper variable to open the
			// media browser in new window at a later point from javascript
			sb.append("BTinyHelper.editorMediaUris.put('").append(domID).append("','");
			ubu.buildURI(sb, null, null);
			sb.append("');");	
			
			// Wait until the browser has fully loaded the tiny js file and the
			// window.tinyMCE object is available. Loop until its there.
			sb.append("if(jQuery.isNumeric(o_info.tinyLoaderId)) window.clearTimeout(o_info.tinyLoaderId);");
			// To actually load tiny we use a function that is executed deferred
			// and retries to initialize the tiny instance as long as it might
			// take to load the tiny code. To not get confused with several tiny
			// instances on the screen we use a custom method name per rich text element
			String checkAndLoadTinyFunctionName = "o_checkTinyLoaded" + domID;
			sb.append("var ").append(checkAndLoadTinyFunctionName).append(" = function() { ");
			sb.append("if(jQuery.type(window.tinyMCE) === 'undefined') o_info.tinyLoaderId = ").append(checkAndLoadTinyFunctionName).append(".delay(0.01); else {");
			// Add custom modules just before initializing tiny		
			RichTextConfiguration richTextConfiguration = te.getEditorConfiguration();
			richTextConfiguration.appendLoadCustomModulesFromConfig(sb);
			// First see if there is an existing editor instance for this DOM element. 
			// If yes, remove the editor first to prevent clashes with the new created 
			// editor instance.
			sb.append("BTinyHelper.removeEditorInstance('").append(domID).append("');");
			// Now initialize IntyMCE with the generated configuration
			sb.append("tinyMCE.init({");
			richTextConfiguration.appendConfigToTinyJSArray(sb);
			// Add set dirty form only if enabled. For the RichTextElement we need
			// some special code to find out when the element is dirty. See the comments
			// BTinyHelpers.js
			sb.append("});");
			
			sb.append("} };");
			sb.append(checkAndLoadTinyFunctionName).append("();");

			if(teC.isExtDelay()) sb.append("},500);");
			else sb.append("})();");
			sb.append("/* ]]> */</script>");
			// Done with loading of TinyMCE code
		}

	}

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#renderBodyOnLoadJSFunctionCall(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.RenderingState)
	 */
	@SuppressWarnings("unused")
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer,
			StringOutput sb, Component source, RenderingState rstate) {
		// nothing to load
	}

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#renderHeaderIncludes(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder,
	 *      org.olat.core.gui.translator.Translator,
	 *      org.olat.core.gui.render.RenderingState)
	 */
	@SuppressWarnings({ "unused", "deprecation" })
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb,
			Component source, URLBuilder ubu, Translator translator,
			RenderingState rstate) {
		// no headers to include
	}

}
