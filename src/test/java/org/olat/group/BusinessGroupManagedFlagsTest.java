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
package org.olat.group;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Test if the mconversion methos of the managed flags are fault tolerant
 * 
 * Initial date: 10.07.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupManagedFlagsTest {
	
	@Test
	public void testManagedConversion() {
		String flags = "all,details";
		BusinessGroupManagedFlag[] arr = BusinessGroupManagedFlag.toEnum(flags);
		Assert.assertNotNull(arr);
		Assert.assertEquals(2, arr.length);
		Assert.assertEquals(arr[0], BusinessGroupManagedFlag.all);
		Assert.assertEquals(arr[1], BusinessGroupManagedFlag.details);
	}
	
	@Test
	public void testManagedConversion_badData_empty() {
		String flags = "all,details,,,,";
		BusinessGroupManagedFlag[] arr = BusinessGroupManagedFlag.toEnum(flags);
		Assert.assertNotNull(arr);
		Assert.assertEquals(2, arr.length);
		Assert.assertEquals(arr[0], BusinessGroupManagedFlag.all);
		Assert.assertEquals(arr[1], BusinessGroupManagedFlag.details);
	}

	@Test
	public void testManagedConversion_badData_wrongFlag() {
		String flags = "all,details,dru,gnu,tools";
		BusinessGroupManagedFlag[] arr = BusinessGroupManagedFlag.toEnum(flags);
		Assert.assertNotNull(arr);
		Assert.assertEquals(3, arr.length);
		Assert.assertEquals(arr[0], BusinessGroupManagedFlag.all);
		Assert.assertEquals(arr[1], BusinessGroupManagedFlag.details);
		Assert.assertEquals(arr[2], BusinessGroupManagedFlag.tools);
	}
}
