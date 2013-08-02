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
import org.olat.core.util.StringHelper;

/**
 * Description:<br>
 * TODO: patrickb Class Description for CheckboxRenderer
 * 
 * <P>
 * Initial Date:  04.01.2007 <br>
 * @author patrickb
 */
class CheckboxRenderer implements ComponentRenderer {

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#render(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component, org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator, org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		//default should allow <b> </b> coming from localstring properties (see also http://bugs.olat.org/jira/browse/OLAT-4208)

		CheckboxElementComponent cec = (CheckboxElementComponent)source;
		boolean escapeHTML = cec.isEscapeHtml();
		
		String subStrName = "name=\"" + cec.getGroupingName() + "\"";
		
		String key = cec.getKey();
		String value = cec.getValue();
		if(escapeHTML){
			key = StringEscapeUtils.escapeHtml(key);
			value = StringEscapeUtils.escapeHtml(value);
		}
		
		boolean selected = cec.isSelected();
		
		
		//read write view
		String cssClass = cec.getCssClass(); //optional CSS class
		sb.append("<input type=\"checkbox\" ");
		sb.append("id=\"");
		sb.append(cec.getFormDispatchId());
		sb.append("\" ");
		sb.append("class=\"b_checkbox\" ");
		sb.append(subStrName);
		sb.append(" value=\"");
		sb.append(key);
		sb.append("\"");
		if (selected) sb.append(" checked=\"checked\" ");
		if(!source.isEnabled()){
			sb.append(" disabled=\"disabled\" ");
		}else{
			//use the selection form dispatch id and not the one of the element!
			sb.append(FormJSHelper.getRawJSFor(cec.getRootForm(),cec.getSelectionElementFormDisId(), cec.getAction()));
		}
		sb.append(" />");
		if (cssClass !=  null) sb.append("<span class=\"").append(cssClass).append("\">");
		if (StringHelper.containsNonWhitespace(value)) {
			sb.append("<label class=\"b_checkbox_label\" for=\"").append(cec.getFormDispatchId()).append("\">");
			sb.append(value);
			sb.append("</label>");			
		}
		if (cssClass !=  null) sb.append("</span>");
		
		if(source.isEnabled()){
			//add set dirty form only if enabled
			sb.append(FormJSHelper.getJSStartWithVarDeclaration(cec.getFormDispatchId()));
			sb.append(FormJSHelper.getSetFlexiFormDirtyForCheckbox(cec.getRootForm(), cec.getFormDispatchId()));
			sb.append(FormJSHelper.getJSEnd());
		}
	
	}

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#renderBodyOnLoadJSFunctionCall(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component, org.olat.core.gui.render.RenderingState)
	 */
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
	// TODO Auto-generated method stub

	}

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#renderHeaderIncludes(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component, org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator, org.olat.core.gui.render.RenderingState)
	 */
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderingState rstate) {
	// TODO Auto-generated method stub

	}

}
