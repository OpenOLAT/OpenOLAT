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
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;

/**
 * enclosing_type Description: <br>
 * 
 * @author Felix Jost
 */
public class TabbedPane extends Container implements Activateable2 {
	private static final OLog log = Tracing.createLoggerFor(TabbedPane.class);
	private static final ComponentRenderer RENDERER = new TabbedPaneRenderer();

	/**
	 * Comment for <code>PARAM_PANE_ID</code>
	 */
	protected static final String PARAM_PANE_ID = "taid";


	private int selectedPane = -1;
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

	/**
	 * @see org.olat.core.gui.components.Component#dispatchRequest(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		// the taid indicates which tab the user clicked
		String s_taid = ureq.getParameter(PARAM_PANE_ID);
		try {
			int newTaid = Integer.parseInt(s_taid);
			dispatchRequest(ureq, newTaid);
		} catch (NumberFormatException e) {
			log.warn("Not a number: " + s_taid);
		}
	}

	/**
	 * @param ureq
	 * @param newTaid
	 */
	private void dispatchRequest(UserRequest ureq, int newTaid) {
		if (isEnabled(newTaid) && newTaid >= 0 && newTaid < getTabCount()) {
			Component oldSelComp = getTabAt(selectedPane);	
			setSelectedPane(newTaid);
			Component newSelComp = getTabAt(selectedPane);
			fireEvent(ureq, new TabbedPaneChangedEvent(oldSelComp, newSelComp));
		}
	}
	
	/**
	 * Sets the selectedPane.
	 * 
	 * @param selectedPane The selectedPane to set
	 */
	public void setSelectedPane(int newSelectedPane) {
		// get old selected component and remove it from render tree
		Component oldSelComp = getTabAt(selectedPane);
		remove(oldSelComp);
		
		// activate new
		selectedPane = newSelectedPane;
		Component newSelComp = getTabAt(newSelectedPane);
		super.put("atp", newSelComp); 
		//setDirty(true); not needed since: line above marks this container automatically dirty
	}
	
	public OLATResourceable getTabResource() {
		return OresHelper.createOLATResourceableInstance("tab", new Long(selectedPane));
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
			if(displayName.equals(tabPanes.get(i).getComponent())) {
				return i;
			}
		}
		return -1;
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
				setSelectedPane(0);
			}
			setDirty(true);
		}
	}

	public void removeAll() {
		if (selectedPane != -1) {
			Component oldSelComp = getTabAt(selectedPane);
			remove(oldSelComp);
		}
		tabPanes.clear();
		selectedPane = -1;
		setDirty(true);
	}
	
	/**
	 * @param position
	 * @return
	 */
	protected Component getTabAt(int position) {
		return tabPanes.get(position).getComponent();
	}

	/**
	 * @param position
	 * @return
	 */
	protected String getDisplayNameAt(int position) {
		return tabPanes.get(position).getDisplayName();
	}

	/**
	 * @return
	 */
	protected int getTabCount() {
		return (tabPanes == null ? 0 : tabPanes.size());
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
	public void setSelectedPane(String displayName) {
		if (displayName == null) return;
		int pos = indexOfTab(displayName);
		if (pos > -1) {
			setSelectedPane(pos);
		}
	}

	/**
	 * @see org.olat.core.gui.components.Component#getExtendedDebugInfo()
	 */
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
		tabPanes.get(pane).setEnabled(!enabled);
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
		
		private boolean enabled;
		private final String displayName;
		private Component component;
		private Controller controller;
		
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
		
		public boolean isEnabled() {
			return enabled;
		}
		
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
		
		public String getDisplayName() {
			return displayName;
		}
		
		public Controller getController() {
			return controller;
		}
		
		public void setController(Controller controller) {
			if(controller == null) {
				controller = null;
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