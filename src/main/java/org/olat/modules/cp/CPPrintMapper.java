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

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.batik.css.parser.ParseException;
import org.apache.batik.css.parser.Parser;
import org.apache.logging.log4j.Logger;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.cp.CPManifestTreeModel.UserObject;
import org.xml.sax.InputSource;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

/**
 * 
 * Description:<br>
 * Deliver the CP as a single page. All the HTML Pages are parser and the
 * attribute href/src are rewritten to absolute /olat/m/xxxx urls. The HTML parser
 * used is validator.HtmlParser. For CSS there is the same process. We use the Batik CSS
 * parser for SAC.
 * 
 * <P>
 * Initial Date:  18 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CPPrintMapper implements Mapper {
	
	private static final Logger log = Tracing.createLoggerFor(CPPrintMapper.class);
	
	private static final String DEFAULT_ENCODING = "iso-8859-1";
	private static final String DEFAULT_CONTENT_TYPE = "text/html";
	private static final String XHTML_EXTENSION = "xhtml";
	private static final String XHTML_CONTENT_TYPE = "application/xhtml+xml";
	private static final Pattern PATTERN_ENCTYPE = Pattern.compile("<meta.*charset=([^\"\']*)[\"\']", Pattern.CASE_INSENSITIVE);
	private static final Pattern PATTERN_XML_ENCTYPE = Pattern.compile("<\\?xml.*encoding=[\"\']([^\"\']*)[\"\']", Pattern.CASE_INSENSITIVE);
	private static final Pattern PATTERN_CONTTYPE = Pattern.compile("<meta.*content-type\"?\\s*content\\s*=\\s*[\"]?+(.+?)([\"]?+\\s*/>)", Pattern.CASE_INSENSITIVE);
	private static final Pattern PATTERN_DOCTYPE = Pattern.compile("<!DOCTYPE\\s*html\\s*PUBLIC\\s*[\"\']\\s*-//W3C//DTD\\s*(.+?)(//EN)", Pattern.CASE_INSENSITIVE);
	private static final String FILE_SUFFIX_HTM = "htm";
	private static final String FILE_SUFFIX_CSS = "css";
	private static final String TAG_FRAMESET = "<frameset";
	private static final String TAG_FRAMESET_UPPERC = "<FRAMESET";
	private static final String FILE_SUFFIX_JS = ".js";
	
	private String gEncoding;
	
	private List<String> selectedNodeIds;
	
	private String baseUri;
	private final String themeBaseUri;
	private final CPManifestTreeModel ctm;
	private final VFSContainer rootDir;
	
	private String contentEncoding;
	private String jsEncoding;
	
	public CPPrintMapper(CPManifestTreeModel ctm, VFSContainer rootContainer, String themeBaseUri) {
		this.themeBaseUri = themeBaseUri;
		this.rootDir = rootContainer;
		this.ctm = ctm;
	}
	
	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}

	public void setSelectedNodeIds(List<String> selectedNodeIds) {
		this.selectedNodeIds = selectedNodeIds;
	}
	
	public void setContentEncoding(String encoding) {
		this.contentEncoding = encoding;
	}
	
	public void setJSEncoding(String encoding) {
		this.jsEncoding = encoding;
	}
	
	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		if(relPath.endsWith("print.html")) {
			return deliverCompositePage(request);
		}
		return deliverFile(request, relPath);
	}

	private MediaResource deliverCompositePage(HttpServletRequest request) {
		List<HtmlPageHandler> parsedPages = composePrintPage(selectedNodeIds);
		
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head>");
		for(HtmlPageHandler page:parsedPages) {
			sb.append("<!-- Header of -->");
			sb.append(page.getHeader()).append("\n\n");
		}
		injectJavascriptAndCss(sb);
		
		sb.append("</head><body onload='window.focus();window.print()'>");
		printPagesList(sb, parsedPages);
		sb.append("</body></html>");

		return prepareMediaResource(request, sb.toString(), gEncoding, "text/html");
	}
	
	public String pagesToHtml() {
		List<String> nodeIds = ctm.getFlattedTree()
				.stream().map(TreeNode::getIdent)
				.collect(Collectors.toList());
		List<HtmlPageHandler> parsedPages = composePrintPage(nodeIds);
		StringBuilder sb = new StringBuilder(12000);
		printPagesList(sb, parsedPages);
		return sb.toString();
	}
	
	private void printPagesList(StringBuilder sb, List<HtmlPageHandler> parsedPages) {
		for(Iterator<HtmlPageHandler> pageIt=parsedPages.iterator(); pageIt.hasNext(); ) {
			HtmlPageHandler page = pageIt.next();
			if(page.isEmpty()) {
				String title = page.getTitle();
				if(StringHelper.containsNonWhitespace(title)) {
					int level = page.getLevel() + 1;
					level = Math.min(level, 6);
					sb.append("<h").append(level).append(">").append(page.getTitle()).append("</h").append(level).append(">");
				}
			} else {
				bodyDecorator(page, sb, !pageIt.hasNext());
			}
		}
	}
	
	private void injectJavascriptAndCss(StringBuilder output) {	
		try(StringOutput sb = new StringOutput(128)) {
			sb.append("<script src=\"");
			StaticMediaDispatcher.renderStaticURI(sb, "js/jquery/jquery-3.6.0.min.js");
			sb.append("\")'></script>");
			output.append(sb.toString());
			output.append("<link href=\"").append(themeBaseUri).append("all/content.css\" rel=\"stylesheet\" />\n");
		} catch(IOException e) {
			log.error("", e);
		}
	}
	
	protected void bodyDecorator(HtmlPageHandler page, StringBuilder sb, boolean last) {
		sb.append("<!-- Body of ").append(page.getDocument().getName()).append("-->");
		sb.append("<div class=\"o_cp_print_page\" style='clear:both; position:relative;");
		if(!last) {
			sb.append(" page-break-after:always;");
		}  
		sb.append("'>")
		  .append(page.getBody())
		  .append("</div>");
	}
	
	private List<HtmlPageHandler> composePrintPage(List<String> nodeIds) {
		List<HtmlPageHandler> pages = new ArrayList<>();
		
		for(String nodeId:nodeIds) {
			HtmlPageHandler parsedPage = null;
			TreeNode treeNode = ctm.getNodeById(nodeId);
			if(treeNode != null && treeNode.getUserObject() instanceof UserObject) {
				String identifierRes = ((UserObject)treeNode.getUserObject()).getHref();
				if(StringHelper.containsNonWhitespace(identifierRes)) {
					VFSItem currentItem = rootDir.resolve(identifierRes);
					if(currentItem instanceof VFSLeaf) {
						String extension = FileUtils.getFileSuffix(currentItem.getName());
						if("htm".equalsIgnoreCase(extension) || "html".equalsIgnoreCase(extension) || "xhtml".equalsIgnoreCase(extension)) {
							VFSLeaf currentLeaf = (VFSLeaf)currentItem;
							parsedPage = parsePage(identifierRes, currentLeaf, treeNode);
						}
					}
				}
			}
			if(parsedPage == null) {
				parsedPage = new HtmlPageHandler(treeNode, null, rootDir, baseUri);
			}
			pages.add(parsedPage);
		}
		return pages;
	}
	
	private HtmlPageHandler parsePage(String identifierRes, VFSLeaf document, TreeNode node) {
		HtmlPageHandler page = new HtmlPageHandler(node, document, rootDir, baseUri);
		int index = identifierRes.lastIndexOf('/');
		if(index > 0) {
			String relativePath = identifierRes.substring(0, index+1);
			page.setRelativePath(relativePath);
		}

		try {
			Page content = loadPageWithGuess(document);
			if(gEncoding == null) {
				gEncoding = content.getEncoding();
			}

			String rawContent;
			if (content.isUseLoadedPageString()) {
				rawContent = content.getPage();
			} else {
				// found a new charset other than iso-8859-1, load string with proper encoding
				rawContent = FileUtils.load(document.getInputStream(), content.getEncoding());
			}
			
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
			parser.setContentHandler(page);
			parser.parse(new InputSource(new StringReader(rawContent)));
			return page;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	protected MediaResource deliverCssFile(VFSLeaf cssFile, HttpServletRequest request) {
		Page page = loadPageWithGuess(cssFile);
		String encoding = page.getEncoding();
		String content = page.getPage();
		
		SACCSSHandler handler = new SACCSSHandler(cssFile, rootDir, baseUri);
		try {
			Parser parser = new Parser();
			parser.setDocumentHandler(handler);
		  parser.parseStyleSheet(new org.w3c.css.sac.InputSource(new StringReader(content)));
		} catch (IOException ioe) {
			log.error("", ioe);
			return null;
		} catch (ParseException pe) {
			log.error("", pe);
			return null;
		}
		
		String cleanStyleSheet = handler.getCleanStylesheet();
		return prepareMediaResource(request, cleanStyleSheet, encoding, "text/css");
	}
	
	protected MediaResource deliverFile(HttpServletRequest httpRequest, String path) {
		//if directory gets renamed root becomes null
		if (rootDir == null) {
			return new NotFoundMediaResource();
		}
		
		VFSLeaf vfsLeaf = null;
		VFSItem vfsItem = rootDir.resolve(path);
		//only files are allowed, but somehow it happened that folders showed up here
		if (vfsItem instanceof VFSLeaf) {
			vfsLeaf = (VFSLeaf)vfsItem;
		} else {
			return new NotFoundMediaResource();
		}
		
		MediaResource mr;
		// check if path ends with .html, .htm or .xhtml. We do this by searching for "htm" 
		// and accept positions of this string at length-3 or length-4
		if (path.toLowerCase().lastIndexOf(FILE_SUFFIX_HTM) >= (path.length()-4)) {
			// set the http content-type and the encoding
			Page page = loadPageWithGuess(vfsLeaf);
			gEncoding = page.getEncoding();
			if (page.isUseLoadedPageString()) {
				mr = prepareMediaResource(httpRequest, page.getPage(), gEncoding, page.getContentType());
			} else {
				// found a new charset other than iso-8859-1, load string with proper encoding
				String content = FileUtils.load(vfsLeaf.getInputStream(), gEncoding);
				mr = prepareMediaResource(httpRequest, content, gEncoding, page.getContentType());
			}
		}	else	if (path.toLowerCase().lastIndexOf(FILE_SUFFIX_CSS) >= (path.length()-4)) {
			// set the http content-type and the encoding
			mr = deliverCssFile(vfsLeaf, httpRequest);
		} else if (path.endsWith(FILE_SUFFIX_JS)) { // a javascript library
			VFSMediaResource vmr = new VFSMediaResource(vfsLeaf);
			// set the encoding; could be null if this page starts with .js file
			// (not very common...).
			// if we set no header here, apache sends the default encoding
			// together with the mime-type, which is wrong.
			// so we assume the .js file has the same encoding as the html file
			// that loads the .js file
			if (jsEncoding != null) vmr.setEncoding(jsEncoding);
			else if (gEncoding != null) vmr.setEncoding(gEncoding);
			mr = vmr;
		} else {
			// binary data: not .html, not .htm, not .js -> treated as is
			mr = new VFSMediaResource(vfsLeaf);
		}

		return mr;
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
			String contentUnicode = FileUtils.load(vfsPage.getInputStream(), StandardCharsets.UTF_8.name());
			guessed = loadPageWithGuess(page, contentUnicode, StandardCharsets.UTF_8.name());
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

	private StringMediaResource prepareMediaResource(HttpServletRequest httpRequest, String page, String enc, String contentType) {
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
		if (page.indexOf(TAG_FRAMESET) != -1 || page.indexOf(TAG_FRAMESET_UPPERC) != -1) {
			//is frameset -> deliver unparsed
			smr.setData(page);			
		} else {
			smr.setData(page);			     
		}
		return smr;
	}
	
	public class Page {
		private String encoding;
		private String contentType;
		private String extension;
		private String page;
		private boolean useLoadedPageString = false;
		
		public String getExtension() {
			return extension;
		}
		
		public void setExtension(String extension) {
			this.extension = extension;
		}

		public String getEncoding() {
			return encoding;
		}

		public void setEncoding(String encoding) {
			this.encoding = encoding;
		}

		public boolean isUseLoadedPageString() {
			return useLoadedPageString;
		}

		public void setUseLoadedPageString(boolean useLoadedPageString) {
			this.useLoadedPageString = useLoadedPageString;
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
	}
}