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

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.AbstractFeedCourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.webFeed.FeedPreviewSecurityCallback;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.ui.FeedUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.ReferencableEntriesSearchController;

/**
 * The abstract feed course node edit controller.
 * 
 * <P>
 * Initial Date: Mar 31, 2009 <br>
 * 
 * @author gwassmann
 */
public abstract class FeedNodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {
	// Constants
	public static final String PANE_TAB_FEED = "pane.tab.feed";
	private static final String PANE_TAB_ACCESS = "pane.tab.access";
	private static final String[] paneKeys = { PANE_TAB_ACCESS, PANE_TAB_FEED };
	// More constants, mainly keys.
	private static final String CHOSEN_FEED_NAME = "chosen_feed_name";
	private static final String NO_FEED_CHOSEN = "no.feed.chosen";
	private static final String SHOW_PREVIEW_LINK = "showPreviewLink";
	private static final String COMMAND_PREVIEW = "command.preview";
	private static final String ERROR_REPOSITORY_ENTRY_MISSING = "error.repository.entry.missing";
	private static final String BUTTON_CHOOSE_FEED = "button.choose.feed";
	private static final String BUTTON_CHANGE_FEED = "button.change.feed";
	private static final String BUTTON_CREATE_FEED = "button.create.feed";
	// Components
	private TabbedPane tabbedPane;
	private Panel learningResource;
	private Link chooseButton, changeButton;
	private Link previewLink;
	private Link editLink;
	private VelocityContainer accessVC, contentVC;
	// Controllers
	private ConditionEditController readerCtr, posterCtr, moderatroCtr;
	private ReferencableEntriesSearchController searchController;
	private CloseableModalController cmc, cmcFeedCtr;
	private Controller feedController;
	// The actual node and its configuration for easy access.
	private ModuleConfiguration config;
	private AbstractFeedCourseNode node;
	private ICourse course;
	private FeedUIFactory uiFactory;
	private String resourceTypeName;

	/**
	 * Constructor. The uiFactory is needed for preview controller and the
	 * resourceTypeName for the repository search.
	 * @param ureq
	 * @param wControl
	 * @param courseNode
	 * @param course
	 * @param uce
	 * @param uiFactory
	 * @param resourceTypeName
	 * @param contentHelpUrl 
	 */
	public FeedNodeEditController(UserRequest ureq, WindowControl wControl, AbstractFeedCourseNode courseNode, ICourse course,
			UserCourseEnvironment uce, FeedUIFactory uiFactory, String resourceTypeName, String contentHelpUrl) {
		super(ureq, wControl);
		this.course = course;
		this.node = courseNode;
		this.config = courseNode.getModuleConfiguration();
		this.uiFactory = uiFactory;
		this.resourceTypeName = resourceTypeName;
		setTranslatorAndFallback(ureq.getLocale());

		this.getClass().getSuperclass();
		// Accessibility tab
		accessVC = new VelocityContainer("accessVC", FeedNodeEditController.class, "access", getTranslator(), this);
		CourseEditorTreeModel editorModel = course.getEditorTreeModel();

		// Moderator precondition
		Condition moderatorCondition = node.getPreConditionModerator();
		moderatroCtr = new ConditionEditController(ureq, getWindowControl(), uce, moderatorCondition,
				AssessmentHelper.getAssessableNodes(editorModel, node));
		this.listenTo(moderatroCtr);
		accessVC.put("moderatorCondition", moderatroCtr.getInitialComponent());

		// Poster precondition
		Condition posterCondition = node.getPreConditionPoster();
		posterCtr = new ConditionEditController(ureq, getWindowControl(), uce, posterCondition, AssessmentHelper
				.getAssessableNodes(editorModel, node));
		this.listenTo(posterCtr);
		accessVC.put("posterCondition", posterCtr.getInitialComponent());

		// Reader precondition
		Condition readerCondition = node.getPreConditionReader();
		readerCtr = new ConditionEditController(ureq, getWindowControl(), uce, readerCondition, AssessmentHelper
				.getAssessableNodes(editorModel, node));
		this.listenTo(readerCtr);
		accessVC.put("readerCondition", readerCtr.getInitialComponent());

		// Podcast tab. Embed the actual podcast learning contentVC into the
		// building block
		learningResource = new Panel("learning_resource_panel");
		contentVC = new VelocityContainer("accessVC", FeedNodeEditController.class, "edit", getTranslator(), this);
		contentVC.contextPut("helpUrl", contentHelpUrl);
		changeButton = LinkFactory.createButtonSmall(BUTTON_CHANGE_FEED, contentVC, this);
		changeButton.setElementCssClass("o_sel_feed_change_repofile");
		chooseButton = LinkFactory.createButtonSmall(BUTTON_CREATE_FEED, contentVC, this);
		chooseButton.setElementCssClass("o_sel_feed_choose_repofile");

		if (config.get(AbstractFeedCourseNode.CONFIG_KEY_REPOSITORY_SOFTKEY) != null) {
			// fetch repository entry to display the repository entry title of the
			// chosen cp
			RepositoryEntry re = courseNode.getReferencedRepositoryEntry();
			if (re == null) {
				// we cannot display the entries name, because the repository entry has
				// been deleted since it was last embeded.
				this.showError(ERROR_REPOSITORY_ENTRY_MISSING);
				contentVC.contextPut(SHOW_PREVIEW_LINK, Boolean.FALSE);
				contentVC.contextPut(CHOSEN_FEED_NAME, translate(NO_FEED_CHOSEN));
			} else {
				// no securitycheck on feeds, editable by everybody
				editLink = LinkFactory.createButtonSmall("edit", contentVC, this);
				contentVC.contextPut(SHOW_PREVIEW_LINK, Boolean.TRUE);
				String displayname = StringHelper.escapeHtml(re.getDisplayname());
				previewLink = LinkFactory.createCustomLink(COMMAND_PREVIEW, COMMAND_PREVIEW, displayname, Link.NONTRANSLATED, contentVC,
						this);
				previewLink.setCustomEnabledLinkCSS("o_preview");
				previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
				previewLink.setTitle(getTranslator().translate(COMMAND_PREVIEW));

			}
		} else {
			// no valid config yet
			contentVC.contextPut(SHOW_PREVIEW_LINK, Boolean.FALSE);
			contentVC.contextPut(CHOSEN_FEED_NAME, translate(NO_FEED_CHOSEN));
		}
		learningResource.setContent(contentVC);
	}

