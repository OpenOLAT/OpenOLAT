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
package org.olat.group.ui.main;

/**
 * A wrappe for the number of xx in a group. If the long == Long.MAX_VALUE, it
 * show infinite, if negative, it show -
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GroupNumber implements Comparable<GroupNumber>  {

	public static final GroupNumber INFINITE = new GroupNumber(Long.MAX_VALUE);
	public static final GroupNumber ZERO = new GroupNumber(0);
	public static final GroupNumber NONE = new GroupNumber(-1);
	
	private long count = -1;
	
	public GroupNumber(long count) {
		this.count = count;
	}

	@Override
	public int compareTo(GroupNumber o) {
		return (count<o.count ? -1 : (count==o.count ? 0 : 1));
	}

	@Override
	public String toString() {
		if(count < 0) {
			return "-";
		} else if(count == Long.MAX_VALUE) {
			return "&infin;";
		}
		return Long.toString(count);
	}
}
