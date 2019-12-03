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
package org.olat.modules.webFeed.dispatching;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


/**
 * 
 * Initial date: 3 déc. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */

@RunWith(Parameterized.class)
public class PathTest {
	
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
        	{ "720898/Wm3T3q/91225001686048/_/feed.rss", 11, 91225001686048l, "Wm3T3q", 720898l, null, null, null },
        	{ "720898/Wm3T3q/91225001686048/_/feed.xml", 11, 91225001686048l, "Wm3T3q", 720898l, null, null, null },
        	{ "720898/Wm3T3q/91225001686050/_/sropenpg_1_90970840170422/media/demo-video.mp4", 13, 91225001686050l, "Wm3T3q", 720898l, null, null, "demo-video.mp4" },
        	
        	{ "coursenode/720898/Wm3T3q/91163349354954/90987128749136/91163349354956/_/feed.rss", 31, 91163349354956l, "Wm3T3q", 720898l, 91163349354954l, "90987128749136", null },
        	{ "coursenode/720898/Wm3T3q/91163349354954/90987128749136/91163349354956/_/feed.xml", 31, 91163349354956l, "Wm3T3q", 720898l, 91163349354954l, "90987128749136", null },
        	{ "coursenode/720898/Wm3T3q/91163349354954/90987128749136/91163349354956/_/sropenpg_1_90987128749491/media/shark.m4v", 33, 91163349354956l, "Wm3T3q", 720898l, 91163349354954l, "90987128749136", "shark.m4v" }
        });
    }
	
	
	private String url;
	private int type;
	private String token;
	private Long feedId;
	private Long identityKey;
	private Long courseId;
	private String courseNodeId;
	private String filename;
	
	public PathTest(String url, int type, Long feedId, String token, Long identityKey, Long courseId, String courseNodeId, String filename) {
		this.url = url;
		this.type = type;
		this.feedId = feedId;
		this.token = token;
		this.courseId = courseId;
		this.courseNodeId = courseNodeId;
		this.identityKey = identityKey;
		this.filename = filename;
	}
	
	
	@Test
	public void testPath() throws InvalidPathException {
		Path path = new Path(url);
		path.compile();
		
		Assert.assertEquals(token, path.getToken());
		Assert.assertEquals(feedId, path.getFeedId());
		Assert.assertEquals(identityKey, path.getIdentityKey());

		Assert.assertEquals(courseId, path.getCourseId());
		Assert.assertEquals(courseNodeId, path.getNodeId());
		
		Assert.assertEquals(filename, path.getItemFileName());
		
		Assert.assertEquals(type, path.getType());
	}

}
