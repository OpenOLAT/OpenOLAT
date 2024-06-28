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
package org.olat.modules.ceditor.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.ceditor.InteractiveAddPageElementHandler;
import org.olat.modules.ceditor.PageEditorProvider;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementHandler;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.ui.component.ContentEditorFragment;
import org.olat.modules.ceditor.ui.event.AddElementEvent;
import org.olat.modules.ceditor.ui.event.CloseElementsEvent;
import org.olat.modules.ceditor.ui.model.EditorFragment;

/**
 * 
 * Initial date: 04.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AddElementsController extends BasicController {
	
	private final int containerColumn;
	private final PageElementTarget target;
	private final EditorFragment referenceFragment;
	private final ContentEditorFragment referenceComponent;
	private Link closeButton;
	private Link mediaCenterButton;
	private PageElementHandler mediaCenterHandler;

	public AddElementsController(UserRequest ureq, WindowControl wControl, PageEditorProvider provider,
			PageElementTarget target, Translator fallbackTranslator) {
		super(ureq, wControl, fallbackTranslator);
		this.target = target;
		containerColumn = -1;
		referenceFragment = null;
		referenceComponent = null;
		initContainer(provider);
	}
	
	public AddElementsController(UserRequest ureq, WindowControl wControl, PageEditorProvider provider,
			EditorFragment referenceFragment, PageElementTarget target, Translator fallbackTranslator) {
		super(ureq, wControl, fallbackTranslator);
		this.target = target;
		containerColumn = -1;
		referenceComponent = null;
		this.referenceFragment = referenceFragment;
		initContainer(provider);
	}
	
	public AddElementsController(UserRequest ureq, WindowControl wControl, PageEditorProvider provider,
			EditorFragment referenceFragment, PageElementTarget target, int containerColumn, Translator fallbackTranslator) {
		super(ureq, wControl, fallbackTranslator);
		this.target = target;
		referenceComponent = null;
		this.containerColumn = containerColumn;
		this.referenceFragment = referenceFragment;
		initContainer(provider);
	}
	
	public AddElementsController(UserRequest ureq, WindowControl wControl, PageEditorProvider provider,
			ContentEditorFragment referenceComponent, PageElementTarget target, Translator fallbackTranslator) {
		super(ureq, wControl, fallbackTranslator);
		this.target = target;
		containerColumn = -1;
		referenceFragment = null;
		this.referenceComponent = referenceComponent;
		initContainer(provider);
	}
	
	public AddElementsController(UserRequest ureq, WindowControl wControl, PageEditorProvider provider,
			ContentEditorFragment referenceComponent, PageElementTarget target, int containerColumn, Translator fallbackTranslator) {
		super(ureq, wControl, fallbackTranslator);
		this.target = target;
		referenceFragment = null;
		this.containerColumn = containerColumn;
		this.referenceComponent = referenceComponent;
		initContainer(provider);
	}
	
	private void initContainer(PageEditorProvider provider) {
		VelocityContainer mainVC = createVelocityContainer("add_elements");
		
		List<CategoryWrapper> categoryWrappers = new ArrayList<>();

		for (PageElementCategory pageElementCategory : PageElementCategory.values()) {
			getCategoryWrapper(categoryWrappers, pageElementCategory);
		}

		mediaCenterHandler = null;
		for(PageElementHandler handler:provider.getCreateHandlers()) {
			if(handler instanceof InteractiveAddPageElementHandler || handler instanceof SimpleAddPageElementHandler) {
				CategoryWrapper categoryWrapper = getCategoryWrapper(categoryWrappers, handler.getCategory());

				if ("others".equals(handler.getType())) {
					mediaCenterHandler = handler;
					continue;
				}

				String id = "add.el." + handler.getType();
				Link addLink = LinkFactory.createLink(id, "add." + handler.getType(), "add.elements", mainVC, this);
				addLink.setIconLeftCSS("o_icon o_icon-fw " + handler.getIconCssClass());
				addLink.setUserObject(handler);
				addLink.setTitle("add." + handler.getType());
				mainVC.put(id, addLink);
				
				categoryWrapper.getLinkIds().add(id);
			}
		}

		categoryWrappers = categoryWrappers.stream().filter(cw -> !cw.linkIds.isEmpty()).toList();

		mainVC.contextPut("categories", categoryWrappers);

		if (mediaCenterHandler != null) {
			mediaCenterButton = LinkFactory.createButton("media.center", mainVC, this);
			mediaCenterButton.setIconLeftCSS("o_icon o_icon_image");
		}
		closeButton = LinkFactory.createButton("close", mainVC, this);

		putInitialPanel(mainVC);
	}

	private CategoryWrapper getCategoryWrapper(List<CategoryWrapper> catgoryWrappers, PageElementCategory category) {
		for (CategoryWrapper categoryWrapper : catgoryWrappers) {
			if (categoryWrapper.getCategory().equals(category)) {
				return categoryWrapper;
			}
		}
		CategoryWrapper categoryWrapper = new CategoryWrapper(category, translate(category.getI18nKey()));
		catgoryWrappers.add(categoryWrapper);
		return categoryWrapper;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link link) {
			if("add.elements".equals(link.getCommand())) {
				PageElementHandler handler = (PageElementHandler)link.getUserObject();
				fireEvent(ureq, new AddElementEvent(referenceFragment, referenceComponent, handler, target, containerColumn));
			} else if(link == closeButton) {
				fireEvent(ureq, new CloseElementsEvent());
			} else if(link == mediaCenterButton) {
				fireEvent(ureq, new AddElementEvent(referenceFragment, referenceComponent, mediaCenterHandler, target, containerColumn));
			}
		}
	}
	
	public static final class CategoryWrapper {
		
		private final PageElementCategory category;
		private final String name;
		private final List<String> linkIds = new ArrayList<>();
		
		public CategoryWrapper(PageElementCategory category, String name) {
			this.category = category;
			this.name = name;
		}

		public PageElementCategory getCategory() {
			return category;
		}

		public String getName() {
			return name;
		}

		public List<String> getLinkIds() {
			return linkIds;
		}
		
	}
}
