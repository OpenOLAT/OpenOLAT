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
package org.olat.core.gui.components.form.flexible.impl.elements;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.AutoCompleter;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 20.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AutoCompleterRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {

		AutoCompleterComponent cmp = (AutoCompleterComponent)source;
		AutoCompleter autoCompleter = cmp.getFormItem();
		
		int limit = autoCompleter.getMaxEntries();
		if(limit <= 0) {
			limit = 5;
		}
		
		final int inputSize = 72;
		String id = autoCompleter.getFormDispatchId();
		if(autoCompleter.isEnabled()) {
			String mapperUri = autoCompleter.getMapperUri();
			int minLength = autoCompleter.getMinLength();
			StringOutput command = new StringOutput(64);
			ubu.createCopyFor(cmp).openXHREvent(command, null, false, false,
					new NameValuePair(VelocityContainer.COMMAND_ID, "select"));
			
			String noResults = translator.translate("autocompletion.results.none");
			
			sb.append("<input type='text' class='form-control' size='").append(inputSize).append("' id='").append(id)
			  .append("' name='").append(id).append("' value=\"");
			if(StringHelper.containsNonWhitespace(autoCompleter.getValue())) {
				sb.append(StringHelper.escapeHtml(autoCompleter.getValue()));
			}
			sb.append("\"");
			if (autoCompleter.hasFocus()) {
				sb.append(" autofocus");
			}
			sb.append(">");
			sb.append("<script>\n")
			  .append("\"use strict\";")
			  .append("jQuery(function(){\n")
			  .append("  var fullNameTypeahead = new Bloodhound({\n")
			  .append("   datumTokenizer: function (d) {\n")
			  .append("    return Bloodhound.tokenizers.whitespace(d.value);\n")
			  .append("   },\n")
			  .append("   queryTokenizer: Bloodhound.tokenizers.whitespace,\n")
			  .append("   remote: {\n")
			  .append("     url: '").append(mapperUri).append("/?place=holder&term=%QUERY',\n")//place holder is useless but for tomcat, sometimes it said that the term parameter is corrupted and will not be part of the request send to OpenOLAT
			  .append("     wildcard: '%QUERY',\n")
			  .append("     filter: function ( response ) {\n")
			  .append("      return jQuery.map(response, function (object) {\n")
			  .append("       return {\n")
			  .append("           cssClass: '' + object.cssClass,\n")
			  .append("           value: '' + object.key,\n");
			if(autoCompleter.isShowDisplayKey()) {
				sb.append("           fullName: object.displayKey + \": \" + object.value\n");
			} else {
				sb.append("           fullName: object.value\n");
			}
			sb.append("         };\n")
			  .append("       });\n")
			  .append("     }\n")
			  .append("   }\n")
			  .append(" });\n")
			  .append(" fullNameTypeahead.initialize();\n")
			  .append(" jQuery('#").append(id).append("').typeahead({\n")
			  .append("   hint: false,\n")
			  .append("   highlight: false,\n")
			  .append("   minLength: ").append(minLength).append("\n")
			  .append(" },{\n")
			  .append("   minLength: ").append(minLength).append(",\n")
			  .append("   displayKey: 'fullName',\n")
			  .append("   limit: ").append(limit).append(",\n")
			  .append("   source: fullNameTypeahead.ttAdapter(),\n")
			  .append("   templates: {\n")
			  .append("     suggestion: function(obj) {\n")
			  .append("       var s = '<div>';\n")
			  .append("       if(obj.cssClass !== \"undefined\" && obj.cssClass != null && obj.cssClass.length > 0) {\n")
			  .append("         s += \"<i class='o_icon \" + obj.cssClass + \"'> </i> \";\n")
			  .append("       }\n")
			  .append("       if('AUTOCOMPLETER_NO_RESULT' === obj.value) {\n")
			  .append("         s += '").append(noResults).append("';\n")
			  .append("       } else {\n")
			  .append("         s += obj.fullName;\n")
			  .append("       }\n")
			  .append("       return s + '</div>';")
			  .append("     }\n")
			  .append("   }")
			  .append(" }).on('typeahead:selected', function (e, object) {\n")
			  .append("   ").append(command).append(",'key',object.value,'value',object.fullName);\n")
			  .append(" })")
			  .append(".on('typeahead:render', function (e, object) {\n") // extend main height if rendered within the main content area and there is not enough space
			  .append("   OPOL.adjustContentHeightForAbsoluteElement('#o_main_center_content_inner .tt-menu');\n")
			  .append(" });\n")
			  .append("});\n")
			  .append("</script>");
		} else {
			String value = "";
			if(StringHelper.containsNonWhitespace(autoCompleter.getValue())) {
				value = autoCompleter.getValue();
			}
			sb.append("<input id=\"").append(id).append("\" type=\"test\" disabled=\"disabled\" class=\"form-control o_disabled\" size=\"")
			  .append(inputSize)
			  .append("\" value=\"").append(value).append("\" />")
			  .append("</span>");
		}
	}
}