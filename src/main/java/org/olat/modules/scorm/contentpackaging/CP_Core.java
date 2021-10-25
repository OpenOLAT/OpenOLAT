/**
 *  RELOAD TOOLS
 *
 *  Copyright (c) 2003 Oleg Liber, Bill Olivier, Phillip Beauvoir
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *  Project Management Contact:
 *
 *  Oleg Liber
 *  Bolton Institute of Higher Education
 *  Deane Road
 *  Bolton BL3 5AB
 *  UK
 *
 *  e-mail:   o.liber@bolton.ac.uk
 *
 *
 *  Technical Contact:
 *
 *  Phillip Beauvoir
 *  e-mail:   p.beauvoir@bolton.ac.uk
 *
 *  Web:      http://www.reload.ac.uk
 *
 */

package org.olat.modules.scorm.contentpackaging;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.olat.modules.scorm.server.servermodels.XMLDocument;


/**
 * Core IMS Content Package Methods and Functionality
 *
 * @author Phillip Beauvoir
 * @version $Id: CP_Core.java,v 1.15 2004/11/06 17:44:25 phillipus Exp $
 */
public class CP_Core {

	/**
	 * The CP Manifest name
	 */
	public static final String MANIFEST_NAME = "imsmanifest.xml";

	// Element and Attribute Names
    public static final String MANIFEST = "manifest";
    public static final String ORGANIZATIONS = "organizations";
    public static final String RESOURCES = "resources";
    public static final String DEFAULT = "default";
    public static final String ORGANIZATION = "organization";
    public static final String ITEM = "item";
    public static final String PARAMETERS = "parameters";
    public static final String RESOURCE = "resource";
    public static final String BASE = "base";
    public static final String FILE = "file";
    public static final String TYPE = "type";
    public static final String HREF = "href";
    public static final String METADATA = "metadata";
    public static final String IDENTIFIER = "identifier";
    public static final String IDENTIFIERREF = "identifierref";
    public static final String STRUCTURE = "structure";
    public static final String TITLE = "title";
    public static final String DEPENDENCY = "dependency";
    public static final String VERSION = "version";
    public static final String SCHEMA = "schema";
    public static final String SCHEMAVERSION = "schemaversion";
    public static final String ISVISIBLE = "isvisible";
    
    public static final String MD_Core_ROOT_NAME = "lom";
    public static final String MD_Core_OLD_ROOT_NAME = "record";

    /**
     * The JDOM Document that forms the Content Package that we shall be working on
     */
    private XMLDocument _doc;

    /**
     * Constructor
     */
    public CP_Core(XMLDocument doc) {
        _doc = doc;
    }

    /**
     * @return the CP Root Folder which is the containing folder of imsmanifest.xml
     */
    public File getRootFolder() {
        File file = _doc.getFile();
        return file == null ? null : file.getParentFile();
    }
    
    /**
     * Destroy this Document
     */
    public void destroy() {
        _doc = null;
    }

    // =============================================================================
    // ELEMENT FINDING/NAVIGATION HANDLING
    // =============================================================================

    
    /**
     * @return The Root manifest element
     */
    public Element getRootManifestElement() {
        return _doc.getRootElement();
    }
    
    /**
     * @param sourceElement
     * @return The Element referenced by identifierref in sourceElement.
     * The search digs downward from sourceElement - this is important.
     * This will be either a Resource or a sub-Manifest.
     */
    public Element getReferencedElement(Element sourceElement) {
        String idref = sourceElement.getAttributeValue(IDENTIFIERREF);

        if(idref != null) {
            Element manifest = getParentManifestElement(sourceElement);
            if(manifest != null) {
                // Search the Resources first
                Element[] resources = getElementsInManifest(manifest, RESOURCE, _doc.getRootNamespace());
                for(int i = 0; i < resources.length; i++) {
                    String id = resources[i].getAttributeValue(IDENTIFIER);
                    if(id != null) {
                        if(idref.equals(id)) return resources[i];
                    }
                }

                // Search for a sub-Manifest
                Element[] submanifests = getElementsInManifest(manifest, MANIFEST, _doc.getRootNamespace());
                for(int i = 0; i < submanifests.length; i++) {
                    String id = submanifests[i].getAttributeValue(IDENTIFIER);
                    if(id != null && idref.equals(id)) return submanifests[i];
                }
            }
        }

        return null;
    }

