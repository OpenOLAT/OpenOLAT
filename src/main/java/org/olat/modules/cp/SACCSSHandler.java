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

package org.olat.modules.cp;

import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;

/**
 * 
 * Description:<br>
 * Rewrite relative path in the CSS. this is a document handler for
 * a SAC Parser
 * 
 * <P>
 * Initial Date:  18 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
//fxdiff VCRP-14: print cp
public class SACCSSHandler implements DocumentHandler {

	private boolean isInline;
	private boolean selectorOpen;
	private StringBuilder styleSheet = new StringBuilder();
	
	private final String relativePath;
	private final String baseUri;
	private final VFSContainer rootContainer;
	
	public SACCSSHandler(VFSLeaf document, VFSContainer rootContainer, String baseUri) {
		this.baseUri = baseUri;
		this.rootContainer = rootContainer;
		relativePath = getRelativeResultingPath(document.getParentContainer());
	}
	
	public String getCleanStylesheet() {
		// Always ensure results contain most recent generation of stylesheet
		return styleSheet.toString();
	}

	@Override
	public void comment(String comment) throws CSSException {
		//
	}
	
	@Override
	public void startDocument(InputSource source) throws CSSException {
		// no-op
	}

	@Override
	public void endDocument(InputSource source) throws CSSException {
		// no-op
	}
	
	@Override
	public void importStyle(String uri, SACMediaList media, String defaultNamespaceURI) throws CSSException {
		//
	}
	
	@Override
	public void startFontFace() throws CSSException {
		// CSS2 Font Face declaration - ignore this for now
	}

	@Override
	public void endFontFace() throws CSSException {
		// CSS2 Font Face declaration - ignore this for now
	}
	
	@Override
	public void startMedia(SACMediaList media) throws CSSException {
		// CSS2 Media declaration - ignore this for now
	}

	@Override
	public void endMedia(SACMediaList media) throws CSSException {
		// CSS2 Media declaration - ignore this for now
	}
	
	@Override
	public void startPage(String name, String pseudo_page) throws CSSException {
		// CSS2 Page declaration - ignore this for now
	}

	@Override
	public void endPage(String name, String pseudo_page) throws CSSException {
		// CSS2 Page declaration - ignore this for now
	}
	
	@Override
	public void startSelector(SelectorList selectors) throws CSSException {
		// keep track of number of valid selectors from this rule
		int selectorCount = 0;

		// check each selector
		for (int i = 0; i < selectors.getLength(); i++) {
			Selector selector = selectors.item(i);
			if (selector != null) {
				String selectorName = selector.toString();
				if (selectorCount > 0) {
					styleSheet.append(',');
					styleSheet.append(' ');
				}
				styleSheet.append(selectorName);
				selectorCount++;
			} 
		}

		// if and only if there were selectors that were valid, append
		// appropriate open brace and set state to within selector
		if (selectorCount > 0) {
			styleSheet.append(' ');
			styleSheet.append('{');
			styleSheet.append('\n');
			selectorOpen = true;
		}
	}

	@Override
	public void endSelector(SelectorList selectors) throws CSSException {
		// if we are in a state within a selector, close brace
		if (selectorOpen) {
			styleSheet.append('}');
			styleSheet.append('\n');
		}

		// reset state
		selectorOpen = false;
	}
	
	@Override
	public void property(String name, LexicalUnit value, boolean important) throws CSSException {
		if (!selectorOpen && !isInline) {
			return;
		}

		// validate the property
		if (!isInline) { 
			styleSheet.append('\t');
		}
		styleSheet.append(name);
		styleSheet.append(':');

		// append all values
		while (value != null) {
			styleSheet.append(' ');
			styleSheet.append(lexicalValueToString(value));
			value = value.getNextLexicalUnit();
		}
		styleSheet.append(';');
		if (!isInline) { 
			styleSheet.append('\n'); 
		}
	}

	@Override
	public void ignorableAtRule(String atRule) throws CSSException {
		// this method is called when the parser hits an unrecognized
		// @-rule. Like the page/media/font declarations, this is
		// CSS2+ stuff
	}

	@Override
	public void namespaceDeclaration(String prefix, String uri) throws CSSException {
		// CSS3 - Namespace declaration - ignore for now
	}
	
	/**
	 * Converts the given lexical unit to a <code>String</code>
	 * representation. This method does not perform any validation - it is meant
	 * to be used in conjunction with the validator/logging methods.
	 * 
	 * @param lu
	 *            the lexical unit to convert
	 * @return a <code>String</code> representation of the given lexical unit
	 */
	public String lexicalValueToString(LexicalUnit lu) {
		switch (lu.getLexicalUnitType()) {
		case LexicalUnit.SAC_PERCENTAGE:
		case LexicalUnit.SAC_DIMENSION:
		case LexicalUnit.SAC_EM:
		case LexicalUnit.SAC_EX:
		case LexicalUnit.SAC_PIXEL:
		case LexicalUnit.SAC_INCH:
		case LexicalUnit.SAC_CENTIMETER:
		case LexicalUnit.SAC_MILLIMETER:
		case LexicalUnit.SAC_POINT:
		case LexicalUnit.SAC_PICA:
		case LexicalUnit.SAC_DEGREE:
		case LexicalUnit.SAC_GRADIAN:
		case LexicalUnit.SAC_RADIAN:
		case LexicalUnit.SAC_MILLISECOND:
		case LexicalUnit.SAC_SECOND:
		case LexicalUnit.SAC_HERTZ:
		case LexicalUnit.SAC_KILOHERTZ:
			// these are all measurements
			return lu.getFloatValue() + lu.getDimensionUnitText();
		case LexicalUnit.SAC_INTEGER:
			// just a number
			return String.valueOf(lu.getIntegerValue());
		case LexicalUnit.SAC_REAL:
			// just a number
			return String.valueOf(lu.getFloatValue());
		case LexicalUnit.SAC_STRING_VALUE:
		case LexicalUnit.SAC_IDENT:
			// just a string/identifier
			String stringValue = lu.getStringValue();
			if(stringValue.indexOf(" ") != -1)
				stringValue = "\""+stringValue+"\"";
			return stringValue;
		case LexicalUnit.SAC_URI:
			// this is a URL
			return "url(" + normalizeUri(lu.getStringValue()) + ")";
		case LexicalUnit.SAC_RGBCOLOR:
			// this is a rgb encoded color
			StringBuffer sb = new StringBuffer("rgb(");
			LexicalUnit param = lu.getParameters();
			sb.append(param.getIntegerValue()); // R value
			sb.append(',');
			param = param.getNextLexicalUnit(); // comma
			param = param.getNextLexicalUnit(); // G value
			sb.append(param.getIntegerValue());
			sb.append(',');
			param = param.getNextLexicalUnit(); // comma
			param = param.getNextLexicalUnit(); // B value
			sb.append(param.getIntegerValue());
			sb.append(')');

			return sb.toString();
		case LexicalUnit.SAC_INHERIT:
			// constant
			return "inherit";
		case LexicalUnit.SAC_OPERATOR_COMMA:
		    	return ",";
		case LexicalUnit.SAC_ATTR:
		case LexicalUnit.SAC_COUNTER_FUNCTION:
		case LexicalUnit.SAC_COUNTERS_FUNCTION:
		case LexicalUnit.SAC_FUNCTION:
		case LexicalUnit.SAC_RECT_FUNCTION:
		case LexicalUnit.SAC_SUB_EXPRESSION:
		case LexicalUnit.SAC_UNICODERANGE:
		default:
			// these are properties that shouldn't be necessary for most run
			// of the mill HTML/CSS
			return null;
		}
	}
	
	private final String normalizeUri(String uri) {
		if(uri.indexOf("://") > 0) {
			return uri;//absolute link, nothing to do
		}
		
		String contextPath = WebappHelper.getServletContextPath();
		if(uri.startsWith(contextPath)) {
			return uri;//absolute within olat
		}
		
		if(uri.startsWith("..")) {
			
			VFSContainer startDir;
			if(relativePath == null) {
				startDir = rootContainer;
			} else {
				startDir = (VFSContainer)rootContainer.resolve(relativePath);
			}
			
			String tmpUri = uri;
			VFSContainer tmpDir = startDir;
			while(tmpUri.startsWith("../")) {
				tmpDir = tmpDir.getParentContainer();
				tmpUri = tmpUri.substring(3);
			}
			
			String diffPath = getRelativeResultingPath(tmpDir);
			if(StringHelper.containsNonWhitespace(diffPath)) {
				return diffPath + tmpUri;
			}
			return tmpUri;
		}
		if (relativePath != null) {
			uri = relativePath + uri;
		}
		return baseUri + "/" + uri;
	}
	
	private String getRelativeResultingPath(VFSItem tmpDir) {
		String diffPath = "";
		while(!tmpDir.isSame(rootContainer)) {
			diffPath = tmpDir.getName() + "/" + diffPath;
			tmpDir = tmpDir.getParentContainer();
		}
		return diffPath;
	}
}