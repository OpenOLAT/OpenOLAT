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
package org.olat.portfolio.model.structel;

/**
 * Initial Date:  11.06.2010 <br>
 * @author rhaag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPDefaultMap extends EPAbstractMap {

	private static final long serialVersionUID = 5327020967451630707L;

	public EPDefaultMap() {
		//
	}

	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof EPDefaultMap) {
			return equalsByPersistableKey((EPDefaultMap)obj);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? -9544 : getKey().hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("epDefaultMap[key=").append(getKey()).append(":")
		  .append("title=").append(getTitle()).append("]");
		return sb.toString();
	}
}
