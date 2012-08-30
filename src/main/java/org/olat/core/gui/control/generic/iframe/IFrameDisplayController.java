/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.gui.control.generic.iframe;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.commons.chiefcontrollers.BaseChiefController;
import org.olat.core.commons.fullWebApp.BaseFullWebappController;
import org.olat.core.defaults.dispatcher.ClassPathStaticDispatcher;
import org.olat.core.defaults.dispatcher.StaticMediaDispatcher;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.htmlheader.jscss.CustomCSS;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.textmarker.GlossaryMarkupItemController;
import org.olat.core.gui.control.generic.textmarker.TextMarkerManagerImpl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.SimpleHtmlParser;
import org.olat.core.util.StringHelper;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaResource;

/**
 * Class that loads a resource (html) in an Iframe and tries to adjust the size of the Iframe to hide the scrollbars.
 * This is done by injecting some javascript into the head part of the loaded html file which then resizes the iframe itself.
 * See package documentation for details.
 * Initial Date: Dec 9, 2004
 * 
 * @author Felix Jost<br>
 *         
 * @author guido
 */
public class IFrameDisplayController extends BasicController implements GenericEventListener {
	private static final String DEFAULT_ENCODING = "iso-8859-1";
	private static final String UNICODE_ENCODING = "unicode";
	private static final String DEFAULT_CONTENT_TYPE = "text/html";
	private static final String XHTML_EXTENSION = "xhtml";
	private static final String XHTML_CONTENT_TYPE = "application/xhtml+xml";
	private static final Pattern PATTERN_ENCTYPE = Pattern.compile("<meta.*charset=([^\"\']*)[\"\']", Pattern.CASE_INSENSITIVE);
	private static final Pattern PATTERN_XML_ENCTYPE = Pattern.compile("<\\?xml.*encoding=[\"\']([^\"\']*)[\"\']", Pattern.CASE_INSENSITIVE);
	private static final Pattern PATTERN_CONTTYPE = Pattern.compile("<meta.*content-type\"?\\s*content\\s*=\\s*[\"]?+(.+?)([\"]?+\\s*/>)", Pattern.CASE_INSENSITIVE);
	private static final Pattern PATTERN_DOCTYPE = Pattern.compile("<!DOCTYPE\\s*html\\s*PUBLIC\\s*[\"\']\\s*-//W3C//DTD\\s*(.+?)(//EN)", Pattern.CASE_INSENSITIVE);
	private static final String NEW_URI_EVENT = "newUriEvent";
	private static final String FILE_SUFFIX_HTM = "htm";
	private static final String TAG_FRAMESET = "<frameset";
	private static final String TAG_FRAMESET_UPPERC = "<FRAMESET";
	private static final String FILE_SUFFIX_JS = ".js";
	private static final String COMMAND_DOWNLOAD = "command.download";

	private VelocityContainer myContent = createVelocityContainer("index");
	private VelocityContainer eventVC = createVelocityContainer("event");
	private Panel newUriEventPanel;
	private Panel main;
	private VFSItem rootDir;

	// the latest encoding is saved since .js files loaded by the browser are
	// assumed to have the same encoding as the html page
	private String g_encoding;
	private OLog log = Tracing.createLoggerFor(this.getClass());
	private String frameId;
	// When textMarking is enabled we include some additional javaScript stuff to show toolTips as well in iframeContent.
	// This is only needed in course context
	private boolean enableTextmarking;
	private Mapper contentMapper;
	private String staticFilesPath;
	private String baseURI;			// base uri of contentMapper
	private String currentUri; 		// relative uri of currently loaded page in iframe
	private boolean checkForInlineEvent; // false when a new currentUri is set
	private boolean adjusteightAutomatically;
	private boolean rawContent = false;
	private String customCssURL;
	private String contentEncoding;
	private String jsEncoding;
	// users theme
	private String themeBaseUri;
	private long suppressEndlessReload;
	private String customHeaderContent;
	
