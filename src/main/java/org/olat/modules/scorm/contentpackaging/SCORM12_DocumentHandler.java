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

import java.util.Hashtable;

import org.jdom2.Document;
import org.jdom2.Namespace;
import org.olat.modules.scorm.server.servermodels.XMLUtils;


/**
 * The DocumentHandler for a SCORM 1.2 version.<br>
 * This currently supports IMS Content Packaging 1.1.2 and 1.1.3<br>
 * 
 * We don't support earlier IMS Content Packaging versions since the Schema is deprecated<br>
 *
 * @author Phillip Beauvoir
*/
public class SCORM12_DocumentHandler {
    
    protected static final String IMS_CONTENT_PACKAGING_1_1 = "IMS Content Packaging 1.1";
    protected static final String IMS_CONTENT_PACKAGING_1_1_2 = "IMS Content Packaging 1.1.2";
    protected static final String IMS_CONTENT_PACKAGING_1_1_3 = "IMS Content Packaging 1.1.3";

    protected static final String ADL_SCORM_1_3 = "ADL SCORM 1.3";
    protected static final String ADL_SCORM_1_2 = "ADL SCORM 1.2";

    // Namespace prefix
    protected static String IMSCP_NAMESPACE_PREFIX = "imscp";

    // CP Version 1.1.3
    protected static Namespace IMSCP_NAMESPACE_113 =  Namespace.getNamespace("http://www.imsglobal.org/xsd/imscp_v1p1");
    
    // CP Version 1.1.2
    protected static Namespace IMSCP_NAMESPACE_112 = Namespace.getNamespace("http://www.imsproject.org/xsd/imscp_rootv1p1p2");
    
    // CP Version 1.1 
    protected static Namespace IMSCP_NAMESPACE_11 = Namespace.getNamespace("http://www.imsproject.org/xsd/ims_cp_rootv1p1");

    /**
     * Namespace prefix for ADL SCORM
     */ 
    protected static String ADLCP_NAMESPACE_PREFIX = "adlcp";

    // ADL SCORM Version 1.2
    protected static Namespace ADLCP_NAMESPACE_12 = Namespace.getNamespace("adlcp", "http://www.adlnet.org/xsd/adlcp_rootv1p2");

    // ADL SCORM Version 1.3 - we don't support this
    protected static Namespace ADLCP_NAMESPACE_13 = Namespace.getNamespace("adlcp", "http://www.adlnet.org/xsd/adlcp_v1p3");

    static Hashtable<Namespace,String> SUPPORTED_NAMESPACES = new Hashtable<>();
    static Hashtable<Namespace,String> SUPPORTED_SCORM_NAMESPACES = new Hashtable<>();
    
    static {
		// Add to table of Supported Namespaces mapped to Friendly Names
        SUPPORTED_NAMESPACES.put(IMSCP_NAMESPACE_11, IMS_CONTENT_PACKAGING_1_1);
		SUPPORTED_NAMESPACES.put(IMSCP_NAMESPACE_112, IMS_CONTENT_PACKAGING_1_1_2);
		SUPPORTED_NAMESPACES.put(IMSCP_NAMESPACE_113, IMS_CONTENT_PACKAGING_1_1_3);
		
		// Add to table of supported SCORM Namespaces mapped to Friendly Names
		SUPPORTED_SCORM_NAMESPACES.put(ADLCP_NAMESPACE_12, ADL_SCORM_1_2);
    }

    protected static boolean canHandle(Document doc) throws DocumentHandlerException {
        // The first thing we do is to see if there is a root Namespace in the Document
		Namespace nameSpace = XMLUtils.getDocumentNamespace(doc);
		
		// No Namespace, sorry we don't know what it is!
		if(nameSpace == null || nameSpace.equals(Namespace.NO_NAMESPACE)) {
		    throw new DocumentHandlerException("No Namespace in Document so cannot determine what it is!");
		}
		
		// Does it have the correct root Namespace?
		if(SUPPORTED_NAMESPACES.containsKey(nameSpace) == false) return false;
		
		// Now find out if it is a SCORM Document and if so whether we support it
		// We'll search all elements for the ADL Namespace
        Namespace nsSCORM = getSCORM_Namespace(doc);
        if(nsSCORM == null) return false;
        
        // Do we support this version of SCORM?
        return SUPPORTED_SCORM_NAMESPACES.containsKey(nsSCORM);
	}

	/**
	 * @param doc
	 * @return The SCORM Namespace if this doc a SCORM Document - we look for the ADL Namespaces
	 * or null if not found in the Document
	 */
    protected static Namespace getSCORM_Namespace(Document doc) {
		// We'll search all elements for the ADL Namespace
		boolean found = XMLUtils.containsNamespace(doc, ADLCP_NAMESPACE_12);
		if(found) return ADLCP_NAMESPACE_12;
		
		found = XMLUtils.containsNamespace(doc, ADLCP_NAMESPACE_13);
		if(found) return ADLCP_NAMESPACE_13;
		
		return null;
	}
}
