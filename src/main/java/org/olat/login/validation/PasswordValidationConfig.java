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
package org.olat.login.validation;

/**
 * 
 * Initial date: 16 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PasswordValidationConfig {

	private final int passwordMinLength;
	private final int passwordMaxLength;
	private final String passwordLetters;
	private final String passwordLettersUppercase;
	private final String passwordLettersLowercase;
	private final String passwordDigitsAndSpecialSigns;
	private final String passwordDigits;
	private final String passwordSpecialSigns;
	private final boolean passwordUsernameForbidden;
	private final boolean passwordFirstnameForbidden;
	private final boolean passwordLastnameForbidden;
	private final int passwordHistory;

	private PasswordValidationConfig(Builder builder) {
		this.passwordMinLength = builder.passwordMinLength;
		this.passwordMaxLength = builder.passwordMaxLength;
		this.passwordLetters = builder.passwordLetters;
		this.passwordLettersUppercase = builder.passwordLettersUppercase;
		this.passwordLettersLowercase = builder.passwordLettersLowercase;
		this.passwordDigitsAndSpecialSigns = builder.passwordDigitsAndSpecialSigns;
		this.passwordDigits = builder.passwordDigits;
		this.passwordSpecialSigns = builder.passwordSpecialSigns;
		this.passwordUsernameForbidden = builder.passwordUsernameForbidden;
		this.passwordFirstnameForbidden = builder.passwordFirstnameForbidden;
		this.passwordLastnameForbidden = builder.passwordLastnameForbidden;
		this.passwordHistory = builder.passwordHistory;
	}
	
	public int getPasswordMinLength() {
		return passwordMinLength;
	}

	public int getPasswordMaxLength() {
		return passwordMaxLength;
	}

	public String getPasswordLetters() {
		return passwordLetters;
	}

	public String getPasswordLettersUppercase() {
		return passwordLettersUppercase;
	}

	public String getPasswordLettersLowercase() {
		return passwordLettersLowercase;
	}

	public String getPasswordDigitsAndSpecialSigns() {
		return passwordDigitsAndSpecialSigns;
	}

	public String getPasswordDigits() {
		return passwordDigits;
	}

	public String getPasswordSpecialSigns() {
		return passwordSpecialSigns;
	}

	public boolean isPasswordUsernameForbidden() {
		return passwordUsernameForbidden;
	}

	public boolean isPasswordFirstnameForbidden() {
		return passwordFirstnameForbidden;
	}

	public boolean isPasswordLastnameForbidden() {
		return passwordLastnameForbidden;
	}

	public int getPasswordHistory() {
		return passwordHistory;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private int passwordMinLength;
		private int passwordMaxLength;
		private String passwordLetters;
		private String passwordLettersUppercase;
		private String passwordLettersLowercase;
		private String passwordDigitsAndSpecialSigns;
		private String passwordDigits;
		private String passwordSpecialSigns;
		private boolean passwordUsernameForbidden;
		private boolean passwordFirstnameForbidden;
		private boolean passwordLastnameForbidden;
		private int passwordHistory;

		private Builder() {
		}

		public Builder withPasswordMinLength(int passwordMinLength) {
			this.passwordMinLength = passwordMinLength;
			return this;
		}

		public Builder withPasswordMaxLength(int passwordMaxLength) {
			this.passwordMaxLength = passwordMaxLength;
			return this;
		}

		public Builder withPasswordLetters(String passwordLetters) {
			this.passwordLetters = passwordLetters;
			return this;
		}

		public Builder withPasswordLettersUppercase(String passwordLettersUppercase) {
			this.passwordLettersUppercase = passwordLettersUppercase;
			return this;
		}

		public Builder withPasswordLettersLowercase(String passwordLettersLowercase) {
			this.passwordLettersLowercase = passwordLettersLowercase;
			return this;
		}

		public Builder withPasswordDigitsAndSpecialSigns(String passwordDigitsAndSpecialSigns) {
			this.passwordDigitsAndSpecialSigns = passwordDigitsAndSpecialSigns;
			return this;
		}

		public Builder withPasswordDigits(String passwordDigits) {
			this.passwordDigits = passwordDigits;
			return this;
		}

		public Builder withPasswordSpecialSigns(String passwordSpecialSigns) {
			this.passwordSpecialSigns = passwordSpecialSigns;
			return this;
		}

		public Builder withPasswordUsernameForbidden(boolean passwordUsernameForbidden) {
			this.passwordUsernameForbidden = passwordUsernameForbidden;
			return this;
		}

		public Builder withPasswordFirstnameForbidden(boolean passwordFirstnameForbidden) {
			this.passwordFirstnameForbidden = passwordFirstnameForbidden;
			return this;
		}

		public Builder withPasswordLastnameForbidden(boolean passwordLastnameForbidden) {
			this.passwordLastnameForbidden = passwordLastnameForbidden;
			return this;
		}
		
		public Builder withPasswordHistory(int passwordHistory) {
			this.passwordHistory = passwordHistory;
			return this;
		}

		public PasswordValidationConfig build() {
			return new PasswordValidationConfig(this);
		}
	}
	
}