    /**
     * @param element The element to test
     * @return True if element has an identifierref attribute
     */
    public boolean isReferencingElement(Element element) {
        String idref = element.getAttributeValue(IDENTIFIERREF);
        return idref != null && !idref.equals("");
    }

    /**
     * Get all available Identifiers from resources and sub-Manifests that an element can legally reference.
     * For an <item> element this will be resources and sub-manifests.
     * For a <dependency> element this will be resources, not including its parent <resource>.
     * 
     * @param element The Element that wishes to reference other Elements.
     * Must be either <item>, or <dependency> 
     * @return all available Identifiers from resources and sub-manifests that an element can legally reference.
     */
    public String[] getReferencedIdentifersAllowed(Element element) {
        List<String> v = new ArrayList<>();

        Element[] elements = getReferencedElementsAllowed(element);
        for(int i = 0; i < elements.length; i++) {
            String id = elements[i].getAttributeValue(IDENTIFIER);
            if(id != null && !id.equals("")) {
                v.add(id);
            }
        }

        return v.toArray(new String[v.size()]);
    }

    /**
     * Get all available Elements (resources and sub-manifests) that an element can legally reference.
     * For an <item> element this will be resources and sub-manifests.
     * For a <dependency> element this will be resources, not including its parent <resource>.
     * 
     * @param element The Element that wishes to reference other Elements.
     * Must be either <item>, or <dependency> 
     * @return All available Elements (Resources and sub-Manifests) that an element can legally reference.
     */
    public Element[] getReferencedElementsAllowed(Element element) {
        List<Element> v = new ArrayList<>();

        String elementName = element.getName();
        
        // Only Items and Dependency Elements allowed
        
        if(elementName.equals(ITEM)) {
            // Get Parent Manifest Element
            Element manifest = getParentManifestElement(element);
            if(manifest != null) {
                // Search the Resources first
                Element[] resources = getElementsInManifest(manifest, RESOURCE, _doc.getRootNamespace());
                for(int i = 0; i < resources.length; i++) {
                    String id = resources[i].getAttributeValue(IDENTIFIER);
                    if(id != null && !id.equals("")) {
                        v.add(resources[i]);
                    }
                }
                
                // Items can reference sub-Manifests
                Element[] submanifests = getElementsInManifest(manifest, MANIFEST, _doc.getRootNamespace());
                for(int i = 0; i < submanifests.length; i++) {
                    String id = submanifests[i].getAttributeValue(IDENTIFIER);
                    if(id != null && !id.equals("")) {
                        v.add(submanifests[i]);
                    }
                }
            }
        }
        
        else if(elementName.equals(DEPENDENCY)) {
            // Get Parent Manifest Element
            Element manifest = getParentManifestElement(element);
            if(manifest != null) {
                // Search the Resources
                Element[] resources = getElementsInManifest(manifest, RESOURCE, _doc.getRootNamespace());
                for(int i = 0; i < resources.length; i++) {
                    String id = resources[i].getAttributeValue(IDENTIFIER);
                    if(id != null && !id.equals("")) {
                        // This is a <dependency> element, so don't add its parent <resource>
                        if(!resources[i].equals(element.getParent())) {
                            v.add(resources[i]);
                        }
                    }
                }
            }
        }

        return v.toArray(new Element[v.size()]);
    }

