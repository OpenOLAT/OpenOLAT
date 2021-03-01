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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
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
	
	private StaticTextElement feedNotChoosenEl;
	private FormLink previewLink;
	private FormLink chooseLink;
	private FormLink replaceLink;
	private FormLink editLink;
	
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController repositorySearchCtrl;
	private Controller feedCtrl;

	private final BreadcrumbPanel stackPanel;
	private final ICourse course;
	private final AbstractFeedCourseNode courseNode;
	private final FeedUIFactory uiFactory;
	private final String resourceTypeName;
	private final String helpUrl;
	private RepositoryEntry feedEntry;
	
	public FeedNodeConfigController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, String translatorPackage,
			ICourse course, AbstractFeedCourseNode courseNode, FeedUIFactory uiFactory, String resourceTypeName, String helpUrl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(translatorPackage, getLocale(), getTranslator()));
		this.stackPanel = stackPanel;
		this.course = course;
		this.courseNode = courseNode;
		this.uiFactory = uiFactory;
		this.resourceTypeName = resourceTypeName;
		this.feedEntry = courseNode.getReferencedRepositoryEntry();
		this.helpUrl = helpUrl;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("form.title.choose.feed");
		setFormContextHelp(helpUrl);
		
		feedNotChoosenEl = uifactory.addStaticTextElement("no.feed.chosen", "chosen.feed",
				translate("no.feed.chosen"), formLayout);
		previewLink = uifactory.addFormLink("chosen.feed", "", translate("chosen.feed"), formLayout,
				Link.NONTRANSLATED);
		previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		chooseLink = uifactory.addFormLink("button.create.feed", buttonsCont, "btn btn-default o_xsmall");
		chooseLink.setElementCssClass("o_sel_feed_choose_repofile");
		replaceLink = uifactory.addFormLink("button.change.feed", buttonsCont, "btn btn-default o_xsmall");
		editLink = uifactory.addFormLink("edit", buttonsCont, "btn btn-default o_xsmall");
		
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

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
