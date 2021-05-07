/**
 * RELOAD TOOLS Copyright (c) 2003 Oleg Liber, Bill Olivier, Phillip Beauvoir,
 * Paul Sharples Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions: The
 * above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS
 * IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. Project
 * Management Contact: Oleg Liber Bolton Institute of Higher Education Deane
 * Road Bolton BL3 5AB UK e-mail: o.liber@bolton.ac.uk Technical Contact:
 * Phillip Beauvoir e-mail: p.beauvoir@bolton.ac.uk Paul Sharples e-mail:
 * p.sharples@bolton.ac.uk Web: http://www.reload.ac.uk
 */
package org.olat.modules.scorm.contentpackaging;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;
import org.jdom.Namespace;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.modules.scorm.SettingsHandler;
import org.olat.modules.scorm.server.servermodels.SequencerModel;
import org.olat.modules.scorm.server.servermodels.XMLDocument;

/**
 * A class used to interrogate the manifest. Used to show the tree in the
 * organizations panel and to figure out which items are scos/assets
 * 
 * @author Paul Sharples
 */
public class NavigationViewer extends XMLDocument {

	/**
	 * Comment for <code>NONE</code>
	 */
	public static final String NONE = "none";

	/**
	 * Our instance of core scorm methods
	 */
	private SCORM12_Core _scormCore;

	/**
	 * An instance of the disk version of the sequencer model.
	 */
	private SequencerModel _sequencerModel;

	private SettingsHandler settings;

	/**
	 * Accessor method to return the scomrCore instance.
	 * 
	 * @return SCORM12_Core
	 */
	public SCORM12_Core getScormCore() {
		return _scormCore;
	}

	/**
	 * Constructor with file param, to load manifest as JDOM doc.
	 * 
	 * @param file
	 * @throws Exception
	 */
	public NavigationViewer(SettingsHandler settings) throws Exception {
		this.settings = settings;
		super.loadDocument(settings.getManifestFile());
		_scormCore = new SCORM12_Core(this);
		_sequencerModel = new SequencerModel(settings.getScoItemSequenceFile());
	}

	/**
	 * A method to get the default organization identifer (string) from the
	 * manifest file
	 * 
	 * @return default Organisation as String
	 */
	public String getDefaultOrg() {
		String defOrg = _sequencerModel.getDefaultOrg();
		if (defOrg == null) defOrg = "";
		return defOrg;
	}

	/**
	 * REturn the default organization element
	 * 
	 * @param organizationsNode
	 * @return xml Element
	 */
	public Element getDefaultOrgElement(Element organizationsNode) {
		return _scormCore.getElementByIdentifier(organizationsNode, getDefaultOrg());
	}

	/**
	 * A method to let us know if the manifest has been updated since it was last
	 * loaded into JDOM. If it has been chnaged then the time stamp will be
	 * updated - and so it will be different to the timestamp we have recorded in
	 * out settings file - so we compare the two.
	 * 
	 * @return true or false
	 */
	public boolean hasManifestChanged() {
		String manifestTimeStamp = Long.toString(getFile().lastModified());
		SequencerModel sequence = new SequencerModel(settings.getScoItemSequenceFile());
		String storedLastModified = sequence.getManifestModifiedDate();
		return (manifestTimeStamp.equals(storedLastModified));
	}

	/**
	 * Method to ascertain which organization is the default, by index
	 * 
	 * @param index
	 * @return
	 */
	public boolean getDefaultOrgByIndex(int index) {
		String theDefaultOrg = getDefaultOrg();
		Element[] orgs = getOrganizationList();
		if (orgs[index].getAttributeValue(SCORM12_Core.IDENTIFIER).equals(theDefaultOrg)) { return true; }
		return false;
	}

	/**
	 * Wrapper method - to see if an element references another element
	 * 
	 * @param element
	 * @return
	 */
	public boolean isReferencingElement(Element element) {
		return _scormCore.isReferencingElement(element);
	}

	/**
	 * Method to ascertain if an item is a sco, asset or does not have a
	 * referenced resource
	 * 
	 * @param element
	 * @return
	 */
	public String findScoType(Element element) {
		if (element.getName() == SCORM12_Core.ITEM) {
			if (!isReferencingElement(element)) {
				return NONE;
			} else {
				Element referencedElement = _scormCore.getReferencedElement(element);
				// does this reference a resource or submanifest
				if (referencedElement.getName().equals(SCORM12_Core.MANIFEST)) { return SCORM12_Core.MANIFEST; }
				String scoType = referencedElement.getAttributeValue(SCORM12_Core.SCORMTYPE, SCORM12_DocumentHandler.ADLCP_NAMESPACE_12);
				if (scoType != null) {
					return scoType;
				} else {
					return SCORM12_Core.ASSET;
				}
			}
		}
		return NONE;
	}

