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
package org.olat.core.util;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * 
 * Initial date: 21 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Parameterized.class)
public class IPUtilsValidRangeTest {
	
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "192.168.100.1/24", "192.168.100.1", Boolean.TRUE },
                { "192.168.100.1/24", "192.168.99.255", Boolean.FALSE },
                { "192.168.100.1/24", "192.168.101.1", Boolean.FALSE },
                { "192.168.100.1/24", "212.34.100.0", Boolean.FALSE },
                { "192.168.100.1/24 ", "192.168.100.1", Boolean.TRUE },
                { "172.20.36.0/24 ", "192.168.100.1", Boolean.FALSE },
                { "192.168.1.0/24", "192.168.100.1", Boolean.FALSE },
                { "192.168.1.0/24", "192.168.1.1", Boolean.TRUE }
        });
    }
    
    private String ip;
    private String ipToCheck;
    private Boolean result;
    
    public IPUtilsValidRangeTest(String ip, String ipToCheck, Boolean result) {
    		this.ip = ip;
    		this.ipToCheck = ipToCheck;
    		this.result = result;
    }
	
	@Test
	public void checkRange() {
		boolean  allowed = IPUtils.isValidRange(ip, ipToCheck);
		Assert.assertEquals(result, Boolean.valueOf(allowed));
	}
}
