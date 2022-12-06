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
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 5 déc. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextElementInlineRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {

		AbstractInlineElementComponent aiec = (AbstractInlineElementComponent) source;

		TextElementImpl itei = (TextElementImpl) aiec.getFormItem();
		StringBuilder htmlVal = new StringBuilder();
		
		/**
		 * in case of an error show the test which caused the error which must be stored by the textelement in the transientValue.
		 * the last valid value is always set over setValue(..) by the textelement, and thus can be retrieved as such here.
		 */
		String tmpVal;
		String emptyVal = (itei.isInlineEditingOn() ? "" : itei.getEmptyDisplayText());
		if(itei.hasError()){
			tmpVal = StringHelper.containsNonWhitespace(itei.getTransientValue()) ? itei.getTransientValue() : emptyVal;
		}else{
			tmpVal = StringHelper.containsNonWhitespace(itei.getValue()) ? itei.getValue() : emptyVal;
		}
		// append the html safe value
		htmlVal.append(StringHelper.escapeHtml(tmpVal));
		if (!itei.isEnabled()) {
			// RO view and not clickable
			String id = aiec.getFormDispatchId();
			sb.append("<div class='form-control-static' id=\"").append(id).append("\" ")
			  .append(" >").append(htmlVal).append("</div>");
		} else {
			//
			// Editable view
			// which can be left
			// .......with clicking outside -> onBlur saves the value
			// .......pressing ENTER/RETURN or TAB -> onBlur saves the value
			// .......presssing ESC -> restore previous value and submit this one.
			if (itei.isInlineEditingOn()) {
				String id = aiec.getFormDispatchId();
				// read write view
				sb.append("<input type=\"").append("input").append("\" class=\"form-control\" id=\"");
				sb.append(id);
				sb.append("\" name=\"");
				sb.append(id);
				sb.append("\" size=\"");
				sb.append("30");
				// if(itei.maxlength > -1){
				// sb.append("\" maxlength=\"");
				// sb.append(itei.maxlength);
				// }
				sb.append("\" value=\"");
				sb.append(htmlVal);
				sb.append("\" >");
				
				// Javascript
				sb.append(FormJSHelper.getJSStart());
				// clicking outside or pressing enter -> OK, pressing ESC -> Cancel
				FormJSHelper.getInlineEditOkCancelJS(sb, id, StringHelper.escapeHtml(itei.getValue()), itei.getRootForm());
				sb.append(FormJSHelper.getJSEnd());

			} else {
				// RO<->RW view which can be clicked 
				Translator trans = Util.createPackageTranslator(TextElementImpl.class, translator.getLocale(), translator);
				String id = aiec.getFormDispatchId();
				sb.append("<div id='").append(id).append("' class='form-control-static' title=\"")
					.appendHtmlEscaped(trans.translate("inline.edit.help"))
					.append("\" ")
					.append(FormJSHelper.getRawJSFor(itei.getRootForm(), id, itei.getAction()))
					.append("> ")
					.append(htmlVal)
					.append(" <i class='o_icon o_icon_inline_editable'> </i></div>");
			}
			
		}//endif 
	}
}
