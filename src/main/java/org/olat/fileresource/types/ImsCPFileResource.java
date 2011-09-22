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
* <p>
*/ 

package org.olat.fileresource.types;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.olat.ims.resources.IMSLoader;

/**
 * Initial Date:  Apr 6, 2004
 *
 * @author Mike Stock
 */
public class ImsCPFileResource extends FileResource {

	/**
	 * IMS CP file resource identifier.
	 */
	public static final String TYPE_NAME = "FileResource.IMSCP";

	/**
	 * Standard constructor.
	 */
	public ImsCPFileResource() { super.setTypeName(TYPE_NAME); }
	
	/**
	 * Check for title and at least one resource.
	 * @param unzippedDir
	 * @return True if is of type.
	 */
	public static boolean validate(File unzippedDir) throws AddingResourceException {
		File fManifest = new File(unzippedDir, "imsmanifest.xml");
		Document doc = IMSLoader.loadIMSDocument(fManifest);
		//do not throw exception already here, as it might be only a generic zip file
		if (doc == null) return false;

		// get all organization elements. need to set namespace
		Element rootElement = doc.getRootElement();
		String nsuri = rootElement.getNamespace().getURI();
		Map nsuris = new HashMap(1);
		nsuris.put("ns", nsuri);

		// Check for organiztaion element. Must provide at least one... title gets ectracted from either
		// the (optional) <title> element or the mandatory identifier attribute.
		// This makes sure, at least a root node gets created in CPManifestTreeModel.
		XPath meta = rootElement.createXPath("//ns:organization");
		meta.setNamespaceURIs(nsuris);
		Element orgaEl = (Element) meta.selectSingleNode(rootElement); // TODO: accept several organizations?
		if (orgaEl == null) throw new AddingResourceException("resource.no.organisation");

		// Check for at least one <item> element referencing a <resource>, which will serve as an entry point.
		// This is mandatory, as we need an entry point as the user has the option of setting
		// CPDisplayController to not display a menu at all, in which case the first <item>/<resource>
		// element pair gets displayed.
		XPath resourcesXPath = rootElement.createXPath("//ns:resources");
		resourcesXPath.setNamespaceURIs(nsuris);
		Element elResources = (Element)resourcesXPath.selectSingleNode(rootElement);
		if (elResources == null) throw new AddingResourceException("resource.no.resource"); // no <resources> element.
		XPath itemsXPath = rootElement.createXPath("//ns:item");
		itemsXPath.setNamespaceURIs(nsuris);
		List items = itemsXPath.selectNodes(rootElement);
		if (items.size() == 0) throw new AddingResourceException("resource.no.item"); // no <item> element.
		for (Iterator iter = items.iterator(); iter.hasNext();) {
			Element item = (Element) iter.next();
			String identifierref = item.attributeValue("identifierref");
			if (identifierref == null) continue;
			XPath resourceXPath = rootElement.createXPath("//ns:resource[@identifier='" + identifierref + "']");
			resourceXPath.setNamespaceURIs(nsuris);
			Element elResource = (Element)resourceXPath.selectSingleNode(elResources);
			if (elResource == null) throw new AddingResourceException("resource.no.matching.resource");
			if (elResource.attribute("scormtype") != null) return false;
			if (elResource.attribute("scormType") != null) return false;
			if (elResource.attribute("SCORMTYPE") != null) return false;
			if (elResource.attributeValue("href") != null) return true; // success.
		}
		return false;
		//throw new AddingResourceException("resource.general.error");
	}
}
