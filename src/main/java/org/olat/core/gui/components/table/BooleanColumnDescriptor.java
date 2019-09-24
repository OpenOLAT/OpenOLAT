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
import org.olat.core.util.StringHelper;

/**
 * @author Felix Jost
 */
public class BooleanColumnDescriptor extends DefaultColumnDescriptor {

	private String falseValue;
	private String trueValue;
	private boolean hasAction;
	private boolean sortingAllowed = true;

	/**
	 * constructor for the columndescriptor with no action (column is not
	 * clickable)
	 * 
	 * @param headerKey Key for internationalizable header title
	 * @param datacolumn position in table
	 * @param trueValue Text string if result is true
	 * @param falseValue Text string if result is false
	 */
	public BooleanColumnDescriptor(final String headerKey, final int datacolumn, final String trueValue, final String falseValue) {
		this(headerKey, datacolumn, null, trueValue, falseValue);
	}

	/**
	 * @param headerKey Key for internationalizable header title
	 * @param datacolumn position in table
	 * @param action action for true value (rendered as link)
	 * @param trueValue Text string if result is true
	 * @param falseValue Text string if result is false
	 */

	public BooleanColumnDescriptor(final String headerKey, final int datacolumn, final String action, final String trueValue, final String falseValue) {
		super(headerKey, datacolumn, action, null);
		this.trueValue = trueValue;
		this.falseValue = falseValue;
		this.hasAction = (action != null);
	}

	/**
	 * @see org.olat.core.gui.components.table.ColumnDescriptor#renderValue(org.olat.core.gui.render.StringOutput,
	 *      int, org.olat.core.gui.render.Renderer)
	 */
	@Override
	public void renderValue(final StringOutput sb, final int row, final Renderer renderer) {
		Object obj = getModelData(row);
		if(obj instanceof Boolean) {
			Boolean bool = (Boolean)obj;
			String val = (bool.booleanValue() ? trueValue : falseValue);
			if(StringHelper.containsNonWhitespace(val)) {
				sb.append(val);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.components.table.ColumnDescriptor#getAction(int)
	 */
	@Override
	public String getAction(final int row) {
		if (!hasAction) {
			return null;
		} else {
			Object bool = getModelData(row);
			// make sure the bool is not null before checkings its value
			if (bool instanceof Boolean && ((Boolean)bool).booleanValue()) {
				return super.getAction(row);
			} else {
				return null;
			}
		}
	}

	public String toString(final int rowid) {
		StringOutput sb = new StringOutput();
		renderValue(sb,rowid,null);
		return sb.toString();
	}

	@Override
	public boolean isSortingAllowed() {
		return this.sortingAllowed;
	}
	
	/**
	 * allow column sorting or not, default is true
	 * @param sortingAllowed
	 */
	public void setSortingAllowed(final boolean sortingAllowed) {
		this.sortingAllowed = sortingAllowed;
	}
	
	
}