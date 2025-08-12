/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.feed;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.editor.CourseNodeReferenceProvider;
import org.olat.course.editor.NodeEditController;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.noderight.NodeRightType;
import org.olat.course.noderight.ui.NodeRightsController;
import org.olat.course.nodes.AbstractFeedCourseNode;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.FeedPreviewSecurityCallback;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.modules.webFeed.ui.FeedMainController;
import org.olat.modules.webFeed.ui.FeedMetadataConfigController;
import org.olat.modules.webFeed.ui.FeedUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryReferenceController;
import org.olat.repository.ui.RepositoryEntryReferenceProvider.ReferenceContentProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 26 Feb 2021<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class FeedNodeConfigsController extends BasicController implements ReferenceContentProvider {

	private final String resourceTypeName;

	private final IconPanelLabelTextContent iconPanelContent;
	private final BreadcrumbPanel stackPanel;
	private final FeedUIFactory feedUIFactory;
	private final ICourse course;
	private final AbstractFeedCourseNode feedCourseNode;
	private final VelocityContainer mainVC;

	private RepositoryEntryReferenceController referenceCtrl;
	private Controller nodeRightCtrl;
	private FeedMetadataConfigController metadataCtrl;

	@Autowired
	protected FeedManager feedManager;
	@Autowired
	private RepositoryService repositoryService;

	public FeedNodeConfigsController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, String translatorPackage,
									 ICourse course, AbstractFeedCourseNode feedCourseNode, FeedUIFactory uiFactory, String resourceTypeName,
									 String helpUrl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(translatorPackage, getLocale(), getTranslator()));
		this.feedCourseNode = feedCourseNode;
		this.stackPanel = stackPanel;
		this.feedUIFactory = uiFactory;
		this.course = course;
		this.resourceTypeName = resourceTypeName;

		mainVC = createVelocityContainer("configs");
		mainVC.contextPut("helpUrl", helpUrl);

		iconPanelContent = new IconPanelLabelTextContent("content");
		initForm(ureq);
		putInitialPanel(mainVC);
	}

	protected void initForm(UserRequest ureq) {
		// empty state configurations for blog and podcast
		EmptyStateConfig emptyStateConfig;
		if (Objects.equals(resourceTypeName, BlogFileResource.TYPE_NAME)) {
			emptyStateConfig = EmptyStateConfig.builder()
					.withMessageTranslated(translate("no.feed.resource.selected"))
					.withDescTranslated(translate("no.feed.resource.selected.text"))
					.withIconCss("o_icon o_blog_icon")
					.build();
		} else {
			emptyStateConfig = EmptyStateConfig.builder()
					.withMessageTranslated(translate("no.feed.resource.selected"))
					.withDescTranslated(translate("no.feed.resource.selected.text"))
					.withIconCss("o_icon o_podcast_icon")
					.build();
		}

		// CourseNodeReferenceProvider for handling references
		RepositoryEntry refRepoEntry = feedCourseNode.getReferencedRepositoryEntry();
		String selectionTitle = translate("button.create.feed");
		CourseNodeReferenceProvider referenceProvider = new CourseNodeReferenceProvider(repositoryService,
				List.of(resourceTypeName), emptyStateConfig, selectionTitle, this);
		referenceCtrl = new RepositoryEntryReferenceController(ureq, getWindowControl(), refRepoEntry, referenceProvider);
		listenTo(referenceCtrl);
		mainVC.put("reference", referenceCtrl.getInitialComponent());

		initUserRights(ureq, refRepoEntry);
		initMetaOptions(ureq);

		// if resource already exists, update UI (iconPanelContent) with labels
		if (refRepoEntry != null) {
			OLATResourceable feedResource = refRepoEntry.getOlatResource();
			updateReferenceContentUI(feedResource);
		}
	}

	private void initUserRights(UserRequest ureq, RepositoryEntry feedEntry) {
		// clean up
		removeAsListenerAndDispose(nodeRightCtrl);
		nodeRightCtrl = null;

		// user rights
		if (!feedCourseNode.hasCustomPreConditions()) {
			List<NodeRightType> nodeRightTypes = new ArrayList<>(AbstractFeedCourseNode.NODE_RIGHT_TYPES);
			// remove post/create items right configuration if the feed is external
			if (feedEntry != null) {
				Feed feed = feedManager.loadFeed(feedEntry.getOlatResource());
				if (feed != null && feed.isExternal()) {
					nodeRightTypes.remove(AbstractFeedCourseNode.NODE_RIGHT_TYPES.stream().filter(r -> r.getIdentifier().equals("post")).findFirst().orElse(null));
				}
			} else {
				nodeRightTypes.remove(AbstractFeedCourseNode.NODE_RIGHT_TYPES.stream().filter(r -> r.getIdentifier().equals("post")).findFirst().orElse(null));
			}

			CourseGroupManager courseGroupManager = course.getCourseEnvironment().getCourseGroupManager();
			nodeRightCtrl = new NodeRightsController(ureq, getWindowControl(), courseGroupManager,
					nodeRightTypes, feedCourseNode.getModuleConfiguration(), null);
			listenTo(nodeRightCtrl);
			mainVC.put("rights", nodeRightCtrl.getInitialComponent());
		}
	}

	private void initMetaOptions(UserRequest ureq) {
		// clean up
		removeAsListenerAndDispose(metadataCtrl);
		metadataCtrl = null;

		metadataCtrl = new FeedMetadataConfigController(ureq, getWindowControl(), feedCourseNode.getModuleConfiguration());
		listenTo(metadataCtrl);
		mainVC.put("metadataConf", metadataCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == nodeRightCtrl) {
			fireEvent(ureq, event);
		} else if (source == referenceCtrl) {
			if (event == RepositoryEntryReferenceController.SELECTION_EVENT) {
				RepositoryEntry newEntry = referenceCtrl.getRepositoryEntry();
				doChangeResource(ureq, newEntry);
				initUserRights(ureq, newEntry);
			} else if (event == RepositoryEntryReferenceController.PREVIEW_EVENT) {
				doPreview(ureq);
			}
		} else if (source == metadataCtrl) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	private void doPreview(UserRequest ureq) {
		RepositoryEntry repositoryEntry = referenceCtrl.getRepositoryEntry();

		if (repositoryEntry != null) {
			FeedSecurityCallback callback = new FeedPreviewSecurityCallback();
			FeedMainController mainController = feedUIFactory.createMainController(repositoryEntry.getOlatResource(), ureq, getWindowControl(),
					callback, course.getResourceableId(), feedCourseNode.getIdent(), feedCourseNode.getModuleConfiguration());
			listenTo(mainController);
			stackPanel.pushController(translate("preview"), mainController);
		} else {
			// should only be the case, if someone else deleted the entry simultaneously
			showError("error.repoentrymissing");
		}
	}

	private void doChangeResource(UserRequest ureq, RepositoryEntry newFeed) {
		if (newFeed == null) {
			return;
		}
		AbstractFeedCourseNode.setReference(feedCourseNode.getModuleConfiguration(), newFeed);
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		updateReferenceContentUI(newFeed.getOlatResource());
	}

	private void updateReferenceContentUI(OLATResourceable feedResourceable) {
		List<IconPanelLabelTextContent.LabelText> labelTexts = new ArrayList<>();
		Feed feed = feedManager.loadFeed(feedResourceable);

		Translator packageTranslator = Util.createPackageTranslator(FeedMainController.class, getLocale());

		if (feed != null) {
			if (feed.isExternal()) {
				labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("feed.url"), feed.getExternalFeedUrl()));
			} else {
				labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("feed.entries"), String.valueOf(feedManager.loadItems(feed).size())));
			}
			labelTexts.add(new IconPanelLabelTextContent.LabelText(packageTranslator.translate("feed.rate.entries.toggle"), feed.getCanRate() ? translate("on") : translate("off")));
			labelTexts.add(new IconPanelLabelTextContent.LabelText(packageTranslator.translate("feed.comment.entries.toggle"), feed.getCanComment() ? translate("on") : translate("off")));
		}

		iconPanelContent.setLabelTexts(labelTexts);
	}

	@Override
	public Component getContent(RepositoryEntry repositoryEntry) {
		return iconPanelContent;
	}

	@Override
	public void refresh(Component cmp, RepositoryEntry repositoryEntry) {
		// Refresh is handled on change event.
	}
}
