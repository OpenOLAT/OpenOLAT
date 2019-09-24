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
package org.olat.modules.video.spi.youtube;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * 
 * Initial date: 2 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Parameterized.class)
public class YoutubeVideoIdTest {
	
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
				{ "youtube.com/v/T2rGplgQ3cA" },
				{ "youtube.com/vi/T2rGplgQ3cA" },
				{ "youtube.com/?v=T2rGplgQ3cA" },
				{ "youtube.com/?vi=T2rGplgQ3cA" },
				{ "youtube.com/watch?v=T2rGplgQ3cA" },
				{ "youtube.com/watch?vi=T2rGplgQ3cA" },
				{ "youtu.be/T2rGplgQ3cA" },
				{ "youtube.com/embed/T2rGplgQ3cA" },
				{ "youtube.com/embed/T2rGplgQ3cA" },
				{ "www.youtube.com/v/T2rGplgQ3cA" },
 /* 10 */		{ "http://www.youtube.com/v/T2rGplgQ3cA" },
				{ "https://www.youtube.com/v/T2rGplgQ3cA" },
				{ "youtube.com/watch?v=T2rGplgQ3cA&wtv=wtv" },
				{ "http://www.youtube.com/watch?dev=inprogress&v=T2rGplgQ3cA&feature=related" },
				{ "https://m.youtube.com/watch?v=T2rGplgQ3cA" },
				//{ "http://www.youtube.com/e/T2rGplgQ3cA" },
				{ "http://www.youtube.com/watch?feature=player_embedded&v=T2rGplgQ3cA" },
				{ "http://www.youtube.com/v/T2rGplgQ3cA?fs=1&hl=en_US&rel=0" },
				{ "http://www.youtube.com/embed/T2rGplgQ3cA?rel=0" },
				{ "http://www.youtube.com/watch?v=T2rGplgQ3cA&feature=feedrec_grec_index" },
/* 20 */		{ "http://www.youtube.com/watch?v=T2rGplgQ3cA" },
				{ "http://youtu.be/T2rGplgQ3cA" },
				//{ "http://www.youtube.com/watch?v=T2rGplgQ3cA#t=0m10s" },
				{ "http://youtu.be/T2rGplgQ3cA" },
				{ "http://www.youtube.com/embed/T2rGplgQ3cA" },
				{ "http://www.youtube.com/v/T2rGplgQ3cA" },
				{ "http://www.youtube.com/watch?v=T2rGplgQ3cA" },
				{ "http://www.youtube-nocookie.com/v/T2rGplgQ3cA?version=3&hl=en_US&rel=0" }
        });
    }
    
    private String url;
    
    public YoutubeVideoIdTest(String url) {
    	this.url = url;
    }

    @Test
    public void extractingVideoIdFromUrlShouldReturnVideoId() {
    	String videoId = YoutubeProvider.getVideoId(url);
        Assert.assertEquals("Unable to extract correct video id from url " + url, "T2rGplgQ3cA", videoId);
    }

}
