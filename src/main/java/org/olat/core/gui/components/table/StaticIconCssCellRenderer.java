/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.table;

/**
 * 
 * Initial date: 7 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class StaticIconCssCellRenderer extends IconCssCellRenderer {
	
	private final String cssClass;
	private final String value;
	private final String hoverText;
	
	public StaticIconCssCellRenderer(String cssClass) {
		this(cssClass, null, null);
	}

	public StaticIconCssCellRenderer(String cssClass, String value, String hoverText) {
		this.cssClass = cssClass;
		this.value = value;
		this.hoverText = hoverText;
	}

	@Override
	protected String getIconCssClass(Object val) {
		return cssClass;
	}

	@Override
	protected String getCellValue(Object val) {
		return value;
	}

	@Override
	protected String getHoverText(Object val) {
		return hoverText;
	}

}
