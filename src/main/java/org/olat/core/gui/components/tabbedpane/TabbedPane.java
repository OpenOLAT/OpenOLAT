/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.gui.components.tabbedpane;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.Container;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;

/**
 * enclosing_type Description: <br>
 * 
 * @author Felix Jost
 */
public class TabbedPane extends Container implements Activateable2 {
	private static final Logger log = Tracing.createLoggerFor(TabbedPane.class);
	private static final ComponentRenderer RENDERER = new TabbedPaneRenderer();

	/**
	 * Comment for <code>PARAM_PANE_ID</code>
	 */
	protected static final String PARAM_PANE_ID = "taid";

	private int selectedPane = -1;
	private boolean dirtyCheck = true;
	private boolean hideDisabledTab = false;
	private final List<TabPane> tabPanes = new ArrayList<>(5);
	private Translator compTrans;
	
	/**
	 * @param name
	 */
	public TabbedPane(String name, Locale locale) {
		super(name);
		compTrans = Util.createPackageTranslator(this.getClass(), locale);		
		setDomReplacementWrapperRequired(false);// we provide our own DOM replacement ID
	}

	public boolean isHideDisabledTab() {
		return hideDisabledTab;
	}

	public void setHideDisabledTab(boolean hideDisabledTab) {
		this.hideDisabledTab = hideDisabledTab;
	}

	/**
	 * @return false suppress the dirty check while switching tabs (default is true)
	 */
	public boolean isDirtyCheck() {
		return dirtyCheck;
	}

	/**
	 * @param dirtyCheck false to suppress the dirty check while switching of tabs
	 */
	public void setDirtyCheck(boolean dirtyCheck) {
		this.dirtyCheck = dirtyCheck;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		// the taid indicates which tab the user clicked
		String taid = ureq.getParameter(PARAM_PANE_ID);
		try {
			int newTaid = Integer.parseInt(taid);
			dispatchRequest(ureq, newTaid);
		} catch (NumberFormatException e) {
			log.warn("Not a number: {}", taid);
		}
	}

	private void dispatchRequest(UserRequest ureq, int newTaid) {
		if (isEnabled(newTaid) && newTaid >= 0 && newTaid < getTabCount()) {
			TabPane pane = getTabPaneAt(selectedPane);	
			setSelectedPane(ureq, newTaid);
			TabPane newPane = getTabPaneAt(selectedPane);
			fireEvent(ureq, new TabbedPaneChangedEvent(pane.getComponent(), newPane.getComponent(), newPane.getController()));
		}
	}
	
	/**
	 * Sets the selectedPane.
	 * 
	 * @param selectedPane The selectedPane to set
	 */
	public void setSelectedPane(UserRequest ureq, int newSelectedPane) {
		// get old selected component and remove it from render tree
		TabPane oldSelectedTab = getTabPaneAt(selectedPane);
		if(oldSelectedTab.getComponent() != null) {
			remove(oldSelectedTab.getComponent());
		}
		
		// activate new
		selectedPane = newSelectedPane;
		TabPane newSelectedTab = getTabPaneAt(newSelectedPane);
		Component component = newSelectedTab.getComponent();
		if(component == null && newSelectedTab.hasTabCreator()) {
			component = newSelectedTab.createComponent(ureq);
		}
		super.put("atp", component); 
	}
	
	public OLATResourceable getTabResource() {
		return OresHelper.createOLATResourceableInstance("tab", Long.valueOf(selectedPane));
	}
	
	public void addToHistory(UserRequest ureq, WindowControl wControl) {
		OLATResourceable ores = getTabResource();
		BusinessControlFactory.getInstance().createBusinessWindowControl(ureq, ores, null, wControl, true);
	}

	/**
	 * @param displayName
	 * @param component
	 * @return
	 */
	public int addTab(String displayName, Component component) {
		tabPanes.add(new TabPane(displayName, component));
		if (selectedPane == -1) {
			selectedPane = 0; // if no pane has been selected, select the first one
			super.put("atp", component); 
		}
		return tabPanes.size() - 1;
	}
	
