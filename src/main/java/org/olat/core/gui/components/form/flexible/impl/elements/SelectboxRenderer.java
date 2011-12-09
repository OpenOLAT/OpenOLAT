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
import org.olat.core.gui.GUIInterna;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * Description:<br>
 * TODO: patrickb Class Description for SingleSelectionSelectboxRenderer
 * 
 * <P>
 * Initial Date:  02.01.2007 <br>
 * @author patrickb
 */
class SelectboxRenderer implements ComponentRenderer {

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#render(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component, org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator, org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
	@SuppressWarnings("unused")
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {

		SelectboxComponent ssec = (SelectboxComponent)source;
		
		if (GUIInterna.isLoadPerformanceMode()) {
			// Just make sure the replayID mapping is known
			ssec.getRootForm().getReplayableDispatchID(source);
		}
		
		String subStrName = "name=\"" + ssec.getGroupingName() + "\"";
		String[] options = ssec.getOptions();
		String[] values = ssec.getValues();
		String[] cssClasses = ssec.getCssClasses();
		
		//read write
		
		/*
		 * opening <select ... >
		 */
		sb.append("<select ");
		if(!ssec.isEnabled()){
			sb.append(" disabled=\"disabled\" ");
		}
		sb.append("id=\"");
		sb.append(ssec.getFormDispatchId());
		sb.append("\" ");
		sb.append(subStrName);//the name
		if(ssec.isMultiSelect()){
			sb.append(" multiple ");
			sb.append(" size=\"3\" ");
		}
		//add ONCHANGE Action to select
		if(ssec.getAction() == FormEvent.ONCHANGE){
			sb.append(FormJSHelper.getRawJSFor(ssec.getRootForm(), ssec.getSelectionElementFormDisId(), ssec.getAction()));
		}
		
		sb.append(">");
		/*
		 * the options <option ...>value</option>
		 */
		int cnt = options.length;
		for (int i = 0; i < cnt; i++) {
			boolean selected = ssec.isSelected(i);
			sb.append("<option value=\"");
			sb.append(StringEscapeUtils.escapeHtml(options[i]));
			sb.append("\" ");
			if (selected) sb.append("selected=\"selected\" ");
			if(ssec.getAction() != FormEvent.ONCHANGE){
				//all other events go to the option
				sb.append(FormJSHelper.getRawJSFor(ssec.getRootForm(), ssec.getSelectionElementFormDisId(), ssec.getAction()));
			}
			if (cssClasses != null) {
				sb.append(" class=\"");
				sb.append(cssClasses[i]);
				sb.append("\"");
			}
			sb.append(">");
			sb.append(StringEscapeUtils.escapeHtml(values[i]));
			sb.append("</option>");
		}
		/*
		 * closing </select>
		 */
		sb.append("</select>");
	

		if(source.isEnabled()){
			//add set dirty form only if enabled
			sb.append(FormJSHelper.getJSStartWithVarDeclaration(ssec.getFormDispatchId()));
			sb.append(FormJSHelper.getSetFlexiFormDirty(ssec.getRootForm(), ssec.getFormDispatchId()));
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
