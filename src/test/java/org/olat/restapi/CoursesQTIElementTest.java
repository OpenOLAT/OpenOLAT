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

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.xml.XMLFactories;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.restapi.support.vo.elements.TestReportConfigVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 * Initial date: 19 mai 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoursesQTIElementTest extends OlatRestTestCase {

	private static final Logger log = Tracing.createLoggerFor(CoursesQTIElementTest.class);
	
	@Autowired
	private DB dbInstance;
	
	private ICourse course;
	
	/**
	 * SetUp is called before each test.
	 */
	@Before
	public void setUp() throws Exception {
		try {
			Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
			URL courseUrl = OlatRestTestCase.class.getResource("file_resources/course_with_qti21.zip");
			RepositoryEntry courseEntry = JunitTestHelper.deployCourse(admin, "QTI 2.1 Course", courseUrl);
			course = CourseFactory.loadCourse(courseEntry);
			dbInstance.closeSession();
		} catch (Exception e) {
			log.error("Exception in setUp(): " + e);
		}
	}
	
	@Test
	public void changeResultsSettings()
	throws IOException, SAXException, URISyntaxException, ParserConfigurationException {
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		// get the editor tree model
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course.getResourceableId() + "/editortreemodel").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_XML, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		String editorTreeModel = EntityUtils.toString(response.getEntity());
		Assert.assertNotNull(editorTreeModel);
		
		// collect the identifier of the test course element
		Document document = XMLFactories.newDocumentBuilderFactory()
				.newDocumentBuilder()
				.parse(new InputSource(new StringReader(editorTreeModel)));
		NodeList courseNodeList = document.getElementsByTagName("cn");
		List<String> nodeIdentifiers = new ArrayList<>();
		for(int i=courseNodeList.getLength(); i-->0; ) {
			Element courseNodeElement = (Element)courseNodeList.item(i);
			String cl = courseNodeElement.getAttribute("class");
			if("org.olat.course.nodes.IQTESTCourseNode".equals(cl)) {
				NodeList identList = courseNodeElement.getElementsByTagName("ident");
				if(identList.getLength() == 1) {
					String ident = getCharacterDataFromElement((Element)identList.item(0));
					nodeIdentifiers.add(ident);
				}
			}
			
		}
		
		Assert.assertEquals(1, nodeIdentifiers.size());
		
		// get the configuration node
		URI configUri = getElementsUri(course)
				.path("test")
				.path(nodeIdentifiers.get(0))
				.path("configuration")
				.path("report").build();
		
		HttpGet getConfig = conn.createGet(configUri, MediaType.APPLICATION_JSON, true);
		HttpResponse configResponse = conn.execute(getConfig);
		Assert.assertEquals(200, configResponse.getStatusLine().getStatusCode());
		TestReportConfigVO testConfig = conn.parse(configResponse, TestReportConfigVO.class);
		Assert.assertNotNull(testConfig);
		
		testConfig.setShowResultsAfterFinish(Boolean.TRUE);
		testConfig.setShowResultsDependendOnDate(editorTreeModel);
		testConfig.setSummaryPresentation("metadata,sectionsSummary,questionSummary,userSolutions,correctSolutions");
		
		// update the configuration node
		HttpPut putConfig = conn.createPut(configUri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(putConfig, testConfig);
		
		HttpResponse updateConfigResponse = conn.execute(putConfig);
		Assert.assertEquals(200, updateConfigResponse.getStatusLine().getStatusCode());
		TestReportConfigVO updatedConfig = conn.parse(updateConfigResponse, TestReportConfigVO.class);
		Assert.assertNotNull(updatedConfig);
	}
	
	private UriBuilder getCoursesUri() {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("courses");
	}
	
	private UriBuilder getElementsUri(ICourse course) {
		return getCoursesUri().path(course.getResourceableId().toString()).path("elements");
	}
	
    private static String getCharacterDataFromElement(Element e) {
    	StringBuilder sb = new StringBuilder();
    	for(Node child = e.getFirstChild(); child != null; child = child.getNextSibling()) {
    		if (child instanceof CharacterData) {
                sb.append(((CharacterData)child).getData());
            }
    	}
        return sb.toString();
    }
	
}
