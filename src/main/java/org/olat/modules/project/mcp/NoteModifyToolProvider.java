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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OresHelper;
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
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjNoteInfo;
import org.olat.modules.project.ProjNoteSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creates, edits and deletes the notes of a project through the
 * ProjectService, the sequence of ProjNoteEditController and its sub
 * controllers (content, references, members, metadata).
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class NoteModifyToolProvider implements McpToolProvider {

	private static final Logger log = Tracing.createLoggerFor(NoteModifyToolProvider.class);

	public static final String NAME = "note_modify";

	static final String ACTION_CREATE = "create";
	static final String ACTION_UPDATE = "update";
	static final String ACTION_DELETE = "delete";
	private static final List<String> ACTIONS = List.of(ACTION_CREATE, ACTION_UPDATE, ACTION_DELETE);
	/** The DB column o_proj_note.p_title */
	static final int TITLE_MAX_LENGTH = 128;
	static final int TEXT_MAX_LENGTH = 200_000;
	/** The fields of the result, the full note comes from note_query get */
	static final List<String> RESULT_FIELDS = List.of("key", "title", "status", "url");

	private static final McpToolDefinition DEFINITION = new McpToolDefinition(
			"OpenOlat: change project notes",
			"Notes",
			"Creates, edits or deletes a note of an OpenOlat project and returns its key, title, status and URL; note_query get returns the full note. "
			+ "A note is a markdown document; its metadata are tags, the involved project members and links to other artefacts "
			+ "of the same project (ToDos by the key of todo_query, notes, files, appointments, decisions; milestones cannot be linked). "
			+ "Call project_query for the projectKey and note_query for the noteKey. "
			+ "update changes only the arguments you pass; tags, members and links replace the current lists, "
			+ "an empty string clears the title or the text. The text is stored as passed, leading and trailing white space included. "
			+ "update is refused while a user edits the note in OpenOlat. delete is a soft delete.",
			inputSchema(), outputSchema(), true, false);

	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
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
			throw new McpPermissionException(ProjectToolSupport.describeProject(projectKey), "create notes");
		}
		ProjProjectSecurityCallback secCallback = support.projectCallback(context, project);
		McpToolResult stateError = secCallback.canViewNotes() ? ProjectToolSupport.stateError(project, null, null) : null;
		if(stateError != null) {
			return stateError;
		}
		if(!secCallback.canCreateNotes()) {
			throw new McpPermissionException(ProjectToolSupport.describeProject(projectKey), "create notes");
		}
		// Validate every argument before anything is written
		String title = McpToolSupport.string(arguments, "title", null);
		// the markdown is stored as typed, ProjNoteContentEditController does not trim it
		String text = McpToolSupport.stringUntrimmed(arguments, "text", null);
		McpToolSupport.checkLength("title", title, TITLE_MAX_LENGTH);
		McpToolSupport.checkLength("text", text, TEXT_MAX_LENGTH);
		if(title == null && text == null) {
			return McpToolResult.error("The action create needs a title or a text.");
		}
		List<String> tags = support.tags(arguments, project);
		List<Identity> members = arguments.containsKey("members") ? support.resolveMembers(arguments, "members", project, "this note") : null;
		Set<ProjArtefact> links = support.resolveLinks(arguments, project, null);

		// The sequence of ProjNoteListController.doCreateNote and ProjNoteEditController.doSave
		Identity doer = context.identity();
		ProjNote note = projectService.createNote(doer, project);
		projectService.updateNote(doer, note, UUID.randomUUID().toString(), title, text);
		if(!tags.isEmpty()) {
			projectService.updateTags(doer, note.getArtefact(), tags);
		}
		if(members != null) {
			projectService.updateMembers(doer, ProjectBCFactory.createFactory(project), note.getArtefact(), new ArrayList<IdentityRef>(members));
		}
		if(!links.isEmpty()) {
			projectService.updateLinkedArtefacts(doer, note.getArtefact(), links);
		}
		return result(note.getKey(), "Created: ");
	}

	private McpToolResult update(McpToolContext context, Map<String,Object> arguments) {
		ProjNote note = loadNote(arguments);
		ProjProject project = note.getArtefact().getProject();
		ProjProjectSecurityCallback secCallback = support.projectCallback(context, project);
		if(!secCallback.canViewNotes()) {
			throw new McpPermissionException("note with key " + note.getKey(), "view the note");
		}
		McpToolResult stateError = ProjectToolSupport.stateError(project, note.getArtefact(), "note " + note.getKey());
		if(stateError != null) {
			return stateError;
		}
		if(!secCallback.canEditNote(note)) {
			throw new McpPermissionException("note with key " + note.getKey(), "edit the note");
		}
		// Validate every argument before anything is written; an absent argument keeps the value, an empty string clears it
		boolean contentChanged = arguments.containsKey("title") || arguments.containsKey("text");
		String title = arguments.containsKey("title") ? McpToolSupport.string(arguments, "title", null) : note.getTitle();
		String text = arguments.containsKey("text") ? McpToolSupport.stringUntrimmed(arguments, "text", null) : note.getText();
		McpToolSupport.checkLength("title", title, TITLE_MAX_LENGTH);
		McpToolSupport.checkLength("text", text, TEXT_MAX_LENGTH);
		if(contentChanged && title == null && text == null) {
			return McpToolResult.error("A note needs a title or a text.");
		}
		List<String> tags = support.tags(arguments, project);
		List<Identity> members = arguments.containsKey("members") ? support.resolveMembers(arguments, "members", project, "this note") : null;
		Set<ProjArtefact> links = arguments.containsKey("links") ? support.resolveLinks(arguments, project, note.getArtefact()) : null;

		// The edit lock of ProjNoteController.doOpenEdit and ProjNoteEditController: one editor at a time
		Identity doer = context.identity();
		LockResult lock = acquireLock(doer, note);
		if(!lock.isSuccess()) {
			String displayName = userManager.getUserDisplayName(lock.getOwner().getKey());
			return McpToolResult.error("The note is being edited by " + displayName + ". Try again later.");
		}
		try {
			// The sequence of ProjNoteEditController.doSave and its sub controllers
			if(contentChanged) {
				projectService.updateNote(doer, note, UUID.randomUUID().toString(), title, text);
			}
			if(arguments.containsKey("tags")) {
				projectService.updateTags(doer, note.getArtefact(), tags);
			}
			if(members != null) {
				projectService.updateMembers(doer, ProjectBCFactory.createFactory(project), note.getArtefact(), new ArrayList<IdentityRef>(members));
			}
			if(links != null) {
				projectService.updateLinkedArtefacts(doer, note.getArtefact(), links);
			}
			// the changes are committed before the release, so a failed commit cannot restore the lock row
			dbInstance.commit();
		} catch (RuntimeException e) {
			dbInstance.rollbackAndCloseSession();
			throw e;
		} finally {
			// the release has its own transaction, it is never rolled back with the changes
			try {
				releaseLock(lock);
				dbInstance.commitAndCloseSession();
			} catch (RuntimeException e) {
				dbInstance.rollbackAndCloseSession();
				log.error("MCP note_modify cannot release the edit lock of the note {}", note.getKey(), e);
			}
		}
		return result(note.getKey(), "Updated: ");
	}

	/**
	 * Acquires the lock of the note edit form without a window, as the REST
	 * API does for its locks. The locker commits the open transaction. The
	 * lock fails if a user holds it in a browser window, the caller included.
	 */
	private LockResult acquireLock(Identity doer, ProjNote note) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ProjNote.class, note.getKey());
		return CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(ores, doer, "", null);
	}

	private void releaseLock(LockResult lock) {
		CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lock);
	}

	private McpToolResult delete(McpToolContext context, Map<String,Object> arguments) {
		ProjNote note = loadNote(arguments);
		ProjProject project = note.getArtefact().getProject();
		ProjProjectSecurityCallback secCallback = support.projectCallback(context, project);
		if(!secCallback.canViewNotes()) {
			throw new McpPermissionException("note with key " + note.getKey(), "view the note");
		}
		McpToolResult stateError = ProjectToolSupport.stateError(project, note.getArtefact(), "note " + note.getKey());
		if(stateError != null) {
			return stateError;
		}
		if(!secCallback.canDeleteNote(note, context.identity())) {
			throw new McpPermissionException("note with key " + note.getKey(), "delete the note");
		}
		projectService.deleteNoteSoftly(context.identity(), note);
		return result(note.getKey(), "Deleted: ");
	}

	private ProjNote loadNote(Map<String,Object> arguments) {
		Long noteKey = McpToolSupport.longValue(arguments, "noteKey");
		if(noteKey == null) {
			throw new McpInvalidArgumentException("This action needs the argument noteKey. Call note_query to find it.");
		}
		ProjNote note = projectService.getNote(() -> noteKey);
		// an unknown key gets the answer of a foreign note
		if(note == null) {
			throw new McpPermissionException("note with key " + noteKey, "view the note");
		}
		return note;
	}

	private McpToolResult result(Long noteKey, String prefix) {
		ProjNoteSearchParams searchParams = new ProjNoteSearchParams();
		searchParams.setNotes(List.of(() -> noteKey));
		ProjNoteInfo info = projectService.getNoteInfos(searchParams, ProjArtefactInfoParams.of(true, false, true)).get(0);
		Map<String,Object> item = support.noteItem(info, false);
		return McpToolResult.of(McpToolSupport.modifyResult(item, RESULT_FIELDS),
				prefix + ProjectToolSupport.noteLine(item) + McpToolSupport.fullItemHint(NoteQueryToolProvider.NAME));
	}

	static Map<String,Object> outputSchema() {
		return McpToolSupport.modifyOutputSchema(ProjectToolSupport.noteItemSchema(), RESULT_FIELDS, List.of("key", "status", "url"));
	}

	private static Map<String,Object> inputSchema() {
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("action", Map.of("type", "string", "enum", ACTIONS,
				"description", "create: a new note in a project; update: change the passed fields; delete: soft delete"));
		properties.put("projectKey", Map.of("type", "integer", "description", "create only, required: the key of the project"));
		properties.put("noteKey", Map.of("type", "integer", "description", "update and delete, required: the key of the note"));
		properties.put("title", Map.of("type", "string", "maxLength", TITLE_MAX_LENGTH,
				"description", "create needs a title or a text; an empty string clears it"));
		properties.put("text", Map.of("type", "string", "maxLength", TEXT_MAX_LENGTH,
				"description", "the note as markdown, stored as passed without trimming; an empty string clears it"));
		properties.put("tags", ProjectToolSupport.tagsInputSchema());
		properties.put("members", ProjectToolSupport.membersInputSchema("note"));
		properties.put("links", ProjectToolSupport.linksInputSchema());
		return Map.of("type", "object", "properties", properties, "required", List.of("action"), "additionalProperties", Boolean.FALSE);
	}
}
