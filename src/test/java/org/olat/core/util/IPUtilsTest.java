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

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Initial date: 22.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IPUtilsTest {
	
	@Test
	public void checkRange_start_end() {
		String start = "192.168.5.5";
		String end = "192.168.5.25";
		
		boolean  check1 = IPUtils.isValidRange(start, end, "192.168.5.21");
		Assert.assertTrue(check1);
		
		boolean  check2 = IPUtils.isValidRange(start, end, "192.168.5.45");
		Assert.assertFalse(check2);
	}
}
