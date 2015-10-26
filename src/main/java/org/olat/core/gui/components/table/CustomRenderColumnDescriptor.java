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

import java.util.Locale;

import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class CustomRenderColumnDescriptor extends DefaultColumnDescriptor {
	private CustomCellRenderer customCellRenderer;

	/**
	 * @param headerKey
	 * @param dataColumn
	 * @param action
	 * @param locale
	 * @param alignment
	 * @param customCellRenderer
	 */
	public CustomRenderColumnDescriptor(final String headerKey, final int dataColumn, final String action, final Locale locale, final int alignment, final CustomCellRenderer customCellRenderer) {
		super(headerKey, dataColumn, action, locale, alignment);
		this.customCellRenderer = customCellRenderer;
	}

	/**
	 * @see org.olat.core.gui.components.table.DefaultColumnDescriptor#renderValue(org.olat.core.gui.render.StringOutput, int, org.olat.core.gui.render.Renderer)
	 */
	@Override
	public void renderValue(final StringOutput sb, final int row, final Renderer renderer) {
		Object val = getModelData(row);
		customCellRenderer.render(sb, renderer, val, getLocale(), getAlignment(), getAction(row));
	}
	
	public String toString(final int rowid) {
		StringOutput sb = new StringOutput();
		renderValue(sb,rowid,null);
		return sb.toString();
	}
	
	public CustomCellRenderer getCustomCellRenderer() {
		return customCellRenderer;
	}
}