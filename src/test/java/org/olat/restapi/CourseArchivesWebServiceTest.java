/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.restapi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.services.export.restapi.ExportMetadataVO;
import org.olat.core.commons.services.export.restapi.ExportMetadataVOes;
import org.olat.core.commons.services.taskexecutor.TaskStatus;
import org.olat.course.archiver.restapi.CourseArchiveOptionsVO;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;

/**
 * 
 * Initial date: 27 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseArchivesWebServiceTest extends OlatRestTestCase {
	
	@Test
	public void getExports() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin auth = JunitTestHelper.createAndPersistRndUser("rest-course-exp-1");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(auth.getIdentity());
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses")
				.path(courseEntry.getOlatResource().getResourceableId().toString()).path("archives").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		ExportMetadataVOes metadataVoes = conn.parse(response, ExportMetadataVOes.class);
		Assert.assertNotNull(metadataVoes);
		conn.close();
	}
	
	@Test
	public void startExportsWithDefaultOptions() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin auth = JunitTestHelper.createAndPersistRndUser("rest-course-exp-2");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(auth.getIdentity());
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses")
				.path(courseEntry.getOlatResource().getResourceableId().toString()).path("archives").build();
		HttpRequest method = conn.createPut(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		ExportMetadataVO metadataVo = conn.parse(response, ExportMetadataVO.class);
		
		Assert.assertNotNull(metadataVo);
		Assert.assertNotNull(metadataVo.getTitle());
		Assert.assertNotNull(metadataVo.getFilename());
		Assert.assertTrue(metadataVo.getTitle().contains("Basic course ("));
		Assert.assertTrue(metadataVo.getFilename().contains("Basic_course_"));
		Assert.assertTrue(TaskStatus.newTask.name().equals(metadataVo.getTaskStatus()) || TaskStatus.inWork.name().equals(metadataVo.getTaskStatus()));
		conn.close();
	}
	
	@Test
	public void postStartExportsWithArchiveOptions() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin auth = JunitTestHelper.createAndPersistRndUser("rest-course-exp-2");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(auth.getIdentity());
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		CourseArchiveOptionsVO archiveOptions = new CourseArchiveOptionsVO();
		archiveOptions.setTitle("Placeholder name");
		archiveOptions.setFilename("Random_filename.zip");
		archiveOptions.setLogFilesAuthors(Boolean.FALSE);
		archiveOptions.setLogFilesUsers(Boolean.FALSE);
		archiveOptions.setLogFilesStatistics(Boolean.FALSE);

		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses")
				.path(courseEntry.getOlatResource().getResourceableId().toString()).path("archives").build();
		HttpRequest method = conn.createPost(request, archiveOptions, MediaType.APPLICATION_JSON);
		
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		ExportMetadataVO metadataVo = conn.parse(response, ExportMetadataVO.class);
		
		Assert.assertNotNull(metadataVo);
		Assert.assertNotNull(metadataVo.getTitle());
		Assert.assertNotNull(metadataVo.getFilename());
		Assert.assertTrue(metadataVo.getTitle().contains("Placeholder name"));
		Assert.assertTrue(metadataVo.getFilename().contains("Random_filename.zip"));
		Assert.assertTrue(TaskStatus.newTask.name().equals(metadataVo.getTaskStatus()) || TaskStatus.inWork.name().equals(metadataVo.getTaskStatus()));
		conn.close();
	}
	
	

}
