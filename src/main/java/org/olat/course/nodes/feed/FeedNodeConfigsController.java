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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
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
import org.olat.modules.webFeed.ui.FeedUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryReferenceController;
import org.olat.repository.ui.RepositoryEntryReferenceProvider.ReferenceContentProvider;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 26 Feb 2021<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class FeedNodeConfigsController extends FormBasicController implements ReferenceContentProvider {

	private final String resourceTypeName;

	private final IconPanelLabelTextContent iconPanelContent;
	private final BreadcrumbPanel stackPanel;
	private final FeedUIFactory feedUIFactory;
	private final ICourse course;
	private final AbstractFeedCourseNode feedCourseNode;

	private RepositoryEntryReferenceController referenceCtrl;
	private Controller nodeRightCtrl;

	@Autowired
	protected FeedManager feedManager;
	@Autowired
	private RepositoryService repositoryService;

	public FeedNodeConfigsController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, String translatorPackage,
									 ICourse course, AbstractFeedCourseNode feedCourseNode, FeedUIFactory uiFactory, String resourceTypeName,
									 String helpUrl) {
		super(ureq, wControl, "configs");
		setTranslator(Util.createPackageTranslator(translatorPackage, getLocale(), getTranslator()));
		this.feedCourseNode = feedCourseNode;
		this.stackPanel = stackPanel;
		this.feedUIFactory = uiFactory;
		this.course = course;
		this.resourceTypeName = resourceTypeName;

		flc.contextPut("helpUrl", helpUrl);

		iconPanelContent = new IconPanelLabelTextContent("content");
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
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
		String selectionTitle = translate("button.create.feed");
		CourseNodeReferenceProvider referenceProvider = new CourseNodeReferenceProvider(repositoryService,
				List.of(resourceTypeName), emptyStateConfig, selectionTitle, this);
		referenceCtrl = new RepositoryEntryReferenceController(ureq, getWindowControl(), feedCourseNode.getReferencedRepositoryEntry(), referenceProvider);
		listenTo(referenceCtrl);
		flc.put("reference", referenceCtrl.getInitialComponent());

		// user rights
		OLATResource feedResource = null;
		if (feedCourseNode.getReferencedRepositoryEntry() != null) {
			feedResource = feedCourseNode.getReferencedRepositoryEntry().getOlatResource();
		}
		if (!feedCourseNode.hasCustomPreConditions()) {
			List<NodeRightType> nodeRightTypes = new ArrayList<>(AbstractFeedCourseNode.NODE_RIGHT_TYPES);
			// remove post/create items right configuration if the feed is external
			if (feedResource != null) {
				Feed feed = feedManager.loadFeed(feedResource);
				if (feed.isExternal()) {
					nodeRightTypes.remove(AbstractFeedCourseNode.NODE_RIGHT_TYPES.stream().filter(r -> r.getIdentifier().equals("post")).findFirst().orElse(null));
				}
			}

			CourseGroupManager courseGroupManager = course.getCourseEnvironment().getCourseGroupManager();
			nodeRightCtrl = new NodeRightsController(ureq, getWindowControl(), courseGroupManager,
					nodeRightTypes, feedCourseNode.getModuleConfiguration(), null);
			listenTo(nodeRightCtrl);
			flc.put("rights", nodeRightCtrl.getInitialComponent());
		}

		// if resource already exists, update UI (iconPanelContent) with labels
		if (feedCourseNode.getReferencedRepositoryEntry() != null) {
			updateReferenceContentUI(feedResource);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// no need
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == nodeRightCtrl) {
			fireEvent(ureq, event);
		} else if (source == referenceCtrl) {
			if (event == RepositoryEntryReferenceController.SELECTION_EVENT) {
				RepositoryEntry newEntry = referenceCtrl.getRepositoryEntry();
				doChangeResource(ureq, newEntry);
			} else if (event == RepositoryEntryReferenceController.PREVIEW_EVENT) {
				doPreview(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	private void doPreview(UserRequest ureq) {
		RepositoryEntry repositoryEntry = referenceCtrl.getRepositoryEntry();

		if (repositoryEntry != null) {
			FeedSecurityCallback callback = new FeedPreviewSecurityCallback();
			FeedMainController mainController = feedUIFactory.createMainController(repositoryEntry.getOlatResource(), ureq, getWindowControl(),
					callback, course.getResourceableId(), feedCourseNode.getIdent());
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

		if (feed != null) {
			if (feed.isExternal()) {
				labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("feed.url"), feed.getExternalFeedUrl()));
			} else {
				labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("feed.entries"), String.valueOf(feedManager.loadItems(feed).size())));
			}
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
