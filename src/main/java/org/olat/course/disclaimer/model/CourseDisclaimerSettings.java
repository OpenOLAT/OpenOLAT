/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
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
package org.olat.course.disclaimer.model;

/**
 * The disclaimer configuration of a course: the terms of use (disclaimer 1)
 * and the data protection declaration (disclaimer 2).
 *
 * Initial date: 2 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class CourseDisclaimerSettings {

	private Disclaimer termsOfUse;
	private Disclaimer dataProtection;

	public CourseDisclaimerSettings(Disclaimer termsOfUse, Disclaimer dataProtection) {
		this.termsOfUse = termsOfUse;
		this.dataProtection = dataProtection;
	}

	public Disclaimer getTermsOfUse() {
		return termsOfUse;
	}

	public void setTermsOfUse(Disclaimer termsOfUse) {
		this.termsOfUse = termsOfUse;
	}

	public Disclaimer getDataProtection() {
		return dataProtection;
	}

	public void setDataProtection(Disclaimer dataProtection) {
		this.dataProtection = dataProtection;
	}

	/**
	 * @param disclaimer 1 for the terms of use, 2 for the data protection declaration
	 * @return The disclaimer
	 */
	public Disclaimer getDisclaimer(int disclaimer) {
		return disclaimer == 1 ? termsOfUse : dataProtection;
	}

	public boolean isAnyEnabled() {
		return (termsOfUse != null && termsOfUse.isEnabled())
				|| (dataProtection != null && dataProtection.isEnabled());
	}

	public static class Disclaimer {

		private boolean enabled;
		private String title;
		private String terms;
		private String label1;
		private String label2;

		public Disclaimer() {
			//
		}

		public Disclaimer(boolean enabled, String title, String terms, String label1, String label2) {
			this.enabled = enabled;
			this.title = title;
			this.terms = terms;
			this.label1 = label1;
			this.label2 = label2;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getTerms() {
			return terms;
		}

		public void setTerms(String terms) {
			this.terms = terms;
		}

		public String getLabel1() {
			return label1;
		}

		public void setLabel1(String label1) {
			this.label1 = label1;
		}

		public String getLabel2() {
			return label2;
		}

		public void setLabel2(String label2) {
			this.label2 = label2;
		}
	}
}
