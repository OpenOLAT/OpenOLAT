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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.modules.video.spi.youtube.YoutubeProvider;
import org.olat.modules.video.spi.youtube.model.YoutubeMetadata;

/**
 * 
 * Initial date: 2 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class YoutubeProviderTest {
	
	@Test
	public void testParser() throws Exception {
		InputStream in = YoutubeProviderTest.class.getResourceAsStream("youtube-ht.json");
		String json = IOUtils.toString(in, StandardCharsets.UTF_8);
		YoutubeMetadata metadata = YoutubeProvider.parseMetadata(json, "T2rGplgQ3cA");
		in.close();
		
		Assert.assertNotNull(metadata);
		Assert.assertEquals("A Different Kind of Photography", metadata.getTitle());
		Assert.assertNotNull(metadata.getDescription());
		Assert.assertEquals(700l, metadata.getDuration());
		Assert.assertEquals("Thomas Heaton", metadata.getAuthors());
		Assert.assertEquals("youtube", metadata.getLicense());
		Assert.assertEquals("https://i.ytimg.com/vi/T2rGplgQ3cA/maxresdefault.jpg", metadata.getThumbnailUrl());
	}

}
