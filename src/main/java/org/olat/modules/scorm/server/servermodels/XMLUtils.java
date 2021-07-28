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

package org.olat.modules.scorm.server.servermodels;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


/**
 * Some useful XML Utilities that leverage the JDOM Package<br>
 *
 * @author Phillip Beauvoir
 * @version $Id: XMLUtils.java,v 1.2 2004/12/09 10:36:56 phillipus Exp $
 */
public final class XMLUtils {
	
    /**
     * The XSI Namespace
     */
    public static Namespace XSI_Namespace = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

    /**
     * The Old XSI Namespace
     */
    public static Namespace XSI_NamespaceOLD = Namespace.getNamespace("xsi", "http://www.w3.org/2000/10/XMLSchema-instance");

    /**
     * The schemaLocation String
     */
    public static String XSI_SchemaLocation = "schemaLocation";
	
	/**
	 * Writes a JDOM Document to file
	 * @param doc The JDOM Document to write
	 * @param file The file to write to
	 * @throws IOException
	 */
    public static void write2XMLFile(Document doc, File file) throws IOException {
		// This gets rid of junk characters
		Format format = Format.getCompactFormat();
		format.setIndent("  ");
		XMLOutputter outputter = new XMLOutputter(format);
		
		// Create parent folder if it doesn't exist
		File parent = file.getParentFile();
		if(parent != null) {
		    parent.mkdirs();
		}
		
		FileOutputStream out = new FileOutputStream(file);
		outputter.output(doc, out);
		out.close();
	}
	
	/**
	 * Reads and returns a JDOM Document from file without Schema Validation
	 * @param file The XML File
	 * @return The JDOM Document or null if not found
	 * @throws FileNotFoundException
	 * @throws JDOMException
	 * @throws IOException
	 */
	public static Document readXMLFile(File file) throws IOException, JDOMException {
		Document doc = null;
		SAXBuilder builder = new SAXBuilder();
		builder.setExpandEntities(false);
		// This allows UNC mapped locations to load
		doc = builder.build(new FileInputStream(file));
		return doc;
	}
    
    /**
     * @return The root Namespace in the Document or null if not found
     */
    public static Namespace getDocumentNamespace(Document doc) {
        Namespace ns = null;
        if(doc.hasRootElement()) {
            ns = doc.getRootElement().getNamespace();
        }
        return ns;
    }
    
    /**
     * Hunt for a Namespace in the Document searching all additional Namespaces and
     * Elements in case the Namespace is declared "in-line" at the Element level
     * @param doc
     * @param ns
     * @return true if found
     */
    public static boolean containsNamespace(Document doc, Namespace ns) {
        return containsNamespace(doc.getRootElement(), ns);
    }
    
    /**
     * Hunt for a Namespace in the Element searching all sub-Elements in case the Namespace
     * is declared "in-line" at the Element level
     * @param element
     * @param ns
     * @return true if found
     */
    private static boolean containsNamespace(Element element, Namespace ns) {
        // Element Namespace?
        if(ns.equals(element.getNamespace())) {
            return true;
        }

        // Additional Namespace?
        Iterator it = element.getAdditionalNamespaces().iterator();
        while(it.hasNext()) {
            Namespace ns1 = (Namespace)it.next();
            if(ns1.equals(ns)) {
                return true;
            }
        }

        // Recurse children
        Iterator i = element.getChildren().iterator();
        while(i.hasNext()) {
            Element child = (Element) i.next();
            boolean found = containsNamespace(child, ns);
            if(found) {
                return true;
            }
        }
        
        return false;
    }
}