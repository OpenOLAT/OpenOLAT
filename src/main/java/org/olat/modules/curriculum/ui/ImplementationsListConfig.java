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
package org.olat.modules.curriculum.ui;

import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;

/**
 *
 * Initial date: 13.04.2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public record ImplementationsListConfig(
		List<GroupRoles> asRoles,
		Identity coachIdentity,
		boolean withPreparation,
		boolean withPreparationWarning,
		boolean withEnhancedInfoHeader,
		boolean withFormTitle,
		String helpUrl,
		boolean withBookmarks,
		boolean withId,
		boolean extRefVisibilityDefault,
		boolean withRoles,
		boolean withStatus,
		boolean withCompletion,
		boolean withCalendar,
		boolean withCancelledFilter) {

	public static Builder builder(List<GroupRoles> asRoles) {
		return new Builder(asRoles);
	}

	public static class Builder {

		private final List<GroupRoles> asRoles;
		private Identity coachIdentity;
		private boolean withPreparation;
		private boolean withPreparationWarning;
		private boolean withEnhancedInfoHeader;
		private boolean withFormTitle;
		private String helpUrl;
		private boolean withBookmarks;
		private boolean withId;
		private boolean extRefVisibilityDefault;
		private boolean withRoles;
		private boolean withStatus;
		private boolean withCompletion;
		private boolean withCalendar;
		private boolean withCancelledFilter;

		private Builder(List<GroupRoles> asRoles) {
			this.asRoles = asRoles;
		}

		public Builder setCoachIdentity(Identity identity) {
			this.coachIdentity = identity;
			return this;
		}

		public Builder enablePreparation() {
			this.withPreparation = true;
			return this;
		}

		public Builder enablePreparationWarning() {
			this.withPreparationWarning = true;
			return this;
		}

		public Builder enableEnhancedInfoHeader() {
			this.withEnhancedInfoHeader = true;
			return this;
		}

		public Builder enableFormTitle() {
			this.withFormTitle = true;
			return this;
		}

		public Builder setHelpUrl(String helpUrl) {
			this.helpUrl = helpUrl;
			return this;
		}

		public Builder enableBookmarks() {
			this.withBookmarks = true;
			return this;
		}

		public Builder enableId() {
			this.withId = true;
			return this;
		}

		public Builder enableExtRefVisibilityDefault() {
			this.extRefVisibilityDefault = true;
			return this;
		}

		public Builder enableRoles() {
			this.withRoles = true;
			return this;
		}

		public Builder enableStatus() {
			this.withStatus = true;
			return this;
		}

		public Builder enableCompletion() {
			this.withCompletion = true;
			return this;
		}

		public Builder enableCalendar() {
			this.withCalendar = true;
			return this;
		}

		public Builder enableCancelledFilter() {
			this.withCancelledFilter = true;
			return this;
		}

		public ImplementationsListConfig build() {
			return new ImplementationsListConfig(asRoles, coachIdentity, withPreparation, withPreparationWarning,
					withEnhancedInfoHeader, withFormTitle, helpUrl, withBookmarks, withId,
					extRefVisibilityDefault, withRoles, withStatus, withCompletion, withCalendar,
					withCancelledFilter);
		}
	}
}
