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

package org.olat.core.gui.components.form.flexible.elements;



/**
 * @author patrickb
 */
public interface SingleSelection extends SelectionElement {
	
	public String NO_SELECTION_KEY = "noSelectionKey";

	public String[] getKeys();
	
	public boolean containsKey(String value);
	
	public String[] getValues();

	public String getSelectedKey();
	
	public String getSelectedValue();

	public boolean isOneSelected();

	public int getSelected();
	
	/**
	 * Set a fix width to the enclosing div/label of the radio elements. Spaced
	 * had a space after the end div/label.
	 * 
	 * @param widthInPercent The width (example: 9)
	 * @param spaced If true had a trailing space
	 */
	public void setWidthInPercent(int width, boolean trailingSpace);
	
	public boolean isAllowNoSelection();
	
	/**
	 * If true, it disables the validation and exception sent if the element
	 * is not selected.
	 * 
	 * @param allowNoSelection
	 */
	public void setAllowNoSelection(boolean allowNoSelection);
	
	public void enableNoneSelection();
	
	/**
	 * Enable the no selection element.
	 * 
	 * @param translatedValue The label for the not selected element.
	 */
	public void enableNoneSelection(String translatedValue);
	
	/**
	 * Remove the no selection element.
	 */
	public void disableNoneSelection();
	
	public boolean isEscapeHtml();
	
	public void setEscapeHtml(boolean escape);

	
	/**
	 * Set new keys and values in this selection box. Be aware that this does
	 * reset the selection index and other parameters. <br />
	 * In most cases is is better to create a new SingleSelection Element than
	 * set new keys and values for an existing SingleSelection, always check
	 * this option.
	 * 
	 * @param keys
	 *            The new keys to use
	 * @param values
	 *            The new values to use
	 * @param cssClasses
	 *            The CSS classes that should be used in the form element for
	 *            each key-value pair or NULL not not use special styling
	 */
	public void setKeysAndValues(String[] keys, String[] values,String[] cssClasses);
	
	public enum Layout {
		vertical,
		horizontal
	}
}