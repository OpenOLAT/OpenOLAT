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
package org.olat.ims.lti13;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.ims.lti13.model.json.LineItemScore;
import org.olat.ims.lti13.model.json.Member;
import org.olat.ims.lti13.model.json.MembershipContainer;
import org.olat.ims.lti13.model.json.Result;

import com.nimbusds.jose.util.IOUtils;

/**
 * 
 * Initial date: 8 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13JsonUtilTest {
	
	@Test
	public void readMemberships() throws Exception {
		URL jsonUrl = LTI13JsonUtilTest.class.getResource("memberships.json");
		File jsonFile = new File(jsonUrl.toURI());
		String content = IOUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);

	    //JSON file to Java object
		MembershipContainer container = LTI13JsonUtil.readValue(content, MembershipContainer.class);
		Assert.assertNotNull(container);
		List<Member> members = container.getMembers();
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
		Member member = members.get(0);
		Assert.assertEquals("Aoi", member.getGivenName());
	}
	
	@Test
	public void readScoreH5p() throws Exception {
		URL jsonUrl = LTI13JsonUtilTest.class.getResource("score_h5p.json");
		File jsonFile = new File(jsonUrl.toURI());
		String content = IOUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);

	    //JSON file to Java object
		LineItemScore score = LTI13JsonUtil.readValue(content, LineItemScore.class);
		Assert.assertNotNull(score);
		Assert.assertNotNull(score.getTimestamp());
	}
	
	@Test
	public void readScoreTimeStamp1() throws Exception {
		URL jsonUrl = LTI13JsonUtilTest.class.getResource("score_timestamps_1.json");
		File jsonFile = new File(jsonUrl.toURI());
		String content = IOUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);

	    //JSON file to Java object
		LineItemScore score = LTI13JsonUtil.readValue(content, LineItemScore.class);
		Assert.assertNotNull(score);
		Assert.assertNotNull(score.getTimestamp());
	}
	
	@Test
	public void readScoreTimeStamp2() throws Exception {
		URL jsonUrl = LTI13JsonUtilTest.class.getResource("score_timestamps_2.json");
		File jsonFile = new File(jsonUrl.toURI());
		String content = IOUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);

	    //JSON file to Java object
		LineItemScore score = LTI13JsonUtil.readValue(content, LineItemScore.class);
		Assert.assertNotNull(score);
		Assert.assertNotNull(score.getTimestamp());
	}
	
	@Test
	public void readScoreTimeStamp3() throws Exception {
		URL jsonUrl = LTI13JsonUtilTest.class.getResource("score_timestamps_3.json");
		File jsonFile = new File(jsonUrl.toURI());
		String content = IOUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);

	    //JSON file to Java object
		LineItemScore score = LTI13JsonUtil.readValue(content, LineItemScore.class);
		Assert.assertNotNull(score);
		Assert.assertNotNull(score.getTimestamp());
	}
	

	@Test
	public void writeResult() throws Exception {
		Result result = new Result();
		result.setId("https://");
		result.setResultScore(null);
		result.setResultMaximum(10.0);

		String json = LTI13JsonUtil.prettyPrint(result);
		Assert.assertNotNull(json);
		Assert.assertTrue(json.contains("resultMaximum"));
	}
	
	@Test
	public void writeScore() throws Exception {
		LineItemScore score = new LineItemScore();
		score.setTimestamp(new Date());

		String json = LTI13JsonUtil.prettyPrint(score);
		Assert.assertNotNull(json);
		Assert.assertTrue(json.contains("scoreMaximum"));
		Assert.assertTrue(json.contains("scoreGiven"));
	}
}
