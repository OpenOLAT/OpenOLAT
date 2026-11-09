/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.restapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.repository.RepositoryEntry;
import org.olat.restapi.support.vo.GroupVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Description:<br>
 * Test the learning group management of a course
 * 
 * <P>
 * Initial Date:  6 mai 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class CourseGroupMgmtTest extends OlatRestTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	private static Organisation defaultUnitTestOrganisation;
	private static IdentityWithLogin defaultUnitTestAdministrator;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-service-unit-test", "Org-service-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
			defaultUnitTestAdministrator = JunitTestHelper
					.createAndPersistRndAdmin("Cur-Elem-Web", defaultUnitTestOrganisation);
		}
	}
	
	@Test
	public void getCourseGroups() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-c-g-5", defaultUnitTestOrganisation, null);
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner, defaultUnitTestOrganisation);
		BusinessGroup businessGroup1 = businessGroupService.createBusinessGroup(owner, "rest-g5", null, BusinessGroup.BUSINESS_TYPE, 0, 10, false, false, courseEntry);
		BusinessGroup businessGroup2 = businessGroupService.createBusinessGroup(owner, "rest-g6", null, BusinessGroup.BUSINESS_TYPE, 0, 10, false, false, courseEntry);
		dbInstance.commitAndCloseSession();
			
		Long courseId = courseEntry.getOlatResource().getResourceableId();
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + courseId + "/groups").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		List<GroupVO> vos = conn.parseList(response, GroupVO.class);
		Assert.assertNotNull(vos);
		Assert.assertEquals(2, vos.size());
		
		assertThat(vos)
			.map(GroupVO::getKey)
			.containsExactlyInAnyOrder(businessGroup1.getKey(), businessGroup2.getKey());
		conn.close();

	}
	
	@Test
	public void getCourseGroupsUnkownId() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

		Long courseId = 1l;
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + courseId + "/groups").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(404, response.statusCode());
		RestConnection.consume(response);
		conn.close();

	}
	
	@Test
	public void getCourseGroup() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-c-g-6", defaultUnitTestOrganisation, null);
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner, defaultUnitTestOrganisation);
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(owner, "rest-g6", null, BusinessGroup.BUSINESS_TYPE, 0, 10, false, false, courseEntry);
		dbInstance.commitAndCloseSession();
		
		Long courseId = courseEntry.getOlatResource().getResourceableId();
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + courseId + "/groups/" + businessGroup.getKey()).build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		
		GroupVO vo = conn.parse(response, GroupVO.class);
		Assert.assertNotNull(vo);
		Assert.assertEquals(businessGroup.getKey(), vo.getKey());
		conn.close();

	}
	
	@Test
	public void putCourseGroupRelation() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-c-g-7", defaultUnitTestOrganisation, null);
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner, defaultUnitTestOrganisation);
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(owner, "rest-g7", null, BusinessGroup.BUSINESS_TYPE, 0, 10, false, false, null);
		dbInstance.commitAndCloseSession();
		
		// No business group linked to this course
		List<BusinessGroup> courseGroups = businessGroupService.findBusinessGroups(new SearchBusinessGroupParams(), courseEntry, 0, -1);
		Assert.assertTrue(courseGroups.isEmpty());
		
		// Add the business group to the course
		Long courseId = courseEntry.getOlatResource().getResourceableId();
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("courses").path(courseId.toString())
				.path("groups").path(businessGroup.getKey().toString()).build();
		HttpRequest method = conn.createPut(request, MediaType.APPLICATION_JSON);
		
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		
		GroupVO responseVo = conn.parse(response, GroupVO.class);
		Assert.assertNotNull(responseVo);
		Assert.assertEquals(businessGroup.getKey(), responseVo.getKey());
		Assert.assertEquals(businessGroup.getName(), responseVo.getName());

		// The business group is linked to this course
		List<BusinessGroup> linkedGroups = businessGroupService.findBusinessGroups(new SearchBusinessGroupParams(), courseEntry, 0, -1);
		Assert.assertEquals(1, linkedGroups.size());
		Assert.assertEquals(businessGroup, linkedGroups.get(0));
		conn.close();

	}
	
	@Test
	public void postCourseGroupRelation() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-c-g-8", defaultUnitTestOrganisation, null);
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner, defaultUnitTestOrganisation);
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(owner, "rest-g8", null, BusinessGroup.BUSINESS_TYPE, 0, 10, false, false, null);
		dbInstance.commitAndCloseSession();
		
		// No business group linked to this course
		List<BusinessGroup> courseGroups = businessGroupService.findBusinessGroups(new SearchBusinessGroupParams(), courseEntry, 0, -1);
		Assert.assertTrue(courseGroups.isEmpty());
		
		// Add the business group to the course
		Long courseId = courseEntry.getOlatResource().getResourceableId();
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("courses").path(courseId.toString())
				.path("groups").path(businessGroup.getKey().toString()).build();
		HttpRequest method = conn.createPost(request, MediaType.APPLICATION_JSON);
		
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		
		GroupVO responseVo = conn.parse(response, GroupVO.class);
		Assert.assertNotNull(responseVo);
		Assert.assertEquals(businessGroup.getKey(), responseVo.getKey());
		Assert.assertEquals(businessGroup.getName(), responseVo.getName());

		// The business group is linked to this course
		List<BusinessGroup> linkedGroups = businessGroupService.findBusinessGroups(new SearchBusinessGroupParams(), courseEntry, 0, -1);
		Assert.assertEquals(1, linkedGroups.size());
		Assert.assertEquals(businessGroup, linkedGroups.get(0));
		conn.close();

	}
	
	@Test
	public void putNewCourseGroup() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-c-g-11", defaultUnitTestOrganisation, null);
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner, defaultUnitTestOrganisation);
		dbInstance.commitAndCloseSession();
		
		GroupVO vo = new GroupVO();
		vo.setName("rest-g11-mod");
		vo.setDescription("rest-g11 description");
		vo.setMinParticipants(5);
		vo.setMaxParticipants(7);
		vo.setType("LeanringGroup");
		
		Long courseId = courseEntry.getOlatResource().getResourceableId();
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("courses").path(courseId.toString())
				.path("groups").build();
		HttpRequest method = conn.createPut(request, vo, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		RestConnection.consume(response);
		assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
		
		// The business group is linked to this course
		List<BusinessGroup> linkedGroups = businessGroupService.findBusinessGroups(new SearchBusinessGroupParams(), courseEntry, 0, -1);
		Assert.assertEquals(1, linkedGroups.size());
		Assert.assertEquals(vo.getName(), linkedGroups.get(0).getName());
		Assert.assertEquals(5, linkedGroups.get(0).getMinParticipants().intValue());
		Assert.assertEquals(7, linkedGroups.get(0).getMaxParticipants().intValue());
		conn.close();

	}
	
	@Test
	public void removeCourseGroup() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-c-g-9", defaultUnitTestOrganisation, null);
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner, defaultUnitTestOrganisation);
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(owner, "rest-g9", null, BusinessGroup.BUSINESS_TYPE, 0, 10, false, false, courseEntry);
		dbInstance.commitAndCloseSession();
		
		// No business group linked to this course
		List<BusinessGroup> courseGroups = businessGroupService.findBusinessGroups(new SearchBusinessGroupParams(), courseEntry, 0, -1);
		Assert.assertEquals(1, courseGroups.size());
		Assert.assertEquals(businessGroup, courseGroups.get(0));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("courses").path(courseEntry.getOlatResource().getResourceableId().toString())
				.path("groups").path(businessGroup.getKey().toString()).build();
		HttpRequest method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		RestConnection.consume(response);
		assertEquals(200, response.statusCode());
		
		// Check removed business group
		List<BusinessGroup> removedGroups = businessGroupService.findBusinessGroups(new SearchBusinessGroupParams(), courseEntry, 0, -1);
		Assert.assertTrue(removedGroups.isEmpty());
		conn.close();

	}
	
	@Test
	public void basicSecurityDeleteCall() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin identity = JunitTestHelper.createAndPersistRndUser("rest-c-g-11");
		
		RestConnection conn = new RestConnection(identity);
		
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-c-g-11", defaultUnitTestOrganisation, null);
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner, defaultUnitTestOrganisation);
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(owner, "rest-g11", null, BusinessGroup.BUSINESS_TYPE, 0, 10, false, false, null);
		dbInstance.commitAndCloseSession();

		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("courses").path(courseEntry.getOlatResource().getResourceableId().toString())
				.path("groups").path(businessGroup.getKey().toString()).build();
		HttpRequest method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		RestConnection.consume(response);
		
		Assert.assertEquals(403, response.statusCode());
		conn.close();

	}
	
	@Test
	public void basicSecurityPutCall() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin identity = JunitTestHelper.createAndPersistRndUser("rest-c-g-10");
		RestConnection conn = new RestConnection(identity);
		
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-c-g-10", defaultUnitTestOrganisation, null);
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner, defaultUnitTestOrganisation);
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(owner, "rest-g10", null, BusinessGroup.BUSINESS_TYPE, 0, 10, false, false, null);
		dbInstance.commitAndCloseSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("courses").path(courseEntry.getOlatResource().getResourceableId().toString())
				.path("groups").path(businessGroup.getKey().toString()).build();
		HttpRequest method = conn.createPut(request, MediaType.APPLICATION_JSON);
		
		HttpResponse<InputStream> response = conn.execute(method);
		RestConnection.consume(response);
		Assert.assertEquals(403, response.statusCode());
		conn.close();

	}
}
