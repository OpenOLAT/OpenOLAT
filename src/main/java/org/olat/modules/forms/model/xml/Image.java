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

import org.olat.modules.ceditor.model.ImageElement;
import org.olat.modules.ceditor.model.StoredData;

/**
 * 
 * Initial date: 21 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Image extends AbstractHTMLElement implements ImageElement {

	private static final long serialVersionUID = 3536645850664794909L;

	public static final String TYPE = "formimage";
	
	private String layoutOptions;
	private StoredData storedData;
	
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
	public StoredData getStoredData() {
		return storedData;
	}
	
	public void setStoredData(StoredData data) {
		storedData = data;
	}
	
	

}
