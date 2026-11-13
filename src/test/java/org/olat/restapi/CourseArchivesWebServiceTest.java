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
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.commons.services.export.manager.ExportMetadataDAO;
import org.olat.core.commons.services.export.restapi.ExportMetadataVO;
import org.olat.core.commons.services.export.restapi.ExportMetadataVOes;
import org.olat.core.commons.services.taskexecutor.TaskStatus;
import org.olat.core.util.DateUtils;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.archiver.CourseArchiveListController;
import org.olat.course.archiver.restapi.CourseArchiveOptionsVO;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseArchivesWebServiceTest extends OlatRestTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ExportMetadataDAO exportMetadataDao;
	
	@Test
	public void getExports() throws IOException, URISyntaxException {
		IdentityWithLogin auth = JunitTestHelper.createAndPersistRndUser("rest-course-exp-1");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(auth.getIdentity());
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses")
				.path(courseEntry.getOlatResource().getResourceableId().toString()).path("archives").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		ExportMetadataVOes metadataVoes = conn.parse(response, ExportMetadataVOes.class);
		Assert.assertNotNull(metadataVoes);
	}
	
	@Test
	public void startExportsWithDefaultOptions() throws IOException, URISyntaxException {
		IdentityWithLogin auth = JunitTestHelper.createAndPersistRndUser("rest-course-exp-2");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(auth.getIdentity());
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses")
				.path(courseEntry.getOlatResource().getResourceableId().toString()).path("archives").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		ExportMetadataVO metadataVo = conn.parse(response, ExportMetadataVO.class);
		
		Assert.assertNotNull(metadataVo);
		Assert.assertNotNull(metadataVo.getTitle());
		Assert.assertNotNull(metadataVo.getFilename());
		Assert.assertTrue(metadataVo.getTitle().contains("Basic course ("));
		Assert.assertTrue(metadataVo.getFilename().contains("Basic_course_"));
		Assert.assertTrue(TaskStatus.newTask.name().equals(metadataVo.getTaskStatus()) || TaskStatus.inWork.name().equals(metadataVo.getTaskStatus()));
	}
	
	@Test
	public void postStartExportsWithArchiveOptions() throws IOException, URISyntaxException {
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
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, archiveOptions);
		method.addHeader("Accept-Language", "en");
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		ExportMetadataVO metadataVo = conn.parse(response, ExportMetadataVO.class);
		
		Assert.assertNotNull(metadataVo);
		Assert.assertNotNull(metadataVo.getTitle());
		Assert.assertNotNull(metadataVo.getFilename());
		Assert.assertTrue(metadataVo.getTitle().contains("Placeholder name"));
		Assert.assertTrue(metadataVo.getFilename().contains("Random_filename.zip"));
		Assert.assertTrue(TaskStatus.newTask.name().equals(metadataVo.getTaskStatus()) || TaskStatus.inWork.name().equals(metadataVo.getTaskStatus()));
	}
	
	/**
	 * An owner of course B cannot download the archive of course A
	 * through course B.
	 */
	@Test
	public void getArchiveOfOtherCourse() throws IOException, URISyntaxException {
		IdentityWithLogin ownerA = JunitTestHelper.createAndPersistRndAuthor("rest-course-exp-10");
		IdentityWithLogin ownerB = JunitTestHelper.createAndPersistRndAuthor("rest-course-exp-11");
		RepositoryEntry courseEntryA = JunitTestHelper.deployBasicCourse(ownerA.getIdentity());
		RepositoryEntry courseEntryB = JunitTestHelper.deployBasicCourse(ownerB.getIdentity());
		ExportMetadata archiveA = createArchive(courseEntryA, ownerA, false);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(ownerB);
		HttpResponse response = conn.execute(conn.createGet(archiveFileUri(courseEntryB, archiveA), "application/zip", true));
		Assert.assertEquals(404, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}
	
	/**
	 * An owner can download an archive of its own course.
	 */
	@Test
	public void getOwnArchive() throws IOException, URISyntaxException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-course-exp-12");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		ExportMetadata archive = createArchive(courseEntry, owner, false);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(owner);
		HttpResponse response = conn.execute(conn.createGet(archiveFileUri(courseEntry, archive), "application/zip", true));
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
		Assert.assertEquals("Archive", content);
	}
	
	/**
	 * An owner which is not administrator or learn resource manager cannot download
	 * an archive restricted to administrators.
	 */
	@Test
	public void getOwnArchiveOnlyAdministrators() throws IOException, URISyntaxException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-course-exp-13");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		ExportMetadata archive = createArchive(courseEntry, owner, true);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(owner);
		HttpResponse response = conn.execute(conn.createGet(archiveFileUri(courseEntry, archive), "application/zip", true));
		Assert.assertEquals(403, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}
	
	/**
	 * An administrator can download an archive restricted to administrators.
	 */
	@Test
	public void getArchiveOnlyAdministratorsAsAdministrator() throws IOException, URISyntaxException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-course-exp-14");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		ExportMetadata archive = createArchive(courseEntry, owner, true);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		HttpResponse response = conn.execute(conn.createGet(archiveFileUri(courseEntry, archive), "application/zip", true));
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}
	
	/**
	 * The list of archives doesn't show the archives restricted to administrators
	 * to a simple owner.
	 */
	@Test
	public void getArchivesHidesOnlyAdministrators() throws IOException, URISyntaxException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-course-exp-15");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		ExportMetadata archive = createArchive(courseEntry, owner, false);
		ExportMetadata adminArchive = createArchive(courseEntry, owner, true);
		dbInstance.commitAndCloseSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses")
				.path(courseEntry.getOlatResource().getResourceableId().toString()).path("archives").build();
		
		// Owner only sees the archive without restriction
		RestConnection conn = new RestConnection(owner);
		HttpResponse response = conn.execute(conn.createGet(request, MediaType.APPLICATION_JSON, true));
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		ExportMetadataVOes metadataVoes = conn.parse(response, ExportMetadataVOes.class);
		Assertions.assertThat(Arrays.asList(metadataVoes.getExportMetadata()))
			.map(ExportMetadataVO::getKey)
			.containsExactly(archive.getKey());
		
		// Administrator sees both
		RestConnection adminConn = new RestConnection("administrator", "openolat");
		HttpResponse adminResponse = adminConn.execute(adminConn.createGet(request, MediaType.APPLICATION_JSON, true));
		Assert.assertEquals(200, adminResponse.getStatusLine().getStatusCode());
		ExportMetadataVOes adminMetadataVoes = adminConn.parse(adminResponse, ExportMetadataVOes.class);
		Assertions.assertThat(Arrays.asList(adminMetadataVoes.getExportMetadata()))
			.map(ExportMetadataVO::getKey)
			.containsExactlyInAnyOrder(archive.getKey(), adminArchive.getKey());
	}
	
	private URI archiveFileUri(RepositoryEntry courseEntry, ExportMetadata archive) {
		return UriBuilder.fromUri(getContextURI()).path("/repo/courses")
				.path(courseEntry.getOlatResource().getResourceableId().toString()).path("archives")
				.path(archive.getKey().toString()).path("file").build();
	}
	
	private ExportMetadata createArchive(RepositoryEntry courseEntry, IdentityWithLogin creator, boolean onlyAdministrators)
	throws IOException {
		String dir = "/unittest_rest_archives/" + UUID.randomUUID();
		VFSContainer container = VFSManager.olatRootContainer(dir);
		VFSLeaf leaf = container.createChildLeaf("archive.zip");
		try(OutputStream out = leaf.getOutputStream(false)) {
			out.write("Archive".getBytes(StandardCharsets.UTF_8));
		}
		
		ExportMetadata metadata = exportMetadataDao.createMetadata("Archive", null, "archive.zip", ArchiveType.COMPLETE,
				DateUtils.addDays(new Date(), 7), onlyAdministrators, courseEntry, courseEntry.getOlatResource(),
				CourseArchiveListController.COURSE_ARCHIVE_SUB_IDENT, creator.getIdentity(), null);
		metadata.setFilePath(dir + "/archive.zip");
		return exportMetadataDao.updateMetadata(metadata);
	}
}
