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
package org.olat.core.commons.fullWebApp;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowSettings;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.ScreenMode.Mode;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.layout.MainLayout3ColumnsController;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;

/**
 * <h3>Description:</h3> This main layout controller provides a three column
 * layout based on the YAML framework. You must use the the
 * BaseFullWebappController as parent controller or implement the necessary YAML
 * HTML wrapper markup code yourself.
 * <p>
 * The meaning of the col1, col2 and col3 are strictly following the YAML
 * concept. This means, that in a brasato web application in most cases the
 * following mapping is applied:
 * <ul>
 * <li>col1: menu</li>
 * <li>col2: toolboxes</li>
 * <li>col3: content area</li>
 * </ul>
 * Read the YAML specification if you don't understand why this is. Rendering is
 * all done using CSS.
 * <p>
 * For information about YAML please see @see http://www.yaml.de
 * <p>
 * <h3>Events thrown by this controller:</h3>
 * <ul>
 * <li>none</li>
 * </ul>
 * <p>
 * Initial Date: 11.10.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class LayoutMain3ColsController extends MainLayoutBasicController implements MainLayout3ColumnsController, Activateable2 {
	private VelocityContainer layoutMainVC;
	// current columns components
	private Component[] columns = new Component[3];
	// current css classes for the main div
	private Set<String> mainCssClasses = new HashSet<>();
	private LayoutMain3ColsConfig localLayoutConfig;
	private String layoutConfigKey = null;
	private Panel panel1, panel2, panel3;
	private Activateable2 activateableDelegate2;
	private boolean fullScreen = false;
	private ChiefController thebaseChief;

	
	
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param colCtrl3
	 */
	public LayoutMain3ColsController(UserRequest ureq, WindowControl wControl, Controller colCtrl3) {
		this(ureq,wControl, null, null, colCtrl3.getInitialComponent(), null, null);
		listenTo(colCtrl3);
		if(colCtrl3 instanceof Activateable2) {
			activateableDelegate2 = (Activateable2)colCtrl3;
		}
	}
	
	
	/**
	 * Constructor for creating a 3 col based menu on the main area. This
	 * constructor uses the default column width configuration
	 * 
	 * @param ureq
	 * @param wControl
	 * @param col1 usually the left column
	 * @param col2 usually the right column
	 * @param col3 usually the content column
	 * @param layoutConfigKey identificator for this layout to persist the users
	 *          column width settings
	 */
	public LayoutMain3ColsController(UserRequest ureq, WindowControl wControl, Component col1, Component col3,
			String layoutConfigKey) {
		this(ureq,wControl, col1, null, col3, layoutConfigKey, null);
	}
	
	/**
	 * Constructor for creating a 3 col based menu on the main area. This
	 * constructor uses the default column width configuration
	 * 
	 * @param ureq
	 * @param wControl
	 * @param col1 usually the left column
	 * @param col2 usually the right column
	 * @param col3 usually the content column
	 * @param layoutConfigKey identificator for this layout to persist the users
	 *          column width settings
	 */
	public LayoutMain3ColsController(UserRequest ureq, WindowControl wControl, Component col1, Component col2, Component col3,
			String layoutConfigKey) {
		this(ureq,wControl, col1, col2, col3, layoutConfigKey, null);
	}
	
	/**
	 * Constructor for creating a 3 col based menu on the main area
	 * 
	 * @param ureq
	 * @param wControl
	 * @param col1 usually the left column
	 * @param col2 usually the right column
	 * @param col3 usually the content column
	 * @param layoutConfigKey identificator for this layout to persist the users
	 *          column width settings
	 * @param defaultConfiguration The layout width configuration to be used
	 */
	public LayoutMain3ColsController(UserRequest ureq, WindowControl wControl, Component col1, Component col2, Component col3,
			String layoutConfigKey, LayoutMain3ColsConfig defaultConfiguration) {
		super(ureq, wControl);
		layoutMainVC = createVelocityContainer("main_3cols");
		this.layoutConfigKey = layoutConfigKey;

		localLayoutConfig = getGuiPrefs(ureq, defaultConfiguration);
		
		WindowSettings wSettings = wControl.getWindowBackOffice().getWindowSettings();

		// Push columns to velocity
		panel1 = new Panel("panel1");
		panel1.setVisible(!wSettings.isHideColumn1());
		if(col1 != null) {
			col1.setVisible(!wSettings.isHideColumn1());
		}
		layoutMainVC.put("col1", panel1);
		setCol1(col1);

		panel2 = new Panel("panel2");
		panel2.setVisible(!wSettings.isHideColumn2());
		if(col2 != null) {
			col2.setVisible(!wSettings.isHideColumn2());
		}
		layoutMainVC.put("col2", panel2);
		setCol2(col2);


		panel3 = new Panel("panel3");
		layoutMainVC.put("col3", panel3);
		setCol3(col3);
		
		if(col1 != null || col2 != null) {
			JSAndCSSComponent js = new JSAndCSSComponent("js", new String[] { "js/jquery/ui/jquery-ui-1.11.4.custom.resize.min.js" }, null);
			layoutMainVC.put("js", js);
		}

		putInitialPanel(layoutMainVC);
	}
	
	public boolean isFullScreen() {
		return fullScreen;
	}
	
	public void setAsFullscreen(UserRequest ureq) {
		ChiefController cc = getWindowControl().getWindowBackOffice().getChiefController();
		if (cc != null) {
			thebaseChief = cc;
			thebaseChief.getScreenMode().setMode(Mode.full, null);
		} else {
			Windows.getWindows(ureq).setFullScreen(Boolean.TRUE);
		}
		fullScreen = true;
	}
	
	public void activate() {
		getWindowControl().pushToMainArea(layoutMainVC);
	}
	
	public void deactivate(UserRequest ureq) {
		getWindowControl().pop();
		if (fullScreen) {
			if(thebaseChief != null) {
				thebaseChief.getScreenMode().setMode(Mode.standard, null);
			} else if (ureq != null){
				ChiefController cc = getWindowControl().getWindowBackOffice().getChiefController();
				if (cc != null) {
					thebaseChief = cc;
					thebaseChief.getScreenMode().setMode(Mode.standard, null);
				}
			}
		}
	}

	/**
	 * Add a controller to this layout controller that should be cleaned up when
	 * this layout controller is diposed. In most scenarios you should hold a
	 * reference to the content controllers that controll the col1, col2 or col3,
	 * but in rare cases this is not the case and you have no local reference to
	 * your controller. You can then use this method to add your controller. At
	 * the dispose time of the layout controller your controller will be disposed
	 * as well.
	 * 
	 * @param toBedisposedControllerOnDispose
	 */
	public void addDisposableChildController(Controller toBedisposedControllerOnDispose) {
		listenTo(toBedisposedControllerOnDispose);
	}
	
	//fxdiff BAKS-7 Resume function
	public void addActivateableDelegate(Activateable2 delegate) {
		this.activateableDelegate2 = delegate;
	}
	//fxdiff BAKS-7 Resume function
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(activateableDelegate2 != null) {
			activateableDelegate2.activate(ureq, entries, state);
		}
	}

	/**
	 * The Controller to be set on the mainPanel in case of disposing this layout
	 * controller.
	 * 
	 * @param disposedMessageControllerOnDipsose
	 */
	public void setDisposedMessageController(Controller disposedMessageControllerOnDipsose) {
		this.setDisposedMsgController(disposedMessageControllerOnDipsose);
	}

	/**
	 * Add a css class to the #o_main wrapper div, e.g. for special background
	 * formatting
	 * 
	 * @param cssClass
	 */
	public void addCssClassToMain(String cssClass) {
		if (mainCssClasses.contains(cssClass)) {
			// do nothing and report as error to console, but no GUI error for user
			getLogger().error("Tried to add CSS class::" + cssClass + " to #o_main but CSS class was already added");
		} else {
			mainCssClasses.add(cssClass);
			// add new CSS classes for main container
			String mainCss = calculateMainCssClasses(mainCssClasses);
			layoutMainVC.contextPut("mainCssClasses", mainCss);
		}
	}

	/**
	 * Remove a CSS class from the #o_main wrapper div
	 * 
	 * @param cssClass
	 */
	public void removeCssClassFromMain(String cssClass) {
		if (mainCssClasses.contains(cssClass)) {
			mainCssClasses.remove(cssClass);
			// add new CSS classes for main container
			String mainCss = calculateMainCssClasses(mainCssClasses);
			layoutMainVC.contextPut("mainCssClasses", mainCss);
		} else {
			// do nothing and report as error to console, but no GUI error for user
			getLogger().error("Tried to remove CSS class::" + cssClass + " from #o_main but CSS class was not there");
		}
	}

	@Override
	protected void doDispose() {
		columns = null;
		mainCssClasses = null;
		layoutMainVC = null;
		thebaseChief = null;
        super.doDispose();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == layoutMainVC) {
			doColumnWidth(ureq, event.getCommand(), ureq.getParameter("newEmWidth"));
		}
	}
	
	private void doColumnWidth(UserRequest ureq, String command, String width) {
		if(!StringHelper.containsNonWhitespace(command)) return;
		
		int parsedWidth;
		try {
			if(StringHelper.isLong(width)) {
				parsedWidth = Integer.parseInt(width);
				if (parsedWidth < 1) {
					// do not allow width smaller than 1em - resizer will be lost
					// otherwhise
					parsedWidth = 1;
				}
			} else {
				parsedWidth = 14; // default value
			}
		} catch (NumberFormatException e) {
			logWarn("Could not parse column width::" + width + " for command::" + command, e);
			parsedWidth = 14; // default value
		}
		if ("saveCol1Width".equals(command)) {
			localLayoutConfig.setCol1WidthEM(parsedWidth);
			saveGuiPrefs(ureq, localLayoutConfig);
			layoutMainVC.getContext().put("col1CustomCSSStyles", "width: " + localLayoutConfig.getCol1WidthEM() + "em;");
		} else if ("saveCol2Width".equals(command)) {
			localLayoutConfig.setCol2WidthEM(parsedWidth);
			saveGuiPrefs(ureq, localLayoutConfig);
			layoutMainVC.getContext().put("col2CustomCSSStyles", "width: " + localLayoutConfig.getCol2WidthEM() + "em;");
		}
	}
	
	private void saveGuiPrefs(UserRequest ureq, LayoutMain3ColsConfig layoutConfig ) {
		// save if not local setting
		if (layoutConfigKey != null) {
			UserSession usess = ureq.getUserSession();
			if(usess.isAuthenticated() && !usess.getRoles().isGuestOnly()) {
				ureq.getUserSession().getGuiPreferences().commit(this.getClass(), layoutConfigKey, layoutConfig);
			}
		}
	}

	/**
	 * Internal helper to load the layout config either from the GUI preferences
	 * or to generate a volatile one
	 * 
	 * @param ureq
	 * @return the layout column config
	 */
	private LayoutMain3ColsConfig getGuiPrefs(UserRequest ureq, LayoutMain3ColsConfig defaultConfiguration) {
		if (localLayoutConfig != null) { return localLayoutConfig; }
		LayoutMain3ColsConfig layoutConfig = null;
		if (layoutConfigKey != null && ureq.getUserSession().isAuthenticated() && !ureq.getUserSession().getRoles().isGuestOnly()) {
			// try to get persisted layout config
			layoutConfig = (LayoutMain3ColsConfig) ureq.getUserSession().getGuiPreferences().get(this.getClass(), layoutConfigKey);
		}
		if (layoutConfig == null) {
			// user has no config so far, use default configuration if available or create a new one
			if (defaultConfiguration == null) {
				layoutMainVC.contextPut("autoWidth", Boolean.TRUE);
				layoutConfig = new LayoutMain3ColsConfig();
			} else {
				layoutMainVC.contextPut("autoWidth", Boolean.FALSE);
				layoutConfig = defaultConfiguration;
			}
		}
		return layoutConfig;

	}

	/**
	 * @see org.olat.core.gui.control.generic.layout.MainLayout3ColumnsController#hideCol1(boolean)
	 */
	public void hideCol1(boolean hide) {
		hideCol(hide, 1);
	}

	/**
	 * @see org.olat.core.gui.control.generic.layout.MainLayout3ColumnsController#hideCol2(boolean)
	 */
	public void hideCol2(boolean hide) {
		hideCol(hide, 2);
	}

	/**
	 * @see org.olat.core.gui.control.generic.layout.MainLayout3ColumnsController#hideCol3(boolean)
	 */
	public void hideCol3(boolean hide) {
		hideCol(hide, 3);
	}

	/**
	 * Internal method to hide a column without removing the component
	 * 
	 * @param hide
	 * @param column
	 */
	private void hideCol(boolean hide, int column) {
		String cssName = "o_hide_main_content";
		if (column == 1) {
			cssName = "o_hide_main_left";
		} if (column == 2) {
			cssName = "o_hide_main_right";
		}
		
		if (hide) {
			if (columns[column - 1] == null) {
				return;
			} else {
				mainCssClasses.add(cssName);
			}
		} else {
			if (columns[column - 1] == null) {
				return;
			} else {
				mainCssClasses.remove(cssName);
			}
		}
		// add new CSS classes for main container
		String mainCss = calculateMainCssClasses(mainCssClasses);
		layoutMainVC.contextPut("mainCssClasses", mainCss);
	}

	/**
	 * @see org.olat.core.gui.control.generic.layout.MainLayout3ColumnsController#setCol1(org.olat.core.gui.components.Component)
	 */
	public void setCol1(Component col1Component) {
		setCol(col1Component, 1);
		panel1.setContent(col1Component);
		// init col width
		if(col1Component != null && col1Component.isVisible()) {
			layoutMainVC.contextPut("col1CustomCSSStyles", "width: " + localLayoutConfig.getCol1WidthEM() + "em;");
			layoutMainVC.contextPut("col3CustomCSSStyles1", "margin-left: " + localLayoutConfig.getCol1WidthEM() + "em;");
		} else {
			layoutMainVC.contextPut("col3CustomCSSStyles1", "margin-left:0;");
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.layout.MainLayout3ColumnsController#setCol2(org.olat.core.gui.components.Component)
	 */
	public void setCol2(Component col2Component) {
		setCol(col2Component, 2);
		panel2.setContent(col2Component);
		if(col2Component != null && col2Component.isVisible()) {
			layoutMainVC.contextPut("col2CustomCSSStyles", "width: " + localLayoutConfig.getCol2WidthEM() + "em;");
			layoutMainVC.contextPut("col3CustomCSSStyles2", "margin-right: " + localLayoutConfig.getCol2WidthEM() + "em;");
		} else {
			layoutMainVC.contextPut("col3CustomCSSStyles2", "margin-right: 0;");
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.layout.MainLayout3ColumnsController#setCol3(org.olat.core.gui.components.Component)
	 */
	public void setCol3(Component col3Component) {
		setCol(col3Component, 3);
		panel3.setContent(col3Component);

	}

	/**
	 * Internal method to set a new column
	 * 
	 * @param newComponent
	 * @param column
	 */
	private void setCol(Component newComponent, int column) {
		String cssName = "o_hide_main_content";
		if (column == 1) {
			cssName = "o_hide_main_left";
		} if (column == 2) {
			cssName = "o_hide_main_right";
		}

		Component oldComp = columns[column - 1];
		// remove old component from velocity first
		if (oldComp == null) {
			// css class to indicate if a column is hidden or shown
			mainCssClasses.remove(cssName);
		} else {
			layoutMainVC.remove(oldComp);
		}

		// add new component to velocity
		if (newComponent == null) {
			// tell YAML layout via css class on main container to not display this
			// column: this will adjust margin of col3 in normal setups
			mainCssClasses.add(cssName);
			layoutMainVC.contextPut("existsCol" + column, Boolean.FALSE);
		} else {
			layoutMainVC.contextPut("existsCol" + column, Boolean.TRUE);
		}

		// add new CSS classes for main container
		String mainCss = calculateMainCssClasses(mainCssClasses);
		layoutMainVC.contextPut("mainCssClasses", mainCss);

		// remember new component
		columns[column - 1] = newComponent;
	}

	/**
	 * Helper to generate the CSS classes that are set on the #o_main container to
	 * correctly render the column width and margins according to the YAML spec
	 * 
	 * @param classes
	 * @return
	 */
	private String calculateMainCssClasses(Set<String> classes) {
		StringBuilder mainCss = new StringBuilder(32);
		for (Iterator<String> iter = classes.iterator(); iter.hasNext();) {
			mainCss.append(iter.next());
			if (iter.hasNext()) {
				mainCss.append(" ");
			}
		}
		return mainCss.toString();
	}
}
