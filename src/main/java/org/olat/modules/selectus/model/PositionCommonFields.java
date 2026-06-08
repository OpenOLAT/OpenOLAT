/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.model;

import java.util.Locale;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 15.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface PositionCommonFields {

	public String getAvailableLanguages();

	public default String[] getAvailableLanguagesArray() {
		if(StringHelper.containsNonWhitespace(getAvailableLanguages())) {
			return getAvailableLanguages().split(",");
		}
		return new String[0];
	}
	
	public String getPositionTitle();
	
	public String getPositionTitleDe();
	
	public String getPositionTitleFr();
	
	public String getPositionTitle(Locale locale);
	
	public String getDepartment();
	
	public String getDepartmentDe();
	
	public String getDepartmentFr();
	
	public String getDepartment(Locale locale);

}
