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
package org.olat.core.gui.control.generic.iframe;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Initial date: 03.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IFrameDeliveryMapperTest {
	
	@Test
	public void testPatternRegex() {
		IFrameDeliveryMapper mapper = new IFrameDeliveryMapper();
		String encoding1 = mapper.guessEncoding("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		Assert.assertEquals("utf-8", encoding1);
		
		String encoding2 = mapper.guessEncoding("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
		Assert.assertEquals("UTF-8", encoding2);
	}
}
