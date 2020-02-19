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
package org.olat.modules.grading.model;

/**
 * 
 * Initial date: 14 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OlatResourceMapKey {
	
	private final String resName;
	private final Long resId;
	

	public OlatResourceMapKey(String resName, Long resId) {
		this.resId = resId;
		this.resName = resName;
	}
	
	public OlatResourceMapKey(ReferenceEntryWithStatistics statistics) {
		resName = statistics.getEntry().getOlatResource().getResourceableTypeName();
		resId = statistics.getEntry().getOlatResource().getResourceableId();
	}
	
	@Override
	public int hashCode() {
		return resId.hashCode() + resName.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof OlatResourceMapKey) {
			OlatResourceMapKey mapKey = (OlatResourceMapKey)obj;
			return resName != null && resName.equals(mapKey.resName)
					&& resId != null && resId.equals(mapKey.resId);
		}
		return false;
	}
}
