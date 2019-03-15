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
package org.olat.core.commons.services.vfs.model;

import org.olat.core.commons.services.vfs.VFSMetadataRef;

/**
 * 
 * Initial date: 13 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VFSMetadataRefImpl implements VFSMetadataRef {
	
	private final Long key;
	
	public VFSMetadataRefImpl(Long key) {
		this.key = key;
	}
	
	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public int hashCode() {
		return key == null ? 860573 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof VFSMetadataRefImpl) {
			VFSMetadataRefImpl ref = (VFSMetadataRefImpl)obj;
			return key != null && key.equals(ref.key);
		}
		return false;
	}
}
