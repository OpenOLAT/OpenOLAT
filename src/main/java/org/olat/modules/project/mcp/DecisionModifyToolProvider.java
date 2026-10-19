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
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
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
import org.olat.modules.project.ProjDecision;
import org.olat.modules.project.ProjDecisionInfo;
import org.olat.modules.project.ProjDecisionSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creates, edits and deletes the decisions of a project through the
 * ProjectService, the sequence of ProjDecisionEditController and its sub
 * controllers (content with tags, references, members).
 *
 * Initial date: 3 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class DecisionModifyToolProvider implements McpToolProvider {

	public static final String NAME = "decision_modify";

	static final String ACTION_CREATE = "create";
	static final String ACTION_UPDATE = "update";
	static final String ACTION_DELETE = "delete";
	private static final List<String> ACTIONS = List.of(ACTION_CREATE, ACTION_UPDATE, ACTION_DELETE);
	/** The fields of the result, the full decision comes from decision_query get */
	static final List<String> RESULT_FIELDS = List.of("key", "title", "status", "url");
	/** The maximum length of the title field of ProjDecisionContentEditController */
	static final int TITLE_MAX_LENGTH = 1000;
	static final int DETAILS_MAX_LENGTH = 200_000;
	private static final List<String> CONTENT_ARGUMENTS = List.of("title", "details", "decisionDate");

	private static final McpToolDefinition DEFINITION = new McpToolDefinition(
			"OpenOlat: change project decisions",
			"Decisions",
			"Creates, edits or deletes a decision of an OpenOlat project and returns its key, title, status and URL; decision_query get returns the full decision. "
			+ "A decision has a title, plain text details and a decision date; its metadata are tags, the involved project members "
			+ "and links to other artefacts of the same project (ToDos by the key of todo_query, notes, files, appointments, decisions; milestones cannot be linked). "
			+ "Call project_query for the projectKey and decision_query for the decisionKey. "
			+ "create needs a title, the decision date (ISO date-time or date) defaults to now, in a template project to none. "
			+ "update changes only the arguments you pass; tags, members and links replace the current lists, "
			+ "an empty string clears the details or the decision date, an empty array clears tags or links; members needs at least one member. delete is a soft delete.",
			inputSchema(), outputSchema(), true, false);

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
			throw new McpPermissionException(ProjectToolSupport.describeProject(projectKey), "create decisions");
		}
		ProjProjectSecurityCallback secCallback = support.projectCallback(context, project);
		McpToolResult stateError = secCallback.canViewDecisions() ? ProjectToolSupport.stateError(project, null, null) : null;
		if(stateError != null) {
			return stateError;
		}
		if(!secCallback.canCreateDecisions()) {
			throw new McpPermissionException(ProjectToolSupport.describeProject(projectKey), "create decisions");
		}
		// Validate every argument before anything is written
		String title = McpToolSupport.string(arguments, "title", null);
		if(title == null) {
			return McpToolResult.error("The action create needs the argument title.");
		}
		McpToolSupport.checkLength("title", title, TITLE_MAX_LENGTH);
		String details = McpToolSupport.string(arguments, "details", null);
		McpToolSupport.checkLength("details", details, DETAILS_MAX_LENGTH);
		// ProjDecisionContentEditController preselects now, except in a template
		boolean template = ProjectToolSupport.isTemplate(project);
		Date decisionDate = arguments.containsKey("decisionDate") ? DecisionToolSupport.decisionDate(arguments, "decisionDate") : (template ? null : new Date());
		List<String> tags = support.tags(arguments, project);
		List<Identity> members = arguments.containsKey("members") ? support.resolveMembers(arguments, "members", project, "this decision") : null;
		Set<ProjArtefact> links = support.resolveLinks(arguments, project, null);

		// The sequence of ProjDecisionEditController.formOK: content with tags, references, members
		Identity doer = context.identity();
		ProjDecision decision = projectService.createDecision(doer, project);
		projectService.updateDecision(doer, decision, title, details, decisionDate);
		if(!tags.isEmpty()) {
			projectService.updateTags(doer, decision.getArtefact(), tags);
		}
		if(!links.isEmpty()) {
			projectService.updateLinkedArtefacts(doer, decision.getArtefact(), links);
		}
		if(members != null) {
			projectService.updateMembers(doer, ProjectBCFactory.createFactory(project), decision.getArtefact(), new ArrayList<IdentityRef>(members));
		}
		return result(decision.getKey(), "Created: ");
	}

	private McpToolResult update(McpToolContext context, Map<String,Object> arguments) {
		ProjDecision decision = loadDecision(arguments);
		ProjProject project = decision.getArtefact().getProject();
		ProjProjectSecurityCallback secCallback = support.projectCallback(context, project);
		if(!secCallback.canViewDecisions()) {
			throw new McpPermissionException("decision with key " + decision.getKey(), "view the decision");
		}
		McpToolResult stateError = ProjectToolSupport.stateError(project, decision.getArtefact(), "decision " + decision.getKey());
		if(stateError != null) {
			return stateError;
		}
		if(!secCallback.canEditDecision(decision)) {
			throw new McpPermissionException("decision with key " + decision.getKey(), "edit the decision");
		}
		// Validate every argument before anything is written; an absent argument keeps the value, an empty string clears it
		String title = arguments.containsKey("title") ? McpToolSupport.string(arguments, "title", null) : decision.getTitle();
		if(title == null) {
			// the title is mandatory in ProjDecisionContentEditController
			return McpToolResult.error("A decision needs a title, it cannot be cleared.");
		}
		McpToolSupport.checkLength("title", title, TITLE_MAX_LENGTH);
		String details = arguments.containsKey("details") ? McpToolSupport.string(arguments, "details", null) : decision.getDetails();
		McpToolSupport.checkLength("details", details, DETAILS_MAX_LENGTH);
		Date decisionDate = arguments.containsKey("decisionDate") ? DecisionToolSupport.decisionDate(arguments, "decisionDate") : decision.getDecisionDate();
		// ProjectService.updateDecision writes nothing for unchanged values, the date compared to the minute
		boolean contentChanged = CONTENT_ARGUMENTS.stream().anyMatch(arguments::containsKey);
		List<String> tags = support.tags(arguments, project);
		List<Identity> members = arguments.containsKey("members") ? support.resolveMembers(arguments, "members", project, "this decision") : null;
		Set<ProjArtefact> links = arguments.containsKey("links") ? support.resolveLinks(arguments, project, decision.getArtefact()) : null;

		// The sequence of ProjDecisionEditController.formOK: content with tags, references, members
		Identity doer = context.identity();
		if(contentChanged) {
			projectService.updateDecision(doer, decision, title, details, decisionDate);
		}
		if(arguments.containsKey("tags")) {
			projectService.updateTags(doer, decision.getArtefact(), tags);
		}
		if(links != null) {
			projectService.updateLinkedArtefacts(doer, decision.getArtefact(), links);
		}
		if(members != null) {
			projectService.updateMembers(doer, ProjectBCFactory.createFactory(project), decision.getArtefact(), new ArrayList<IdentityRef>(members));
		}
		return result(decision.getKey(), "Updated: ");
	}

	private McpToolResult delete(McpToolContext context, Map<String,Object> arguments) {
		ProjDecision decision = loadDecision(arguments);
		ProjProject project = decision.getArtefact().getProject();
		ProjProjectSecurityCallback secCallback = support.projectCallback(context, project);
		if(!secCallback.canViewDecisions()) {
			throw new McpPermissionException("decision with key " + decision.getKey(), "view the decision");
		}
		McpToolResult stateError = ProjectToolSupport.stateError(project, decision.getArtefact(), "decision " + decision.getKey());
		if(stateError != null) {
			return stateError;
		}
		if(!secCallback.canDeleteDecision(decision, context.identity())) {
			throw new McpPermissionException("decision with key " + decision.getKey(), "delete the decision");
		}
		projectService.deleteDecisionSoftly(context.identity(), decision);
		return result(decision.getKey(), "Deleted: ");
	}

	private ProjDecision loadDecision(Map<String,Object> arguments) {
		Long decisionKey = McpToolSupport.longValue(arguments, "decisionKey");
		if(decisionKey == null) {
			throw new McpInvalidArgumentException("This action needs the argument decisionKey. Call decision_query to find it.");
		}
		ProjDecision decision = projectService.getDecision(() -> decisionKey);
		// an unknown key gets the answer of a foreign decision
		if(decision == null) {
			throw new McpPermissionException("decision with key " + decisionKey, "view the decision");
		}
		return decision;
	}

	private McpToolResult result(Long decisionKey, String prefix) {
		ProjDecisionSearchParams searchParams = new ProjDecisionSearchParams();
		searchParams.setDecisions(List.of(() -> decisionKey));
		ProjDecisionInfo info = projectService.getDecisionInfos(searchParams, ProjArtefactInfoParams.of(true, false, true)).get(0);
		Map<String,Object> item = DecisionToolSupport.decisionItem(support, info, false);
		return McpToolResult.of(McpToolSupport.modifyResult(item, RESULT_FIELDS),
				prefix + DecisionToolSupport.decisionLine(item) + McpToolSupport.fullItemHint(DecisionQueryToolProvider.NAME));
	}

	static Map<String,Object> outputSchema() {
		return McpToolSupport.modifyOutputSchema(DecisionToolSupport.decisionItemSchema(), RESULT_FIELDS, List.of("key", "status", "url"));
	}

	private static Map<String,Object> inputSchema() {
		Map<String,Object> properties = new LinkedHashMap<>();
		properties.put("action", Map.of("type", "string", "enum", ACTIONS,
				"description", "create: a new decision in a project; update: change the passed fields; delete: soft delete"));
		properties.put("projectKey", Map.of("type", "integer", "description", "create only, required: the key of the project"));
		properties.put("decisionKey", Map.of("type", "integer", "description", "update and delete, required: the key of the decision"));
		properties.put("title", Map.of("type", "string", "maxLength", TITLE_MAX_LENGTH,
				"description", "create, required: what was decided; it cannot be cleared"));
		properties.put("details", Map.of("type", "string", "maxLength", DETAILS_MAX_LENGTH,
				"description", "plain text, line breaks are kept; an empty string clears it"));
		properties.put("decisionDate", Map.of("type", "string", "description",
				"when the decision was taken: ISO date-time with offset or local in the server zone (yyyy-MM-ddTHH:mm), "
						+ "or a date (yyyy-MM-dd) for the start of the day; create defaults to now, in a template project to none; an empty string clears it"));
		properties.put("tags", ProjectToolSupport.tagsInputSchema());
		properties.put("members", ProjectToolSupport.membersInputSchema("decision"));
		properties.put("links", ProjectToolSupport.linksInputSchema());
		return Map.of("type", "object", "properties", properties, "required", List.of("action"), "additionalProperties", Boolean.FALSE);
	}
}
