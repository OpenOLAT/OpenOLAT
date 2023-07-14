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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
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

	private final static String CMD_SELECT = "select";

	private final Long badgeClassKey;
	private final String mediaUrl;
	private TableModel tableModel;
	private FlexiTableElement tableEl;
	private CloseableModalController cmc;
	private BadgeAssertionPublicController badgeAssertionPublicController;

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

		flc.contextPut("img", mediaUrl + "/" + badgeClass.getImage());
		flc.contextPut("badgeClass", badgeClass);

		RepositoryEntry courseEntry = badgeClass.getEntry();
		if (courseEntry != null) {
			ICourse course = CourseFactory.loadCourse(courseEntry);
			uifactory.addStaticTextElement("form.course", course.getCourseTitle(), formLayout);
		}

		uifactory.addStaticTextElement("form.createdOn",
				Formatter.getInstance(getLocale()).formatDateAndTime(badgeClass.getCreationDate()), formLayout);

		if (badgeClass.isValidityEnabled()) {
			String validityPeriod = badgeClass.getValidityTimelapse() + " " +
					translate("form.time." + badgeClass.getValidityTimelapseUnit().name());
			uifactory.addStaticTextElement("form.valid", validityPeriod, formLayout);
		}

		Profile issuer = new Profile(new JSONObject(badgeClass.getIssuer()));

		uifactory.addStaticTextElement("class.issuer", issuer.getName(), formLayout);

		if (badgeClass.getLanguage() != null) {
			String languageDisplayName = Locale.forLanguageTag(badgeClass.getLanguage()).getDisplayName(getLocale());
			uifactory.addStaticTextElement("form.language", languageDisplayName, formLayout);
		}

		uifactory.addStaticTextElement("form.version", badgeClass.getVersion(), formLayout);

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

		if (!badgeCriteria.isAwardAutomatically()) {
			uifactory.addStaticTextElement("badge.issued.manually", null,
					translate("badge.issued.manually"), formLayout);
		}

		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.recipient, CMD_SELECT));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.issuedOn));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.status));
		tableModel = new TableModel(columnModel, userManager);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, getTranslator(), formLayout);
		loadData();
	}

	private void loadData() {
		BadgeClass badgeClass = openBadgesManager.getBadgeClass(badgeClassKey);
		List<Row> rows = openBadgesManager
				.getBadgeAssertions(badgeClass)
				.stream()
				.map(ba -> new Row(ba)).toList();
		tableModel.setObjects(rows);
		tableEl.reset();

		flc.contextPut("hasRecipients", !rows.isEmpty());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc) {
			cleanUp();
		} else if (source == badgeAssertionPublicController) {
			cmc.deactivate();
			cleanUp();
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(badgeAssertionPublicController);
		cmc = null;
		badgeAssertionPublicController = null;
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
		}
		super.formInnerEvent(ureq, source, event);
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
		status("form.status");

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

	record Row(BadgeAssertion badgeAssertion) {
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
			};
		}
	}
}
