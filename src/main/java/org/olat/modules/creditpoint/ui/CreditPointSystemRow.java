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
package org.olat.modules.creditpoint.ui;

import java.util.Date;

import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointSystemStatus;
import org.olat.modules.creditpoint.model.CreditPointSystemInfos;
import org.olat.modules.creditpoint.model.CreditPointExpiration;

/**
 * 
 * Initial date: 3 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointSystemRow {
	
	private final CreditPointSystemInfos system;
	
	public CreditPointSystemRow(CreditPointSystemInfos system) {
		this.system = system;
	}
	
	public Long getKey() {
		return system.system().getKey();
	}

	public long getUsage() {
		return system.usage();
	}
	
	public String getName() {
		return system.system().getName();
	}
	
	public Date getCreationDate() {
		return system.system().getCreationDate();
	}
	
	public CreditPointExpiration getExpiration() {
		if(system.system().getDefaultExpiration() != null && system.system().getDefaultExpirationUnit() != null) {
			return new CreditPointExpiration(system.system().getDefaultExpiration(), system.system().getDefaultExpirationUnit());
		}
		return null;
	}

	public String getLabel() {
		return system.system().getLabel();
	}

	public CreditPointSystemStatus getStatus() {
		return system.system().getStatus();
	}

	public CreditPointSystem getSystem() {
		return system.system();
	}
}
