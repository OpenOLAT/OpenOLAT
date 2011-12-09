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
*/
package org.olat.course.statistic;

import java.util.Locale;

import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;

/**
 * This renderer is used by the StatisticDisplayController to render the 'Total' column 
 * (the last column). 
 * <p>
 * The idea is to render it bold and filter the boldiness out for the export, that's it.
 * <P>
 * Initial Date:  16.02.2010 <br>
 * @author Stefan
 */
public class TotalColumnRenderer implements CustomCellRenderer {

	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		if (val==null) {
			// don't render nulls
			return;
		}
		
		if (renderer==null) {
			// if no renderer is set, then we assume it's a table export - in which case we don't want the htmls
			sb.append(String.valueOf(val));
			return;
		}
		
		// this is the normal case
		TotalRendererHelper.renderTotalValue(sb, (Integer) val);
	}

}
