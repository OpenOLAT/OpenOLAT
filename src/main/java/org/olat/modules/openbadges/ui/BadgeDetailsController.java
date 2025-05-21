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

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.ui.CreateBadgeClassWizardContext.Mode;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-07-13<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeDetailsController extends BasicController {

	private Long badgeClassKey;
	private final RepositoryEntrySecurity reSecurity;

	private final VelocityContainer mainVC;
	
	private final Link awardManuallyButton;
	private final Link createNewVersionAndEdit;
	private final Link edit;

	private TabbedPane tabPane;
	
	private int overviewTab;
	private BadgeDetailsOverviewController overviewCtrl;

	private int recipientsTab;
	private BadgeDetailsRecipientsController recipientsCtrl;

	private final String mediaUrl;

	private CloseableModalController cmc;
	private IssueGlobalBadgeController issueGlobalBadgeCtrl;
	private IssueCourseBadgeController issueCourseBadgeCtrl;
	private CreateBadgeClassWizardContext createBadgeClassContext;
	private StepsMainRunController stepsController;

	@Autowired
	private OpenBadgesManager openBadgesManager;

	public BadgeDetailsController(UserRequest ureq, WindowControl wControl, Long badgeClassKey,
								  RepositoryEntrySecurity reSecurity) {
		super(ureq, wControl);
		this.badgeClassKey = badgeClassKey;
		this.reSecurity = reSecurity;

		mainVC = createVelocityContainer("badge_details");
		
		awardManuallyButton = LinkFactory.createButton("award.manually", mainVC, this);
		awardManuallyButton.setIconLeftCSS("o_icon o_icon-fw o_icon_badge");
		
		createNewVersionAndEdit = LinkFactory.createButton("create.a.new.version.and.edit", mainVC, this);
		createNewVersionAndEdit.setIconLeftCSS("o_icon o_icon-fw o_icon_add");

		edit = LinkFactory.createButton("edit", mainVC, this);
		edit.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		
		tabPane = new TabbedPane("tabs", getLocale());
		tabPane.addListener(this);
		mediaUrl = registerMapper(ureq, new BadgeClassMediaFileMapper());
		initTabPane(ureq);
		
		mainVC.put("tabs", tabPane);
		putInitialPanel(mainVC);
		
		loadData();

		tabPane.setSelectedPane(ureq, overviewTab);
	}

	private void initTabPane(UserRequest ureq) {
		overviewTab = tabPane.addTab(ureq, translate("tab.overview"), uureq -> {
			overviewCtrl = new BadgeDetailsOverviewController(uureq, getWindowControl(), badgeClassKey);
			listenTo(overviewCtrl);
			return overviewCtrl.getInitialComponent();
		});
		
		recipientsTab = tabPane.addTab(ureq, translate("tab.recipients"), uureq -> {
			recipientsCtrl = new BadgeDetailsRecipientsController(uureq, getWindowControl(), badgeClassKey, reSecurity);
			listenTo(recipientsCtrl);
			return recipientsCtrl.getInitialComponent();
		});
	}
	
	private void loadData() {
		BadgeClass badgeClass = openBadgesManager.getBadgeClassByKey(badgeClassKey);

		switch (badgeClass.getStatus()) {
			case preparation -> {
				awardManuallyButton.setVisible(true);
				edit.setVisible(true);
				createNewVersionAndEdit.setVisible(false);
			}
			case active ->  {
				awardManuallyButton.setVisible(true);
				edit.setVisible(false);
				createNewVersionAndEdit.setVisible(true);
			}
			default -> {
				awardManuallyButton.setVisible(false);
				edit.setVisible(false);
				createNewVersionAndEdit.setVisible(false);
			}
		}
		mainVC.contextPut("img", mediaUrl + "/" + badgeClass.getImage());
		mainVC.contextPut("imgAlt", translate("badge.image") + ": " + badgeClass.getNameWithScan());
		mainVC.contextPut("badgeClass", badgeClass);
		mainVC.contextPut("isCourseBadge", badgeClass.getEntry() != null);
		
		RepositoryEntry courseEntry = badgeClass.getEntry();
		if (courseEntry != null) {
			ICourse course = CourseFactory.loadCourse(courseEntry);
			mainVC.contextPut("courseTitle", StringHelper.escapeHtml(course.getCourseTitle()));
			mainVC.contextPut("courseUrl", Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + courseEntry.getKey());
		}
		
		if (overviewCtrl != null) {
			overviewCtrl.loadData();
		}
		if (recipientsCtrl != null) {
			recipientsCtrl.loadData();
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == awardManuallyButton) {
			doAwardManually(ureq);
		} else if (source == edit) {
			doEdit(ureq, false);
		} else if  (source == createNewVersionAndEdit) {
			doCreateNewVersionAndEdit(ureq);
		}
	}

	private void doEdit(UserRequest ureq, boolean newVersion) {
		BadgeClass badgeClass = openBadgesManager.getBadgeClassByKey(badgeClassKey);
		if (!BadgeClass.BadgeClassStatus.preparation.equals(badgeClass.getStatus())) {
			return;
		}
		
		createBadgeClassContext = new CreateBadgeClassWizardContext(badgeClass, reSecurity, getTranslator(),
				newVersion ? Mode.editNewVersion : Mode.edit);

		Step start;
		if (createBadgeClassContext.isEditWithVersion()) {
			start = new CreateBadge01ImageStep(ureq, createBadgeClassContext);
		} else {
			start = new CreateBadge03CriteriaStep(ureq, createBadgeClassContext);
		}

		StepRunnerCallback finish = (innerUreq, innerWControl, innerRunContext) -> {
			BadgeClass updatedBadgeClass = openBadgesManager.updateBadgeClass(createBadgeClassContext.getBadgeClass());
			updateImage(createBadgeClassContext, updatedBadgeClass);
			openBadgesManager.issueBadgeManually(updatedBadgeClass, createBadgeClassContext.getEarners(), getIdentity());
			loadData();
			return StepsMainRunController.DONE_MODIFIED;
		};

		stepsController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate(newVersion ? "form.create.new.badge.version" : "form.edit.badge"), 
				"o_sel_edit_badge_wizard");
		listenTo(stepsController);
		getWindowControl().pushAsModalDialog(stepsController.getInitialComponent());
	}

	private void updateImage(CreateBadgeClassWizardContext createContext, BadgeClass badgeClass) {
		if (!createContext.isEditWithVersion() || !createContext.imageWasSelected()) {
			return;
		}
		if (createContext.updateImage(openBadgesManager, badgeClass, getIdentity())) {
			openBadgesManager.updateBadgeClass(badgeClass);
		}
	}

	private void doCreateNewVersionAndEdit(UserRequest ureq) {
		BadgeClass badgeClass = openBadgesManager.getBadgeClassByKey(badgeClassKey);
		openBadgesManager.createNewBadgeClassVersion(badgeClassKey, getIdentity());
		BadgeClass reloadedBadgeClass = openBadgesManager.getCurrentBadgeClass(badgeClass.getRootId());
		if (reloadedBadgeClass == null) {
			return;
		}
		
		badgeClassKey = reloadedBadgeClass.getKey();
		doEdit(ureq, true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc) {
			cleanUp();
		} else if (source == issueGlobalBadgeCtrl) {
			cmc.deactivate();
			cleanUp();
			if (event == Event.DONE_EVENT) {
				loadData();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if (source == issueCourseBadgeCtrl) {
			cmc.deactivate();
			cleanUp();
			if (event == Event.DONE_EVENT) {
				loadData();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if (source == stepsController) {
			if (event == Event.CANCELLED_EVENT || event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(stepsController);
				loadData();
			}
			if (event == Event.CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		} else if (source == overviewCtrl) {
			if (event == BadgeDetailsOverviewController.SHOW_RECIPIENTS_EVENT) {
				tabPane.setSelectedPane(ureq, recipientsTab);
			}
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(issueGlobalBadgeCtrl);
		removeAsListenerAndDispose(issueCourseBadgeCtrl);
		cmc = null;
		issueGlobalBadgeCtrl = null;
		issueCourseBadgeCtrl = null;
	}

	private void doAwardManually(UserRequest ureq) {
		BadgeClass badgeClass = openBadgesManager.getBadgeClassByKey(badgeClassKey);

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

	public String getName() {
		return overviewCtrl != null ? overviewCtrl.getName() : "";
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
}
