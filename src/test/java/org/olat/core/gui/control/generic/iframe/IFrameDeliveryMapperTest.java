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

		// capital / lowercase mixed
		String encoding3 = mapper.guessEncoding("<meta http-equiv=\"Content-type\" content=\"text/html; charset=utf-8\">");
		Assert.assertEquals("utf-8", encoding3);

		// short charset rules often used in html5 apps
		String encoding4 = mapper.guessEncoding("<meta charset=\"utf-8\">");
		Assert.assertEquals("utf-8", encoding4);

		String encoding5 = mapper.guessEncoding("<meta charset='utf-8'>");
		Assert.assertEquals("utf-8", encoding5);

		String encoding6 = mapper.guessEncoding("<meta charset=utf-8>");
		Assert.assertEquals("utf-8", encoding6);

		String encoding7 = mapper.guessEncoding("<meta charset=utf-8/>");
		Assert.assertEquals("utf-8", encoding7);

		String encoding8 = mapper.guessEncoding("<meta charset=utf-8 />");
		Assert.assertEquals("utf-8", encoding8);

		// wrong stuff
		String encoding9 = mapper.guessEncoding("<meta gugus='asdf'><dings charset='utf-8' />");
		Assert.assertNull(encoding9);
		
	}
}
