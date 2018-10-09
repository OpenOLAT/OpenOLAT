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
package org.olat.repository.model;

import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 09.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryRefImpl implements RepositoryEntryRef {
	
	private final Long repoKey;
	
	public RepositoryEntryRefImpl(Long repoKey) {
		this.repoKey = repoKey;
	}

	@Override
	public Long getKey() {
		return repoKey;
	}

	@Override
	public int hashCode() {
		return repoKey == null ? -635465 : repoKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		} else if (obj instanceof RepositoryEntryRefImpl) {
			RepositoryEntryRefImpl ref = (RepositoryEntryRefImpl)obj;
			return repoKey != null && repoKey.equals(ref.getKey());
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RepositoryEntryRefImpl [repoKey=");
		builder.append(repoKey);
		builder.append("]");
		return builder.toString();
	}
}