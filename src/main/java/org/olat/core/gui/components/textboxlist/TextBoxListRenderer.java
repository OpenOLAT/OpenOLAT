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

import java.util.Map;
import java.util.Map.Entry;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.elements.TextBoxListElementComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.TextBoxListElementImpl;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * Description:<br>
 * renderer for the textboxlist-component can be used in a flexiform mode or
 * without and will then provide its own form
 * 
 * <P>
 * Initial Date: 23.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class TextBoxListRenderer implements ComponentRenderer {

	private static OLog logger = Tracing.createLoggerFor(TextBoxListRenderer.class);
	
	/**
	 * default constructor
	 */
	public TextBoxListRenderer() {

	}

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#render(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder,
	 *      org.olat.core.gui.translator.Translator,
	 *      org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
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
	 * @param output
	 *            the StringOutput
	 * @param translator
	 */
	private void renderEnabledMode(TextBoxListComponent tblComponent, StringOutput output, Translator translator) {
		
		/* if in debug mode, create more readable javascript code */
		String lineBreak = "";
		if(logger.isDebug()){
			lineBreak = Character.toString((char)10);
			logger.debug("rendering TextBoxListComponent in debug mode, nice JS output.");
		}
		
		TextBoxListElementImpl te = ((TextBoxListElementComponent)tblComponent).getTextElementImpl();
		Form rootForm = te.getRootForm();
		String dispatchId = tblComponent.getFormDispatchId();

		output.append("<input type=\"text\" id=\"textboxlistinput").append(dispatchId).append("\" ")
		      .append("name='textboxlistinput").append(dispatchId).append("' ")
		      .append("value='").append(tblComponent.getInitialItemsAsString()).append("'")
		      .append("/>\n");

		// OO-137 : here, we display the currentItems. (at first render, this is equal to initialItems)
		// on succeeding rendering, we want to reflect the current component status

		// generate the JS-code for the tagit
		output.append(FormJSHelper.getJSStart());
		output.append("jQuery(function(){\n")
		      .append("  jQuery('#textboxlistinput").append(dispatchId).append("').tagit({\n")
		      .append("    allowDuplicates:").append(tblComponent.isAllowDuplicates()).append(",\n")
		      .append("    autocomplete: {\n")
		      .append("      delay: 100,");
		
		//set autocompleter.source if a provider is around
		if (tblComponent.getProvider() != null) {
			String mapperUri = tblComponent.getMapperUri();
			output.append("    source: function(request, response) {\n")
			      .append("      jQuery.ajax('").append(mapperUri).append("',{\n")
			      .append("        data: request,\n")
			      .append("        dataType:'json',\n")
			      .append("        success: function(data) {\n")
			      .append("          response(jQuery.map(data, function( item ) {\n")
			      .append("            return item;\n")
			      .append("          }));\n")
			      .append("        }\n")
			      .append("      });\n")
			      .append("    },\n");
		}
 
		output.append("      minLength: 2\n")
		      .append("    },\n")
		      .append("    availableTags:[");
		Map<String, String> initItems = tblComponent.getCurrentItems();
		if (initItems != null) {
			boolean sep = true;
			for (Entry<String, String> item :initItems.entrySet()) {
				if(sep) sep = false;
				else output.append(",");

				String value;
				if (StringHelper.containsNonWhitespace(item.getValue())) {
					value = item.getValue();
				} else {
					value = item.getKey();
				}
				output.append("'").append(value).append("'");
			}
		}
    output.append("],\n");  
		
			// otherwise, o_ffEvents are fired: OO-137 ( invoke o_ffEvent on UserAdd or userRemove ) but only in flexiform
		String o_ffEvent = FormJSHelper.getJSFnCallFor(rootForm, dispatchId, 2);
		output.append("  afterTagAdded: function(input){\n")
			    .append(o_ffEvent).append("; console.log('Added',input)},\n")
			    .append("  afterTagRemoved: function(input){\n")
			    .append(o_ffEvent).append("; console.log('Remove',input)}\n");
		output.append("  });\n")
		      .append("})\n");
		
		output.append(FormJSHelper.getJSEnd()).append(lineBreak);
	}
	/*
	private void test(TextBoxListComponent tblComponent, StringOutput output, Translator translator) {
		
		// if in debug mode, create more readable javascript code
		String lineBreak = "";
		if(logger.isDebug()){
			lineBreak = Character.toString((char)10);
			logger.debug("rendering TextBoxListComponent in debug mode, nice JS output.");
		}
		
		TextBoxListElementImpl te = ((TextBoxListElementComponent) tblComponent).getTextElementImpl();
		Form rootForm = te.getRootForm();
		Translator fullTrans = Util.createPackageTranslator(TextBoxListRenderer.class, translator.getLocale(), translator);
		String dispatchId = tblComponent.getFormDispatchId();
		output.append("tlist = new ProtoMultiSelect('textboxlistinput").append(dispatchId).append("', 'textboxlist-auto").append(dispatchId)
				.append("',{ newValues: ").append(lineBreak);
		output.append(Boolean.toString(tblComponent.isAllowNewValues())).append(lineBreak);
		output.append(", ").append(lineBreak);
		
		if (tblComponent.getProvider() != null) {
			// use autocomplete provider instead of prebuilt-map
			String mapperUri = tblComponent.getMapperUri();
			output.append("fetchFile: '").append(mapperUri).append("', ").append(lineBreak);
		}
		output.append("jsonInputValue: false,").append(lineBreak);
		
		// hint for the input field
		String setHintKey = tblComponent.getInputHint();
		String inputHint;
		if (!StringHelper.containsNonWhitespace(setHintKey))
			inputHint = fullTrans.translate("default.input.hint");
		else
			inputHint = tblComponent.getTranslator().translate(setHintKey);
		
		output.append("inputMessage: '").append(inputHint).append("',").append(lineBreak);
		output.append("sortResults: true, ").append(lineBreak);
		output.append("autoResize: true, ").append(lineBreak);
		
		if (tblComponent.getMaxResults() > 0 ) {
			output.append("results: ").append(tblComponent.getMaxResults()).append(", ").append(lineBreak);
			output.append("maxResults: ").append(tblComponent.getMaxResults()).append(", ").append(lineBreak);
		}
		output.append("encodeEntities: false, ");
		output.append("addPrefix: '").append(fullTrans.translate("add.new.element.prefix")).append("', ").append(lineBreak);
		output.append("searchMessage: '").append(fullTrans.translate("please.wait.searching")).append("', ").append(lineBreak);
		output.append("moreMessage: '").append(fullTrans.translate("more.results.found.specify.search")).append("', ").append(lineBreak);
		output.append("allowDuplicates: ").append(Boolean.toString(tblComponent.isAllowDuplicates())).append(", ").append(lineBreak);
		output.append("loadFromInput: false,").append(lineBreak);
		output.append("fetchParameters: 'keyword', ").append(lineBreak);
		
		// depending on config of the Component, form is submitted on input
		if (false) {
			output.append("onEmptyInput: function(input){ ");
			output.append("document.forms['").append(rootForm.getFormName()).append("'].submit();} , ").append(lineBreak);
		
			output.append("onUserAdd: function(input){");
			output.append("document.forms['").append(rootForm.getFormName()).append("'].submit();} , ").append(lineBreak);
		
			output.append("onUserRemove: function(input){");
			output.append("document.forms['").append(rootForm.getFormName()).append("'].submit();} , ").append(lineBreak);
		} else {
			// otherwise, o_ffEvents are fired:
			// OO-137 ( invoke o_ffEvent on UserAdd or userRemove )
			// but only in flexiform
			String o_ffEvent = FormJSHelper.getJSFnCallFor(rootForm, dispatchId, 2);
			output.append("onEmptyInput: function(input){").append(lineBreak);
			//output.append("console.log('onEmptyInput');").append(lineBreak);
			output.append(o_ffEvent).append("; } , ").append(lineBreak);
			
			output.append("onUserAdd: function(input){").append(lineBreak);
			//output.append("console.log('onUserAdd');").append(lineBreak);
			output.append(o_ffEvent).append("; } , ").append(lineBreak);
		
			output.append("onUserRemove: function(input){").append(lineBreak);
			//output.append("console.log('onUserRemove');").append(lineBreak);
			output.append(o_ffEvent).append("; } , ").append(lineBreak);
		}
		output.append("regexSearch: false });").append(lineBreak);
		
		if (tblComponent.getProvider() == null) {
			// no provider is specified, use a prebuilt-map to look for
			// autocompletion
			output.append("var myjson = ");
			output.append(tblComponent.getAutoCompleteJSON().toString());
			output.append(";");
			output.append("myjson.each(function(t){tlist.autoFeed(t)});");
		}
	}*/

	/**
	 * Renders the textBoxListComponent in disabled/read-only mode
	 * 
	 * @param tblComponent
	 * @param output
	 */
	private void renderDisabledMode(TextBoxListComponent tblComponent, StringOutput output) {
		// read only view, we just display the initialItems as
		// comma-separated string
		String readOnlyContent = tblComponent.getInitialItemsAsString();
		if (readOnlyContent.length() > 0) {
			output.append("<div class=\"b_with_small_icon_left b_tag_icon\">");
			FormJSHelper.appendReadOnly(readOnlyContent, output);
			output.append("</div>");
		} else {
			FormJSHelper.appendReadOnly("-", output);
		}
	}

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#renderHeaderIncludes(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder,
	 *      org.olat.core.gui.translator.Translator,
	 *      org.olat.core.gui.render.RenderingState)
	 */
	@Override
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderingState rstate) {
		// nothing
	}

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#renderBodyOnLoadJSFunctionCall(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.RenderingState)
	 */
	@Override
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
		// nothing to load
	}

}
