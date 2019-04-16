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
package org.olat.core.commons.services.doceditor.onlyoffice.restapi;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 
 * Initial date: 12 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CallbackVO {
	
	// see https://api.onlyoffice.com/editors/callback
	
	private List<Action> actions;
	private Integer forcesavetype;
	private String key;
	private Integer status;
	private String url;
	private String[] users;
	
	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	public Integer getForcesavetype() {
		return forcesavetype;
	}

	public void setForcesavetype(Integer forcesavetype) {
		this.forcesavetype = forcesavetype;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String[] getUsers() {
		return users;
	}

	public void setUsers(String[] users) {
		this.users = users;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CallbackVO [actions=");
		builder.append(actions);
		builder.append(", forcesavetype=");
		builder.append(forcesavetype);
		builder.append(", key=");
		builder.append(key);
		builder.append(", status=");
		builder.append(status);
		builder.append(", url=");
		builder.append(url);
		builder.append(", users=");
		builder.append(Arrays.toString(users));
		builder.append("]");
		return builder.toString();
	}

	public static class Action {
		
		private Integer type;
		private String userid;
		
		public Integer getType() {
			return type;
		}
		public void setType(Integer type) {
			this.type = type;
		}
		public String getUserid() {
			return userid;
		}
		public void setUserid(String userid) {
			this.userid = userid;
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Action [type=");
			builder.append(type);
			builder.append(", userid=");
			builder.append(userid);
			builder.append("]");
			return builder.toString();
		}
		
	}
	
}
