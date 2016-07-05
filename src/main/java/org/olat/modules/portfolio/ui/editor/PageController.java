package org.olat.modules.portfolio.ui.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Initial date: 05.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageController extends BasicController {
	
	private List<PageFragment> fragments = new ArrayList<>();
	
	private int counter;
	private final PageProvider provider;
	private final VelocityContainer mainVC;
	
	private Map<String,PageElementHandler> handlerMap = new HashMap<>();
	
	public PageController(UserRequest ureq, WindowControl wControl, PageProvider provider) {
		super(ureq, wControl);
		this.provider = provider;
		
		for(PageElementHandler handler:provider.getAvailableHandlers()) {
			handlerMap.put(handler.getType(), handler);
		}
		mainVC = createVelocityContainer("page_run");
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public void loadElements(UserRequest ureq) {
		List<? extends PageElement> elements = provider.getElements();
		List<PageFragment> newFragments = new ArrayList<>(elements.size());
		for(PageElement element:elements) {
			PageElementHandler handler = handlerMap.get(element.getType());
			if(handler != null) {
				Component cmp = handler.getContent(ureq, getWindowControl(), element);
				String cmpId = "cpt-" + (++counter);
				newFragments.add(new PageFragment(cmpId, cmp));
				mainVC.put(cmpId, cmp);
			}
		}
		fragments = newFragments;
		mainVC.contextPut("fragments", fragments);
	}
	
	public static final class PageFragment {
		
		private final String componentName;
		private final Component component;
		
		public PageFragment(String componentName, Component component) {
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
