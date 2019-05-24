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
package org.olat.core.util.filter.impl;

import org.olat.core.util.filter.Filter;
import org.owasp.html.HtmlChangeListener;

/**
 * Description:<br>
 * OWASP AntiSamy XSSFilter 
 * creates a DOM-Tree, parses it and filters everything invalid out, expect items in the policy-file
 * 
 * this is way better than trying to handle input by regexp's:
 * http://htmlparsing.icenine.ca/doku.php/#summary
 * 
 * OWASP AntiSamy docu: see http://www.owasp.org/index.php/AntiSamy
 * HTML Parser based on: http://nekohtml.sourceforge.net/
 *  
 * <P>
 * Initial Date:  30.07.2009 <br>
 * @author Roman Haag, roman.haag@frentix.com
 */
public class OWASPAntiSamyXSSFilter implements Filter {
	
	public OWASPAntiSamyXSSFilter() {
		//
	}
	
	@Override
    public String filter(String original) {
        if (original == null) {
            return null;
        }
        return OpenOLATPolicy.POLICY_DEFINITION.sanitize(original);
	}
	
    public boolean errors(String original) {
        if (original == null) {
            return false;
        }
        ChangeListener listener = new ChangeListener();
        OpenOLATPolicy.POLICY_DEFINITION.sanitize(original, listener, this);
        return listener.getErrors() > 0;
	}
    
    private static class ChangeListener implements HtmlChangeListener<OWASPAntiSamyXSSFilter> {
    	
    	private int errors = 0;
    	
    	public int getErrors() {
    		return errors;
    	}
    	
    	@Override
		public void discardedTag(OWASPAntiSamyXSSFilter context, String elementName) {
    		errors++;
		}

		@Override
		public void discardedAttributes(OWASPAntiSamyXSSFilter context, String tagName, String... attributeNames) {
			errors++;
		}
    }
}
