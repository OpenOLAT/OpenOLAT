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
package org.olat.core.util.mail.model;

import org.olat.core.util.mail.MailRef;

/**
 * 
 * Initial date: 20 mai 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public record MailRefImpl(Long key, String metaId) implements MailRef {

	public static final MailRef valueOf(DBMailLight mail) {
		return new MailRefImpl(mail.getKey(), mail.getMetaId());
	}
	
	@Override
	public Long getKey() {
		return key();
	}

	@Override
	public String getMetaId() {
		return metaId();
	}
	
	@Override
	public int hashCode() {
		return key() == null ? 4376 : key().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof MailRefImpl ref) {
			return key() != null && key().equals(ref.key());
		}
		return false;
	}
}
