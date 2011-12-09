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

import org.olat.core.gui.render.StringOutput;

/**
 * Helper class for having all rendering related constants
 * in one place with regards to rendering the 'Total' 
 * title and values in the statistics table.
 * <p>
 * Initial Date:  16.02.2010 <br>
 * @author Stefan
 */
public class TotalRendererHelper {

	/**
	 * Render the given value with any applicable pre/postfix into
	 * the given StringOutput
	 * @param sb where the resulting rendering should be appended
	 * @param value the value which should be rendered with pre/postfix if applicable
	 */
	public static void renderTotalValue(StringOutput sb, Integer value) {
		renderTotalValuePrefix(sb);
		sb.append(value);
		renderTotalValuePostfix(sb);
	}

	/**
	 * Render just the prefix to the total value if applicable
	 * @param sb where the resulting rendering should be appended
	 */
	public static void renderTotalValuePrefix(StringOutput sb) {
		sb.append("<i>");
	}

	/**
	 * Render just the postfix to the total value if applicable
	 * @param sb where the resulting rendering should be appended
	 */
	public static void renderTotalValuePostfix(StringOutput sb) {
		sb.append("</i>");
	}

	/**
	 * Render the given totalTitle with any applicable pre/postfix into
	 * the given StringOutput
	 * @param sb where the resulting rendering should be appended
	 * @param totalTitle the title which should be rendered into the StringOutput
	 */
	public static void renderTotalTitle(StringOutput sb, String totalTitle) {
		sb.append("<b><i>");
		sb.append(totalTitle);
		sb.append("</i></b>");
	}

}
