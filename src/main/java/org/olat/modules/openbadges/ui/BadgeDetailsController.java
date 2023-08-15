/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.openbadges.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.criteria.BadgeCondition;
import org.olat.modules.openbadges.criteria.BadgeCriteria;
import org.olat.modules.openbadges.criteria.BadgeCriteriaXStream;
import org.olat.modules.openbadges.v2.Profile;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;

import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-07-13<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeDetailsController extends FormBasicController {

	private static final String CMD_TOOLS = "tools";
	private final static String CMD_SELECT = "select";

	private final Long badgeClassKey;
	private final String mediaUrl;
	private FormLink editDetailsButton;
	private FormLink courseEl;
	private StaticTextElement validityPeriodEl;
	private StaticTextElement issuerEl;
	private StaticTextElement languageEl;
	private StaticTextElement versionEl;
	private StaticTextElement issuedManuallyEl;
	private FormLink awardBadgeButton;
	private TableModel tableModel;
	private FlexiTableElement tableEl;
	private CloseableModalController cmc;
	private BadgeAssertionPublicController badgeAssertionPublicController;
	private CreateBadgeClassWizardContext createBadgeClassContext;
	private StepsMainRunController addStepsController;
	private IssueGlobalBadgeController issueGlobalBadgeCtrl;
	private IssueCourseBadgeController issueCourseBadgeCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private ToolsController toolsCtrl;
	private DialogBoxController confirmRevokeCtrl;

	@Autowired
	private UserManager userManager;
	@Autowired
	private OpenBadgesManager openBadgesManager;

	public BadgeDetailsController(UserRequest ureq, WindowControl wControl, Long badgeClassKey) {
		super(ureq, wControl, "badge_details");
		this.badgeClassKey = badgeClassKey;

		mediaUrl = registerMapper(ureq, new BadgeClassMediaFileMapper());

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		BadgeClass badgeClass = openBadgesManager.getBadgeClass(badgeClassKey);

		editDetailsButton = uifactory.addFormLink("class.edit.details", formLayout, Link.BUTTON);
		editDetailsButton.setElementCssClass("o_right");

		courseEl = uifactory.addFormLink("form.course", "goToCourse", "", translate("form.course"), formLayout, Link.NONTRANSLATED);
		uifactory.addStaticTextElement("form.createdOn",
				Formatter.getInstance(getLocale()).formatDateAndTime(badgeClass.getCreationDate()), formLayout);
		validityPeriodEl = uifactory.addStaticTextElement("form.valid", "", formLayout);
		issuerEl = uifactory.addStaticTextElement("class.issuer", "", formLayout);
		languageEl = uifactory.addStaticTextElement("form.language", "", formLayout);
		versionEl = uifactory.addStaticTextElement("form.version", "", formLayout);
		issuedManuallyEl = uifactory.addStaticTextElement("badge.issued.manually", null,
				translate("badge.issued.manually"), formLayout);

		awardBadgeButton = uifactory.addFormLink("award.badge", formLayout, Link.BUTTON);
		awardBadgeButton.setElementCssClass("o_right");

		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.recipient, CMD_SELECT));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.issuedOn));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.status));

		StickyActionColumnModel toolsColumn = new StickyActionColumnModel(
				Cols.tools.i18nHeaderKey(),
				Cols.tools.ordinal()
		);
		columnModel.addFlexiColumnModel(toolsColumn);

		tableModel = new TableModel(columnModel, userManager);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, getTranslator(), formLayout);

		loadData();
	}

	private void loadData() {
		BadgeClass badgeClass = openBadgesManager.getBadgeClass(badgeClassKey);

		flc.contextPut("img", mediaUrl + "/" + badgeClass.getImage());
		flc.contextPut("badgeClass", badgeClass);
		flc.contextPut("isCourseBadge", badgeClass.getEntry() != null);

		RepositoryEntry courseEntry = badgeClass.getEntry();
		if (courseEntry != null) {
			ICourse course = CourseFactory.loadCourse(courseEntry);
			courseEl.setI18nKey(course.getCourseTitle());
			courseEl.setVisible(true);
		} else {
			courseEl.setVisible(false);
		}

		if (badgeClass.isValidityEnabled()) {
			String validityPeriod = badgeClass.getValidityTimelapse() + " " +
					translate("form.time." + badgeClass.getValidityTimelapseUnit().name());
			validityPeriodEl.setValue(validityPeriod);
			validityPeriodEl.setVisible(true);
		} else {
			validityPeriodEl.setVisible(false);
		}

		Profile issuer = new Profile(new JSONObject(badgeClass.getIssuer()));
		issuerEl.setValue(issuer.getName());

		if (badgeClass.getLanguage() != null) {
			String languageDisplayName = Locale.forLanguageTag(badgeClass.getLanguage()).getDisplayName(getLocale());
			languageEl.setValue(languageDisplayName);
			languageEl.setVisible(true);
		} else {
			languageEl.setVisible(false);
		}

		versionEl.setValue(badgeClass.getVersion());

		BadgeCriteria badgeCriteria = BadgeCriteriaXStream.fromXml(badgeClass.getCriteria());
		flc.contextPut("criteriaDescription", badgeCriteria.getDescription());
		flc.contextPut("showConditions", badgeCriteria.isAwardAutomatically());

		List<BadgeCondition> badgeConditions = badgeCriteria.getConditions();
		List<Condition> conditions = new ArrayList<>();
		for (int i = 0; i < badgeConditions.size(); i++) {
			BadgeCondition badgeCondition = badgeConditions.get(i);
			Condition condition = new Condition(badgeCondition, i == 0, getTranslator());
			conditions.add(condition);
		}
		flc.contextPut("conditions", conditions);

		issuedManuallyEl.setVisible(!badgeCriteria.isAwardAutomatically());

		if (badgeClass.getStatus() != BadgeClass.BadgeClassStatus.preparation) {
			editDetailsButton.setEnabled(false);
		} else {
			editDetailsButton.setEnabled(true);
		}

		List<Row> rows = openBadgesManager
				.getBadgeAssertions(badgeClass)
				.stream()
				.map(this::mapBadgeAssertionToRow)
				.toList();
		tableModel.setObjects(rows);
		tableEl.reset();

		flc.contextPut("hasRecipients", !rows.isEmpty());
	}

	private Row mapBadgeAssertionToRow(BadgeAssertion badgeAssertion) {
		String toolId = "tool_" + badgeAssertion.getUuid();
		FormLink toolLink = (FormLink) flc.getComponent(toolId);
		if (toolLink == null) {
			toolLink = uifactory.addFormLink(toolId, CMD_TOOLS, "", tableEl, Link.LINK | Link.NONTRANSLATED);
			toolLink.setTranslator(getTranslator());
			toolLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
			toolLink.setTitle(translate("table.header.actions"));
		}
		toolLink.setUserObject(badgeAssertion);
		return new Row(badgeAssertion, toolLink);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc) {
			cleanUp();
		} else if (source == badgeAssertionPublicController) {
			cmc.deactivate();
			cleanUp();
		} else if (source == addStepsController) {
			if (event == Event.CANCELLED_EVENT || event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(addStepsController);
			}
		} else if (source == issueGlobalBadgeCtrl) {
			cmc.deactivate();
			cleanUp();
			if (event == Event.DONE_EVENT) {
				loadData();
			}
		} else if (source == issueCourseBadgeCtrl) {
			cmc.deactivate();
			cleanUp();
			if (event == Event.DONE_EVENT) {
				loadData();
			}
		} else if (source == toolsCtrl) {
			if (calloutCtrl != null) {
				calloutCtrl.deactivate();
			}
			cleanUp();
		} else if (source == confirmRevokeCtrl) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				BadgeAssertion badgeAssertion = (BadgeAssertion) confirmRevokeCtrl.getUserObject();
				doRevoke(badgeAssertion);
			}
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(badgeAssertionPublicController);
		removeAsListenerAndDispose(issueGlobalBadgeCtrl);
		removeAsListenerAndDispose(issueCourseBadgeCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		cmc = null;
		badgeAssertionPublicController = null;
		issueGlobalBadgeCtrl = null;
		issueCourseBadgeCtrl = null;
		calloutCtrl = null;
		toolsCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent selectionEvent) {
				String command = selectionEvent.getCommand();
				Row row = tableModel.getObject(selectionEvent.getIndex());
				if (CMD_SELECT.equals(command)) {
					doSelect(ureq, row);
				}
			}
		} else if (source == editDetailsButton) {
			doEdit(ureq);
		} else if (source == awardBadgeButton) {
			doAwardBadge(ureq);
		} else if (source == courseEl) {
			fireEvent(ureq, FormEvent.BACK_EVENT);
		} else if (source instanceof FormLink link) {
			if (CMD_TOOLS.equals(link.getCmd()) && link.getUserObject() instanceof BadgeAssertion badgeAssertion) {
				doOpenTools(ureq, link, badgeAssertion);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doOpenTools(UserRequest ureq, FormLink link, BadgeAssertion badgeAssertion) {
		toolsCtrl = new ToolsController(ureq, getWindowControl(), badgeAssertion);
		listenTo(toolsCtrl);

		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}

	private void doEdit(UserRequest ureq) {
		BadgeClass badgeClass = openBadgesManager.getBadgeClass(badgeClassKey);
		createBadgeClassContext = new CreateBadgeClassWizardContext(badgeClass);
		Step start = new CreateBadge02DetailsStep(ureq, createBadgeClassContext);

		StepRunnerCallback finish = (innerUreq, innerWControl, innerRunContext) -> {
			BadgeClass updatedBadgeClass = openBadgesManager.updateBadgeClass(createBadgeClassContext.getBadgeClass());
			if (createBadgeClassContext.isCourseBadge()) {
				openBadgesManager.issueBadge(updatedBadgeClass, createBadgeClassContext.getEarners(), getIdentity());
			}
			loadData();
			return StepsMainRunController.DONE_MODIFIED;
		};

		addStepsController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("form.edit.badge"), "o_sel_add_badge_wizard");
		listenTo(addStepsController);
		getWindowControl().pushAsModalDialog(addStepsController.getInitialComponent());
	}

	private void doAwardBadge(UserRequest ureq) {
		BadgeClass badgeClass = openBadgesManager.getBadgeClass(badgeClassKey);

		if (badgeClass.getEntry() == null) {
			issueGlobalBadgeCtrl = new IssueGlobalBadgeController(ureq, getWindowControl(), badgeClass);
			listenTo(issueGlobalBadgeCtrl);

			String title = translate("issueGlobalBadge");
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					issueGlobalBadgeCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		} else {
			issueCourseBadgeCtrl = new IssueCourseBadgeController(ureq, getWindowControl(), badgeClass);
			listenTo(issueCourseBadgeCtrl);

			String title = translate("issueBadge");
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					issueCourseBadgeCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}


	private void doSelect(UserRequest ureq, Row row) {
		BadgeAssertion badgeAssertion = row.badgeAssertion();
		badgeAssertionPublicController = new BadgeAssertionPublicController(ureq, getWindowControl(), badgeAssertion.getUuid());
		listenTo(badgeAssertionPublicController);

		String title = translate("issuedBadge");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				badgeAssertionPublicController.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doConfirmRevoke(UserRequest ureq, BadgeAssertion badgeAssertion) {
		String recipientDisplayName = userManager.getUserDisplayName(badgeAssertion.getRecipient());
		String title = translate("confirm.revoke.issued.badge.title", recipientDisplayName);
		String text = translate("confirm.revoke.issued.badge", recipientDisplayName);
		confirmRevokeCtrl = activateOkCancelDialog(ureq, title, text, confirmRevokeCtrl);
		confirmRevokeCtrl.setUserObject(badgeAssertion);
	}

	private void doViewBadgeInfo(UserRequest ureq, BadgeAssertion badgeAssertion) {
		badgeAssertionPublicController = new BadgeAssertionPublicController(ureq, getWindowControl(), badgeAssertion.getUuid());
		listenTo(badgeAssertionPublicController);

		String title = translate("form.view.badge.info");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				badgeAssertionPublicController.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doRevoke(BadgeAssertion badgeAssertion) {
		openBadgesManager.revokeBadgeAssertion(badgeAssertion.getKey());
		loadData();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private class BadgeClassMediaFileMapper implements Mapper {

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			VFSLeaf classFileLeaf = openBadgesManager.getBadgeClassVfsLeaf(relPath);
			if (classFileLeaf != null) {
				return new VFSMediaResource(classFileLeaf);
			}
			return new NotFoundMediaResource();
		}
	}

	public record Condition(BadgeCondition badgeCondition, boolean first, Translator translator) {
		@Override
		public String toString() {
			return badgeCondition.toString(translator);
		}
	}

	enum Cols implements FlexiSortableColumnDef {
		recipient("form.recipient"),
		issuedOn("form.issued.on"),
		status("form.status"),
		tools("table.header.actions");

		Cols(String i18n) {
			this.i18nKey = i18n;
		}

		private final String i18nKey;

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}

	record Row(BadgeAssertion badgeAssertion, FormLink toolLink) {
	}

	private class TableModel extends DefaultFlexiTableDataModel<Row> {
		private final UserManager userManager;

		public TableModel(FlexiTableColumnModel columnModel, UserManager userManager) {
			super(columnModel);
			this.userManager = userManager;
		}

		@Override
		public Object getValueAt(int row, int col) {
			BadgeAssertion badgeAssertion = getObject(row).badgeAssertion();
			return switch (Cols.values()[col]) {
				case recipient -> userManager.getUserDisplayName(badgeAssertion.getRecipient());
				case status -> translate("assertion.status." + badgeAssertion.getStatus().name());
				case issuedOn -> Formatter.getInstance(getLocale()).formatDateAndTime(badgeAssertion.getIssuedOn());
				case tools -> getObject(row).toolLink();
			};
		}
	}

	private class ToolsController extends BasicController {

		private final Link viewInfoLink;
		private final Link revokeLink;
		private final BadgeAssertion badgeAssertion;

		protected ToolsController(UserRequest ureq, WindowControl wControl, BadgeAssertion badgeAssertion) {
			super(ureq, wControl);
			this.badgeAssertion = badgeAssertion;

			VelocityContainer mainVC = createVelocityContainer("tools");

			viewInfoLink = LinkFactory.createLink("form.view.badge.info", "viewBadgeInfo", getTranslator(), mainVC, this, Link.LINK);
			mainVC.put("form.view.badge.info", viewInfoLink);

			revokeLink = LinkFactory.createLink("table.revoke", "revoke", getTranslator(), mainVC, this, Link.LINK);
			revokeLink.setVisible(badgeAssertion.getStatus() == BadgeAssertion.BadgeAssertionStatus.issued);
			mainVC.put("tool.revoke", revokeLink);

			putInitialPanel(mainVC);
		}


		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			if (source == revokeLink) {
				doConfirmRevoke(ureq, badgeAssertion);
			} else if (source == viewInfoLink) {
				doViewBadgeInfo(ureq, badgeAssertion);
			}
		}
	}
}
