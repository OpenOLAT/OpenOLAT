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
package org.olat.modules.quality.data;

import jakarta.annotation.Generated;

/**
 * 
 * Initial date: 05.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class Parameter {
	
	private final String name;
	private final String identifier;
	private final String identifierDelim;
	private final int numberLevel1;
	private final String nameLevel1;
	private final String identifierLevel1;
	private final int numberLevel2;
	private final String nameLevel2;
	private final String identifierLevel2;
	private final int numberLevel3;
	private final String nameLevel3;
	private final String identifierLevel3;
	private final int maxNumberParticipants;
	private final int minNumberParticipants;
	private final int numberCourses;

	@Generated("SparkTools")
	private Parameter(Builder builder) {
		this.name = builder.name;
		this.identifier = builder.identifier;
		this.identifierDelim = builder.identifierDelim;
		this.numberLevel1 = builder.numberLevel1;
		this.nameLevel1 = builder.nameLevel1;
		this.identifierLevel1 = builder.identifierLevel1;
		this.numberLevel2 = builder.numberLevel2;
		this.nameLevel2 = builder.nameLevel2;
		this.identifierLevel2 = builder.identifierLevel2;
		this.numberLevel3 = builder.numberLevel3;
		this.nameLevel3 = builder.nameLevel3;
		this.identifierLevel3 = builder.identifierLevel3;
		this.maxNumberParticipants = builder.maxNumberParticipants;
		this.minNumberParticipants = builder.minNumberParticipants;
		this.numberCourses = builder.numberCourses;
	}

	public String getName() {
		return name;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getIdentifierDelim() {
		return identifierDelim;
	}

	public int getNumberLevel1() {
		return numberLevel1;
	}

	public String getNameLevel1() {
		return nameLevel1;
	}

	public String getIdentifierLevel1() {
		return identifierLevel1;
	}

	public int getNumberLevel2() {
		return numberLevel2;
	}

	public String getNameLevel2() {
		return nameLevel2;
	}

	public String getIdentifierLevel2() {
		return identifierLevel2;
	}

	public int getNumberLevel3() {
		return numberLevel3;
	}

	public String getNameLevel3() {
		return nameLevel3;
	}

	public String getIdentifierLevel3() {
		return identifierLevel3;
	}

	public int getMaxNumberParticipants() {
		return maxNumberParticipants;
	}

	public int getMinNumberParticipants() {
		return minNumberParticipants;
	}

	public int getNumberCourses() {
		return numberCourses;
	}

	/**
	 * Creates builder to build {@link Parameter}.
	 * @return created builder
	 */
	@Generated("SparkTools")
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link Parameter}.
	 */
	@Generated("SparkTools")
	public static final class Builder {

		private String name = "";
		private String identifier = "R";
		private String identifierDelim = "-";
		private int numberLevel1 = 2;
		private String nameLevel1 = "First";
		private String identifierLevel1 = "F";
		private int numberLevel2 = 3;
		private String nameLevel2 = "Second";
		private String identifierLevel2 = "S";
		private int numberLevel3 = 4;
		private String nameLevel3 = "Third";
		private String identifierLevel3 = "T";
		private int maxNumberParticipants = 30;
		private int minNumberParticipants = 5;
		private int numberCourses = 3;;

		private Builder() {
		}

		public Builder withName(String name) {
			this.name = name;
			return this;
		}

		public Builder withIdentifier(String identifier) {
			this.identifier = identifier;
			return this;
		}

		public Builder withIdentifierDelim(String identifierDelim) {
			this.identifierDelim = identifierDelim;
			return this;
		}

		public Builder withNumberLevel1(int numberLevel1) {
			this.numberLevel1 = numberLevel1;
			return this;
		}

		public Builder withNameLevel1(String nameLevel1) {
			this.nameLevel1 = nameLevel1;
			return this;
		}

		public Builder withIdentifierLevel1(String identifierLevel1) {
			this.identifierLevel1 = identifierLevel1;
			return this;
		}

		public Builder withNumberLevel2(int numberLevel2) {
			this.numberLevel2 = numberLevel2;
			return this;
		}

		public Builder withNameLevel2(String nameLevel2) {
			this.nameLevel2 = nameLevel2;
			return this;
		}

		public Builder withIdentifierLevel2(String identifierLevel2) {
			this.identifierLevel2 = identifierLevel2;
			return this;
		}

		public Builder withNumberLevel3(int numberLevel3) {
			this.numberLevel3 = numberLevel3;
			return this;
		}

		public Builder withNameLevel3(String nameLevel3) {
			this.nameLevel3 = nameLevel3;
			return this;
		}

		public Builder withIdentifierLevel3(String identifierLevel3) {
			this.identifierLevel3 = identifierLevel3;
			return this;
		}

		public Builder withMaxNumberParticipants(int maxNumberParticipants) {
			this.maxNumberParticipants = maxNumberParticipants;
			return this;
		}

		public Builder withMinNumberParticipants(int minNumberParticipants) {
			this.minNumberParticipants = minNumberParticipants;
			return this;
		}

		public Builder withNumberCourses(int numberCourses) {
			this.numberCourses = numberCourses;
			return this;
		}

		public Parameter build() {
			return new Parameter(this);
		}
	}
	
}