	//download link
	private Link downloadLink;
	private boolean allowDownload = false;
	
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param fileRoot File that points to the root directory of the resource 
	 */
	public IFrameDisplayController(UserRequest ureq, WindowControl wControl, File fileRoot) {
		this(ureq, wControl, new LocalFolderImpl(fileRoot), null);
	}
	
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param fileRoot
	 * @param ores - send an OLATresourcable of the context (e.g. course) where the iframe runs and it will be checked if the user has textmarking (glossar) enabled in this course
	 */
	public IFrameDisplayController(UserRequest ureq, WindowControl wControl, File fileRoot, OLATResourceable ores) {
		this(ureq, wControl, new LocalFolderImpl(fileRoot), null, ores);
	}
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param rootDir VFSItem that points to the root folder of the resource
	 */
	public IFrameDisplayController(UserRequest ureq, WindowControl wControl, VFSContainer rootDir) {
		this(ureq, wControl, rootDir, null, null);
	}
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param rootDir
	 * @param ores - send an OLATresourcable of the context (e.g. course) where the iframe runs and it will be checked if the user has textmarking (glossar) enabled in this course
	 */
	public IFrameDisplayController(UserRequest ureq, WindowControl wControl, VFSContainer rootDir, OLATResourceable ores) {
		this(ureq, wControl, rootDir, null, ores);
	}
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param rootDir
	 * @param frameId if you need access to the iframe html id, provide it here
	 * @param enableTextmarking to enable textmakring of the content in the iframe enable it here
	 */
	public IFrameDisplayController(final UserRequest ureq, WindowControl wControl, VFSContainer rootDir, String frameId, OLATResourceable contextRecourcable) {
		// FIXME:fj:c performance: use a line iterator which finds the charset
		// statement and can switch encoding while reading it into a stringbuffer

		// FIXME:fj:b add a link to open the iframe in a new window
		super(ureq, wControl);
		this.enableTextmarking = TextMarkerManagerImpl.getInstance().isTextmarkingEnabled(ureq, contextRecourcable);
		
		//register this object for textMarking on/off events
		//TODO:gs how to unregister and where? unregister need ureq so dispose does not work
		if (contextRecourcable != null) {
			ureq.getUserSession().getSingleUserEventCenter().registerFor(this, getIdentity(), contextRecourcable);
		}
		
		// Set correct user content theme
		themeBaseUri = wControl.getWindowBackOffice().getWindow().getGuiTheme().getBaseURI();
		
		// Deliver js files via class path static dispatcher to enable browser caching
		staticFilesPath = ClassPathStaticDispatcher.getInstance().getMapperBasePath(this.getClass());
		
		//Delivers content files via local mapper to enable session based browser caching for at least this instance
		//FIXME:FG: implement named mapper concept based on business path to allow browser caching and distributed media server
		//TODO:gs may use the same contentMapper if users clicks again on the same singlePage, now each time a new Mapper gets created and 
		//therefore the browser can not reuse the cached elements
		contentMapper = new Mapper() {
			public MediaResource handle(String relPath, HttpServletRequest request) {
				String isPopUpParam = request.getParameter("olatraw");
				boolean isPopUp = false;
				if (isPopUpParam != null && isPopUpParam.equals("true")) isPopUp = true;
				return deliverFile(request, relPath, isPopUp);
			}			
		};
		
		String mapperID = VFSManager.getRealPath(rootDir);
		if (mapperID == null) {
			// can't cache mapper, no cacheable context available
			baseURI = registerMapper(contentMapper);
		} else {
			// Add classname to the file path to remove conflicts with other
			// usages of the same file path
			mapperID = this.getClass().getSimpleName() + ":" + mapperID;
			baseURI = registerCacheableMapper(mapperID, contentMapper);				
		}
		myContent.contextPut("baseURI", baseURI);
		newUriEventPanel = new Panel("newUriEvent");
		newUriEventPanel.setContent(eventVC);
		
		this.rootDir = rootDir;
		this.frameId = frameId;
		main = new Panel("iframemain");
		if (frameId == null) this.frameId = "ifdc" + hashCode();
		main.setContent(myContent);
		myContent.contextPut("frameId", this.frameId);
		myContent.put("newUriEvent", newUriEventPanel);
		// add default iframe height
		myContent.contextPut("iframeHeight", 600); // used as fallback
		adjusteightAutomatically = true; // default
		myContent.contextPut("adjustAutoHeight", Boolean.TRUE);
		
		// Add us as cycle listener to be notified when current dispatch cycle is
		// finished. we then need to add the css which is not yet defined at this
		// point
		getWindowControl().getWindowBackOffice().addCycleListener(this);
		//
		putInitialPanel(main);
	}

