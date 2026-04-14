/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.commons.services.ai.model;

import java.util.Locale;
import java.util.UUID;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;

public record AiUsageContext(
		String usageContextType,
		String usageContextId,
		String resourceType,
		Long resourceId,
		String resourceSubId,
		Identity identity,
		Locale locale) {

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String usageContextType;
		private String usageContextId;
		private Identity identity;
		private String resourceType;
		private Long resourceId;
		private String resourceSubId;
		private Locale locale;

		public Builder usageContextType(String usageContextType) {
			this.usageContextType = usageContextType;
			return this;
		}

		public Builder usageContextId(String usageContextId) {
			this.usageContextId = usageContextId;
			return this;
		}

		public Builder identity(Identity identity) {
			this.identity = identity;
			return this;
		}

		public Builder resource(OLATResourceable ores) {
			this.resourceType = ores.getResourceableTypeName();
			this.resourceId = ores.getResourceableId();
			return this;
		}

		public Builder resourceType(String resourceType) {
			this.resourceType = resourceType;
			return this;
		}

		public Builder resourceId(Long resourceId) {
			this.resourceId = resourceId;
			return this;
		}

		public Builder resourceSubId(String resourceSubId) {
			this.resourceSubId = resourceSubId;
			return this;
		}

		public Builder locale(Locale locale) {
			this.locale = locale;
			return this;
		}

		public AiUsageContext build() {
			String contextId = usageContextId != null ? usageContextId : UUID.randomUUID().toString();
			return new AiUsageContext(usageContextType, contextId, resourceType, resourceId, resourceSubId, identity, locale);
		}
	}
}
