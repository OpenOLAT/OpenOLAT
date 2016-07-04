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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaHandler;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PagePart;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.MediaPart;
import org.olat.modules.portfolio.ui.editor.AddElementInfos;
import org.olat.modules.portfolio.ui.editor.HTMLRawPageElementHandler;
import org.olat.modules.portfolio.ui.editor.InteractiveAddPageElementHandler;
import org.olat.modules.portfolio.ui.editor.PageEditorController;
import org.olat.modules.portfolio.ui.editor.PageEditorProvider;
import org.olat.modules.portfolio.ui.editor.PageElement;
import org.olat.modules.portfolio.ui.editor.PageElementAddController;
import org.olat.modules.portfolio.ui.editor.PageElementHandler;
import org.olat.modules.portfolio.ui.editor.SimpleAddPageElementHandler;
import org.olat.modules.portfolio.ui.event.MediaSelectionEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageEditController extends BasicController {
	
	private Controller editCtrl;
	private PageMetadataController metaCtrl;
	
	private Page page;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public PageEditController(UserRequest ureq, WindowControl wControl, BinderSecurityCallback secCallback, Page page) {
		super(ureq, wControl);
		this.page = page;
		
		metaCtrl = new PageMetadataController(ureq, getWindowControl(), secCallback, page);
		listenTo(metaCtrl);
		
		editCtrl = new PageEditorController(ureq, getWindowControl(), new PortfolioPageEditorProvider());
		//editCtrl = new PageEditorBakController(ureq, getWindowControl(), page);
		listenTo(editCtrl);
		
		VelocityContainer mainVC = createVelocityContainer("page_editor_wrapper");
		mainVC.put("meta", metaCtrl.getInitialComponent());
		mainVC.put("editor", editCtrl.getInitialComponent());
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		fireEvent(ureq, event);
	}
	
	private class PortfolioPageEditorProvider implements PageEditorProvider {
		
		private final List<PageElementHandler> handlers = new ArrayList<>();
		private final List<PageElementHandler> creationHandlers = new ArrayList<>();
		
		public PortfolioPageEditorProvider() {
			List<MediaHandler> mediaHandlers = portfolioService.getMediaHandlers();
			for(MediaHandler mediaHandler:mediaHandlers) {
				if(mediaHandler instanceof PageElementHandler) {
					handlers.add((PageElementHandler)mediaHandler);
					if(mediaHandler instanceof InteractiveAddPageElementHandler
							|| mediaHandler instanceof SimpleAddPageElementHandler) {
						creationHandlers.add((PageElementHandler)mediaHandler);
					}
				}
			}
			
			//add the hook to pick media from the media center
			creationHandlers.add(new OtherArtefactsHandler());
			//handler for HTML code
			HTMLRawPageElementHandler htlmRawHandler = new HTMLRawPageElementHandler();
			handlers.add(htlmRawHandler);
			creationHandlers.add(htlmRawHandler);
		}

		@Override
		public List<? extends PageElement> getElements() {
			return portfolioService.getPageParts(page);
		}

		@Override
		public List<PageElementHandler> getCreateHandlers() {
			return creationHandlers;
		}

		@Override
		public List<PageElementHandler> getAvailableHandlers() {
			return handlers;
		}

		@Override
		public PageElement appendPageElement(PageElement element) {
			PagePart part = null;
			if(element instanceof PagePart) {
				part = portfolioService.appendNewPagePart(page, (PagePart)element);
			}
			return part;
		}

		@Override
		public PageElement appendPageElementAt(PageElement element, int index) {
			PagePart part = null;
			if(element instanceof PagePart) {
				part = portfolioService.appendNewPagePartAt(page, (PagePart)element, index);
			}
			return part;
		}

		@Override
		public void removePageElement(PageElement element) {
			if(element instanceof PagePart) {
				portfolioService.removePagePart(page, (PagePart)element);
			}
		}

		@Override
		public void moveUpPageElement(PageElement element) {
			if(element instanceof PagePart) {
				portfolioService.moveUpPagePart(page, (PagePart)element);
			}
		}

		@Override
		public void moveDownPageElement(PageElement element) {
			if(element instanceof PagePart) {
				portfolioService.moveDownPagePart(page, (PagePart)element);
			}
		}
	}
	
	private static class OtherArtefactsHandler implements PageElementHandler, InteractiveAddPageElementHandler {

		@Override
		public String getType() {
			return "others";
		}

		@Override
		public String getIconCssClass() {
			return "o_icon_others";
		}

		@Override
		public Component getContent(UserRequest ureq, WindowControl wControl, PageElement element) {
			return null;
		}

		@Override
		public Controller getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
			return null;
		}
		
		@Override
		public PageElementAddController getAddPageElementController(UserRequest ureq, WindowControl wControl) {
			return new OtherArtfectsChooserController(ureq, wControl);
		}
	}
	
	private static class OtherArtfectsChooserController extends BasicController implements PageElementAddController {
		
		private MediaPart mediaPart;
		private AddElementInfos userObject;
		private final MediaCenterController mediaListCtrl;
		
		public OtherArtfectsChooserController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl);
			mediaListCtrl = new MediaCenterController(ureq, getWindowControl());
			listenTo(mediaListCtrl);
			putInitialPanel(mediaListCtrl.getInitialComponent());
		}

		@Override
		public PageElement getPageElement() {
			return mediaPart;
		}

		@Autowired
		public AddElementInfos getUserObject() {
			return userObject;
		}

		@Autowired
		public void setUserObject(AddElementInfos userObject) {
			this.userObject = userObject;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			//
		}
		
		@Override
		protected void event(UserRequest ureq, Controller source, Event event) {
			if(event instanceof MediaSelectionEvent) {
				MediaSelectionEvent mse = (MediaSelectionEvent)event;
				if(mse.getMedia() != null) {
					doAddMedia(mse.getMedia());
					fireEvent(ureq, Event.DONE_EVENT);
				}
			}
			super.event(ureq, source, event);
		}
		
		private void doAddMedia(Media media) {
			mediaPart = new MediaPart();
			mediaPart.setMedia(media);
		}

		@Override
		protected void doDispose() {
			//
		}
	}
}
