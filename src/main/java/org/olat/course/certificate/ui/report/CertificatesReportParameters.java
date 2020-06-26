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
package org.olat.course.certificate.ui.report;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * Initial date: 26 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificatesReportParameters {
	
	private final List<With> with;
	private final boolean onlyPassed;
	private final Date certificateStart;
	private final Date certificateEnd;
	
	public CertificatesReportParameters(Date certificateStart, Date certificateEnd, List<With> with, boolean onlyPassed) {
		this.with = with;
		this.onlyPassed = onlyPassed;
		this.certificateEnd = certificateEnd;
		this.certificateStart = certificateStart;
	}

	public List<With> getWith() {
		return with;
	}
	
	public boolean isOnlyPassed() {
		return onlyPassed;
	}

	public Date getCertificateStart() {
		return certificateStart;
	}

	public Date getCertificateEnd() {
		return certificateEnd;
	}
	
	public enum With {
		withoutCertificate,
		validCertificate,
		expiredCertificate;
		
		public static List<With> values(Collection<String> keys) {
			return keys.stream()
					.map(With::valueOf)
					.collect(Collectors.toList());
		}
	}
}
