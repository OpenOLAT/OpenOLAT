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
package org.olat.restapi.audit;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Filters of the audit log view.
 * 
 * Initial date: 16 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ApiAuditLogSearchParams {
	
	private Date createdAfter;
	private Date createdBefore;
	private Long identityKey;
	private String userSearch;
	private List<ApiAuditChannel> channels;
	private List<String> methods;
	private String resourceClass;
	private List<ApiAuditStatusClass> statusClasses;
	private OrderBy order;
	private boolean orderAsc;
	
	public Date getCreatedAfter() {
		return createdAfter;
	}
	
	public void setCreatedAfter(Date createdAfter) {
		this.createdAfter = createdAfter;
	}
	
	public Date getCreatedBefore() {
		return createdBefore;
	}
	
	public void setCreatedBefore(Date createdBefore) {
		this.createdBefore = createdBefore;
	}
	
	public Long getIdentityKey() {
		return identityKey;
	}
	
	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}
	
	/**
	 * @return a free text searched in the user name, the nick name, the email and the login attempt
	 */
	public String getUserSearch() {
		return userSearch;
	}
	
	public void setUserSearch(String userSearch) {
		this.userSearch = userSearch;
	}
	
	public List<ApiAuditChannel> getChannels() {
		return channels;
	}
	
	public void setChannels(List<ApiAuditChannel> channels) {
		this.channels = channels;
	}
	
	public List<String> getMethods() {
		return methods;
	}
	
	public void setMethods(List<String> methods) {
		this.methods = methods;
	}
	
	public String getResourceClass() {
		return resourceClass;
	}
	
	public void setResourceClass(String resourceClass) {
		this.resourceClass = resourceClass;
	}
	
	public List<ApiAuditStatusClass> getStatusClasses() {
		return statusClasses;
	}
	
	public void setStatusClasses(List<ApiAuditStatusClass> statusClasses) {
		this.statusClasses = statusClasses;
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
		creationDate,
		channel,
		identity,
		authProvider,
		ip,
		method,
		path,
		resourceClass,
		resourceMethod,
		status,
		durationMs;
		
		private static final Map<String, OrderBy> secureValues = List.of(values()).stream()
				.collect(Collectors.toMap(OrderBy::name, Function.identity()));
		
		public static final OrderBy secureValueOf(String val) {
			return secureValues.getOrDefault(val, null);
		}
	}
}
