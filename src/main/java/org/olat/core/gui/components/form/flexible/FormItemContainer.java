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
package org.olat.core.gui.components.form.flexible;

import java.util.Map;

/**
 * Description:<br>
 * FormContainer and FormComponent -> Composite Pattern
 * Implementors of FormContainer should also extend the olat Container and 
 * override its put(.., Component) methods by throwing an exception -> use
 * add instead.<br>
 * <P>
 * Initial Date: 24.11.2006 <br>
 * 
 * @author patrickb
 */
public interface FormItemContainer extends FormItem, FormItemCollection {
	
	public boolean isDomReplacementWrapperRequired();

	/**
	 * add a formelement or container by adding subcomponents
	 * <ul>
	 * <li>name_LABEL</li>
	 * <li>name_ERROR</li>
	 * <li>name_EXAMPLE</li>
	 * </ul>
	 * 
	 * @param name
	 * @param formComp
	 */
	public void add(FormItem formComp);
	
	/**
	 * add with different name
	 * @param name
	 * @param formComp
	 */
	public void add(String name, FormItem formComp);
	
	/**
	 * remove the component from this container
	 * @param formComp
	 */
	public void remove(FormItem formComp);

	/**
	 * remove the component with the give name from this container	  
	 * @param name
	 */
	public void remove(String name);

	/**
	 * the form components managed by this container
	 * @return Return an unmodifiable map
	 */
	public Map<String, FormItem> getFormComponents();
	
	/**
	 * @return True if the container manages this item.
	 */
	public boolean hasFormComponent(FormItem item);
	
}
