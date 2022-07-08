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
package org.olat.course.certificate;

import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 7 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum CertificateManagedFlag {
	
	all,
    delete(all);
	

	private CertificateManagedFlag[] parents;
	private static final Logger log = Tracing.createLoggerFor(CertificateManagedFlag.class);
	public static final CertificateManagedFlag[] EMPTY_ARRAY = new CertificateManagedFlag[0];

	private static CertificatesModule certificateModule;
	
	private CertificateManagedFlag() {
		//
	}
	
	private CertificateManagedFlag(CertificateManagedFlag... parents) {
		if(parents == null) {
			this.parents = new CertificateManagedFlag[0];
		} else {
			this.parents = parents;
		}
	}
	
	public static CertificateManagedFlag[] toEnum(String flags) {
		if(StringHelper.containsNonWhitespace(flags)) {
			String[] flagArr = flags.split(",");
			CertificateManagedFlag[] flagEnums = new CertificateManagedFlag[flagArr.length];
	
			int count = 0;
			for(String flag:flagArr) {
				if(StringHelper.containsNonWhitespace(flag)) {
					try {
						CertificateManagedFlag flagEnum = valueOf(flag);
						flagEnums[count++] = flagEnum;
					} catch (Exception e) {
						log.warn("Cannot parse this managed flag: {}", flag, e);
					}
				}
			}
			
			if(count != flagEnums.length) {
				flagEnums = Arrays.copyOf(flagEnums, count);
			}
			return flagEnums;
		} else {
			return EMPTY_ARRAY;
		}
	}
	
	public static String toString(CertificateManagedFlag... flags) {
		StringBuilder sb = new StringBuilder();
		if(flags != null && flags.length > 0 && flags[0] != null) {
			for(CertificateManagedFlag flag:flags) {
				if(flag != null) {
					if(sb.length() > 0) sb.append(",");
					sb.append(flag.name());
				}
			}
		}
		return sb.length() == 0 ? null : sb.toString();
	}
	
	public static boolean isManaged(Certificate cert, CertificateManagedFlag marker) {
		if(certificateModule == null) {
			certificateModule = CoreSpringFactory.getImpl(CertificatesModule.class);
		}
		if(!certificateModule.isManagedCertificates()) {
			return false;
		}
		
		if(cert != null && (contains(cert, marker) || contains(cert, marker.parents))) {
			return true;
		}
		return false;
	}
	
	public static boolean isManaged(CertificateLight cert, CertificateManagedFlag marker) {
		if(certificateModule == null) {
			certificateModule = CoreSpringFactory.getImpl(CertificatesModule.class);
		}
		if(!certificateModule.isManagedCertificates()) {
			return false;
		}
		return cert != null && (contains(cert.getManagedFlags(), marker) || contains(cert.getManagedFlags(), marker.parents));
	}
	
	public static boolean isManaged(CertificateManagedFlag[] flags, CertificateManagedFlag marker) {
		if(certificateModule == null) {
			certificateModule = CoreSpringFactory.getImpl(CertificatesModule.class);
		}
		if(!certificateModule.isManagedCertificates()) {
			return false;
		}
		return flags != null && (contains(flags, marker) || contains(flags, marker.parents));
	}
	
	private static boolean contains(Certificate cert, CertificateManagedFlag... markers) {
		if(cert == null) return false;
		CertificateManagedFlag[] flags = cert.getManagedFlags();
		return contains(flags, markers);
	}

	private static boolean contains(CertificateManagedFlag[] flags, CertificateManagedFlag... markers) {
		if(flags == null || flags.length == 0) return false;

		for(CertificateManagedFlag flag:flags) {
			for(CertificateManagedFlag marker:markers) {
				if(flag.equals(marker)) {
					return true;
				}
			}
		}
		return false;
	}
}