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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsAndRatingsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.text.TextComponent;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.MediaHandler;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PagePart;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.model.HTMLPart;
import org.olat.modules.portfolio.model.MediaPart;
import org.olat.modules.portfolio.ui.event.PublishEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageController extends BasicController implements TooledController {

	private VelocityContainer mainVC;
	private Link editLink, editMetadataLink;
	protected final TooledStackedPanel stackPanel;
	private List<FragmentWrapper> fragments = new ArrayList<>();
	
	private CloseableModalController cmc;
	private PageMetadataController pageMetaCtrl;
	private PageEditController editCtrl;
	private DialogBoxController confirmPublishCtrl;
	private PageMetadataEditController editMetadataCtrl;
	private UserCommentsAndRatingsController commentsCtrl;
	
	private int counter;
	private Page page;
	private boolean dirtyMarker = false;
	private final BinderSecurityCallback secCallback;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public PageController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BinderSecurityCallback secCallback, Page page) {
		super(ureq, wControl);
		this.page = page;
		this.stackPanel = stackPanel;
		this.secCallback = secCallback;
		
		mainVC = createVelocityContainer("page_content");
		mainVC.contextPut("pageTitle", page.getTitle());
		
		loadMeta(ureq);
		loadModel(ureq);
		stackPanel.addListener(this);

		if(secCallback.canComment(page)) {
			CommentAndRatingSecurityCallback commentSecCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), false, false);
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(Page.class, page.getKey());
			commentsCtrl = new UserCommentsAndRatingsController(ureq, getWindowControl(), ores, null, commentSecCallback, true, false, true);
			listenTo(commentsCtrl);
			mainVC.put("comments", commentsCtrl.getInitialComponent());
		}
		putInitialPanel(mainVC);
	}

	@Override
	public void initTools() {
		if(secCallback.canEditPage(page)) {
			editLink = LinkFactory.createToolLink("edit.page", translate("edit.page"), this);
			editLink.setIconLeftCSS("o_icon o_icon-lg o_icon_new_portfolio");
			stackPanel.addTool(editLink, Align.left);
		}
		
		if(secCallback.canEditMetadataBinder()) {
			editMetadataLink = LinkFactory.createToolLink("edit.page.metadata", translate("edit.page.metadata"), this);
			editMetadataLink.setIconLeftCSS("o_icon o_icon-lg o_icon_new_portfolio");
			stackPanel.addTool(editMetadataLink, Align.left);
		}
	}
	
	private void loadModel(UserRequest ureq) {
		mainVC.contextPut("pageTitle", page.getTitle());
		
		List<PagePart> parts = portfolioService.getPageParts(page);
		List<FragmentWrapper> newFragments = new ArrayList<>(parts.size());
		for(PagePart part:parts) {
			Fragment fragment = createFragment(ureq, part);
			if(fragment != null) {
				String cmpId = "cpt-" + (++counter);
				newFragments.add(new FragmentWrapper(cmpId, fragment.getContent()));
				mainVC.put(cmpId, fragment.getContent());
			}
		}
		fragments = newFragments;
		mainVC.contextPut("fragments", fragments);
		dirtyMarker = false;
	}
	
	private void loadMeta(UserRequest ureq) {
		removeAsListenerAndDispose(pageMetaCtrl);
		
		mainVC.contextPut("pageTitle", page.getTitle());
		pageMetaCtrl = new PageMetadataController(ureq, getWindowControl(), secCallback, page);
		listenTo(pageMetaCtrl);
		mainVC.put("meta", pageMetaCtrl.getInitialComponent());
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(editCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				dirtyMarker = true;
			} else if(event instanceof PublishEvent) {
				doConfirmPublish(ureq);
			} else {
				stackPanel.popUpToController(this);
				loadModel(ureq);
			}
		} else if(editMetadataCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadMeta(ureq);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(pageMetaCtrl == source) {
			if(event instanceof PublishEvent) {
				doConfirmPublish(ureq);
			}
		} else if(confirmPublishCtrl == source) {
			doPublish(ureq);
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editMetadataCtrl);
		removeAsListenerAndDispose(cmc);
		editMetadataCtrl = null;
		cmc = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(editLink == source) {
			doEditPage(ureq);
		} else if(editMetadataLink == source) {
			doEditMetadata(ureq);
		} else if(stackPanel == source) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				if(pe.getController() == editCtrl && dirtyMarker) {
					loadModel(ureq);
				}
			}
		}
	}
	
	private void doConfirmPublish(UserRequest ureq) {
		String title = translate("publish.confirm.title");
		String text = translate("publish.confirm.descr", new String[]{ page.getTitle() });
		confirmPublishCtrl = activateYesNoDialog(ureq, title, text, confirmPublishCtrl);
	}
	
	private void doPublish(UserRequest ureq) {
		page = portfolioService.changePageStatus(page, PageStatus.published);
		stackPanel.popUpToController(this);
		loadMeta(ureq);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doEditMetadata(UserRequest ureq) {
		if(editMetadataCtrl != null) return;
		
		removeAsListenerAndDispose(editMetadataCtrl);
		
		Binder binder = null;
		Section section = null;
		if(page.getSection() != null) {
			section = page.getSection();
			binder = portfolioService.getBinderBySection(section);
		}
		
		editMetadataCtrl = new PageMetadataEditController(ureq, getWindowControl(), binder, true, section, true, page);
		listenTo(editMetadataCtrl);
		
		String title = translate("edit.page.metadata");
		cmc = new CloseableModalController(getWindowControl(), null, editMetadataCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditPage(UserRequest ureq) {
		removeAsListenerAndDispose(editCtrl);

		editCtrl = new PageEditController(ureq, getWindowControl(), secCallback, page);
		listenTo(editCtrl);
		
		stackPanel.pushController("Edit", editCtrl);
	}
	
	private Fragment createFragment(UserRequest ureq, PagePart part) {
		if(part instanceof HTMLPart) {
			HTMLPart htmlPart = (HTMLPart)part;
			HTMLFragment editorFragment = new HTMLFragment(htmlPart);
			return editorFragment;
		} else if(part instanceof MediaPart) {
			MediaPart htmlPart = (MediaPart)part;
			MediaFragment editorFragment = new MediaFragment(htmlPart, ureq);
			return editorFragment;
		}
		return null;
	}
	
	public class MediaFragment implements Fragment {
		
		private Controller controller;
		
		public MediaFragment(MediaPart part, UserRequest ureq) {
			MediaHandler handler = portfolioService.getMediaHandler(part.getMedia().getType());
			controller = handler.getMediaController(ureq, getWindowControl(), part.getMedia());
		}
		
		@Override
		public Component getContent() {
			return controller.getInitialComponent();
		}

	}
	
	public static class HTMLFragment implements Fragment {
		
		private final TextComponent component;
		
		public HTMLFragment(HTMLPart part) {
			component = TextFactory.createTextComponentFromString("cmp" + CodeHelper.getRAMUniqueID(), part.getContent(), null, false, null);
		}

		@Override
		public Component getContent() {
			return component;
		}
	}
	
	public interface Fragment {
		
		public Component getContent();
		
	}
	
	public static final class FragmentWrapper {
		
		private final String componentName;
		private final Component component;
		
		public FragmentWrapper(String componentName, Component component) {
			this.componentName = componentName;
			this.component = component;
		}
		
		public String getComponentName() {
			return componentName;
		}
		
		public Component getComponent() {
			return component;
		}
	}
}
