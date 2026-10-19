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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.mcp.tool.McpInvalidArgumentException;
import org.olat.mcp.tool.McpPermissionException;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolDefinition;
import org.olat.mcp.tool.McpToolProvider;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactInfoParams;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjFileInfo;
import org.olat.modules.project.ProjFileSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Uploads, edits and deletes the files of a project through the
 * ProjectService, the sequence of ProjFileUploadController and
 * ProjFileEditController with their sub controllers (content, references,
 * members). The content of an existing file is not replaced, the UI has no
 * such function either.
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class FileModifyToolProvider implements McpToolProvider {

	public static final String NAME = "file_modify";

	static final String ACTION_CREATE = "create";
	static final String ACTION_UPDATE = "update";
	static final String ACTION_DELETE = "delete";
	private static final List<String> ACTIONS = List.of(ACTION_CREATE, ACTION_UPDATE, ACTION_DELETE);
	/** The fields of the result, the full file comes from file_query get */
	static final List<String> RESULT_FIELDS = List.of("key", "filename", "title", "status", "url");
	/** ProjFileContentController refuses 255 and more */
	static final int FILENAME_MAX_LENGTH = 254;
	static final int TITLE_MAX_LENGTH = 255;
	static final int DESCRIPTION_MAX_LENGTH = 4000;

	private static final McpToolDefinition DEFINITION = new McpToolDefinition(
			"OpenOlat: change project files",
			"Files",
			"Uploads, edits or deletes a file (document) of an OpenOlat project and returns its key, filename, title, status and URL; file_query get returns the full file. "
			+ "create needs projectKey, filename and the content: text files as content, other files as contentBase64 (any file type, "
			+ "the request limit bounds the size); optional title, description, tags, members and links to other artefacts of the project "
			+ "(ToDos by the key of todo_query, notes, files, appointments, decisions; milestones cannot be linked). "
			+ "update changes only the arguments you pass: filename renames the file; tags, members and links replace the current lists; "
			+ "an empty string clears the title or the description; the content of an existing file cannot be replaced, upload a new file instead. "
			+ "delete is a soft delete. Call project_query for the projectKey and file_query for the fileKey.",
			inputSchema(), outputSchema(), true, false);

	@Autowired
	private DB dbInstance;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private ProjectToolSupport support;

	@Override
	public String getToolName() {
		return NAME;
	}

	@Override
	public String getPack() {
		return ProjectMcpPack.NAME;
	}

	@Override
	public McpToolScope getScope() {
		return McpToolScope.WRITE;
	}

	@Override
	public boolean isAvailable() {
		return support.isPackAvailable();
	}

	@Override
	public McpToolDefinition getDefinition() {
		return DEFINITION;
	}

	@Override
	public McpToolResult execute(McpToolContext context, Map<String,Object> arguments) {
		String action = McpToolSupport.string(arguments, "action", null);
		if(action == null) {
			return McpToolResult.error("The argument action is required. Use one of: " + String.join(", ", ACTIONS) + ".");
		}
		return switch(action) {
			case ACTION_CREATE -> create(context, arguments);
			case ACTION_UPDATE -> update(context, arguments);
			case ACTION_DELETE -> delete(context, arguments);
			default -> McpToolResult.error("Unknown action " + action + ". Use one of: " + String.join(", ", ACTIONS) + ".");
		};
	}

	private McpToolResult create(McpToolContext context, Map<String,Object> arguments) {
		Long projectKey = McpToolSupport.longValue(arguments, "projectKey");
		if(projectKey == null) {
			return McpToolResult.error("The action create needs the argument projectKey. Call project_query to list your projects.");
		}
		ProjProject project = projectService.getProject(() -> projectKey);
		// an unknown key gets the answer of a foreign project
		if(project == null) {
			throw new McpPermissionException(ProjectToolSupport.describeProject(projectKey), "create files");
		}
		ProjProjectSecurityCallback secCallback = support.projectCallback(context, project);
		McpToolResult stateError = secCallback.canViewFiles() ? ProjectToolSupport.stateError(project, null, null) : null;
		if(stateError != null) {
			return stateError;
		}
		if(!secCallback.canCreateFiles()) {
			throw new McpPermissionException(ProjectToolSupport.describeProject(projectKey), "create files");
		}
		// Validate every argument before anything is written
		String filename = filename(arguments, null);
		if(filename == null) {
			return McpToolResult.error("The action create needs the argument filename, e.g. report.pdf.");
		}
		if(projectService.existsFile(project, filename)) {
			return McpToolResult.error("A file named " + filename + " exists in this project. Choose another filename or update the existing file.");
		}
		byte[] bytes = content(arguments);
		if(bytes == null) {
			return McpToolResult.error("The action create needs the content: text files as content, other files as contentBase64.");
		}
		String title = McpToolSupport.string(arguments, "title", null);
		String description = McpToolSupport.string(arguments, "description", null);
		McpToolSupport.checkLength("title", title, TITLE_MAX_LENGTH);
		McpToolSupport.checkLength("description", description, DESCRIPTION_MAX_LENGTH);
		List<String> tags = support.tags(arguments, project);
		List<Identity> members = arguments.containsKey("members") ? support.resolveMembers(arguments, "members", project, "this file") : null;
		Set<ProjArtefact> links = support.resolveLinks(arguments, project, null);

		// The sequence of ProjFileUploadController.handleFileCreation and the reference and member sub controllers
		Identity doer = context.identity();
		ProjFile file = projectService.createFile(doer, project, filename, new ByteArrayInputStream(bytes), true);
		if(file == null) {
			throw new IllegalStateException("Cannot store the file " + filename + " in project " + projectKey);
		}
		// the size and the dates are written by a bulk update, reload the entity
		dbInstance.getCurrentEntityManager().refresh(file.getVfsMetadata());
		if(!tags.isEmpty()) {
			projectService.updateTags(doer, file.getArtefact(), tags);
		}
		if(title != null || description != null) {
			VFSMetadata metadata = file.getVfsMetadata();
			metadata.setTitle(title);
			metadata.setComment(description);
			vfsRepositoryService.updateMetadata(metadata);
		}
		if(members != null) {
			projectService.updateMembers(doer, ProjectBCFactory.createFactory(project), file.getArtefact(), new ArrayList<IdentityRef>(members));
		}
		if(!links.isEmpty()) {
			projectService.updateLinkedArtefacts(doer, file.getArtefact(), links);
		}
		return result(file.getKey(), "Uploaded: ");
	}

	private McpToolResult update(McpToolContext context, Map<String,Object> arguments) {
		ProjFile file = loadFile(arguments);
		ProjProject project = file.getArtefact().getProject();
		ProjProjectSecurityCallback secCallback = support.projectCallback(context, project);
		if(!secCallback.canViewFiles()) {
			throw new McpPermissionException("file with key " + file.getKey(), "view the file");
		}
		McpToolResult stateError = ProjectToolSupport.stateError(project, file.getArtefact(), "file " + file.getKey());
		if(stateError != null) {
			return stateError;
		}
		if(!secCallback.canEditFile(file)) {
			throw new McpPermissionException("file with key " + file.getKey(), "edit the file");
		}
		if(arguments.containsKey("content") || arguments.containsKey("contentBase64")) {
			return McpToolResult.error("The content of an existing file cannot be replaced via MCP. Upload a new file with action create, "
					+ "or replace the content in OpenOlat: " + ProjectToolSupport.url(project, ProjFile.TYPE, file.getKey()));
		}
		// Validate every argument before anything is written; an absent argument keeps the value, an empty string clears it
		VFSMetadata metadata = file.getVfsMetadata();
		String currentFilename = metadata.getFilename();
		String filename = arguments.containsKey("filename") ? filename(arguments, currentFilename) : currentFilename;
		if(filename == null) {
			return McpToolResult.error("The filename of a file cannot be empty.");
		}
		if(!filename.equals(metadata.getFilename()) && projectService.existsFile(project, filename)) {
			return McpToolResult.error("A file named " + filename + " exists in this project. Choose another filename.");
		}
		String title = arguments.containsKey("title") ? McpToolSupport.string(arguments, "title", null) : metadata.getTitle();
		String description = arguments.containsKey("description") ? McpToolSupport.string(arguments, "description", null) : metadata.getComment();
		McpToolSupport.checkLength("title", title, TITLE_MAX_LENGTH);
		McpToolSupport.checkLength("description", description, DESCRIPTION_MAX_LENGTH);
		List<String> tags = support.tags(arguments, project);
		List<Identity> members = arguments.containsKey("members") ? support.resolveMembers(arguments, "members", project, "this file") : null;
		Set<ProjArtefact> links = arguments.containsKey("links") ? support.resolveLinks(arguments, project, file.getArtefact()) : null;

		// The sequence of ProjFileEditController.formOK
		Identity doer = context.identity();
		if(!filename.equals(currentFilename)) {
			// The rename first and alone: ProjectService.updateFile ignores a failed rename and
			// marks the news of a change, which commits. A failed rename stops the call before any write.
			// The price: a rename plus a title or description change writes two fileContentUpdate
			// activities (the UI one). One combined call would commit the new title on a failed rename.
			projectService.updateFile(doer, file, filename, metadata.getTitle(), metadata.getComment());
			if(!projectService.existsFile(project, filename)) {
				throw new McpInvalidArgumentException("The file cannot be renamed to " + filename + ". Nothing was changed. Choose another filename.");
			}
		}
		if(arguments.containsKey("title") || arguments.containsKey("description")) {
			projectService.updateFile(doer, file, filename, title, description);
		}
		if(arguments.containsKey("tags")) {
			projectService.updateTags(doer, file.getArtefact(), tags);
		}
		if(links != null) {
			projectService.updateLinkedArtefacts(doer, file.getArtefact(), links);
		}
		if(members != null) {
			projectService.updateMembers(doer, ProjectBCFactory.createFactory(project), file.getArtefact(), new ArrayList<IdentityRef>(members));
		}
		return result(file.getKey(), "Updated: ");
	}

	private McpToolResult delete(McpToolContext context, Map<String,Object> arguments) {
		ProjFile file = loadFile(arguments);
		ProjProject project = file.getArtefact().getProject();
		ProjProjectSecurityCallback secCallback = support.projectCallback(context, project);
		if(!secCallback.canViewFiles()) {
			throw new McpPermissionException("file with key " + file.getKey(), "view the file");
		}
		McpToolResult stateError = ProjectToolSupport.stateError(project, file.getArtefact(), "file " + file.getKey());
		if(stateError != null) {
			return stateError;
		}
		if(!secCallback.canDeleteFile(file, context.identity())) {
			throw new McpPermissionException("file with key " + file.getKey(), "delete the file");
		}
		projectService.deleteFileSoftly(context.identity(), file);
		return result(file.getKey(), "Deleted: ");
	}

	/**
	 * @param arguments The arguments
	 * @param currentFilename The current filename for update, its suffix is kept as the UI does, null for create
	 * @return The validated filename or null when absent or blank
	 */
	static String filename(Map<String,Object> arguments, String currentFilename) {
		String filename = McpToolSupport.string(arguments, "filename", null);
		if(filename == null) {
			return null;
		}
		// ProjFileContentController.getCleanedFilename keeps the suffix of an existing file
		String suffix = currentFilename == null ? null : FileUtils.getFileSuffix(currentFilename);
		if(StringHelper.containsNonWhitespace(suffix) && !filename.toLowerCase().endsWith("." + suffix.toLowerCase())) {
			filename = filename + "." + suffix;
		}
		// checked with the suffix: the stored name must fit the column and the file system
		if(filename.length() > FILENAME_MAX_LENGTH) {
			throw new McpInvalidArgumentException("The filename " + (currentFilename == null ? "" : "with the extension of the file ")
					+ "is longer than " + FILENAME_MAX_LENGTH + " characters.");
		}
		if(!FileUtils.validateFilename(filename)) {
			throw new McpInvalidArgumentException("The filename " + filename + " is not valid: use letters, digits, dots, dashes and underscores, no path separators.");
		}
		return filename;
	}

	/**
	 * @param arguments The arguments
	 * @return The bytes of content (UTF-8) or contentBase64, null when neither is present
	 */
	static byte[] content(Map<String,Object> arguments) {
		Object text = arguments.get("content");
		Object base64 = arguments.get("contentBase64");
		if(text != null && base64 != null) {
			throw new McpInvalidArgumentException("Pass either content or contentBase64, not both.");
		}
		if(text != null) {
			return text.toString().getBytes(StandardCharsets.UTF_8);
		}
		if(base64 != null) {
			try {
				return Base64.getDecoder().decode(base64.toString().strip());
			} catch (IllegalArgumentException e) {
				throw new McpInvalidArgumentException("The argument contentBase64 is not valid base64.");
			}
		}
		return null;
	}

	private ProjFile loadFile(Map<String,Object> arguments) {
		Long fileKey = McpToolSupport.longValue(arguments, "fileKey");
		if(fileKey == null) {
			throw new McpInvalidArgumentException("This action needs the argument fileKey. Call file_query to find it.");
		}
		ProjFile file = projectService.getFile(() -> fileKey);
		// an unknown key gets the answer of a foreign file
		if(file == null) {
			throw new McpPermissionException("file with key " + fileKey, "view the file");
		}
		return file;
	}

	private ProjFileInfo info(Long fileKey) {
		ProjFileSearchParams searchParams = new ProjFileSearchParams();
		searchParams.setFiles(List.of(() -> fileKey));
		return projectService.getFileInfos(searchParams, ProjArtefactInfoParams.of(true, false, true)).get(0);
	}

	private McpToolResult result(Long fileKey, String prefix) {
		Map<String,Object> item = support.fileItem(info(fileKey), false);
		return McpToolResult.of(McpToolSupport.modifyResult(item, RESULT_FIELDS),
				prefix + ProjectToolSupport.fileLine(item) + McpToolSupport.fullItemHint(FileQueryToolProvider.NAME));
	}

	static Map<String,Object> outputSchema() {
		return McpToolSupport.modifyOutputSchema(ProjectToolSupport.fileItemSchema(), RESULT_FIELDS, List.of("key", "filename", "status", "url"));
	}

	private static Map<String,Object> inputSchema() {
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("action", Map.of("type", "string", "enum", ACTIONS,
				"description", "create: upload a new file to a project; update: change the passed fields; delete: soft delete"));
		properties.put("projectKey", Map.of("type", "integer", "description", "create only, required: the key of the project"));
		properties.put("fileKey", Map.of("type", "integer", "description", "update and delete, required: the key of the file"));
		properties.put("filename", Map.of("type", "string", "maxLength", FILENAME_MAX_LENGTH,
				"description", "create: required, with extension, e.g. notes.md; update: renames the file, the extension is kept; at most " + FILENAME_MAX_LENGTH + " characters with the extension"));
		properties.put("content", Map.of("type", "string", "description", "create only: the content of a text file (UTF-8)"));
		properties.put("contentBase64", Map.of("type", "string", "description", "create only: the content of any file as standard base64 without line breaks"));
		properties.put("title", Map.of("type", "string", "maxLength", TITLE_MAX_LENGTH, "description", "an empty string clears it"));
		properties.put("description", Map.of("type", "string", "maxLength", DESCRIPTION_MAX_LENGTH, "description", "plain text, an empty string clears it"));
		properties.put("tags", ProjectToolSupport.tagsInputSchema());
		properties.put("members", ProjectToolSupport.membersInputSchema("file"));
		properties.put("links", ProjectToolSupport.linksInputSchema());
		return Map.of("type", "object", "properties", properties, "required", List.of("action"), "additionalProperties", Boolean.FALSE);
	}
}
