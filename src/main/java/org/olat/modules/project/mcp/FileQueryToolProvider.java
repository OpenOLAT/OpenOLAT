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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.mcp.tool.McpBinaryContent;
import org.olat.mcp.tool.McpPermissionException;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolDefinition;
import org.olat.mcp.tool.McpToolProvider;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.project.ProjArtefactInfoParams;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjFileInfo;
import org.olat.modules.project.ProjFileSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The files of one project as the files list of the project shows them
 * (ProjFileListController), and one file with its metadata, its links and
 * optionally its content: text inline, images as image content, every other
 * type as an embedded binary resource.
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class FileQueryToolProvider implements McpToolProvider {

	public static final String NAME = "file_query";

	static final String ACTION_LIST = "list";
	static final String ACTION_GET = "get";
	private static final List<String> ACTIONS = List.of(ACTION_LIST, ACTION_GET);
	static final int SUFFIX_MAX_LENGTH = 32;

	private static final McpToolDefinition DEFINITION = new McpToolDefinition(
			"OpenOlat: project files",
			"Files",
			"Returns the files (documents) of an OpenOlat project with the metadata title, description, tags, involved members and links "
			+ "to other artefacts of the project (ToDos, notes, files, appointments, milestones, decisions). The files of a project are a flat list. "
			+ "Use action list with projectKey (from project_query) and the filters status, suffix (file extension), createdByMe and createdAfter, "
			+ "sorted by the last modification. Use action get with fileKey for the metadata and the links; add content=true to receive the content: "
			+ "text files (up to 1 MiB) as the field text, images and other files (up to 4 MiB) as a binary content part. "
			+ "To upload, edit or delete a file call file_modify.",
			inputSchema(), outputSchema(), false, true);

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
		return McpToolScope.READ;
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
		String action = McpToolSupport.string(arguments, "action", ACTION_LIST);
		return switch(action) {
			case ACTION_LIST -> list(context, arguments);
			case ACTION_GET -> get(context, arguments);
			default -> McpToolResult.error("Unknown action " + action + ". Use one of: " + String.join(", ", ACTIONS) + ".");
		};
	}

	private McpToolResult list(McpToolContext context, Map<String,Object> arguments) {
		Long projectKey = McpToolSupport.longValue(arguments, "projectKey");
		if(projectKey == null) {
			return McpToolResult.error("The action list needs the argument projectKey. Call project_query to list your projects.");
		}
		String statusValue = McpToolSupport.string(arguments, "status", ProjectStatus.active.name());
		List<ProjectStatus> status = ProjectToolSupport.statusFilter(statusValue, ProjectToolSupport.ARTEFACT_STATUS);
		if(status == null) {
			return McpToolResult.error(ProjectToolSupport.unknownStatusFilter(statusValue, ProjectToolSupport.ARTEFACT_STATUS));
		}
		String suffix = McpToolSupport.string(arguments, "suffix", null);
		McpToolSupport.checkLength("suffix", suffix, SUFFIX_MAX_LENGTH);
		boolean createdByMe = McpToolSupport.bool(arguments, "createdByMe", false);
		Date createdAfter = McpToolSupport.date(arguments, "createdAfter");
		int limit = McpToolSupport.limit(arguments);

		ProjProject project = projectService.getProject(() -> projectKey);
		// an unknown key gets the answer of a foreign project
		if(project == null || !support.projectCallback(context, project).canViewFiles()) {
			throw new McpPermissionException(ProjectToolSupport.describeProject(projectKey), "view the files");
		}

		// The search parameters of ProjFileListController.createSearchParams and its filters
		ProjFileSearchParams searchParams = new ProjFileSearchParams();
		searchParams.setProject(project);
		if(!status.isEmpty()) {
			searchParams.setStatus(status);
		}
		if(suffix != null) {
			searchParams.setSuffixes(List.of(suffix.startsWith(".") ? suffix.substring(1).toLowerCase() : suffix.toLowerCase()));
		}
		if(createdByMe) {
			searchParams.setCreators(List.of(context.identity()));
		}
		searchParams.setCreatedAfter(createdAfter);
		// ProjectService has no paged query; the UI loads the same set
		List<ProjFileInfo> infos = new ArrayList<>(projectService.getFileInfos(searchParams, ProjArtefactInfoParams.of(true, false, true)));
		infos.sort(Comparator.comparing((ProjFileInfo info) -> info.getFile().getArtefact().getContentModifiedDate(), Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(info -> info.getFile().getKey(), Comparator.reverseOrder()));
		int total = infos.size();

		List<Map<String,Object>> items = new ArrayList<>();
		List<String> lines = new ArrayList<>();
		for(ProjFileInfo info:infos.subList(0, Math.min(limit, total))) {
			Map<String,Object> item = support.fileItem(info, false);
			items.add(item);
			lines.add(ProjectToolSupport.fileLine(item));
		}
		return McpToolResult.of(McpToolSupport.listResult(items, total, limit), McpToolSupport.listText(lines, total, "files"));
	}

	private McpToolResult get(McpToolContext context, Map<String,Object> arguments) {
		Long fileKey = McpToolSupport.longValue(arguments, "fileKey");
		if(fileKey == null) {
			return McpToolResult.error("The action get needs the argument fileKey. Call file_query with action list to find it.");
		}
		boolean withContent = McpToolSupport.bool(arguments, "content", false);
		ProjFileSearchParams searchParams = new ProjFileSearchParams();
		searchParams.setFiles(List.of(() -> fileKey));
		List<ProjFileInfo> infos = projectService.getFileInfos(searchParams, ProjArtefactInfoParams.of(true, false, true));
		// an unknown key gets the answer of a foreign file
		if(infos.isEmpty()) {
			throw new McpPermissionException("file with key " + fileKey, "view the file");
		}
		ProjFileInfo info = infos.get(0);
		ProjFile file = info.getFile();
		ProjProject project = file.getArtefact().getProject();
		if(!support.projectCallback(context, project).canViewFiles()) {
			throw new McpPermissionException("file with key " + fileKey, "view the file");
		}
		Map<String,Object> item = support.fileItem(info, true);
		Map<String,Object> result = new LinkedHashMap<>();
		result.put("item", item);
		String line = "File " + ProjectToolSupport.fileLine(item);
		if(!withContent) {
			return McpToolResult.of(result, line);
		}

		// The content, as the download of ProjFileListController
		String mimeType = (String)item.get("mimeType");
		long size = ((Long)item.get("size")).longValue();
		boolean text = ProjectToolSupport.isText(mimeType);
		long max = text ? ProjectToolSupport.FILE_TEXT_MAX_BYTES : ProjectToolSupport.FILE_BINARY_MAX_BYTES;
		if(size > max) {
			return tooLarge(item, String.valueOf(size), text, max);
		}
		if(!(vfsRepositoryService.getItemFor(file.getVfsMetadata()) instanceof VFSLeaf leaf)) {
			return McpToolResult.error("The content of the file " + item.get("filename") + " is not available.");
		}
		// The size of the metadata can be out of date: read at most one byte more than the limit
		byte[] bytes;
		try(InputStream in = leaf.getInputStream()) {
			bytes = in == null ? new byte[0] : in.readNBytes((int)max + 1);
		} catch (IOException e) {
			throw new IllegalStateException("Cannot read the file " + fileKey, e);
		}
		if(bytes.length > max) {
			return tooLarge(item, "more than " + max, text, max);
		}
		projectService.createActivityDownload(context.identity(), file.getArtefact());

		if(text) {
			String content = new String(bytes, StandardCharsets.UTF_8);
			item.put("content", "text");
			item.put("text", content);
			return McpToolResult.of(result, line + "\n\n" + content);
		}
		boolean image = McpBinaryContent.isInlineImage(mimeType);
		item.put("content", image ? "image" : "blob");
		String uri = "openolat://project/" + project.getKey() + "/file/" + fileKey + "/" + FileUtils.cleanFilename((String)item.get("filename"));
		return McpToolResult.of(result, line + (image ? "\nThe image follows as image content." : "\nThe content follows as an embedded resource (base64)."),
				List.of(new McpBinaryContent(uri, mimeType, bytes)));
	}

	private static McpToolResult tooLarge(Map<String,Object> item, String size, boolean text, long max) {
		return McpToolResult.error("The file " + item.get("filename") + " has " + size + " bytes, the content limit of a "
				+ (text ? "text" : "binary") + " file is " + max + " bytes. Open it in OpenOlat: " + item.get("url"));
	}

	private static Map<String,Object> inputSchema() {
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("action", Map.of("type", "string", "enum", ACTIONS, "default", ACTION_LIST,
				"description", "list: the files of a project; get: one file with the links and optionally the content"));
		properties.put("projectKey", Map.of("type", "integer", "description", "list only, required: the key of the project"));
		properties.put("status", ProjectToolSupport.artefactStatusFilterSchema("files"));
		properties.put("suffix", Map.of("type", "string", "maxLength", SUFFIX_MAX_LENGTH,
				"description", "list only: file extension without the dot, e.g. pdf"));
		properties.put("createdByMe", Map.of("type", "boolean", "default", Boolean.FALSE,
				"description", "list only: only the files the account uploaded"));
		properties.put("createdAfter", Map.of("type", "string", "format", "date",
				"description", "list only: files created on or after this day (yyyy-MM-dd)"));
		properties.put("fileKey", Map.of("type", "integer", "description", "get only, required: the key of the file"));
		properties.put("content", Map.of("type", "boolean", "default", Boolean.FALSE,
				"description", "get only: true returns the content, text up to 1 MiB inline, images and other files up to 4 MiB as a binary part"));
		properties.put("limit", Map.of("type", "integer", "minimum", 1, "maximum", McpToolSupport.MAX_LIMIT,
				"default", McpToolSupport.DEFAULT_LIMIT, "description", "list only: maximum number of files returned"));
		return Map.of("type", "object", "properties", properties, "additionalProperties", Boolean.FALSE);
	}

	static Map<String,Object> outputSchema() {
		Map<String,Object> itemSchema = ProjectToolSupport.fileItemSchema();
		return McpToolSupport.queryOutputSchema(itemSchema, ProjectToolSupport.outputDefs());
	}
}
