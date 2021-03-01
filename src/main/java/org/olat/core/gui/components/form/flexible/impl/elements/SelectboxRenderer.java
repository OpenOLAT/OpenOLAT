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
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * <P>
 * Initial Date:  02.01.2007 <br>
 * @author patrickb
 */
class SelectboxRenderer extends DefaultComponentRenderer {


	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {

		SelectboxComponent ssec = (SelectboxComponent)source;

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
		
		sb.append(" class='form-control'>");
		/*
		 * the options <option ...>value</option>
		 */
		int cnt = options.length;
		boolean escapeHtml = ssec.isEscapeHtml();
		for (int i = 0; i < cnt; i++) {
			if(SingleSelection.SEPARATOR.equals(options[i])) {
				sb.append("<option disabled>\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501</option>");
			} else {
				boolean selected = ssec.isSelected(options[i]);
				sb.append("<option value=\"").append(StringHelper.escapeHtml(options[i])).append("\" ");
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
				if(escapeHtml) {
					sb.append(StringEscapeUtils.escapeHtml(values[i]));
				} else {
					sb.append(values[i]);
				}
				sb.append("</option>");
			}
		}
		/*
		 * closing </select>
		 */
		sb.append("</select>");
	
		if(source.isEnabled()){
			//add set dirty form only if enabled
			FormJSHelper.appendFlexiFormDirty(sb, ssec.getRootForm(), ssec.getFormDispatchId());
		}
	}
}