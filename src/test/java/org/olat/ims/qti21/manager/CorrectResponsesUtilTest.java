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
package org.olat.ims.qti21.manager;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ed.ph.jqtiplus.types.DataTypeBinder;

/**
 * 
 * Initial date: 27.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CorrectResponsesUtilTest {
	
	@Test
	public void parseResponses() {
		String stringuifiedResponses = "[20 30][test][]";
		List<String> responses = CorrectResponsesUtil.parseResponses(stringuifiedResponses);
		
		Assert.assertNotNull(responses);
		Assert.assertEquals(3, responses.size());
		Assert.assertEquals("20 30", responses.get(0));
		Assert.assertEquals("test", responses.get(1));
		Assert.assertEquals("", responses.get(2));
	}

	@Test
	public void parseDouble() {
		double val = DataTypeBinder.parseFloat("42.0");
		Assert.assertEquals(42.0, val, 0.0001);
		

		double thousandVal = DataTypeBinder.parseFloat("42,0");
		Assert.assertEquals(42.0, thousandVal, 0.0001);
	}
}
