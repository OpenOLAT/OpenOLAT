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
import java.net.URI;
import java.net.URL;

/**
 * A URL/Path representaion of a File.
 * For each platform and for each Browser there are problems.  Some OSs like to have
 * a URL such as "file:///c:/somefile" while others like Win98 just want the File path.
 * So we can wrap a File in this class and query it as we wish
 *
 * @author Phillip Beauvoir
 * @version $Id: FilePath.java,v 1.1 2004/07/08 11:37:23 phillipus Exp $
 */
class FilePath
{
	
	/**
	 * The File this represents
	 */
    private File _file;
	
	/**
	 * Any additional parameters for a File URL
	 */
    private String _params;
	
	/**
	 * Constructor for File
	 * @param file
	 */
	public FilePath(File file) {
		_file = file;
	}
	
	/**
	 * Constructor for File with additional URL-type parameters (like ?dothis or #tag)
	 * @param file
	 * @param params
	 */
	public FilePath(File file, String params) {
		_file = file;
		_params = params;
	}
	
	/**
	 * Mac and Linux absolute file paths begin with "/"
	 * i.e /Users/fredbloggs/packages/index.htm
	 * so simply prepending "file:///" to obtain a valid URL will not work because
	 * we will end up with four forward slashes, which is an illegal URL
	 * This method examines the file reference and if it starts with a
	 * forward slash prepends "file://" instead of "file:///" and returns
	 * the String result.
	 * If the file represents a Windows networked path then the format will be "file:///\\Network%20Path\path\file.txt"
	 * @return a valid local URL reference as a string in the form "file:///C:/Hello%20There/file.txt"
	 */
	public String getURL() {
	    String path = null;
	    
	    URI uri = _file.toURI();
	    
	    try {
            URL url = uri.toURL();
            path = url.getPath();
        }
        catch(MalformedURLException ex) {
            ex.printStackTrace();
        }
        
		// Params
		if(_params != null) {
		    path = path + _params;
		}

	    // Check for networked path "\\"
	    if(_file.getAbsolutePath().startsWith("\\\\")) {
	        path = path.replaceAll("/", "\\\\");
	    }

	    if(path.startsWith("/")) {
		    return "file://" + path;
		}
		else {
			return "file:///" + path;
		}
	}
	
//	/**
//	 * Old method - not used
//	 */
//	public String getURL() {
//		String path = _file.getAbsolutePath();
//		
//		// A Mac needs this
//		path = path.replaceAll(" ", "%20");
//		
//		path = path.replace(File.separatorChar, '/');
//		
//		// Params
//		if(_params != null) {
//		    path = path + _params;
//		}
//		
//		if(!path.endsWith("/") && _file.isDirectory()) {
//			path = path + "/";
//		}
//		
//		if(path.startsWith("/")) {
//		    return "file://" + path;
//		}
//		else {
//			return "file:///" + path;
//		}
//	}
	
	/**
	 * @return the File as a String Path
	 */
	public String getPath() {
		String path = _file.getAbsolutePath();
		return path;
	}
}
