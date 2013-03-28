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
package org.olat.core.gui.control.generic.iframe;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.commons.chiefcontrollers.BaseChiefController;
import org.olat.core.defaults.dispatcher.StaticMediaDispatcher;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.SimpleHtmlParser;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class IFrameDeliveryMapper implements Mapper, Serializable {

	private static final long serialVersionUID = 8710796223152048613L;
	private static final OLog log = Tracing.createLoggerFor(IFrameDeliveryMapper.class);
	
	private static final String DEFAULT_ENCODING = "iso-8859-1";
	private static final String UNICODE_ENCODING = "unicode";
	private static final String DEFAULT_CONTENT_TYPE = "text/html";
	private static final String XHTML_EXTENSION = "xhtml";
	private static final String XHTML_CONTENT_TYPE = "application/xhtml+xml";
	private static final Pattern PATTERN_ENCTYPE = Pattern.compile("<meta.*charset=([^\"\']*)[\"\']", Pattern.CASE_INSENSITIVE);
	private static final Pattern PATTERN_XML_ENCTYPE = Pattern.compile("<\\?xml.*encoding=[\"\']([^\"\']*)[\"\']", Pattern.CASE_INSENSITIVE);
	private static final Pattern PATTERN_CONTTYPE = Pattern.compile("<meta.*content-type\"?\\s*content\\s*=\\s*[\"]?+(.+?)([\"]?+\\s*/>)", Pattern.CASE_INSENSITIVE);
	private static final Pattern PATTERN_DOCTYPE = Pattern.compile("<!DOCTYPE\\s*html\\s*PUBLIC\\s*[\"\']\\s*-//W3C//DTD\\s*(.+?)(//EN)", Pattern.CASE_INSENSITIVE);
	private static final String FILE_SUFFIX_HTM = "htm";
	private static final String TAG_FRAMESET = "<frameset";
	private static final String TAG_FRAMESET_UPPERC = "<FRAMESET";
	private static final String FILE_SUFFIX_JS = ".js";
	
	private VFSItem rootDir;
	
	private boolean rawContent;
	private boolean enableTextmarking;
	private boolean adjusteightAutomatically;
	
	private String g_encoding;
	private String jsEncoding;
	private String contentEncoding;

	private String frameId;
	private String customCssURL;
	private String themeBaseUri;
	private String customHeaderContent;
	
	private String staticFilesPath;
	private String textMarkerPath;
	
	private transient boolean checkForInlineEvent;
	private transient long suppressEndlessReload;
	
	public IFrameDeliveryMapper() {
		//for XStream
	}
	
	public IFrameDeliveryMapper(VFSItem rootDir, boolean rawContent, boolean enableTextmarking, boolean adjusteightAutomatically,
			String g_encoding, String jsEncoding, String contentEncoding,
			String frameId, String customCssURL, String themeBaseUri, String customHeaderContent,
			String staticFilesPath, String textMarkerPath) {
		
		this.rootDir = rootDir;
		
		this.rawContent = rawContent;
		this.enableTextmarking = enableTextmarking;
		this.adjusteightAutomatically = adjusteightAutomatically;
		
		this.g_encoding = g_encoding;
		this.jsEncoding = jsEncoding;
		this.contentEncoding = contentEncoding;
		
		this.frameId = frameId;
		this.customCssURL = customCssURL;
		this.themeBaseUri = themeBaseUri;
		this.customHeaderContent = customHeaderContent;
		
		this.staticFilesPath = staticFilesPath;
		this.textMarkerPath = textMarkerPath;
	}

	public void setCheckForInlineEvent(boolean checkForInlineEvent) {
		this.checkForInlineEvent = checkForInlineEvent;
	}

	public void setAdjusteightAutomatically(boolean adjusteightAutomatically) {
		this.adjusteightAutomatically = adjusteightAutomatically;
	}
	
	public void setEnableTextmarking(boolean enableTextmarking) {
		this.enableTextmarking = enableTextmarking;
	}

	public void setRawContent(boolean rawContent) {
		this.rawContent = rawContent;
	}

	public void setJsEncoding(String jsEncoding) {
		this.jsEncoding = jsEncoding;
	}

	public void setContentEncoding(String contentEncoding) {
		this.contentEncoding = contentEncoding;
	}

	public void setCustomHeaderContent(String customHeaderContent) {
		this.customHeaderContent = customHeaderContent;
	}

	public void setCustomCssURL(String customCssURL) {
		this.customCssURL = customCssURL;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		
		String isPopUpParam = request.getParameter("olatraw");
		boolean isPopUp = false;
		if (isPopUpParam != null && isPopUpParam.equals("true")) {
			isPopUp = true;
		}
		return deliverFile(request, relPath, isPopUp);
	}

	protected MediaResource deliverFile(HttpServletRequest httpRequest, String path, boolean isPopUp) {
		MediaResource mr;
		VFSLeaf vfsLeaf = null;
		VFSItem vfsItem = null;
		//if directory gets renamed root becomes null
		if (rootDir == null) {
			return new NotFoundMediaResource("directory not found"+path);
		} else {
			vfsItem = rootDir.resolve(path);
		}
		//only files are allowed, but somehow it happened that folders showed up here
		if (vfsItem instanceof VFSLeaf) {
			vfsLeaf = (VFSLeaf) rootDir.resolve(path);
		} else {
			mr = new NotFoundMediaResource(path);
		}
		if (vfsLeaf == null) {
			mr = new NotFoundMediaResource(path);
		} else {
			// check if path ends with .html, .htm or .xhtml. We do this by searching for "htm" 
			// and accept positions of this string at length-3 or length-4
			if (path.toLowerCase().lastIndexOf(FILE_SUFFIX_HTM) >= (path.length()-4)) {
				// set the http content-type and the encoding
				Page page = loadPageWithGuess(vfsLeaf);
				g_encoding = page.getEncoding();
				if (page.isUseLoadedPageString()) {
					mr = prepareMediaResource(httpRequest, page.getPage(), g_encoding, page.getContentType(), isPopUp);
				} else {
					// found a new charset other than iso-8859-1, load string with proper encoding
					String content = FileUtils.load(vfsLeaf.getInputStream(), g_encoding);
					mr = prepareMediaResource(httpRequest, content, g_encoding, page.getContentType(), isPopUp);
				}
			} else if (path.endsWith(FILE_SUFFIX_JS)) { // a javascript library
				VFSMediaResource vmr = new VFSMediaResource(vfsLeaf);
				// set the encoding; could be null if this page starts with .js file
				// (not very common...).
				// if we set no header here, apache sends the default encoding
				// together with the mime-type, which is wrong.
				// so we assume the .js file has the same encoding as the html file
				// that loads the .js file
				if (jsEncoding != null) vmr.setEncoding(jsEncoding);
				else if (g_encoding != null) vmr.setEncoding(g_encoding);
				mr = vmr;
			} else {
				// binary data: not .html, not .htm, not .js -> treated as is
				VFSMediaResource vmr = new VFSMediaResource(vfsLeaf);
				mr = vmr;
			}
		}
		return mr;
	}
	
	private StringMediaResource prepareMediaResource(HttpServletRequest httpRequest, String page, String enc, String contentType, boolean isPopUp) {
		StringMediaResource smr = new StringMediaResource();
		if(XHTML_CONTENT_TYPE.equals(contentType)) {
			//check if the application/xhtml+xml is supported (not supported by IEs)
			//if not, replace the content type by text/html for compatibility
			String accept = httpRequest.getHeader("Accept");
			if(accept == null || accept.indexOf(XHTML_CONTENT_TYPE) < 0) {
				contentType = DEFAULT_CONTENT_TYPE;
			}
		}
		
		String mimetype = contentType + ";charset=" + StringHelper.check4xMacRoman(enc);
		smr.setContentType(mimetype);
		smr.setEncoding(enc);
		//inject some javascript code to size iframe to proper height, but only when not a page with framesets
		if (page.indexOf(TAG_FRAMESET) != -1 || page.indexOf(TAG_FRAMESET_UPPERC) != -1 || isPopUp) {
			//is frameset -> deliver unparsed
			smr.setData(page);			
		} else {
			String agent = httpRequest.getHeader("User-Agent");
			boolean firefoxWorkaround = agent != null && agent.indexOf("Firefox/") > 0;
			smr.setData(injectJavaScript(page, mimetype, checkForInlineEvent, firefoxWorkaround));			     
			// When loading next page, check if it was an inline user click
			this.checkForInlineEvent = true; 

		}
		return smr;
	}
	

	/**
	 * it would be possible to access the iframe.document but there is no event
	 * sended when the content changes. Like this is is easier to inject the js
	 * code and resize the iframe like this.
	 * 
	 * @param page
	 * @param addCheckForInlineEvents
	 *            true: check if page is rendered in iframe, if yes send event
	 *            to framework; false: don't do this check
	 * @return
	 */
	/**
	 * TODO:gs make more stable by only adding some js stuff to the end of the page. First check if document.height is ready
	 * when puttings js to the end or menachism like ext.onReady is needed
	 */
	private String injectJavaScript(String page, String mimetype, boolean addCheckForInlineEvents, boolean anchorFirefoxWorkaround) {
		//if raw content, add nothing
		if(rawContent) {
			return page;
		}
		
		//do not use parser and just check for css and script stuff myself and append just before body and head
		SimpleHtmlParser parser = new SimpleHtmlParser(page);
		if (!parser.isValidHtml()) {
			return page;
		}

		String docType = parser.getHtmlDocType();	
		StringOutput sb = new StringOutput(page.length() + 1000);
		if (docType != null) sb.append(docType).append("\n");
		if (parser.getXhtmlNamespaces() == null) sb.append("<html><head>");
		else {
			sb.append(parser.getXhtmlNamespaces());
			sb.append("<head><meta http-equiv=\"Content-Script-Type\" content=\"text/javascript\"/>");//neded to allow body onload attribute
		}
		//<meta http-equiv="content-type" content="text/html; charset=utf-8" />
		sb.append("<meta http-equiv=\"content-type\" content=\"").append(mimetype).append("\"");
		if (docType != null && docType.indexOf("XHTML") > 0) sb.append("/"); // close tag only when xhtml to validate
		sb.append(">");
		
		if (!parser.hasOwnCss()) {
			// add olat content css as used in html editor
			sb.append("<link href=\"").append(themeBaseUri).append("all/content.css\" rel=\"stylesheet\" type=\"text/css\" ");
			if (docType != null && docType.indexOf("XHTML") > 0) sb.append("/"); // close tag only when xhtml to validate
			sb.append(">\n");
			if (customCssURL != null) {
				// add the custom  CSS, e.g. the course css that overrides the standard content css
				sb.append("<link href=\"").append(customCssURL).append("\" rel=\"stylesheet\" type=\"text/css\" ");
				if (docType != null && docType.indexOf("XHTML") > 0) sb.append("/"); // close tag only when xhtml to validate
				sb.append(">\n");				
			}
		}
		
		//TODO:gs:a do not include if it is a scorm packge!! may results in problems
		if (enableTextmarking) {
			if (log.isDebug()) {
				log.debug("Textmarking is enabled, including tooltips js files into iframe source...");
			}
			sb.append("\n<script type=\"text/javascript\" src=\"");
			StaticMediaDispatcher.renderStaticURI(sb, "js/jquery/jquery-1.9.1.min.js");
			sb.append("\"></script>\n<script type=\"text/javascript\" src=\"");
			StaticMediaDispatcher.renderStaticURI(sb, "js/jquery/jquery-migrate-1.1.1.min.js");
			sb.append("\"></script>\n<script type=\"text/javascript\" src=\"");
			StaticMediaDispatcher.renderStaticURI(sb, "js/jshashtable-2.1_src.js");
			sb.append("\"></script>\n<script type=\"text/javascript\" src=\"");
			StaticMediaDispatcher.renderStaticURI(sb, "js/jquery/ui/jquery-ui-1.10.2.custom.min.js");
			sb.append("\"></script>");
			// Load glossary code now			
			sb.append("\n<script type=\"text/javascript\" id=\"textmarkerLib\" src=\"");
			sb.append(textMarkerPath) ;
			sb.append("/js/glossaryhighlighter.js");
			sb.append("\"></script>");
			sb.append("\n<link rel=\"stylesheet\" type=\"text/css\" id=\"textmarkercss\" href=\"")
			  .append(textMarkerPath).append("/css/textmarker.css\"");
			if (docType != null && docType.indexOf("XHTML") > 0) sb.append("/"); // close tag only when xhtml to validate
			sb.append(">\n<link rel=\"stylesheet\" type=\"text/css\" id=\"jqueryiocss\" href=\"");
		  StaticMediaDispatcher.renderStaticURI(sb, "js/jquery/ui/jquery-ui-1.10.2.custom.min.css");
		  sb.append("\" ");
			if (docType != null && docType.indexOf("XHTML") > 0) sb.append("/"); // close tag only when xhtml to validate
			sb.append(">\n");
		}
		
		// Load some iframe.js helper code
		sb.append("\n<script type=\"text/javascript\">\n/* <![CDATA[ */\n");
		// Set the iframe id, used by the resize function. Important to set before iframe.js is loaded
		sb.append("b_iframeid=\"").append(frameId).append("\";");
		sb.append("b_isInlineUri=").append(Boolean.valueOf(addCheckForInlineEvents).toString()).append(";");
		sb.append("\n/* ]]> */\n</script>");
		sb.append("<script type=\"text/javascript\" src=\"").append(staticFilesPath).append("/js/iframe.js\"></script>\n");

		// Resize frame to fit height of html page. 
		// Do this only when there is some content available. This can be false when
		// the content is written all dynamically via javascript. In this cases, the
		// resizeing is meaningless anyway. 
		if (parser.getHtmlContent().length() > 0) {
			sb.append("\n<script type=\"text/javascript\">\n/* <![CDATA[ */\n");
			// register the resize code to be executed on document load and click events
			if (adjusteightAutomatically) {
				sb.append("b_addOnloadEvent(b_sizeIframe);");		
				sb.append("b_addOnclickEvent(b_sizeIframe);");		
			}
			// register the tooltips enabling on document load event
			sb.append("b_addOnloadEvent(b_hideExtMessageBox);");
			if (addCheckForInlineEvents) {
				// Refresh dirty menu tree by triggering client side request to component which fires events
				// which is not possible by mappers. The method will first check if the page is loaded in our
				// iframe and ignore all other requests (files in framesets, sub-iframes, AJAX calls etc)
				if ((System.currentTimeMillis() - this.suppressEndlessReload) > 2000) sb.append("b_addOnloadEvent(b_sendNewUriEventToParent);");
				this.suppressEndlessReload = System.currentTimeMillis();
			}
			sb.append("b_addOnloadEvent(b_changeLinkTargets);");
			
			if (this.enableTextmarking){
				sb.append("b_addOnloadEvent(b_glossaryHighlight);");
			}
			
			if(anchorFirefoxWorkaround) {
				sb.append("b_addOnloadEvent(b_anchorFirefoxWorkaround);");
			}
			
			sb.append("\n/* ]]> */\n</script>");
		}		
		
		
		String origHTMLHead = parser.getHtmlHead();
		// jsMath brute force approach to render latex formulas: add library if
		// a jsmath class is found in the code and the library is not already in
		// the header of the page
		if (BaseChiefController.isJsMathEnabled()) {
			if ((page.indexOf("class=\"math\"") != -1 || page.indexOf("class='math'") != -1) && (origHTMLHead == null || origHTMLHead.indexOf("jsMath/easy/load.js") == -1)) {
				sb.append("\n<script type=\"text/javascript\" src=\"");
				StaticMediaDispatcher.renderStaticURI(sb, "js/jsMath/easy/load.js");
				sb.append("\"></script>");			
				// don't show jsmath info box, aready visible in parent window
				sb.append("<style type='text/css'>#jsMath_button {display:none}</style>");			
			}			
		}

		// add some custom header things like js code or css
		if (customHeaderContent  != null) {
			sb.append(customHeaderContent);
		}

		// Add HTML header stuff from original page: css, javascript, title etc.
		if (origHTMLHead != null) sb.append(origHTMLHead);		
		sb.append("</head>");
		// use the original body tag, may include all kind of attributes (class, style, onload, on...)
		sb.append(parser.getBodyTag());
		// finally add content and finish page
		sb.append(parser.getHtmlContent());
		sb.append("</body></html>");
		
		return sb.toString();
	}
	
	private Page loadPageWithGuess(VFSLeaf vfsPage) {
		if(contentEncoding != null && isCharsetSupported(contentEncoding)) {
			Page page = new Page();
			page.setExtension(FileUtils.getFileSuffix(vfsPage.getName()));
			page.setEncoding(contentEncoding);
			page.setUseLoadedPageString(true);
			String content = FileUtils.load(vfsPage.getInputStream(), contentEncoding);
			page.setContentType(guessContentType(page, content));
			page.setPage(content);
			return page;
		}
		
		Page page = new Page();
		page.setExtension(FileUtils.getFileSuffix(vfsPage.getName()));
		page.setEncoding(DEFAULT_ENCODING);
		String content = FileUtils.load(vfsPage.getInputStream(), DEFAULT_ENCODING);
		page.setContentType(guessContentType(page, content));
		// <meta.*charset=([^"]*)"
		
		//extract only the charset attribute without the overhead of creating an htmlparser
		boolean guessed = loadPageWithGuess(page, content, DEFAULT_ENCODING);
		if(!guessed) {
			//try opening it with utf-8
			String contentUnicode = FileUtils.load(vfsPage.getInputStream(), UNICODE_ENCODING);
			guessed = loadPageWithGuess(page, contentUnicode, UNICODE_ENCODING);
			if(!guessed) {
				//take default
				page.setPage(content);
				page.setUseLoadedPageString(true);
			}
		}
		return page;
	}
	
	private boolean loadPageWithGuess(Page page, String content, String encoding) {
		//default encoding for xhtml 
		if(XHTML_CONTENT_TYPE.equals(page.getContentType())) {
			page.setEncoding("utf-8");
		}
		
		String guessedEncoding = guessEncoding(content);
		if (guessedEncoding != null) {
			// use found char set
			//if longer than 50 the regexp did fail
			if (isCharsetSupported(guessedEncoding)) {
				page.setEncoding(guessedEncoding);
			} else {
				return false;
			}
			
			// reuse already loaded page when page uses the default encoding
			if (page.getEncoding().equalsIgnoreCase(encoding) || page.getEncoding().contains(encoding)
					|| page.getEncoding().toLowerCase().contains(encoding)) {
				page.setUseLoadedPageString(true);
				page.setPage(content);
			}
			return true;
		}
		return false;
	}
	
	private String guessContentType(Page page, String content) {
		String cType = null;
		if(XHTML_EXTENSION.equals(page.getExtension())) {
			Matcher dm = PATTERN_DOCTYPE.matcher(content);
			if (dm.find()) {
				String doctype = dm.group(1).toLowerCase();
				//default settings for XHTML-documents, should be taken if no <meta  http-equiv="content-type" .../> is given
				if (doctype.indexOf("xhtml") == 0 && doctype.indexOf("mathml") > 0) {
					cType = XHTML_CONTENT_TYPE;
				}
			}
		}
		
		Matcher cm = PATTERN_CONTTYPE.matcher(content);
		if (cm.find()) {
			//use found content-type
			String contentType = cm.group(1);
			String[] types=contentType.split(";");
			for (int i=0;i<types.length;i++) {
				if (!(types[i].contains("charset"))) {
					contentType=types[i].trim();
					break;
				}
			}
			//if longer than 50 the regexp did fail
			if (contentType.length() < 50) {
				cType = contentType;
			}			
		}
		
		if(cType == null) {
			return DEFAULT_CONTENT_TYPE;
		}
		if(cType.contains("text/xhtml")) {
			//text/xhtml is not accepted as html mime type by most of the browsers
			return DEFAULT_CONTENT_TYPE;
		}
		return cType;
	}
	
	private String guessEncoding(String content) {
		Matcher m = PATTERN_ENCTYPE.matcher(content);
		if (m.find()) {
			// use found char set
			String htmlcharset = m.group(1);
			//if longer than 50 the regexp did fail
			if (htmlcharset.length() < 50 ) {
				return htmlcharset;
			}
		}
		
		Matcher xmlDeclaration = PATTERN_XML_ENCTYPE.matcher(content);
		if (xmlDeclaration.find()) {
			// use found char set
			String xmlcharset = xmlDeclaration.group(1);
			//if longer than 50 the regexp did fail
			if (xmlcharset.length() < 50 ) {
				return xmlcharset;
			}
		}
		
		return null;
	}
	
	private boolean isCharsetSupported(String enc) {
		try {
			return Charset.isSupported(enc);
		} catch (IllegalCharsetNameException e) {
			return false;
		}
	}
	
	private static class Page {
		private String encoding;
		private String contentType;
		private String extension;
		private String page;
		private boolean useLoadedPageString = false;
		
		public String getEncoding() {
			return encoding;
		}
		
		public void setEncoding(String encoding) {
			this.encoding = encoding;
		}
		
		public String getExtension() {
			return extension;
		}

		public void setExtension(String extension) {
			this.extension = extension;
		}

		public String getContentType() {
			return contentType;
		}

		public void setContentType(String contentType) {
			this.contentType = contentType;
		}

		public String getPage() {
			return page;
		}
		
		public void setPage(String page) {
			this.page = page;
		}

		public boolean isUseLoadedPageString() {
			return useLoadedPageString;
		}

		public void setUseLoadedPageString(boolean useLoadedPageString) {
			this.useLoadedPageString = useLoadedPageString;
		}
	}

}