    /**
     * @param manifestElement
     * @param elementName Name of element
     * @param ns Namespace
     * @return all sub-Elements of type elementName from a given parent Manifest
     * This does a deep recursive search.
     */
    public Element[] getElementsInManifest(Element manifestElement, String elementName, Namespace ns) {
        List<Element> v = new ArrayList<>();
        _getElementsInManifest(manifestElement, v, elementName, ns);
        return v.toArray(new Element[v.size()]);
    }

    private void _getElementsInManifest(Element parent, List<Element> v, String elementName, Namespace ns) {
        Iterator<Element> it = parent.getChildren().iterator();
        while(it.hasNext()) {
            Element child = it.next();
            if(child.getName().equals(elementName) && child.getNamespace().equals(ns)) {
                v.add(child);
            }
            _getElementsInManifest(child, v, elementName, ns);
        }
    }

    /**
     * @param element
     * @return the local parent "manifest" Element for a given Element
     * If element is the manifest, will return that
     */
    public Element getParentManifestElement(Element element) {
        while(!element.getName().equals(MANIFEST)) {
            element = element.getParentElement();
            if(element == null) return null;
        }
        return element;
    }

    /**
     * @param parent
     * @param identifier
     * @return an Element by its IDENTIFIER attribute starting at parent element
     * This will do a deep recursive search from parent Element
     */
    public Element getElementByIdentifier(Element parent, String identifier) {
        String id = parent.getAttributeValue(IDENTIFIER);
        if(id != null && id.equals(identifier)) return parent;
        
        Iterator<Element> it = parent.getChildren().iterator();
        while(it.hasNext()) {
            Element child = it.next();
            if(_doc.isDocumentNamespace(child)) {
                Element e = getElementByIdentifier(child, identifier);
                if(e != null) return e;
            }
        }

        return null;
    }

    /**
     * @param orgsElement
     * @return all ORGANIZATION elements in orgsElement
     */
    public Element[] getOrganizations(Element orgsElement) {
        List<Element> v = new ArrayList<>();

        Iterator<Element> it = orgsElement.getChildren(ORGANIZATION, orgsElement.getNamespace()).iterator();
        while(it.hasNext()) {
            Element orgElement = it.next();
            v.add(orgElement);
        }

        return v.toArray(new Element[v.size()]);
    }

    /**
     * @param orgsElement
     * @return all ORGANIZATION elements in an ORGANIZATIONS element that can be referenced
     * i.e they have IDENTIFIER attributes
     */
    public Element[] getOrganizationsAllowed(Element orgsElement) {
        List<Element> v = new ArrayList<>();

        Element[] orgs = getOrganizations(orgsElement);
        for(int i = 0; i < orgs.length; i++) {
            String id = orgs[i].getAttributeValue(IDENTIFIER);
            if(id != null && !id.equals("")) {
            	v.add(orgs[i]);
            }
        }

        return v.toArray(new Element[v.size()]);
    }

    /**
     * @param orgsElement
     * @return the default Organization for an ORGANIZATIONS element
     * If there is a DEFAULT ref in the ORGANIZATIONS element will return that
     * otherwise return first ORGANIZATION Element if found
     */
    public Element getDefaultOrganization(Element orgsElement) {
        Element element = null;

        if(orgsElement != null && orgsElement.getName().equals(ORGANIZATIONS)) {
            // Get ORGANIZATION by default attribute ref
            String defOrg = orgsElement.getAttributeValue(DEFAULT);
            if(defOrg != null) element = getElementByIdentifier(orgsElement, defOrg);
            // Not found so get first ORGANIZATION Element
            if(element == null) element = orgsElement.getChild(ORGANIZATION, orgsElement.getNamespace());
        }

        return element;
    }

    /**
     * @param name
     * @return True if name is the "lom" element name
     */
    public boolean isMetadataRoot(String name) {
        return name.equals(MD_Core_ROOT_NAME) || name.equals(MD_Core_OLD_ROOT_NAME);
    }

