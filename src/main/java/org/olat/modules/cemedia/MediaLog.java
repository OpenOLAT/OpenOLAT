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
package org.olat.modules.cemedia;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 7 juil. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface MediaLog extends CreateInfo {
	
	public Long getKey();
	
	public Action getAction();
	
	public Identity getIdentity();
	
	public enum Action {
		ASSIGNMENT("log.action.assignment"),
		CREATED("log.action.created"),
		RECORDED("log.action.recorded"),
		COLLECTED("log.action.collected"),
		UPDATE("log.action.update"),
		UPLOAD("log.action.upload"),
		IMPORTED("log.action.imported"),
		VERSIONED("log.action.versioned");
		
		private final String i18nKey;
		
		private Action(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
}
