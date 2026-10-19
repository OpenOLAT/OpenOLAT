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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.mcp.tool.McpInvalidArgumentException;
import org.olat.mcp.tool.McpPermissionException;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolDefinition;
import org.olat.mcp.tool.McpToolProvider;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.mcp.tool.McpToolSupport;
import org.olat.modules.project.ProjArtefactInfoParams;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjMilestoneInfo;
import org.olat.modules.project.ProjMilestoneSearchParams;
import org.olat.modules.project.ProjMilestoneStatus;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creates, edits and deletes the milestones of a project through the
 * ProjectService, the sequence of ProjMilestoneEditController and
 * ProjMilestoneContentEditController. As in the UI a milestone has no
 * involved members and no links: the project service has no activity for
 * the members and the links of a milestone.
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class MilestoneModifyToolProvider implements McpToolProvider {

	public static final String NAME = "milestone_modify";

	static final String ACTION_CREATE = "create";
	static final String ACTION_UPDATE = "update";
	static final String ACTION_SET_STATUS = "set_status";
	static final String ACTION_DELETE = "delete";
	private static final List<String> ACTIONS = List.of(ACTION_CREATE, ACTION_UPDATE, ACTION_SET_STATUS, ACTION_DELETE);
	/** The fields of the result, the full milestone comes from milestone_query get */
	static final List<String> RESULT_FIELDS = List.of("key", "subject", "milestoneStatus", "status", "url");
	private static final List<String> CONTENT_FIELDS = List.of("subject", "description", "dueDate", "milestoneStatus", "color");

	private static final McpToolDefinition DEFINITION = new McpToolDefinition(
			"OpenOlat: change project milestones",
			"Milestones",
			"Creates, edits or deletes a milestone of an OpenOlat project and returns its key, subject, milestone status, status and URL; milestone_query get returns the full milestone. "
			+ "create needs projectKey and subject; optional description, dueDate (yyyy-MM-dd), milestoneStatus (open default, achieved), "
			+ "color and tags. update changes only the arguments you pass; tags replace the current list, "
			+ "an empty string clears the description, the due date or the color, the subject cannot be cleared. "
			+ "set_status marks a milestone open or achieved. delete is a soft delete. "
			+ "OpenOlat has no involved members and no links for milestones. A milestone of a template project has no due date. "
			+ "Call project_query for the projectKey and milestone_query for the milestoneKey.",
			inputSchema(), outputSchema(), true, false);

	@Autowired
	private ProjectService projectService;
	@Autowired
	private ProjectToolSupport support;
	@Autowired
	private MilestoneToolSupport milestoneSupport;

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
			case ACTION_SET_STATUS -> setStatus(context, arguments);
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
			throw new McpPermissionException(ProjectToolSupport.describeProject(projectKey), "create milestones");
		}
		ProjProjectSecurityCallback secCallback = support.projectCallback(context, project);
		McpToolResult stateError = secCallback.canViewMilestones() ? ProjectToolSupport.stateError(project, null, null) : null;
		if(stateError != null) {
			return stateError;
		}
		if(!secCallback.canCreateMilestones()) {
			throw new McpPermissionException(ProjectToolSupport.describeProject(projectKey), "create milestones");
		}
		// Validate every argument before anything is written
		checkNoMembersAndLinks(arguments);
		String subject = McpToolSupport.string(arguments, "subject", null);
		if(subject == null) {
			return McpToolResult.error("The action create needs the argument subject.");
		}
		McpToolSupport.checkLength("subject", subject, MilestoneToolSupport.SUBJECT_MAX_LENGTH);
		String description = McpToolSupport.string(arguments, "description", null);
		McpToolSupport.checkLength("description", description, MilestoneToolSupport.DESCRIPTION_MAX_LENGTH);
		Date dueDate = dueDate(arguments, project);
		ProjMilestoneStatus milestoneStatus = milestoneStatus(arguments, ProjMilestoneStatus.open);
		String color = color(arguments);
		List<String> tags = support.tags(arguments, project);

		// The sequence of ProjMilestoneContentEditController.formOK
		Identity doer = context.identity();
		ProjectBCFactory bcFactory = ProjectBCFactory.createFactory(project);
		ProjMilestone milestone = projectService.createMilestone(doer, bcFactory, project);
		projectService.updateMilestone(doer, bcFactory, milestone, milestoneStatus, dueDate, subject, description, color);
		if(!tags.isEmpty()) {
			projectService.updateTags(doer, milestone.getArtefact(), tags);
		}
		return result(milestone.getKey(), "Created: ");
	}

	private McpToolResult update(McpToolContext context, Map<String,Object> arguments) {
		ProjMilestone milestone = loadMilestone(arguments);
		ProjProject project = milestone.getArtefact().getProject();
		McpToolResult stateError = checkEdit(context, milestone);
		if(stateError != null) {
			return stateError;
		}
		// Validate every argument before anything is written; an absent argument keeps the value, an empty string clears it
		checkNoMembersAndLinks(arguments);
		boolean contentChanged = CONTENT_FIELDS.stream().anyMatch(arguments::containsKey);
		String subject = milestone.getSubject();
		if(arguments.containsKey("subject")) {
			subject = McpToolSupport.string(arguments, "subject", null);
			if(subject == null) {
				return McpToolResult.error("A milestone needs a subject.");
			}
			McpToolSupport.checkLength("subject", subject, MilestoneToolSupport.SUBJECT_MAX_LENGTH);
		}
		String description = arguments.containsKey("description") ? McpToolSupport.string(arguments, "description", null) : milestone.getDescription();
		McpToolSupport.checkLength("description", description, MilestoneToolSupport.DESCRIPTION_MAX_LENGTH);
		Date dueDate = arguments.containsKey("dueDate") ? dueDate(arguments, project) : milestone.getDueDate();
		ProjMilestoneStatus milestoneStatus = milestoneStatus(arguments, milestone.getStatus());
		String color = arguments.containsKey("color") ? color(arguments) : milestone.getColor();
		List<String> tags = support.tags(arguments, project);

		// The sequence of ProjMilestoneContentEditController.formOK
		Identity doer = context.identity();
		if(contentChanged) {
			projectService.updateMilestone(doer, ProjectBCFactory.createFactory(project), milestone, milestoneStatus, dueDate, subject, description, color);
		}
		if(arguments.containsKey("tags")) {
			projectService.updateTags(doer, milestone.getArtefact(), tags);
		}
		return result(milestone.getKey(), "Updated: ");
	}

	private McpToolResult setStatus(McpToolContext context, Map<String,Object> arguments) {
		ProjMilestone milestone = loadMilestone(arguments);
		McpToolResult stateError = checkEdit(context, milestone);
		if(stateError != null) {
			return stateError;
		}
		if(McpToolSupport.string(arguments, "milestoneStatus", null) == null) {
			return McpToolResult.error("The action set_status needs the argument milestoneStatus: "
					+ String.join(", ", McpToolSupport.names(MilestoneToolSupport.MILESTONE_STATUS)) + ".");
		}
		ProjMilestoneStatus milestoneStatus = milestoneStatus(arguments, null);
		// ProjCalendarAllController.doAcomplishMilestone
		projectService.updateMilestoneStatus(context.identity(), ProjectBCFactory.createFactory(milestone.getArtefact().getProject()),
				milestone, milestoneStatus);
		return result(milestone.getKey(), "Status changed: ");
	}

	private McpToolResult delete(McpToolContext context, Map<String,Object> arguments) {
		ProjMilestone milestone = loadMilestone(arguments);
		ProjProject project = milestone.getArtefact().getProject();
		ProjProjectSecurityCallback secCallback = support.projectCallback(context, project);
		if(!secCallback.canViewMilestones()) {
			throw new McpPermissionException("milestone with key " + milestone.getKey(), "view the milestone");
		}
		McpToolResult stateError = ProjectToolSupport.stateError(project, milestone.getArtefact(), "milestone " + milestone.getKey());
		if(stateError != null) {
			return stateError;
		}
		if(!secCallback.canDeleteMilestone(milestone, context.identity())) {
			throw new McpPermissionException("milestone with key " + milestone.getKey(), "delete the milestone");
		}
		projectService.deleteMilestoneSoftly(context.identity(), milestone);
		return result(milestone.getKey(), "Deleted: ");
	}

	/**
	 * @return The state error of a deleted project or milestone, null if the milestone can be edited
	 * @throws McpPermissionException if the caller may not view or edit the milestone
	 */
	private McpToolResult checkEdit(McpToolContext context, ProjMilestone milestone) {
		ProjProject project = milestone.getArtefact().getProject();
		ProjProjectSecurityCallback secCallback = support.projectCallback(context, project);
		if(!secCallback.canViewMilestones()) {
			throw new McpPermissionException("milestone with key " + milestone.getKey(), "view the milestone");
		}
		McpToolResult stateError = ProjectToolSupport.stateError(project, milestone.getArtefact(), "milestone " + milestone.getKey());
		if(stateError != null) {
			return stateError;
		}
		if(!secCallback.canEditMilestone(milestone)) {
			throw new McpPermissionException("milestone with key " + milestone.getKey(), "edit the milestone");
		}
		return null;
	}

	private static void checkNoMembersAndLinks(Map<String,Object> arguments) {
		if(arguments.containsKey("members") || arguments.containsKey("links")) {
			throw new McpInvalidArgumentException("OpenOlat has no involved members and no links for milestones.");
		}
	}

	/**
	 * @return The due date of the argument dueDate, null when absent or empty
	 */
	private static Date dueDate(Map<String,Object> arguments, ProjProject project) {
		Date dueDate = McpToolSupport.date(arguments, "dueDate");
		// ProjMilestoneContentEditController disables the due date in a template
		if(dueDate != null && ProjectToolSupport.isTemplate(project)) {
			throw new McpInvalidArgumentException("A milestone of a template project has no due date.");
		}
		return dueDate;
	}

	private static ProjMilestoneStatus milestoneStatus(Map<String,Object> arguments, ProjMilestoneStatus defaultStatus) {
		String value = McpToolSupport.string(arguments, "milestoneStatus", null);
		if(value == null) {
			return defaultStatus;
		}
		ProjMilestoneStatus status = McpToolSupport.enumValue(MilestoneToolSupport.MILESTONE_STATUS, value);
		if(status == null) {
			throw new McpInvalidArgumentException(McpToolSupport.unknownValue("milestoneStatus", value, MilestoneToolSupport.MILESTONE_STATUS));
		}
		return status;
	}

	/**
	 * @return The color of the argument color, null when absent or empty (the default milestone color)
	 */
	private static String color(Map<String,Object> arguments) {
		String color = McpToolSupport.string(arguments, "color", null);
		if(color != null && !MilestoneToolSupport.COLORS.contains(color)) {
			throw new McpInvalidArgumentException("Unknown color " + color + ". Use one of: " + String.join(", ", MilestoneToolSupport.COLORS) + ".");
		}
		return color;
	}

	private ProjMilestone loadMilestone(Map<String,Object> arguments) {
		Long milestoneKey = McpToolSupport.longValue(arguments, "milestoneKey");
		if(milestoneKey == null) {
			throw new McpInvalidArgumentException("This action needs the argument milestoneKey. Call milestone_query to find it.");
		}
		ProjMilestone milestone = projectService.getMilestone(() -> milestoneKey);
		// an unknown key gets the answer of a foreign milestone
		if(milestone == null) {
			throw new McpPermissionException("milestone with key " + milestoneKey, "view the milestone");
		}
		return milestone;
	}

	private McpToolResult result(Long milestoneKey, String prefix) {
		ProjMilestoneSearchParams searchParams = new ProjMilestoneSearchParams();
		searchParams.setMilestones(List.of(() -> milestoneKey));
		ProjMilestoneInfo info = projectService.getMilestoneInfos(searchParams, ProjArtefactInfoParams.of(true, false, true)).get(0);
		Map<String,Object> item = milestoneSupport.milestoneItem(info, false);
		return McpToolResult.of(McpToolSupport.modifyResult(item, RESULT_FIELDS),
				prefix + MilestoneToolSupport.milestoneLine(item) + McpToolSupport.fullItemHint(MilestoneQueryToolProvider.NAME));
	}

	static Map<String,Object> outputSchema() {
		return McpToolSupport.modifyOutputSchema(MilestoneToolSupport.milestoneItemSchema(), RESULT_FIELDS, List.of("key", "milestoneStatus", "status", "url"));
	}

	private static Map<String,Object> inputSchema() {
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("action", Map.of("type", "string", "enum", ACTIONS,
				"description", "create: a new milestone in a project; update: change the passed fields; "
						+ "set_status: mark open or achieved; delete: soft delete"));
		properties.put("projectKey", Map.of("type", "integer", "description", "create only, required: the key of the project"));
		properties.put("milestoneKey", Map.of("type", "integer", "description", "update, set_status and delete, required: the key of the milestone"));
		properties.put("subject", Map.of("type", "string", "maxLength", MilestoneToolSupport.SUBJECT_MAX_LENGTH,
				"description", "create: required; update: cannot be cleared"));
		properties.put("description", Map.of("type", "string", "maxLength", MilestoneToolSupport.DESCRIPTION_MAX_LENGTH,
				"description", "plain text, an empty string clears it"));
		properties.put("dueDate", Map.of("type", "string", "description", "the due day (yyyy-MM-dd), an empty string clears it; not in a template project"));
		properties.put("milestoneStatus", Map.of("type", "string", "enum", McpToolSupport.names(MilestoneToolSupport.MILESTONE_STATUS),
				"description", MilestoneToolSupport.MILESTONE_STATUS_WORDING + "; create: default open; set_status: required"));
		List<String> colors = new ArrayList<>(MilestoneToolSupport.COLORS);
		colors.add("");
		properties.put("color", Map.of("type", "string", "enum", colors,
				"description", "the calendar color, an empty string resets it to the default milestone color"));
		properties.put("tags", ProjectToolSupport.tagsInputSchema());
		return Map.of("type", "object", "properties", properties, "required", List.of("action"), "additionalProperties", Boolean.FALSE);
	}
}
