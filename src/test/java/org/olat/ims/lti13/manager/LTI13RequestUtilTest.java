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
package org.olat.ims.lti13.manager;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Initial date: 11 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13RequestUtilTest {
	
	@Test
	public void scoreUrl() {
		String mEndpointUrl = "https://cuberai.frentix.com/moodle/mod/lti/services.php/2/lineitems/7/lineitem?type_id=1";
		String scoreUrl = LTI13RequestUtil.scoreUrl(mEndpointUrl);
		Assert.assertEquals("https://cuberai.frentix.com/moodle/mod/lti/services.php/2/lineitems/7/lineitem/scores?type_id=1", scoreUrl);
	}

}