	/**
	 * Sets the start page, may be null, a relative or an absolute URI
	 * 
	 * @param newCurrentURI The currentURI to set
	 */
	public void setCurrentURI(String newCurrentURI) {
		// set new uri and the content dirty to redraw on screen
		changeCurrentURI(newCurrentURI, true);
	}
	
	/**
	 * Allow download for all types but html
	 * @param allow
	 */
	public void setAllowDownload(boolean allow) {
		this.allowDownload = allow;
		setPageDownload(isPageDownloadAllowed(currentUri));
	}
	
	/**
	 * Configuration method to use an explicit height for the iframe instead of
	 * the default automatic sizeing code. If you don't call this method, OLAT
	 * will try to size the iframe so that no scrollbars appear. In most cases
	 * this works. If it does not work, use this method to set an explicit height.
	 * <br />
	 * Set 0 to reset to automatic behaviour.
	 * 
	 * @param height
	 */
	public void setHeightPX(int height) {
		if (height == 0) {			
			myContent.contextPut("iframeHeight", 600);
			adjusteightAutomatically = true; 
			myContent.contextPut("adjustAutoHeight", Boolean.TRUE);			
			
		} else {
			myContent.contextPut("iframeHeight", height);
			adjusteightAutomatically = false; 
			myContent.contextPut("adjustAutoHeight", Boolean.FALSE);			
		}
	}
	
	public void setRawContent(boolean rawContent) {
		this.rawContent = rawContent;
	}
	
	public void setContentEncoding(String encoding) {
		this.contentEncoding = encoding;
	}
	
	public void setJSEncoding(String encoding) {
		this.jsEncoding = encoding;
	}

	/**
	 * Add a custom HTML header element. This string will be added into the HTML
	 * HEAD part of the HTML page. This could be CSS, JS or other header elements.
	 * 
	 * @param customHeaderContent A custom HEAD element or NULL
	 */
	public void setCustomHeaderContent(String customHeaderContent) {
		this.customHeaderContent = customHeaderContent;
	}


	/**
	 * Change the start page, may be null
	 * 
	 * @param currentURI
	 *            The currentURI to set
	 * @param forceLoading
	 *            true: force rendering of iframe velocity container, will
	 *            redraw everything; 
	 *            false: set new uri without redrawing
	 *            iframe. This implies that the content in the iframe has
	 *            already been loaded (e.g. by an in line user click)
	 */
	private void changeCurrentURI(String uri, boolean forceLoading) {
		if (uri == null) {
			uri = "";
			myContent.contextPut("isAbsoluteURI", Boolean.FALSE);
		} else if (uri.startsWith("http")) {
			// http and https urls are absolute urls that do not need be fetched from
			// the OLAT server, they retrieve their content from an external content server
			//TODO:gs a if an absolut uri is loaded the iframe should size to the default site, see functions.js stuff
			myContent.contextPut("isAbsoluteURI", Boolean.TRUE);
		} else {
			// Check for problematic URI that start with '/' (would lead to a logout (login screen)).
			if (uri.startsWith("/")) uri = uri.substring(1);
			myContent.contextPut("isAbsoluteURI", Boolean.FALSE);
		}
		// set new current uri and push to velocity
		this.currentUri = uri;
		myContent.contextPut("currentURI", this.currentUri);
		if (forceLoading) {
			// Serve new URI as currentURI, no need to check for inline events
			this.checkForInlineEvent = false;
		} else {
			// Don't redraw iframe
			myContent.setDirty(false);
			this.checkForInlineEvent = true;
		}
		
		setPageDownload(isPageDownloadAllowed(currentUri));
	}
	
