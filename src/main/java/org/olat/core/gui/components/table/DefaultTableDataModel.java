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

package org.olat.core.gui.components.table;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Locale;

import org.olat.core.logging.AssertException;

/**
 * enclosing_type Description: <br>
 * 
 * @author Felix Jost
 */
public abstract class DefaultTableDataModel<U> implements TableDataModel<U> {
	private Locale locale;
	protected List<U> objects;

	/**
	 * @param objects
	 */
	public DefaultTableDataModel(final List<U> objects) {
		this.objects = objects;
	}
	
	

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	@Override
	public abstract int getColumnCount();

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return objects == null ? 0 : objects.size();
	}
	
	public boolean isSelectable(@SuppressWarnings("unused") int row) {
		return true;
	}
	
	public boolean isRowLoaded(int row) {
		if(objects == null) return false;
		return row < objects.size();
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	@Override
	public abstract Object getValueAt(int row, int col);

	/**
	 * Sets the objects.
	 * 
	 * @param objects The objects to set
	 */
	@Override
	public void setObjects(List<U> objects) {
		this.objects = objects;
	}

	/**
	 * @param row
	 * @return
	 */
	@Override
	public U getObject(final int row) {
		return objects.get(row);
	}
	

	/**
	 * Return the objects as marked in the BitSet.
	 * Each bit in the BitSet corresponds to a row in 
	 * the table data model.
	 * 
	 * Use this method to retreive all selected objects
	 * from a table with multiselect enabled.
	 * 
	 * @param objectMarkers
	 * @return
	 */
	public List<U> getObjects(final BitSet objectMarkers) {
		List<U> results = new ArrayList<>();
		for(int i=objectMarkers.nextSetBit(0); i >= 0; i=objectMarkers.nextSetBit(i+1)) {
			results.add(getObject(i));
		}
		return results;
	}
	
	/**
	 * @return Locale
	 */
	protected Locale getLocale() {
		return locale;
	}

	/**
	 * Sets the locale.
	 * 
	 * @param locale The locale to set
	 */
	public void setLocale(final Locale locale) {
		this.locale = locale;
	}

	/**
	 * @return List
	 */
	public List<U> getObjects() {
		return objects;
	}

	@Override
	public Object createCopyWithEmptyList() {
		throw new AssertException("createCopyWithEmptyList not implemented for model:" + this.getClass().getName()); 
	}
}