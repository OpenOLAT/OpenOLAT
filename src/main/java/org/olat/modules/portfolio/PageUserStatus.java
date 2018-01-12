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
package org.olat.modules.portfolio;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 11 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum PageUserStatus {
	
	incoming("status.user.incoming"),
	inProcess("status.user.inProcess"),
	done("status.user.done");
	
	private final String i18nKey;
	
	private PageUserStatus(String i18nKey) {
		this.i18nKey = i18nKey;
	}
	
	public String i18nKey() {
		return i18nKey;
	}
	
	public static final PageUserStatus valueOfWithDefault(String val) {
		if(StringHelper.containsNonWhitespace(val)) {
			return PageUserStatus.valueOf(val);
		}
		return PageUserStatus.incoming;
	}
}