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

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.repository.RepositoryEntry;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-06-28<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class IssuedBadgesController extends FormBasicController implements FlexiTableComponentDelegate, Activateable2 {
	private final static String CMD_SELECT = "select";
	private final String mediaUrl;
	private final Identity identity;
	private final RepositoryEntry courseEntry;
	private final String titleKey;
	private BadgeToolTableModel tableModel;
	private FlexiTableElement tableEl;
	private CloseableModalController cmc;
	private BadgeAssertionPublicController badgeAssertionPublicController;

	@Autowired
	private OpenBadgesManager openBadgesManager;

	public IssuedBadgesController(UserRequest ureq, WindowControl wControl, String titleKey, RepositoryEntry courseEntry, Identity identity) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.titleKey = titleKey;
		this.courseEntry = courseEntry;
		this.identity = identity;
		mediaUrl = registerMapper(ureq, new BadgeImageMapper());

		initForm(ureq);
		loadModel(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (titleKey != null) {
			setFormTitle(titleKey);
		}
		setFormTitleIconCss("o_icon o_icon-fw o_icon_badge");

		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgeToolTableModel.AssertionCols.name, CMD_SELECT));

		tableModel = new BadgeToolTableModel(columnModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20,
				false, getTranslator(), formLayout);
		tableEl.setCssDelegate(CssDelegate.DELEGATE);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		VelocityContainer rowVC = createVelocityContainer("assertion_row");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
	}

	private void loadModel(UserRequest ureq) {
		List<BadgeToolRow> badgeToolRows = openBadgesManager.getBadgeAssertionsWithSizes(identity, courseEntry).stream()
				.map(ba -> {
					BadgeToolRow row = new BadgeToolRow(ba);
					forgeRow(row, ba);
					return row;
				}).toList();
		tableModel.setObjects(badgeToolRows);
		tableEl.reset(true, true, true);
	}

	private void forgeRow(BadgeToolRow row, OpenBadgesManager.BadgeAssertionWithSize badgeAssertionWithSize) {
		BadgeAssertion badgeAssertion = badgeAssertionWithSize.badgeAssertion();
		String imageUrl = mediaUrl + "/" + badgeAssertion.getBakedImage();
		BadgeImageComponent badgeImage = new BadgeImageComponent("badgeImage", imageUrl, BadgeImageComponent.Size.cardSize);
		row.setBadgeImage(badgeImage);
		row.setIssuedOn(Formatter.getInstance(getLocale()).formatDateAndTime(badgeAssertion.getIssuedOn()));
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(badgeAssertionPublicController);
		removeAsListenerAndDispose(cmc);
		badgeAssertionPublicController = null;
		cmc = null;
	}


	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (event instanceof SelectionEvent selectionEvent) {
			BadgeToolRow row = tableModel.getObject(selectionEvent.getIndex());
			String uuid = row.getBadgeAssertion().getUuid();
			doOpenDetails(ureq, uuid);
		} else if (source == flc) {
			String selectString = ureq.getParameter("select");
			if (selectString != null) {
				Long assertionKey = Long.parseLong(selectString);
				BadgeToolRow row = tableModel.getObjects().stream().filter(ba -> ba.getBadgeAssertion().getKey() == assertionKey).findFirst().orElse(null);
				if (row != null) {
					doOpenDetails(ureq, row.getBadgeAssertion().getUuid());
				}
			}
		}
	}

	private void doOpenDetails(UserRequest ureq, String uuid) {
		badgeAssertionPublicController = new BadgeAssertionPublicController(ureq, getWindowControl(), uuid);
		listenTo(badgeAssertionPublicController);

		String title = translate("form.badge");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				badgeAssertionPublicController.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	@Override
	protected void formOK(UserRequest ureq) {
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>();
		if (rowObject instanceof BadgeToolRow badgeToolRow) {
			components.add(badgeToolRow.getBadgeImage());
		}

		return components;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	private class BadgeImageMapper implements Mapper {

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			VFSLeaf templateLeaf = openBadgesManager.getBadgeAssertionVfsLeaf(relPath);
			if (templateLeaf != null) {
				return new VFSMediaResource(templateLeaf);
			}
			return new NotFoundMediaResource();
		}
	}

	private static final class CssDelegate extends DefaultFlexiTableCssDelegate {
		private static final CssDelegate DELEGATE = new CssDelegate();

		@Override
		public String getTableCssClass(FlexiTableRendererType type) {
			return "o_badge_tool_rows o_block_top";
		}

		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return "o_badge_tool_row";
		}
	}
}
