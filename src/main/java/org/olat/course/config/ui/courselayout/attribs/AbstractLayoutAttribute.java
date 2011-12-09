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

import java.util.Arrays;
import java.util.List;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.StringHelper;


/**
 * Description:<br>
 * contains common stuff for an css-elements attribute
 * give the attribute its name by attributeKey, set all available keys & values
 * if used by elements children, the relative keys must be set
 * getFormItem can be overwritten to return another component than a drop-down
 * 
 * <P>
 * Initial Date:  03.02.2011 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public abstract class AbstractLayoutAttribute {

	private String attributeKey;
	private String attributeValue = null;
	private String[] relativeKeys;
	private String[] availKeys;
	private String[] availValues;
	private String[] availCSS;
	
	/**
	 * @param availKeys The availKeys to set.
	 */
	protected void setAvailKeys(String[] availKeys) {
		this.availKeys = ArrayHelper.addToArray(availKeys, "", false);
	}
	
	/**
	 * @param availValues The availValues to set.
	 */
	protected void setAvailValues(String[] availValues) {
		this.availValues = ArrayHelper.addToArray(availValues, "--", false);
	}
	
	/**
	 * @param availCSS The availCSS to set.
	 */
	protected void setAvailCSS(String[] availCSS) {
		this.availCSS = ArrayHelper.addToArray(availCSS, "", false);
	}
	
	/**
	 * @param relativeKeys The relativeKeys to set.
	 */
	protected void setRelativeKeys(String[] relativeKeys) {
		this.relativeKeys = relativeKeys;
	}

	// as per default attaches a dropdown and selects what is given
	public FormItem getFormItem(String compName, FormItemContainer formLayout){
		FormUIFactory uifact = FormUIFactory.getInstance();

		SingleSelection fi = uifact.addDropdownSingleselect(compName, null, formLayout, availKeys, availValues, availCSS);
		if (attributeValue!=null && Arrays.asList(availKeys).contains(attributeValue)){
			fi.select(attributeValue, true);
			fi.showLabel(false);
		} 
		return fi;		
	}
	

	public String getRelativeCompiledAttribute(int rel){
		if (StringHelper.containsNonWhitespace(getAttributeKey())) {
			return getAttributeKey() + ": " + getRelativeValue(getAttributeValue(), rel) + "; \n";
		} else {
			return "";
		}
	}
	
	/**
	 * @param attributeKey The attributeKey to set.
	 */
	public void setAttributeKey(String attributeKey) {
		this.attributeKey = attributeKey;
	}

	/**
	 * @return Returns the attributeKey.
	 */
	public String getAttributeKey() {
		return attributeKey;
	}
	
	/**
	 * @param attributeValue The attributeValue to set.
	 */
	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}
	
	/**
	 * @return Returns the attributeValue.
	 */
	public String getAttributeValue() {
		return attributeValue;
	}

	public abstract String getLayoutAttributeTypeName();
	
	/**
	 * get value relative to choosen attribute value.
	 * @param value
	 * @param rel
	 * @return the <rel> higher or lower value if any were set in attribute config
	 */
	public String getRelativeValue(String value, int rel){
		List<String> keyL = Arrays.asList(availKeys);
		if(keyL.contains(value) && relativeKeys != null){
			int pos = keyL.indexOf(value);
			int relpos = (pos + rel); 
			if (relpos >= 0 && relpos < relativeKeys.length) {
				return relativeKeys[relpos];
			} else if (relpos < 0){
				return relativeKeys[0];
			} else if (relpos == relativeKeys.length){
				return relativeKeys[relativeKeys.length-1];
			}
		} 
		return value;		
	}	
}
