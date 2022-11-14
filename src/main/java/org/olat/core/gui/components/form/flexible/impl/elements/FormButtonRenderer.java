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
 * <P>
 * Initial Date: 08.12.2006 <br>
 * 
 * @author patrickb
 */
class FormButtonRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {

		FormButtonComponent fsC = (FormButtonComponent) source;
		FormButton fs = fsC.getFormItem();
		String id = fsC.getFormDispatchId();
		//may be no id needed.. problems if the same submit button is 
		//rendered twice, e.g. usability
		sb.append("<button type=\"button\" id=\"").append(id)
		//name must stay id, this is used to fetch all submit buttons by name
		  .append("\" name=\"").append(id)
		  .append("\" value=\"").append(StringHelper.escapeHtml(fs.getTranslated())).append("\" ");
		if(!source.isEnabled()){
			sb.append(" disabled=\"disabled\" ");
		} else if (fs.hasFocus()) {
			sb.append(" autofocus");
		}
		
		StringBuilder js = FormJSHelper.getRawJSFor(fs.getRootForm(), id, fs.getAction(), fs.isNewWindowAfterDispatchUrl(), null, id);
		sb.append(js)
		// Prevent 2 submits by onchange and click (button or submit) events
		  .append(" onmousedown=\"o_info.preventOnchange=true;\" onmouseup=\"o_info.preventOnchange=false;\" class=\"btn");
		if (fsC.getIsSubmitAndValidate()) {
			sb.append(" btn-primary");			
		} else {
			sb.append(" btn-default");						
		}
		if(!source.isEnabled()){
			sb.append(" o_disabled");
		}
		if(StringHelper.containsNonWhitespace(fs.getElementCssClass())) {
			sb.append(" ").append(fs.getElementCssClass());
		}
		if(fs.isNewWindowAfterDispatchUrl()) {
			sb.append(" o_new_window");
		}
		
		sb.append("\">");
		
		// CSS icon
		if (fs.getIconLeftCSS() != null) {
			sb.append("<i class='").append(fs.getIconLeftCSS()).append("'></i> "); // one space needed
		}
					
		sb.append("<span>").append(fs.getTranslated()).append("</span>");
		
		// CSS icon
		if (fs.getIconRightCSS() != null) {
			sb.append(" <i class='").append(fs.getIconRightCSS()).append("'></i> ");  // one space needed
		}
		
		if(source.isEnabled() && fsC.getIsSubmitAndValidate()){
			//it is a submitting and validating button (e.g. FormSubmit)
			sb.append("<script>\n");
			sb.append(FormJSHelper.getJSSubmitRegisterFn(fs.getRootForm(),id));
			if(!fs.getRootForm().isSubmittedAndValid()){
				//mark as dirty, because form is not yet submitted or
				//it was submitted but has errors.
				sb.append(FormJSHelper.getSetFlexiFormDirtyFnCallOnly(fs.getRootForm()));
			}
			sb.append("</script>");
		}
		sb.append("</button>");
	}
}