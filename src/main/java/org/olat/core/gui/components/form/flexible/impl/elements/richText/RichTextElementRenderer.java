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

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
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
class RichTextElementRenderer extends DefaultComponentRenderer {

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#render(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder,
	 *      org.olat.core.gui.translator.Translator,
	 *      org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {

		RichTextElementComponent teC = (RichTextElementComponent) source;
		RichTextElementImpl te = teC.getRichTextElementImpl();
		int rows = teC.getRows();
		// DOM ID used to identify the rich text element in the browser DOM
		String domID = teC.getFormDispatchId();
		
        // Use an empty string as default value
		String value = te.getRawValue();
		if (value == null) {
			value = "";
		}

		if (!source.isEnabled()) {
			// Read only view
			sb.append("<div ");
			sb.append(FormJSHelper.getRawJSFor(te.getRootForm(), domID, te.getAction()));
			sb.append(" id=\"");
			sb.append(domID);
			sb.append("_disabled\" class='form-control-static o_disabled' style=\"");
			if (rows != -1) {
				sb.append(" min-height:").append(rows).append("em;");
			}
			sb.append("\" >");
			sb.append(Formatter.formatLatexFormulas(value));
			sb.append("</div>");
		} else {
			sb.append("<div id='").append(domID).append("_diw' class='o_richtext_mce");
			if(!te.getEditorConfiguration().isPathInStatusBar()) {
				sb.append(" o_richtext_mce_without_path");
			}
			sb.append("'>");
			renderTinyMCE_4(sb, domID, teC, ubu, source.getTranslator());
			sb.append("</div>");
		}
	}

	private void renderTinyMCE_4(StringOutput sb, String domID, RichTextElementComponent teC, URLBuilder ubu, Translator translator) {
		RichTextElementImpl te = teC.getRichTextElementImpl();
		RichTextConfiguration config = te.getEditorConfiguration();
		List<String> onInit = config.getOnInit();

		StringOutput configurations = new StringOutput();
		config.appendConfigToTinyJSArray_4(configurations, translator);
		
		StringOutput baseUrl = new StringOutput();
		StaticMediaDispatcher.renderStaticURI(baseUrl, "js/tinymce4/tinymce/tinymce.min.js", true);
		
		// Read write view
		if(config.isInline()) {
			renderInlineEditor(sb, domID, teC);
		} else {
			renderTextarea(sb, domID, teC);
		}
		
		Form form = te.getRootForm();
		configurations.append("ffxhrevent: { formNam:\"").append(form.getFormName()).append("\", dispIdField:\"").append(form.getDispatchFieldId()).append("\",")
		 .append(" dispId:\"").append(teC.getFormDispatchId()).append("\", eventIdField:\"").append(form.getEventFieldId()).append("\"}\n");
		
		sb.append("<script type='text/javascript'>/* <![CDATA[ */\n");
		//file browser url
		sb.append("  BTinyHelper.editorMediaUris.put('").append(domID).append("','");
		ubu.buildURI(sb, null, null);
		sb.append("');\n");
		sb.append("  jQuery('#").append(domID).append("').tinymce({\n")
		  .append("    selector: '#").append(domID).append("',\n")
		  .append("    script_url: '").append(baseUrl.toString()).append("',\n")
		  .append("    setup: function(ed){\n")
		  .append("      ed.on('init', function(e) {\n")
		  .append("        ").append(onInit.get(0).replace(".curry(", "(")).append(";\n")
		  .append("      });\n")
		  .append("      ed.on('change', function(e) {\n")
		  .append("        BTinyHelper.triggerOnChange('").append(domID).append("');\n")
		  .append("      });\n");
		if(config.isInline() || config.isSendOnBlur()) {
			sb.append("      ed.on('blur', function(e) {\n")
			  .append("        o_ffXHREvent('").append(form.getFormName()).append("','").append(form.getDispatchFieldId()).append("','").append(teC.getFormDispatchId()).append("','").append(form.getEventFieldId()).append("', 2, false, false,'cmd','saveinlinedtiny','").append(domID).append("',ed.getContent());\n")
	          .append("      });\n");
		}
		sb.append("    },\n")
		  .append(configurations)
		  .append("  });\n")
		  .append("/* ]]> */</script>\n");
	}
	
	private void renderInlineEditor(StringOutput sb, String domID, RichTextElementComponent teC) {
		RichTextElementImpl te = teC.getRichTextElementImpl();
		int cols = teC.getCols();
		int rows = teC.getRows();
		String value = te.getRawValue();
		
		// Read write view
		sb.append("<div id=\"");
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
		sb.append(value);
		sb.append("</div>");
	}
	
	private void renderTextarea(StringOutput sb, String domID, RichTextElementComponent teC) {
		RichTextElementImpl te = teC.getRichTextElementImpl();
		int cols = teC.getCols();
		int rows = teC.getRows();
		String value = te.getRawValue();
		
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
	}
}
