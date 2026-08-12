/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.factsheet;

/**
 *
 * Initial date: 12 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
class FactImpl implements Fact {

	private final String iconCss;
	private final String label;
	private final String value;
	private final String subValue;

	FactImpl(String iconCss, String label, String value, String subValue) {
		this.iconCss = iconCss;
		this.label = label;
		this.value = value;
		this.subValue = subValue;
	}

	@Override
	public String getIconCss() {
		return iconCss;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String getSubValue() {
		return subValue;
	}

}
