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
import java.util.BitSet;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.Container;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;

/**
 * enclosing_type Description: <br>
 * 
 * @author Felix Jost
 */
public class TabbedPane extends Container implements Activateable2 {
	private static final ComponentRenderer RENDERER = new TabbedPaneRenderer();

	/**
	 * Comment for <code>PARAM_PANE_ID</code>
	 */
	protected static final String PARAM_PANE_ID = "taid";

	private List<Component> tabbedPanes = new ArrayList<Component>(4);
	private List<String> displayNames = new ArrayList<String>(4);
	private BitSet disabledPanes = new BitSet(4);
	private int selectedPane = -1;
	
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
	protected void doDispatchRequest(UserRequest ureq) {
		// the taid indicates which tab the user clicked
		String s_taid = ureq.getParameter(PARAM_PANE_ID);
		int newTaid = Integer.parseInt(s_taid);
				
		dispatchRequest(ureq, newTaid);
	}

	/**
	 * @param ureq
	 * @param newTaid
	 */
	private void dispatchRequest(UserRequest ureq, int newTaid) {
		if (!isEnabled(newTaid)) throw new AssertException("tab with id "+newTaid+" is not enabled, but was dispatched");
		Component oldSelComp = getTabAt(selectedPane);	
		setSelectedPane(newTaid);
		Component newSelComp = getTabAt(selectedPane);
		fireEvent(ureq, new TabbedPaneChangedEvent(oldSelComp, newSelComp));
	}
	
	/**
	 * Sets the selectedPane.
	 * 
	 * @param selectedPane The selectedPane to set
	 */
	public void setSelectedPane(int selectedPane) {
		// get old selected component and remove it from render tree
		Component oldSelComp = getTabAt(this.selectedPane);
		remove(oldSelComp);
		
		// activate new
		this.selectedPane = selectedPane;
		Component newSelComp = getTabAt(selectedPane);
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
		displayNames.add(displayName);
		tabbedPanes.add(component);
		if (selectedPane == -1) {
			selectedPane = 0; // if no pane has been selected, select the first one
			super.put("atp", component); 
		}
		return tabbedPanes.size() - 1;
	}
	
	public boolean containsTab(Component component) {
		return tabbedPanes.contains(component);
	}
	
	public boolean containsTab(String displayName) {
		return displayNames.contains(displayName);
	}
	
	public void replaceTab(int pos, Component component) {
		tabbedPanes.set(pos, component);
		if(pos == selectedPane) {
			super.put("atp", component);
		}
	}
	
	public void removeTab(Component component) {
		int index = tabbedPanes.indexOf(component);
		if(index >= 0 && index < tabbedPanes.size()) {
			tabbedPanes.remove(index);
			displayNames.remove(index);
			if(selectedPane == index) {
				setSelectedPane(0);
			}
			setDirty(true);
		}
	}

	public void removeAll() {
		if (this.selectedPane != -1) {
			Component oldSelComp = getTabAt(this.selectedPane);
			remove(oldSelComp);
		}
		tabbedPanes.clear();
		displayNames.clear();
		disabledPanes.clear();
		selectedPane = -1;
		setDirty(true);
	}
	
	/**
	 * @param position
	 * @return
	 */
	protected Component getTabAt(int position) {
		return tabbedPanes.get(position);
	}

	/**
	 * @param position
	 * @return
	 */
	protected String getDisplayNameAt(int position) {
		return displayNames.get(position);
	}

	/**
	 * @return
	 */
	protected int getTabCount() {
		return (tabbedPanes == null ? 0 : tabbedPanes.size());
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
	 * @deprecated
	 * @param displayName
	 */
	public void setSelectedPane(String displayName) {
		if (displayName == null) return;
		int pos = displayNames.indexOf(displayName);
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
		boolean wasEnabled = isEnabled();
		if (wasEnabled ^ enabled) {
			setDirty(true);
		}
		disabledPanes.set(pane, !enabled);
	}

	/**
	 * @param pane
	 * @return
	 */
	protected boolean isEnabled(int pane) {
		return !disabledPanes.get(pane);
	}

	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	protected Translator getCompTrans() {
		return compTrans;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		int pos = entries.get(0).getOLATResourceable().getResourceableId().intValue();
		if(pos != selectedPane) {
			dispatchRequest(ureq, pos);
		}
	}
}