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
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
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
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjActivity.Action;
import org.olat.modules.project.ProjActivitySearchParams;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.json.schema.JsonSchemaValidator;

/**
 * Integration tests of the tool file_query: the list of a project with its
 * filters, the file with metadata, links and content, the rights.
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class FileQueryToolProviderTest extends OlatTestCase {

	private static final McpGrants GRANTS = new McpGrants(Set.of(FileQueryToolProvider.NAME), null, List.of("test"));
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
	private FileQueryToolProvider sut;

	@Test
	public void definition() {
		Assert.assertEquals("file_query", sut.getToolName());
		Assert.assertEquals(ProjectMcpPack.NAME, sut.getPack());
		Assert.assertEquals(McpToolScope.READ, sut.getScope());
		assertThat(sut.getDefinition().description()).contains("project_query").contains("file_modify").contains("content=true");
		Assert.assertFalse(sut.getDefinition().destructive());
		Assert.assertTrue(sut.getDefinition().idempotent());
	}

	@Test
	public void inputSchema() {
		Map<String,Object> schema = sut.getDefinition().inputSchema();
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("projectKey", 12)).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "list", "projectKey", 12, "status", "all", "suffix", "pdf",
				"createdByMe", true, "createdAfter", "2026-10-01", "limit", 200)).valid());
		Assert.assertTrue(VALIDATOR.validate(schema, Map.of("action", "get", "fileKey", 3, "content", true)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("projectKey", 12, "status", "archived")).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("projectKey", 12, "limit", 201)).valid());
		Assert.assertFalse(VALIDATOR.validate(schema, Map.of("projectKey", 12, "query", "x")).valid());
	}

	@Test
	public void mimeTypes() {
		Assert.assertEquals("text/markdown", ProjectToolSupport.mimeType("notes.md"));
		Assert.assertEquals("application/json", ProjectToolSupport.mimeType("data.JSON"));
		Assert.assertEquals("application/pdf", ProjectToolSupport.mimeType("report.pdf"));
		Assert.assertEquals("image/png", ProjectToolSupport.mimeType("logo.png"));
		Assert.assertEquals("application/octet-stream", ProjectToolSupport.mimeType("archive.unknownsuffix"));
		Assert.assertTrue(ProjectToolSupport.isText("text/markdown"));
		Assert.assertTrue(ProjectToolSupport.isText("application/json"));
		Assert.assertTrue(ProjectToolSupport.isText("image/svg+xml"));
		Assert.assertFalse(ProjectToolSupport.isText("application/pdf"));
		Assert.assertFalse(ProjectToolSupport.isText("image/png"));
	}

	@Test
	public void list_invalidArguments() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fq-1");
		ProjProject project = createProject(owner);
		dbInstance.commitAndCloseSession();

		McpToolResult noKey = invoke(context(owner), Map.of());
		Assert.assertTrue(noKey.error());
		assertThat(noKey.text()).contains("projectKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknownProject = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("projectKey", Long.valueOf(-5))));
		Assert.assertEquals("project with key -5", unknownProject.getObjectDescription());
		Assert.assertEquals("view the files", unknownProject.getPermission());

		McpToolResult badStatus = invoke(context(owner), Map.of("projectKey", project.getKey(), "status", "archived"));
		Assert.assertTrue(badStatus.error());
		assertThat(badStatus.text()).contains("Unknown status 'archived'");

		McpToolResult badDate = invoke(context(owner), Map.of("projectKey", project.getKey(), "createdAfter", "yesterday"));
		Assert.assertTrue(badDate.error());
		assertThat(badDate.text()).contains("createdAfter");
	}

	@Test
	public void list_membersMetadataAndSorting() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fq-2");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fq-3");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjFile first = createTextFile(owner, project, "first.md", "# First");
		ProjFile second = createTextFile(participant, project, "second.txt", "second");
		projectService.updateTags(owner, first.getArtefact(), List.of("meeting", "alpha"));
		// the metadata change moves the first file to the top of the list
		projectService.updateFile(owner, first, null, "First title", "First description");
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(participant), Map.of("projectKey", project.getKey()));
		Map<String,Object> structured = structured(result);
		List<Map<String,Object>> items = items(structured);
		Assert.assertEquals(Integer.valueOf(2), structured.get("total"));
		Assert.assertEquals(first.getKey(), items.get(0).get("key"));
		Assert.assertEquals(second.getKey(), items.get(1).get("key"));

		Map<String,Object> firstItem = items.get(0);
		Assert.assertEquals("first.md", firstItem.get("filename"));
		Assert.assertEquals("First title", firstItem.get("title"));
		Assert.assertEquals("First description", firstItem.get("description"));
		Assert.assertEquals("text/markdown", firstItem.get("mimeType"));
		Assert.assertEquals(Long.valueOf(7), firstItem.get("size"));
		Assert.assertEquals("active", firstItem.get("status"));
		Assert.assertEquals(project.getKey(), firstItem.get("projectKey"));
		Assert.assertEquals(List.of("alpha", "meeting"), firstItem.get("tags"));
		Assert.assertFalse(firstItem.containsKey("links"));
		Assert.assertFalse(firstItem.containsKey("text"));
		assertThat(persons(firstItem, "members")).containsExactly(username(owner));
		Assert.assertEquals(username(owner), ((Map<?,?>)firstItem.get("creator")).get("username"));
		assertThat(firstItem.get("url").toString()).contains("/Project/" + project.getKey()).contains("/Projectfile/" + first.getKey());

		Map<String,Object> secondItem = items.get(1);
		Assert.assertFalse(secondItem.containsKey("title"));
		Assert.assertEquals("text/plain", secondItem.get("mimeType"));
		assertThat(result.text()).contains("2 files").contains("first.md (key " + first.getKey() + ", \"First title\", text/markdown, 7 bytes");
	}

	@Test
	public void list_filters() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fq-4");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fq-5");
		ProjProject project = createProject(owner);
		projectService.updateMember(owner, new ProjectBCFactory(), project, participant, Set.of(ProjectRole.participant), null);
		ProjFile mine = createTextFile(owner, project, "mine.md", "m");
		ProjFile theirs = createTextFile(participant, project, "theirs.txt", "t");
		ProjFile deleted = createTextFile(owner, project, "deleted.md", "d");
		projectService.deleteFileSoftly(owner, deleted);
		dbInstance.commitAndCloseSession();

		assertThat(keys(invoke(context(owner), Map.of("projectKey", project.getKey()))))
				.containsExactlyInAnyOrder(mine.getKey(), theirs.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", project.getKey(), "status", "deleted"))))
				.containsExactly(deleted.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", project.getKey(), "status", "all"))))
				.containsExactlyInAnyOrder(mine.getKey(), theirs.getKey(), deleted.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", project.getKey(), "createdByMe", true))))
				.containsExactly(mine.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", project.getKey(), "suffix", "md"))))
				.containsExactly(mine.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", project.getKey(), "suffix", ".TXT"))))
				.containsExactly(theirs.getKey());
		assertThat(keys(invoke(context(owner), Map.of("projectKey", project.getKey(), "createdAfter",
				McpToolSupport.isoDate(DateUtils.addDays(new Date(), 1)))))).isEmpty();
		assertThat(keys(invoke(context(owner), Map.of("projectKey", project.getKey(), "createdAfter",
				McpToolSupport.isoDate(DateUtils.addDays(new Date(), -1))))))
				.containsExactlyInAnyOrder(mine.getKey(), theirs.getKey());
	}

	@Test
	public void list_limitAndTruncated() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fq-6");
		ProjProject project = createProject(owner);
		createTextFile(owner, project, "a.txt", "a");
		createTextFile(owner, project, "b.txt", "b");
		createTextFile(owner, project, "c.txt", "c");
		dbInstance.commitAndCloseSession();

		Map<String,Object> structured = structured(invoke(context(owner), Map.of("projectKey", project.getKey(), "limit", 2)));
		Assert.assertEquals(2, items(structured).size());
		Assert.assertEquals(Integer.valueOf(3), structured.get("total"));
		Assert.assertEquals(Boolean.TRUE, structured.get("truncated"));
	}

	@Test
	public void list_notMember_denied() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fq-7");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fq-8");
		ProjProject project = createProject(owner);
		createTextFile(owner, project, "secret.txt", "s");
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("projectKey", project.getKey()));
			Assert.fail("A non-member cannot list the files");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the files", e.getPermission());
			Assert.assertEquals("project with key " + project.getKey(), e.getObjectDescription());
		}
	}

	@Test
	public void get_metadataAndLinks() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fq-9");
		ProjProject project = createProject(owner);
		ProjFile file = createTextFile(owner, project, "kickoff.md", "# Kickoff");
		ProjNote note = projectService.createNote(owner, project);
		projectService.updateNote(owner, note, UUID.randomUUID().toString(), "Linked note", "x");
		projectService.linkArtefacts(owner, file.getArtefact(), note.getArtefact());
		projectService.updateTags(owner, file.getArtefact(), List.of("kickoff"));
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(owner), Map.of("action", "get", "fileKey", file.getKey()));
		Map<String,Object> item = item(structured(result));
		Assert.assertEquals(file.getKey(), item.get("key"));
		Assert.assertEquals("kickoff.md", item.get("filename"));
		Assert.assertEquals(List.of("kickoff"), item.get("tags"));
		Assert.assertFalse(item.containsKey("content"));
		Assert.assertFalse(item.containsKey("text"));
		@SuppressWarnings("unchecked")
		List<Map<String,Object>> links = (List<Map<String,Object>>)item.get("links");
		Assert.assertEquals(1, links.size());
		Assert.assertEquals("note", links.get(0).get("type"));
		Assert.assertEquals(note.getKey(), links.get(0).get("key"));
		Assert.assertTrue(result.attachments().isEmpty());
		assertThat(result.text()).startsWith("File kickoff.md (key " + file.getKey());
		// no download activity without content
		assertThat(actions(project, owner)).doesNotContain(Action.fileDownload);
	}

	@Test
	public void get_textContent() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fq-10");
		ProjProject project = createProject(owner);
		String content = "# Kickoff\n\n- one\n- zwei: ü";
		ProjFile file = createTextFile(owner, project, "kickoff.md", content);
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(owner), Map.of("action", "get", "fileKey", file.getKey(), "content", true));
		Map<String,Object> item = item(structured(result));
		Assert.assertEquals("text", item.get("content"));
		Assert.assertEquals(content, item.get("text"));
		Assert.assertTrue(result.attachments().isEmpty());
		assertThat(result.text()).contains("# Kickoff").contains("zwei: ü");
		dbInstance.commitAndCloseSession();
		assertThat(actions(project, owner)).contains(Action.fileDownload);
	}

	@Test
	public void get_imageAndBinaryContent() throws IOException {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fq-11");
		ProjProject project = createProject(owner);
		byte[] png = resource("IMG_1483.png");
		byte[] pdf = resource("handInTopic1.pdf");
		ProjFile image = createFile(owner, project, "picture.png", png);
		ProjFile document = createFile(owner, project, "paper.pdf", pdf);
		dbInstance.commitAndCloseSession();

		McpToolResult imageResult = invoke(context(owner), Map.of("action", "get", "fileKey", image.getKey(), "content", true));
		Map<String,Object> imageItem = item(structured(imageResult));
		Assert.assertEquals("image", imageItem.get("content"));
		Assert.assertEquals("image/png", imageItem.get("mimeType"));
		Assert.assertEquals(Long.valueOf(png.length), imageItem.get("size"));
		Assert.assertFalse(imageItem.containsKey("text"));
		Assert.assertEquals(1, imageResult.attachments().size());
		Assert.assertEquals("image/png", imageResult.attachments().get(0).mimeType());
		Assert.assertArrayEquals(png, imageResult.attachments().get(0).data());
		assertThat(imageResult.text()).contains("image content");

		McpToolResult pdfResult = invoke(context(owner), Map.of("action", "get", "fileKey", document.getKey(), "content", true));
		Map<String,Object> pdfItem = item(structured(pdfResult));
		Assert.assertEquals("blob", pdfItem.get("content"));
		Assert.assertEquals("application/pdf", pdfItem.get("mimeType"));
		Assert.assertEquals(1, pdfResult.attachments().size());
		Assert.assertEquals("application/pdf", pdfResult.attachments().get(0).mimeType());
		Assert.assertArrayEquals(pdf, pdfResult.attachments().get(0).data());
		assertThat(pdfResult.attachments().get(0).uri()).isEqualTo("openolat://project/" + project.getKey() + "/file/" + document.getKey() + "/paper.pdf");
	}

	@Test
	public void get_contentTooLarge() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fq-12");
		ProjProject project = createProject(owner);
		byte[] big = new byte[(int)ProjectToolSupport.FILE_TEXT_MAX_BYTES + 1];
		java.util.Arrays.fill(big, (byte)'x');
		ProjFile file = createFile(owner, project, "big.txt", big);
		dbInstance.commitAndCloseSession();

		McpToolResult result = invoke(context(owner), Map.of("action", "get", "fileKey", file.getKey(), "content", true));
		Assert.assertTrue(result.error());
		assertThat(result.text()).contains("big.txt").contains(String.valueOf(ProjectToolSupport.FILE_TEXT_MAX_BYTES)).contains("/Projectfile/" + file.getKey());
		// the metadata are still available
		Assert.assertFalse(invoke(context(owner), Map.of("action", "get", "fileKey", file.getKey())).error());
	}

	/**
	 * The size of the metadata is out of date: the content on the disk is
	 * larger than the limit. The read stops after the limit plus one byte.
	 */
	@Test
	public void get_contentTooLarge_staleMetadata() throws IOException {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fq-12b");
		ProjProject project = createProject(owner);
		ProjFile file = createTextFile(owner, project, "grown.txt", "small");
		dbInstance.commitAndCloseSession();

		VFSLeaf leaf = (VFSLeaf)vfsRepositoryService.getItemFor(file.getVfsMetadata());
		byte[] big = new byte[(int)ProjectToolSupport.FILE_TEXT_MAX_BYTES + 10];
		java.util.Arrays.fill(big, (byte)'x');
		// bypass the VFS, the metadata keep the old size
		Files.write(((LocalFileImpl)leaf).getBasefile().toPath(), big);

		McpToolResult result = invoke(context(owner), Map.of("action", "get", "fileKey", file.getKey(), "content", true));
		Assert.assertTrue(result.error());
		assertThat(result.text()).contains("grown.txt").contains("more than " + ProjectToolSupport.FILE_TEXT_MAX_BYTES);
	}

	@Test
	public void get_notMember_denied() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fq-13");
		Identity stranger = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fq-14");
		ProjProject project = createProject(owner);
		ProjFile file = createTextFile(owner, project, "secret.txt", "s");
		dbInstance.commitAndCloseSession();

		try {
			invoke(context(stranger), Map.of("action", "get", "fileKey", file.getKey(), "content", true));
			Assert.fail("A non-member cannot read the file");
		} catch (McpPermissionException e) {
			Assert.assertEquals("view the file", e.getPermission());
			Assert.assertEquals("file with key " + file.getKey(), e.getObjectDescription());
		}
	}

	@Test
	public void get_invalidArguments() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-fq-15");

		McpToolResult noKey = invoke(context(owner), Map.of("action", "get"));
		Assert.assertTrue(noKey.error());
		assertThat(noKey.text()).contains("fileKey");

		// an unknown key gets the answer of a foreign object
		McpPermissionException unknown = Assert.assertThrows(McpPermissionException.class, () -> invoke(context(owner), Map.of("action", "get", "fileKey", Long.valueOf(-7))));
		Assert.assertEquals("file with key -7", unknown.getObjectDescription());
		Assert.assertEquals("view the file", unknown.getPermission());
	}

	private ProjProject createProject(Identity owner) {
		ProjProject project = projectService.createProject(owner, new ProjectBCFactory(), owner);
		return projectService.updateProject(owner, new ProjectBCFactory(), project, null, "Project " + random(), null, null, false, false);
	}

	private ProjFile createTextFile(Identity doer, ProjProject project, String filename, String content) {
		return createFile(doer, project, filename, content.getBytes(StandardCharsets.UTF_8));
	}

	private ProjFile createFile(Identity doer, ProjProject project, String filename, byte[] content) {
		ProjFile file = projectService.createFile(doer, project, filename, new ByteArrayInputStream(content), true);
		Assert.assertNotNull(file);
		return file;
	}

	static byte[] resource(String name) throws IOException {
		try(InputStream in = JunitTestHelper.class.getResourceAsStream("file_resources/" + name)) {
			Assert.assertNotNull(in);
			return in.readAllBytes();
		}
	}

	private List<Action> actions(ProjProject project, Identity doer) {
		ProjActivitySearchParams searchParams = new ProjActivitySearchParams();
		searchParams.setProject(project);
		searchParams.setDoer(doer);
		return projectService.getActivities(searchParams, 0, 100).stream().map(ProjActivity::getAction).toList();
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

	private List<Long> keys(McpToolResult result) {
		return items(structured(result)).stream().map(item -> (Long)item.get("key")).toList();
	}

	/**
	 * @return The structured content, validated against the output schema of the tool
	 */
	@SuppressWarnings("unchecked")
	private Map<String,Object> structured(McpToolResult result) {
		Assert.assertFalse(result.text(), result.error());
		JsonSchemaValidator.ValidationResponse response = VALIDATOR.validate(sut.getDefinition().outputSchema(), result.structuredContent());
		Assert.assertTrue(response.errorMessage(), response.valid());
		return (Map<String,Object>)result.structuredContent();
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String,Object>> items(Map<String,Object> structured) {
		return (List<Map<String,Object>>)structured.get("items");
	}

	@SuppressWarnings("unchecked")
	private static Map<String,Object> item(Map<String,Object> structured) {
		return (Map<String,Object>)structured.get("item");
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
