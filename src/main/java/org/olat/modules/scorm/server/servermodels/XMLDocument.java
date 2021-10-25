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
import java.io.IOException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;




/**
 * This encapsulates the JDOM XML Document file plus helper methods.
 *
 * @author Phillip Beauvoir
 * @version $Id: XMLDocument.java,v 1.4 2004/11/18 19:36:23 phillipus Exp $
 */
public class XMLDocument {
    /**
     * The JDOM Document
     */
    private Document _doc;

    /**
     * A flag to set if this Document is dirty (edited)
     */
    private boolean _dirty;

    /**
     * The File for this Document - this may be null if not saved to disk
     */
    private File _file;


    /**
     * Default Constructor
     */
    public XMLDocument() { }
        
    /**
     * Constructor
     */
    public XMLDocument(Document doc) {
        _doc = doc;
    }
    
    /**
     * Set the Document
     * @param doc
     */
    public void setDocument(Document doc) {
        _doc = doc;
    }

    /**
     * @return The JDOM Document
     */
    public Document getDocument() {
        return _doc;
    }
    
    /**
     * Set File
     * @param file
     */
    public void setFile(File file) {
        _file = file;
    }

    /**
     * @return the File of this Document.  This might be null.
     */
    public File getFile() {
        return _file;
    }

    /**
     * Load the JDOM Document File
     * @param file The XML File to read in
     * @throws JDOMException
     * @throws IOException
     */
    public void loadDocument(File file) throws JDOMException, IOException {
        _file = file;
        _doc = XMLUtils.readXMLFile(file);
        _dirty = false;
    }

    /**
     * This will save the XML IMS file with the existing File ref.
     * @throws IOException 
     */
    public void saveDocument() throws IOException {
    	if(_doc != null && _file != null) {
    		XMLUtils.write2XMLFile(_doc, _file);
    		_dirty = false;
    	}
    }

    /**
     * @return whether this Document has been changed in some way
     */
    public boolean isDirty() {
        return _dirty;
    }

    /**
     * Set whether this Document is dirty or not
     * @param isDirty true or false
     */
    public void setDirty(boolean isDirty) {
        _dirty = isDirty;
    }

    /**
     * This will save the XML IMS file with a new File ref
     * @param file The File to save as
     * @throws IOException 
     */
    public void saveAsDocument(File file) throws IOException {
        _file = file;
        saveDocument();
    }

    /**
     * @return The Root Element of the JDOM Document or null if none
     */
    public Element getRootElement() {
        if(_doc != null && _doc.hasRootElement()) {
            return _doc.getRootElement();
        }
        return null;
    }

    /**
     * @return the Root Namespace of the Document if there is one
     */
    public Namespace getRootNamespace() {
        Element root = getRootElement();
        if(root != null) {
            return root.getNamespace();
        }
        else return null;
    }

    /**
     * @return true if element belongs to the Namespace of this Document
     */
    public boolean isDocumentNamespace(Element element) {
        Namespace ns = element.getNamespace();
        if(ns == null) {
            return false;
        }
        return ns.equals(getRootNamespace());
    }

    /**
     * @return the index position of an Element in relation to its parent
     */
    public int indexOfElement(Element element) {
        int index = 0;
        Element parent = element.getParentElement();
        if(parent != null) {
            index = parent.getChildren().indexOf(element);
            if(index == -1) {
                index = 0;
            }
        }
        return index;
    }
    
    /**
     * @param element
     * @return The previous sibling Element of the same name and Namespace as element or null
     */
    public Element getPreviousSiblingSameType(Element element) {
        Element prevSibling = element;
        
        while((prevSibling = getPreviousSibling(prevSibling)) != null) {
            if(element.getName().equals(prevSibling.getName()) && 
                    element.getNamespace().equals(prevSibling.getNamespace()))
            {
                return prevSibling;
            }
        }
        
        return null;
    }
    
    /**
     * @param element
     * @return The previous sibling Element of element or null if there isn't one
     */
    public Element getPreviousSibling(Element element) {
        if(element == null) {
            return null;
        }
        
        Element parent = element.getParentElement();
        if(parent == null) {
            return null;
        }
        
        int index = indexOfElement(element);
        
        // First one
        if(index < 1) {
            return null;
        }
        
        return parent.getChildren().get(index - 1);
    }

    /**
     * @param element
     * @return The next sibling Element of the same name and Namespace as element or null
     */
    public Element getNextSiblingSameType(Element element) {
        Element nextSibling = element;
        
        while((nextSibling = getNextSibling(nextSibling)) != null) {
            if(element.getName().equals(nextSibling.getName()) && 
                    element.getNamespace().equals(nextSibling.getNamespace()))
            {
                return nextSibling;
            }
        }
        
        return null;
    }
    
    /**
     * @param element
     * @return The next sibling Element of element or null if there isn't one
     */
    public Element getNextSibling(Element element) {
        if(element == null) {
            return null;
        }
        
        Element parent = element.getParentElement();
        if(parent == null) {
            return null;
        }
        
        int index = indexOfElement(element);
        
        // Last one
        if(index == parent.getChildren().size() - 1) {
            return null;
        }
        
        return parent.getChildren().get(index + 1);
    }

    /**
     * Destroy this Document
     */
    public void destroy() {
        _doc = null;
    }
}
