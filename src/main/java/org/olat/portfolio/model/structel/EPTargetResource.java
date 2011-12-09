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

import org.olat.core.id.OLATResourceable;

/**
 * 
 * Description:<br>
 * EPTargetResource is a component for the hibernate mapping
 * 
 * <P>
 * Initial Date:  2 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EPTargetResource implements OLATResourceable {
	
	private String resName;
	private Long resId;
	private String resSubPath;
	private String businessPath;

	@Override
	public String getResourceableTypeName() {
		return resName;
	}
	
	public void setResourceableTypeName(String resName) {
		this.resName = resName;
	}

	@Override
	public Long getResourceableId() {
		return resId;
	}
	
	public void setResourceableId(Long resId) {
		this.resId = resId;
	}

	public String getSubPath() {
		return resSubPath;
	}

	public void setSubPath(String resSubPath) {
		this.resSubPath = resSubPath;
	}

	public String getBusinessPath() {
		return businessPath;
	}

	public void setBusinessPath(String businessPath) {
		this.businessPath = businessPath;
	}

	public OLATResourceable getOLATResourceable() {
		final Long id = resId;
		final String name = resName;
		
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

	@Override
	public String toString() {
		return "[" + resName + ":" + resId + "]/" + resSubPath;
	}
}