	public int addTab(String displayName, Controller controller) {
		TabPane tab = new TabPane(displayName, controller);
		tabPanes.add(tab);
		if (selectedPane == -1) {
			selectedPane = 0; // if no pane has been selected, select the first one
			super.put("atp", tab.getComponent()); 
		}
		return tabPanes.size() - 1;
	}
	
	public int addTab(UserRequest ureq, String displayName, TabComponentCreator creator) {
		TabPane tab = new TabPane(displayName, creator);
		tabPanes.add(tab);
		if (selectedPane == -1) {
			selectedPane = 0; // if no pane has been selected, select the first one
			if(tab.getComponent() == null) {
				tab.createComponent(ureq);
			}
			super.put("atp", tab.getComponent()); 
		}
		return tabPanes.size() - 1;
	}
	
	public int addTabControllerCreator(UserRequest ureq, String displayName, TabControllerCreator creator) {
		TabPane tab = new TabPane(displayName, creator);
		tabPanes.add(tab);
		if (selectedPane == -1) {
			selectedPane = 0; // if no pane has been selected, select the first one
			if(tab.getComponent() == null) {
				tab.createComponent(ureq);
			}
			super.put("atp", tab.getComponent()); 
		}
		return tabPanes.size() - 1;
	}
	
	public int addTab(int pos, String displayName, Controller controller) {
		TabPane tab = new TabPane(displayName, controller);
		tabPanes.add(pos, tab);
		if (selectedPane == -1) {
			selectedPane = 0; // if no pane has been selected, select the first one
			super.put("atp", tab.getComponent()); 
		} else if(selectedPane == pos) {
			super.put("atp", tab.getComponent());
		}
		return pos;
	}
	
	public boolean containsTab(Component component) {
		boolean found = false;
		for(int i=tabPanes.size(); i-->0; ) {
			if(tabPanes.get(i).getComponent() == component) {
				found = true;
			}
		}
		return found;
	}
	
	public int indexOfTab(Component component) {
		for(int i=tabPanes.size(); i-->0; ) {
			if(tabPanes.get(i).getComponent() == component) {
				return i;
			}
		}
		return -1;
	}
	
	public int indexOfTab(Controller controller) {
		for(int i=tabPanes.size(); i-->0; ) {
			if(tabPanes.get(i).getController() == controller) {
				return i;
			}
		}
		return -1;
	}
	
	public boolean containsTab(String displayName) {
		boolean found = false;
		for(int i=tabPanes.size(); i-->0; ) {
			if(displayName.equals(tabPanes.get(i).getDisplayName())) {
				found = true;
			}
		}
		return found;
	}
	
	public int indexOfTab(String displayName) {
		for(int i=tabPanes.size(); i-->0; ) {
			if(displayName.equals(tabPanes.get(i).getDisplayName())) {
				return i;
			}
		}
		return -1;
	}
	
	public void replaceTab(int pos, Controller controller) {
		tabPanes.get(pos).setController(controller);
		if(pos == selectedPane) {
			super.put("atp", controller.getInitialComponent());
		}
	}
	
	public void replaceTab(int pos, Component component) {
		tabPanes.get(pos).setComponent(component);
		if(pos == selectedPane) {
			super.put("atp", component);
		}
	}
	
	public void removeTab(Component component) {
		int index = indexOfTab(component);
		if(index >= 0 && index < tabPanes.size()) {
			tabPanes.remove(index);
			if(selectedPane == index) {
				for(int i=0; i<tabPanes.size(); i++) {
					if(tabPanes.get(i).getComponent() != null) {
						setSelectedPane(null, i);
						break;
					}
				}
			}
		}
	}

	public void removeAll() {
		if (selectedPane != -1) {
			TabPane selected = getTabPaneAt(selectedPane);
			if(selected != null && selected.getComponent() != null) {
				remove(selected.getComponent());
			}
		}
		tabPanes.clear();
		selectedPane = -1;
		setDirty(true);
	}
	