	private void setTranslatorAndFallback(Locale locale) {
		// The implementing class
		Class<? extends FeedNodeEditController> thisClass = this.getClass();
		Translator fallback = Util.createPackageTranslator(thisClass.getSuperclass(), locale);
		Translator translator = Util.createPackageTranslator(thisClass, locale, fallback);
		setTranslator(translator);
	}

	/**
	 * @see org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController#getPaneKeys()
	 */
	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	/**
	 * @see org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController#getTabbedPane()
	 */
	@Override
	public TabbedPane getTabbedPane() {
		return tabbedPane;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		if (cmc != null) {
			cmc.dispose();
			cmc = null;
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == chooseButton || source == changeButton) {
			searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, resourceTypeName, translate(BUTTON_CHOOSE_FEED));
			listenTo(searchController);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent(), true,
					translate(BUTTON_CREATE_FEED));
			cmc.activate();
		} else if (source == previewLink) {
			// Preview as modal dialogue only if the config is valid
			RepositoryEntry re = node.getReferencedRepositoryEntry();
			if (re == null) {
				// The repository entry has been deleted meanwhile.
				showError("error.repoentrymissing");
			} else {
				FeedSecurityCallback callback = new FeedPreviewSecurityCallback();
				feedController = uiFactory.createMainController(re.getOlatResource(), ureq, getWindowControl(), callback, course
						.getResourceableId(), node.getIdent());
				cmcFeedCtr = new CloseableModalController(getWindowControl(), translate("command.close"), feedController.getInitialComponent());
				listenTo(cmcFeedCtr);
				cmcFeedCtr.activate();
			}
			
		} else if (source == editLink) {
			boolean launched = CourseNodeFactory.getInstance().launchReferencedRepoEntryEditor(ureq, getWindowControl(), node);
			if(!launched) {
				RepositoryEntry re = node.getReferencedRepositoryEntry();
				if (re == null) {
					showError("error.repoentrymissing");
				} else {
					showError("error.wrongtype");	
				}
			}
		}
	}

	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == moderatroCtr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = moderatroCtr.getCondition();
				node.setPreConditionModerator(cond);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == posterCtr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = posterCtr.getCondition();
				node.setPreConditionPoster(cond);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == readerCtr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = readerCtr.getCondition();
				node.setPreConditionReader(cond);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == searchController) {
			cmc.deactivate();
			// repository search controller done
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				RepositoryEntry re = searchController.getSelectedEntry();
				if (re != null) {
					config.set(AbstractFeedCourseNode.CONFIG_KEY_REPOSITORY_SOFTKEY, re.getSoftkey());

					contentVC.contextPut("showPreviewLink", Boolean.TRUE);
					String displayname = StringHelper.escapeHtml(re.getDisplayname());
					previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", displayname, Link.NONTRANSLATED,
							contentVC, this);
					previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
					previewLink.setCustomEnabledLinkCSS("o_preview");
					previewLink.setTitle(getTranslator().translate("command.preview"));
					// no securitycheck on feeds, editable by everybody
					editLink = LinkFactory.createButtonSmall("edit", contentVC, this);
					// fire event so the updated config is saved by the
					// editormaincontroller
					fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
				}
			} // else cancelled repo search
		} else if (source == cmcFeedCtr) {
			if (event == CloseableModalController.CLOSE_MODAL_EVENT) {
				cmcFeedCtr.dispose();
				feedController.dispose();
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.tabbable.TabbableController#addTabs(org.olat.core.gui.components.tabbedpane.TabbedPane)
	 */
	@Override
	public void addTabs(TabbedPane tabbedPane) {
		this.tabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_ACCESS), accessVC);
		tabbedPane.addTab(translate(PANE_TAB_FEED), learningResource);
	}

	/**
	 * remove ref to repo entry from the config
	 * 
	 * @param moduleConfig
	 */
	public static void removeReference(ModuleConfiguration moduleConfig) {
		moduleConfig.remove(AbstractFeedCourseNode.CONFIG_KEY_REPOSITORY_SOFTKEY);
	}

	/**
	 * set an repository reference to the feed course node
	 * 
	 * @param re
	 * @param moduleConfiguration
	 */
	public static void setReference(RepositoryEntry re, ModuleConfiguration moduleConfiguration) {
		moduleConfiguration.set(AbstractFeedCourseNode.CONFIG_KEY_REPOSITORY_SOFTKEY, re.getSoftkey());
	}
}
