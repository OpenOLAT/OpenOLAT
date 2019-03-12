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

import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;

/**
 * Description: <BR>
 * TODO: Class Description for ColumnDescriptor
 * <P>
 * 
 * @author Felix Jost
 */
public interface ColumnDescriptor {

	/**
	 * means text output of this column should be aligned to the right
	 */
	static final int ALIGNMENT_RIGHT = 0;

	/**
	 * means text output of this column should be centered
	 */
	static final int ALIGNMENT_CENTER = 1;

	/**
	 * means text output of this column should be aligned to the left (default)
	 */
	static final int ALIGNMENT_LEFT = 2;

	/**
	 * @return the key of the header of this column. Is translated by the
	 *         translator of the table to which this columndescriptor belongs to.
	 */
	String getHeaderKey();
	
	/**
	 * @return <code>true</code> if the header key should be translated by the
	 * renderer, otherwise <code>false</code>
	 */
	boolean translateHeaderKey();
	
	/**
	 * Return the index used to retrieve the value in the table model
	 * @return
	 */
	public int getDataColumn();

	/**
	 * @return
	 */
	int getAlignment();
	
	int getHeaderAlignment();

	/**
	 * @param sb
	 * @param row
	 * @param renderer the Renderer. if null this means that the renderValue
	 *          should be in plain text (e.g. for excel download)
	 */
	void renderValue(StringOutput sb, int row, Renderer renderer);

	/**
	 * @param rowa
	 * @param rowb
	 * @return
	 */
	int compareTo(int rowa, int rowb);

	/**
	 * @param table the backreference to the table
	 */
	void setTable(Table table);

	/**
	 * gets the action code for the column, null if no action
	 * 
	 * @param row the current row
	 * @return
	 */
	String getAction(int row);

	/**
	 * called by the table if the model has changed. Useful in combination with
	 * sortingAboutToStart() to know when sortingAboutToStart() can use the cache
	 * or when it has to resort again
	 */
	void modelChanged();

	/**
	 * called before the actual sorting calls with compareTo(int rowa, int rowb)
	 * take place
	 */
	void sortingAboutToStart();

	/**
	 * called when this columnDescriptor is not being sorted by the user, but
	 * another columnDescriptor. usage: e.g. so getRenderValue(int row) can
	 * deliver different results w/o being active (formatting or such)
	 */
	void otherColumnDescriptorSorted();

	/**
	 * @return true if this column should offer sorting (by clicking on the column
	 *         header)
	 */
	boolean isSortingAllowed();

	/**
	 * @return true if the action link should open in a new window using java
	 *         script
	 */
	boolean isPopUpWindowAction();

	/**
	 * @return javascript window.open attributes or null if browsers default
	 *         should be used
	 */
	String getPopUpWindowAttributes();


}