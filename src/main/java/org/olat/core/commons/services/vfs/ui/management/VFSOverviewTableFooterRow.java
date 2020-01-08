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
package org.olat.core.commons.services.vfs.ui.management;

/**
 * 
 * Initial date: 23 Dec 2019<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 *
 */
public class VFSOverviewTableFooterRow {
	private final String name;
	private final Long size;
	private final Long amount;
	private final String action;
	
	public VFSOverviewTableFooterRow(String name, Long amount, Long size, String action) {
		this.name = name;
		this.amount = amount;
		this.size = size;
		this.action = action;
	}

	public String getName() {
		return name;
	}

	public Long getSize() {
		return size;
	}

	public Long getAmount() {
		return amount;
	}

	public String getAction() {
		return action;
	}
}
