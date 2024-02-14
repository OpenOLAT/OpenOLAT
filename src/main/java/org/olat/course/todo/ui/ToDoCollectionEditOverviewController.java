/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.todo.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.todo.manager.CourseCollectionToDoTaskProvider;
import org.olat.course.todo.manager.CourseCollectionToDoTaskProvider.UpdateToDoTaskStrategie;
import org.olat.course.todo.model.ToDoTaskCollectionEditContext;
import org.olat.course.todo.model.ToDoTaskCollectionEditContext.Field;
import org.olat.modules.todo.ToDoExpenditureOfWork;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoRole;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskMembers;
import org.olat.modules.todo.ui.ToDoUIFactory;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 Jan 2024<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoCollectionEditOverviewController extends StepFormBasicController {
	
	private static final String CMD_CHANGES = "changes";
	
	private FormLayoutContainer overviewCont;
	
	private CloseableCalloutWindowController calloutCtrl;
	
	private final ToDoTaskCollectionEditContext context;
	private int counter = 0;
	
	@Autowired
	private ToDoService toDoService;
	@Autowired
	private CourseCollectionToDoTaskProvider collectionProvider;
	@Autowired
	private UserManager userManager;

	public ToDoCollectionEditOverviewController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		setTranslator(Util.createPackageTranslator(ToDoUIFactory.class, getLocale(), getTranslator()));
		
		context = (ToDoTaskCollectionEditContext)getFromRunContext(ToDoTaskCollectionEditContext.KEY);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		DryRunChangeStrategie strategie = new DryRunChangeStrategie();
		collectionProvider.updateToDoTaskCollection(context, strategie);
		
		setFormTitle("course.todo.collection.overview.step");
		setFormInfo("course.todo.collection.overview.info", new String[] {String.valueOf(strategie.getNumElements())});
		
		overviewCont = FormLayoutContainer.createCustomFormLayout("overview", getTranslator(), velocity_root + "/bulk_overview.html");
		overviewCont.setRootForm(mainForm);
		formLayout.add(overviewCont);
		
		List<OverviewField> fields = new ArrayList<>(2);
		if (context.isSelected(Field.title)) {
			String text = translate("course.todo.collection.overview.change.title", StringHelper.escapeHtml(context.getTitle()));
			String resourceItemName = createResourceLink(context.isOverride(Field.title), strategie.getChanges(Field.title), strategie.getNumElements());
			fields.add(new OverviewField(text, resourceItemName));
		}
		if (context.isSelected(Field.status)) {
			String text = translate("course.todo.collection.overview.change.status", ToDoUIFactory.getDisplayName(getTranslator(), context.getStatus()));
			String resourceItemName = createResourceLink(context.isOverride(Field.status), strategie.getChanges(Field.status), strategie.getNumElements());
			fields.add(new OverviewField(text, resourceItemName));
		}
		if (context.isSelected(Field.priority)) {
			String text = translate("course.todo.collection.overview.change.priority", ToDoUIFactory.getDisplayName(getTranslator(), context.getPriority()));
			String resourceItemName = createResourceLink(context.isOverride(Field.priority), strategie.getChanges(Field.priority), strategie.getNumElements());
			fields.add(new OverviewField(text, resourceItemName));
		}
		if (context.isSelected(Field.startDate)) {
			String text = translate("course.todo.collection.overview.change.start.date", ToDoUIFactory.getDateOrAnytime(getTranslator(), context.getStartDate()));
			String resourceItemName = createResourceLink(context.isOverride(Field.startDate), strategie.getChanges(Field.startDate), strategie.getNumElements());
			fields.add(new OverviewField(text, resourceItemName));
		}
		if (context.isSelected(Field.dueDate)) {
			String text = translate("course.todo.collection.overview.change.due.date", ToDoUIFactory.getDateOrAnytime(getTranslator(), context.getDueDate()));
			String resourceItemName = createResourceLink(context.isOverride(Field.dueDate), strategie.getChanges(Field.dueDate), strategie.getNumElements());
			fields.add(new OverviewField(text, resourceItemName));
		}
		if (context.isSelected(Field.expenditureOfWork)) {
			ToDoExpenditureOfWork expenditureOfWork = toDoService.getExpenditureOfWork(context.getExpenditureOfWork());
			String formattedExpenditureOfWork = ToDoUIFactory.format(expenditureOfWork);
			String text = translate("course.todo.collection.overview.change.expenditure.of.work", formattedExpenditureOfWork);
			String resourceItemName = createResourceLink(context.isOverride(Field.expenditureOfWork), strategie.getChanges(Field.expenditureOfWork), strategie.getNumElements());
			fields.add(new OverviewField(text, resourceItemName));
		}
		if (context.isSelected(Field.description)) {
			String text = translate("course.todo.collection.overview.change.description", StringHelper.escapeHtml(context.getDescription()));
			String resourceItemName = createResourceLink(context.isOverride(Field.description), strategie.getChanges(Field.description), strategie.getNumElements());
			fields.add(new OverviewField(text, resourceItemName));
		}
		if (context.isSelected(Field.tagDisplayNames)) {
			List<String> tagDisplayNameAdded = new ArrayList<>(context.getTagDisplayNames());
			tagDisplayNameAdded.removeAll(context.getCollectionTagDisplayNames());
			List<String> tagDisplayNameRemoved = new ArrayList<>(context.getCollectionTagDisplayNames());
			tagDisplayNameRemoved.removeAll(context.getTagDisplayNames());
			
			for (String tagAdded : tagDisplayNameAdded) {
				String text = translate("course.todo.collection.overview.change.tag.add", tagAdded);
				String resourceItemName = createResourceLink(context.isOverride(Field.tagDisplayNames),
						strategie.getTagAddedToMembers().getOrDefault(tagAdded, List.of()),
						strategie.getNumElements());
				fields.add(new OverviewField(text, resourceItemName));
			}
			for (String tagRemoved : tagDisplayNameRemoved) {
				String text = translate("course.todo.collection.overview.change.tag.remove", tagRemoved);
				String resourceItemName = createResourceLink(context.isOverride(Field.tagDisplayNames),
						strategie.getTagRemovedToMembers().getOrDefault(tagRemoved, List.of()),
						strategie.getNumElements());
				fields.add(new OverviewField(text, resourceItemName));
			}
		}
		
		List<OverviewStep> overviewSteps = List.of(new OverviewStep(translate("course.todo.collection.overview.change.todo"), fields));
		overviewCont.contextPut("steps", overviewSteps);
	}

	private String createResourceLink(boolean overriden, List<ToDoTaskMembers> changes, int all) {
		String linkText = null;
		if (overriden) {
			linkText = translate("course.todo.collection.overview.override.all");
		} else if (changes == null || changes.isEmpty()) {
			linkText = translate("course.todo.collection.overview.override.none");
		} else {
			linkText = translate("course.todo.collection.overview.override.multi", String.valueOf(changes.size()), String.valueOf(all));
		}
		
		String linkName = "o_change_" + counter++;
		FormLink link = uifactory.addFormLink(linkName, CMD_CHANGES, "", null, overviewCont, Link.LINK | Link.NONTRANSLATED);
		link.setI18nKey(linkText);
		link.setEnabled(changes != null && !changes.isEmpty());
		link.setUserObject(changes);
		return linkName;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if (CMD_CHANGES.equals(cmd)) {
				@SuppressWarnings("unchecked")
				List<ToDoTaskMembers> changes = (List<ToDoTaskMembers>)link.getUserObject();
				doOpenChanges(ureq, changes, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}
	
	private void doOpenChanges(UserRequest ureq, List<ToDoTaskMembers> changes, FormLink link) {
		removeAsListenerAndDispose(calloutCtrl);
		
		List<String> names = changes.stream()
				.flatMap(members -> members.getMembers(ToDoRole.assignee).stream())
				.map(assigne -> userManager.getUserDisplayName(assigne.getKey()))
				.sorted()
				.toList();
		
		VelocityContainer changesVC = createVelocityContainer("bulk_changes");
		changesVC.contextPut("names", names);
		
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), changesVC, link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private static final class DryRunChangeStrategie implements UpdateToDoTaskStrategie {
		
		private final Map<ToDoTaskCollectionEditContext.Field, List<ToDoTaskMembers>> fieldToChangedToDoMembers = new HashMap<>();
		private final Map<String, List<ToDoTaskMembers>> tagAddedToMembers = new HashMap<>();
		private final Map<String, List<ToDoTaskMembers>> tagRemovedToMembers = new HashMap<>();
		private ToDoTaskMembers members;
		private int numElements = 0;

		@Override
		public void init(ToDoTask toDoTask, ToDoTaskMembers members) {
			this.members = members;
			numElements++;
		}

		@Override
		public void updateTitle(String title) {
			addChange(Field.title);
		}

		@Override
		public void updateDescription(String description) {
			addChange(Field.description);
		}

		@Override
		public void updateStatus(ToDoStatus status) {
			addChange(Field.status);
		}

		@Override
		public void updatePriority(ToDoPriority priority) {
			addChange(Field.priority);
		}

		@Override
		public void updateExpenditureOfWork(Long expenditureOfWork) {
			addChange(Field.expenditureOfWork);
		}

		@Override
		public void updateStartDate(Date startDate) {
			addChange(Field.startDate);
		}

		@Override
		public void updateDueDate(Date dueDate) {
			addChange(Field.dueDate);
		}
		
		@Override
		public void updateTagDisplayNames(List<String> tagDisplayNames, List<String> tagDisplayNameAdded,
				List<String> tagDisplayNameRemoved) {
			tagDisplayNameAdded.forEach(tagDisplayName -> {
				tagAddedToMembers.computeIfAbsent(tagDisplayName, key -> new ArrayList<>()).add(members);
			});
			tagDisplayNameRemoved.forEach(tagDisplayName -> {
				tagRemovedToMembers.computeIfAbsent(tagDisplayName, key -> new ArrayList<>()).add(members);
			});
		}

		@Override
		public void update() {
			//
		}

		public int getNumElements() {
			return numElements;
		}

		public List<ToDoTaskMembers> getChanges(Field field) {
			return fieldToChangedToDoMembers.get(field);
		}

		private void addChange(Field field) {
			fieldToChangedToDoMembers.computeIfAbsent(field, key -> new ArrayList<>()).add(members);
		}

		public Map<String, List<ToDoTaskMembers>> getTagAddedToMembers() {
			return tagAddedToMembers;
		}

		public Map<String, List<ToDoTaskMembers>> getTagRemovedToMembers() {
			return tagRemovedToMembers;
		}
		
	}
	
	public static final class OverviewStep {
		
		private final String name;
		private final List<OverviewField> fields;
		
		public OverviewStep(String name, List<OverviewField> fields) {
			this.name = name;
			this.fields = fields;
		
		}
		public String getName() {
			return name;
		}
		
		public List<OverviewField> getFields() {
			return fields;
		}
		
	}
	
	public static final class OverviewField {
		
		private final String text;
		private final String resourceItemName;
		
		public OverviewField(String text, String resourceItemName) {
			this.text = text;
			this.resourceItemName = resourceItemName;
		}
		
		public String getText() {
			return text;
		}
		
		public String getResourceItemName() {
			return resourceItemName;
		}
		
	}

}