	/**
	 * Wrapper method
	 * 
	 * @param element
	 * @param attribute
	 * @param ns
	 * @return
	 */
	public String getNamedAttributeFromElement(Element element, String attribute, Namespace ns) {
		return element.getAttributeValue(attribute, ns);
	}

	/**
	 * Return the title of the element
	 * 
	 * @param element
	 * @return
	 */
	public String getTitleOfElement(Element element) {
		if (element.getName().equals(SCORM12_Core.ORGANIZATION) || element.getName().equals(SCORM12_Core.ITEM)) {
			Element titleElement = element.getChild(SCORM12_Core.TITLE, getRootElement().getNamespace());
			if (titleElement != null) { return titleElement.getText(); }
		}
		return null;
	}

	/**
	 * A method to get an array of all the organization elements in the navigation
	 * file.
	 * 
	 * @return an array of <org> elements
	 */
	public Element[] getOrganizationList() {
		// need to work on a copy here...
		Element manifestRoot = (Element) getRootElement().clone();
		Element orgsNode = manifestRoot.getChild(SCORM12_Core.ORGANIZATIONS, getRootElement().getNamespace());
		return _scormCore.getOrganizations(orgsNode);
	}

	/**
	 * Method to change the default organization in the navigation file
	 * 
	 * @param index
	 */
	public void updateDefaultOrg(int index) {
		Element[] orgs = getOrganizationList();
		String newDefault = orgs[index].getAttributeValue(SCORM12_Core.IDENTIFIER);
		_sequencerModel.setDefaultOrg(newDefault);
		try {
			_sequencerModel.saveDocument();
		} catch (IOException ex) {
			throw new OLATRuntimeException(this.getClass(), "Could not save changes to default organization. ", ex);
		}

	}

	/**
	 * Get the launch for a particular item
	 * 
	 * @param element
	 * @return String
	 */
	public String getLaunch(Element element) {
		String url = _scormCore.getAbsoluteURL(element);
		// an item that references somthing has been found..
		if (url.startsWith("file:///")) {
			String tempHref;
			if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
				tempHref = url.substring(8, url.length());// windows
			} else {
				tempHref = url.substring(7, url.length()); // mac & linux
			}
			tempHref = tempHref.replaceAll("%20", " ");
			// String testHref =
			// ScormTomcatHandler.getSharedInstance().getScormWebAppPath().toString().replace('\\',
			// '/');
			String testHref = "bla";
			testHref = testHref.replaceAll("%20", " ");
			if (tempHref.startsWith(testHref)) {
				String localUrlMinusPath = tempHref.substring(
				// ScormTomcatHandler.getSharedInstance().getScormWebAppPath().toString().length(),
						3, tempHref.length());
				String correctLocalUrl = localUrlMinusPath.replace('\\', '/');
				url = "../.." + correctLocalUrl;
			}
		}
		return url;
	}

	/**
	 * Return the prerequisite string
	 * 
	 * @param element
	 * @return
	 */
	public String getPrerequisites(Element element) {
		if (element != null && isDocumentNamespace(element)) {
			// SCORM <prerequisite> elements
			Element prereq = element.getChild(SCORM12_Core.PREREQUISITES, SCORM12_DocumentHandler.ADLCP_NAMESPACE_12);
			if (prereq != null) {
				String script = prereq.getText();
				if (script != null && !script.equals("")) { return script; }
			}
		}
		return null;
	}

	/**
	 * Method used to check that all of the sco xml files exist on disk
	 * 
	 * @return true if all scoItem files exist
	 */
	public boolean doScoSettingsExist() {
		boolean allFound = true;
		String[] scos = getAllScoIdentifiers();
		for (int i = 0; i < scos.length; i++) {
			if (!settings.getScoDataModelFile(scos[i]).exists()) {
				allFound = false;
				break;
			}
		}
		return allFound;
	}

	/**
	 * Method to make sure the settings file is on disk.
	 * 
	 * @return true if navFile exists
	 */
	public boolean doesNavFileExist() {
		return (settings.getScoItemSequenceFile().exists());
	}
	
	public int getNumOfSCOs() {
		int count = 0;
		Element[] element = _scormCore.getElementsInManifest(getRootElement(), SCORM12_Core.ITEM, getRootElement().getNamespace());
		for (int i = 0; i < element.length; i++) {
			if (findScoType(element[i]).equals(SCORM12_Core.SCO)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Method to return all of the identifiers for scos found in a package
	 * 
	 * @return String[]
	 */
	public String[] getAllScoIdentifiers() {
		List<String> v = new ArrayList<>();
		Element[] element = _scormCore.getElementsInManifest(this.getRootElement(), SCORM12_Core.ITEM, getRootElement().getNamespace());
		for (int i = 0; i < element.length; i++) {
			if (findScoType(element[i]).equals(SCORM12_Core.SCO)) {
				v.add(element[i].getAttributeValue(SCORM12_Core.IDENTIFIER));
			}
		}
		return v.toArray(new String[v.size()]);
	}
}
