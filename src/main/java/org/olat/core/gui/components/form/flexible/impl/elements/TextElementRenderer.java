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
package org.olat.core.gui.components.form.flexible.impl.elements;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * Initial Date: 08.12.2006 <br>
 * 
 * @author patrickb
 */
class TextElementRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		TextElementComponent teC = (TextElementComponent) source;
		TextElementImpl te = teC.getFormItem();
		String id = teC.getFormDispatchId();

		String value = te.getValue();
		if(value == null){
			value = "";
		}
		StringBuilder htmlVal = new StringBuilder();
		htmlVal.append(StringHelper.escapeHtml(value));
		String elementCSS = te.getElementCssClass();
		if (source.isEnabled()) {
			//read write view			
			sb.append("<input type=\"").append(te.getHtmlInputType()).append("\" id=\"").append(id)
			  .append("\" name=\"").append(id)
			  .append("\" class='form-control ").append(elementCSS, elementCSS != null).append(" o_show_hide_eye", te.isShowHideEye())
			  .append("' size=\"").append(te.displaySize);
			if(te.maxlength > -1){
				sb.append("\" maxlength=\"");
				sb.append(te.maxlength);
			}
			sb.append("\" value=\"").append(htmlVal).append("\" ")
			  .append(FormJSHelper.getRawJSFor(te.getRootForm(), id, te.getAction()));
			
			if (te.hasPlaceholder()) {
				sb.append(" placeholder=\"").append(te.getPlaceholder()).append("\"");
			}
			
			if (te.hasFocus()) {
				sb.append(" autofocus");
			}
			if (te.isMandatory()) {
				sb.append(" aria-required=\"true\"");				
			}
			if (te.getAutocomplete() != null) {
				sb.append(" autocomplete=\"").append(te.getAutocomplete()).append("\"");
			}
			if(StringHelper.containsNonWhitespace(te.getAriaLabel())) {
				sb.append(" aria-label=\"").append(te.getAriaLabel()).append("\"");
			}
			if(te.hasError() || te.hasWarning()) {
				sb.append(" aria-label=\"").append(te.getAriaLabel()).append("\"");
			}
			appendErrorAriaDescribedby(sb, te);
			sb.append(">");
			
			if(te.isShowHideEye()) {
				sb.append("<i id='").append(id).append("_eye' class='o_icon o_icon_eye form-control-feedback'> </i>");
			}
			
			//add set dirty form only if enabled
			FormJSHelper.appendFlexiFormDirty(sb, te.getRootForm(), id);
			if(te.getRootForm().isInlineValidationOn() || te.isInlineValidationOn()) {
				FormJSHelper.appendValidationListeners(sb, te.getRootForm(), id);
			}

			// for an one time password, some javascript is needed to change the look/feel
			// and to trigger an event after the 8th digit is entered
			if (te.isOneTimePassword()) {
				sb.append("<script>\n")
						.append("\"use strict\";\n")
						.append("var otpInput = document.querySelector('[autocomplete=one-time-code]');\n")
						.append("otpInput.addEventListener('input', () => {\n")
						.append("    otpInput.style.setProperty('--_otp-digit', otpInput.selectionStart);\n")
						.append("// Check if 8 digits are entered\n")
						.append("	if (otpInput.value.length === 8 && /^\\d{8}$/.test(otpInput.value)) {\n")
						.append(FormJSHelper.getJSFnCallFor(te.getRootForm(), id, te.getAction()))
						.append("   }\n")
						.append("});\n")
						.append("</script>\n");
			}
			
			if (te.isPlaceholderUpdate()) {
				sb.append("<script>\n")
				  .append("\"use strict\";\n")
				  .append(" function o_up").append(te.getPlaceholderId()).append("(){")
				  .append(" try {\n")
				  .append("  jQuery('#").append(id).append("').attr('placeholder',")
				  .append("   function(){return jQuery('#").append(te.getPlaceholderId()).append("').val()");
				if (te.getPlaceholderMaxLength() != null) {
					sb.append(" .substring(0,").append(te.getPlaceholderMaxLength().intValue()).append(")");
				}
				sb.append(";}\n");  //function
				sb.append("  );\n") // attr
				  .append(" } catch (e){}\n")
				  .append(" }\n") // try function
				  .append("jQuery(function(){\n")
				  .append(" jQuery('#").append(te.getPlaceholderId()).append("').ready(o_up").append(te.getPlaceholderId()).append(");")
				  .append(" jQuery('#").append(te.getPlaceholderId()).append("').keyup(o_up").append(te.getPlaceholderId()).append(");")
				  .append("});\n")
				  .append("</script>\n");
			}
			if(te.isShowHideEye()) {
				sb.append("<script>")
				  .append("\"use strict\";\n")
				  .append("jQuery(function(){\n")
				  .append(" jQuery('#").append(id).append("_eye').on('click',function(el) {\n")
				  .append("  var pEl = document.querySelector('#").append(id).append("');\n")
				  .append("  var type = pEl.getAttribute('type') === 'password' ? 'text' : 'password';\n")
				  .append("  pEl.setAttribute('type', type);\n")
				  .append("  this.classList.toggle('o_icon_eye_slash');\n")
				  .append(" });\n")
				  .append("});\n")
				  .append("</script>\n");
			}
		} else {
			//read only view
			sb.append("<span id=\"").append(id).append("_wp\" ")
			  .append(FormJSHelper.getRawJSFor(te.getRootForm(), id, te.getAction()))
			  .append(" title=\"").append(htmlVal) //the uncutted value in tooltip
			  .append("\" ").append(" >");
			// use the longer from display size or real value length
			int size = (te.displaySize > value.length() ? te.displaySize : value.length());
			sb.append("<input id=\"").append(id).append("\" type=\"").append(te.getHtmlInputType())
			  .append("\" disabled=\"disabled\" class='form-control o_disabled ").append(elementCSS, elementCSS != null)
			  .append("' size=\"").append(size)
			  .append("\" value=\"").append(htmlVal).append("\"");
			if (te.hasPlaceholder()) {
				sb.append(" placeholder=\"").append(te.getPlaceholder()).append("\"");
			}
			sb.append(">")
			  .append("</span>");
		}
		
		if(StringHelper.containsNonWhitespace(te.getTextAddOn()) ) {
			sb.append(" <span class=''>");
			if(te.isTranslateTextAddOn()) {
				sb.append(translator.translate(te.getTextAddOn()));
			} else {
				sb.append(te.getTextAddOn());
			}
			sb.append("</span>");
		}
	}
}