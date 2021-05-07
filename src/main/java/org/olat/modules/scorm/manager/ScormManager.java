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
*/

package org.olat.modules.scorm.manager;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;
import org.jdom.input.JDOMParseException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.modules.scorm.SettingsHandler;
import org.olat.modules.scorm.contentpackaging.CP_Core;
import org.olat.modules.scorm.contentpackaging.NavigationViewer;
import org.olat.modules.scorm.server.sequence.SequenceManager;


/**
 * Initial Date: 13.06.2005 <br>
 * @author guido
 */
public class ScormManager {

	/**
	 * instance of the navigationviewer - used to lookup values/elements from the
	 * manifest.
	 */
	private NavigationViewer _navViewer;

	/**
	 * A boolean to tell us if the player should show the tree widget
	 */
	private boolean _showTreeWidget;

	/**
	 * A boolean to tell us if the player should show the next and prev navigation
	 */
	private boolean _showNavigation;

	/**
	 * Whether to use auto progression thru the package
	 */
	private boolean _autoProgression;

	/**
	 * The manager's instance of the scorm sequence manager - used for sequencing
	 * and prerequisites, and cmi updating models
	 */
	private SequenceManager _sequence;

	/**
	 * boolean value used to flag that navigation nav file has been found
	 */
	private boolean _error_found;

	private boolean _ReloadNoItemFoundExceptionFlag = true;

	/**
	 * Keep hold of the default org (JDOM) element
	 */
	private Element _defaultorg;
	private List<Element> organizationElements;

	/**
	 * Used to keep track of how many items there are in a organization (this is
	 * array based, so initially set to -1)
	 */
	private int _itemCount = -1;

	/**
	 * Default constructor
	 */
	public ScormManager() {
	// empty constructor
	}

	/**
	 * Comment for <code>scormFileHandler</code>
	 */
	private SettingsHandler scormSettingsHandler;

	/**
	 * Alternate constructor - used to pass the the package name (i.e. - used to
	 * construct a path to disk and find the files needed. It also allows us to
	 * specify if we want to show the tree widget and the next and previous
	 * buttons in the navigation
	 * 
	 * @param packagePath
	 * @param repoId
	 * @param courseId
	 * @param userPath
	 * @param username
	 * @param userid
	 * @param showTreeWidget
	 * @param showNavigation
	 * @param autoProgression
	 * @param lesson_mode
	 * @param credit_mode
	 */

	public ScormManager(String packagePath, boolean showTreeWidget, boolean showNavigation, boolean autoProgression, SettingsHandler settings)
	throws IOException {
		this.scormSettingsHandler = settings;
		_error_found = false;
		_showNavigation = showNavigation;
		_showTreeWidget = showTreeWidget;
		_autoProgression = autoProgression;
		init();
	}

	/**
	 * 
	 */
	private void init() throws IOException {
		// Make sure its there
		if (scormSettingsHandler.getManifestFile().exists()) {
			try {
				_navViewer = new NavigationViewer(scormSettingsHandler);
			} catch(IOException e) {
				throw e;
			} catch(JDOMParseException e) {
				throw new IOException("Parse exception", e);
			} catch (Exception ex) {
				throw new OLATRuntimeException(ScormManager.class, "Could not load manifest",ex);
			}
			// Package Links - use a clone so we can add referenced elements
			Element manifestRoot = (Element) _navViewer.getRootElement().clone();
			// now get the organizations node
			Element orgs = manifestRoot.getChild(CP_Core.ORGANIZATIONS, manifestRoot.getNamespace());
			Element[] tmp = _navViewer.getOrganizationList();
			organizationElements = new LinkedList<>();
			for (int i = 0; i < tmp.length; ++i) {
				organizationElements.add(tmp[i]);
			}
			// remember the default organization
			_defaultorg = _navViewer.getDefaultOrgElement(orgs);
			// NO orgs, so cant play - flag it
			if (_defaultorg == null) {
				_ReloadNoItemFoundExceptionFlag = false;
			} else {
				_sequence = new SequenceManager(_defaultorg.getAttributeValue(CP_Core.IDENTIFIER), scormSettingsHandler);
				// it exists so try to load it...
				try {
					initialize();
				} catch (Exception ex) {
					_error_found = true;
					throw new OLATRuntimeException(ScormManager.class, "Could not initialise manifest",ex);
				}
			} //
		} else {
			// problems if we cannot find the nav file...
			_error_found = true;
		}

	}

	/**
	 * Accesor method for the sequencer
	 * 
	 * @return SequenceManager
	 */
	public SequenceManager getSequence() {
		return _sequence;
	}

	/**
	 * Accessor method - should we show the tree or not?
	 * 
	 * @return boolean
	 */
	public boolean isTreeVisible() {
		return _showTreeWidget;
	}

	/**
	 * Accessor method - should we show the navigation or not?
	 * 
	 * @return boolean
	 */
	public boolean isNavVisible() {
		return _showNavigation;
	}

	/**
	 * Accessor method - boolean to use auto progression of items in player
	 * 
	 * @return boolean
	 */
	public boolean isAutoProgressionEnabled() {
		return _autoProgression;
	}

