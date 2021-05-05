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

import org.olat.modules.scorm.server.servermodels.XMLDocument;

/**
 * Core ADL SCORM 1.2 Content Package Methods and Functionality
 *
 * @author Phillip Beauvoir
 * @version $Id: SCORM12_Core.java,v 1.3 2004/06/07 13:19:48 phillipus Exp $
 */
public class SCORM12_Core
extends CP_Core
{
    // Element and Attribute Names
    public static final String LOCATION = "location";
    public static final String PREREQUISITES = "prerequisites";
    public static final String MAXTIMEALLOWED = "maxtimeallowed";
    public static final String TIMELIMITACTION = "timelimitaction";
    public static final String DATAFROMLMS = "datafromlms";
    public static final String MASTERYSCORE = "masteryscore";
    public static final String SCORMTYPE = "scormtype";
    public static final String SCO = "sco";
    public static final String ASSET = "asset";

    public SCORM12_Core(XMLDocument doc) {
        super(doc);
    }

}