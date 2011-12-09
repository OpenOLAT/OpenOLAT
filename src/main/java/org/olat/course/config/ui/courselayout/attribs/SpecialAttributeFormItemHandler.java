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

import org.olat.core.gui.components.form.flexible.FormItem;

/**
 * 
 * Description:<br>
 * Interface to use when an Attribute has a complex getValue()
 * 
 * <P>
 * Initial Date:  17.02.2011 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public abstract class SpecialAttributeFormItemHandler {
	
	private FormItem formItem;

	protected SpecialAttributeFormItemHandler(FormItem formItem){
		this.formItem = formItem;
	}
	
	public abstract String getValue();
	
	protected FormItem getFormItem(){
		return formItem;
	}
	
	public boolean hasError(){
		return formItem.hasError();
	}

}