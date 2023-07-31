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
package org.olat.course.quota.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.olat.admin.quota.GenericQuotaEditController;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.chart.PieChartElement;
import org.olat.core.gui.components.chart.PiePoint;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeNodeComparator;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseFactory;
import org.olat.course.PersistingCourseImpl;
import org.olat.course.assessment.ui.tool.event.CourseNodeEvent;
import org.olat.course.core.CourseElement;
import org.olat.course.core.CourseElementSearchParams;
import org.olat.course.core.CourseNodeService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeWithFiles;
import org.olat.course.nodes.PFCourseNode;
import org.olat.course.nodes.bc.CoachFolderFactory;
import org.olat.course.nodes.bc.CourseDocumentsFactory;
import org.olat.course.nodes.pf.manager.PFManager;
import org.olat.course.run.GoToEvent;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Jul 04, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CourseQuotaUsageController extends FormBasicController {

	private static final String ALL_TAB_ID = "All";
	private static final String CMD_DISPLAY_RSS = "displayRss";
	private static final String CMD_EDIT_QUOTA = "editQuota";
	private static final String CMD_EXTERNAL = "external";
	private static final String CUR_USED_PROGRESS_BAR = "curUsed";
	private static final String DISPLAY_RSS_LINK = "displayRssLink";
	private static final String EDIT_QUOTA_LINK = "editQuotaLink";
	private static final String EXTERNAL_LINK = "externalLink";
	private static final String TABLE_COLUMN_DISPLAY_RSS = "table.column.display.rss";
	private static final String TABLE_COLUMN_EDIT_QUOTA = "table.column.edit.quota";

	private final List<CourseQuotaUsageRow> courseQuotaUsageRows = new ArrayList<>();
	// hashmap to improve performance, mapping subIdents to row
	private final Map<String, CourseQuotaUsageRow> subIdentToRow = new HashMap<>();
	// hashmap to improve performance, mapping relativePaths of each element to row
	private final Map<String, CourseQuotaUsageRow> relPathToRow = new HashMap<>();
	private final IdentityEnvironment identityEnvironment;
	private final RepositoryEntry entry;
	private final Roles roles;
	private CourseQuotaUsageTreeTableModel courseQuotaUsageTableDataModel;

	private FlexiTableElement tableEl;
	private FlexiFiltersTab allTab;
	private FlexiFiltersTab internalWithQuotaTab;
	private FlexiFiltersTab internalWithoutQuotaTab;
	private FlexiFiltersTab externalTab;

	private CloseableModalController cmc;
	private GenericQuotaEditController quotaEditCtr;

	@Autowired
	private CourseNodeService courseNodeService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private QuotaManager quotaManager;
	@Autowired
	private PFManager pfManager;

	public CourseQuotaUsageController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, "courseQuotaUsage");
		this.entry = entry;
		this.roles = ureq.getUserSession().getRoles();
		identityEnvironment = ureq.getUserSession().getIdentityEnvironment();

		TableGuiConfiguration tableGuiPrefs = new TableGuiConfiguration();
		tableGuiPrefs.setTableEmptyMessage(translate("table.course.quota.empty"), "", "o_icon_notification");

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		TreeNodeFlexiCellRenderer treeNodeRenderer = new TreeNodeFlexiCellRenderer(false);

		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CourseQuotaUsageCols.resource, treeNodeRenderer));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CourseQuotaUsageCols.type, new CourseNodeTypeCellRenderer()));
		// tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CourseQuotaUsageCols.external));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CourseQuotaUsageCols.numOfFiles));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CourseQuotaUsageCols.totalUsedSize));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CourseQuotaUsageCols.quota));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CourseQuotaUsageCols.curUsed));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CourseQuotaUsageCols.editQuota));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CourseQuotaUsageCols.displayRss));

		courseQuotaUsageTableDataModel = new CourseQuotaUsageTreeTableModel(tableColumnModel, getLocale());

		tableEl = uifactory.addTableElement(getWindowControl(), "courseQuotaUsageTable", courseQuotaUsageTableDataModel, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
		tableEl.setExportEnabled(true);

		initFiltersPresets(ureq);
		loadModel(null, allTab);
		loadCourseQuotaStatistics();
	}

	private void loadModel(String searchString, FlexiFiltersTab filtersTab) {
		CourseElementSearchParams searchParams = new CourseElementSearchParams();
		searchParams.setRepositoryEntries(Collections.singletonList(entry));
		List<CourseElement> courseElements = courseNodeService.getCourseElements(searchParams);
		courseQuotaUsageRows.clear();

		// load fixed folders first (if available)
		loadCourseFolder();
		if (CourseFactory.loadCourse(entry).getCourseConfig().isCoachFolderEnabled()) {
			loadCoachFolder();
		}
		if (CourseFactory.loadCourse(entry).getCourseConfig().isDocumentsEnabled()) {
			loadCourseDocumentsFolder();
		}

		for (CourseElement courseElement : courseElements) {
			// ignore rootNode (course itself) TODO: Needed or not?
			/*if (courseElement.getLongTitle().equals(CourseFactory.loadCourse(entry).getRunStructure().getRootNode().getLongTitle())) {
				continue;
			}*/

			FormLink editQuotaLink = null;
			FormLink displayRssLink = uifactory.addFormLink(DISPLAY_RSS_LINK + courseElement.getSubIdent(), CMD_DISPLAY_RSS, TABLE_COLUMN_DISPLAY_RSS, null, flc, Link.LINK);
			displayRssLink.setUserObject(courseElement.getSubIdent());

			String businessPath = "[RepositoryEntry:" + entry.getKey() + "][CourseNode:" + courseElement.getSubIdent() + "]";
			displayRssLink.setUrl(BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathStrings(businessPath));

			if (courseElement.getType().equals("bc") || courseElement.getType().equals("pf")) {
				editQuotaLink = uifactory.addFormLink(EDIT_QUOTA_LINK + courseElement.getSubIdent(), CMD_EDIT_QUOTA, TABLE_COLUMN_EDIT_QUOTA, null, flc, Link.LINK);
				editQuotaLink.setUserObject(courseElement.getSubIdent());
			}

			CourseQuotaUsageRow row =
					new CourseQuotaUsageRow(
							courseElement.getSubIdent(),
							courseElement.getLongTitle(),
							courseElement.getType(),
							editQuotaLink,
							displayRssLink);
			courseQuotaUsageRows.add(row);
			subIdentToRow.put(row.getSubIdent(), row);
		}

		// build up tree structure of course elements
		CourseNode rootNode = CourseFactory.loadCourse(entry).getRunStructure().getRootNode();
		for (int i = 0; i < rootNode.getChildCount(); i++) {
			setTreeStructure(rootNode, (CourseNode) rootNode.getChildAt(i), 0);
		}

		// load filterTabs, depending on search/tabs
		loadFilterTabs(filtersTab, searchString);

		List<String> vfsMetadataList = vfsRepositoryService.getRelativePaths(relPathToRow.keySet().stream().toList());
		for (Map.Entry<String, CourseQuotaUsageRow> relativePath : relPathToRow.entrySet()) {
			int numOfFiles = (int) vfsMetadataList
					.stream()
					.filter(v -> v.contains(relativePath.getKey()))
					.count();
			relativePath.getValue().setNumOfFiles(numOfFiles);
		}

		courseQuotaUsageRows.sort(new CourseQuotaUsageTreeNodeComparator());
		courseQuotaUsageTableDataModel.setObjects(courseQuotaUsageRows);
		tableEl.reset(true, true, true);
	}

	private void loadCourseFolder() {
		VFSContainer courseFolderContainer = CourseFactory.loadCourse(entry).getCourseFolderContainer(identityEnvironment);
		Quota courseFolderQuota = Objects.requireNonNull(VFSManager.findInheritedSecurityCallback(courseFolderContainer)).getQuota();
		if (courseFolderQuota != null) {
			// subIdent starting with 0, for the correct order in table
			String subIdent = "0" + courseFolderContainer.getRelPath();

			FormLink editQuotaLink = uifactory.addFormLink(EDIT_QUOTA_LINK + subIdent, CMD_EDIT_QUOTA, TABLE_COLUMN_EDIT_QUOTA, null, flc, Link.LINK);
			editQuotaLink.setUserObject(subIdent);
			FormLink displayRssLink = uifactory.addFormLink(DISPLAY_RSS_LINK + subIdent, CMD_DISPLAY_RSS, TABLE_COLUMN_DISPLAY_RSS, null, flc, Link.LINK);
			displayRssLink.setUserObject(subIdent);

			ProgressBar currentlyUsedBar = setProgressBar(courseFolderQuota);

			CourseQuotaUsageRow courseFolderRow =
					new CourseQuotaUsageRow(
							subIdent,
							translate("command.coursefolder"),
							"bc",
							editQuotaLink,
							displayRssLink);
			courseFolderRow.setElementQuota(courseFolderQuota);
			courseFolderRow.setCurUsed(currentlyUsedBar);
			courseFolderRow.setRelPath(subIdent.substring(2));
			courseFolderRow.setTotalUsedSize(courseFolderQuota.getQuotaKB() - courseFolderQuota.getRemainingSpace());
			courseQuotaUsageRows.add(courseFolderRow);
			relPathToRow.put(subIdent.substring(2), courseFolderRow);
			subIdentToRow.put(courseFolderRow.getSubIdent(), courseFolderRow);
		}
	}

	private void loadCoachFolder() {
		UserCourseEnvironment userCourseEnvironment = new UserCourseEnvironmentImpl(identityEnvironment, CourseFactory.loadCourse(entry).getCourseEnvironment());
		Quota coachFolderQuota = CoachFolderFactory.getSecurityCallback(userCourseEnvironment).getQuota();
		if (coachFolderQuota != null && !coachFolderQuota.getPath().contains(PersistingCourseImpl.COURSEFOLDER)) {
			// subIdent starting with 0, for the correct order in table
			String subIdent = "00" + coachFolderQuota.getPath();

			FormLink editQuotaLink = uifactory.addFormLink(EDIT_QUOTA_LINK + subIdent, CMD_EDIT_QUOTA, TABLE_COLUMN_EDIT_QUOTA, null, flc, Link.LINK);
			editQuotaLink.setUserObject(subIdent);
			FormLink displayRssLink = uifactory.addFormLink(DISPLAY_RSS_LINK + subIdent, CMD_DISPLAY_RSS, TABLE_COLUMN_DISPLAY_RSS, null, flc, Link.LINK);
			displayRssLink.setUserObject(subIdent);

			ProgressBar currentlyUsedBar = setProgressBar(coachFolderQuota);

			CourseQuotaUsageRow coachFolderRow =
					new CourseQuotaUsageRow(
							subIdent,
							translate("command.coachfolder"),
							"bc",
							editQuotaLink,
							displayRssLink);
			coachFolderRow.setElementQuota(coachFolderQuota);
			coachFolderRow.setCurUsed(currentlyUsedBar);
			coachFolderRow.setRelPath(subIdent.substring(3));
			coachFolderRow.setTotalUsedSize(coachFolderQuota.getQuotaKB() - coachFolderQuota.getRemainingSpace());
			courseQuotaUsageRows.add(coachFolderRow);
			relPathToRow.put(subIdent.substring(3), coachFolderRow);
			subIdentToRow.put(coachFolderRow.getSubIdent(), coachFolderRow);
		}
	}

	private void loadCourseDocumentsFolder() {
		UserCourseEnvironment userCourseEnvironment = new UserCourseEnvironmentImpl(identityEnvironment, CourseFactory.loadCourse(entry).getCourseEnvironment());
		Quota courseDocumentsFolderQuota = CourseDocumentsFactory.getSecurityCallback(userCourseEnvironment).getQuota();
		if (courseDocumentsFolderQuota != null && !courseDocumentsFolderQuota.getPath().contains(PersistingCourseImpl.COURSEFOLDER)) {
			// subIdent starting with 0, for the correct order in table
			String subIdent = "000" + courseDocumentsFolderQuota.getPath();

			FormLink editQuotaLink = uifactory.addFormLink(EDIT_QUOTA_LINK + subIdent, CMD_EDIT_QUOTA, TABLE_COLUMN_EDIT_QUOTA, null, flc, Link.LINK);
			editQuotaLink.setUserObject(subIdent);
			FormLink displayRssLink = uifactory.addFormLink(DISPLAY_RSS_LINK + subIdent, CMD_DISPLAY_RSS, TABLE_COLUMN_DISPLAY_RSS, null, flc, Link.LINK);
			displayRssLink.setUserObject(subIdent);

			ProgressBar currentlyUsedBar = setProgressBar(courseDocumentsFolderQuota);

			CourseQuotaUsageRow courseDocumentsFolderRow =
					new CourseQuotaUsageRow(
							subIdent,
							translate("command.documents"),
							"bc",
							editQuotaLink,
							displayRssLink);
			courseDocumentsFolderRow.setElementQuota(courseDocumentsFolderQuota);
			courseDocumentsFolderRow.setCurUsed(currentlyUsedBar);
			courseDocumentsFolderRow.setRelPath(subIdent.substring(4));
			courseDocumentsFolderRow.setTotalUsedSize(courseDocumentsFolderQuota.getQuotaKB() - courseDocumentsFolderQuota.getRemainingSpace());
			courseQuotaUsageRows.add(courseDocumentsFolderRow);
			relPathToRow.put(subIdent.substring(4), courseDocumentsFolderRow);
			subIdentToRow.put(courseDocumentsFolderRow.getSubIdent(), courseDocumentsFolderRow);
		}
	}

	private ProgressBar setProgressBar(Quota folderQuota) {
		ProgressBar currentlyUsedBar = new ProgressBar(CUR_USED_PROGRESS_BAR);
		currentlyUsedBar.setLabelAlignment(ProgressBar.LabelAlignment.none);
		currentlyUsedBar.setMax(100);
		currentlyUsedBar.setActual((float) (folderQuota.getQuotaKB() - folderQuota.getRemainingSpace()) / (float) (folderQuota.getQuotaKB()) * 100);
		ProgressBar.BarColor barColor = currentlyUsedBar.getActual() < 80
				? ProgressBar.BarColor.primary
				: ProgressBar.BarColor.danger;
		currentlyUsedBar.setBarColor(barColor);

		return currentlyUsedBar;
	}

	private void loadFilterTabs(FlexiFiltersTab filtersTab, String searchString) {
		List<CourseQuotaUsageRow> rowsToRemove = new ArrayList<>();
		for (CourseQuotaUsageRow row : courseQuotaUsageRows) {
			// according to filtersTab/searchString remove every row which does not comply to that row
			if ((filtersTab.equals(internalWithQuotaTab) &&
					(row.getExternal() != null || !StringHelper.containsNonWhitespace(row.getQuota())))
					|| (filtersTab.equals(internalWithoutQuotaTab)
					&& (row.getExternal() != null || (StringHelper.containsNonWhitespace(row.getQuota()) || row.getTotalUsedSize() == null)))
					|| (filtersTab.equals(externalTab) && row.getExternal() == null)
					|| searchString != null && (!row.getResource().toLowerCase().contains(searchString.toLowerCase())
					&& !row.getType().toLowerCase().contains(searchString.toLowerCase()))) {
				rowsToRemove.add(row);
				// decrementNumOfChildren, except for special case participant folders
				if (row.getParent() != null && !((CourseQuotaUsageRow) row.getParent()).getType().equals("pf")) {
					((CourseQuotaUsageRow) row.getParent()).decrementNumberOfChildren();
				}
			}
		}
		courseQuotaUsageRows.removeAll(rowsToRemove);

		// add all parents back, if they got removed, so full tree can be shown
		// Independent of filterTab
		if (!rowsToRemove.isEmpty()) {
			List<CourseQuotaUsageRow> parentsToAdd = new ArrayList<>();
			for (CourseQuotaUsageRow row : courseQuotaUsageRows) {
				CourseQuotaUsageRow parent = (CourseQuotaUsageRow) row.getParent();
				if (parent != null) {
					do {
						if (!courseQuotaUsageRows.contains(parent) && !parentsToAdd.contains(parent)) {
							parent.incrementNumberOfChildren();
							parentsToAdd.add(parent);
						}
						parent = (CourseQuotaUsageRow) parent.getParent();
					} while (parent != null);
				}
			}
			courseQuotaUsageRows.addAll(parentsToAdd);
		}
	}

	private void setTreeStructure(CourseNode parentNode, CourseNode childNode, int depth) {
		CourseQuotaUsageRow row = subIdentToRow.get(childNode.getIdent());

		if (row != null) {
			CourseQuotaUsageRow parentRow = subIdentToRow.get(parentNode.getIdent());
			row.setParent(parentRow);
			if (parentRow != null) {
				parentRow.incrementNumberOfChildren();
			}

			// set values for nodesWithFiles
			if (childNode instanceof CourseNodeWithFiles nodeWithFiles) {
				CourseEnvironment courseEnvironment = CourseFactory.loadCourse(entry).getCourseEnvironment();
				// remove dedicated folderElements, which are inside courseFolder
				if (nodeWithFiles.isStorageInCourseFolder()) {
					courseQuotaUsageRows.remove(row);
					if (row.getParent() != null) {
						((CourseQuotaUsageRow) row.getParent()).decrementNumberOfChildren();
					}
				} else {
					Quota quota = nodeWithFiles.getQuota(getIdentity(), roles, entry, quotaManager);
					// set quota values if available
					if (quota != null) {
						ProgressBar currentlyUsedBar = setProgressBar(quota);

						row.setElementQuota(quota);
						if (!(nodeWithFiles instanceof PFCourseNode)) {
							row.setCurUsed(currentlyUsedBar);
						}
						if (nodeWithFiles.isStorageExtern()) {
							FormLink externalLink = uifactory.addFormLink(EXTERNAL_LINK + childNode.getIdent(), CMD_EXTERNAL, "table.column.external.link", null, flc, Link.LINK);
							row.setExternal(externalLink);
						}
					}
					row.setTotalUsedSize(nodeWithFiles.getUsageKb(courseEnvironment));

					String relPath = nodeWithFiles.getRelPath(courseEnvironment);
					if (relPath != null && relPath.length() > 1) {
						row.setRelPath(relPath.substring(1));
					}

					// only special use cases should be set via interface
					// e.g. file dialog: counting happens through element count
					Integer numOfFiles = nodeWithFiles.getNumOfFiles(courseEnvironment);
					if (numOfFiles != null) {
						row.setNumOfFiles(numOfFiles);
					} else {
						relPathToRow.put(row.getRelPath(), row);
					}

					// build up participant folder structure with return/drop boxes
					if (nodeWithFiles instanceof PFCourseNode pfCourseNode) {
						List<VFSItem> participantsInPF = VFSManager.olatRootContainer(relPath, null).getItems();
						List<LocalFolderImpl> participantFolders = participantsInPF.stream().map(p -> VFSManager.olatRootContainer(p.getRelPath(), null)).toList();
						List<String> participants = pfManager.getParticipants(getIdentity(), courseEnvironment, true).stream().map(p -> p.getKey().toString()).toList();

						for (VFSItem participant : participantsInPF) {
							CourseQuotaUsageRow participantFolderRow =
									new CourseQuotaUsageRow(
											participant.getRelPath(),
											UserManager.getInstance().getUserDisplayName(Long.valueOf(participant.getName())) + (!participants.contains(participant.getName()) ? " " + translate("course.quota.pf.former") : ""),
											"bc",
											null,
											null);
							participantFolderRow.setTotalUsedSize(VFSManager.getUsageKB(participantFolders.stream().filter(pf -> pf.getRelPath().equals(participant.getRelPath())).findFirst().orElse(null)));
							participantFolderRow.setParent(relPathToRow.get(pfCourseNode.getRelPath(courseEnvironment).substring(1)));
							relPathToRow.get(pfCourseNode.getRelPath(courseEnvironment).substring(1)).incrementNumberOfChildren();
							courseQuotaUsageRows.add(participantFolderRow);
							relPathToRow.put(participant.getRelPath().substring(1), participantFolderRow);
							subIdentToRow.put(participantFolderRow.getSubIdent(), participantFolderRow);

							List<VFSItem> dropReturnBoxes = ((LocalFolderImpl) participant).getItems();

							for (VFSItem pfBox : dropReturnBoxes) {
								CourseQuotaUsageRow participantPfBoxRow =
										new CourseQuotaUsageRow(
												pfBox.getRelPath(),
												pfBox.getName().contains("dropbox") ? translate("pf.dropbox") : translate("pf.returnbox"),
												"bc",
												null,
												null);
								participantPfBoxRow.setTotalUsedSize(VFSManager.getUsageKB(VFSManager.olatRootContainer(pfBox.getRelPath())));
								participantPfBoxRow.setParent(participantFolderRow);
								participantFolderRow.incrementNumberOfChildren();
								courseQuotaUsageRows.add(participantPfBoxRow);
								relPathToRow.put(pfBox.getRelPath().substring(1), participantPfBoxRow);
								subIdentToRow.put(participantPfBoxRow.getSubIdent(), participantPfBoxRow);

								// set quota and progressbar for drop- and returnBox
								if (quota != null) {
									participantPfBoxRow.setElementQuota(quota);

									ProgressBar currentlyUsedBar = new ProgressBar(CUR_USED_PROGRESS_BAR);
									currentlyUsedBar.setLabelAlignment(ProgressBar.LabelAlignment.none);
									currentlyUsedBar.setMax(100);
									currentlyUsedBar.setActual((float) participantPfBoxRow.getTotalUsedSize() / (float) quota.getQuotaKB() * 100);
									ProgressBar.BarColor barColor = currentlyUsedBar.getActual() < 80
											? ProgressBar.BarColor.primary
											: ProgressBar.BarColor.danger;
									currentlyUsedBar.setBarColor(barColor);

									participantPfBoxRow.setCurUsed(currentlyUsedBar);
								}
							}

						}
					}
				}
			}

			// recursively set child/parent relation, starting at bottom of tree - 'youngest' child
			for (int i = 0; i < childNode.getChildCount(); i++) {
				setTreeStructure(childNode, (CourseNode) childNode.getChildAt(i), ++depth);
			}

			// remove every element which has no quota data and is not a structure
			if (row.getTotalUsedSize() == null
					&& !row.getType().equals("st")
					&& row.getNumOfChildren() < 1
					&& courseQuotaUsageRows.remove(row)
					&& row.getParent() != null) {
				((CourseQuotaUsageRow) row.getParent()).decrementNumberOfChildren();
			}
			// remove empty structures
			if (row.getType().equals("st")
					&& row.getNumOfChildren() < 1
					&& courseQuotaUsageRows.remove(row)
					&& row.getParent() != null) {
				((CourseQuotaUsageRow) row.getParent()).decrementNumberOfChildren();
			}
		}
	}

	private void loadCourseQuotaStatistics() {
		PieChartElement chartEl = new PieChartElement("quota.chart");
		chartEl.setElementCssClass("o_course_quota_piechart");
		chartEl.setLayer(13);
		chartEl.setTitleY(4);
		long courseQuotaSum = courseQuotaUsageRows
				.stream()
				.mapToLong((c -> c.getTotalUsedSize() != null ?
						c.getTotalUsedSize()
						: 0L))
				.sum();
		long courseQuotaNumOfFiles = courseQuotaUsageRows
				.stream()
				.mapToLong(c -> c.getNumOfFiles() != null
						? c.getNumOfFiles()
						: 0L)
				.sum();
		long courseQuotaTotalInternalSize = courseQuotaUsageRows
				.stream()
				.mapToLong((c -> c.getTotalUsedSize() != null && c.getExternal() == null ?
						c.getTotalUsedSize()
						: 0L))
				.sum();
		long courseQuotaInternalSizeWithQuota = courseQuotaUsageRows
				.stream()
				.mapToLong((c -> c.getExternal() == null && c.getElementQuota() != null ?
						c.getTotalUsedSize()
						: 0L))
				.sum();
		long courseQuotaInternalSizeWithoutQuota = courseQuotaUsageRows
				.stream()
				.mapToLong((c -> c.getExternal() == null && c.getElementQuota() == null && c.getTotalUsedSize() != null ?
						c.getTotalUsedSize()
						: 0L))
				.sum();
		/*long courseQuotaExternalSize = courseQuotaUsageRows
				.stream()
				.mapToLong((c -> c.getTotalUsedSize() != null && c.getExternal() != null ?
						c.getTotalUsedSize()
						: 0L))
				.sum();*/

		int elementsWithQuota = 0;
		int elementsWithoutQuota = 0;
		//int elementsExternal = 0;
		// count occurrences
		for (CourseQuotaUsageRow row : courseQuotaUsageRows) {
			if (row.getExternal() == null && row.getElementQuota() != null) {
				elementsWithQuota++;
			}
			if (row.getExternal() == null && row.getElementQuota() == null && row.getTotalUsedSize() != null) {
				elementsWithoutQuota++;
			}
			/*if (row.getExternal() != null) {
				elementsExternal++;
				sumOfElements++;
			}*/
		}

		flc.contextPut("numOfElementWithQuota", elementsWithQuota);
		flc.contextPut("numOfElementWithoutQuota", elementsWithoutQuota);
		//flc.contextPut("numOfElementExternal", elementsExternal);
		flc.contextPut("withQuotaPercentage", Math.round((double) courseQuotaInternalSizeWithQuota / (double) courseQuotaTotalInternalSize * 100.0d));
		flc.contextPut("withoutQuotaPercentage", Math.round((double) courseQuotaInternalSizeWithoutQuota / (double) courseQuotaTotalInternalSize * 100.0d));
		//flc.contextPut("externalPercentage", Math.round((double) elementsExternal / (double) sumOfElements * 100.0d));

		flc.contextPut("courseQuotaTotalSize", Formatter.formatKBytes(courseQuotaSum));
		flc.contextPut("courseQuotaNumOfFiles", courseQuotaNumOfFiles);
		flc.contextPut("courseQuotaInternalSize", Formatter.formatKBytes(courseQuotaTotalInternalSize));
		//flc.contextPut("courseQuotaExternalSize", Formatter.formatKBytes(courseQuotaExternalSize));

		chartEl.setTitle(Formatter.formatKBytes(courseQuotaSum));
		chartEl.addPoints(new PiePoint(elementsWithQuota, "o_course_quota_intern_quota"));
		chartEl.addPoints(new PiePoint(elementsWithoutQuota, "o_course_quota_intern"));
		//chartEl.addPoints(new PiePoint(elementsExternal, "o_course_quota_extern"));
		flc.add("quota.chart", chartEl);
	}

	private void doEditQuota(UserRequest ureq, Quota q) {
		if (guardModalController(quotaEditCtr)) return;

		quotaEditCtr = new GenericQuotaEditController(ureq, getWindowControl(), q, false);
		listenTo(quotaEditCtr);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), quotaEditCtr.getInitialComponent(), true, translate("qf.edit"));
		cmc.activate();
		listenTo(cmc);
	}

	private void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();

		allTab = FlexiFiltersTabFactory.tabWithFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.clear, List.of());
		internalWithQuotaTab = FlexiFiltersTabFactory.tabWithFilters("internal.quota", translate("filter.internal.with.quota"),
				TabSelectionBehavior.reloadData, List.of());
		internalWithoutQuotaTab = FlexiFiltersTabFactory.tabWithFilters("internal", translate("filter.internal.without.quota"),
				TabSelectionBehavior.clear, List.of());
		/*externalTab = FlexiFiltersTabFactory.tabWithFilters(CMD_EXTERNAL, translate("filter.external"),
				TabSelectionBehavior.clear, List.of());*/
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		tabs.add(internalWithQuotaTab);
		tabs.add(internalWithoutQuotaTab);
		//tabs.add(externalTab);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == quotaEditCtr
				&& (event == Event.CANCELLED_EVENT
				|| event == Event.CHANGED_EVENT)) {
			loadModel(tableEl.getQuickSearchString(), tableEl.getSelectedFilterTab());
			cmc.deactivate();

		}
		cleanUp();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			String subIdent = String.valueOf(link.getUserObject());

			if (CMD_EDIT_QUOTA.equals(cmd)) {
				courseQuotaUsageTableDataModel.getObjects()
						.stream()
						.filter(cr -> cr.getSubIdent().equals(subIdent))
						.findFirst().ifPresent(selectedRow -> doEditQuota(ureq, selectedRow.getElementQuota()));
			} else if (CMD_DISPLAY_RSS.equals(cmd)) {
				if (subIdent.contains(PersistingCourseImpl.COURSEFOLDER)
						|| subIdent.contains(CoachFolderFactory.FOLDER_NAME)
						|| subIdent.contains(CourseDocumentsFactory.FOLDER_NAME)) {
					fireEvent(ureq, new CourseNodeEvent(CourseNodeEvent.SELECT_COURSE_NODE, subIdent));
				} else {
					fireEvent(ureq, new GoToEvent(GoToEvent.GOTO_NODE, subIdent));
				}
			}
		} else if (event instanceof FlexiTableFilterTabEvent tab) {
			if (tab.getTab() == allTab) {
				loadModel(null, allTab);
			} else if (tab.getTab() == internalWithQuotaTab) {
				loadModel(null, internalWithQuotaTab);
			} else if (tab.getTab() == internalWithoutQuotaTab) {
				loadModel(null, internalWithoutQuotaTab);
			} else if (tab.getTab() == externalTab) {
				loadModel(null, externalTab);
			}
		} else if (event instanceof FlexiTableSearchEvent) {
			loadModel(tableEl.getQuickSearchString(), allTab);
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(quotaEditCtr);
		removeAsListenerAndDispose(cmc);
		quotaEditCtr = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// no need
	}

	private static class CourseQuotaUsageTreeNodeComparator extends FlexiTreeNodeComparator {

		@Override
		protected int compareNodes(FlexiTreeTableNode o1, FlexiTreeTableNode o2) {
			CourseQuotaUsageRow r1 = (CourseQuotaUsageRow) o1;
			CourseQuotaUsageRow r2 = (CourseQuotaUsageRow) o2;

			int c;
			if (r1 == null || r2 == null) {
				c = compareNullObjects(r1, r2);
			} else {
				String c1 = r1.getSubIdent();
				String c2 = r2.getSubIdent();
				if (c1 == null || c2 == null) {
					c = -compareNullObjects(c1, c2);
				} else {
					c = c1.compareTo(c2);
				}
			}
			return c;
		}
	}
}
