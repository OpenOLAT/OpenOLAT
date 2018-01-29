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
 * Initial date: 26 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Parameterized.class)
public class FormatterHourAndSecondsTest {
	
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { 0l, "0:00" },
                { 70000l, "0:01" },
                { 599000l, "0:09" },
                { 825000l, "0:13" },
                { 3661000l, "1:01" },
                { 14625000l, "4:03" },
                { 15825000l, "4:23" },
                { 116625000l, "32:23" },
                { 1916625000l, "532:23" }
        });
    }
    
    private Long milliSeconds;
    private String output;
    
    public FormatterHourAndSecondsTest(Long milliSeconds, String output) {
    		this.milliSeconds = milliSeconds;
    		this.output = output;
    }
    
	@Test
	public void formatHourAndSeconds() {
		Assert.assertEquals(output, Formatter.formatHourAndSeconds(milliSeconds));
	}
}
