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

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Initial date: 18 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IpListValidatorTest {
	
	@Test
	public void isIpAllowed_exactMatch() {
		String ipList = "192.168.1.203";

		boolean allowed1 = IpListValidator.isIpAllowed(ipList, "192.168.1.203");
		Assert.assertTrue(allowed1);

		//negative test
		boolean notAllowed1 = IpListValidator.isIpAllowed(ipList, "192.168.1.129");
		Assert.assertFalse(notAllowed1);
		boolean notAllowed2 = IpListValidator.isIpAllowed(ipList, "192.168.1.204");
		Assert.assertFalse(notAllowed2);
		boolean notAllowed3 = IpListValidator.isIpAllowed(ipList, "192.168.100.203");
		Assert.assertFalse(notAllowed3);
		boolean notAllowed4 = IpListValidator.isIpAllowed(ipList, "192.203.203.203");
		Assert.assertFalse(notAllowed4);
	}
	
	@Test
	public void isIpAllowed_pseudoRange() {
		String ipList = "192.168.1.1 - 192.168.1.128";

		boolean allowed1 = IpListValidator.isIpAllowed(ipList, "192.168.1.64");
		Assert.assertTrue(allowed1);

		//negative test
		boolean notAllowed1 = IpListValidator.isIpAllowed(ipList, "192.168.1.129");
		Assert.assertFalse(notAllowed1);
		boolean notAllowed2 = IpListValidator.isIpAllowed(ipList, "192.168.1.204");
		Assert.assertFalse(notAllowed2);
		boolean notAllowed3 = IpListValidator.isIpAllowed(ipList, "192.168.100.64");
		Assert.assertFalse(notAllowed3);
		boolean notAllowed4 = IpListValidator.isIpAllowed(ipList, "212.203.203.64");
		Assert.assertFalse(notAllowed4);
	}
	
	@Test
	public void isIpAllowed_cidr() {
		String ipList = "192.168.100.1/24";

		boolean allowed1 = IpListValidator.isIpAllowed(ipList, "192.168.100.64");
		Assert.assertTrue(allowed1);

		//negative test
		boolean notAllowed1 = IpListValidator.isIpAllowed(ipList, "192.168.99.129");
		Assert.assertFalse(notAllowed1);
		boolean notAllowed2 = IpListValidator.isIpAllowed(ipList, "192.168.101.204");
		Assert.assertFalse(notAllowed2);
		boolean notAllowed3 = IpListValidator.isIpAllowed(ipList, "192.167.100.1");
		Assert.assertFalse(notAllowed3);
		boolean notAllowed4 = IpListValidator.isIpAllowed(ipList, "212.203.203.64");
		Assert.assertFalse(notAllowed4);
	}
	
	@Test
	public void isIpAllowed_all() {
		String ipList = "192.168.1.203\n192.168.30.1 - 192.168.32.128\n192.168.112.1/24";

		boolean allowed1 = IpListValidator.isIpAllowed(ipList, "192.168.1.203");
		Assert.assertTrue(allowed1);
		boolean allowed2 = IpListValidator.isIpAllowed(ipList, "192.168.31.203");
		Assert.assertTrue(allowed2);
		boolean allowed3 = IpListValidator.isIpAllowed(ipList, "192.168.112.203");
		Assert.assertTrue(allowed3);

		//negative test
		boolean notAllowed1 = IpListValidator.isIpAllowed(ipList, "192.168.99.129");
		Assert.assertFalse(notAllowed1);
		boolean notAllowed2 = IpListValidator.isIpAllowed(ipList, "192.168.101.204");
		Assert.assertFalse(notAllowed2);
		boolean notAllowed3 = IpListValidator.isIpAllowed(ipList, "192.167.100.1");
		Assert.assertFalse(notAllowed3);
		boolean notAllowed4 = IpListValidator.isIpAllowed(ipList, "212.203.203.64");
		Assert.assertFalse(notAllowed4);
	}

}
