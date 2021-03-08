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
package org.olat.course.nodes.feed;

import static org.olat.core.gui.translator.TranslatorHelper.translateAll;

import java.util.Collection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.AbstractFeedCourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.FOCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.webFeed.FeedPreviewSecurityCallback;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.ui.FeedUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.ReferencableEntriesSearchController;

/**
 * 
 * Initial date: 10 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FeedNodeConfigController extends FormBasicController {
	
	private static final String MODERATOR_COACH = "edit.moderator.coach";
	private static final String[] MODERATOR_KEYS = new String[] { MODERATOR_COACH };
	private static final String POSTER_COACH = "edit.poster.coach";
	private static final String POSTER_PARTICIPANT = "edit.poster.participant";
	private static final String[] POSTER_KEYS = new String[] {
			POSTER_COACH,
			POSTER_PARTICIPANT
	};

	private StaticTextElement feedNotChoosenEl;
	private FormLink previewLink;
	private FormLink chooseLink;
	private FormLink replaceLink;
	private FormLink editLink;
	private MultipleSelectionElement moderatorRolesEl;
	private MultipleSelectionElement posterRolesEl;
	
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController repositorySearchCtrl;
	private Controller feedCtrl;

	private final BreadcrumbPanel stackPanel;
	private final ICourse course;
	private final AbstractFeedCourseNode courseNode;
	private final ModuleConfiguration moduleConfig;
	private final FeedUIFactory uiFactory;
	private final String resourceTypeName;
	private final String helpUrl;
	private RepositoryEntry feedEntry;
	
	public FeedNodeConfigController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, String translatorPackage,
			ICourse course, AbstractFeedCourseNode courseNode, FeedUIFactory uiFactory, String resourceTypeName, String helpUrl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(translatorPackage, getLocale(), getTranslator()));
		this.stackPanel = stackPanel;
		this.course = course;
		this.courseNode = courseNode;
		this.moduleConfig = courseNode.getModuleConfiguration();
		this.uiFactory = uiFactory;
		this.resourceTypeName = resourceTypeName;
		this.feedEntry = courseNode.getReferencedRepositoryEntry();
		this.helpUrl = helpUrl;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer resourceCont = FormLayoutContainer.createDefaultFormLayout("resource", getTranslator());
		formLayout.add(resourceCont);
		resourceCont.setRootForm(mainForm);
		resourceCont.setFormTitle(translate("form.title.choose.feed"));
		resourceCont.setFormContextHelp(helpUrl);
		
		feedNotChoosenEl = uifactory.addStaticTextElement("no.feed.chosen", "chosen.feed",
				translate("no.feed.chosen"), resourceCont);
		previewLink = uifactory.addFormLink("chosen.feed", "", translate("chosen.feed"), resourceCont,
				Link.NONTRANSLATED);
		previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		resourceCont.add(buttonsCont);
		chooseLink = uifactory.addFormLink("button.create.feed", buttonsCont, "btn btn-default o_xsmall");
		chooseLink.setElementCssClass("o_sel_feed_choose_repofile");
		replaceLink = uifactory.addFormLink("button.change.feed", buttonsCont, "btn btn-default o_xsmall");
		editLink = uifactory.addFormLink("edit", buttonsCont, "btn btn-default o_xsmall");
		
		if (!courseNode.hasCustomPreConditions()) {
			FormLayoutContainer rightsCont = FormLayoutContainer.createDefaultFormLayout("rights", getTranslator());
			formLayout.add(rightsCont);
			rightsCont.setFormTitle(translate("user.rights"));
			
			moderatorRolesEl = uifactory.addCheckboxesVertical("edit.moderator", rightsCont, MODERATOR_KEYS,
					translateAll(getTranslator(), MODERATOR_KEYS), 1);
			moderatorRolesEl.select(MODERATOR_COACH, moduleConfig.getBooleanSafe(FOCourseNode.CONFIG_COACH_MODERATE_ALLOWED));
			moderatorRolesEl.addActionListener(FormEvent.ONCHANGE);
			
			posterRolesEl = uifactory.addCheckboxesVertical("edit.poster", rightsCont, POSTER_KEYS,
					translateAll(getTranslator(), POSTER_KEYS), 1);
			posterRolesEl.select(POSTER_COACH, moduleConfig.getBooleanSafe(FOCourseNode.CONFIG_COACH_POST_ALLOWED));
			posterRolesEl.select(POSTER_PARTICIPANT,
					moduleConfig.getBooleanSafe(FOCourseNode.CONFIG_PARTICIPANT_POST_ALLOWED));
			posterRolesEl.addActionListener(FormEvent.ONCHANGE);
		}
		
		updateUI();
	}
	
	private void updateUI() {
		boolean feedSelected = feedEntry != null;
		if (feedSelected) {
			String displayname = StringHelper.escapeHtml(feedEntry.getDisplayname());
			previewLink.setI18nKey(displayname);
			flc.setDirty(true);
		}
		feedNotChoosenEl.setVisible(!feedSelected);
		chooseLink.setVisible(!feedSelected);
		previewLink.setVisible(feedSelected);
		replaceLink.setVisible(feedSelected);
		editLink.setVisible(feedSelected);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == chooseLink || source == replaceLink) {
			doSelectFeed(ureq);
		} else if (source == previewLink) {
			doPreviewFeed(ureq);
		} else if (source == editLink) {
			doEditFeed(ureq);
		} else if (source == moderatorRolesEl) {
			doUpdateModeratorRoles(ureq);
		} else if (source == posterRolesEl) {
			doUpdatePosterRoles(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == repositorySearchCtrl) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				feedEntry = repositorySearchCtrl.getSelectedEntry();
				if (feedEntry != null) {
					AbstractFeedCourseNode.setReference(courseNode.getModuleConfiguration(), feedEntry);
					fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
					updateUI();
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cmc.deactivate();
			cleanUp();
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(repositorySearchCtrl);
		removeAsListenerAndDispose(feedCtrl);
		removeAsListenerAndDispose(cmc);
		repositorySearchCtrl = null;
		feedCtrl = null;
		cmc = null;
	}
	
	private void doSelectFeed(UserRequest ureq) {
		repositorySearchCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq, resourceTypeName,
				translate("button.choose.feed"));
		listenTo(repositorySearchCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				repositorySearchCtrl.getInitialComponent(), true, translate("button.create.feed"));
		cmc.activate();
	}

	private void doPreviewFeed(UserRequest ureq) {
		if (feedEntry == null) {
			showError("error.repoentrymissing");
		} else {
			removeAsListenerAndDispose(feedCtrl);
			FeedSecurityCallback callback = new FeedPreviewSecurityCallback();
			feedCtrl = uiFactory.createMainController(feedEntry.getOlatResource(), ureq, getWindowControl(), callback, course
					.getResourceableId(), courseNode.getIdent());
			listenTo(feedCtrl);
			stackPanel.pushController(translate("preview"), feedCtrl);
		}
	}

	private void doEditFeed(UserRequest ureq) {
		if (feedEntry == null) {
			showError("error.repoentrymissing");
		} else {
			boolean launched = CourseNodeFactory.getInstance().launchReferencedRepoEntryEditor(ureq, getWindowControl(), courseNode);
			if(!launched) {
				showError("error.wrongtype");
			}
		}
	}

	private void doUpdateModeratorRoles(UserRequest ureq) {
		Collection<String> selectedKeys = moderatorRolesEl.getSelectedKeys();
		moduleConfig.setBooleanEntry(FOCourseNode.CONFIG_COACH_MODERATE_ALLOWED, selectedKeys.contains(MODERATOR_COACH));
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	private void doUpdatePosterRoles(UserRequest ureq) {
		Collection<String> selectedKeys = posterRolesEl.getSelectedKeys();
		moduleConfig.setBooleanEntry(FOCourseNode.CONFIG_COACH_POST_ALLOWED, selectedKeys.contains(POSTER_COACH));
		moduleConfig.setBooleanEntry(FOCourseNode.CONFIG_PARTICIPANT_POST_ALLOWED, selectedKeys.contains(POSTER_PARTICIPANT));
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
