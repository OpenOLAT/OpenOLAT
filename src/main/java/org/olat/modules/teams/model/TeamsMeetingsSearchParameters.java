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
package org.olat.modules.teams.model;

/**
 * 
 * Initial date: 2 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsMeetingsSearchParameters {
	
	private String searchString;
	private OrderBy order;
	private boolean orderAsc;

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public OrderBy getOrder() {
		return order;
	}

	public void setOrder(OrderBy order) {
		this.order = order;
	}

	public boolean isOrderAsc() {
		return orderAsc;
	}

	public void setOrderAsc(boolean orderAsc) {
		this.orderAsc = orderAsc;
	}

	public enum OrderBy {
		subject,
		startDate,
		endDate,
		context;
		
		public static final OrderBy secureValueOf(String val) {
			for(OrderBy order:values()) {
				if(order.name().equals(val)) {
					return order;
				}
			}
			return null;
		}
	}

}
