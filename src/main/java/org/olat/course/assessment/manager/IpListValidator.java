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
package org.olat.course.assessment.manager;

import java.util.StringTokenizer;

import org.olat.core.util.IPUtils;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 18 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IpListValidator {
	
	private IpListValidator() {
		//
	}
	
	/**
	 * 
	 * @param ipList A list of IPs as string
	 * @param address An address, IP or domain
	 * @return true if the specified address match the ips list
	 */
	public static boolean isIpAllowed(String ipList, String address) {
		boolean allOk = false;
		if(!StringHelper.containsNonWhitespace(ipList)) {
			allOk |= true;
		} else {
			for(StringTokenizer tokenizer = new StringTokenizer(ipList, "\n\r", false); tokenizer.hasMoreTokens(); ) {
				String ipRange = tokenizer.nextToken();
				if(StringHelper.containsNonWhitespace(ipRange)) {
					int indexMask = ipRange.indexOf('/');
					int indexPseudoRange = ipRange.indexOf('-');
					if(indexMask > 0) {
						allOk |= IPUtils.isValidRange(ipRange, address);
					} else if(indexPseudoRange > 0) {
						String begin = ipRange.substring(0, indexPseudoRange).trim();
						String end = ipRange.substring(indexPseudoRange + 1).trim();
						allOk |= IPUtils.isValidRange(begin, end, address);
					} else {
						allOk |= ipRange.equals(address);
					}
				}
			}
		}
		return allOk;
	}

}
