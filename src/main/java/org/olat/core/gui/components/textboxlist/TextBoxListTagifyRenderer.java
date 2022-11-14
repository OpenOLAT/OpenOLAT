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
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
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

		TextBoxListElementComponent tblComponent = (TextBoxListElementComponent) source;
		if (tblComponent.isEnabled()) {
			renderEnabledMode(tblComponent, sb, translator);
		} else {
			renderDisabledMode(tblComponent, sb, translator);
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
	private void renderEnabledMode(TextBoxListElementComponent tblComponent, StringOutput sb, Translator translator) {
		TextBoxListElementImpl te = tblComponent.getFormItem();
		String dispatchId = tblComponent.getFormDispatchId();
		
		String initialValJson = renderItemsAsJsonString(tblComponent.getCurrentItems());
		if (StringHelper.containsNonWhitespace(tblComponent.getIcon())) {
			sb.append("<span class='o_tags_with_icon input-group-sm'>");
			sb.append("<i class='o_icon o_icon_fw ")
			  .append(tblComponent.getIcon()).append("'")
			  .append(StringHelper.containsNonWhitespace(tblComponent.getIconTitleKey()) ? " title='" + translator.translate(tblComponent.getIconTitleKey()) + "'" : "")
			  .append("> </i>");
		}
		if (tblComponent.isShowLabelAsInlineText() && StringHelper.containsNonWhitespace(tblComponent.getFormItem().getLabelText())) {
			sb.append("&nbsp;").append(tblComponent.getFormItem().getLabelText()).append("&nbsp;");
		}
		if (tblComponent.showSaveButton()) {
			sb.append("<span class='input-group input-group-sm'>");
		}
		if (!tblComponent.showSaveButton() && !StringHelper.containsNonWhitespace(tblComponent.getIcon())) {
			sb.append("<span class='input-group-sm'>");
		}
		sb.append("<input class='form-control' type='text' id='textboxlistinput").append(dispatchId).append("'")
		  .append(" name='textboxlistinput").append(dispatchId).append("'");
		if (te.hasFocus()) {
			sb.append(" autofocus");
		}
		sb.append(" value='").append(initialValJson).append("'");
		Translator myTrans = Util.createPackageTranslator(this.getClass(), translator.getLocale());
		sb.append(" placeholder='").append(Formatter.escapeDoubleQuotes(myTrans.translate("add.enter"))).append("' class='o_textbox'/>\n");
		
		renderTagifyMode(tblComponent, sb);
		
		if (tblComponent.showSaveButton()) {
			sb.append("<span class='input-group-btn'>")
			  .append("<button class='btn btn-primary o_button_dirty' type='submit'");
			
			StringBuilder js = FormJSHelper.getRawJSFor(te.getRootForm(), dispatchId, FormEvent.ONCLICK);
			sb.append(js);
			sb.append(">")
			  .append(translator.translate("save"))
			  .append("</button></span>")
			  .append("</span>");
		}
		
		if (!tblComponent.showSaveButton()) {
			sb.append("</span>");
		}
	}
	
	private void renderTagifyMode(TextBoxListElementComponent tblComponent, StringOutput sb) {
		TextBoxListElementImpl te = tblComponent.getFormItem();
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
		  .append("     tagData.class += ' o_tag");
		if (StringHelper.containsNonWhitespace(tblComponent.getCustomCSSForItems())) {
			sb.append(" " + tblComponent.getCustomCSSForItems());
		}
		sb.append("'\n")
		  .append("   },\n")
		  .append("   keepInvalidTags:false,\n");
		
		List<TextBoxItem> autocompleteContent = tblComponent.getAutoCompleteContent();
		if(autocompleteContent != null && !autocompleteContent.isEmpty()) {
			sb.append("   whitelist: ").append(renderAutoCompleteContentAsTagifyJson(tblComponent.getAutoCompleteContent(), tblComponent.getCurrentItems())).append(",\n");
		}
		  
		sb.append("   enforceWhitelist: ").append(!tblComponent.isAllowNewValues()).append(",\n")
		  .append("   templates: {\n")
		  .append("     tag : function(value, tagData){\n")
		  .append("       var label = (tagData.label || value);\n")
		  .append("       var labelVal = (label.indexOf('a:') == 0) ? label.substring(2, value.length) : label;\n")
		  .append("       return \"<tag title='\" + label + \"' contenteditable='false' spellcheck='false' class='tagify__tag \" + (tagData[\"class\"] ? tagData[\"class\"] : '') + \"' \" + (this.getAttributes(tagData)) + \"><x title='' class='tagify__tag__removeBtn' role='button' aria-label='remove tag'></x><div><span class='tagify__tag-text'>\" + label + \"</span></div></tag>\";\n")
		  .append("     },\n")
		  .append("     dropdownItem: function(tagData) {\n")
		  .append("       if(tagData.searchBy == 'zxyhideme') return \"<div class='tagify__dropdown__item ' style='display:none;'></div>\";\n")
		  .append("       var tagValue = (tagData.label || tagData.value);\n")
		  .append("       var tagInfo = '';\n")
		  .append("       if(tagData.info) {\n")
		  .append("         tagInfo = '<div class=\"text-muted o_textbox_dropdown_info\">' + tagData.info + '</div>';\n")
		  .append("       }\n")
		  .append("       tagValue = (tagValue.indexOf('a:') == 0) ? tagValue.substring(2, tagValue.length) : tagValue;\n")
		  .append("       return \"<div class='o_tag ")
	  		.append(StringHelper.containsNonWhitespace(tblComponent.getCustomCSSForItems()) ? tblComponent.getCustomCSSForItems() : "")
	  		.append(" tagify__dropdown__item' \" + ((typeof tagData.color !== 'undefined') ? \"style='background-color:\" + tagData.color + \";'\" : \"\") + \"><div class='o_textbox_dropdown_item_value'>\" + tagValue + \"</div>\" + tagInfo + \"</div>\";\n")
		  .append("     }\n")
		  .append("   },\n")
		  .append("   dropdown: {\n")
		  .append("     classname: 'o_textbox_dropdown',\n")
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
			if(StringHelper.containsNonWhitespace(item.getDropDownInfo())) {
				obj.put("info", item.getDropDownInfo());
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
	private void renderDisabledMode(TextBoxListElementComponent tblComponent, StringOutput output, Translator translator) {
		// read only view, we just display the initialItems as
		// comma-separated string
		List<TextBoxItem> items = tblComponent.getCurrentItems();
		output.append("<span class='o_textbox_disabled o_tags_with_icon'>");
		
		if (StringHelper.containsNonWhitespace(tblComponent.getIcon())) {
			output.append("<i class='o_icon o_icon_fw ")
			  	  .append(tblComponent.getIcon()).append("'")
			  	  .append(StringHelper.containsNonWhitespace(tblComponent.getIconTitleKey()) ? " title='" + translator.translate(tblComponent.getIconTitleKey()) + "'" : "")
			  	  .append("> </i>");
		}
		
		if (tblComponent.isShowLabelAsInlineText() && StringHelper.containsNonWhitespace(tblComponent.getFormItem().getLabelText())) {
			output.append("&nbsp;").append(tblComponent.getFormItem().getLabelText()).append("&nbsp;");
		}
		
		if (items != null) {
			int pos = 0;
			for (TextBoxItem item : items) {
				String dispatchId = tblComponent.getFormDispatchId() + "_" + pos;
				
				output.append("<span")
					.append(" id=o_tag_item_" + dispatchId)
					.append(" class='o_tag");
				if (StringHelper.containsNonWhitespace(item.getCustomCSS())) {
					output.append(" " + item.getCustomCSS());
				}
				String label = item.getLabel();
				if(!StringHelper.containsNonWhitespace(label)) {
					label = item.getValue();
				}
				if(label.startsWith("a:")) {
					output.append(" o_tag_admin");
					label = label.substring(2, label.length());
				}
				output.append("'");
				
				if(StringHelper.containsNonWhitespace(item.getColor())) {
					output.append(" style='background-color:").append(item.getColor()).append(";'");
				}
				
				output.append(">").append(label).append("</span>");		
				if (item.showTooltip()) {
					// render tooltip only when it contains something
					output.append("<script>")
					  .append("jQuery(function() {\n")
					  .append("  jQuery('#o_tag_item_").append(dispatchId).append("').tooltip({\n")
					  .append("    html: true,\n")
					  .append("    container: 'body',\n")
					  .append("    title: '").append(item.getTooltip()).append("' \n")
					  .append("  });\n")
					  .append("});")
					  .append("</script>");
				}
				
				// Increment position
				pos++;
			}
		} 
		
		output.append("</span>");
	}
}
