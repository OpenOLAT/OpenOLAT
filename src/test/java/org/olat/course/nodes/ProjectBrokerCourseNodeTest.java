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
package org.olat.course.nodes;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.olat.course.nodes.ProjectBrokerCourseNode.ProjectBrokerConfig;

/**
 * 
 * Initial date: 22 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ProjectBrokerCourseNodeTest {
	
	@Test
	public void readProjectBrokerConfig() throws URISyntaxException {
		URL configUrl = ProjectBrokerCourseNodeTest.class.getResource("projectbroker_config.xml");
		File configFile = new File(configUrl.toURI());
		ProjectBrokerConfig config = (ProjectBrokerConfig)ProjectBrokerCourseNode.getXStream().fromXML(configFile);
		Assert.assertNotNull(config);
		Assert.assertEquals(Long.valueOf(134905856l), config.getAccountGroupKey());
	}
	
	@Test
	public void readProjectBrokerTypicalMapConfig() throws URISyntaxException {
		URL configUrl = ProjectBrokerCourseNodeTest.class.getResource("projectbroker_map.xml");
		File configFile = new File(configUrl.toURI());
		@SuppressWarnings("unchecked")
		Map<String,Object> config = (Map<String,Object>)ProjectBrokerCourseNode.getXStream().fromXML(configFile);
		Assert.assertNotNull(config);
		Assert.assertEquals(Integer.valueOf(5), config.get("customeFieldSize"));
	}

}
