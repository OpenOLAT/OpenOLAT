/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.ims.qti21.model.xml;

import java.util.List;

import org.xml.sax.SAXParseException;

import uk.ac.ed.ph.jqtiplus.provision.BadResourceException;
import uk.ac.ed.ph.jqtiplus.reading.QtiModelBuildingError;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlInterpretationException;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlInterpretationException.InterpretationFailureReason;
import uk.ac.ed.ph.jqtiplus.xmlutils.XmlParseResult;

/**
 * 
 * Initial date: 09.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BadRessourceHelper {

	public static boolean hasFatalErrors(BadResourceException e) {
		if(e instanceof QtiXmlInterpretationException) {
        	QtiXmlInterpretationException qe = (QtiXmlInterpretationException)e;
        	if(qe.getXmlParseResult() != null) {
        		XmlParseResult result = qe.getXmlParseResult();
        		return (result.getFatalErrors() != null && result.getFatalErrors().size() > 0);
        	}
		}
		return false;
	}
	
	public static void extractMessage(BadResourceException e, StringBuilder out) {
        if(e instanceof QtiXmlInterpretationException) {
        	QtiXmlInterpretationException qe = (QtiXmlInterpretationException)e;
        	if(qe.getQtiModelBuildingErrors() != null) {
	        	for(QtiModelBuildingError error :qe.getQtiModelBuildingErrors()) {
	        		String localName = error.getElementLocalName();
	        		String msg = error.getException().getMessage();
	        		if(error.getElementLocation() != null) {
	        			int lineNumber = error.getElementLocation().getLineNumber();
	        			out.append(lineNumber + " :: " + localName + " :: " + msg + "\n");
	        		} else {
	        			out.append(localName + " :: " + msg + "\n");
	        		}
	        	}
        	}
        	
        	if(qe.getInterpretationFailureReason() != null) {
        		InterpretationFailureReason reason = qe.getInterpretationFailureReason();
        		out.append("Failure: " + reason + "\n");
        	}
        	
        	if(qe.getXmlParseResult() != null) {
        		XmlParseResult result = qe.getXmlParseResult();
        		if(result.getWarnings() != null) {
        			for(SAXParseException saxex : result.getWarnings()) {
        				int lineNumber = saxex.getLineNumber();
        				int columnNumber = saxex.getColumnNumber();
        				String msg = saxex.getMessage();
    	        		out.append("Warnings: " + lineNumber + ":" + columnNumber + " :: " + msg + "\n");
        			}
        		}
        		
        		if(result.getErrors() != null) {
        			for(SAXParseException saxex : result.getErrors()) {
        				int lineNumber = saxex.getLineNumber();
        				int columnNumber = saxex.getColumnNumber();
        				String msg = saxex.getMessage();
    	        		out.append("Error: " + lineNumber + ":" + columnNumber + " :: " + msg + "\n");
        			}
        		}
        		
        		if(result.getFatalErrors() != null) {
        			for(SAXParseException saxex : result.getFatalErrors()) {
        				int lineNumber = saxex.getLineNumber();
        				int columnNumber = saxex.getColumnNumber();
        				String msg = saxex.getMessage();
    	        		out.append("Fatal: " + lineNumber + ":" + columnNumber + " :: " + msg + "\n");
        			}
        		}
        		
        		if(result.getUnsupportedSchemaNamespaces() != null) {
        			List<String> unsupportedSchemaNamespaces = result.getUnsupportedSchemaNamespaces();
        			for(String unsupportedSchemaNamespace : unsupportedSchemaNamespaces) {
    	        		out.append("Error unsupported namespace: " + unsupportedSchemaNamespace + "\n");
        			}
        		}
        	}
        }
	}

	public static void extractMessage(XmlParseResult result, StringBuilder out) {
		if(result.getWarnings() != null) {
			for(SAXParseException saxex : result.getWarnings()) {
				int lineNumber = saxex.getLineNumber();
				int columnNumber = saxex.getColumnNumber();
				String msg = saxex.getMessage();
        		out.append("Error: " + lineNumber + ":" + columnNumber + " :: " + msg + "\n");
			}
		}
		
		if(result.getErrors() != null) {
			for(SAXParseException saxex : result.getErrors()) {
				int lineNumber = saxex.getLineNumber();
				int columnNumber = saxex.getColumnNumber();
				String msg = saxex.getMessage();
        		out.append("Error: " + lineNumber + ":" + columnNumber + " :: " + msg + "\n");
			}
		}
		
		if(result.getFatalErrors() != null) {
			for(SAXParseException saxex : result.getFatalErrors()) {
				int lineNumber = saxex.getLineNumber();
				int columnNumber = saxex.getColumnNumber();
				String msg = saxex.getMessage();
        		out.append("Fatal: " + lineNumber + ":" + columnNumber + " :: " + msg + "\n");
			}
		}
	}
}
