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

package org.olat.core.id.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * <h3>Description:</h3>
 * <p>
 * <p>
 * Initial Date:  18 jan. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class HistoryPointImpl implements HistoryPoint, Serializable {

	private static final long serialVersionUID = -7002881531372365733L;
	private final String uuid;
	private final String businessPath;
	private final List<ContextEntry> entries;
	
	public HistoryPointImpl(String uuid, String businessPath, List<ContextEntry> entries) {
		this.uuid = uuid;
		this.businessPath = businessPath;
		if(entries == null) {
			this.entries = Collections.emptyList();
		} else {
			this.entries = new ArrayList<>(entries.size());
			for(ContextEntry entry:entries) {
				this.entries.add(entry.clone());
			}
		}
	}
	

	public String getUuid() {
		return uuid;
	}

	public String getBusinessPath() {
		return businessPath;
	}

	public List<ContextEntry> getEntries() {
		return entries;
	}
	
	@Override
	public String toString() {
		return businessPath;
	}
}
