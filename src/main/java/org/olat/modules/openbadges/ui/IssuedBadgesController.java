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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.services.image.Size;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.fileresource.DownloadeableMediaResource;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-06-28<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class IssuedBadgesController extends FormBasicController implements FlexiTableComponentDelegate, Activateable2 {

	private static final String CMD_TOOLS = "tools";
	private final static String CMD_SELECT = "select";
	private final String mediaUrl;
	private final String downloadUrl;
	private final Identity identity;
	private final RepositoryEntry courseEntry;
	private final boolean nullEntryMeansAll;
	private final String titleKey;
	private final boolean mine;
	private final String helpLink;
	private IssuedBadgesTableModel tableModel;
	private FlexiTableElement tableEl;
	private CloseableModalController cmc;
	private CloseableCalloutWindowController calloutCtrl;
	private BadgeAssertionPublicController badgeAssertionPublicController;
	private ToolsController toolsCtrl;

	@Autowired
	private OpenBadgesManager openBadgesManager;
	@Autowired
	private UserManager userManager;

	public IssuedBadgesController(UserRequest ureq, WindowControl wControl, String titleKey, RepositoryEntry courseEntry,
								  boolean nullEntryMeansAll, Identity identity, boolean mine, String helpLink) {
		super(ureq, wControl, "issued_badges");
		this.titleKey = titleKey;
		this.courseEntry = courseEntry;
		this.nullEntryMeansAll = nullEntryMeansAll;
		this.identity = identity;
		this.mine = mine;
		this.helpLink = helpLink;
		mediaUrl = registerMapper(ureq, new BadgeImageMapper());
		downloadUrl = registerMapper(ureq, new BadgeAssertionDownloadableMediaFileMapper());

		if (titleKey != null) {
			flc.contextPut("titleKey", titleKey);
		}
		if (helpLink != null) {
			flc.contextPut("helpLink", helpLink);
		}

		initForm(ureq);
		loadModel(ureq, null);
		initFilters();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IssuedBadgesTableModel.IssuedBadgeCols.image,
				(renderer, sb, val, row, source, ubu, translator) -> {
					Size targetSize = tableModel.getObject(row).fitIn(60, 60);
					int width = targetSize.getWidth();
					int height = targetSize.getHeight();
					sb.append("<div style='width: ").append(width).append("px; height: ").append(height).append("px;'>");
					sb.append("<div class='o_image'>");
					if (val instanceof String image) {
						sb.append("<img src=\"");
						sb.append(mediaUrl).append("/").append(image).append("\" ");
						sb.append(" width='").append(width).append("px' height='").append(height).append("px' >");
					}
					sb.append("</div>");
					sb.append("</div>");
				}));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IssuedBadgesTableModel.IssuedBadgeCols.title, CMD_SELECT));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IssuedBadgesTableModel.IssuedBadgeCols.status, new BadgeAssertionStatusRenderer()));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IssuedBadgesTableModel.IssuedBadgeCols.issuer));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IssuedBadgesTableModel.IssuedBadgeCols.issuedOn));
		if (identity == null) {
			columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IssuedBadgesTableModel.IssuedBadgeCols.recipient));
		}

		StickyActionColumnModel toolsColumn = new StickyActionColumnModel(
				IssuedBadgesTableModel.IssuedBadgeCols.tools.i18nHeaderKey(),
				IssuedBadgesTableModel.IssuedBadgeCols.tools.ordinal()
		);
		columnModel.addFlexiColumnModel(toolsColumn);

		tableModel = new IssuedBadgesTableModel(columnModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 10,
				false, getTranslator(), formLayout);
		tableEl.setCssDelegate(CssDelegate.DELEGATE);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		VelocityContainer rowVC = createVelocityContainer("assertion_row");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
		tableEl.setSortEnabled(true);
		tableEl.setSearchEnabled(false);
		tableEl.setEmptyTableSettings("empty.badges.table", null,
				"o_icon_badge", null, null,
				false);
	}

	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();

		SelectionValues statusKV = new SelectionValues();
		List.of(BadgeAssertion.BadgeAssertionStatus.values())
				.stream()
				.filter(status -> status != BadgeAssertion.BadgeAssertionStatus.editing)
				.forEach(status -> statusKV.add(SelectionValues.entry(status.name(), translate("assertion.status." + status))));
		filters.add(new FlexiTableMultiSelectionFilter(translate(IssuedBadgesTableModel.IssuedBadgesFilter.STATUS.getI18nKey()),
				IssuedBadgesTableModel.IssuedBadgesFilter.STATUS.name(), statusKV, true));

		SelectionValues issuersKV = new SelectionValues();
		Set<String> issuers = tableModel.getObjects().stream().map(IssuedBadgeRow::getIssuer).collect(Collectors.toSet());
		issuers.forEach(issuer -> issuersKV.add(SelectionValues.entry(Long.toString(Math.abs(issuer.hashCode())), issuer)));
		filters.add(new FlexiTableMultiSelectionFilter(translate(IssuedBadgesTableModel.IssuedBadgesFilter.ISSUER.getI18nKey()),
				IssuedBadgesTableModel.IssuedBadgesFilter.ISSUER.name(), issuersKV , true));

		tableEl.setFilters(true, filters, true, false);
		tableEl.expandFilters(true);
	}

	private void loadModel(UserRequest ureq, List<FlexiTableFilter> filters) {
		removeTemporaryFiles();
		List<IssuedBadgeRow> issuedBadgeRows = openBadgesManager.getBadgeAssertionsWithSizes(identity, courseEntry,
						nullEntryMeansAll).stream()
				.map(ba -> {
					IssuedBadgeRow row = new IssuedBadgeRow(ba);
					forgeRow(row, ba);
					return row;
				}).toList();
		if (filters == null) {
			tableModel.setObjects(issuedBadgeRows);
		} else {
			List<IssuedBadgeRow> filteredRows = issuedBadgeRows.stream().filter(r -> {
				String issuerHashString = Long.toString(Math.abs(r.getIssuer().hashCode()));
				for (FlexiTableFilter filter : filters) {
					boolean matchFound = false;
					if (filter instanceof FlexiTableMultiSelectionFilter multiSelectionFilter) {
						if (multiSelectionFilter.getValues() == null || multiSelectionFilter.getValues().isEmpty()) {
							continue;
						}
						if (IssuedBadgesTableModel.IssuedBadgesFilter.STATUS.name().equals(filter.getFilter())) {
							for (String statusString : multiSelectionFilter.getValues()) {
								if (r.getBadgeAssertion().getStatus().name().equals(statusString)) {
									matchFound = true;
									break;
								}
								if ((r.getBadgeAssertion().getStatus() == BadgeAssertion.BadgeAssertionStatus.editing) &&
									statusString.equals(BadgeAssertion.BadgeAssertionStatus.issued.name())) {
									matchFound = true;
									break;
								}
							}
						} else if (IssuedBadgesTableModel.IssuedBadgesFilter.ISSUER.name().equals(filter.getFilter())) {
							for (String issuer : multiSelectionFilter.getValues()) {
								if (issuer.equals(issuerHashString)) {
									matchFound = true;
									break;
								}
							}
						}
					}
					if (!matchFound) {
						return false;
					}
				}
				return true;
			}).toList();
			tableModel.setObjects(filteredRows);
		}
		tableEl.reset(true, true, true);
	}

	private void forgeRow(IssuedBadgeRow row, OpenBadgesManager.BadgeAssertionWithSize badgeAssertionWithSize) {
		BadgeAssertion badgeAssertion = badgeAssertionWithSize.badgeAssertion();

		String imageUrl = mediaUrl + "/" + badgeAssertion.getBakedImage();
		BadgeImageComponent badgeImage = new BadgeImageComponent("badgeImage", imageUrl, BadgeImageComponent.Size.cardSize);
		row.setBadgeImage(badgeImage);

		row.setIssuedOn(Formatter.getInstance(getLocale()).formatDateAndTime(badgeAssertion.getIssuedOn()));

		row.setIssuer(badgeAssertion.getBadgeClass().getIssuerDisplayString());

		row.setDownloadUrl(downloadUrl + "/" + badgeAssertion.getDownloadFileName());

		if (identity == null) {
			String recipientName = userManager.getUserDisplayName(badgeAssertion.getRecipient());
			row.setRecipientName(StringHelper.xssScan(recipientName));
		}

		if (showLinkedInLink(badgeAssertion)) {
			row.setAddToLinkedInUrl(openBadgesManager.badgeAssertionAsLinkedInUrl(badgeAssertion));
		}

		String toolId = "tool_" + badgeAssertion.getUuid();
		FormLink toolLink = (FormLink) flc.getComponent(toolId);
		if (toolLink == null) {
			toolLink = uifactory.addFormLink(toolId, CMD_TOOLS, "", tableEl, Link.LINK | Link.NONTRANSLATED);
			toolLink.setTranslator(getTranslator());
			toolLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
			toolLink.setTitle(translate("table.header.actions"));
		}
		toolLink.setUserObject(badgeAssertion);
		row.setToolLink(toolLink);
	}

	private boolean showLinkedInLink(BadgeAssertion badgeAssertion) {
		if (!mine) {
			return false;
		}
		if (BadgeAssertion.BadgeAssertionStatus.revoked.equals(badgeAssertion.getStatus())) {
			return false;
		}
		if (openBadgesManager.isBadgeAssertionExpired(badgeAssertion)) {
			return false;
		}
		return true;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc) {
			cleanUp();
		} else if (source == toolsCtrl) {
			if (calloutCtrl != null) {
				calloutCtrl.deactivate();
			}
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(badgeAssertionPublicController);
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		badgeAssertionPublicController = null;
		cmc = null;
		calloutCtrl = null;
		toolsCtrl = null;
	}


	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (event instanceof SelectionEvent selectionEvent) {
			IssuedBadgeRow row = tableModel.getObject(selectionEvent.getIndex());
			String uuid = row.getBadgeAssertion().getUuid();
			doOpenDetails(ureq, uuid);
		} else if (source == flc) {
			String selectString = ureq.getParameter("select");
			if (selectString != null) {
				Long assertionKey = Long.parseLong(selectString);
				tableModel.getObjects().stream()
						.filter(ba -> ba.getBadgeAssertion().getKey().equals(assertionKey))
						.findFirst().ifPresent(row -> doOpenDetails(ureq, row.getBadgeAssertion().getUuid()));
			}
		} else if (source == tableEl) {
			if (event instanceof FlexiTableSearchEvent searchEvent && FlexiTableSearchEvent.FILTER.equals(searchEvent.getCommand())) {
				loadModel(ureq, searchEvent.getFilters());
			}
		} else if (source instanceof FormLink link) {
			if (CMD_TOOLS.equals(link.getCmd()) && link.getUserObject() instanceof BadgeAssertion badgeAssertion) {
				doOpenTools(ureq, link, badgeAssertion);
			}
		}
	}

	private void doOpenTools(UserRequest ureq, FormLink link, BadgeAssertion badgeAssertion) {
		toolsCtrl = new ToolsController(ureq, getWindowControl(), badgeAssertion);
		listenTo(toolsCtrl);

		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
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
	protected void doDispose() {
		removeTemporaryFiles();
	}

	private void removeTemporaryFiles() {
		if (tableModel != null && tableModel.getObjects() != null) {
			for (IssuedBadgeRow row : tableModel.getObjects()) {
				File temporaryFile = new File(WebappHelper.getTmpDir(), row.getBadgeAssertion().getDownloadFileName());
				if (temporaryFile.exists()) {
					temporaryFile.delete();
				}
			}
		}
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>();
		if (rowObject instanceof IssuedBadgeRow issuedBadgeRow) {
			components.add(issuedBadgeRow.getBadgeImage());
		}

		return components;
	}

	private File createTemporaryFile(String fileName) {
		File temporaryFile = new File(WebappHelper.getTmpDir(), fileName);
		if (temporaryFile.exists()) {
			return temporaryFile;
		}

		Optional<LocalFileImpl> bakedImageFile = tableModel.getObjects().stream()
				.filter(row -> row.getDownloadUrl().contains(fileName))
				.map(row -> openBadgesManager.getBadgeAssertionVfsLeaf(row.getBadgeAssertion().getBakedImage()))
				.filter(leaf -> leaf instanceof LocalFileImpl)
				.map(leaf -> (LocalFileImpl) leaf)
				.findFirst();

		if (bakedImageFile.isPresent()) {
			FileUtils.copyFileToFile(bakedImageFile.get().getBasefile(), temporaryFile, false);
			return temporaryFile;
		}

		return null;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	public void showAssertion(UserRequest ureq, Long key) {
		tableModel.getObjects().stream().filter(row -> row.getBadgeAssertion().getKey() == key)
				.forEach(row -> doOpenDetails(ureq, row.getBadgeAssertion().getUuid()));
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

	private class BadgeAssertionDownloadableMediaFileMapper implements Mapper {

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			if (calloutCtrl != null) {
				calloutCtrl.deactivate();
				cleanUp();
			}

			File temporaryFile = createTemporaryFile(relPath);
			if (temporaryFile != null) {
				return new DownloadeableMediaResource(temporaryFile);
			}

			return new NotFoundMediaResource();
		}
	}

	private class ToolsController extends BasicController {

		protected ToolsController(UserRequest ureq, WindowControl wControl, BadgeAssertion badgeAssertion) {
			super(ureq, wControl);

			VelocityContainer mainVC = createVelocityContainer("issued_badge_tools");

			badgeAssertion.getDownloadFileName();

			tableModel.getObjects().forEach(r -> {
				if (r.getBadgeAssertion().getUuid().equals(badgeAssertion.getUuid())) {
					mainVC.contextPut("downloadUrl", r.getDownloadUrl());
					if (showLinkedInLink(r.getBadgeAssertion())) {
						mainVC.contextPut("addToLinkedInUrl", r.getAddToLinkedInUrl());
					}
				}
			});
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.CLOSE_EVENT);
		}
	}
}
