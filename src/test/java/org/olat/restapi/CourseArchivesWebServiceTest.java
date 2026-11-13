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
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

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
import org.olat.core.util.httpclient.ConnectionUtilities;
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
	
	/**
	 * An owner of course B cannot download the archive of course A
	 * through course B.
	 * @throws InterruptedException 
	 */
	@Test
	public void getArchiveOfOtherCourse() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin ownerA = JunitTestHelper.createAndPersistRndAuthor("rest-course-exp-10");
		IdentityWithLogin ownerB = JunitTestHelper.createAndPersistRndAuthor("rest-course-exp-11");
		RepositoryEntry courseEntryA = JunitTestHelper.deployBasicCourse(ownerA.getIdentity());
		RepositoryEntry courseEntryB = JunitTestHelper.deployBasicCourse(ownerB.getIdentity());
		ExportMetadata archiveA = createArchive(courseEntryA, ownerA, false);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(ownerB);
		HttpResponse<InputStream> response = conn.execute(conn.createGet(archiveFileUri(courseEntryB, archiveA), "application/zip"));
		Assert.assertEquals(404, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.close();
	}
	
	/**
	 * An owner can download an archive of its own course.
	 * @throws InterruptedException 
	 */
	@Test
	public void getOwnArchive() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-course-exp-12");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		ExportMetadata archive = createArchive(courseEntry, owner, false);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(owner);
		HttpResponse<InputStream> response = conn.execute(conn.createGet(archiveFileUri(courseEntry, archive), "application/zip"));
		Assert.assertEquals(200, response.statusCode());
		String content = ConnectionUtilities.toString(response, "UTF-8");
		Assert.assertEquals("Archive", content);
		conn.close();
	}
	
	/**
	 * An owner which is not administrator or learn resource manager cannot download
	 * an archive restricted to administrators.
	 * @throws InterruptedException 
	 */
	@Test
	public void getOwnArchiveOnlyAdministrators() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-course-exp-13");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		ExportMetadata archive = createArchive(courseEntry, owner, true);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(owner);
		HttpResponse<InputStream> response = conn.execute(conn.createGet(archiveFileUri(courseEntry, archive), "application/zip"));
		Assert.assertEquals(403, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.close();
	}
	
	/**
	 * An administrator can download an archive restricted to administrators.
	 * @throws InterruptedException 
	 */
	@Test
	public void getArchiveOnlyAdministratorsAsAdministrator() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-course-exp-14");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		ExportMetadata archive = createArchive(courseEntry, owner, true);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		HttpResponse<InputStream> response = conn.execute(conn.createGet(archiveFileUri(courseEntry, archive), "application/zip"));
		Assert.assertEquals(200, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.close();
	}
	
	/**
	 * The list of archives doesn't show the archives restricted to administrators
	 * to a simple owner.
	 * @throws InterruptedException 
	 */
	@Test
	public void getArchivesHidesOnlyAdministrators() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-course-exp-15");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		ExportMetadata archive = createArchive(courseEntry, owner, false);
		ExportMetadata adminArchive = createArchive(courseEntry, owner, true);
		dbInstance.commitAndCloseSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses")
				.path(courseEntry.getOlatResource().getResourceableId().toString()).path("archives").build();
		
		// Owner only sees the archive without restriction
		RestConnection conn = new RestConnection(owner);
		HttpResponse<InputStream> response = conn.execute(conn.createGet(request, MediaType.APPLICATION_JSON));
		Assert.assertEquals(200, response.statusCode());
		ExportMetadataVOes metadataVoes = conn.parse(response, ExportMetadataVOes.class);
		Assertions.assertThat(Arrays.asList(metadataVoes.getExportMetadata()))
			.map(ExportMetadataVO::getKey)
			.containsExactly(archive.getKey());
		
		// Administrator sees both
		RestConnection adminConn = new RestConnection("administrator", "openolat");
		HttpResponse<InputStream> adminResponse = adminConn.execute(adminConn.createGet(request, MediaType.APPLICATION_JSON));
		Assert.assertEquals(200, adminResponse.statusCode());
		ExportMetadataVOes adminMetadataVoes = adminConn.parse(adminResponse, ExportMetadataVOes.class);
		Assertions.assertThat(Arrays.asList(adminMetadataVoes.getExportMetadata()))
			.map(ExportMetadataVO::getKey)
			.containsExactlyInAnyOrder(archive.getKey(), adminArchive.getKey());
		conn.close();
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
