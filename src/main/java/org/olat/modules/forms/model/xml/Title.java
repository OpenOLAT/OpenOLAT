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

import org.olat.modules.ceditor.model.TitleElement;

/**
 * 
 * Initial date: 7 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Title extends AbstractHTMLElement implements TitleElement {

	private static final long serialVersionUID = 1567753376804106600L;

	public static final String TYPE = "formhtitle";
	
	private String layoutOptions;
	
	@Override
	public String getLayoutOptions() {
		return layoutOptions;
	}

	@Override
	public void setLayoutOptions(String layoutOptions) {
		this.layoutOptions = layoutOptions;
	}
	
	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof Title) {
			Title title = (Title)obj;
			return getId() != null && getId().equals(title.getId());
		}
		return super.equals(obj);
	}
}
