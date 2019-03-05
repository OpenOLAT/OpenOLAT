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
package org.olat.restapi;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.olat.core.util.vfs.restapi.VFSWebservice;
/**
 * 
 * <h3>Description:</h3>
 * <p>
 * Initial Date:  28 jan. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
@RunWith(Parameterized.class)
public class FolderTest {
	
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "HASTDJUR", "HÄSTDJUR" },
        		{ "HASTDJUR", "HÄSTDJÜR" },
        		{ "HAST_DJUR", "HÄST_DJUR" },
        		{ "This_is_a_funky_String", "Tĥïŝ ĩš â fůňķŷ Šťŕĭńġ" }
        });
    }
    
    private String expected;
    private String string;
    
    public FolderTest(String expected, String string) {
    	this.expected = expected;
    	this.string = string;
    }

	@Test
	public void testNormalizer() {
		String normalized = VFSWebservice.normalize(string);
		assertEquals(expected, normalized);
	}
}
