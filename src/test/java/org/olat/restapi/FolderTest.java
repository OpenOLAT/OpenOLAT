/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.restapi;

import static org.olat.core.util.vfs.restapi.VFSWebservice.normalize;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
/**
 * 
 * <h3>Description:</h3>
 * <p>
 * Initial Date:  28 jan. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class FolderTest {

	@Test
	public void testNormalizer() {
		assertEquals("HASTDJUR", normalize("HÄSTDJUR"));
		assertEquals("HASTDJUR", normalize("HÄSTDJÜR"));
		assertEquals("HAST_DJUR", normalize("HÄST_DJUR"));
		assertEquals("This_is_a_funky_String", normalize("Tĥïŝ ĩš â fůňķŷ Šťŕĭńġ"));
	}
}
