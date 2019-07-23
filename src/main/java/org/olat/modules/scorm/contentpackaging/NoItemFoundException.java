/**
 *  RELOAD TOOLS
 *
 *  Copyright (c) 2003 Oleg Liber, Bill Olivier, Phillip Beauvoir, Paul Sharples
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
 *  Paul Sharples
 *  e-mail:   p.sharples@bolton.ac.uk
 *
 *  Web:      http://www.reload.ac.uk
 *
 */

package org.olat.modules.scorm.contentpackaging;

/**
 * The NoItemFoundException Class that can be thrown for debugging<br>
 *
 * @author Paul Sharples
*/
public class NoItemFoundException extends Exception{

	private static final long serialVersionUID = -5966627685214023530L;

	public static final String CR = System.getProperty("line.separator");
    
    public static final String NO_ITEM_FOUND_MSG =
        "The package you are importing conforms to SCORM 1.2." + CR +
        "However, it appears there are no items within the " + CR +
        "manifest file. (or none that point to a resource)  " + CR +
        "This is probably because this is a resource package" + CR +
        "and not a content aggregation package.  " + CR +
        "This package will play but will be of little " + CR +
        "use, if there is no organization/item structure.";
    
    /**
     * Constructor for NoItemFoundException.
     * @param ex The parent Exception that this wraps
     */
    public NoItemFoundException(Exception ex) {
        super(ex.getMessage());
    }
    
    /**
     * Constructor for NoItemFoundException.
     * Used for creating an original Exception.
     * @param msg The Error Message to display
     */
    public NoItemFoundException(String msg) {
        super(msg);
    }
    
}

