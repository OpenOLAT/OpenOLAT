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

import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextElementImpl.TextModeState;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;

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

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {

		RichTextElementComponent teC = (RichTextElementComponent) source;
		RichTextElementImpl te = teC.getFormItem();
		// DOM ID used to identify the rich text element in the browser DOM
		String domID = teC.getFormDispatchId();

		if (!source.isEnabled()) {
			renderDisabled(sb, domID, teC);
		} else {
			sb.append("<div id='").append(domID).append("_diw' class='o_richtext_mce");
			if(!te.getEditorConfiguration().isPathInStatusBar()) {
				sb.append(" o_richtext_mce_without_path");
			}
			sb.append("'>");
			//switches
			TextMode currentTextMode;
			if(te.getEditorConfiguration().getTextModes().size() > 1) {
				TextModeState textModeState = te.getAvailableTextModes();
				currentTextMode = textModeState.getCurrentMode();
				List<TextMode> modes = textModeState.getAvailableTextModes();
				if(!modes.isEmpty()) {
					Form form = te.getRootForm();
					sb.append("<div class='o_richtext_mce_modes'><div class='btn-group'>");
					for(TextMode mode:modes) {
						sb.append("<a href='javascript:;' class='btn btn-default btn-xs")
						  .append(" active", currentTextMode == mode).append("' ")
						  .onClickKeyEnter(FormJSHelper.getXHRFnCallFor(form, teC.getFormDispatchId(), 1, false, false, true,
								new NameValuePair("cmd", mode.name()))).append(">")
						  .append(source.getTranslator().translate(mode.name()))
						  .append("</a>");
					}
					sb.append("</div></div>");
				}
			} else {
				currentTextMode = TextMode.formatted;
			}
			
			switch(currentTextMode) {
				case formatted:
					renderTinyMCE(renderer, sb, domID, teC, ubu, source.getTranslator());
					break;
				case multiLine:
					renderMultiLine(sb, domID, teC);
					break;
				case oneLine:
					renderOneLine(sb, domID, teC);
					break;
			}
			sb.append("</div>");
		}
	}
	
	private void renderDisabled(StringOutput sb, String domID, RichTextElementComponent teC) {
		int rows = teC.getRows();
		RichTextElementImpl te = teC.getFormItem();

		// Read only view
		sb.append("<div ")
		  .append(FormJSHelper.getRawJSFor(te.getRootForm(), domID, te.getAction()))
		  .append(" id=\"")
		  .append(domID)
		  .append("_disabled\" class='form-control-static o_disabled' style=\"");
		if (rows != -1) {
			sb.append(" min-height:").append(rows).append("em;");
		}
		sb.append("\" >");

		String value = te.getRawValue();
		if(StringHelper.containsNonWhitespace(value)) {
			String mapperUri = te.getEditorConfiguration().getMapperURI();
			if(StringHelper.containsNonWhitespace(mapperUri)) {
				value = FilterFactory.getBaseURLToMediaRelativeURLFilter(mapperUri).filter(value);
			}
			sb.append(Formatter.formatLatexFormulas(value));
		}
		sb.append("</div>");
	}
	
	private void renderOneLine(StringOutput sb, String domID, RichTextElementComponent teC) {
		RichTextElementImpl te = teC.getFormItem();
		te.setRenderingMode(TextMode.oneLine);
		String htmlVal = StringHelper.escapeHtml(te.getRawValue(TextMode.oneLine));

		sb.append("<input id=\"").append(domID).append("\" name=\"").append(domID).append("\" ")
		  .append(" type='text' class='form-control'")
		  .append(" value=\"").append(htmlVal).append("\" ")
		  .append(FormJSHelper.getRawJSFor(te.getRootForm(), domID, te.getAction()));
		if (te.hasPlaceholder()) {
			sb.append(" placeholder=\"").append(te.getPlaceholder()).append("\"");
		}
		if (te.hasFocus()) {
			sb.append(" autofocus");
		}

		sb.append(">");
		//add set dirty form only if enabled
		FormJSHelper.appendFlexiFormDirty(sb, te.getRootForm(), teC.getFormDispatchId());	
	}
	
	private void renderMultiLine(StringOutput sb, String domID, RichTextElementComponent teC) {
		RichTextElementImpl te = teC.getFormItem();
		te.setRenderingMode(TextMode.multiLine);
		int cols = teC.getCols();
		int rows = teC.getRows();
		String value = te.getRawValue(TextMode.multiLine);
		
		// Read write view
		sb.append("<textarea id=\"").append(domID).append("\" name=\"").append(domID).append("\" ");
		StringBuilder rawData = FormJSHelper.getRawJSFor(te.getRootForm(), domID, te.getAction());
		sb.append(rawData.toString());
		sb.append(" class='form-control BGlossarIgnore' style=\"width:");
		if (cols == -1) {
			sb.append("100%;");
		} else {
			sb.append(cols).append("em;");
		}
		sb.append("height:");
		if (rows == -1) {
			sb.append("100%;");
		} else {
			sb.append(rows).append("em;");
		}
		sb.append("\">")
		  .append(value)
		  .append("</textarea>");
		FormJSHelper.appendFlexiFormDirty(sb, te.getRootForm(), domID);
		FormJSHelper.appendPreventEnterPropagation(sb, domID);
	}

	private void renderTinyMCE(Renderer renderer, StringOutput sb, String domID, RichTextElementComponent teC, URLBuilder ubu, Translator translator) {
		RichTextElementImpl te = teC.getFormItem();
		te.setRenderingMode(TextMode.formatted);
		RichTextConfiguration config = te.getEditorConfiguration();

		StringOutput configurations = new StringOutput();
		config.appendConfigToTinyJSArray(configurations, translator);
		if(config.getAdditionalConfiguration() != null) {
			config.getAdditionalConfiguration().appendConfigToTinyJSArray_4(configurations, translator);
		}
		
		String baseUrl = StaticMediaDispatcher.getStaticURI("js/tinymce4/tinymce/tinymce.min.js");
		String iconsUrl = StaticMediaDispatcher.getStaticURI("js/tinymce4/BTinyIcons.js");
		
		// Read write view
		renderTinyMCETextarea(sb, domID, teC);
		
		Form form = te.getRootForm();
		configurations.append("ffxhrevent: { formNam:\"").append(form.getFormName()).append("\", dispIdField:\"").append(form.getDispatchFieldId()).append("\",")
		  .append(" dispId:\"").append(teC.getFormDispatchId()).append("\", eventIdField:\"").append(form.getEventFieldId())
		  .append("\", csrf:\"").append(renderer.getCsrfToken()).append("\"},\n");
		configurations.append("contextPath: \"").append(Settings.getServerContextPath()).append("\",\n");
		if(StringHelper.containsNonWhitespace(WebappHelper.getMathJaxCdn())) {
			configurations.append("mathJaxUrl: \"").append(WebappHelper.getMathJaxCdn()).append("\",\n");
		}
		if(StringHelper.containsNonWhitespace(WebappHelper.getMathLiveCdn())) {
			configurations.append("mathLiveUrl: \"").append(WebappHelper.getMathLiveCdn()).append("\",\n");
		}
		if(te.getMaxLength() > 0) {
			configurations.append("maxSize:").append(te.getMaxLength()).append("\n");
		}
		
		Integer currentHeight = teC.getCurrentHeight();
		String uploadUrl = getImageUploadURL(renderer, teC, ubu);
		String height = config.getEditorHeight();
		
		sb.append("<input type='hidden' id='rtinye_").append(teC.getFormDispatchId()).append("' name='rtinye_").append(teC.getFormDispatchId()).append("' value=''>");
		sb.append("<script>\n");
		sb.append(" (function() {")
		  .append(" \"use strict\";");
		if("full".equals(height)) {
			sb.append("  var oTop = jQuery('#").append(domID).append("_diw").append("').offset().top;\n")
			  .append("  var cssHeight = (oTop + 300 > window.innerHeight) ? '300px': 'calc(100vh - ' + (oTop + 53) + 'px)';\n");
		}
		sb.append("  jQuery('#").append(domID).append("').tinymce({\n")
		  .append("    selector: '#").append(domID).append("',\n")
		  .append("    script_url: '").append(baseUrl).append("',\n")
		  .append("    icons_url: '").append(iconsUrl).append("',\n")
		  .append("    image_uploadtab: false,\n")
		  .append("    icons: 'openolat',\n");
		if(uploadUrl != null) {
			sb.append("    images_upload_url: '").append(uploadUrl).append("',\n")
			  .append("    automatic_uploads: true,\n");
		}
		if("full".equals(height)) {
			sb.append("    height: cssHeight,\n")
			  .append("    min_height:200,\n");
		} else if(StringHelper.containsNonWhitespace(height)) {
			sb.append("    height: '").append(config.getEditorHeight()).append("',\n");
		} else if(currentHeight != null && currentHeight.intValue() > 20) {
			sb.append("    height: ").append(currentHeight).append(",\n");
		} else if(teC.getRows() > 0) {
			int heightInPx = teC.getRows() * 40;
			sb.append("    height: '").append(heightInPx).append("px',\n");
		}
		
		sb.append("    setup: function(ed){\n")
		  .append("      ed.on('init', function(e) {\n")
		  .append("        var updateDirty = function() {\n")
		  .append("          o_extraTinyDirty(ed);")
		  .append("          if(ed.isDirty()) {\n")
		  .append("            setFlexiFormDirty('").append(form.getDispatchFieldId()).append("', false);\n")
		  .append("          }\n")
		  .append("        }\n")
		  
		  .append("        var global = tinymce.util.Tools.resolve('tinymce.util.Delay');\n")
		  .append("        var debouncedUpdate = global.debounce(function () {\n")
		  .append("          return updateDirty();\n")
		  .append("        }, 300);\n")
		  .append("        ed.on('SetContent BeforeAddUndo Undo Redo ViewUpdate keyup', debouncedUpdate);\n");
		
		sb.append("      });\n")
		  .append("      ed.on('change', function(e) {\n")
		  .append("        var domElem = jQuery('#").append(domID).append("');\n")
		  .append("        if (domElem && domElem.onchange) {\n")
		  .append("          domElem.onchange();\n")
		  .append("        };\n")
		  .append("      });\n")
		  .append("      ed.on('ResizeEditor', function(e) {\n")
		  .append("        try {\n")
		  .append("          jQuery('#rtinye_").append(teC.getFormDispatchId()).append("').val(ed.contentAreaContainer.clientHeight);\n")
		  .append("        } catch(e) { }\n")
		  .append("      });\n");
		
		if(config.isSendOnBlur()) {
			sb.append("      ed.on('blur', function(e) {\n")
			  .append("        if(jQuery('#mathlive').length == 0) {\n")// MathLive plug-in takes the focus and blur Tiny
			  .append("          o_ffXHREvent('").append(form.getFormName()).append("','").append(form.getDispatchFieldId()).append("','").append(teC.getFormDispatchId()).append("','").append(form.getEventFieldId()).append("', 2, false, false, false, false, 'cmd','saveinlinedtiny','").append(domID).append("',ed.getContent());\n")
			  .append("        }\n")
			  .append("      });\n");
		}
		if(te.getRootForm().isInlineValidationOn() || te.isInlineValidationOn()) {
			sb.append("      ed.on('focusout', function(e) {\n")
			  .append("       setTimeout(function() {\n")
			  .append(FormJSHelper.getXHRFnCallFor(form, te.getFormDispatchId(), 5, false, false, true, false))
			  .append("      },100); });\n");
		}

		sb.append("    },\n")
		  .append(configurations)
		  .append("  });\n")
		  .append("}());\n")// end timeout
		  .append("</script>\n");
	}
	
	private String getImageUploadURL(Renderer renderer, RichTextElementComponent teC, URLBuilder ubu) {
		RichTextElementImpl te = teC.getFormItem();
		RichTextConfiguration configuration = te.getEditorConfiguration();
		VFSContainer baseContainer = configuration.getLinkBrowserBaseContainer();
		if(baseContainer != null && baseContainer.canWrite() == VFSConstants.YES && te.getRootForm().isMultipartEnabled()) {
			StringOutput su = new StringOutput();
			Component rootCmp = te.getRootForm().getInitialComponent();
			URLBuilder rubu = ubu.createCopyFor(rootCmp);
			
			rubu.buildURI(su, new String[] { Form.FORMID, "_csrf", "dispatchevent", "dispatchuri", "imageupload" },
					new String[] { Form.FORMCMD, renderer.getCsrfToken(), "2", teC.getFormDispatchId(), teC.getFormDispatchId() }, null, AJAXFlags.MODE_NORMAL, false);
			return su.toString();
		}
		return null;
	}
	
	private void renderTinyMCETextarea(StringOutput sb, String domID, RichTextElementComponent teC) {
		RichTextElementImpl te = teC.getFormItem();
		int rows = teC.getRows();
		String value = te.getRawValue(TextMode.formatted);
		
		// Read write view
		sb.append("<textarea id=\"");
		sb.append(domID);
		sb.append("\" name=\"");
		sb.append(domID);
		sb.append("\" ");
		StringBuilder rawData = FormJSHelper.getRawJSFor(te.getRootForm(), domID, te.getAction());
		sb.append(rawData.toString());
		sb.append(" style=\"");
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
		sb.appendHtmlEscaped(value);
		sb.append("</textarea>");
	}
}
