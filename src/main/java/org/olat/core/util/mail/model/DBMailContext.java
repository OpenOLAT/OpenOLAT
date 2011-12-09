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


package org.olat.core.util.mail.model;

import org.olat.core.id.OLATResourceable;
import org.olat.core.util.mail.MailContext;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date:  30 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class DBMailContext implements MailContext {
	
	private String resName;
	private Long resId;
	private String businessPath;
	private String resSubPath;
	
	
	public OLATResourceable getOLATResourceable() {
		final Long id = resId;
		final String name = resName;
		
		if(id == null || name == null) return null;
		
		return new OLATResourceable() {
			@Override
			public Long getResourceableId() {
				return id;
			}

			@Override
			public String getResourceableTypeName() {
				return name;
			}
		};
	}
	
	public String getResSubPath() {
		return resSubPath;
	}

	public void setResSubPath(String resSubPath) {
		this.resSubPath = resSubPath;
	}
	
	public String getBusinessPath() {
		return businessPath;
	}
	
	public void setBusinessPath(String businessPath) {
		this.businessPath = businessPath;
	}

	public String getResName() {
		return resName;
	}

	public void setResName(String resName) {
		this.resName = resName;
	}

	public Long getResId() {
		return resId;
	}

	public void setResId(Long resId) {
		this.resId = resId;
	}
}
