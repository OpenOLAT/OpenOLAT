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
package org.olat.ims.qti21.model.xml;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


/**
 * 
 * Initial date: 18 mai 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Parameterized.class)
public class TestFeedbackBuilderTest {
	
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { true, "<p>\n </p>" },
                { true, "\n" },
                { true, "<span></span>" },
                { false, "Hello" },
                { false, "<span class='olatFlashMovieViewer'></span>" },
                { false, "<img src='img.jpg' />" },
                { false, "<img src='img.jpg'></img>" },
                { false, "<p>Hello</p>" }  
        });
    }
    
    private boolean empty;
    private String input;
    
    public TestFeedbackBuilderTest(boolean empty, String input) {
    	this.empty = empty;
    	this.input = input;
    }
    
    @Test
    public void isEmpty() {
    	boolean output = TestFeedbackBuilder.isEmpty(input);
    	Assert.assertEquals(empty, output);
    }
}
