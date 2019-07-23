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

package org.olat.core.commons.services.mark.impl;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;

/**
 * Initial Date:  9 mar. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class MarkImpl extends PersistentObject implements Mark {

	private static final long serialVersionUID = 9010184053104344959L;
	
	private String resName;
	private Long resId;
	private String resSubPath;
	private String businessPath;
	private Identity creator;
	
	@Override
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
	public boolean isMarked() {
		return false;
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

	public Identity getCreator() {
		return creator;
	}

	public void setCreator(Identity creator) {
		this.creator = creator;
	}
}