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

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.components.htmlheader.jscss.CustomCSSDelegate;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.SimpleHtmlParser;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.vfs.filters.VFSItemFilter;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class IFrameDeliveryMapper implements Mapper {

	private static final Logger log = Tracing.createLoggerFor(IFrameDeliveryMapper.class);
	
	private static final String DEFAULT_CONTENT_TYPE = "text/html";
	private static final String XHTML_EXTENSION = "xhtml";
	private static final String XHTML_CONTENT_TYPE = "application/xhtml+xml";
	private static final Pattern PATTERN_ENCTYPE = Pattern.compile("<meta[^>]*charset=[\"\']?([A-Za-z0-9.:\\-_]*)", Pattern.CASE_INSENSITIVE);
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
	
	private String jsEncoding;
	private String contentEncoding;

	private String frameId;
	private String customCssURL;
	private transient CustomCSSDelegate customCssDelegate;
	private String themeBaseUri;
	private String customHeaderContent;
	
	private Boolean jQueryEnabled;
	private Boolean prototypeEnabled;
	private Boolean openolatCss;
	
	private transient boolean checkForInlineEvent;
	private transient long suppressEndlessReload;
	
	public IFrameDeliveryMapper() {
		//for XStream
	}
	
	public IFrameDeliveryMapper(VFSItem rootDir, boolean rawContent, boolean enableTextmarking, String frameId,
			String customCssURL, String themeBaseUri, String customHeaderContent) {
		
		this.rootDir = rootDir;
		
		this.rawContent = rawContent;
		this.enableTextmarking = enableTextmarking;
		
		this.frameId = frameId;
		this.customCssURL = customCssURL;
		this.themeBaseUri = themeBaseUri;
		this.customHeaderContent = customHeaderContent;
	}
	
	public void setDeliveryOptions(DeliveryOptions config) {
		if(config != null) {
			Boolean standard = config.getStandardMode();
			if(standard != null && standard.booleanValue()) {
				rawContent = true;
				openolatCss = false;
				jQueryEnabled = false;
				prototypeEnabled = false;
				enableTextmarking = false;
			} else {
				jQueryEnabled = config.getjQueryEnabled();
				prototypeEnabled = config.getPrototypeEnabled();
				if(config.getGlossaryEnabled() != null) {
					enableTextmarking = config.getGlossaryEnabled().booleanValue();
				}
				openolatCss = config.getOpenolatCss();
			}
			
			if(config.getContentEncoding() != null) {
				contentEncoding = config.getContentEncoding();
			}
			if(config.getJavascriptEncoding() != null) {
				jsEncoding = config.getJavascriptEncoding();
			}
		}
	}

	public void setCheckForInlineEvent(boolean checkForInlineEvent) {
		this.checkForInlineEvent = checkForInlineEvent;
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
	
	public void setCustomCssDelegate(CustomCSSDelegate customCssDelegate) {
		this.customCssDelegate = customCssDelegate;
		if(customCssDelegate.getCustomCSS() != null) {
			customCssURL = customCssDelegate.getCustomCSS().getCSSURLIFrame();
		}
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
		//if directory gets renamed root becomes null
		if (rootDir == null) {
			return new NotFoundMediaResource();
		} 
		
		VFSLeaf vfsLeaf = resolveFile(path);

		MediaResource mr;
		if (vfsLeaf == null) {
			mr = new NotFoundMediaResource();
		} else {
			// check if path ends with .html, .htm or .xhtml. We do this by searching for "htm" 
			// and accept positions of this string at length-3 or length-4
			if (path.toLowerCase().lastIndexOf(FILE_SUFFIX_HTM) >= (path.length() - 4)) {
				mr = deliverHtmlFile(httpRequest, vfsLeaf, isPopUp);
			} else if (path.endsWith(FILE_SUFFIX_JS)) { // a javascript library
				mr = deliverJavascriptFile(vfsLeaf);
			} else {
				// binary data: not .html, not .htm, not .js -> treated as is
				VFSMediaResource vmr = new VFSMediaResource(vfsLeaf);
				String filename = vfsLeaf.getName();
				// This is to prevent the login prompt in Excel, Word and PowerPoint
				if(filename.endsWith(".xlsx") || filename.endsWith(".pptx") || filename.endsWith(".docx")) {
					vmr.setDownloadable(true);
				}
				mr = vmr;
			}
		}
		return mr;
	}
	
	/**
	 * @param path The path
	 * @return A leaf
	 */
	private final VFSLeaf resolveFile(String path) {
		VFSItem vfsItem = rootDir.resolve(path);
		if(vfsItem == null && rootDir instanceof VFSContainer) {
			path = VFSManager.sanitizePath(path);
			List<VFSItem> items = null;
			int lastSlash = path.lastIndexOf('/');
			if(lastSlash == 0) {
				String filename = path.substring(1);
				items = ((VFSContainer)rootDir).getItems(new ByNameCaseInsensitive(filename));
			} else if (lastSlash > 0) {
				String containerPath = path.substring(0, lastSlash);
				String filename = path.substring(lastSlash + 1);
				VFSItem parentItem = rootDir.resolve(containerPath);
				if(parentItem != null) {
					items = ((VFSContainer)parentItem).getItems(new ByNameCaseInsensitive(filename));
				}
			} else {
				items = ((VFSContainer)rootDir).getItems(new ByNameCaseInsensitive(path));
			}
			
			if(items != null && items.size() == 1) {
				vfsItem = items.get(0);
			}
		}
		
		VFSLeaf vfsLeaf = null;
		//only files are allowed, but somehow it happened that folders showed up here
		if (vfsItem instanceof VFSLeaf) {
			vfsLeaf = (VFSLeaf)vfsItem;
		}
		return vfsLeaf;
	}
	
	private MediaResource deliverJavascriptFile(VFSLeaf vfsLeaf) {
		VFSMediaResource vmr = new VFSMediaResource(vfsLeaf);
		// set the encoding; could be null if this page starts with .js file
		// (not very common...).
		// if we set no header here, apache sends the default encoding
		// together with the mime-type, which is wrong.
		// so we assume the .js file has the same encoding as the html file
		// that loads the .js file
		
		String encoding;
		if (jsEncoding != null) {
			encoding = jsEncoding;
		} else if (contentEncoding != null) {
			encoding = contentEncoding;
		} else {
			encoding = StandardCharsets.ISO_8859_1.name();
		}
		vmr.setEncoding(encoding);
		return vmr;
	}
	
	/**
	 * Set the http content-type and the encoding
	 * 
	 * @param httpRequest The HTTP request
	 * @param vfsLeaf The file to delivery
	 * @param isPopUp If it's a popup or not
	 * @return The media resource
	 */
	private MediaResource deliverHtmlFile(HttpServletRequest httpRequest, VFSLeaf vfsLeaf, boolean isPopUp) {
		Page page = loadPageWithGuess(vfsLeaf);
		String pageEncoding = page.getEncoding();
		if(pageEncoding == null) {
			pageEncoding = StandardCharsets.ISO_8859_1.name();
		} else if(contentEncoding == null) {
			contentEncoding = pageEncoding;
		}
		
		if (page.isUseLoadedPageString()) {
			return prepareMediaResource(httpRequest, page.getContent(), pageEncoding, page.getContentType(), isPopUp);
		}
		// found a new charset other than iso-8859-1, load string with proper encoding
		String content = FileUtils.load(vfsLeaf.getInputStream(), pageEncoding);
		return prepareMediaResource(httpRequest, content, pageEncoding, page.getContentType(), isPopUp);
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
			if(rawContent) {
				smr.setData(page);	
			} else {
				smr.setData(injectJavaScript(page, mimetype, checkForInlineEvent, firefoxWorkaround));	
			}
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
	private String injectJavaScript(String page, String mimetype, boolean addCheckForInlineEvents, boolean anchorFirefoxWorkaround) {
		//do not use parser and just check for css and script stuff myself and append just before body and head
		SimpleHtmlParser parser = new SimpleHtmlParser(page);
		if (!parser.isValidHtml()) {
			return page;
		}

		String docType = parser.getHtmlDocType();	
		try(HtmlOutput sb = new HtmlOutput(docType, themeBaseUri, page.length() + 1000)) {
			if (docType != null) sb.append(docType).append("\n");
			if (parser.getXhtmlNamespaces() == null) {
				sb.append("<!DOCTYPE HTML><html style=\"height: 100%;\" lang=\"de-DE\"><head>");
			} else {
				sb.append(parser.getXhtmlNamespaces());
				sb.append("<head>\n<meta http-equiv=\"Content-Script-Type\" content=\"text/javascript\"/>");//neded to allow body onload attribute
			}
			//<meta http-equiv="content-type" content="text/html; charset=utf-8" />
			/* sb.append("\n<meta http-equiv=\"content-type\" content=\"").append(mimetype).append("\"");
			if (docType != null && docType.indexOf("XHTML") > 0) sb.append("/"); // close tag only when xhtml to validate
			sb.append(">");*/
			
			if(openolatCss != null && openolatCss.booleanValue()) {
				sb.appendOpenolatCss();
			}
			
			if(!parser.hasOwnCss()) {
				if(openolatCss == null || openolatCss.booleanValue()) {
					//add olat content css as used in html editor
					sb.appendOpenolatCss();//css only loaded once in HtmlOutput
				}
				if(customCssDelegate != null && customCssDelegate.getCustomCSS() != null
						&& customCssDelegate.getCustomCSS().getCSSURLIFrame() != null) {
					String  customCssURL = customCssDelegate.getCustomCSS().getCSSURLIFrame();
					sb.appendCss(customCssURL, "customcss");	
				} else if (customCssURL != null) {
					// add the custom  CSS, e.g. the course css that overrides the standard content css
					sb.appendCss(customCssURL, "customcss");				
				} 
			}
			
			if (enableTextmarking) {
				if (log.isDebugEnabled()) {
					log.debug("Textmarking is enabled, including tooltips js files into iframe source...");
				}
				sb.appendJQuery();	
				sb.appendGlossary();
			}
			
			if(jQueryEnabled != null && jQueryEnabled.booleanValue()) {
				sb.appendJQuery();
			}
			
			if(prototypeEnabled != null && prototypeEnabled.booleanValue()) {
				sb.appendPrototype();
			}
			/*
			// Load some iframe.js helper code
			sb.append("\n<script>\n");
			// Set the iframe id. Important to set before iframe.js is loaded.
			sb.append("b_iframeid=\"").append(frameId).append("\";");
			sb.append("b_isInlineUri=").append(Boolean.toString(addCheckForInlineEvents)).append(";");
			sb.append("\n</script>");
			sb.appendStaticJs("js/openolat/iframe.js");
			sb.appendStaticJs("js/iframeResizer/iframeResizer.contentWindow.min.js");
			
	
			if (parser.getHtmlContent().length() > 0) {
				sb.append("\n<script>\n");
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
				
				if(enableTextmarking){
					sb.append("b_addOnloadEvent(b_glossaryHighlight);");
				}
				
				if(anchorFirefoxWorkaround) {
					sb.append("b_addOnloadEvent(b_anchorFirefoxWorkaround);");
				}
				
				sb.append("\n</script>");
			}
			*/		
	
			String origHTMLHead = parser.getHtmlHead();
			// jsMath brute force approach to render latex formulas: add library if
			// a jsmath class is found in the code and the library is not already in
			// the header of the page
			if ((page.indexOf("<math") > -1 || page.indexOf("class=\"math\"") != -1 || page.indexOf("class='math'") != -1) && (origHTMLHead == null || origHTMLHead.indexOf("jsMath/easy/load.js") == -1)) {
				sb.appendJsMath();		
			}
	
			// add some custom header things like js code or css
			if (customHeaderContent  != null) {
				sb.append(customHeaderContent);
			}
	
			// Add HTML header stuff from original page: css, javascript, title etc.
			if (origHTMLHead != null) sb.append(origHTMLHead);		
			sb.append("\n</head>\n");
			// use the original body tag, may include all kind of attributes (class, style, onload, on...)
			sb.append(parser.getBodyTag());
			// finally add content and finish page
			sb.append(parser.getHtmlContent());
			// iFrameResizer adds that snippet at the end of the iFrame body, but without &nbsp.
			// Sometimes this leads to a invisible line at the end of the iFrame, so we add the
			// same snippet but with &nbsp.
			sb.append("<div style=\"clear: both; display: block;\">&nbsp;</div>");
			sb.append("</body>");
			String outerBody = parser.getOuterBodyContent();
			if(StringHelper.containsNonWhitespace(outerBody)) {
				sb.append(outerBody);
			}
			sb.append("</html>");
			
			return sb.toString();
		} catch(Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	private Page loadPageWithGuess(VFSLeaf vfsPage) {
		if(contentEncoding != null && isCharsetSupported(contentEncoding)) {
			Page page = new Page();
			page.setExtension(FileUtils.getFileSuffix(vfsPage.getName()));
			page.setEncoding(contentEncoding);
			page.setUseLoadedPageString(true);
			String content = FileUtils.load(vfsPage.getInputStream(), contentEncoding);
			page.setContentType(guessContentType(page, content));
			page.setContent(content);
			return page;
		}
		
		Page page = new Page();
		page.setExtension(FileUtils.getFileSuffix(vfsPage.getName()));
		String content = FileUtils.load(vfsPage.getInputStream(), StandardCharsets.ISO_8859_1.name());
		page.setContentType(guessContentType(page, content));
		// <meta.*charset=([^"]*)"
		
		//extract only the charset attribute without the overhead of creating an htmlparser
		boolean guessed = loadPageWithGuess(page, content, StandardCharsets.ISO_8859_1.name());
		if(!guessed) {
			//try opening it with utf-8
			String contentUnicode = FileUtils.load(vfsPage.getInputStream(), StandardCharsets.UTF_8.name());
			guessed = loadPageWithGuess(page, contentUnicode, StandardCharsets.UTF_8.name());
			if(!guessed) {
				//take default
				page.setContent(content);
				page.setUseLoadedPageString(true);
			}
		}
		return page;
	}
	
	private boolean loadPageWithGuess(Page page, String content, String encoding) {
		//default encoding for xhtml 
		if(XHTML_CONTENT_TYPE.equals(page.getContentType())) {
			page.setEncoding(StandardCharsets.UTF_8.name());
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
				page.setContent(content);
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
	
	protected String guessEncoding(String content) {
		Matcher m = PATTERN_ENCTYPE.matcher(content);
		if (m.find()) {
			// use found char set
			String htmlcharset = m.group(1);
			//if longer than 50 the regexp did fail
			if (htmlcharset.length() < 50 && htmlcharset.length() != 0) {
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
	
	private static class HtmlOutput extends StringOutput {
		private boolean ooCssLoaded = false;
		private boolean jqueryLoaded = false;

		private final String docType;
		private final String themeBaseUri;
		
		public HtmlOutput(String docType, String themeBaseUri, int length) {
			super(length);
			this.docType = docType;
			this.themeBaseUri = themeBaseUri + "content.css";
		}
		
		private void appendOpenolatCss() {
			if(ooCssLoaded) return;
			appendCss(themeBaseUri, "themecss");
			ooCssLoaded = true;
		}
		
		public void appendJQuery() {
			if(jqueryLoaded) return;
			
			appendJQuery2Cond();
			appendStaticJs("js/jshashtable-2.1_src.js");
			appendStaticJs("js/jquery/ui/jquery-ui-1.11.4.custom.min.js");
			appendStaticCss("js/jquery/ui/jquery-ui-1.11.4.custom.min.css", "jqueryuicss");
			jqueryLoaded = true;
		}
		
		public void appendJQuery2Cond() {
			appendStaticJs("js/jquery/jquery-3.5.1.min.js");
		}

		public void appendPrototype() {
			appendStaticJs("js/prototype/prototype.js");
		}
		
		public void appendJsMath() {
			append("<script>\n");
			append("window.MathJax = {\n");
			append(" extensions: [\"jsMath2jax.js\"],\n");
			append(" messageStyle: 'none',\n");
			append(" showProcessingMessages: false,\n");
			append(" showMathMenu: false,\n");
			append(" menuSettings: { },\n");
			append(" jsMath2jax: {\n");
			append("   preview: \"none\"\n");
			append(" },\n");
			append(" tex2jax: {\n");
			append("   ignoreClass: \"math\"\n");
			append(" },\n");
			append(" \"HTML-CSS\": {\n");
			append("   EqnChunk: 5, EqnChunkFactor: 1, EqnChunkDelay: 100\n");
			append(" },\n");
			append(" \"fast-preview\": {\n");
			append("   disabled: true\n");
			append(" }\n");
			append("};");
			append("</script>");
			append("<script src=\"");
			append(WebappHelper.getMathJaxCdn());
			append("MathJax.js?config=");
			append(WebappHelper.getMathJaxConfig());
			append("\"></script>\n");
		}
		
		public void appendGlossary() {
			appendStaticJs("js/openolat/glossaryhighlighter.js");
			appendStaticJs("js/openolat/glossary.bootstrap.tooltip.js");
			appendStaticCss("js/openolat/glossaryhighlighter.css", "textmarkercss");
		}

		public void appendStaticJs(String javascript) {
			append("<script src=\"");
			StaticMediaDispatcher.renderStaticURI(this, javascript);
			append("\"></script>\n");
		}
		
		public void appendStaticCss(String css, String id) {
			append("\n<link rel=\"stylesheet\" id=\"").append(id).append("\" href=\"");
		  StaticMediaDispatcher.renderStaticURI(this, css);
		  append("\"");
			if (docType != null && docType.indexOf("XHTML") > 0) append("/"); // close tag only when xhtml to validate
			append(">\n");
		}
		
		public void appendCss(String css, String id) {
			append("\n<link rel=\"stylesheet\" id=\"").append(id).append("\" href=\"").append(css).append("\"");
			if (docType != null && docType.indexOf("XHTML") > 0) append("/"); // close tag only when xhtml to validate
			append(">\n");
		}
	}
	
	private static class Page {
		private String encoding;
		private String contentType;
		private String extension;
		private String content;
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

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public boolean isUseLoadedPageString() {
			return useLoadedPageString;
		}

		public void setUseLoadedPageString(boolean useLoadedPageString) {
			this.useLoadedPageString = useLoadedPageString;
		}
	}
	
	private static class ByNameCaseInsensitive implements VFSItemFilter {
		
		private final String filename;
		
		public ByNameCaseInsensitive(String filename) {
			this.filename = filename;
		}

		@Override
		public boolean accept(VFSItem vfsItem) {
			return vfsItem != null && filename.equalsIgnoreCase(vfsItem.getName());
		}
	}
}
