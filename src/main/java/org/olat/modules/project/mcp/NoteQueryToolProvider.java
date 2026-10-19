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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.olat.mcp.tool.McpPermissionException;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolDefinition;
import org.olat.mcp.tool.McpToolProvider;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.project.ProjArtefactInfoParams;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjNoteInfo;
import org.olat.modules.project.ProjNoteSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The notes of one project as the notes list of the project shows them
 * (ProjNoteListController), and one note with its markdown text and its
 * links to other artefacts of the project.
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class NoteQueryToolProvider implements McpToolProvider {

	public static final String NAME = "note_query";

	static final String ACTION_LIST = "list";
	static final String ACTION_GET = "get";
	private static final List<String> ACTIONS = List.of(ACTION_LIST, ACTION_GET);

	private static final McpToolDefinition DEFINITION = new McpToolDefinition(
			"OpenOlat: project notes",
			"Notes",
			"Returns the notes of an OpenOlat project: markdown documents with the metadata tags, involved members and links to other "
			+ "artefacts of the project (ToDos, notes, files, appointments, milestones, decisions). "
			+ "Use action list with projectKey (from project_query) and the filters status, createdByMe and createdAfter; "
			+ "list returns a preview of the text, sorted by the last modification. "
			+ "Use action get with noteKey for the full markdown text and the links; to change a note call note_modify.",
			inputSchema(), outputSchema(), false, true);

	@Autowired
	private ProjectService projectService;
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
		boolean createdByMe = McpToolSupport.bool(arguments, "createdByMe", false);
		Date createdAfter = McpToolSupport.date(arguments, "createdAfter");
		int limit = McpToolSupport.limit(arguments);

		ProjProject project = projectService.getProject(() -> projectKey);
		// an unknown key gets the answer of a foreign project
		if(project == null || !support.projectCallback(context, project).canViewNotes()) {
			throw new McpPermissionException(ProjectToolSupport.describeProject(projectKey), "view the notes");
		}

		// The search parameters of ProjNoteListController.createSearchParams and its filters
		ProjNoteSearchParams searchParams = new ProjNoteSearchParams();
		searchParams.setProject(project);
		if(!status.isEmpty()) {
			searchParams.setStatus(status);
		}
		if(createdByMe) {
			searchParams.setCreators(List.of(context.identity()));
		}
		searchParams.setCreatedAfter(createdAfter);
		// ProjectService has no paged query; the UI loads the same set
		List<ProjNoteInfo> infos = new ArrayList<>(projectService.getNoteInfos(searchParams, ProjArtefactInfoParams.of(true, false, true)));
		infos.sort(Comparator.comparing((ProjNoteInfo info) -> info.getNote().getArtefact().getContentModifiedDate(), Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(info -> info.getNote().getKey(), Comparator.reverseOrder()));
		int total = infos.size();

		List<Map<String,Object>> items = new ArrayList<>();
		List<String> lines = new ArrayList<>();
		for(ProjNoteInfo info:infos.subList(0, Math.min(limit, total))) {
			Map<String,Object> item = support.noteItem(info, false);
			items.add(item);
			lines.add(ProjectToolSupport.noteLine(item));
		}
		return McpToolResult.of(McpToolSupport.listResult(items, total, limit), McpToolSupport.listText(lines, total, "notes"));
	}

	private McpToolResult get(McpToolContext context, Map<String,Object> arguments) {
		Long noteKey = McpToolSupport.longValue(arguments, "noteKey");
		if(noteKey == null) {
			return McpToolResult.error("The action get needs the argument noteKey. Call note_query with action list to find it.");
		}
		ProjNoteSearchParams searchParams = new ProjNoteSearchParams();
		searchParams.setNotes(List.of(() -> noteKey));
		List<ProjNoteInfo> infos = projectService.getNoteInfos(searchParams, ProjArtefactInfoParams.of(true, false, true));
		// an unknown key gets the answer of a foreign note
		if(infos.isEmpty()) {
			throw new McpPermissionException("note with key " + noteKey, "view the note");
		}
		ProjNoteInfo info = infos.get(0);
		ProjNote note = info.getNote();
		ProjProject project = note.getArtefact().getProject();
		if(!support.projectCallback(context, project).canViewNotes()) {
			throw new McpPermissionException("note with key " + noteKey, "view the note");
		}
		Map<String,Object> item = support.noteItem(info, true);
		Map<String,Object> result = new LinkedHashMap<>();
		result.put("item", item);
		return McpToolResult.of(result, "Note " + ProjectToolSupport.noteLine(item) + "\n\n" + item.get("text"));
	}

	private static Map<String,Object> inputSchema() {
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("action", Map.of("type", "string", "enum", ACTIONS, "default", ACTION_LIST,
				"description", "list: the notes of a project with a preview; get: one note with the full text and the links"));
		properties.put("projectKey", Map.of("type", "integer", "description", "list only, required: the key of the project"));
		properties.put("status", ProjectToolSupport.artefactStatusFilterSchema("notes"));
		properties.put("createdByMe", Map.of("type", "boolean", "default", Boolean.FALSE,
				"description", "list only: only the notes the account created"));
		properties.put("createdAfter", Map.of("type", "string", "format", "date",
				"description", "list only: notes created on or after this day (yyyy-MM-dd)"));
		properties.put("noteKey", Map.of("type", "integer", "description", "get only, required: the key of the note"));
		properties.put("limit", Map.of("type", "integer", "minimum", 1, "maximum", McpToolSupport.MAX_LIMIT,
				"default", McpToolSupport.DEFAULT_LIMIT, "description", "list only: maximum number of notes returned"));
		return Map.of("type", "object", "properties", properties, "additionalProperties", Boolean.FALSE);
	}

	static Map<String,Object> outputSchema() {
		Map<String,Object> itemSchema = ProjectToolSupport.noteItemSchema();
		return McpToolSupport.queryOutputSchema(itemSchema, ProjectToolSupport.outputDefs());
	}
}
