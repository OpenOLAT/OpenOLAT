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

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.vfs.VFSManager;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;

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
	
	private static final OLog log = Tracing.createLoggerFor(OWASPAntiSamyXSSFilter.class);

	//to be found in /_resources
	private static final String POLICY_FILE = "antisamy-tinymce.xml";
	private static final String WIKI_POLICY_FILE = "antisamy-wiki.xml";
	private static boolean jUnitDebug;
	private CleanResults cr;
	private final int maxLength;
	private final Variant variant;
	private final boolean entityEncodeIntlChars;
	
	private static Policy tinyMcePolicy;
	private static Policy internalionalTinyMcePolicy;
	private static Policy wikiPolicy;
	private static Policy internalionalWikiPolicy;
	
	static {
		String fPath = VFSManager.sanitizePath(OWASPAntiSamyXSSFilter.class.getPackage().getName());
		fPath = fPath.replace('.', '/');
		String tinyPath = fPath + "/_resources/" + POLICY_FILE;
		try(InputStream inStream = OWASPAntiSamyXSSFilter.class.getResourceAsStream(tinyPath)) {
			tinyMcePolicy = Policy.getInstance(inStream);
			internalionalTinyMcePolicy = tinyMcePolicy.cloneWithDirective("entityEncodeIntlChars", "false");
		} catch (Exception e) {
			log.error("", e);
		}
		
		String wikiPath = fPath + "/_resources/" + WIKI_POLICY_FILE;
		try(InputStream inStream = OWASPAntiSamyXSSFilter.class.getResourceAsStream(wikiPath)) {
			wikiPolicy = Policy.getInstance(inStream);
			internalionalWikiPolicy = wikiPolicy.cloneWithDirective("entityEncodeIntlChars", "false");
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	public OWASPAntiSamyXSSFilter(){
		this(-1, true, Variant.tinyMce, false);
	}

	/**
	 * @param maxLength
	 * @param junitDebug
	 */
	public OWASPAntiSamyXSSFilter(int maxLength, boolean junitDebug){
		this(maxLength, true, Variant.tinyMce, junitDebug);
	}
	
	public OWASPAntiSamyXSSFilter(int maxLength, boolean entityEncodeIntlChars, Variant variant, boolean junitDebug){
		OWASPAntiSamyXSSFilter.jUnitDebug = junitDebug;
		this.variant = variant;
		this.maxLength = maxLength;
		this.entityEncodeIntlChars = entityEncodeIntlChars;
	}
	
	@Override
    public String filter(String original) {
        if (original == null) {
            if (log.isDebug()) log.debug("  Filter-Input was null, is this intended?", null);
            return null;
        }
        String output = getCleanHTML(original);
        if (original.equals(output)) {
        	// works
		} else {
			String errMsg = getOrPrintErrorMessages();
			if (!errMsg.equals("")) {
				log.warn(" Filter applied! => message from filter, check if this should not be allowed: " + errMsg, null);
				log.info(" Original Input: \n" + original, null);
				log.info(" Filter Result: \n" +  output, null);
			} else {
				log.debug(" Filter result doesn't match input! / no message from filter! maybe only some formatting differences.", null);
			}
		}
		return output;
	}

	private void printOriginStackTrace() {
		// use stacktrace to find out more where the filter was used
		OLATRuntimeException ore = new OLATRuntimeException("XSSFilter dummy", null);
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		ore.printStackTrace(printWriter);
	}
	
	private String getCleanHTML(String original)	{
		Policy policy;
		if(variant == Variant.wiki) {
			if(entityEncodeIntlChars) {
				policy = wikiPolicy;
			} else {
				policy = internalionalWikiPolicy;
			}
		} else {
			if(entityEncodeIntlChars) {
				policy = tinyMcePolicy;
			} else {
				policy = internalionalTinyMcePolicy;
			}
		}
		
		if(maxLength > 0) {
			policy = policy.cloneWithDirective("maxInputSize", Integer.toString(maxLength));
		}

		AntiSamy as = new AntiSamy();
		cr = null;
		try {
			cr = as.scan(original, policy);
		} catch (ScanException e) {
			log.error("XSS Filter scan error", e);
			printOriginStackTrace();
		} catch (PolicyException e) {
            log.error("XSS Filter policy error", e);
            printOriginStackTrace();
        } 
        String output; 
        try {
            output = cr.getCleanHTML();
        } catch (Exception | Error e){
            output = "";
            log.error("Error getting cleaned HTML from string::" + original, e);
        }
        if (jUnitDebug) System.out.println("OWASP-AntiSamy-Outp: " + output);
        getOrPrintErrorMessages();
        if (jUnitDebug) System.out.println("OWASP-ParseTime:                    " + cr.getScanTime());
		
		return output;
	}
	
	public int getNumOfErrors() {
		if (cr != null) {
			return cr.getNumberOfErrors();
		}
		return -1;
	}

	/**
	 * get Errors/Messages from filter. 
	 * This have not to be "errors", its what has been filtered and gets reported.
	 * @return
	 */
	public String getOrPrintErrorMessages(){
		String errors = "";
		if (cr!=null){
			if (cr.getNumberOfErrors()!=0) {
				errors = "OWASP-Errors: " + cr.getErrorMessages();
				if (jUnitDebug) System.out.println(errors);
			}
		}
		return errors;
	}
	
	public enum Variant {
		tinyMce,
		wiki
		
	}
}
