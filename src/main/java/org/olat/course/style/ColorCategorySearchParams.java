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
package org.olat.course.style;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 
 * Initial date: 29 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ColorCategorySearchParams {
	
	private final Boolean enabled;
	private final Collection<ColorCategory.Type> types;
	private final Collection<String> excludedIdentifiers;

	private ColorCategorySearchParams(Builder builder) {
		this.enabled = builder.enabled;
		this.types = builder.types != null? new ArrayList<>(builder.types): null;
		this.excludedIdentifiers = builder.excludedIdentifiers != null? new ArrayList<>(builder.excludedIdentifiers): null;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public Collection<ColorCategory.Type> getTypes() {
		return types;
	}

	public Collection<String> getExcludedIdentifiers() {
		return excludedIdentifiers;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private Boolean enabled;
		private Collection<ColorCategory.Type> types;
		private Collection<String> excludedIdentifiers;

		private Builder() {
		}

		/**
		 * Filter enabled / disabled color categories
		 * true: enabled only
		 * false: disabled only
		 * null: no filter
		 *
		 * @param enabled
		 */
		public Builder withEnabled(Boolean enabled) {
			this.enabled = enabled;
			return this;
		}
		
		public Builder addType(ColorCategory.Type type) {
			if (types == null) {
				types = new ArrayList<>(3);
			}
			types.add(type);
			return this;
		}
		
		public Builder addColorTypes() {
			addType(ColorCategory.Type.predefined);
			addType(ColorCategory.Type.custom);
			return this;
		}
		
		public Builder addExcludedIdentifier(String identifier) {
			if (excludedIdentifiers == null) {
				excludedIdentifiers = new ArrayList<>(3);
			}
			excludedIdentifiers.add(identifier);
			return this;
		}
		
		public Builder excludeInherited() {
			addExcludedIdentifier(ColorCategory.IDENTIFIER_INHERITED);
			return this;
		}
		
		public ColorCategorySearchParams build() {
			return new ColorCategorySearchParams(this);
		}
	}
	
}
