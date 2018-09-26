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
package org.olat.modules.quality.analysis;

/**
 * 
 * Initial date: 24.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MultiGroupBy {
	
	private final GroupBy groupBy1;
	private final GroupBy groupBy2;
	private final GroupBy groupBy3;
	
	public static MultiGroupBy noGroupBy() {
		return of(null);
	}
	
	public static MultiGroupBy of(GroupBy groupBy1) {
		return of(groupBy1, null);
	}
	
	public static MultiGroupBy of(GroupBy groupBy1, GroupBy groupBy2) {
		return of(groupBy1, groupBy2, null);
	}
	
	public static MultiGroupBy of(GroupBy groupBy1, GroupBy groupBy2, GroupBy groupBy3) {
		return new MultiGroupBy(groupBy1, groupBy2, groupBy3);
	}
	
	private MultiGroupBy(GroupBy groupBy1, GroupBy groupBy2, GroupBy groupBy3) {
		this.groupBy1 = groupBy1;
		this.groupBy2 = groupBy2;
		this.groupBy3 = groupBy3;
	}

	public GroupBy getGroupBy1() {
		return groupBy1;
	}

	public GroupBy getGroupBy2() {
		return groupBy2;
	}

	public GroupBy getGroupBy3() {
		return groupBy3;
	}
	
	public boolean isNoGroupBy() {
		return groupBy1 == null && groupBy2 == null && groupBy3 == null;
	}

}
