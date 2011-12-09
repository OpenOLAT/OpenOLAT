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
package org.olat.course.config.ui.courselayout.attribs;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.util.StringHelper;


/**
 * 
 * Description:<br>
 * get and validate values from a color attribute (ColorLA) with two formItems
 * 
 * <P>
 * Initial Date:  17.02.2011 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
class ColorSpecialHandler extends SpecialAttributeFormItemHandler {
	
	private static final String HEX_PATTERN = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
	private boolean hasError = false;	
	
	ColorSpecialHandler(FormItem item) {
		super (item);		
	}
	
	@Override
	public String getValue(){
		FormLayoutContainer innerFLC = (FormLayoutContainer) getFormItem();
		Map<String, FormItem> items = innerFLC.getFormComponents();
		String ddValue = "";
		String inputValue = "";
		FormItem inputItem = null;
		for (Entry<String, FormItem> fiEntry : items.entrySet()) {
			String compName = fiEntry.getKey();
			FormItem fi = fiEntry.getValue();
			if (compName.endsWith("sel") && fi instanceof SingleSelection){
				ddValue = ((SingleSelection)fi).isOneSelected() ? ((SingleSelection)fi).getSelectedKey() : "";
			}
			if (compName.endsWith("value") && fi instanceof TextElement) {
				inputItem = fi;
				inputValue = ((TextElement)fi).getValue();
			}				
		}
		if (ddValue.equals("") && StringHelper.containsNonWhitespace(inputValue)){
			// use input-value if valid
			 Pattern pattern = Pattern.compile(HEX_PATTERN);
			 Matcher matcher = pattern.matcher(inputValue);
			 if (matcher.matches()) {
				 hasError = false;
				 return inputValue;
			 } else {
				 hasError = true;
				 inputItem.setErrorKey("color.hex.error", null);
				 return "";
			 }
		}	
		if (!ddValue.equals("") && StringHelper.containsNonWhitespace(inputValue)){
			inputItem.setErrorKey("color.double.error", null);
		}			
		return ddValue;
	}

	@Override
	public boolean hasError() {
		return hasError;
	}		
	
}