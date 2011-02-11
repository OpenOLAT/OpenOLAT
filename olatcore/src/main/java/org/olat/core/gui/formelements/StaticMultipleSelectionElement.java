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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;

/**
 * @author Felix Jost
 */
public class StaticMultipleSelectionElement extends AbstractFormElement implements MultipleSelectionElement {
	private String[] values;
	private String[] keys;
	private Set selected;
	private boolean enableCheckAll;


	/**
	 * @param labelKey
	 * @param keys
	 * @param values
	 * @param enableCheckAll
	 */
	public StaticMultipleSelectionElement(String labelKey, String[] keys, String[] values, boolean enableCheckAll) {
		this.keys = keys;
		this.values = values;
		this.enableCheckAll = enableCheckAll;
		setLabelKey(labelKey);
		selected = new HashSet();
	}

	/**
	 * @see org.olat.core.gui.formelements.SelectionElement#getKey(int)
	 */
	public String getKey(int which) {
		return keys[which];
	}

	/**
	 * @see org.olat.core.gui.formelements.SelectionElement#getValue(int)
	 */
	public String getValue(int which) {
		return values[which];
	}

	/**
	 * @see org.olat.core.gui.formelements.SelectionElement#getSize()
	 */
	public int getSize() {
		return keys.length;
	}

	/**
	 * @see org.olat.core.gui.formelements.SelectionElement#isSelected(int)
	 */
	public boolean isSelected(int which) {
		String key = getKey(which);
		return selected.contains(key);
	}

	/**
	 * input: keys of selected checkboxes
	 * 
	 * @see org.olat.core.gui.formelements.FormElement#setValues(java.lang.String[])
	 */
	public void setValues(String[] values) {
		selected = new HashSet(3);
		if (values == null) return; // no selection made (no checkbox activated) ->
		// selection is empty
		// H: values != null
		for (int i = 0; i < values.length; i++) {
			String key = values[i];
			// prevent introducing fake keys
			int ksi = keys.length;
			boolean foundKey = false;
			int j = 0;
			while (!foundKey && j < ksi) {
				String eKey = keys[j];
				if (eKey.equals(key)) foundKey = true;
				j++;
			}
			if (!foundKey) throw new AssertException("submitted key '"+key+"' was not found in the keys of formelement named "+this.getName()+" , keys="+Arrays.asList(keys));
			selected.add(key);
		}
	}

	/**
	 * @see org.olat.core.gui.formelements.MultipleSelectionElement#getSelectedKeys()
	 */
	public Set getSelectedKeys() {
		return selected;
	}

	/**
	 * @see org.olat.core.gui.formelements.MultipleSelectionElement#isAtLeastSelected(int,
	 *      java.lang.String)
	 */
	public boolean isAtLeastSelected(int howmany, String errorKey) {
		boolean ok = selected.size() >= howmany;
		if (!ok) setErrorKey(errorKey);
		return ok;
	}

	/**
	 * @see org.olat.core.gui.formelements.SelectionElement#select(java.lang.String,
	 *      boolean)
	 */
	public void select(String key, boolean select) {
		if (select) {
			selected.add(key);
		} else {
			selected.remove(key);
		}
	}

	/**
	 * @see org.olat.core.gui.formelements.FormElement#isDirty()
	 */
	public boolean isDirty() {
		throw new OLATRuntimeException(StaticMultipleSelectionElement.class, "isDirty not implemented for StaticMultipleSelectionElement", null);
	}

	/**
	 * @see org.olat.core.gui.formelements.MultipleSelectionElement#enableCheckAll()
	 */
	public boolean enableCheckAll() {
		return enableCheckAll;
	}
	
}