	/**
	 * A method to do some initialising before we actually play the package. Here
	 * we get the default organization and then pass that to a method which will
	 * parse that organization (JDOM) and figure out what we need, in terms of
	 * items, sequence, prerequisites, etc
	 */
	public void initialize() {
		if (_navViewer.getDocument() == null) {
			return;
		}
		parse(false);
	}

	/**
	 * A method to start the parse of the manifest and write the javascript
	 * strings which the tree widget needs and also to populate the sequencer
	 * 
	 * @param useRelativePaths
	 * @return String[]
	 */
	private void parse(boolean useRelativePaths) {
		// New Vector

		// now call createNavLinks() which should interrogate the org/item structure
		createNavLinksForAllOranizations("menu", useRelativePaths);
	}
	
	public String[] getAllScoIdentifiers() {
		return _navViewer.getAllScoIdentifiers();
	}
	
	public int getNumOfSCOs() {
		return _navViewer.getAllScoIdentifiers().length;
	}

	private void createNavLinksForAllOranizations(String menuParent, boolean useRelativePaths) {
		_itemCount = -1;
		for (int i = 0; i < organizationElements.size(); ++i) {
			createNavLinks(organizationElements.get(i), menuParent, useRelativePaths);
		}
	}
	
	/**
	 * Recursive method to find all of the javascript strings (item info) and
	 * populate the sequencer
	 * 
	 * @param javascriptStrings
	 * @param element
	 * @param menuParent
	 * @param useRelativePaths
	 */
	private void createNavLinks(Element element, String menuParent, boolean useRelativePaths) {
		String name = element.getName();
		// ORGANIZATION
		if (name.equals(CP_Core.ORGANIZATION) && _navViewer.isDocumentNamespace(element)) {
			menuParent = "menu";
		}
		// ITEM
		else if (name.equals(CP_Core.ITEM) && _navViewer.isDocumentNamespace(element)) {
			++_itemCount;
			String itemId = element.getAttributeValue(CP_Core.IDENTIFIER);
			String hyperLink = "";
			String url = "";
			String scoType = "";
			// Display Title if there is one
			String title = "Item";
			Element titleElement = element.getChild(CP_Core.TITLE, element.getNamespace());
			if (titleElement != null) {
				if (!titleElement.getText().equals("")) title = titleElement.getText();
			}
			// check to see that the isvisible attribute is not set to false...
			String isVisibleAttrib = element.getAttributeValue(CP_Core.ISVISIBLE);
			if (isVisibleAttrib != null) {
				if (isVisibleAttrib.equals("false")) {
					title = "* hidden";
				}
			}
			// What does this Item reference?
			Element ref_element = _navViewer.getScormCore().getReferencedElement(element);
			String prerequisites = "";
			if (ref_element != null) {
				String ref_name = ref_element.getName();
				// A RESOURCE
				if (ref_name.equals(CP_Core.RESOURCE)) {
					scoType = _navViewer.findScoType(element);
					// Relative path for export - Note the "../" is relative to where the
					// Nav file is!
					if (useRelativePaths) {
						url = _navViewer.getScormCore().getRelativeURL(element);
						// Only if local path add relative bit
						if (isExternalURL(url) == false) {
							url = "../" + url;
						}
					}
					// Absolute Paths for Previewing in-situ
					else {
						String turl = _navViewer.getLaunch(element);
						url = turl;
					}
					if (url != null) {
						hyperLink = url;
						if (!title.equals("* hidden")) {
							prerequisites = _navViewer.getPrerequisites(element);
							if (prerequisites == null) {
								prerequisites = "";
							}
						}
					}
				}
				// A sub-MANIFEST
				else if (ref_name.equals(CP_Core.MANIFEST)) {
					hyperLink = "javascript:void(0)";
					// Get ORGANIZATIONS Element
					Element orgsElement = ref_element.getChild(CP_Core.ORGANIZATIONS, ref_element.getNamespace());
					// Now we have to get the default ORGANIZATION
					if (orgsElement != null) ref_element = _navViewer.getScormCore().getDefaultOrganization(orgsElement);
					// Get the children of the referenced <organization> element and graft
					// clones
					if (ref_element != null) {
						Iterator it = ref_element.getChildren().iterator();
						while (it.hasNext()) {
							Element ref_child = (Element) it.next();
							element.addContent((Element) ref_child.clone());
						}
					}
				}
			} else {
				hyperLink = "javascript:void(0)";
			}
			_sequence.addNewItem(itemId, hyperLink, _itemCount, scoType, title, prerequisites);
			menuParent = itemId;
		}
		// round we go again...
		Iterator it = element.getChildren().iterator();
		while (it.hasNext()) {
			Element child = (Element) it.next();
			createNavLinks(child, menuParent, useRelativePaths);
		}
	}

	/**
	 * @return Returns the _errorfound.
	 */
	public boolean isErrorFound() {
		return _error_found;
	}

	public boolean hasItemsFound() {
		return this._ReloadNoItemFoundExceptionFlag;
	}

	/**
	 * @param href The href to parse
	 * @return true if href is an external URL
	 */
	private static boolean isExternalURL(String href) {
		if (href != null) {
			href = href.toLowerCase();
			return href.startsWith("http") || href.startsWith("www") || href.startsWith("ftp:");
		}
		return false;
	}
}