	/**
	 * The method doessn't instantiate any component via
	 * the TabCreaator interface.
	 * 
	 * @param position The index of the tab
	 * @return A component
	 */
	protected Component getTabAt(int position) {
		return tabPanes.get(position).getComponent();
	}
	
	protected TabPane getTabPaneAt(int position) {
		return tabPanes.get(position);
	}

	/**
	 * @param position
	 * @return
	 */
	protected String getDisplayNameAt(int position) {
		return tabPanes.get(position).getDisplayName();
	}

	/**
	 * @return The number of tabs
	 */
	protected int getTabCount() {
		return tabPanes.size();
	}

	/**
	 * Returns the selectedPane.
	 * 
	 * @return int
	 */
	public int getSelectedPane() {
		return selectedPane;
	}
	
	/**
	 * Return the selected controller only if you used
	 * the addTab with a controller as parameter!
	 * 
	 * @return
	 */
	public Controller getSelectedController() {
		int index = getSelectedPane();
		if(index >= 0 && index < this.tabPanes.size()) {
			return tabPanes.get(index).getController();
		}
		return null;
	}

	/**
	 * @deprecated
	 * @param displayName
	 */
	@Deprecated
	public void setSelectedPane(UserRequest ureq, String displayName) {
		if (displayName == null) return;
		int pos = indexOfTab(displayName);
		if (pos > -1) {
			setSelectedPane(ureq, pos);
		}
	}

	@Override
	public String getExtendedDebugInfo() {
		return "selectedPane:" + selectedPane;
	}

	/**
	 * @param pane
	 * @param enabled
	 */
	public void setEnabled(int pane, boolean enabled) {
		boolean wasEnabled = isEnabled(pane);
		if (wasEnabled != enabled) {
			setDirty(true);
		}
		tabPanes.get(pane).setEnabled(enabled);
		setDirty(true);
	}

	/**
	 * @param pane
	 * @return
	 */
	protected boolean isEnabled(int pane) {
		return tabPanes.get(pane).isEnabled();
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	protected Translator getCompTrans() {
		return compTrans;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		OLATResourceable ores = entries.get(0).getOLATResourceable();
		if("tab".equals(ores.getResourceableTypeName())) {
			int pos = ores.getResourceableId().intValue();
			if(pos != selectedPane && pos >= 0 && pos < getTabCount()) {
				dispatchRequest(ureq, pos);
			}
		}
	}
	
	private static class TabPane {
		
		private boolean enabled = true;
		private final String displayName;
		private Component component;
		private Controller controller;
		private TabComponentCreator componentCreator;
		private TabControllerCreator controllerCreator;
		
		public TabPane(String displayName, Component component) {
			this.displayName = displayName;
			this.component = component;
			this.enabled = true;
		}
		
		public TabPane(String displayName, Controller controller) {
			this.displayName = displayName;
			this.controller = controller;
			this.component = controller.getInitialComponent();
			this.enabled = true;
		}
		
		public TabPane(String displayName, TabComponentCreator componentCreator) {
			this.displayName = displayName;
			this.componentCreator = componentCreator;
			this.enabled = true;
		}
		
		public TabPane(String displayName, TabControllerCreator controllerCreator) {
			this.displayName = displayName;
			this.controllerCreator = controllerCreator;
			this.enabled = true;
		}
		
		public boolean isEnabled() {
			return enabled;
		}
		
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
		
		public String getDisplayName() {
			return displayName;
		}
		
		public boolean hasTabCreator() {
			return componentCreator != null || controllerCreator != null;
		}
		
		public Component createComponent(UserRequest ureq) {
			if(componentCreator != null) {
				component = componentCreator.create(ureq);
			} else if(controllerCreator != null) {
				controller = controllerCreator.create(ureq);
				component = controller.getInitialComponent();
			}
			return component;
		}
		
		public Controller getController() {
			return controller;
		}
		
		public void setController(Controller controller) {
			if(controller == null) {
				component = null;
			} else {
				this.controller = controller;
				this.component = controller.getInitialComponent();
			}
		}
		
		public Component getComponent() {
			return component;
		}
		
		public void setComponent(Component component) {
			this.component = component;
		}
	}
}