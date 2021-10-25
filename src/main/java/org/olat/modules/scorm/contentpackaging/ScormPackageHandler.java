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


import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.modules.scorm.SettingsHandler;
import org.olat.modules.scorm.server.servermodels.CMI_DataModel;
import org.olat.modules.scorm.server.servermodels.SequencerModel;
import org.olat.modules.scorm.server.servermodels.XMLDocument;


/**
 * The ScormPackageHandler Class. A class used to parse a scorm imsmanifest.xml
 * file and build a xml file for each sco encountered. Each item is examined and
 * a decision is made about whether or not to generate a sco cmi data model.
 * 
 * @author Paul Sharples
 */
public class ScormPackageHandler extends XMLDocument {
	protected static final String ARCHIVE_EXTENSION = ".zip";
	// public static final String XML_EXTENSION = ".xml";

	/**
	 * A xml file to hold the state of the course - which items have been
	 * completed etc.
	 */
	protected SequencerModel _sequencerModel;

	/**
	 * A var to flag if any item were found in the manifest. If there are none
	 * found then this package cannot be played and/or is a resource package.
	 */
	public boolean _hasItemsToPlay = false;

	/**
	 * Default org id
	 */
	protected String _currentOrgId;

	/**
	 * Our instance of core scorm methods
	 */
	protected SCORM12_Core _scormCore;

	private SettingsHandler settings;
	
	/**
	 * Default Constructor
	 * 
	 * @param manifest
	 * @throws JDOMException
	 * @throws IOException
	 */
	public ScormPackageHandler(SettingsHandler settings) throws JDOMException, IOException {
		this.settings = settings;
		// Load the Document
		loadDocument(settings.getManifestFile());
		_sequencerModel = new SequencerModel(new File(settings.getScoItemSequenceFilePath()));
		_scormCore = new SCORM12_Core(this);
	}

	/**
	 * A class to actually do the bulk of the work. It creates an xml file
	 * representing the organizations, similar to the imsmanifest, but also
	 * modelling the sco/asset attributes needed by the runtime system - ie order
	 * of sequence, launch URL...
	 * 
	 * @throws NoItemFoundException
	 */
	public void buildSettings() throws NoItemFoundException {
		// get the root element of the manifest
		// NOTE: CLONE IT first- must work on a copy of the original JDOM doc.
		Element manifestRoot = getDocument().getRootElement().clone();
		_sequencerModel.setManifestModifiedDate(super.getFile().lastModified());
		// now get the organizations node
		Element orgs = manifestRoot.getChild(CP_Core.ORGANIZATIONS, manifestRoot.getNamespace());
		// get the identifier for the default organization
		Element defaultOrgNode = getDefaultOrganization(orgs);
		if (defaultOrgNode != null) {
			// and store the default identifier
			String defaultOrgIdentifier = defaultOrgNode.getAttributeValue(CP_Core.IDENTIFIER);
			// set the default organization
			_sequencerModel.setDefaultOrg(defaultOrgIdentifier);
			iterateThruManifest(manifestRoot);
		} else {
			_sequencerModel.setDefaultOrg("");
		}
		try {
			_sequencerModel.saveDocument(true);
		} catch (IOException ex) {
			throw new OLATRuntimeException(this.getClass(), "Could not save package status.", ex);
		}
		// throw an exception if no items were found in the manifest
		if (!_hasItemsToPlay) { throw new NoItemFoundException(NoItemFoundException.NO_ITEM_FOUND_MSG); }
	}

