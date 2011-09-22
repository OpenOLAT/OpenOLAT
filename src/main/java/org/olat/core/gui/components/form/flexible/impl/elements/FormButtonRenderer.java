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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package org.olat.core.gui.components.form.flexible.impl.elements;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * Description:<br>
 * TODO: patrickb Class Description for FormSubmitRenderer
 * <P>
 * Initial Date: 08.12.2006 <br>
 * 
 * @author patrickb
 */
class FormButtonRenderer implements ComponentRenderer {
	@SuppressWarnings("unused")
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		//
		FormButtonComponent fsC = (FormButtonComponent) source;
		FormButton fs = fsC.getFormButton();
		fs.getRootForm().getDispatchFieldId();
		//
		String id = fsC.getFormDispatchId();
		//may be no id needed.. problems if the same submit button is 
		//rendered twice, e.g. usability
		sb.append("<button type=\"button\" id=\"");
		sb.append(id);
		//name must stay id, this is used to fetch all submit buttons by name
		sb.append("\" name=\"");
		sb.append(id);
		sb.append("\" value=\"");
		sb.append(StringEscapeUtils.escapeHtml(fs.getTranslated()));
		sb.append("\" ");
		if(!source.isEnabled()){
			sb.append(" disabled=\"disabled\" ");
		}
		sb.append(FormJSHelper.getRawJSFor(fs.getRootForm(), id, fs.getAction()));
		sb.append(" class=\"b_button ");
		if(!source.isEnabled()){
			sb.append(" b_disabled ");
		}		
		sb.append("\"><span>");
		sb.append(fs.getTranslated());
		sb.append("</span></button>");
		if(source.isEnabled() && fsC.getIsSubmitAndValidate()){
			//it is a submitting and validating button (e.g. FormSubmit)
			sb.append("<script type=\"text/javascript\">\n /* <![CDATA[ */ \n");
			sb.append(FormJSHelper.getJSSubmitRegisterFn(fs.getRootForm(),id));
			if(!fs.getRootForm().isSubmittedAndValid()){
				//mark as dirty, because form is not yet submitted or
				//it was submitted but has errors.
				sb.append(FormJSHelper.getSetFlexiFormDirtyFnCallOnly(fs.getRootForm()));
			}
			sb.append("\n/* ]]> */ \n</script>");
		}
	}

	@SuppressWarnings("unused")
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
	// TODO Auto-generated method stub

	}

	@SuppressWarnings("unused")
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderingState rstate) {
	// TODO Auto-generated method stub

	}

}