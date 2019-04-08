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
package org.olat.core.commons.services.doceditor.collabora.restapi;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * 
 * Initial date: 11 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PutFileVO {
	
	private final String lastModifiedTime;

	@Generated("SparkTools")
	private PutFileVO(Builder builder) {
		this.lastModifiedTime = builder.lastModified;
	}

	public String getLastModifiedTime() {
		return lastModifiedTime;
	}

	/**
	 * Creates builder to build {@link PutFileVO}.
	 * @return created builder
	 */
	@Generated("SparkTools")
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link PutFileVO}.
	 */
	@Generated("SparkTools")
	public static final class Builder {
		private String lastModified;

		private Builder() {
		}

		public Builder withLastModifiedTime(String lastModifiedTime) {
			this.lastModified = lastModifiedTime;
			return this;
		}

		public PutFileVO build() {
			return new PutFileVO(this);
		}
	}

}
