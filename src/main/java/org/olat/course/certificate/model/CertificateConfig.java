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
package org.olat.course.certificate.model;

import java.io.Serializable;

/**
 * 
 * Initial date: 23 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CertificateConfig implements Serializable {
	
	private static final long serialVersionUID = 8837808595823549502L;
	
	private final String custom1;
	private final String custom2;
	private final String custom3;
	private final boolean sendEmailBcc;
	private final boolean sendEmailLinemanager;

	private CertificateConfig(Builder builder) {
		this.custom1 = builder.custom1;
		this.custom2 = builder.custom2;
		this.custom3 = builder.custom3;
		this.sendEmailBcc = builder.sendEmailBcc;
		this.sendEmailLinemanager = builder.sendEmailLinemanager;
	}
	
	public String getCustom1() {
		return custom1;
	}

	public String getCustom2() {
		return custom2;
	}

	public String getCustom3() {
		return custom3;
	}

	public boolean isSendEmailBcc() {
		return sendEmailBcc;
	}

	public boolean isSendEmailLinemanager() {
		return sendEmailLinemanager;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String custom1;
		private String custom2;
		private String custom3;
		private boolean sendEmailBcc;
		private boolean sendEmailLinemanager;

		private Builder() {
		}

		public Builder withCustom1(String custom1) {
			this.custom1 = custom1;
			return this;
		}

		public Builder withCustom2(String custom2) {
			this.custom2 = custom2;
			return this;
		}

		public Builder withCustom3(String custom3) {
			this.custom3 = custom3;
			return this;
		}

		public Builder withSendEmailBcc(boolean sendEmailBcc) {
			this.sendEmailBcc = sendEmailBcc;
			return this;
		}

		public Builder withSendEmailLinemanager(boolean sendEmailLinemanager) {
			this.sendEmailLinemanager = sendEmailLinemanager;
			return this;
		}

		public CertificateConfig build() {
			return new CertificateConfig(this);
		}
	}

}
