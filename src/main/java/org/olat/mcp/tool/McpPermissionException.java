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
package org.olat.mcp.tool;

/**
 * A tool throws it when the security callback of its module denies the
 * operation. The registry turns it into a tool result with isError and the
 * audit status 403. The message names the object and the permission, the
 * model can tell the user what is missing.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpPermissionException extends RuntimeException {

	private static final long serialVersionUID = 2026100203L;

	private final String objectDescription;
	private final String permission;

	/**
	 * @param objectDescription What was accessed, e.g. "project 'Alpha' (key 123)"
	 * @param permission What is missing, e.g. "create ToDos"
	 */
	public McpPermissionException(String objectDescription, String permission) {
		super("Permission denied: " + permission + " on " + objectDescription);
		this.objectDescription = objectDescription;
		this.permission = permission;
	}

	public String getObjectDescription() {
		return objectDescription;
	}

	public String getPermission() {
		return permission;
	}
}
