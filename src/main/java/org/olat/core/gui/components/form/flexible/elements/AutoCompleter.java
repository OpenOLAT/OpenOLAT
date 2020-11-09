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
package org.olat.core.gui.components.form.flexible.elements;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.util.UserSession;

/**
 * 
 * Initial date: 20.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface AutoCompleter extends FormItem {
	
	public String getKey();
	
	public void setKey(String key);
	
	public int getMinLength();

	/**
	 * Set the minimal length to start a query server-side and
	 * show the auto-completion.
	 * 
	 * @param minLength
	 */
	public void setMinLength(int minLength);
	
	public String getValue();
	
	public void setValue(String value);

	public int getMaxEntries();
	
	public String getMapperUri();
	
	public void setListProvider(ListProvider provider, UserSession usess);

}
