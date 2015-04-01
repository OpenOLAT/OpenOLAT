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
package org.olat.group.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.group.BusinessGroupRef;

/**
 * 
 * Initial date: 09.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public final class BusinessGroupRefImpl implements BusinessGroupRef {
	
	private final Long groupKey;
	
	public BusinessGroupRefImpl(Long groupKey) {
		this.groupKey = groupKey;
	}

	@Override
	public Long getKey() {
		return groupKey;
	}
	
	public static final List<BusinessGroupRef> toRefs(List<Long> keys) {
		if(keys == null || keys.isEmpty()) return Collections.emptyList();
		List<BusinessGroupRef> refs = new ArrayList<>(keys.size());
		for(Long key:keys) {
			refs.add(new BusinessGroupRefImpl(key));
		}
		return refs;
	}
	
	public static final List<Long> toKeys(List<? extends BusinessGroupRef> refs) {
		if(refs == null || refs.isEmpty()) return Collections.emptyList();
		List<Long> keys = new ArrayList<>(refs.size());
		for(BusinessGroupRef ref:refs) {
			keys.add(ref.getKey());
		}
		return keys;
	}

	@Override
	public int hashCode() {
		return groupKey == null ? 98376802 : groupKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if(obj instanceof BusinessGroupRefImpl) {
			BusinessGroupRefImpl ref = (BusinessGroupRefImpl)obj;
			return groupKey != null && groupKey.equals(ref.groupKey);
		}
		return false;
	}
}
