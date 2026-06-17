/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.components;

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.ui.components.ReflectionStaticElement.ReflectionType;

/**
 * 
 * Initial date: 20.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReflectionStaticRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		ReflectionStaticComponent cmp = (ReflectionStaticComponent)source;
		ReflectionStaticElement item = cmp.getFormItem();
		
		String id = item.getFormDispatchId();
		String valueToCopy = item.getReflectedValue();
		if(valueToCopy == null) {
			valueToCopy = "";
		} else {
			valueToCopy = StringHelper.escapeHtml(valueToCopy);
		}
		sb.append("<p id='").append(id).append("' class='form-control-static'><span id='ref_").append(id).append("'>").append(valueToCopy).append("</span>");
		if(StringHelper.containsNonWhitespace(item.getTextAddOn())) {
			sb.append(" <span>").append(translator.translate(item.getTextAddOn())).append("</span>");
		}
		sb.append("</p>");
		
		if(item.getReflectionType() == ReflectionType.concat) {
			renderConcatScript(sb, id, item.getTextElements());
		} else if(item.getReflectionType() == ReflectionType.sumInteger) {
			renderSumScript(sb, id, item.isFormatTextElements(), item.getTextElements());
		}
	}
	
	private void renderConcatScript(StringOutput sb, String id, List<TextElement> elementsToCopy) {
		sb.append("<script>\n")
		  .append("jQuery(function() {\n")
		  .append(" 'use strict';\n");
		renderCopyArray(sb, elementsToCopy);
		sb.append(" jQuery('#ref_").append(id).append("').text(concatString(copyIds));\n")
		  .append(" for(var i=0;i<copyIds.length; i++) {\n")
		  .append("   jQuery('#' + copyIds[i]).keyup(function() {\n")
		  .append("     jQuery('#ref_").append(id).append("').text(concatString(copyIds));\n")
		  .append("   });\n")
		  .append(" }\n")
		  .append(" function concatString(ids) {")
		  .append("   var val = '';\n")
		  .append("   for(var j=0;j<ids.length; j++) {\n")
		  .append("     if(j > 0) val += '';\n")
		  .append("     val += jQuery('#' + copyIds[j]).val();\n")
		  .append("   }\n")
		  .append("   return val;\n")
		  .append(" }\n")
		  .append("});\n")
		  .append("</script>");
	}
	
	private void renderSumScript(StringOutput sb, String id, boolean cleanFields, List<TextElement> elementsToCopy) {
		sb.append("<script>\n")
		  .append("jQuery(function() {\n");
		renderCopyArray(sb, elementsToCopy);
		sb.append(" jQuery('#ref_").append(id).append("').text(sumInteger(copyIds));\n")
		  .append(" for(var i=0;i<copyIds.length; i++) {\n")
		  .append("   var copyId = copyIds[i];\n")
		  .append("   jQuery('#' + copyId).keyup(function() {\n")
		  .append("     jQuery('#ref_").append(id).append("').text(sumInteger(copyIds));\n")
		  .append("   });");
		if(cleanFields) {
			sb.append("   jQuery('#' + copyId).change(function() {\n")
			  .append("     var fEl = jQuery(this)\n")
			  .append("     fEl.val(cleanValue(fEl.val()));\n")
			  .append("   });\n");
		}
		sb.append(" }\n")
		  .append(" function sumInteger(ids) {")
		  .append("   var val = 0;\n")
		  .append("   for(var j=0;j<ids.length; j++) {\n")
		  .append("     var valStr = jQuery('#' + copyIds[j]).val();\n")
		  .append("     if(valStr != null && valStr != '') {")
		  .append("       valStr = cleanValue(valStr);\n")
		  .append("       val += parseInt(valStr);\n")
		  .append("     }\n")
		  .append("   }\n")
		  .append("   return val;\n")
		  .append(" }\n")
		  .append(" function cleanValue(str) {\n")
		  .append("   str = replaceAll(str, \" \");\n")
		  .append("   str = removeFraction(str, '.');\n")
		  .append("   str = removeFraction(str, ',');\n")
		  .append("   str = replaceAll(str, \".\");\n")
		  .append("   str = replaceAll(str, \",\");\n")
		  .append("   str = replaceAll(str, \"'\");\n")
		  .append("   str = replaceAll(str, \"\u2019\");\n")
		  .append("   str = replaceAll(str, \"\u2032\");\n")
		  .append("   return str;\n")
		  .append(" }\n")
		  .append(" function replaceAll(str, find) {\n")
		  .append("   while(str.indexOf(find) >= 0) {\n")
		  .append("     str = str.replace(find,\"\");\n")
		  .append("   }\n")
		  .append("   return str;\n")
		  .append(" }\n")
		  .append(" function removeFraction(val, separator) {\n")
		  .append("   var lastIndex = val.lastIndexOf(separator);\n")
		  .append("   var backIndex = val.length - lastIndex - 1;\n")
		  .append("   if(lastIndex >= 0 && (backIndex == 1 || backIndex == 2)) {\n")
		  .append("     return val.substring(0, lastIndex);\n")
		  .append("   }\n")
		  .append("   return val;\n")
		  .append(" }\n")
		  .append("});\n")
		  .append("</script>");
	}
	
	private void renderCopyArray(StringOutput sb, List<TextElement> elementsToCopy) {
		sb.append("  var copyIds = [");
		boolean start = true;
		for(TextElement elementToCopy:elementsToCopy) {
			if(start) {
				start = false;
			} else {
				sb.append(",");
			}
			String copyFromId = elementToCopy.getFormDispatchId();
			sb.append("\"").append(copyFromId).append("\"");
		}
		sb.append("];\n");
	}
}
