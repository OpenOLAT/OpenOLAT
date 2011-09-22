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

package org.olat.core.gui.formelements;

import org.olat.core.gui.translator.Translator;

/**
 * @author Felix Jost
 */
public interface FormElement {
	/**
	 * @param name
	 */
	public void setName(String name);

	/**
	 * @return
	 */
	public String getName();

	/**
	 * Set a translated error message.
	 * @param error
	 */
	public void setError(String error);
	
	/**
	 * Set an error translation key.
	 * @param errorKey
	 */
	public void setErrorKey(String errorKey);
	
	/**
	 * Error translation key with parameters to be set in the translation.
	 * 
	 * @param errorKey
	 * @param params
	 */
	public void setErrorKeyWithParams(String errorKey, String[] params);
	
	/**
	 * @return a translated error message
	 */
	public String getError(Translator translator);

	/**
	 * @return true if an error or error key was set. 
	 */
	public boolean isError();
	

	/**
	 * Clear the error message. After that <code>isError()</code> returns
	 * <code>false</code>.
	 */
	public void clearError();

	/**
	 * // if the parameter is missing (values == null), ignore (this may be if a
	 * formelement is readonly e.g.) H: formelement not readonly
	 * 
	 * @param values
	 */
	public void setValues(String[] values);

	/**
	 * @return
	 */
	public String getLabelKey();

	/**
	 * @param labelKey
	 */
	public void setLabelKey(String labelKey);

	/**
	 * @return
	 */
	public boolean isReadOnly();

	/**
	 * @param readOnly
	 */
	public void setReadOnly(boolean readOnly);

	/**
	 * @return
	 */
	public boolean isMandatory();

	/**
	 * @param mandatory
	 */
	public void setMandatory(boolean mandatory);

	/**
	 * @return
	 */
	public String getExample();

	/**
	 * @return
	 */
	public boolean isDirty();
	
	void setVisualMarked(boolean on);
	boolean getVisualMarked();

}