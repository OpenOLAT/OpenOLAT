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
package org.olat.core.gui.components.textboxlist;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.elements.TextBoxListElementComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.TextBoxListElementImpl;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;

/**
 * 
 * Initial date: 14 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextBoxListTagifyRenderer extends DefaultComponentRenderer {


	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult,
			String[] args) {

		TextBoxListComponent tblComponent = (TextBoxListElementComponent) source;
		if (tblComponent.isEnabled()) {
			renderEnabledMode(tblComponent, sb, translator);
		} else {
			renderDisabledMode(tblComponent, sb);
		}
	}

	/**
	 * renders the component in Enabled / editable mode
	 * 
	 * @param tblComponent
	 *            the component to render
	 * @param sb
	 *            the StringOutput
	 * @param translator
	 */
	private void renderEnabledMode(TextBoxListComponent tblComponent, StringOutput sb, Translator translator) {
		TextBoxListElementImpl te = ((TextBoxListElementComponent)tblComponent).getTextElementImpl();
		String dispatchId = tblComponent.getFormDispatchId();
		
		String initialValJson = renderItemsAsJsonString(tblComponent.getCurrentItems());
		sb.append("<input type='text' id='textboxlistinput").append(dispatchId).append("'")
		  .append(" name='textboxlistinput").append(dispatchId).append("'");
		if (te.hasFocus()) {
			sb.append(" autofocus");
		}
		sb.append(" value='").append(initialValJson).append("'");
		Translator myTrans = Util.createPackageTranslator(this.getClass(), translator.getLocale());
		sb.append(" placeholder='").append(Formatter.escapeDoubleQuotes(myTrans.translate("add.enter"))).append("' class='o_textbox'/>\n");
		
		renderTagifyMode(tblComponent, sb);
	}
	
	private void renderTagifyMode(TextBoxListComponent tblComponent, StringOutput sb) {
		TextBoxListElementImpl te = ((TextBoxListElementComponent)tblComponent).getTextElementImpl();
		Form rootForm = te.getRootForm();
		String dispatchId = tblComponent.getFormDispatchId();
		
		// generate the JS-code for the bootstrap tagsinput
		sb.append(FormJSHelper.getJSStart())
		  .append("jQuery(function(){\n")
		  .append("  var tagEl = document.querySelector('#textboxlistinput").append(dispatchId).append("');\n")
		  .append("  var tagify = new Tagify(tagEl, {\n")
		  .append("   transformTag: function(tagData) {\n")
		  .append("     tagData.style = '--tag-bg:' + tagData.color;\n")
		  .append("     if(tagData.value.indexOf('a:') == 0) {\n")
		  .append("       tagData.class += ' o_tag_admin';\n")
		  .append("     }\n")
		  .append("   },\n")
		  .append("   keepInvalidTags:true,\n");
		
		List<TextBoxItem> autocompleteContent = tblComponent.getAutoCompleteContent();
		if(autocompleteContent != null && !autocompleteContent.isEmpty()) {
			sb.append("   whitelist: ").append(renderAutoCompleteContentAsTagifyJson(tblComponent.getAutoCompleteContent(), tblComponent.getCurrentItems())).append(",\n");
		}
		  
		sb.append("   enforceWhitelist: ").append(!tblComponent.isAllowNewValues()).append(",\n")
		  .append("   templates: {\n")
		  .append("     tag : function(value, tagData){\n")
		  .append("       var labelVal = (value.indexOf('a:') == 0) ? value.substring(2, value.length) : value;\n")
		  .append("       return \"<tag title='\" + (tagData.title || value) + \"' contenteditable='false' spellcheck='false' class='tagify__tag \" + (tagData[\"class\"] ? tagData[\"class\"] : '') + \"' \" + (this.getAttributes(tagData)) + \"><x title='' class='tagify__tag__removeBtn' role='button' aria-label='remove tag'></x><div><span class='tagify__tag-text'>\" + labelVal + \"</span></div></tag>\";\n")
		  .append("     },\n")
		  .append("     dropdownItem: function(tagData) {\n")
		  .append("       if(tagData.searchBy == 'zxyhideme') return \"<div class='tagify__dropdown__item ' style='display:none;'></div>\";\n")
		  .append("       var tagValue = (tagData.value.indexOf('a:') == 0) ? tagData.value.substring(2, tagData.value.length) : tagData.value;\n")
		  .append("       return \"<div class='tagify__dropdown__item' \" + ((typeof tagData.color !== 'undefined') ? \"style='background-color:\" + tagData.color + \";'\" : \"\") + \"><span>\" + tagValue + \"</span></div>\";\n")
		  .append("     }\n")
		  .append("   },\n")
		  .append("   dropdown: {\n")
		  .append("     classname: 'o_textbox_dropdown_item',\n")
		  .append("     enabled: 0,\n")
		  .append("     maxItems: 250,\n")
		  .append("     fuzzySearch: true\n")
		  .append("   }\n")
		  .append("  }).on('add',function(event) {\n")
		  .append(FormJSHelper.getSetFlexiFormDirtyFnCallOnly(rootForm)).append("\n")
		  .append("  }).on('remove',function(event) {\n")
		  .append(FormJSHelper.getSetFlexiFormDirtyFnCallOnly(rootForm)).append("\n")
		  .append("  });\n");
		
		if(tblComponent.hasMapper()) {
			String mapperUrl = tblComponent.getMapperUri();
			sb.append("tagify.on('input', function(e) {\n")
			  .append("  var value = e.detail.value;\n")
			  .append("  tagify.settings.whitelist.length = 0;") // reset
			  .append("  jQuery.ajax('").append(mapperUrl).append("?term=' + value, {\n")
			  .append("    type:'POST',\n")
			  .append("    cache: false,\n")
			  .append("    dataType: 'json',\n")
			  .append("    success: function(responseData, textStatus, jqXHR) {\n")
			  .append("      tagify.settings.whitelist = responseData;\n")
			  .append("      tagify.dropdown.show.call(tagify, value);\n")// render the suggestions dropdown
			  .append("    }\n")
			  .append("  });\n")
			  .append("});");
		}
		
		sb.append("});\n")
		  .append(FormJSHelper.getJSEnd());
	}
	
	private String renderItemsAsJsonString(List<TextBoxItem> content) {
		JSONArray array = new JSONArray();
		if (content != null && !content.isEmpty()) {
			OWASPAntiSamyXSSFilter filter = new OWASPAntiSamyXSSFilter();
			for(TextBoxItem item:content) {
				String antiItem = filter.filter(item.getValue());
				if(StringHelper.containsNonWhitespace(antiItem)) {
					JSONObject obj = new JSONObject();
					obj.put("value", antiItem);
					if(StringHelper.containsNonWhitespace(item.getColor())) {
						obj.put("color", item.getColor());
					}
					if(StringHelper.containsNonWhitespace(item.getLabel())) {
						obj.put("label", item.getLabel());
					}
					if(!item.isEditable()) {
						obj.put("readonly", "true");
					}
					array.put(obj);
				}	
			}
		}
		return array.toString();
	}
	
	private String renderAutoCompleteContentAsTagifyJson(List<TextBoxItem> autoCompletionValues, List<TextBoxItem> currentItems) {
		JSONArray array = new JSONArray();
		if (autoCompletionValues != null && !autoCompletionValues.isEmpty()) {
			OWASPAntiSamyXSSFilter filter = new OWASPAntiSamyXSSFilter();
			for(TextBoxItem item:autoCompletionValues) {
				addItem(array, item, filter);
			}
		}
		if (currentItems != null && !currentItems.isEmpty()) {
			OWASPAntiSamyXSSFilter filter = new OWASPAntiSamyXSSFilter();
			for(TextBoxItem item:currentItems) {
				if(!item.isEditable()) {
					addItem(array, item, filter);
				}
			}
		}
		return array.toString();
	}
	
	private void addItem(JSONArray array, TextBoxItem item, OWASPAntiSamyXSSFilter filter) {
		String antiItem = filter.filter(item.getValue());
		if(StringHelper.containsNonWhitespace(antiItem)) {
			JSONObject obj = new JSONObject();
			obj.put("value", antiItem);
			if(StringHelper.containsNonWhitespace(item.getColor())) {
				obj.put("color", item.getColor());
			}
			if(StringHelper.containsNonWhitespace(item.getLabel())) {
				obj.put("label", item.getLabel());
				obj.put("searchBy", item.getLabel());
			}
			if(!item.isEditable()) {
				obj.put("searchBy", "zxyhideme");
			}
			array.put(obj);
		}
	}

	/**
	 * Renders the textBoxListComponent in disabled/read-only mode
	 * 
	 * @param tblComponent
	 * @param output
	 */
	private void renderDisabledMode(TextBoxListComponent tblComponent, StringOutput output) {
		// read only view, we just display the initialItems as
		// comma-separated string
		List<TextBoxItem> items = tblComponent.getCurrentItems();
		if (items != null && !items.isEmpty()) {
			output.append("<span class='o_textbox_disabled'><i class='o_icon o_icon_tags'> </i> ");
			for (TextBoxItem item : items) {
				output.append("<span class='tag label label-info");
				String label = item.getLabel();
				if(label.startsWith("a:")) {
					output.append(" o_tag_admin");
					label = label.substring(2, label.length());
				}
				output.append("'");
				
				String color;
				if(StringHelper.containsNonWhitespace(item.getColor())) {
					color = item.getColor();
				} else {
					color = "#5bc0de";
				}
				output.append(" style='background-color:").append(color).append(";'")
				      .append(">").append(label).append("</span>");								
			}
			output.append("</span>");
		} else {
			output.append("-");
		}
	}
}
