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

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement.Layout;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 13.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MultipleSelectionRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		
		MultipleSelectionComponent stC = (MultipleSelectionComponent)source;
		MultipleSelectionElementImpl stF = stC.getMultipleSelectionElement();
		if(stF.getLayout() == Layout.vertical) {
			int columns = stC.getMultipleSelectionElement().getColumns();
			if(columns <= 1) {
				renderVertical(sb, stC);
			} else {
				renderMultiColumnsVertical(sb, stC, columns);
			}
		} else {
			renderHorizontal(sb, stC);
		}
	}
	
	private StringOutput appendIdIfRequired(StringOutput sb, MultipleSelectionComponent stC) {
		if(!stC.isDomReplacementWrapperRequired()) {
			sb.append(" id='").append(stC.getDispatchID()).append("'");
		}
		return sb;
	}
	
	private void renderVertical(StringOutput sb, MultipleSelectionComponent stC) {
		for(CheckboxElement check:stC.getCheckComponents()) {
			sb.append("<div ");
			appendIdIfRequired(sb, stC).append(" class='checkbox'>");
			renderCheckbox(sb, check, stC, false);
			sb.append("</div>");
		}
	}

	private void renderMultiColumnsVertical(StringOutput sb, MultipleSelectionComponent stC, int columns) {
		String columnCss;
		if(columns == 2) {
			columnCss = "col-sm-6";
		} else if(columns == 3) {
			columnCss = "col-sm-4";
		} else {
			columns = 4;
			columnCss = "col-sm-3";
		}

		sb.append("<div ");
		appendIdIfRequired(sb, stC).append(">");
		CheckboxElement[] checks = stC.getCheckComponents();
		for(int i=0; i<checks.length; ) {
			sb.append("<div class='row'>");
			
			for(int j=columns; j-->0; ) {
				if(i < checks.length) {
					CheckboxElement check = checks[i++];
					sb.append("<div class='").append(columnCss).append("'>");
					renderCheckbox(sb, check, stC, false);
					sb.append("</div>");
				}
			}

			sb.append("</div>");
		}
		sb.append("</div>");
	}
	
	private void renderHorizontal(StringOutput sb, MultipleSelectionComponent stC) {
		sb.append("<div ");
		appendIdIfRequired(sb, stC).append(" class='form-inline'>");
		for(CheckboxElement check:stC.getCheckComponents()) {
			renderCheckbox(sb, check, stC, true);
		}
		sb.append("</div>");
	}
	
	private void renderCheckbox(StringOutput sb, CheckboxElement check, MultipleSelectionComponent stC, boolean inline) {
		MultipleSelectionElementImpl stF = stC.getMultipleSelectionElement();

		String subStrName = "name='" + check.getGroupingName() + "'";
			
		String key = check.getKey();
		String value = check.getValue();
		if(stF.isEscapeHtml()){
			key = StringEscapeUtils.escapeHtml(key);
			value = StringEscapeUtils.escapeHtml(value);
		}
			
		boolean selected = check.isSelected();
		String formDispatchId = check.getFormDispatchId();
			
		//read write view
		String cssClass = check.getCssClass(); //optional CSS class
		sb.append("<div>", !inline); // normal checkboxes need a wrapper (bootstrap) ...
		sb.append("<label class='").append("checkbox-inline ", inline); // ... and inline a class on the label (bootstrap)			
		sb.append(cssClass, cssClass != null).append("' for=\"").append(formDispatchId).append("\">");
		
		
		sb.append("<input type='checkbox' id='").append(formDispatchId).append("' ")
		  .append(subStrName)
		  .append(" value='").append(key).append("'");
		if (selected) {
			sb.append(" checked='checked' ");
		}
		if(!stC.isEnabled() || !check.isEnabled()) {
			sb.append(" disabled='disabled' ");
		} else if(stF.isAjaxOnly()) {
			// The implementation is conservative as it send the state of the check box,
			// this is especially useful if an issue of double evaluation appears.
			sb.append(" onclick=\"javascript: this.checked ?")
		      .append(FormJSHelper.getXHRFnCallFor(stF.getRootForm(), stC.getFormDispatchId(), 1, false, false, false,
		    		  new NameValuePair("achkbox", key), new NameValuePair("checked", "true")))
		      .append(" : ")
		      .append(FormJSHelper.getXHRFnCallFor(stF.getRootForm(), stC.getFormDispatchId(), 1, false, false, false,
		    		  new NameValuePair("achkbox", key), new NameValuePair("checked", "false")))
			  .append(";\"");
		} else {
			//use the selection form dispatch id and not the one of the element!
			sb.append(FormJSHelper.getRawJSFor(check.getRootForm(), check.getSelectionElementFormDispatchId(), check.getAction()));
		}
		sb.append(" />");
		String iconLeftCSS = check.getIconLeftCSS();
		if (StringHelper.containsNonWhitespace(iconLeftCSS)) {
			sb.append(" <i class='").append(iconLeftCSS).append("'> </i> ");
		}
		if (StringHelper.containsNonWhitespace(value)) {
			sb.append(" ").append(value);		
		} else if(inline) {
			// at least something in label required for properly aligned rendering, nbsp is important for bootstrap
			sb.append("&nbsp;"); 
		}
		sb.append("</label>");
		sb.append("</div>", !inline); // normal radios need a wrapper (bootstrap)
			
		if(stC.isEnabled()){
			//add set dirty form only if enabled
			FormJSHelper.appendFlexiFormDirtyForCheckbox(sb, stF.getRootForm(), formDispatchId);
		}
	}
}