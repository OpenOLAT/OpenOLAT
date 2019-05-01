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
 * Initial date: 30 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Parameterized.class)
public class ZipUtilConcatTest {
	
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
        	{ null, "test.txt", "test.txt" },
        	{ "", "test.txt", "test.txt" },
        	{ "dir", "test.txt", "dir/test.txt" },
        	{ "dir/", "test.txt", "dir/test.txt" },
        	{ "dir/more", "test.txt", "dir/more/test.txt" },
        	{ "dir/more/", "test.txt", "dir/more/test.txt" }
        });
    };
    
    private final String dirName;
    private final String name;
    private final String expected;
    
    public ZipUtilConcatTest(String dirName, String name, String expected) {
    	this.dirName = dirName;
    	this.name = name;
    	this.expected = expected;
    }
	
	@Test
	public void concat() {
		String path = ZipUtil.concat(dirName, name);
		Assert.assertEquals(expected, path);
	}

}