	private boolean isPageDownloadAllowed(String uri) {
		if(!allowDownload || !StringHelper.containsNonWhitespace(uri)) {
			return false;
		}
		String uriLc = uri.toLowerCase();
		if(uriLc.endsWith(".html") || uriLc.endsWith(".htm") || uriLc.endsWith(".xhtml")) {
			return false;
		}
		return true;
	}
	
	private void setPageDownload(boolean allow) {
		if(allow) {
			if(downloadLink == null) {
				downloadLink = LinkFactory.createCustomLink(COMMAND_DOWNLOAD, COMMAND_DOWNLOAD, "", Link.NONTRANSLATED, myContent, this);
				downloadLink.setCustomEnabledLinkCSS("b_content_download");
				downloadLink.setTooltip(getTranslator().translate(COMMAND_DOWNLOAD), false);
			} else if (!downloadLink.isVisible()) {
				downloadLink.setVisible(true);
			}
			//update always the name
			downloadLink.setCustomDisplayText(currentUri);
		} else {
			if(downloadLink != null) {
				downloadLink.setVisible(false);
			}
		}
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == eventVC) {
			if (event.getCommand().equals(NEW_URI_EVENT)) {
				// This event gets triggered from the iframe content by calling a js function outside 
				// Get new uri from JS method and fire to parents
				String newUri = ureq.getModuleURI();
				int baseUriPos = newUri.indexOf(baseURI);
				if (baseUriPos != -1) {
					int newUriPos =  baseUriPos + baseURI.length();
					if (newUri.length() > newUriPos) {
						newUri = newUri.substring(newUriPos);
						String hash = ureq.getHttpReq().getParameter("hash");
						if (StringHelper.containsNonWhitespace(hash)) {
							// force iframe reload to fix truncated page problem
							changeCurrentURI(newUri + '#' + hash, true);
							fireEvent(ureq, new NewIframeUriEvent(newUri));													
						}
						if (newUri.startsWith("/")) {
							// clean newUri to make equals check work
							newUri = newUri.substring(1);
						}
						if (! newUri.equals(this.currentUri)) {
							changeCurrentURI(newUri, false);
							fireEvent(ureq, new NewIframeUriEvent(currentUri));													
						}
						// else probably a reload, no need to propagate new uri event
					}
					// else ?? don't do anything
				}
				// else ?? don't do anything
				//
				// don't mark as dirty to prevent re-rendering in AJAX mode - event was a
				// background event only
				eventVC.setDirty(false);
			}
		} else if (source == downloadLink) {
			
			MediaResource mediaResource = deliverFile(ureq.getHttpReq(), currentUri, false);
			if(mediaResource instanceof VFSMediaResource) {
				((VFSMediaResource)mediaResource).setDownloadable(true);
			}
			ureq.getDispatchResult().setResultingMediaResource(mediaResource);
		}
		
	}
	
	/**
	 * deliver the file (html, css, js) as MediaResource
	 * @param path
	 * @return
	 */
	/**
	 * TODO: firefox 2.0 has an strange error when the iframe is loaded the browser requests the first file always twice
	 * @param isPopUp 
	 */
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
		
		StringOutput sb = new StringOutput();
		//do not use parser and just check for css and script stuff myself and append just before body and head
		SimpleHtmlParser parser = new SimpleHtmlParser(page);
		if (!parser.isValidHtml()) {
			return page;
		}

		String docType = parser.getHtmlDocType();		
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
		if (this.enableTextmarking) {
			if (log.isDebug()) log.debug("Textmarking is enabled, including tooltips js files into iframe source...");
			sb.append("\n<script type=\"text/javascript\" src=\"");
			StaticMediaDispatcher.renderStaticURI(sb, "js/prototype/prototype.js");
			sb.append("\"></script>");
			sb.append("\n<script type=\"text/javascript\" src=\"");
			StaticMediaDispatcher.renderStaticURI(sb, "js/ext/adapter/prototype/ext-prototype-adapter.js");
			sb.append("\"></script>");
			sb.append("\n<link rel=\"stylesheet\" type=\"text/css\" href=\"");
			StaticMediaDispatcher.renderStaticURI(sb, "js/ext/resources/css/ext-all.css");
			sb.append("\"");
			if (docType != null && docType.indexOf("XHTML") > 0) sb.append("/"); // close tag only when xhtml to validate
			// Loading ExtJS minimalisic, only what's needed for the quick tips
			sb.append(">\n<script type=\"text/javascript\" src=\"");
			StaticMediaDispatcher.renderStaticURI(sb, "js/ext/pkgs/ext-core.js");
			sb.append("\"></script>");
			sb.append("\n<script type=\"text/javascript\" src=\"");
			StaticMediaDispatcher.renderStaticURI(sb, "js/ext/pkgs/ext-foundation.js");
			sb.append("\"></script>");
			sb.append("\n<script type=\"text/javascript\" src=\"");
			StaticMediaDispatcher.renderStaticURI(sb, "js/ext/pkgs/cmp-foundation.js");
			sb.append("\"></script>");
			sb.append("\n<script type=\"text/javascript\" src=\"");
			StaticMediaDispatcher.renderStaticURI(sb, "js/ext/pkgs/ext-dd.js");
			sb.append("\"></script>");
			sb.append("\n<script type=\"text/javascript\" src=\"");
			StaticMediaDispatcher.renderStaticURI(sb, "js/ext/pkgs/pkg-tips.js");
			sb.append("\"></script>");
			// Load glossary code now			
			sb.append("\n<script type=\"text/javascript\" id=\"textmarkerLib\" src=\"");
			sb.append( getWindowControl().getWindowBackOffice().getWindowManager().getMapPathFor(GlossaryMarkupItemController.class) ) ;
			sb.append("/js/glossaryhighlighter.js");
			sb.append("\"></script>");
			
			sb.append("\n<link rel=\"stylesheet\" type=\"text/css\" id=\"textmarkercss\" href=\"");
			sb.append( getWindowControl().getWindowBackOffice().getWindowManager().getMapPathFor(GlossaryMarkupItemController.class) ) ;
			sb.append("/css/textmarker.css\"");
			if (docType != null && docType.indexOf("XHTML") > 0) sb.append("/"); // close tag only when xhtml to validate
			sb.append(">\n");
		}
		
		// Load some iframe.js helper code
		sb.append("\n<script type=\"text/javascript\">\n/* <![CDATA[ */\n");
		// Set the iframe id, used by the resize function. Important to set before iframe.js is loaded
		sb.append("b_iframeid=\"").append(this.frameId).append("\";");
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
			sb.append("b_addOnloadEvent(b_enableTooltips);");
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

	@Override
	protected void doDispose() {
		//contentMapper get's unregistered automatically with basic controller
		// remove us as listener if not already done
		getWindowControl().getWindowBackOffice().removeCycleListener(this);
	}

	/**
	 * this event gets fired from the TextMarkerController when the user swiches on/off textmarking
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	public void event(Event event) {
		if (event instanceof MultiUserEvent) {
			if (event.getCommand().equals("glossaryOn")) {
				this.enableTextmarking = true;
			} else if (event.getCommand().equals("glossaryOff")) {
				this.enableTextmarking = false;
			}
		}
		else if (event.equals(Window.BEFORE_INLINE_RENDERING)){
			// Set the custom CSS URL that is used by the current tab or site if
			// available. The reason why we do this here and not in the constructor is
			// that during the constructing phase this property is not yet set on the
			// window. 
			Window myWindow = getWindowControl().getWindowBackOffice().getWindow();
			CustomCSS currentCustomCSS = (CustomCSS) myWindow.getAttribute(BaseFullWebappController.CURRENT_CUSTOM_CSS_KEY);
			if (currentCustomCSS != null)	customCssURL = currentCustomCSS.getCSSURLIFrame();
			// done, remove us as listener
			getWindowControl().getWindowBackOffice().removeCycleListener(this);
		}
	}
	
	public class Page {
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
