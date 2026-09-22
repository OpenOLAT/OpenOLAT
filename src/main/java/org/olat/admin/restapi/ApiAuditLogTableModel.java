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
package org.olat.admin.restapi;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataSourceModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.restapi.audit.ApiAuditLog;

/**
 * Initial date: 17 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ApiAuditLogTableModel extends DefaultFlexiTableDataSourceModel<ApiAuditLog> {
	
	private static final ApiAuditLogCols[] COLS = ApiAuditLogCols.values();
	
	public ApiAuditLogTableModel(FlexiTableDataSourceDelegate<ApiAuditLog> dataSource, FlexiTableColumnModel columnModel) {
		super(dataSource, columnModel);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		ApiAuditLog auditLog = getObject(row);
		return switch(COLS[col]) {
			case creationDate -> auditLog.getCreationDate();
			case channel -> auditLog.getChannel();
			case identity -> auditLog.getIdentity();
			case loginAttempt -> auditLog.getLoginAttempt();
			case authProvider -> auditLog.getAuthProvider();
			case ip -> auditLog.getIp();
			case userAgent -> auditLog.getUserAgent();
			case method -> auditLog.getMethod();
			case path -> auditLog.getPath();
			case query -> auditLog.getQuery();
			case resourceClass -> auditLog.getResourceClass();
			case resourceMethod -> auditLog.getResourceMethod();
			case pathParams -> auditLog.getPathParams();
			case status -> Integer.valueOf(auditLog.getStatus());
			case durationMs -> auditLog.getDurationMs();
			case requestBody -> auditLog.getRequestBody();
			case ref -> auditLog.getRef();
			case nodeId -> auditLog.getNodeId();
		};
	}
	
	@Override
	public ApiAuditLogTableModel createCopyWithEmptyList() {
		return new ApiAuditLogTableModel(getSourceDelegate(), getTableColumnModel());
	}
	
	public enum ApiAuditLogCols implements FlexiSortableColumnDef {
		creationDate("auditlog.col.creationDate", "creationDate"),
		channel("auditlog.col.channel", "channel"),
		identity("auditlog.col.identity", "identity"),
		loginAttempt("auditlog.col.loginAttempt", null),
		authProvider("auditlog.col.authProvider", "authProvider"),
		ip("auditlog.col.ip", "ip"),
		userAgent("auditlog.col.userAgent", null),
		method("auditlog.col.method", "method"),
		path("auditlog.col.path", "path"),
		query("auditlog.col.query", null),
		resourceClass("auditlog.col.resourceClass", "resourceClass"),
		resourceMethod("auditlog.col.resourceMethod", "resourceMethod"),
		pathParams("auditlog.col.pathParams", null),
		status("auditlog.col.status", "status"),
		durationMs("auditlog.col.durationMs", "durationMs"),
		requestBody("auditlog.col.requestBody", null),
		ref("auditlog.col.ref", null),
		nodeId("auditlog.col.nodeId", null);
		
		private final String i18nKey;
		private final String sortKey;
		
		ApiAuditLogCols(String i18nKey, String sortKey) {
			this.i18nKey = i18nKey;
			this.sortKey = sortKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
		
		@Override
		public boolean sortable() {
			return sortKey != null;
		}
		
		@Override
		public String sortKey() {
			return sortKey;
		}
	}
}
