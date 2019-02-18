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
package org.olat.modules.forms.model.xml;

import org.olat.modules.ceditor.model.TableElement;

/**
 * 
 * Initial date: 18 Feb 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class Table extends AbstractHTMLElement implements TableElement {

	private static final long serialVersionUID = 1567753376804106123L;

	public static final String TYPE = "formtable";
	
	private String layoutOptions;

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getLayoutOptions() {
		return layoutOptions;
	}

	@Override
	public void setLayoutOptions(String layoutOptions) {
		this.layoutOptions = layoutOptions;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof Table) {
			Table table = (Table)obj;
			return getId() != null && getId().equals(table.getId());
		}
		return super.equals(obj);
	}

}