    /**
     * @param name
     * @return True if name is the "metadata" element
     */
    public boolean isMetadataElement(String name) {
        return name.equals(METADATA);
    }

    /**
     * @param element
     * @return The Relative URL string that an Element references
     */
    public String getRelativeURL(Element element) {
        String params = getParameters(element);
        
        // Check for www.xxx.com
        String url = getElementHREF(element);
        if(url.toLowerCase().startsWith("www")) url = "http://" + url;
        
        return url + (params == null ? "" : params);
    }
        
    /**
     * Determine ehe Absolute URL string that an Element references.  
     * This is used to get the Absolute file location of a resource.
     * @param element
     * @return The Absolute URL string that an Element references
     */
    public String getAbsoluteURL(Element element) {
        String href = getElementHREF(element);

        if(href != null) {
            // Save any parameters
            String params = getParameters(element);

            try {
                // Is it a local file for local people?
                
                // Need to get rid of %20 for this test
                String tmp = href.replaceAll("%20", " ");
                
                // Save anything to the right of '?' as params
                int index = tmp.indexOf("?");
                if(index != -1) {
                    params = tmp.substring(index);
                    tmp = tmp.substring(0, index);
                }
                
                File file = new File(getRootFolder(), tmp);
                // Yes, convert to URL
                if(file.exists()) {
                    FilePath filePath = new FilePath(file, params);
                    return filePath.getURL();
                }

                // No, so see if it's a URL
                else {
                    URL urlPath = new URL(href + (params == null ? "" : params));
                    return urlPath.toString();
                }
            }
            catch(MalformedURLException ex) {
                try {
                    // Failsafe
                    URL urlPath = new URL("http://" + href + (params == null ? "" : params));
                    return urlPath.toString();
                }
                catch(MalformedURLException ex1) {
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * @param element
     * @return An actual HREF reference from an Element's HREF taking into account xml:base
     */
    public String getElementHREF(Element element) {
        String name = element.getName();

        // If this is an ITEM OR DEPENDENCY, change element to referenced element
        if(name.equals(ITEM) || name.equals(DEPENDENCY)) {
            // Get referenced Element
            element = getReferencedElement(element);
            if(element == null) return null;
        }

        // Get the HREF
        String href = element.getAttributeValue(HREF);
        
        // Some people use backslashes  :-(
        if(href != null) {
            href = href.replace('\\', '/');
        }

        // Do we have a "base" attribute that needs to be prefixed?
    	// We need to prefix the href with this if the href is not an external URL
        // (an imported sub-manifest will add a /submanifest1 Base address at the manifest level
        // so any resources with external URLs will get mangled)
        if(href != null && GeneralUtils.isExternalURL(href) == false) {
            String base = getElementBase(element);
            if(base != null) href = base + href;
        }

        return href;
    }
    
    /**
     * @param element
     * @return An actual BASE reference for an Element taking into account xml:base
     */
    public String getElementBase(Element element) {
        String totalBase = "";
        
        do {
            String base = element.getAttributeValue(BASE, Namespace.XML_NAMESPACE);
            if(base != null) {
                totalBase = base + totalBase;
                if(base.startsWith("/") || base.startsWith("http:")) break; // Stop at absolute URL
            }
            element = element.getParentElement();
        }
        while(element != null);

        return totalBase.equals("") ? null : totalBase;
    }

    /**
     * @param element
     * @return the parameters attribute of an element
     */
    public String getParameters(Element element) {
        String params = element.getAttributeValue(PARAMETERS);

        if(params != null && !params.equals("")) {
            char c = params.charAt(0);
            if((c != '?') && (c != '#')) params = "?" + params;
        }

        return params;
    }

    /**
     * @param element
     * @return An actual File reference that an Element references.
     * If it's not a local file or does not exist, return null
     */
    public File getResourceFile(Element element) {
        String href = getElementHREF(element);
        if(href != null) {
            File file = new File(getRootFolder(), href.replaceAll("%20", " "));
            if(file.exists()) return file;
        }
        return null;
    }


    /**
     * @param manifestElement
     * @return All Existing Local Files for a given Manifest Element taking into account xml:base
     * External URLS are not included and the file has to physically exist
     */
    public File[] getResourceFiles(Element manifestElement) {
        List<File> v = new ArrayList<>();
        if(MANIFEST.equals(manifestElement.getName())) {
            _getResourceFiles(manifestElement, v);
        }
        return v.toArray(new File[v.size()]);
    }

    private void _getResourceFiles(Element manifestElement, List<File> v) {
        // Resources Element
        Element resourcesElement = manifestElement.getChild(RESOURCES, manifestElement.getNamespace());
        if(resourcesElement != null) {
            // Resource Elements
            Iterator<Element> it = resourcesElement.getChildren(RESOURCE, resourcesElement.getNamespace()).iterator();
            while(it.hasNext()) {
                Element resourceElement = it.next();
                File file = getResourceFile(resourceElement);
                if(file != null && !v.contains(file)) {
                	v.add(file);
                }
                // File Elements
                Iterator<Element> it2 = resourceElement.getChildren(FILE, resourceElement.getNamespace()).iterator();
                while(it2.hasNext()) {
                    Element fileElement = it2.next();
                    File file2 = getResourceFile(fileElement);
                    if(file2 != null && !v.contains(file2)) v.add(file2);
                }
            }
        }
        // Recurse Sub-Manifests           
        List<Element> submanifestElement = manifestElement.getChildren(MANIFEST, manifestElement.getNamespace());
        Iterator<Element> listElement = submanifestElement.iterator();
        while (listElement.hasNext()) {
        	Element asubmanifest = listElement.next();
        	_getResourceFiles(asubmanifest, v);
        }
    }

    /**
     * @param href
     * @param resourcesElement The RESOURCES Element
     * @return a Resource Element given its HREF - the first one found
     */
    public Element getResourceElementByHREF(String href, Element resourcesElement) {
        // Got to be the Resources Element
        if(RESOURCES.equals(resourcesElement.getName()) == false) return null;
        
        Iterator<Element> it = resourcesElement.getChildren(RESOURCE, resourcesElement.getNamespace()).iterator();
        while(it.hasNext()) {
            Element resource = it.next();
            String tmp = resource.getAttributeValue(HREF);
            if(tmp != null) {
                // Ignore these chars on compare
                tmp = tmp.replaceAll("%20", " ");
                href = href.replaceAll("%20", " ");
                if(tmp.equalsIgnoreCase(href)) return resource;
            }
        }
        
        return null;
    }

    /**
     * @param href
     * @param resourceElement The RESOURCE Element
     * @return a File Element given its HREF - the first one found
     */
    public Element getFileElementByHREF(String href, Element resourceElement) {
        // Got to be the Resource Element
        if(RESOURCE.equals(resourceElement.getName()) == false) return null;

        Iterator<Element> it = resourceElement.getChildren(FILE, resourceElement.getNamespace()).iterator();
        while(it.hasNext()) {
            Element file = it.next();
            String tmp = file.getAttributeValue(HREF);
            if(tmp != null) {
                // Ignore these chars on compare
                tmp = tmp.replaceAll("%20", " ");
                href = href.replaceAll("%20", " ");
                if(tmp.equalsIgnoreCase(href)) return file;
            }
        }
        return null;
    }

    /**
     * Find the local "resources" Element for a given Element position.  This is because there may
     * be more than one with sub-manifests
     * @param element The starting element position
     * @return The element if found, else null
     */
    public Element getResourcesElement(Element element) {
        if(element.getName().equals(RESOURCES)) return element;
        Element manifest = getParentManifestElement(element);
        return manifest == null ? null : manifest.getChild(RESOURCES, element.getNamespace());
    }

}