	/**
	 * A method to read through the imsmanifest and build our JDOM model in memory
	 * representing our navigation file.
	 * 
	 * @param element
	 */
	public void iterateThruManifest(Element element) {
		String name = element.getName();
		if (name.equals(CP_Core.ORGANIZATION) && isDocumentNamespace(element)) {
			_currentOrgId = element.getAttributeValue(CP_Core.IDENTIFIER);
		}
		if (name.equals(CP_Core.ITEM) && isDocumentNamespace(element)) {
			String id = element.getAttributeValue(CP_Core.IDENTIFIER);
			String url = "";
			String scoType = "";
			Element ref_element = getReferencedElement(element);
			if (ref_element != null) {
				String ref_name = ref_element.getName();
				// A RESOURCE
				if (ref_name.equals(CP_Core.RESOURCE)) {
					// get the sco type
					String theScoType = ref_element.getAttributeValue("scormtype", SCORM12_DocumentHandler.ADLCP_NAMESPACE_12);
					if (theScoType != null) {
						scoType = theScoType;
					}
					boolean isVisible = true;
					// check that the item is not hidden
					String isVisibleAttrib = element.getAttributeValue(CP_Core.ISVISIBLE);
					if (isVisibleAttrib != null) {
						if (isVisibleAttrib.equals("false")) {
							isVisible = false;
						}
					}
					if (!isVisible) {
						// add this item to the tracking xml file
						_sequencerModel.addTrackedItem(id, _currentOrgId, SequencerModel.ITEM_COMPLETED);
					} else {
						_sequencerModel.addTrackedItem(id, _currentOrgId, SequencerModel.ITEM_NOT_ATTEMPTED);
					}

					url = getAbsoluteURL(element);
					// an item that references somthing has been found..
					_hasItemsToPlay = true;
					if (url.startsWith("file:///")) {
						String tempHref;
						if (GeneralUtils.getOS() == GeneralUtils.MACINTOSH || GeneralUtils.getOS() == GeneralUtils.UNIX) {
							tempHref = url.substring(7, url.length());// mac & linux
						} else {
							tempHref = url.substring(8, url.length()); // windows
						}
						tempHref = tempHref.replaceAll("%20", " ");
						// String testHref =
						// ScormTomcatHandler.getSharedInstance().getScormWebAppPath().toString().replace('\\',
						// '/');
						String testHref = "bla";
						testHref = testHref.replaceAll("%20", " ");

						if (tempHref.startsWith(testHref)) {
							String localUrlMinusPath = tempHref.substring(
							// ScormTomcatHandler.getSharedInstance().getScormWebAppPath().toString().length()+1,
									3, tempHref.length());
							String correctLocalUrl = localUrlMinusPath.replace('\\', '/');
							url = correctLocalUrl;
						}
					}
				}

				// A sub-MANIFEST
				else if (ref_name.equals(CP_Core.MANIFEST)) {
					// Get ORGANIZATIONS Element
					Element orgsElement = ref_element.getChild(CP_Core.ORGANIZATIONS, ref_element.getNamespace());
					// Now we have to get the default ORGANIZATION
					if (orgsElement != null) ref_element = getDefaultOrganization(orgsElement);

					// Get the children of the referenced <organization> element and graft
					// clones
					if (ref_element != null) {
						Iterator<Element> it = ref_element.getChildren().iterator();
						while (it.hasNext()) {
							Element ref_child = it.next();
							element.addContent(ref_child.clone());
						}
					}
				}

			}

			// next we need to find any MAXTIMEALLOWED entries
			String maxTimeText = "";
			Element maxTime = element.getChild(SCORM12_Core.MAXTIMEALLOWED, SCORM12_DocumentHandler.ADLCP_NAMESPACE_12);
			if (maxTime != null) {
				maxTimeText = maxTime.getText();
			}

			// next find any TIMELIMITACTION entries
			String timeLimitText = "";
			Element timeLimit = element.getChild(SCORM12_Core.TIMELIMITACTION, SCORM12_DocumentHandler.ADLCP_NAMESPACE_12);
			if (timeLimit != null) {
				timeLimitText = timeLimit.getText();
			}

			// next find any DATAFROMLMS entries
			String datafromLmsText = "";
			Element dataFromLms = element.getChild(SCORM12_Core.DATAFROMLMS, SCORM12_DocumentHandler.ADLCP_NAMESPACE_12);
			if (dataFromLms != null) {
				datafromLmsText = dataFromLms.getText();
			}

			// next find any MASTERYSCORE entries
			String masteryScoreText = "";
			Element masteryScore = element.getChild(SCORM12_Core.MASTERYSCORE, SCORM12_DocumentHandler.ADLCP_NAMESPACE_12);
			if (masteryScore != null) {
				masteryScoreText = masteryScore.getText();
			}

			// if the scoType is "sco", then we have to generate our CMI model
			// for it...
			if (scoType.equals(SCORM12_Core.SCO)) {
				CMI_DataModel scoModel = new CMI_DataModel(settings.getStudentId(), settings.getStudentName(), maxTimeText, timeLimitText,
						datafromLmsText, masteryScoreText, settings.getLessonMode(), settings.getCreditMode());

				scoModel.buildFreshModel();
				Document theModel = scoModel.getModel();
				File scoFile = settings.getScoDataModelFile(id);
				scoFile.getParentFile().mkdirs();
				scoModel.setDocument(theModel);
				scoModel.setFile(scoFile);
				try {
					scoModel.saveDocument();
				} catch (IOException ex) {
					throw new OLATRuntimeException(this.getClass(), "Could not save sco settings.", ex);					
				}
			}
		}
		Iterator<Element> it = element.getChildren().iterator();
		while (it.hasNext()) {
			Element child = it.next();
			iterateThruManifest(child);
		}

	}

	/**
	 * getDefaultOrganization - wraps the same method found in SCORM1_2Core.
	 * 
	 * @param orgs
	 * @return - the JDOM element representing the default organization
	 */
	public Element getDefaultOrganization(Element orgs) {
		return _scormCore.getDefaultOrganization(orgs);
	}

	/**
	 * getReferencedElement - wraps the same method found in SCORM1_2Core.
	 * 
	 * @param sourceElement
	 * @return - the JDOM element representing the resource
	 */
	public Element getReferencedElement(Element sourceElement) {
		return _scormCore.getReferencedElement(sourceElement);
	}

	/**
	 * @return The Absolute URL string that an Element references
	 */
	public String getAbsoluteURL(Element element) {
		return _scormCore.getAbsoluteURL(element);
	}

	/**
	 * @return True if this is a Manifest that we can handle
	 */
	public boolean isSCORM12Manifest() throws DocumentHandlerException {
		// Has to be a CP Package with a SCORM 1.2 Namespace in there
		return SCORM12_DocumentHandler.canHandle(getDocument());
	}

	/**
	 * This method checks if the Scorm package has been changed by comparing the
	 * lastModified value of the Scorms "ismanifest" file with the value saved in
	 * the reload-settings.xml.
	 */
	public boolean checkIfScormPackageHasChanged() {
		boolean check = false;
		if (_sequencerModel != null && _sequencerModel.getManifestModifiedDate() != null) {
			if (Long.valueOf(_sequencerModel.getManifestModifiedDate()) != super.getFile().lastModified()) {
				check = true;
			}
		}
		return check;
	}

}
