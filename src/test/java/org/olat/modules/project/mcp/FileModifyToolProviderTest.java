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
package org.olat.modules.project.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.mcp.McpGrants;
import org.olat.mcp.manager.McpJsonSchemaValidator;
import org.olat.mcp.manager.McpToolRegistry;
import org.olat.mcp.tool.McpInvalidArgumentException;
import org.olat.mcp.tool.McpPermissionException;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjActivity.Action;
import org.olat.modules.project.ProjActivitySearchParams;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactInfoParams;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjFileSearchParams;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.modules.todo.ToDoStatus;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.json.schema.JsonSchemaValidator;

/**
 * Integration tests of the tool file_modify: the rights per action and
 * project role, the upload of text and binary content, the metadata round
 * trips and the side effects (activity log, publisher news).
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class FileModifyToolProviderTest extends OlatTestCase {

	private static final McpGrants GRANTS = new McpGrants(Set.of(FileModifyToolProvider.NAME), null, List.of("test"));
	private static final McpJsonSchemaValidator VALIDATOR = new McpJsonSchemaValidator(new ObjectMapper());

	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private FileModifyToolProvider sut;
	@Autowired
	private ProjectToolSupport support;

	@Test
	public void definition() {
		Assert.assertEquals("file_modify", sut.getToolName());
		Assert.assertEquals(ProjectMcpPack.NAME, sut.getPack());
		Assert.assertEquals(McpToolScope.WRITE, sut.getScope());
		Assert.assertTrue(sut.getDefinition().destructive());
		Assert.assertFalse(sut.getDefinition().idempotent());
		assertThat(sut.getDefinition().description()).contains("file_query").contains("project_query").contains("contentBase64")
			.contains("milestones cannot be linked").doesNotContain("appointments, milestones, decisions");
		Assert.assertTrue(sut.isAvailable());
	}

	@Test
	public void inputSchema() {
		Map<String,Object> schema = sut.getDefinition().inputSchema();
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "create", "projectKey", 3, "filename", "a.md", "content", "# md",
				"title", "T", "description", "D", "tags", List.of("a"), "members", List.of("jdoe"), "links", List.of(Map.of("type", "note", "key", 5)))).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "create", "projectKey", 3, "filename", "a.pdf", "contentBase64", "AAAA")).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "update", "fileKey", 1, "filename", "b", "title", "", "links", List.of())).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "delete", "fileKey", 1)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("fileKey", 1)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "archive", "fileKey", 1)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("action", "update", "fileKey", 1, "status", "deleted")).valid());
	}

	@Test
	public void filenameAndContentHelpers() {
		Assert.assertEquals("report.pdf", FileModifyToolProvider.filename(Map.of("filename", " report.pdf "), null));
		Assert.assertEquals("report.pdf", FileModifyToolProvider.filename(Map.of("filename", "report"), "old.pdf"));
		Assert.assertEquals("report.PDF", FileModifyToolProvider.filename(Map.of("filename", "report.PDF"), "old.pdf"));
		Assert.assertNull(FileModifyToolProvider.filename(Map.of("filename", "  "), null));
		// V14: the limit applies to the name with the kept suffix
		String fits = "b".repeat(FileModifyToolProvider.FILENAME_MAX_LENGTH - 4);
		Assert.assertEquals(fits + ".pdf", FileModifyToolProvider.filename(Map.of("filename", fits), "old.pdf"));
		McpInvalidArgumentException tooLong = Assert.assertThrows(McpInvalidArgumentException.class,
				() -> FileModifyToolProvider.filename(Map.of("filename", fits + "b"), "old.pdf"));
		assertThat(tooLong.getMessage()).contains("with the extension");
		for(String invalid:List.of("../x.txt", ".hidden", "a/b.txt", "a:b.txt", "x".repeat(255))) {
			try {
				FileModifyToolProvider.filename(Map.of("filename", invalid), null);
				Assert.fail("Invalid filename accepted: " + invalid);
			} catch (IllegalArgumentException e) {
				assertThat(e.getMessage()).contains("filename");
			}
		}
		Assert.assertArrayEquals("ü".getBytes(StandardCharsets.UTF_8), FileModifyToolProvider.content(Map.of("content", "ü")));
		Assert.assertArrayEquals(new byte[] { 1, 2, 3 }, FileModifyToolProvider.content(Map.of("contentBase64", Base64.getEncoder().encodeToString(new byte[] { 1, 2, 3 }))));
		Assert.assertNull(FileModifyToolProvider.content(Map.of()));
		try {
			FileModifyToolProvider.content(Map.of("content", "a", "contentBase64", "YQ=="));
			Assert.fail("Both contents accepted");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage()).contains("not both");
		}
		try {
			FileModifyToolProvider.content(Map.of("contentBase64", "not base64 !!"));
			Assert.fail("Invalid base64 accepted");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage()).contains("base64");
		}
	}

	@Test
	public void invalidActions() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-1");

		McpToolResult missing = invoke(context(owner), Map.of());
		Assert.assertTrue(missing.error());
		assertThat(missing.text()).contains("action is required");

		McpToolResult unknown = invoke(context(owner), Map.of("action", "archive"));
		Assert.assertTrue(unknown.error());
		assertThat(unknown.text()).contains("Unknown action archive");

		McpToolResult noKey = invoke(context(owner), Map.of("action", "update", "title", "x"));
		Assert.assertTrue(noKey.error());
		assertThat(noKey.text()).contains("fileKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknownKey = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("action", "delete", "fileKey", Long.valueOf(-9))));
		Assert.assertEquals("file with key -9", unknownKey.getObjectDescription());
		Assert.assertEquals("view the file", unknownKey.getPermission());

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknownProject = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("action", "create", "projectKey", Long.valueOf(-9), "filename", "x.txt", "content", "x")));
		Assert.assertEquals("project with key -9", unknownProject.getObjectDescription());
		Assert.assertEquals("create files", unknownProject.getPermission());
	}

	// create

	@Test
	public void create_textFile() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-2");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();
		Date before = setPublisherNewsInPast(project);

		McpToolResult result = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(),
				"filename", "minutes.md", "content", "# Minutes\n\nü"));
		Map<String,Object> item = item(result);
		dbInstance.commitAndCloseSession();

		ProjFile file = projectService.getFile(() -> (Long)item.get("key"));
		Assert.assertEquals("minutes.md", file.getVfsMetadata().getFilename());
		Assert.assertEquals(project.getKey(), file.getArtefact().getProject().getKey());
		Assert.assertEquals(owner, file.getArtefact().getCreator());
		Assert.assertEquals("# Minutes\n\nü", readContent(file));
		Assert.assertEquals("minutes.md", item.get("filename"));
		Assert.assertEquals("text/markdown", item.get("mimeType"));
		Assert.assertEquals(Long.valueOf("# Minutes\n\nü".getBytes(StandardCharsets.UTF_8).length), item.get("size"));
		Assert.assertFalse(item.containsKey("title"));
		Assert.assertEquals(List.of(), item.get("tags"));
		Assert.assertEquals(List.of(), item.get("links"));
		assertThat(persons(item, "members")).containsExactly(username(owner));
		assertThat(result.text()).startsWith("Uploaded: minutes.md (key " + file.getKey());
		assertThat(actions(project, owner)).contains(Action.fileUpload);
		assertNewsAfter(project, before);
	}

	@Test
	public void create_binaryWithMetadataAndLinks() throws IOException {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-3");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-4");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjNote note = createNote(owner, project, "Note");
		ProjToDo toDo = createToDo(owner, project, "Follow up");
		byte[] pdf = FileQueryToolProviderTest.resource("handInTopic1.pdf");
		dbInstance.commitAndCloseSession();

		Map<String,Object> arguments = new HashMap<>();
		arguments.put("action", "create");
		arguments.put("projectKey", project.getKey());
		arguments.put("filename", "paper.pdf");
		arguments.put("contentBase64", Base64.getEncoder().encodeToString(pdf));
		arguments.put("title", "The paper");
		arguments.put("description", "Hand in");
		arguments.put("tags", List.of("paper", "handin"));
		arguments.put("members", List.of(username(owner), participant.getUser().getEmail()));
		arguments.put("links", List.of(Map.of("type", "note", "key", note.getKey()), Map.of("type", "toDo", "key", toDo.getToDoTask().getKey())));
		McpToolResult result = invoke(context(participant), arguments);
		Map<String,Object> item = item(result);
		dbInstance.commitAndCloseSession();

		Long fileKey = (Long)item.get("key");
		ProjFile file = projectService.getFile(() -> fileKey);
		Assert.assertEquals("paper.pdf", file.getVfsMetadata().getFilename());
		Assert.assertEquals("The paper", file.getVfsMetadata().getTitle());
		Assert.assertEquals("Hand in", file.getVfsMetadata().getComment());
		Assert.assertEquals(pdf.length, file.getVfsMetadata().getFileSize());
		Assert.assertEquals(participant, file.getArtefact().getCreator());
		Assert.assertArrayEquals(pdf, readBytes(file));
		Assert.assertEquals("application/pdf", item.get("mimeType"));
		Assert.assertEquals("The paper", item.get("title"));
		Assert.assertEquals(List.of("handin", "paper"), item.get("tags"));
		assertThat(persons(item, "members")).containsExactlyInAnyOrder(username(owner), username(participant));
		assertThat(links(item)).containsExactly("toDo:" + toDo.getToDoTask().getKey(), "note:" + note.getKey());
		assertThat(projectService.getLinkedArtefacts(file.getArtefact())).extracting(ProjArtefact::getKey)
				.containsExactlyInAnyOrder(note.getArtefact().getKey(), toDo.getArtefact().getKey());
		assertThat(actions(project, participant)).contains(Action.fileUpload, Action.fileTagsUpdate, Action.fileMemberAdd, Action.fileReferenceAdd);
		assertThat(result.text()).startsWith("Uploaded: paper.pdf (key " + fileKey + ", \"The paper\", application/pdf, " + pdf.length + " bytes");
	}

	@Test
	public void create_permissions() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-5");
		Identity client = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-6");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-7");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, client, Set.of(ProjectRole.client), null);
		dbInstance.commitAndCloseSession();

		for(Identity identity:List.of(client, stranger)) {
			try {
				invoke(context(identity), Map.of("action", "create", "projectKey", project.getKey(), "filename", "x.txt", "content", "x"));
				Assert.fail("Cannot create files: " + identity.getKey());
			} catch (McpPermissionException e) {
				Assert.assertEquals("create files", e.getPermission());
				Assert.assertEquals("project with key " + project.getKey(), e.getObjectDescription());
			}
		}
		projectService.setStatusDeleted(owner, new ProjectBCFactory(), project);
		dbInstance.commitAndCloseSession();
		// V19: a state error, not a permission error
		McpToolResult deletedProject = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "filename", "x.txt", "content", "x"));
		Assert.assertTrue(deletedProject.error());
		assertThat(deletedProject.text()).startsWith("The project " + project.getKey() + " is deleted");
	}

	@Test
	public void create_invalidArguments() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-8");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-9");
		ProjProject project = createProject(owner);
		ProjProject otherProject = createProject(stranger);
		ProjFile foreignFile = createTextFile(stranger, otherProject, "foreign.txt", "x");
		createTextFile(owner, project, "exists.txt", "x");
		dbInstance.commitAndCloseSession();
		long filesBefore = countFiles(project);

		McpToolResult noFilename = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "content", "x"));
		Assert.assertTrue(noFilename.error());
		assertThat(noFilename.text()).contains("filename");

		McpToolResult noContent = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "filename", "x.txt"));
		Assert.assertTrue(noContent.error());
		assertThat(noContent.text()).contains("contentBase64");

		McpToolResult badName = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "filename", "../x.txt", "content", "x"));
		Assert.assertTrue(badName.error());
		assertThat(badName.text()).contains("not valid");

		McpToolResult exists = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "filename", "exists.txt", "content", "x"));
		Assert.assertTrue(exists.error());
		assertThat(exists.text()).contains("exists.txt").contains("exists in this project");

		McpToolResult badBase64 = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "filename", "x.bin", "contentBase64", "%%%"));
		Assert.assertTrue(badBase64.error());
		assertThat(badBase64.text()).contains("base64");

		McpToolResult notMember = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "filename", "x.txt", "content", "x",
				"members", List.of(username(stranger))));
		Assert.assertTrue(notMember.error());
		Assert.assertEquals("Invalid argument: " + username(stranger) + " is not a valid member for this file.", notMember.text());

		McpToolResult foreign = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "filename", "x.txt", "content", "x",
				"links", List.of(Map.of("type", "file", "key", foreignFile.getKey()))));
		Assert.assertTrue(foreign.error());
		assertThat(foreign.text()).endsWith("is not an active file of this project.");

		// nothing was written
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(filesBefore, countFiles(project));
	}

	// update

	@Test
	public void update_onlySuppliedFields() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-10");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-11");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjFile file = createTextFile(owner, project, "report.md", "# Report");
		projectService.updateFile(owner, file, null, "Title", "Description");
		ProjNote linked = createNote(owner, project, "Linked");
		projectService.updateTags(owner, file.getArtefact(), List.of("keep"));
		projectService.linkArtefacts(owner, file.getArtefact(), linked.getArtefact());
		dbInstance.commitAndCloseSession();
		Date before = setPublisherNewsInPast(project);

		// a participant who is not the creator may edit
		McpToolResult result = invoke(context(participant), Map.of("action", "update", "fileKey", file.getKey(), "description", "Description v2"));
		Map<String,Object> item = item(result);
		dbInstance.commitAndCloseSession();

		ProjFile reloaded = projectService.getFile(file);
		Assert.assertEquals("report.md", reloaded.getVfsMetadata().getFilename());
		Assert.assertEquals("Title", reloaded.getVfsMetadata().getTitle());
		Assert.assertEquals("Description v2", reloaded.getVfsMetadata().getComment());
		Assert.assertEquals("# Report", readContent(reloaded));
		Assert.assertEquals(List.of("keep"), item.get("tags"));
		// the service adds the editor of the metadata as an involved member, as the UI does
		assertThat(persons(item, "members")).containsExactlyInAnyOrder(username(owner), username(participant));
		assertThat(links(item)).containsExactly("note:" + linked.getKey());
		assertThat(result.text()).startsWith("Updated: report.md (key " + file.getKey());
		assertThat(actions(project, participant)).contains(Action.fileContentUpdate);
		assertNewsAfter(project, before);
	}

	@Test
	public void update_renameAndMetadata() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-12");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-13");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjFile file = createTextFile(owner, project, "draft.md", "# Draft");
		projectService.updateFile(owner, file, null, "Title", "Description");
		createTextFile(owner, project, "taken.md", "x");
		ProjNote oldLink = createNote(owner, project, "Old");
		ProjNote newLink = createNote(owner, project, "New");
		ProjToDo toDo = createToDo(owner, project, "ToDo");
		projectService.updateTags(owner, file.getArtefact(), List.of("old"));
		projectService.linkArtefacts(owner, file.getArtefact(), oldLink.getArtefact());
		dbInstance.commitAndCloseSession();

		// a rename to an existing name is refused
		McpToolResult taken = invoke(context(owner), Map.of("action", "update", "fileKey", file.getKey(), "filename", "taken"));
		Assert.assertTrue(taken.error());
		assertThat(taken.text()).contains("taken.md").contains("exists in this project");

		Map<String,Object> arguments = new HashMap<>();
		arguments.put("action", "update");
		arguments.put("fileKey", file.getKey());
		arguments.put("filename", "final");
		arguments.put("title", "");
		arguments.put("tags", List.of("new"));
		arguments.put("members", List.of(username(participant)));
		arguments.put("links", List.of(Map.of("type", "note", "key", newLink.getKey()), Map.of("type", "toDo", "key", toDo.getToDoTask().getKey())));
		long contentUpdatesBefore = actions(project, owner).stream().filter(Action.fileContentUpdate::equals).count();
		Map<String,Object> item = item(invoke(context(owner), arguments));
		dbInstance.commitAndCloseSession();
		// F8, accepted: the rename runs alone so a failed rename commits nothing (ProjectService.updateFile marks
		// the news, which commits); a rename plus a title change therefore writes two activities, the UI one
		Assert.assertEquals(contentUpdatesBefore + 2, actions(project, owner).stream().filter(Action.fileContentUpdate::equals).count());

		ProjFile reloaded = projectService.getFile(file);
		// the suffix of the file is kept on rename, as in the UI
		Assert.assertEquals("final.md", reloaded.getVfsMetadata().getFilename());
		Assert.assertNull(reloaded.getVfsMetadata().getTitle());
		Assert.assertEquals("Description", reloaded.getVfsMetadata().getComment());
		Assert.assertEquals("# Draft", readContent(reloaded));
		Assert.assertEquals("final.md", item.get("filename"));
		Assert.assertFalse(item.containsKey("title"));
		Assert.assertEquals(List.of("new"), item.get("tags"));
		assertThat(persons(item, "members")).containsExactly(username(participant));
		assertThat(links(item)).containsExactly("toDo:" + toDo.getToDoTask().getKey(), "note:" + newLink.getKey());
		assertThat(actions(project, owner)).contains(Action.fileTagsUpdate, Action.fileMemberAdd, Action.fileMemberRemove,
				Action.fileReferenceAdd, Action.fileReferenceRemove);

		// empty lists clear tags and links, the members stay
		Map<String,Object> cleared = item(invoke(context(owner), Map.of("action", "update", "fileKey", file.getKey(),
				"tags", List.of(), "links", List.of(), "description", "")));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(List.of(), cleared.get("tags"));
		assertThat(persons(cleared, "members")).contains(username(participant));
		Assert.assertEquals(List.of(), cleared.get("links"));
		Assert.assertFalse(cleared.containsKey("description"));
		assertThat(projectService.getLinkedArtefacts(file.getArtefact())).isEmpty();

		// an empty member list is refused, the UI requires at least one involved member
		McpToolResult noMembers = invoke(context(owner), Map.of("action", "update", "fileKey", file.getKey(), "members", List.of()));
		Assert.assertTrue(noMembers.error());
		assertThat(noMembers.text()).contains("at least one project member");
		Assert.assertFalse(VALIDATOR.validate(sut.getDefinition().inputSchema(), Map.of("action", "update", "fileKey", 1, "members", List.of())).valid());
	}

	@Test
	public void update_contentRefused() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-14");
		ProjProject project = createProject(owner);
		ProjFile file = createTextFile(owner, project, "report.md", "# Report");
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(owner), Map.of("action", "update", "fileKey", file.getKey(), "content", "# Report v2"));
		Assert.assertTrue(result.error());
		assertThat(result.text()).contains("cannot be replaced").contains("/Projectfile/" + file.getKey());
		Assert.assertEquals("# Report", readContent(projectService.getFile(file)));
	}

	@Test
	public void update_permissions() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-15");
		Identity client = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-16");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-17");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, client, Set.of(ProjectRole.client), null);
		ProjFile file = createTextFile(owner, project, "report.md", "x");
		ProjFile deleted = createTextFile(owner, project, "deleted.md", "x");
		projectService.deleteFileSoftly(owner, deleted);
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("action", "update", "fileKey", file.getKey(), "title", "x"));
			Assert.fail("A non-member cannot edit");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the file", e.getPermission());
		}
		try {
			invoke(context(client), Map.of("action", "update", "fileKey", file.getKey(), "title", "x"));
			Assert.fail("A client cannot edit");
		} catch (McpPermissionException e) {
			Assert.assertEquals("edit the file", e.getPermission());
			Assert.assertEquals("file with key " + file.getKey(), e.getObjectDescription());
		}
		McpToolResult deletedFile = invoke(context(owner), Map.of("action", "update", "fileKey", deleted.getKey(), "title", "x"));
		Assert.assertTrue(deletedFile.error());
		Assert.assertEquals("The file " + deleted.getKey() + " is already deleted.", deletedFile.text());
		Assert.assertNull(projectService.getFile(file).getVfsMetadata().getTitle());
	}

	// delete

	@Test
	public void delete_creatorAndAdminRoles() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-18");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-19");
		Identity otherParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-20");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		projectService.updateMember(owner, new ProjectBCFactory(), project, otherParticipant, Set.of(ProjectRole.participant), null);
		ProjFile byParticipant = createTextFile(participant, project, "by-participant.txt", "x");
		ProjFile byOwner = createTextFile(owner, project, "by-owner.txt", "x");
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(otherParticipant), Map.of("action", "delete", "fileKey", byParticipant.getKey()));
			Assert.fail("A participant cannot delete the file of another creator");
		} catch (McpPermissionException e) {
			Assert.assertEquals("delete the file", e.getPermission());
		}

		Date before = setPublisherNewsInPast(project);
		Map<String,Object> item = item(invoke(context(participant), Map.of("action", "delete", "fileKey", byParticipant.getKey())));
		Assert.assertEquals("deleted", item.get("status"));
		invoke(context(owner), Map.of("action", "delete", "fileKey", byOwner.getKey()));
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(ProjectStatus.deleted, projectService.getFile(byParticipant).getArtefact().getStatus());
		Assert.assertEquals(ProjectStatus.deleted, projectService.getFile(byOwner).getArtefact().getStatus());
		assertThat(actions(project, participant)).contains(Action.fileStatusDelete);
		assertNewsAfter(project, before);

		McpToolResult twice = invoke(context(owner), Map.of("action", "delete", "fileKey", byOwner.getKey()));
		Assert.assertTrue(twice.error());
		Assert.assertEquals("The file " + byOwner.getKey() + " is already deleted.", twice.text());
	}

	/**
	 * V14: the length is checked with the suffix the rename appends, and a
	 * rename the file system refuses is reported and changes nothing.
	 */
	@Test
	public void update_renameLengthAndFailure() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-40");
		ProjProject project = createProject(owner);
		ProjFile file = createTextFile(owner, project, "draft.md", "# Draft");
		ProjFile lost = createTextFile(owner, project, "lost.md", "# Lost");
		dbInstance.commitAndCloseSession();

		// 252 characters plus ".md" are 255, longer than the limit
		String longName = "a".repeat(FileModifyToolProvider.FILENAME_MAX_LENGTH - 2);
		McpToolResult tooLong = invoke(context(owner), Map.of("action", "update", "fileKey", file.getKey(), "filename", longName));
		Assert.assertTrue(tooLong.error());
		assertThat(tooLong.text()).contains("filename").contains(String.valueOf(FileModifyToolProvider.FILENAME_MAX_LENGTH));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals("draft.md", projectService.getFile(file).getVfsMetadata().getFilename());

		// the result carries the filename
		McpToolResult renamed = invoke(context(owner), Map.of("action", "update", "fileKey", file.getKey(), "filename", "final"));
		Map<String,Object> item = item(renamed);
		Assert.assertEquals("final.md", item.get("filename"));
		@SuppressWarnings("unchecked")
		Map<String,Object> minimal = (Map<String,Object>)((Map<String,Object>)renamed.structuredContent()).get("item");
		Assert.assertEquals("final.md", minimal.get("filename"));
		dbInstance.commitAndCloseSession();

		// the file is gone from the disk: the rename fails, the tool reports it and the registry rolls back
		VFSLeaf leaf = (VFSLeaf)vfsRepositoryService.getItemFor(lost.getVfsMetadata());
		Assert.assertTrue(((LocalFileImpl)leaf).getBasefile().delete());
		McpToolResult failed = invoke(context(owner), Map.of("action", "update", "fileKey", lost.getKey(), "filename", "found", "title", "New title"));
		dbInstance.rollbackAndCloseSession();
		Assert.assertTrue(failed.error());
		Assert.assertEquals("Invalid argument: The file cannot be renamed to found.md. Nothing was changed. Choose another filename.", failed.text());
		ProjFile reloaded = projectService.getFile(lost);
		Assert.assertEquals("lost.md", reloaded.getVfsMetadata().getFilename());
		Assert.assertNull(reloaded.getVfsMetadata().getTitle());
	}

	/**
	 * V10: the involved members of a file of a template are read only.
	 */
	@Test
	public void template_membersRefused() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-41");
		ProjProject template = createProject(owner);
		template = projectService.updateProject(owner, new ProjectBCFactory(), template, null, template.getTitle(), null, null, true, false);
		ProjFile file = createTextFile(owner, template, "template.md", "x");
		dbInstance.commitAndCloseSession();

		McpToolResult create = invoke(context(owner), Map.of("action", "create", "projectKey", template.getKey(), "filename", "x.md", "content", "x",
				"members", List.of(username(owner))));
		Assert.assertTrue(create.error());
		assertThat(create.text()).contains("template project have no involved members");
		McpToolResult update = invoke(context(owner), Map.of("action", "update", "fileKey", file.getKey(), "members", List.of(username(owner))));
		Assert.assertTrue(update.error());
		assertThat(update.text()).contains("template project have no involved members");
	}

	/**
	 * V3: tags of a file are de-duplicated and matched to the project tags.
	 */
	@Test
	public void tags_deduplicatedAndMatched() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-42");
		ProjProject project = createProject(owner);
		ProjFile tagged = createTextFile(owner, project, "tagged.md", "x");
		projectService.updateTags(owner, tagged.getArtefact(), List.of("Draft"));
		dbInstance.commitAndCloseSession();

		Map<String,Object> item = item(invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "filename", "y.md", "content", "y",
				"tags", List.of("draft", "DRAFT"))));
		Assert.assertEquals(List.of("Draft"), item.get("tags"));
	}

	@Test
	public void delete_notMember_denied() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-21");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-22");
		ProjProject project = createProject(owner);
		ProjFile file = createTextFile(owner, project, "report.md", "x");
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("action", "delete", "fileKey", file.getKey()));
			Assert.fail("A non-member cannot delete");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the file", e.getPermission());
		}
		Assert.assertEquals(ProjectStatus.active, projectService.getFile(file).getArtefact().getStatus());
	}

	@Test
	public void boundedInputs() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fm-23");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();

		McpToolResult title = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "filename", "x.txt", "content", "x",
				"title", "t".repeat(FileModifyToolProvider.TITLE_MAX_LENGTH + 1)));
		Assert.assertTrue(title.error());
		assertThat(title.text()).contains("title").contains(String.valueOf(FileModifyToolProvider.TITLE_MAX_LENGTH));

		McpToolResult tags = invoke(context(owner), Map.of("action", "create", "projectKey", project.getKey(), "filename", "x.txt", "content", "x",
				"tags", List.of("y".repeat(ProjectToolSupport.TAG_MAX_LENGTH + 1))));
		Assert.assertTrue(tags.error());
		assertThat(tags.text()).contains("tags");
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(0, countFiles(project));
	}

	private ProjProject createProject(Identity owner) {
		ProjProject project = projectService.createProject(owner, new ProjectBCFactory(), owner);
		return projectService.updateProject(owner, new ProjectBCFactory(), project, null, "Project " + random(), null, null, false, false);
	}

	private ProjFile createTextFile(Identity doer, ProjProject project, String filename, String content) {
		ProjFile file = projectService.createFile(doer, project, filename, new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), true);
		Assert.assertNotNull(file);
		return file;
	}

	private ProjNote createNote(Identity doer, ProjProject project, String title) {
		ProjNote note = projectService.createNote(doer, project);
		projectService.updateNote(doer, note, UUID.randomUUID().toString(), title, "x");
		return note;
	}

	private ProjToDo createToDo(Identity doer, ProjProject project, String title) {
		ProjToDo toDo = projectService.createToDo(doer, project);
		projectService.updateToDo(doer, toDo, title, ToDoStatus.open, null, null, null, null, null);
		return toDo;
	}

	private String readContent(ProjFile file) {
		return new String(readBytes(file), StandardCharsets.UTF_8);
	}

	private byte[] readBytes(ProjFile file) {
		VFSLeaf leaf = (VFSLeaf)vfsRepositoryService.getItemFor(file.getVfsMetadata());
		try(InputStream in = leaf.getInputStream()) {
			return in.readAllBytes();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private long countFiles(ProjProject project) {
		ProjFileSearchParams searchParams = new ProjFileSearchParams();
		searchParams.setProject(project);
		return projectService.getFilesCount(searchParams);
	}

	private List<Action> actions(ProjProject project, Identity doer) {
		ProjActivitySearchParams searchParams = new ProjActivitySearchParams();
		searchParams.setProject(project);
		searchParams.setDoer(doer);
		return projectService.getActivities(searchParams, 0, 100).stream().map(ProjActivity::getAction).toList();
	}

	private Date setPublisherNewsInPast(ProjProject project) {
		Publisher publisher = notificationsManager.getPublisher(projectService.getSubscriptionContext(project));
		Assert.assertNotNull(publisher);
		publisher.setLatestNewsDate(DateUtils.addDays(new Date(), -1));
		dbInstance.getCurrentEntityManager().merge(publisher);
		dbInstance.commitAndCloseSession();
		return DateUtils.addSeconds(new Date(), -1);
	}

	private void assertNewsAfter(ProjProject project, Date before) {
		dbInstance.commitAndCloseSession();
		Publisher publisher = notificationsManager.getPublisher(projectService.getSubscriptionContext(project));
		assertThat(publisher.getLatestNewsDate()).isAfterOrEqualTo(before);
	}

	private McpToolContext context(Identity identity) {
		return new McpToolContext(identity, securityManager.getRoles(identity), GRANTS, null, Locale.ENGLISH, "127.0.0.1");
	}

	private static String username(Identity identity) {
		return identity.getUser().getNickName();
	}

	@SuppressWarnings("unchecked")
	private static List<String> persons(Map<String,Object> item, String name) {
		return ((List<Map<String,Object>>)item.get(name)).stream().map(person -> person.get("username").toString()).toList();
	}

	@SuppressWarnings("unchecked")
	private static List<String> links(Map<String,Object> item) {
		return ((List<Map<String,Object>>)item.get("links")).stream().map(link -> link.get("type") + ":" + link.get("key")).toList();
	}

	@SuppressWarnings("unchecked")
	/**
	 * Checks the minimal result of the modify tool and returns the full item
	 * as the query tool builds it, for the assertions on the persisted object.
	 */
	private Map<String,Object> item(McpToolResult result) {
		Assert.assertFalse(result.text(), result.error());
		JsonSchemaValidator.ValidationResponse response = VALIDATOR.validate(sut.getDefinition().outputSchema(), result.structuredContent());
		Assert.assertTrue(response.errorMessage(), response.valid());
		Map<String,Object> minimal = (Map<String,Object>)((Map<String,Object>)result.structuredContent()).get("item");
		Assert.assertNotNull(minimal);
		assertThat(FileModifyToolProvider.RESULT_FIELDS).containsAll(minimal.keySet());
		assertThat(result.text()).endsWith("Call file_query get for the full item.");
		Long key = (Long)minimal.get("key");
		ProjFileSearchParams searchParams = new ProjFileSearchParams();
		searchParams.setFiles(List.of(() -> key));
		return support.fileItem(projectService.getFileInfos(searchParams, ProjArtefactInfoParams.of(true, false, true)).get(0), true);
	}
	/**
	 * Calls the tool and maps an invalid argument as {@link McpToolRegistry#call} does.
	 */
	private McpToolResult invoke(McpToolContext toolContext, Map<String,Object> arguments) {
		try {
			return sut.execute(toolContext, arguments);
		} catch (McpInvalidArgumentException e) {
			return McpToolResult.error(McpToolRegistry.invalidArgumentText(e));
		}
	}
}
