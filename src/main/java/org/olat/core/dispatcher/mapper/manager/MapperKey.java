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
package org.olat.core.dispatcher.mapper.manager;

import java.io.Serializable;

import org.olat.core.util.SessionInfo;
import org.olat.core.util.UserSession;

/**
 * 
 * Initial date: 22.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MapperKey implements Serializable {

	private static final long serialVersionUID = 4078036800789246979L;
	
	private String mapperId;
	private String sessionId;
	private String url;
	
	public MapperKey() {
		//
	}
	
	public MapperKey(UserSession usess, String mapperId) {
		this.mapperId = mapperId;
		if(usess != null && usess.getSessionInfo() != null) {
			SessionInfo infos = usess.getSessionInfo();
			if(infos.getSession() != null) {
				sessionId = infos.getSession().getId();
			}
		}
		
		if(sessionId == null) {
			sessionId = "";
		}
	}
	
	public String getMapperId() {
		return mapperId;
	}
	
	public void setMapperId(String mapperId) {
		this.mapperId = mapperId;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public int hashCode() {
		return sessionId.hashCode() + mapperId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof MapperKey) {
			MapperKey mkey = (MapperKey)obj;
			return sessionId.equals(mkey.sessionId) && mapperId.equals(mkey.mapperId);
		}
		return super.equals(obj);
	}
}