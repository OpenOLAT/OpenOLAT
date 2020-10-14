/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.olat.core.commons.services.webdav.servlets;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.Normalizer;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.webdav.WebDAVDispatcher;
import org.olat.core.commons.services.webdav.WebDAVManager;
import org.olat.core.commons.services.webdav.WebDAVModule;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.vfs.QuotaExceededException;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.lock.LockInfo;
import org.olat.core.util.vfs.lock.LockResult;
import org.olat.core.util.vfs.lock.VFSLockManagerImpl;
import org.olat.core.util.xml.XMLFactories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Servlet which adds support for WebDAV level 2. All the basic HTTP requests
 * are handled by the DefaultServlet. The WebDAVServlet must not be used as the
 * default servlet (ie mapped to '/') as it will not work in this configuration.
 * <p/>
 * Mapping a subpath (e.g. <code>/webdav/*</code> to this servlet has the effect
 * of re-mounting the entire web application under that sub-path, with WebDAV
 * access to all the resources. This <code>WEB-INF</code> and <code>META-INF</code>
 * directories are protected in this re-mounted resource tree.
 * <p/>
 * To enable WebDAV for a context add the following to web.xml:
 * <pre>
 * &lt;servlet&gt;
 *  &lt;servlet-name&gt;webdav&lt;/servlet-name&gt;
 *  &lt;servlet-class&gt;org.apache.catalina.servlets.WebdavServlet&lt;/servlet-class&gt;
 *    &lt;init-param&gt;
 *      &lt;param-name&gt;debug&lt;/param-name&gt;
 *      &lt;param-value&gt;0&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *      &lt;param-name&gt;listings&lt;/param-name&gt;
 *      &lt;param-value&gt;false&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *  &lt;/servlet&gt;
 *  &lt;servlet-mapping&gt;
 *    &lt;servlet-name&gt;webdav&lt;/servlet-name&gt;
 *    &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *  &lt;/servlet-mapping&gt;
 * </pre>
 * This will enable read only access. To enable read-write access add:
 * <pre>
 *  &lt;init-param&gt;
 *    &lt;param-name&gt;readonly&lt;/param-name&gt;
 *    &lt;param-value&gt;false&lt;/param-value&gt;
 *  &lt;/init-param&gt;
 * </pre>
 * To make the content editable via a different URL, use the following
 * mapping:
 * <pre>
 *  &lt;servlet-mapping&gt;
 *    &lt;servlet-name&gt;webdav&lt;/servlet-name&gt;
 *    &lt;url-pattern&gt;/webdavedit/*&lt;/url-pattern&gt;
 *  &lt;/servlet-mapping&gt;
 * </pre>
 * By default access to /WEB-INF and META-INF are not available via WebDAV. To
 * enable access to these URLs, use add:
 * <pre>
 *  &lt;init-param&gt;
 *    &lt;param-name&gt;allowSpecialPaths&lt;/param-name&gt;
 *    &lt;param-value&gt;true&lt;/param-value&gt;
 *  &lt;/init-param&gt;
 * </pre>
 * Don't forget to secure access appropriately to the editing URLs, especially
 * if allowSpecialPaths is used. With the mapping configuration above, the
 * context will be accessible to normal users as before. Those users with the
 * necessary access will be able to edit content available via
 * http://host:port/context/content using
 * http://host:port/context/webdavedit/content
 *
 * @author Remy Maucherat
 * @version $Id$
 */
@Service("webDAVDispatcher")
public class WebDAVDispatcherImpl
    extends DefaultDispatcher implements WebDAVDispatcher, Dispatcher {

    private static final long serialVersionUID = 1L;
    private static final Logger log = Tracing.createLoggerFor(WebDAVDispatcherImpl.class);


    // -------------------------------------------------------------- Constants

    private static final String METHOD_PROPFIND = "PROPFIND";
    private static final String METHOD_PROPPATCH = "PROPPATCH";
    private static final String METHOD_MKCOL = "MKCOL";
    private static final String METHOD_COPY = "COPY";
    private static final String METHOD_MOVE = "MOVE";
    private static final String METHOD_LOCK = "LOCK";
    private static final String METHOD_UNLOCK = "UNLOCK";
    private static final String METHOD_DELETE = "DELETE";
    private static final String METHOD_HEAD = "HEAD";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_OPTIONS = "OPTIONS";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_PUT = "PUT";

    /**
     * PROPFIND - Specify a property mask.
     */
    private static final int FIND_BY_PROPERTY = 0;


    /**
     * PROPFIND - Display all properties.
     */
    private static final int FIND_ALL_PROP = 1;


    /**
     * PROPFIND - Return property names.
     */
    private static final int FIND_PROPERTY_NAMES = 2;


    /**
     * Create a new lock.
     */
    private static final int LOCK_CREATION = 0;


    /**
     * Refresh lock.
     */
    private static final int LOCK_REFRESH = 1;


    /**
     * Default lock timeout value.
     */
    private static final int DEFAULT_TIMEOUT = 3600;


    /**
     * Maximum lock timeout.
     */
    private static final int MAX_TIMEOUT = 604800;


    /**
     * Default namespace.
     */
    protected static final String DEFAULT_NAMESPACE = "DAV:";


    /**
     * Simple date format for the creation date ISO representation (partial).
     */
    protected static final ConcurrentDateFormat creationDateFormat =
        new ConcurrentDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US,
                TimeZone.getTimeZone("GMT"));


    // ----------------------------------------------------- Instance Variables

    /**
     * Default depth in spec is infinite. Limit depth to 3 by default as
     * infinite depth makes operations very expensive.
     */
    public static final int maxDepth = 3;


    /**
     * Is access allowed via WebDAV to the special paths (/WEB-INF and
     * /META-INF)?
     */
    private boolean allowSpecialPaths = false;
    
    @Autowired
    private VFSLockManagerImpl lockManager;
    @Autowired
    private WebDAVManager webDAVManager;
    @Autowired
    private WebDAVModule webDAVModule;

    public WebDAVDispatcherImpl() {
    	//
    }

	@Override
	protected WebResourceRoot getResources(HttpServletRequest req) {
		return webDAVManager.getWebDAVRoot(req);
	}

	/**
     * Return JAXP document builder instance.
     */
    protected DocumentBuilder getDocumentBuilder(HttpServletRequest req)
        throws ServletException {
        DocumentBuilder documentBuilder = null;
        try {
        	DocumentBuilderFactory documentBuilderFactory = XMLFactories.newDocumentBuilderFactory();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setExpandEntityReferences(false);
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setEntityResolver(
                    new WebdavResolver(req.getServletContext()));
        } catch(ParserConfigurationException e) {
            throw new ServletException("webdavservlet.jaxpfailed");
        }
        return documentBuilder;
    }

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		if (webDAVManager == null) {
			resp.setStatus(WebdavStatus.SC_INTERNAL_SERVER_ERROR);
		} else if(webDAVModule == null || !webDAVModule.isEnabled() || isUserAgentExcluded(req)) {
			resp.setStatus(WebdavStatus.SC_FORBIDDEN);
		} else if (webDAVManager.handleAuthentication(req, resp)) {
			webdavService(req, resp);
		} else {
			//the method handleAuthentication will send the challenges for authentication
		}
	}
	
	private boolean isUserAgentExcluded(HttpServletRequest req) {
		String userAgent = ServletUtil.getUserAgent(req);
		if(!StringHelper.containsNonWhitespace(userAgent)) {
			userAgent = "";
		}
		String[] blackList = webDAVModule.getUserAgentExclusionListArray();
		for(String blackListedAgent:blackList) {
			if((blackListedAgent.length() < 2 && userAgent.equalsIgnoreCase(blackListedAgent))
					|| (blackListedAgent.length() >= 2 && userAgent.contains(blackListedAgent))) {
				return true;
			}
		}
		return false;
	}

	/**
     * Handles the special WebDAV methods.
     */
    private void webdavService(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        final String path = getRelativePath(req);

        // Block access to special subdirectories.
        // DefaultServlet assumes it services resources from the root of the web app
        // and doesn't add any special path protection
        // WebdavServlet remounts the webapp under a new path, so this check is
        // necessary on all methods (including GET).
        if (isSpecialPath(path)) {
            resp.setStatus(WebdavStatus.SC_NOT_FOUND);
            return;
        }

        final String method = req.getMethod();

        if (log.isDebugEnabled()) {
            log.debug("[" + method + "] " + path);
        }
        
        if (method.equals(METHOD_PROPFIND)) {
            doPropfind(req, resp);
        } else if (method.equals(METHOD_PROPPATCH)) {
            doProppatch(req, resp);
        } else if (method.equals(METHOD_MKCOL)) {
            doMkcol(req, resp);
        } else if (method.equals(METHOD_COPY)) {
            doCopy(req, resp);
        } else if (method.equals(METHOD_MOVE)) {
            doMove(req, resp);
        } else if (method.equals(METHOD_LOCK)) {
            doLock(req, resp);
        } else if (method.equals(METHOD_UNLOCK)) {
            doUnlock(req, resp);
        } else if (method.equals(METHOD_GET)) {
            doGet(req, resp);
        } else if (method.equals(METHOD_HEAD)) {
            doHead(req, resp);
        } else if (method.equals(METHOD_POST)) {
            doPost(req, resp);
        } else if (method.equals(METHOD_PUT)) {
            doPut(req, resp);
        } else if (method.equals(METHOD_DELETE)) {
            doDelete(req, resp);
        } else if (method.equals(METHOD_OPTIONS)) {
            doOptions(req,resp);  
        }
    }


    /**
     * Checks whether a given path refers to a resource under
     * <code>WEB-INF</code> or <code>META-INF</code>.
     * @param path the full path of the resource being accessed
     * @return <code>true</code> if the resource specified is under a special path
     */
    private final boolean isSpecialPath(final String path) {
        return !allowSpecialPaths && (
                path.toUpperCase(Locale.ENGLISH).startsWith("/WEB-INF") ||
                path.toUpperCase(Locale.ENGLISH).startsWith("/META-INF"));
    }


    @Override
    protected boolean checkIfHeaders(HttpServletRequest request,
                                     HttpServletResponse response,
                                     WebResource resource)
        throws IOException {

        if (!super.checkIfHeaders(request, response, resource))
            return false;

        return true;
    }


    /**
     * Override the DefaultServlet implementation and only use the PathInfo. If
     * the ServletPath is non-null, it will be because the WebDAV servlet has
     * been mapped to a url other than /* to configure editing at different url
     * than normal viewing.
     *
     * @param request The servlet request we are processing
     */
    @Override
    protected String getRelativePath(HttpServletRequest request) {
        // Are we being processed by a RequestDispatcher.include()?
        if (request.getAttribute(
                RequestDispatcher.INCLUDE_REQUEST_URI) != null) {
            String result = (String) request.getAttribute(
                    RequestDispatcher.INCLUDE_PATH_INFO);
            if ((result == null) || (result.equals("")))
                result = "/";
            return (result);
        }

        // No, extract the desired path directly from the request
        String result = request.getPathInfo();
        if ((result == null) || (result.equals(""))) {
            result = "/";
        }
        
        result = Normalizer.normalize(result, Normalizer.Form.NFC);
        return (result);

    }


    /**
     * Determines the prefix for standard directory GET listings.
     */
    @Override
    protected String getPathPrefix(final HttpServletRequest request) {
        // Repeat the servlet path (e.g. /webdav/) in the listing path
        String contextPath = request.getContextPath();
        if (request.getServletPath() !=  null) {
            contextPath = contextPath + request.getServletPath();
        }
        return contextPath;
    }


    /**
     * OPTIONS Method.
     *
     * @param req The request
     * @param resp The response
     * @throws ServletException If an error occurs
     * @throws IOException If an IO error occurs
     */
    public void doOptions(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        resp.addHeader("DAV", "1,2");

        StringBuilder methodsAllowed = determineMethodsAllowed(req);

        resp.addHeader("Allow", methodsAllowed.toString());
        resp.addHeader("MS-Author-Via", "DAV");
    }

    @Override
	public void doRootOptions(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
        resp.addHeader("DAV", "1,2");
        resp.setHeader("Allow", "OPTIONS, GET, HEAD, POST, DELETE, TRACE, PROPPATCH, COPY, MOVE, LOCK, UNLOCK");
        
        resp.addHeader("MS-Author-Via", "DAV");
		resp.setDateHeader("Date", new Date().getTime());
        resp.setHeader("Content-Length", "0");
        resp.setContentLength(0);
        resp.setStatus(200);
	}

	@Override
	public void doWebdavOptions(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
        resp.addHeader("DAV", "1,2");
        resp.setHeader("Allow", "PROPFIND, OPTIONS");
        resp.addHeader("MS-Author-Via", "DAV");
		resp.setDateHeader("Date", new Date().getTime());
        resp.setHeader("Content-Length", "0");
        resp.setContentLength(0);
        resp.setStatus(200);
	}

	/**
     * PROPFIND Method.
     */
    public void doPropfind(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        String path = getRelativePath(req);
        if (path.length() > 1 && path.endsWith("/"))
            path = path.substring(0, path.length() - 1);

        // Properties which are to be displayed.
        Vector<String> properties = null;
        // Propfind depth
        int depth = maxDepth;
        // Propfind type
        int type = FIND_ALL_PROP;

        String depthStr = req.getHeader("Depth");

        if (depthStr == null) {
            depth = maxDepth;
        } else {
            if (depthStr.equals("0")) {
                depth = 0;
            } else if (depthStr.equals("1")) {
                depth = 1;
            } else if (depthStr.equals("infinity")) {
                depth = maxDepth;
            }
        }

        Node propNode = null;

        if (req.getContentLength() > 0) {
            DocumentBuilder documentBuilder = getDocumentBuilder(req);

            try {
                Document document = documentBuilder.parse
                    (new InputSource(req.getInputStream()));

                // Get the root element of the document
                Element rootElement = document.getDocumentElement();
                NodeList childList = rootElement.getChildNodes();

                for (int i=0; i < childList.getLength(); i++) {
                    Node currentNode = childList.item(i);
                    switch (currentNode.getNodeType()) {
                    case Node.TEXT_NODE:
                        break;
                    case Node.ELEMENT_NODE:
                        if (currentNode.getNodeName().endsWith("prop")) {
                            type = FIND_BY_PROPERTY;
                            propNode = currentNode;
                        }
                        if (currentNode.getNodeName().endsWith("propname")) {
                            type = FIND_PROPERTY_NAMES;
                        }
                        if (currentNode.getNodeName().endsWith("allprop")) {
                            type = FIND_ALL_PROP;
                        }
                        break;
                    }
                }
            } catch (SAXException e) {
                // Something went wrong - bad request
                resp.setStatus(WebdavStatus.SC_BAD_REQUEST);
                return;
            } catch (IOException e) {
                // Something went wrong - bad request
                resp.setStatus(WebdavStatus.SC_BAD_REQUEST);
                return;
            }
        }

        if (type == FIND_BY_PROPERTY) {
            properties = new Vector<>();
            // propNode must be non-null if type == FIND_BY_PROPERTY
            NodeList childList = propNode.getChildNodes();

            for (int i=0; i < childList.getLength(); i++) {
                Node currentNode = childList.item(i);
                switch (currentNode.getNodeType()) {
                case Node.TEXT_NODE:
                    break;
                case Node.ELEMENT_NODE:
                    String nodeName = currentNode.getNodeName();
                    String propertyName = null;
                    if (nodeName.indexOf(':') != -1) {
                        propertyName = nodeName.substring
                            (nodeName.indexOf(':') + 1);
                    } else {
                        propertyName = nodeName;
                    }
                    // href is a live property which is handled differently
                    properties.addElement(propertyName);
                    break;
                }
            }

        }

        WebResourceRoot resources = getResources(req);
        WebResource resource = resources.getResource(path);

        if (!resource.exists()) {
            int slash = path.lastIndexOf('/');
            if (slash != -1) {
                String parentPath = path.substring(0, slash);
                WebResource parentResource = resources.getResource(parentPath);
                List<String> currentLockNullResources = lockManager.getLockNullResource(parentResource);
                if (currentLockNullResources != null) {
                    for(String lockNullPath:currentLockNullResources) {
                        if (lockNullPath.equals(path)) {
                            resp.setStatus(WebdavStatus.SC_MULTI_STATUS);
                            resp.setContentType("text/xml; charset=UTF-8");
                            // Create multistatus object
                            XMLWriter generatedXML = new XMLWriter(resp.getWriter());
                            generatedXML.writeXMLHeader();
                            generatedXML.writeElement("D", DEFAULT_NAMESPACE, "multistatus", XMLWriter.OPENING);
                            parseLockNullProperties(req, generatedXML, lockNullPath, type, properties);
                            generatedXML.writeElement("D", "multistatus", XMLWriter.CLOSING);
                            generatedXML.sendData();
                            return;
                        }
                    }
                }
            }
        }

        if (!resource.exists()) {
        	resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        resp.setStatus(WebdavStatus.SC_MULTI_STATUS);

        resp.setContentType("text/xml; charset=UTF-8");

        // Create multistatus object
        XMLWriter generatedXML = new XMLWriter(resp.getWriter());
        generatedXML.writeXMLHeader();

        generatedXML.writeElement("D", DEFAULT_NAMESPACE, "multistatus", XMLWriter.OPENING);

        if (depth == 0) {
            parseProperties(req, generatedXML, path, type,
                            properties);
        } else {
            // The stack always contains the object of the current level
            Stack<String> stack = new Stack<>();
            stack.push(path);

            // Stack of the objects one level below
            Stack<String> stackBelow = new Stack<>();

            while ((!stack.isEmpty()) && (depth >= 0)) {

                final String currentPath = stack.pop();
                parseProperties(req, generatedXML, currentPath, type, properties);

                resource = resources.getResource(currentPath);

                if (resource.isDirectory() && (depth > 0)) {

                    Collection<VFSItem> entries = resources.list(currentPath);
                    for (VFSItem entry : entries) {
                        String newPath = currentPath;
                        if (!(newPath.endsWith("/")))
                                newPath += "/";
                        newPath += entry.getName();
                        stackBelow.push(newPath);
                    }

                    // Displaying the lock-null resources present in that
                    // collection
                    String lockPath = currentPath;
                    if (lockPath.endsWith("/")) {
                        lockPath = lockPath.substring(0, lockPath.length() - 1);
                    }
                    
                    List<String> currentLockNullResources = lockManager.getLockNullResource(resource);
                    if (currentLockNullResources != null) {
                        for(String lockNullPath : currentLockNullResources) {
                            parseLockNullProperties(req, generatedXML, lockNullPath, type, properties);
                        }
                    }
                }

                if (stack.isEmpty()) {
                    depth--;
                    stack = stackBelow;
                    stackBelow = new Stack<>();
                }

                generatedXML.sendData();

            }
        }

        generatedXML.writeElement("D", "multistatus", XMLWriter.CLOSING);

        generatedXML.sendData();

    }


    /**
     * PROPPATCH Method.
     */
    public void doProppatch(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        if (isLocked(req)) {
            resp.setStatus(WebdavStatus.SC_LOCKED);
            return;
        }

        String path = getRelativePath(req);
        WebResourceRoot resources = getResources(req);
        if(!resources.canWrite(path)) {
            resp.setStatus(WebdavStatus.SC_FORBIDDEN);
        	return;
        }
        
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        resp.setStatus(WebdavStatus.SC_MULTI_STATUS);
        resp.setContentType("text/xml; charset=UTF-8");
        
        /*
   <?xml version="1.0" encoding="utf-8" ?>
   <D:multistatus xmlns:D="DAV:"
   xmlns:Z="http://www.w3.com/standards/z39.50">
     <D:response>
          <D:href>http://www.foo.com/bar.html</D:href>
          <D:propstat>
               <D:prop><Z:Authors/></D:prop>
               <D:status>HTTP/1.1 424 Failed Dependency</D:status>
          </D:propstat>
          <D:propstat>
               <D:prop><Z:Copyright-Owner/></D:prop>
               <D:status>HTTP/1.1 409 Conflict</D:status>
          </D:propstat>
          <D:responsedescription> Copyright Owner can not be deleted or altered.</D:responsedescription>
     </D:response>
   </D:multistatus>
        */
        
        XMLWriter generatedXML = new XMLWriter(resp.getWriter());
        generatedXML.writeXMLHeader();
        generatedXML.writeElement("D", DEFAULT_NAMESPACE, "multistatus", XMLWriter.OPENING);
        
        parseProperties( req, generatedXML, path, 32, new Vector<String>());

        generatedXML.writeElement("D", "multistatus", XMLWriter.CLOSING);
        generatedXML.sendData();
    }


    /**
     * MKCOL Method.
     */
    public void doMkcol(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        if (isLocked(req)) {
            resp.setStatus(WebdavStatus.SC_LOCKED);
            return;
        }

        final String path = getRelativePath(req);
        final WebResourceRoot resources = getResources(req);
        final WebResource resource = resources.getResource(path);

        // Can't create a collection if a resource already exists at the given
        // path
        if (resource.exists()) {
            // Get allowed methods
            StringBuilder methodsAllowed = determineMethodsAllowed(req);

            resp.addHeader("Allow", methodsAllowed.toString());

            resp.setStatus(WebdavStatus.SC_METHOD_NOT_ALLOWED);
            return;
        }

        if (req.getContentLength() > 0) {
            DocumentBuilder documentBuilder = getDocumentBuilder(req);
            try {
                documentBuilder.parse(new InputSource(req.getInputStream()));
                resp.setStatus(WebdavStatus.SC_NOT_IMPLEMENTED);
                return;

            } catch(SAXException saxe) {
                // Parse error - assume invalid content
                resp.setStatus(WebdavStatus.SC_UNSUPPORTED_MEDIA_TYPE);
                return;
            }
        }

        if (resources.mkdir(path)) {
            resp.setStatus(WebdavStatus.SC_CREATED);
            // Removing any lock-null resource which would be present
            lockManager.removeLockNullResource(resource);
        } else {
            resp.setStatus(WebdavStatus.SC_CONFLICT,
                           WebdavStatus.getStatusText
                           (WebdavStatus.SC_CONFLICT));
        }
    }


    /**
     * DELETE Method.
     */
    public void doDelete(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        if (isLocked(req)) {
            resp.setStatus(WebdavStatus.SC_LOCKED);
            return;
        }
        
        String path = this.getRelativePath(req);
        WebResourceRoot resources = this.getResources(req);
        if(!resources.canDelete(path)) {
            resp.setStatus(WebdavStatus.SC_FORBIDDEN);
            return;
        }

        deleteResource(req, resp);
    }
    
    /**
     * Process a HEAD request for the specified resource.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    protected void doHead(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
        // Serve the requested resource, without the data content
    	boolean serveContent = DispatcherType.INCLUDE.equals(request.getDispatcherType());
        serveResource(request, response, serveContent, fileEncoding);
    }
  
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		serveResource(request, response, true, fileEncoding);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		serveResource(request, response, true, fileEncoding);
	}

	/**
     * Process a PUT request for the specified resource.
     *
     * @param req The servlet request we are processing
     * @param resp The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    public void doPut(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        if (isLocked(req)) {
            resp.setStatus(WebdavStatus.SC_LOCKED);
            return;
        }

        final String path = getRelativePath(req);
        final WebResourceRoot resources = getResources(req);
    	if (!resources.canWrite(path)) {
    		resp.setStatus(WebdavStatus.SC_FORBIDDEN);
    		return;
    	}
       
        final WebResource resource = resources.getResource(path);
        Range range = parseContentRange(req, resp);
        InputStream resourceInputStream = null;

        try {
            // Append data specified in ranges to existing content for this
            // resource - create a temp. file on the local filesystem to
            // perform this operation
            // Assume just one range is specified for now
            if (range != null) {
                File contentFile = executePartialPut(req, range, path);
                resourceInputStream = new FileInputStream(contentFile);
            } else {
                resourceInputStream = req.getInputStream();
            }

            if (resources.write(path, resourceInputStream, true, null)) {
                if (resource.exists()) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    resp.setStatus(HttpServletResponse.SC_CREATED);
                    PrintWriter writer = resp.getWriter();
                    writer.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n")
                    	.append("<html><head>\n")
                    	.append("<title>201 Created</title>\n")
                    	.append("</head><body>\n")
                    	.append("<h1>Created</h1>\n")
                    	.append("<p>Resource ").append(path).append(" created.</p>\n")
                    	.append("</body></html>\n");
                    resp.setContentType("text/html; charset=ISO-8859-1");
                    
                    String location = Settings.getServerContextPathURI() + path;
                    resp.setHeader("Location", location);
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
            }
        } catch(QuotaExceededException e) {
            resp.setStatus(WebdavStatus.SC_INSUFFICIENT_STORAGE);
        } finally {
            if (resourceInputStream != null) {
                try {
                    resourceInputStream.close();
                } catch (IOException ioe) {
                    // Ignore
                }
            }
        }

        // Removing any lock-null resource which would be present
        lockManager.removeLockNullResource(resource);
    }
    
    /**
     * Handle a partial PUT.  New content specified in request is appended to
     * existing content in oldRevisionContent (if present). This code does
     * not support simultaneous partial updates to the same resource.
     */
    private File executePartialPut(HttpServletRequest req, Range range, String path)
    throws IOException {

        // Append data specified in ranges to existing content for this
        // resource - create a temp. file on the local filesystem to
        // perform this operation
        File tempDir = (File) req.getServletContext().getAttribute(ServletContext.TEMPDIR);
        
        // Convert all '/' characters to '.' in resourcePath
        String convertedResourcePath = path.replace('/', '.');
        File contentFile = new File(tempDir, convertedResourcePath);
        if (contentFile.createNewFile()) {
            // Clean up contentFile when Tomcat is terminated
            contentFile.deleteOnExit();
        }

        RandomAccessFile randAccessContentFile =
            new RandomAccessFile(contentFile, "rw");

        WebResource oldResource = getResources(req).getResource(path);

        // Copy data in oldRevisionContent to contentFile
        if (oldResource.isFile()) {
            BufferedInputStream bufOldRevStream =
                new BufferedInputStream(oldResource.getInputStream(),
                        BUFFER_SIZE);

            int numBytesRead;
            byte[] copyBuffer = new byte[BUFFER_SIZE];
            while ((numBytesRead = bufOldRevStream.read(copyBuffer)) != -1) {
                randAccessContentFile.write(copyBuffer, 0, numBytesRead);
            }

            bufOldRevStream.close();
        }

        randAccessContentFile.setLength(range.length);

        // Append data in request input stream to contentFile
        randAccessContentFile.seek(range.start);
        int numBytesRead;
        byte[] transferBuffer = new byte[BUFFER_SIZE];
        BufferedInputStream requestBufInStream =
            new BufferedInputStream(req.getInputStream(), BUFFER_SIZE);
        while ((numBytesRead = requestBufInStream.read(transferBuffer)) != -1) {
            randAccessContentFile.write(transferBuffer, 0, numBytesRead);
        }
        randAccessContentFile.close();
        requestBufInStream.close();

        return contentFile;
    }

    /**
     * COPY Method.
     */
    public void doCopy(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String path = getRelativePath(req);
        WebResourceRoot resources = getResources(req);
    	if (resources.canWrite(path)) {
    		copyResource(req, resp, false);
    	} else {
    		resp.setStatus(WebdavStatus.SC_FORBIDDEN);
    	}
    }


    /**
     * MOVE Method.
     */
    public void doMove(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        if (isLocked(req)) {
            resp.setStatus(WebdavStatus.SC_LOCKED);
            return;
        }

        String path = getRelativePath(req);
        WebResourceRoot resources = this.getResources(req);
        if(!resources.canRename(path)) {
            resp.setStatus(WebdavStatus.SC_FORBIDDEN);
            return;
        }
        
        if (copyResource(req, resp, true)) {
            deleteResource(path, req, resp, false);
        }
    }


    /**
     * LOCK Method.
     */
    public void doLock(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        if(isLocked(req)) {
            resp.setStatus(WebdavStatus.SC_LOCKED);
            return;
        }

        final String path = getRelativePath(req);
        final WebResourceRoot resources = getResources(req);
        if(!resources.canWrite(path)) {
            resp.setStatus(WebdavStatus.SC_FORBIDDEN);
        	return;
        }
        
        
        final WebResource resource = resources.getResource(path);
    	UserSession usess = webDAVManager.getUserSession(req);
        LockResult lockResult = lockManager.lock(resource, usess.getIdentity());
        if(!lockResult.isAcquired()) {
            resp.setStatus(WebdavStatus.SC_LOCKED);
            return;
        }

        LockInfo lock = lockResult.getLockInfo();
        lock.setWebResource(resource);

        // Parsing lock request

        // Parsing depth header

        String depthStr = req.getHeader("Depth");

        if (depthStr == null) {
            lock.setDepth(maxDepth);
        } else {
            if (depthStr.equals("0")) {
                lock.setDepth(0);
            } else {
                lock.setDepth(maxDepth);
            }
        }
        
        if(log.isDebugEnabled()) {
        	log.debug("Lock the ressource: " + path + " with depth:" + lock.getDepth());
        }

        // Parsing timeout header

        int lockDuration = DEFAULT_TIMEOUT;
        String lockDurationStr = req.getHeader("Timeout");
        if (lockDurationStr == null) {
            lockDuration = DEFAULT_TIMEOUT;
        } else {
            int commaPos = lockDurationStr.indexOf(",");
            // If multiple timeouts, just use the first
            if (commaPos != -1) {
                lockDurationStr = lockDurationStr.substring(0,commaPos);
            }
            if (lockDurationStr.startsWith("Second-")) {
                lockDuration =
                    (new Integer(lockDurationStr.substring(7))).intValue();
            } else {
                if (lockDurationStr.equalsIgnoreCase("infinity")) {
                    lockDuration = MAX_TIMEOUT;
                } else {
                    try {
                        lockDuration =
                            (new Integer(lockDurationStr)).intValue();
                    } catch (NumberFormatException e) {
                        lockDuration = MAX_TIMEOUT;
                    }
                }
            }
            if (lockDuration == 0) {
                lockDuration = DEFAULT_TIMEOUT;
            }
            if (lockDuration > MAX_TIMEOUT) {
                lockDuration = MAX_TIMEOUT;
            }
        }
        lock.setExpiresAt(System.currentTimeMillis() + (lockDuration * 1000));

        int lockRequestType = LOCK_CREATION;

        Node lockInfoNode = null;

        DocumentBuilder documentBuilder = getDocumentBuilder(req);

        try {
            Document document = documentBuilder.parse(new InputSource(req.getInputStream()));

            // Get the root element of the document
            Element rootElement = document.getDocumentElement();
            lockInfoNode = rootElement;
        } catch (IOException e) {
            lockRequestType = LOCK_REFRESH;
        } catch (SAXException e) {
            lockRequestType = LOCK_REFRESH;
        }

        if (lockInfoNode != null) {

            // Reading lock information

            NodeList childList = lockInfoNode.getChildNodes();
            StringWriter strWriter = null;
            DOMWriter domWriter = null;

            Node lockScopeNode = null;
            Node lockTypeNode = null;
            Node lockOwnerNode = null;

            for (int i=0; i < childList.getLength(); i++) {
                Node currentNode = childList.item(i);
                switch (currentNode.getNodeType()) {
                case Node.TEXT_NODE:
                    break;
                case Node.ELEMENT_NODE:
                    String nodeName = currentNode.getNodeName();
                    if (nodeName.endsWith("lockscope")) {
                        lockScopeNode = currentNode;
                    }
                    if (nodeName.endsWith("locktype")) {
                        lockTypeNode = currentNode;
                    }
                    if (nodeName.endsWith("owner")) {
                        lockOwnerNode = currentNode;
                    }
                    break;
                }
            }

            if (lockScopeNode != null) {

                childList = lockScopeNode.getChildNodes();
                for (int i=0; i < childList.getLength(); i++) {
                    Node currentNode = childList.item(i);
                    switch (currentNode.getNodeType()) {
                    case Node.TEXT_NODE:
                        break;
                    case Node.ELEMENT_NODE:
                        String tempScope = currentNode.getNodeName();
                        if (tempScope.indexOf(':') != -1) {
                            lock.setScope(tempScope.substring(tempScope.indexOf(':') + 1));
                        } else {
                            lock.setScope(tempScope);
                        }
                        break;
                    }
                }

                if (lock.getScope() == null) {
                    // Bad request
                    resp.setStatus(WebdavStatus.SC_BAD_REQUEST);
                }

            } else {
                // Bad request
                resp.setStatus(WebdavStatus.SC_BAD_REQUEST);
            }

            if (lockTypeNode != null) {

                childList = lockTypeNode.getChildNodes();
                for (int i=0; i < childList.getLength(); i++) {
                    Node currentNode = childList.item(i);
                    switch (currentNode.getNodeType()) {
                    case Node.TEXT_NODE:
                        break;
                    case Node.ELEMENT_NODE:
                        String tempType = currentNode.getNodeName();
                        if (tempType.indexOf(':') != -1) {
                            lock.setType(tempType.substring(tempType.indexOf(':') + 1));
                        } else {
                            lock.setType(tempType);
                        }
                        break;
                    }
                }

                if (lock.getType() == null) {
                    // Bad request
                    resp.setStatus(WebdavStatus.SC_BAD_REQUEST);
                }

            } else {
                // Bad request
                resp.setStatus(WebdavStatus.SC_BAD_REQUEST);
            }

            if (lockOwnerNode != null) {

                childList = lockOwnerNode.getChildNodes();
                for (int i=0; i < childList.getLength(); i++) {
                    Node currentNode = childList.item(i);
                    switch (currentNode.getNodeType()) {
                    case Node.TEXT_NODE:
                        lock.setOwner(lock.getOwner() + currentNode.getNodeValue());
                        break;
                    case Node.ELEMENT_NODE:
                        strWriter = new StringWriter();
                        domWriter = new DOMWriter(strWriter, true);
                        domWriter.print(currentNode);
                        lock.setOwner(lock.getOwner() + strWriter.toString());
                        break;
                    }
                }

                if (lock.getOwner() == null) {
                    // Bad request
                    resp.setStatus(WebdavStatus.SC_BAD_REQUEST);
                }

            } else {
                lock.setOwner("");
            }

        }

       

        Iterator<LockInfo> locksList = null;

        if (lockRequestType == LOCK_CREATION) {

            // Generating lock id
            
            String lockToken = lockManager.generateLockToken(lock, usess.getIdentity());
            if (resource.isDirectory() && lock.getDepth() == maxDepth) {

                // Locking a collection (and all its member resources)

                // Checking if a child resource of this collection is
                // already locked
                List<String> lockPaths = new Vector<>();
                locksList = lockManager.getCollectionLocks();
                while (locksList.hasNext()) {
                    LockInfo currentLock = locksList.next();
                    if (currentLock.hasExpired()) {
                    	WebResource currentLockedResource = resources.getResource(currentLock.getWebPath());
                        lockManager.removeResourceLock(currentLockedResource);
                        continue;
                    }
                    if ( (currentLock.getWebPath().startsWith(lock.getWebPath())) &&
                         ((currentLock.isExclusive()) ||
                          (lock.isExclusive())) ) {
                        // A child collection of this collection is locked
                        lockPaths.add(currentLock.getWebPath());
                    }
                }
                locksList = lockManager.getResourceLocks().iterator();
                while (locksList.hasNext()) {
                    LockInfo currentLock = locksList.next();
                    if (currentLock.hasExpired()) {
                    	WebResource currentLockedResource = resources.getResource(currentLock.getWebPath());
                        lockManager.removeResourceLock(currentLockedResource);
                        continue;
                    }
                    if ( (currentLock.getWebPath().startsWith(lock.getWebPath())) &&
                         ((currentLock.isExclusive()) ||
                          (lock.isExclusive())) ) {
                        // A child resource of this collection is locked
                        lockPaths.add(currentLock.getWebPath());
                    }
                }

                if (!lockPaths.isEmpty()) {

                    // One of the child paths was locked
                    // We generate a multistatus error report

                    Iterator<String> lockPathsList = lockPaths.iterator();

                    resp.setStatus(WebdavStatus.SC_CONFLICT);

                    XMLWriter generatedXML = new XMLWriter();
                    generatedXML.writeXMLHeader();
                    generatedXML.writeElement("D", DEFAULT_NAMESPACE, "multistatus", XMLWriter.OPENING);

                    while (lockPathsList.hasNext()) {
                        generatedXML.writeElement("D", "response", XMLWriter.OPENING);
                        generatedXML.writeElement("D", "href", XMLWriter.OPENING);
                        generatedXML.writeText(lockPathsList.next());
                        generatedXML.writeElement("D", "href", XMLWriter.CLOSING);
                        generatedXML.writeElement("D", "status", XMLWriter.OPENING);
                        generatedXML.writeText("HTTP/1.1 " + WebdavStatus.SC_LOCKED + " " + WebdavStatus.getStatusText(WebdavStatus.SC_LOCKED));
                        generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
                        generatedXML.writeElement("D", "response", XMLWriter.CLOSING);
                    }

                    generatedXML.writeElement("D", "multistatus", XMLWriter.CLOSING);

                    Writer writer = resp.getWriter();
                    writer.write(generatedXML.toString());
                    writer.close();
                    return;
                }

                boolean addLock = true;

                // Checking if there is already a shared lock on this path
                locksList = lockManager.getCollectionLocks();
                while (locksList.hasNext()) {

                    LockInfo currentLock = locksList.next();
                    if (currentLock.getWebPath().equals(lock.getWebPath())) {

                        if (currentLock.isExclusive()) {
                            resp.setStatus(WebdavStatus.SC_LOCKED);
                            return;
                        } else {
                            if (lock.isExclusive()) {
                                resp.setStatus(WebdavStatus.SC_LOCKED);
                                return;
                            }
                        }

                        currentLock.addToken(lockToken);
                        lock = currentLock;
                        addLock = false;

                    }

                }

                if (addLock) {
                    lock.addToken(lockToken);
                    lockManager.addCollectionLock(lock);
                }

            } else {
                // Locking a single resource
                // Retrieving an already existing lock on that resource
                lock.addToken(lockToken);
                // Checking if a resource exists at this path
                if (!resource.exists()) {
                    // "Creating" a lock-null resource
                    int slash = lock.getWebPath().lastIndexOf('/');
                    String parentPath = lock.getWebPath().substring(0, slash);
                    WebResource parentResource = resources.getResource(parentPath);
                    List<String> lockNulls = lockManager.getLockNullResource(parentResource);
                    if (lockNulls == null) {
                        lockNulls = new Vector<>();
                        lockManager.putLockNullResource(parentPath, lockNulls);
                    }

                    lockNulls.add(lock.getWebPath());

                }
                // Add the Lock-Token header as by RFC 2518 8.10.1
                // - only do this for newly created locks
                resp.addHeader("Lock-Token", "<opaquelocktoken:" + lockToken + ">");
            }
        }

        if (lockRequestType == LOCK_REFRESH) {

            String ifHeader = req.getHeader("If");
            if (ifHeader == null)
                ifHeader = "";

            // Checking resource locks

            LockInfo toRenew = lockManager.getLock(resource);
            if (toRenew != null) {
                // At least one of the tokens of the locks must have been given
            	Iterator<String> tokenList = toRenew.tokens();
                while (tokenList.hasNext()) {
                    String token = tokenList.next();
                    if (ifHeader.indexOf(token) != -1) {
                        toRenew.setExpiresAt(lock.getExpiresAt());
                        toRenew.setWebDAVLock(true);
                        lock = toRenew;
                    }
                }
            }

            // Checking inheritable collection locks

            Iterator<LockInfo> collectionLocksList = lockManager.getCollectionLocks();
            while (collectionLocksList.hasNext()) {
                toRenew = collectionLocksList.next();
                if (path.equals(toRenew.getWebPath())) {

                	Iterator<String> tokenList = toRenew.tokens();
                    while (tokenList.hasNext()) {
                        String token = tokenList.next();
                        if (ifHeader.indexOf(token) != -1) {
                            toRenew.setExpiresAt(lock.getExpiresAt());
                            lock = toRenew;
                        }
                    }

                }
            }

        }

        // Set the status, then generate the XML response containing
        // the lock information
        XMLWriter generatedXML = new XMLWriter();
        generatedXML.writeXMLHeader();
        generatedXML.writeElement("D", DEFAULT_NAMESPACE, "prop", XMLWriter.OPENING);
        generatedXML.writeElement("D", "lockdiscovery", XMLWriter.OPENING);

        lock.toXML(generatedXML);

        generatedXML.writeElement("D", "lockdiscovery", XMLWriter.CLOSING);
        generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);

        resp.setStatus(WebdavStatus.SC_OK);
        resp.setContentType("text/xml; charset=UTF-8");
        Writer writer = resp.getWriter();
        writer.write(generatedXML.toString());
        writer.close();
    }

    /**
     * UNLOCK Method.
     */
    protected void doUnlock(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
    	
        if (isLocked(req)) {
            resp.setStatus(WebdavStatus.SC_LOCKED);
            return;
        }

        final String path = getRelativePath(req);
        final WebResourceRoot resources = getResources(req);
        final WebResource resource = resources.getResource(path);

        String lockTokenHeader = req.getHeader("Lock-Token");
        if (lockTokenHeader == null) {
        	lockTokenHeader = "";
        } else if(lockTokenHeader != null && lockTokenHeader.startsWith("<opaquelocktoken") && !lockTokenHeader.endsWith(">")) {
        	lockTokenHeader += ">";
        }

        // Checking resource locks
        if(log.isDebugEnabled()) {
        	log.debug("Unlock the ressource: " + path);
        }

        LockInfo lock = lockManager.getLock(resource);
        if (lock != null) {

            // At least one of the tokens of the locks must have been given

        	Iterator<String> tokenList = lock.tokens();
            while (tokenList.hasNext()) {
                String token = tokenList.next();
                if (lockTokenHeader.indexOf(token) != -1) {
                    lock.removeToken(token);
                }
            }

            if (lock.getTokensSize() == 0) {
                lockManager.removeResourceLock(resource);
                // Removing any lock-null resource which would be present
                lockManager.removeLockNullResource(resource);
            }

        }

        // Checking inheritable collection locks

        Iterator<LockInfo> collectionLocksList = lockManager.getCollectionLocks();
        while (collectionLocksList.hasNext()) {
            lock = collectionLocksList.next();
            if (path.equals(lock.getWebPath())) {

            	Iterator<String> tokenList = lock.tokens();
                while (tokenList.hasNext()) {
                    String token = tokenList.next();
                    if (lockTokenHeader.indexOf(token) != -1) {
                        lock.removeToken(token);
                        break;
                    }
                }

                if (lock.getTokensSize() == 0) {
                    lockManager.removeCollectionLock(lock);
                    // Removing any lock-null resource which would be present
                    lockManager.removeLockNullResource(resource);
                }

            }
        }

        resp.setStatus(WebdavStatus.SC_NO_CONTENT);

    }

    // -------------------------------------------------------- Private Methods

    /**
     * Check to see if a resource is currently write locked. The method
     * will look at the "If" header to make sure the client
     * has give the appropriate lock tokens.
     *
     * @param req Servlet request
     * @return boolean true if the resource is locked (and no appropriate
     * lock token has been found for at least one of the non-shared locks which
     * are present on the resource).
     */
    private boolean isLocked(HttpServletRequest req) {

        final String path = getRelativePath(req);
        final WebResourceRoot resources = getResources(req);
        final WebResource resource = resources.getResource(path);

        String ifHeader = req.getHeader("If");
        if (ifHeader == null)
            ifHeader = "";

        String lockTokenHeader = req.getHeader("Lock-Token");
        if (lockTokenHeader == null) {
        	lockTokenHeader = "";
        } else if(lockTokenHeader != null && lockTokenHeader.startsWith("<opaquelocktoken") && !lockTokenHeader.endsWith(">")) {
        	lockTokenHeader += ">";
        }
        
        UserSession usess = webDAVManager.getUserSession(req);
        boolean locked = lockManager.isLocked(resource, ifHeader + lockTokenHeader, usess.getIdentity());
        if(locked && log.isDebugEnabled()) {
        	log.debug("Ressource is locked: " + req.getPathInfo());
        }
        return locked;
    }

    /**
     * Copy a resource.
     *
     * @param req Servlet request
     * @param resp Servlet response
     * @return boolean true if the copy is successful
     */
    private boolean copyResource(HttpServletRequest req, HttpServletResponse resp, boolean moved)
    throws IOException {

        // Parsing destination header
    	String destinationPath = getDestinationPath(req);
    	if (destinationPath == null) {
            resp.setStatus(WebdavStatus.SC_BAD_REQUEST);
            return false;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo != null) {
            String servletPath = req.getServletPath();
            if ((servletPath != null) &&
                (destinationPath.startsWith(servletPath))) {
                destinationPath = destinationPath
                    .substring(servletPath.length());
            }
        }

        if (log.isDebugEnabled()) log.debug("Dest path :" + destinationPath);

        // Check destination path to protect special subdirectories
        if (isSpecialPath(destinationPath)) {
            resp.setStatus(WebdavStatus.SC_FORBIDDEN);
            return false;
        }

        String path = getRelativePath(req);

        if (destinationPath.equals(path)) {
            resp.setStatus(WebdavStatus.SC_FORBIDDEN);
            return false;
        }

        // Parsing overwrite header

        boolean overwrite = true;
        String overwriteHeader = req.getHeader("Overwrite");

        if (overwriteHeader != null) {
            if (overwriteHeader.equalsIgnoreCase("T")) {
                overwrite = true;
            } else {
                overwrite = false;
            }
        }

        // Overwriting the destination
        final WebResourceRoot resources = getResources(req);
        final WebResource destination = resources.getResource(destinationPath);

        if (overwrite) {
            // Delete destination resource, if it exists
            if (destination.exists()) {
                if (!deleteResource(destinationPath, req, resp, true)) {
                    return false;
                }
            } else {
                resp.setStatus(WebdavStatus.SC_CREATED);
            }
        } else {
            // If the destination exists, then it's a conflict
            if (destination.exists()) {
                resp.setStatus(WebdavStatus.SC_PRECONDITION_FAILED);
                return false;
            }
        }

        // Copying source to destination

        Hashtable<String,Integer> errorList = new Hashtable<>();

        boolean result = copyResource(req, errorList, path, destinationPath, moved);

        if ((!result) || (!errorList.isEmpty())) {
            if (errorList.size() == 1) {
                resp.setStatus(errorList.elements().nextElement().intValue());
            } else {
                sendReport(req, resp, errorList);
            }
            return false;
        }

        // Copy was successful
        if (destination.exists()) {
            resp.setStatus(WebdavStatus.SC_NO_CONTENT);
        } else {
            resp.setStatus(WebdavStatus.SC_CREATED);
        }

        // Removing any lock-null resource which would be present at
        // the destination path
        lockManager.removeLockNullResource(destination);

        return true;
    }
    
    private String getDestinationPath(HttpServletRequest req) {
    	String destinationPath = req.getHeader("Destination");
    	if (destinationPath == null) {
            return null;
        }

        // Remove url encoding from destination
        destinationPath = RequestUtil.URLDecode(destinationPath, "UTF8");

        int protocolIndex = destinationPath.indexOf("://");
        if (protocolIndex >= 0) {
            // if the Destination URL contains the protocol, we can safely
            // trim everything upto the first "/" character after "://"
            int firstSeparator =
                destinationPath.indexOf("/", protocolIndex + 4);
            if (firstSeparator < 0) {
                destinationPath = "/";
            } else {
                destinationPath = destinationPath.substring(firstSeparator);
            }
        } else {
            String hostName = req.getServerName();
            if ((hostName != null) && (destinationPath.startsWith(hostName))) {
                destinationPath = destinationPath.substring(hostName.length());
            }

            int portIndex = destinationPath.indexOf(":");
            if (portIndex >= 0) {
                destinationPath = destinationPath.substring(portIndex);
            }

            if (destinationPath.startsWith(":")) {
                int firstSeparator = destinationPath.indexOf("/");
                if (firstSeparator < 0) {
                    destinationPath = "/";
                } else {
                    destinationPath =
                        destinationPath.substring(firstSeparator);
                }
            }
        }

        // Normalise destination path (remove '.' and '..')
        destinationPath = RequestUtil.normalize(destinationPath);

        String contextPath = req.getContextPath();
        if ((contextPath != null) &&
            (destinationPath.startsWith(contextPath))) {
            destinationPath = destinationPath.substring(contextPath.length());
        }
        
        return destinationPath;
    }


    /**
     * Copy a collection.
     *
     * @param errorList Hashtable containing the list of errors which occurred
     * during the copy operation
     * @param source Path of the resource to be copied
     * @param dest Destination path
     */
    private boolean copyResource(HttpServletRequest req, Hashtable<String,Integer> errorList,
            String source, String dest, boolean moved) {

        if (log.isDebugEnabled()) log.debug("Copy: " + source + " To: " + dest);
        
        WebResourceRoot resources = getResources(req);
        WebResource sourceResource = resources.getResource(source);

        if (sourceResource.isDirectory()) {
            if (!resources.mkdir(dest)) {
                WebResource destResource = resources.getResource(dest);
                if (!destResource.isDirectory()) {
                    errorList.put(dest, new Integer(WebdavStatus.SC_CONFLICT));
                    return false;
                }
            }

            Collection<VFSItem> entries = resources.list(source);
            for (VFSItem entry : entries) {
                String childDest = dest;
                if (!childDest.equals("/")) {
                    childDest += "/";
                }
                childDest += entry.getName();
                String childSrc = source;
                if (!childSrc.equals("/")) {
                    childSrc += "/";
                }
                childSrc += entry.getName();
                copyResource(req, errorList, childSrc, childDest, moved);
            }
        } else if (sourceResource.isFile()) {
            	WebResource destResource = resources.getResource(dest);
            if (!destResource.exists() && !destResource.getPath().endsWith("/")) {
                int lastSlash = destResource.getPath().lastIndexOf('/');
                if (lastSlash > 0) {
                    String parent = destResource.getPath().substring(0, lastSlash);
                    WebResource parentResource = resources.getResource(parent);
                    if (!parentResource.isDirectory()) {
                        errorList.put(source, new Integer(WebdavStatus.SC_CONFLICT));
                        return false;
                    }
                }
            }

            WebResource movedFrom = moved ? sourceResource : null; 
            try(InputStream in = sourceResource.getInputStream()) {
				if (!resources.write(dest, in, false, movedFrom)) {
				    errorList.put(source, Integer.valueOf(WebdavStatus.SC_INTERNAL_SERVER_ERROR));
				    return false;
				}
			} catch (QuotaExceededException e) {
				errorList.put(source, Integer.valueOf(WebdavStatus.SC_INSUFFICIENT_STORAGE));
			    return false;
			} catch (IOException e) {
				errorList.put(source, Integer.valueOf(WebdavStatus.SC_INTERNAL_SERVER_ERROR));
			    return false;
			}
        } else {
            errorList.put(source, Integer.valueOf(WebdavStatus.SC_INTERNAL_SERVER_ERROR));
            return false;
        }
        return true;
    }


    /**
     * Delete a resource.
     *
     * @param req Servlet request
     * @param resp Servlet response
     * @return boolean true if the copy is successful
     */
    private boolean deleteResource(HttpServletRequest req,
                                   HttpServletResponse resp)
            throws IOException {

        String path = getRelativePath(req);

        return deleteResource(path, req, resp, true);

    }


    /**
     * Delete a resource.
     *
     * @param path Path of the resource which is to be deleted
     * @param req Servlet request
     * @param resp Servlet response
     * @param setStatus Should the response status be set on successful
     *                  completion
     */
    private boolean deleteResource(final String path, HttpServletRequest req,
                                   HttpServletResponse resp, boolean setStatus)
            throws IOException {

        String ifHeader = req.getHeader("If");
        if (ifHeader == null)
            ifHeader = "";

        String lockTokenHeader = req.getHeader("Lock-Token");
        if (lockTokenHeader == null)
            lockTokenHeader = "";

        final WebResourceRoot resources = getResources(req);
        final WebResource resource = resources.getResource(path);
        UserSession usess = webDAVManager.getUserSession(req);
        if (lockManager.isLocked(resource, ifHeader + lockTokenHeader, usess.getIdentity())) {
            resp.setStatus(WebdavStatus.SC_LOCKED);
            return false;
        }

        if (!resource.exists()) {
            resp.setStatus(WebdavStatus.SC_NOT_FOUND);
            return false;
        }

        if (!resource.isDirectory()) {
            if (!resources.delete(resource)) {
                resp.setStatus(WebdavStatus.SC_INTERNAL_SERVER_ERROR);
                return false;
            }
        } else {

            Hashtable<String,Integer> errorList = new Hashtable<>();

            deleteCollection(req, path, errorList);
            if (!resources.delete(resource)) {
                errorList.put(path, new Integer
                    (WebdavStatus.SC_INTERNAL_SERVER_ERROR));
            }

            if (!errorList.isEmpty()) {
                sendReport(req, resp, errorList);
                return false;
            }
        }
        if (setStatus) {
            resp.setStatus(WebdavStatus.SC_NO_CONTENT);
        }
        return true;
    }


    /**
     * Deletes a collection.
     *
     * @param path Path to the collection to be deleted
     * @param errorList Contains the list of the errors which occurred
     */
    private void deleteCollection(HttpServletRequest req,
                                  String path,
                                  Map<String,Integer> errorList) {

        if (log.isDebugEnabled()) log.debug("Delete:" + path);

        // Prevent deletion of special subdirectories
        if (isSpecialPath(path)) {
            errorList.put(path, new Integer(WebdavStatus.SC_FORBIDDEN));
            return;
        }

        String ifHeader = req.getHeader("If");
        if (ifHeader == null)
            ifHeader = "";

        String lockTokenHeader = req.getHeader("Lock-Token");
        if (lockTokenHeader == null)
            lockTokenHeader = "";

        final WebResourceRoot resources = getResources(req);
        Collection<VFSItem> entries = resources.list(path);
        UserSession usess = webDAVManager.getUserSession(req);

        for (VFSItem entry : entries) {
            String childName = path;
            if (!childName.equals("/")) {
                childName += "/";
            }
            childName += entry.getName();
            WebResource childResource = resources.getResource(childName);

            if (lockManager.isLocked(childResource, ifHeader + lockTokenHeader, usess.getIdentity())) {

                errorList.put(childName, new Integer(WebdavStatus.SC_LOCKED));

            } else {
                if (childResource.isDirectory()) {
                    deleteCollection(req, childName, errorList);
                }

                if (!resources.delete(childResource)) {
                    if (!childResource.isDirectory()) {
                        // If it's not a collection, then it's an unknown error
                        errorList.put(childName, new Integer(WebdavStatus.SC_INTERNAL_SERVER_ERROR));
                    }
                }
            }
        }
    }


    /**
     * Send a multistatus element containing a complete error report to the
     * client.
     *
     * @param req Servlet request
     * @param resp Servlet response
     * @param errorList List of error to be displayed
     */
    private void sendReport(HttpServletRequest req, HttpServletResponse resp,
                            Hashtable<String,Integer> errorList)
            throws IOException {

        resp.setStatus(WebdavStatus.SC_MULTI_STATUS);

        String absoluteUri = req.getRequestURI();
        String relativePath = getRelativePath(req);

        XMLWriter generatedXML = new XMLWriter();
        generatedXML.writeXMLHeader();

        generatedXML.writeElement("D", DEFAULT_NAMESPACE, "multistatus",
                XMLWriter.OPENING);

        Enumeration<String> pathList = errorList.keys();
        while (pathList.hasMoreElements()) {

            String errorPath = pathList.nextElement();
            int errorCode = errorList.get(errorPath).intValue();

            generatedXML.writeElement("D", "response", XMLWriter.OPENING);

            generatedXML.writeElement("D", "href", XMLWriter.OPENING);
            String toAppend = errorPath.substring(relativePath.length());
            if (!toAppend.startsWith("/"))
                toAppend = "/" + toAppend;
            generatedXML.writeText(absoluteUri + toAppend);
            generatedXML.writeElement("D", "href", XMLWriter.CLOSING);
            generatedXML.writeElement("D", "status", XMLWriter.OPENING);
            generatedXML.writeText("HTTP/1.1 " + errorCode + " "
                    + WebdavStatus.getStatusText(errorCode));
            generatedXML.writeElement("D", "status", XMLWriter.CLOSING);

            generatedXML.writeElement("D", "response", XMLWriter.CLOSING);

        }

        generatedXML.writeElement("D", "multistatus", XMLWriter.CLOSING);

        Writer writer = resp.getWriter();
        writer.write(generatedXML.toString());
        writer.close();

    }


    /**
     * Propfind helper method.
     *
     * @param req The servlet request
     * @param resources Resources object associated with this context
     * @param generatedXML XML response to the Propfind request
     * @param path Path of the current resource
     * @param type Propfind type
     * @param propertiesVector If the propfind type is find properties by
     * name, then this Vector contains those properties
     */
    private void parseProperties(HttpServletRequest req,
                                 XMLWriter generatedXML,
                                 final String path, int type,
                                 Vector<String> propertiesVector) {

        // Exclude any resource in the /WEB-INF and /META-INF subdirectories
        if (isSpecialPath(path))
            return;

        final WebResourceRoot resources = getResources(req);
        final WebResource resource = resources.getResource(path);
        if (!resource.exists()) {
            // File is in directory listing but doesn't appear to exist
            // Broken symlink or odd permission settings?
            return;
        }

        generatedXML.writeElement("D", "response", XMLWriter.OPENING);
        String status = "HTTP/1.1 " + WebdavStatus.SC_OK + " " + WebdavStatus.getStatusText(WebdavStatus.SC_OK);

        // Generating href element
        generatedXML.writeElement("D", "href", XMLWriter.OPENING);

        String href = req.getContextPath() + req.getServletPath();
        if ((href.endsWith("/")) && (path.startsWith("/")))
            href += path.substring(1);
        else
            href += path;
        if (resource.isDirectory() && (!href.endsWith("/")))
            href += "/";

        String nfcNormalizedHref = Normalizer.normalize(href, Normalizer.Form.NFC);
        generatedXML.writeText(rewriteUrl(nfcNormalizedHref));

        generatedXML.writeElement("D", "href", XMLWriter.CLOSING);

        String resourceName = Normalizer.normalize(path, Normalizer.Form.NFC);
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash != -1)
            resourceName = resourceName.substring(lastSlash + 1);

        switch (type) {

        case FIND_ALL_PROP :

            generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
            generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

            generatedXML.writeProperty("D", "creationdate",
                    getISOCreationDate(resource.getCreation()));
            generatedXML.writeElement("D", "displayname", XMLWriter.OPENING);
            generatedXML.writeData(resourceName);
            generatedXML.writeElement("D", "displayname", XMLWriter.CLOSING);
            if (resource.isFile()) {
                generatedXML.writeProperty("D", "getlastmodified", FastHttpDateFormat.formatDate(resource.getLastModified(), null));
                generatedXML.writeProperty("D", "getcontentlength",String.valueOf(resource.getContentLength()));
                String contentType = req.getServletContext().getMimeType(resource.getName());
                if (contentType != null) {
                    generatedXML.writeProperty("D", "getcontenttype",contentType);
                }
                generatedXML.writeProperty("D", "getetag",resource.getETag());
                generatedXML.writeElement("D", "resourcetype", XMLWriter.NO_CONTENT);
            } else {
                generatedXML.writeElement("D", "resourcetype", XMLWriter.OPENING);
                generatedXML.writeElement("D", "collection", XMLWriter.NO_CONTENT);
                generatedXML.writeElement("D", "resourcetype", XMLWriter.CLOSING);
            }

            generatedXML.writeProperty("D", "source", "");

            String supportedLocks = "<D:lockentry>"
                + "<D:lockscope><D:exclusive/></D:lockscope>"
                + "<D:locktype><D:write/></D:locktype>"
                + "</D:lockentry>" + "<D:lockentry>"
                + "<D:lockscope><D:shared/></D:lockscope>"
                + "<D:locktype><D:write/></D:locktype>"
                + "</D:lockentry>";
            generatedXML.writeElement("D", "supportedlock", XMLWriter.OPENING);
            generatedXML.writeText(supportedLocks);
            generatedXML.writeElement("D", "supportedlock", XMLWriter.CLOSING);

            generateLockDiscovery(resource, path, generatedXML);

            generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
            generatedXML.writeElement("D", "status", XMLWriter.OPENING);
            generatedXML.writeText(status);
            generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
            generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

            break;

        case FIND_PROPERTY_NAMES :

            generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
            generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

            generatedXML.writeElement("D", "creationdate",
                                      XMLWriter.NO_CONTENT);
            generatedXML.writeElement("D", "displayname", XMLWriter.NO_CONTENT);
            if (resource.isFile()) {
                generatedXML.writeElement("D", "getcontentlanguage",
                        XMLWriter.NO_CONTENT);
                generatedXML.writeElement("D", "getcontentlength",
                        XMLWriter.NO_CONTENT);
                generatedXML.writeElement("D", "getcontenttype",
                        XMLWriter.NO_CONTENT);
                generatedXML.writeElement("D", "getetag", XMLWriter.NO_CONTENT);
                generatedXML.writeElement("D", "getlastmodified",
                        XMLWriter.NO_CONTENT);
            }
            generatedXML.writeElement("D", "resourcetype",
                                      XMLWriter.NO_CONTENT);
            generatedXML.writeElement("D", "source", XMLWriter.NO_CONTENT);
            generatedXML.writeElement("D", "lockdiscovery",
                                      XMLWriter.NO_CONTENT);

            generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
            generatedXML.writeElement("D", "status", XMLWriter.OPENING);
            generatedXML.writeText(status);
            generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
            generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

            break;

        case FIND_BY_PROPERTY :

            Vector<String> propertiesNotFound = new Vector<>();

            // Parse the list of properties

            generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
            generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

            Enumeration<String> properties = propertiesVector.elements();

            while (properties.hasMoreElements()) {

                String property = properties.nextElement();

                if (property.equals("creationdate")) {
                    generatedXML.writeProperty
                        ("D", "creationdate",
                         getISOCreationDate(resource.getCreation()));
                } else if (property.equals("displayname")) {
                    generatedXML.writeElement
                        ("D", "displayname", XMLWriter.OPENING);
                    generatedXML.writeData(resourceName);
                    generatedXML.writeElement
                        ("D", "displayname", XMLWriter.CLOSING);
                } else if (property.equals("getcontentlanguage")) {
                    if (resource.isDirectory()) {
                        propertiesNotFound.addElement(property);
                    } else {
                        generatedXML.writeElement("D", "getcontentlanguage",
                                                  XMLWriter.NO_CONTENT);
                    }
                } else if (property.equals("getcontentlength")) {
                    if (resource.isDirectory()) {
                        propertiesNotFound.addElement(property);
                    } else {
                        generatedXML.writeProperty
                            ("D", "getcontentlength",
                             (String.valueOf(resource.getContentLength())));
                    }
                } else if (property.equals("getcontenttype")) {
                    if (resource.isDirectory()) {
                        propertiesNotFound.addElement(property);
                    } else {
                        generatedXML.writeProperty
                            ("D", "getcontenttype",
                             req.getServletContext().getMimeType
                             (resource.getName()));
                    }
                } else if (property.equals("getetag")) {
                    if (resource.isDirectory()) {
                        propertiesNotFound.addElement(property);
                    } else {
                        generatedXML.writeProperty
                            ("D", "getetag", resource.getETag());
                    }
                } else if (property.equals("getlastmodified")) {
                    if (resource.isDirectory()) {
                        propertiesNotFound.addElement(property);
                    } else {
                        generatedXML.writeProperty
                            ("D", "getlastmodified", FastHttpDateFormat.formatDate
                                    (resource.getLastModified(), null));
                    }
                } else if (property.equals("resourcetype")) {
                    if (resource.isDirectory()) {
                        generatedXML.writeElement("D", "resourcetype",
                                XMLWriter.OPENING);
                        generatedXML.writeElement("D", "collection",
                                XMLWriter.NO_CONTENT);
                        generatedXML.writeElement("D", "resourcetype",
                                XMLWriter.CLOSING);
                    } else {
                        generatedXML.writeElement("D", "resourcetype",
                                XMLWriter.NO_CONTENT);
                    }
                } else if (property.equals("source")) {
                    generatedXML.writeProperty("D", "source", "");
                } else if (property.equals("supportedlock")) {
                    supportedLocks = "<D:lockentry>"
                        + "<D:lockscope><D:exclusive/></D:lockscope>"
                        + "<D:locktype><D:write/></D:locktype>"
                        + "</D:lockentry>" + "<D:lockentry>"
                        + "<D:lockscope><D:shared/></D:lockscope>"
                        + "<D:locktype><D:write/></D:locktype>"
                        + "</D:lockentry>";
                    generatedXML.writeElement("D", "supportedlock",
                            XMLWriter.OPENING);
                    generatedXML.writeText(supportedLocks);
                    generatedXML.writeElement("D", "supportedlock",
                            XMLWriter.CLOSING);
                } else if (property.equals("lockdiscovery")) {
                    if (!generateLockDiscovery(resource, path, generatedXML)) {
                        propertiesNotFound.addElement(property);
                    }
                } else {
                    propertiesNotFound.addElement(property);
                }

            }

            generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
            generatedXML.writeElement("D", "status", XMLWriter.OPENING);
            generatedXML.writeText(status);
            generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
            generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

            Enumeration<String> propertiesNotFoundList =
                propertiesNotFound.elements();

            if (propertiesNotFoundList.hasMoreElements()) {

                status = "HTTP/1.1 " + WebdavStatus.SC_NOT_FOUND + " " +
                        WebdavStatus.getStatusText(WebdavStatus.SC_NOT_FOUND);

                generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
                generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

                while (propertiesNotFoundList.hasMoreElements()) {
                    generatedXML.writeElement
                        ("D", propertiesNotFoundList.nextElement(),
                         XMLWriter.NO_CONTENT);
                }

                generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
                generatedXML.writeElement("D", "status", XMLWriter.OPENING);
                generatedXML.writeText(status);
                generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
                generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

            }

            break;

        }

        generatedXML.writeElement("D", "response", XMLWriter.CLOSING);

    }


    /**
     * Propfind helper method. Displays the properties of a lock-null resource.
     *
     * @param resources Resources object associated with this context
     * @param generatedXML XML response to the Propfind request
     * @param path Path of the current resource
     * @param type Propfind type
     * @param propertiesVector If the propfind type is find properties by
     * name, then this Vector contains those properties
     */
    private void parseLockNullProperties(HttpServletRequest req,
                                         XMLWriter generatedXML,
                                         final String path, int type,
                                         Vector<String> propertiesVector) {

        // Exclude any resource in the /WEB-INF and /META-INF subdirectories
        if (isSpecialPath(path))
            return;

        final WebResourceRoot resources = getResources(req);
        final WebResource resource = resources.getResource(path);
        
        // Retrieving the lock associated with the lock-null resource
        LockInfo lock = lockManager.getLock(resource);

        if (lock == null)
            return;

        generatedXML.writeElement("D", "response", XMLWriter.OPENING);
        String status = "HTTP/1.1 " + WebdavStatus.SC_OK + " " +
                WebdavStatus.getStatusText(WebdavStatus.SC_OK);

        // Generating href element
        generatedXML.writeElement("D", "href", XMLWriter.OPENING);

        String absoluteUri = req.getRequestURI();
        String relativePath = getRelativePath(req);
        String toAppend = path.substring(relativePath.length());
        if (!toAppend.startsWith("/"))
            toAppend = "/" + toAppend;

        String normalizedUrl = RequestUtil.normalize(absoluteUri + toAppend);
        String nfcNormalizedUrl = Normalizer.normalize(normalizedUrl, Normalizer.Form.NFC);
        generatedXML.writeText(rewriteUrl(nfcNormalizedUrl));

        generatedXML.writeElement("D", "href", XMLWriter.CLOSING);

        String resourceName = Normalizer.normalize(path, Normalizer.Form.NFC);
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash != -1) {
            resourceName = resourceName.substring(lastSlash + 1);
        }

        switch (type) {

        case FIND_ALL_PROP :

            generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
            generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

            generatedXML.writeProperty("D", "creationdate", getISOCreationDate(lock.getCreationDate().getTime()));
            generatedXML.writeElement("D", "displayname", XMLWriter.OPENING);
            generatedXML.writeData(resourceName);
            generatedXML.writeElement("D", "displayname", XMLWriter.CLOSING);
            generatedXML.writeProperty("D", "getlastmodified", FastHttpDateFormat.formatDate(lock.getCreationDate().getTime(), null));
            generatedXML.writeProperty("D", "getcontentlength", String.valueOf(0));
            generatedXML.writeProperty("D", "getcontenttype", "");
            generatedXML.writeProperty("D", "getetag", "");
            generatedXML.writeElement("D", "resourcetype", XMLWriter.OPENING);
            generatedXML.writeElement("D", "lock-null", XMLWriter.NO_CONTENT);
            generatedXML.writeElement("D", "resourcetype", XMLWriter.CLOSING);

            generatedXML.writeProperty("D", "source", "");

            String supportedLocks = "<D:lockentry>"
                + "<D:lockscope><D:exclusive/></D:lockscope>"
                + "<D:locktype><D:write/></D:locktype>"
                + "</D:lockentry>" + "<D:lockentry>"
                + "<D:lockscope><D:shared/></D:lockscope>"
                + "<D:locktype><D:write/></D:locktype>"
                + "</D:lockentry>";
            generatedXML.writeElement("D", "supportedlock", XMLWriter.OPENING);
            generatedXML.writeText(supportedLocks);
            generatedXML.writeElement("D", "supportedlock", XMLWriter.CLOSING);

            generateLockDiscovery(resource, path, generatedXML);

            generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
            generatedXML.writeElement("D", "status", XMLWriter.OPENING);
            generatedXML.writeText(status);
            generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
            generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

            break;

        case FIND_PROPERTY_NAMES :

            generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
            generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

            generatedXML.writeElement("D", "creationdate",
                                      XMLWriter.NO_CONTENT);
            generatedXML.writeElement("D", "displayname", XMLWriter.NO_CONTENT);
            generatedXML.writeElement("D", "getcontentlanguage",
                                      XMLWriter.NO_CONTENT);
            generatedXML.writeElement("D", "getcontentlength",
                                      XMLWriter.NO_CONTENT);
            generatedXML.writeElement("D", "getcontenttype",
                                      XMLWriter.NO_CONTENT);
            generatedXML.writeElement("D", "getetag", XMLWriter.NO_CONTENT);
            generatedXML.writeElement("D", "getlastmodified",
                                      XMLWriter.NO_CONTENT);
            generatedXML.writeElement("D", "resourcetype",
                                      XMLWriter.NO_CONTENT);
            generatedXML.writeElement("D", "source", XMLWriter.NO_CONTENT);
            generatedXML.writeElement("D", "lockdiscovery",
                                      XMLWriter.NO_CONTENT);

            generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
            generatedXML.writeElement("D", "status", XMLWriter.OPENING);
            generatedXML.writeText(status);
            generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
            generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

            break;

        case FIND_BY_PROPERTY :

            Vector<String> propertiesNotFound = new Vector<>();

            // Parse the list of properties

            generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
            generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

            Enumeration<String> properties = propertiesVector.elements();

            while (properties.hasMoreElements()) {

                String property = properties.nextElement();

                if (property.equals("creationdate")) {
                    generatedXML.writeProperty("D", "creationdate", getISOCreationDate(lock.getCreationDate().getTime()));
                } else if (property.equals("displayname")) {
                    generatedXML.writeElement("D", "displayname",
                            XMLWriter.OPENING);
                    generatedXML.writeData(resourceName);
                    generatedXML.writeElement("D", "displayname",
                            XMLWriter.CLOSING);
                } else if (property.equals("getcontentlanguage")) {
                    generatedXML.writeElement("D", "getcontentlanguage",
                            XMLWriter.NO_CONTENT);
                } else if (property.equals("getcontentlength")) {
                    generatedXML.writeProperty("D", "getcontentlength",
                            (String.valueOf(0)));
                } else if (property.equals("getcontenttype")) {
                    generatedXML.writeProperty("D", "getcontenttype", "");
                } else if (property.equals("getetag")) {
                    generatedXML.writeProperty("D", "getetag", "");
                } else if (property.equals("getlastmodified")) {
                    generatedXML.writeProperty("D", "getlastmodified", FastHttpDateFormat.formatDate(lock.getCreationDate().getTime(), null));
                } else if (property.equals("resourcetype")) {
                    generatedXML.writeElement("D", "resourcetype",
                            XMLWriter.OPENING);
                    generatedXML.writeElement("D", "lock-null",
                            XMLWriter.NO_CONTENT);
                    generatedXML.writeElement("D", "resourcetype",
                            XMLWriter.CLOSING);
                } else if (property.equals("source")) {
                    generatedXML.writeProperty("D", "source", "");
                } else if (property.equals("supportedlock")) {
                    supportedLocks = "<D:lockentry>"
                        + "<D:lockscope><D:exclusive/></D:lockscope>"
                        + "<D:locktype><D:write/></D:locktype>"
                        + "</D:lockentry>" + "<D:lockentry>"
                        + "<D:lockscope><D:shared/></D:lockscope>"
                        + "<D:locktype><D:write/></D:locktype>"
                        + "</D:lockentry>";
                    generatedXML.writeElement("D", "supportedlock",
                            XMLWriter.OPENING);
                    generatedXML.writeText(supportedLocks);
                    generatedXML.writeElement("D", "supportedlock",
                            XMLWriter.CLOSING);
                } else if (property.equals("lockdiscovery")) {
                    if (!generateLockDiscovery(resource, path, generatedXML))
                        propertiesNotFound.addElement(property);
                } else {
                    propertiesNotFound.addElement(property);
                }

            }

            generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
            generatedXML.writeElement("D", "status", XMLWriter.OPENING);
            generatedXML.writeText(status);
            generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
            generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

            Enumeration<String> propertiesNotFoundList = propertiesNotFound.elements();

            if (propertiesNotFoundList.hasMoreElements()) {

                status = "HTTP/1.1 " + WebdavStatus.SC_NOT_FOUND + " " +
                        WebdavStatus.getStatusText(WebdavStatus.SC_NOT_FOUND);

                generatedXML.writeElement("D", "propstat", XMLWriter.OPENING);
                generatedXML.writeElement("D", "prop", XMLWriter.OPENING);

                while (propertiesNotFoundList.hasMoreElements()) {
                    generatedXML.writeElement
                        ("D", propertiesNotFoundList.nextElement(),
                         XMLWriter.NO_CONTENT);
                }

                generatedXML.writeElement("D", "prop", XMLWriter.CLOSING);
                generatedXML.writeElement("D", "status", XMLWriter.OPENING);
                generatedXML.writeText(status);
                generatedXML.writeElement("D", "status", XMLWriter.CLOSING);
                generatedXML.writeElement("D", "propstat", XMLWriter.CLOSING);

            }

            break;

        }

        generatedXML.writeElement("D", "response", XMLWriter.CLOSING);

    }


    /**
     * Print the lock discovery information associated with a path.
     *
     * @param path Path
     * @param generatedXML XML data to which the locks info will be appended
     * @return true if at least one lock was displayed
     */
    private boolean generateLockDiscovery(final WebResource resource, final String path, XMLWriter generatedXML) {
    	
        LockInfo resourceLock = lockManager.getLock(resource);
        Iterator<LockInfo> collectionLocksList = lockManager.getCollectionLocks();

        boolean wroteStart = false;

        if (resourceLock != null) {
            wroteStart = true;
            generatedXML.writeElement("D", "lockdiscovery", XMLWriter.OPENING);
            resourceLock.toXML(generatedXML);
        } else {
        	LockInfo ooLock = lockManager.getLock(resource);
        	if(ooLock != null) {
        		wroteStart = true;
        		generatedXML.writeElement("D", "lockdiscovery", XMLWriter.OPENING);
        		ooLock.toXML(generatedXML);
        	}
        }

        while (collectionLocksList.hasNext()) {
            LockInfo currentLock = collectionLocksList.next();
            if (path.startsWith(currentLock.getWebPath())) {
                if (!wroteStart) {
                    wroteStart = true;
                    generatedXML.writeElement("D", "lockdiscovery", XMLWriter.OPENING);
                }
                currentLock.toXML(generatedXML);
            }
        }

        if (wroteStart) {
            generatedXML.writeElement("D", "lockdiscovery", XMLWriter.CLOSING);
        } else {
            return false;
        }

        return true;
    }


    /**
     * Get creation date in ISO format.
     */
    private String getISOCreationDate(long creationDate) {
        return creationDateFormat.format(new Date(creationDate));
    }

    /**
     * Determines the methods normally allowed for the resource.
     *
     */
    private StringBuilder determineMethodsAllowed(HttpServletRequest req) {

        StringBuilder methodsAllowed = new StringBuilder();

        WebResourceRoot resources = getResources(req);
        WebResource resource = resources.getResource(getRelativePath(req));

        if (!resource.exists()) {
            methodsAllowed.append("OPTIONS, MKCOL, PUT, LOCK");
            return methodsAllowed;
        }

        methodsAllowed.append("OPTIONS, GET, HEAD, POST, DELETE, TRACE");
        methodsAllowed.append(", PROPPATCH, COPY, MOVE, LOCK, UNLOCK");
        methodsAllowed.append(", PROPFIND");

        if (resource.isFile()) {
            methodsAllowed.append(", PUT");
        }

        return methodsAllowed;
    }

    // --------------------------------------------------  LockInfo Inner Class



    // --------------------------------------------- WebdavResolver Inner Class
    /**
     * Work around for XML parsers that don't fully respect
     * {@link DocumentBuilderFactory#setExpandEntityReferences(boolean)} when
     * called with <code>false</code>. External references are filtered out for
     * security reasons. See CVE-2007-5461.
     */
    private static class WebdavResolver implements EntityResolver {
        private ServletContext context;

        public WebdavResolver(ServletContext theContext) {
            context = theContext;
        }

        @Override
        public InputSource resolveEntity (String publicId, String systemId) {
            context.log("webdavservlet.enternalEntityIgnored" +
                    publicId + " : " + systemId);
            return new InputSource(
                    new StringReader("Ignored external entity"));
        }
    }
}


// --------------------------------------------------------  WebdavStatus Class


/**
 * Wraps the HttpServletResponse class to abstract the
 * specific protocol used.  To support other protocols
 * we would only need to modify this class and the
 * WebDavRetCode classes.
 *
 * @author              Marc Eaddy
 * @version             1.0, 16 Nov 1997
 */
class WebdavStatus {


    // ----------------------------------------------------- Instance Variables


    /**
     * This Hashtable contains the mapping of HTTP and WebDAV
     * status codes to descriptive text.  This is a static
     * variable.
     */
    private static final Hashtable<Integer,String> mapStatusCodes =
            new Hashtable<>();


    // ------------------------------------------------------ HTTP Status Codes


    /**
     * Status code (200) indicating the request succeeded normally.
     */
    public static final int SC_OK = HttpServletResponse.SC_OK;


    /**
     * Status code (201) indicating the request succeeded and created
     * a new resource on the server.
     */
    public static final int SC_CREATED = HttpServletResponse.SC_CREATED;


    /**
     * Status code (202) indicating that a request was accepted for
     * processing, but was not completed.
     */
    public static final int SC_ACCEPTED = HttpServletResponse.SC_ACCEPTED;


    /**
     * Status code (204) indicating that the request succeeded but that
     * there was no new information to return.
     */
    public static final int SC_NO_CONTENT = HttpServletResponse.SC_NO_CONTENT;


    /**
     * Status code (301) indicating that the resource has permanently
     * moved to a new location, and that future references should use a
     * new URI with their requests.
     */
    public static final int SC_MOVED_PERMANENTLY =
        HttpServletResponse.SC_MOVED_PERMANENTLY;


    /**
     * Status code (302) indicating that the resource has temporarily
     * moved to another location, but that future references should
     * still use the original URI to access the resource.
     */
    public static final int SC_MOVED_TEMPORARILY =
        HttpServletResponse.SC_MOVED_TEMPORARILY;


    /**
     * Status code (304) indicating that a conditional GET operation
     * found that the resource was available and not modified.
     */
    public static final int SC_NOT_MODIFIED =
        HttpServletResponse.SC_NOT_MODIFIED;


    /**
     * Status code (400) indicating the request sent by the client was
     * syntactically incorrect.
     */
    public static final int SC_BAD_REQUEST =
        HttpServletResponse.SC_BAD_REQUEST;


    /**
     * Status code (401) indicating that the request requires HTTP
     * authentication.
     */
    public static final int SC_UNAUTHORIZED =
        HttpServletResponse.SC_UNAUTHORIZED;


    /**
     * Status code (403) indicating the server understood the request
     * but refused to fulfill it.
     */
    public static final int SC_FORBIDDEN = HttpServletResponse.SC_FORBIDDEN;


    /**
     * Status code (404) indicating that the requested resource is not
     * available.
     */
    public static final int SC_NOT_FOUND = HttpServletResponse.SC_NOT_FOUND;


    /**
     * Status code (500) indicating an error inside the HTTP service
     * which prevented it from fulfilling the request.
     */
    public static final int SC_INTERNAL_SERVER_ERROR =
        HttpServletResponse.SC_INTERNAL_SERVER_ERROR;


    /**
     * Status code (501) indicating the HTTP service does not support
     * the functionality needed to fulfill the request.
     */
    public static final int SC_NOT_IMPLEMENTED =
        HttpServletResponse.SC_NOT_IMPLEMENTED;


    /**
     * Status code (502) indicating that the HTTP server received an
     * invalid response from a server it consulted when acting as a
     * proxy or gateway.
     */
    public static final int SC_BAD_GATEWAY =
        HttpServletResponse.SC_BAD_GATEWAY;


    /**
     * Status code (503) indicating that the HTTP service is
     * temporarily overloaded, and unable to handle the request.
     */
    public static final int SC_SERVICE_UNAVAILABLE =
        HttpServletResponse.SC_SERVICE_UNAVAILABLE;


    /**
     * Status code (100) indicating the client may continue with
     * its request.  This interim response is used to inform the
     * client that the initial part of the request has been
     * received and has not yet been rejected by the server.
     */
    public static final int SC_CONTINUE = 100;


    /**
     * Status code (405) indicating the method specified is not
     * allowed for the resource.
     */
    public static final int SC_METHOD_NOT_ALLOWED = 405;


    /**
     * Status code (409) indicating that the request could not be
     * completed due to a conflict with the current state of the
     * resource.
     */
    public static final int SC_CONFLICT = 409;


    /**
     * Status code (412) indicating the precondition given in one
     * or more of the request-header fields evaluated to false
     * when it was tested on the server.
     */
    public static final int SC_PRECONDITION_FAILED = 412;


    /**
     * Status code (413) indicating the server is refusing to
     * process a request because the request entity is larger
     * than the server is willing or able to process.
     */
    public static final int SC_REQUEST_TOO_LONG = 413;


    /**
     * Status code (415) indicating the server is refusing to service
     * the request because the entity of the request is in a format
     * not supported by the requested resource for the requested
     * method.
     */
    public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;


    // -------------------------------------------- Extended WebDav status code


    /**
     * Status code (207) indicating that the response requires
     * providing status for multiple independent operations.
     */
    public static final int SC_MULTI_STATUS = 207;
    // This one collides with HTTP 1.1
    // "207 Partial Update OK"


    /**
     * Status code (418) indicating the entity body submitted with
     * the PATCH method was not understood by the resource.
     */
    public static final int SC_UNPROCESSABLE_ENTITY = 418;
    // This one collides with HTTP 1.1
    // "418 Reauthentication Required"


    /**
     * Status code (419) indicating that the resource does not have
     * sufficient space to record the state of the resource after the
     * execution of this method.
     */
    public static final int SC_INSUFFICIENT_SPACE_ON_RESOURCE = 419;
    // This one collides with HTTP 1.1
    // "419 Proxy Reauthentication Required"


    /**
     * Status code (420) indicating the method was not executed on
     * a particular resource within its scope because some part of
     * the method's execution failed causing the entire method to be
     * aborted.
     */
    public static final int SC_METHOD_FAILURE = 420;


    /**
     * Status code (423) indicating the destination resource of a
     * method is locked, and either the request did not contain a
     * valid Lock-Info header, or the Lock-Info header identifies
     * a lock held by another principal.
     */
    public static final int SC_LOCKED = 423;
    
    /**
     * The 507 (Insufficient Storage) status code means the method
     * could not be performed on the resource because the server is
     * unable to store the representation needed to successfully complete
     * the request. This condition is considered to be temporary. If
     * the request that received this status code was the result of a
     * user action, the request MUST NOT be repeated until it is
     * requested by a separate user .
     */
    public static final int SC_INSUFFICIENT_STORAGE = 507;


    // ------------------------------------------------------------ Initializer


    static {
        // HTTP 1.0 status Code
        addStatusCodeMap(SC_OK, "OK");
        addStatusCodeMap(SC_CREATED, "Created");
        addStatusCodeMap(SC_ACCEPTED, "Accepted");
        addStatusCodeMap(SC_NO_CONTENT, "No Content");
        addStatusCodeMap(SC_MOVED_PERMANENTLY, "Moved Permanently");
        addStatusCodeMap(SC_MOVED_TEMPORARILY, "Moved Temporarily");
        addStatusCodeMap(SC_NOT_MODIFIED, "Not Modified");
        addStatusCodeMap(SC_BAD_REQUEST, "Bad Request");
        addStatusCodeMap(SC_UNAUTHORIZED, "Unauthorized");
        addStatusCodeMap(SC_FORBIDDEN, "Forbidden");
        addStatusCodeMap(SC_NOT_FOUND, "Not Found");
        addStatusCodeMap(SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
        addStatusCodeMap(SC_NOT_IMPLEMENTED, "Not Implemented");
        addStatusCodeMap(SC_BAD_GATEWAY, "Bad Gateway");
        addStatusCodeMap(SC_SERVICE_UNAVAILABLE, "Service Unavailable");
        addStatusCodeMap(SC_CONTINUE, "Continue");
        addStatusCodeMap(SC_METHOD_NOT_ALLOWED, "Method Not Allowed");
        addStatusCodeMap(SC_CONFLICT, "Conflict");
        addStatusCodeMap(SC_PRECONDITION_FAILED, "Precondition Failed");
        addStatusCodeMap(SC_REQUEST_TOO_LONG, "Request Too Long");
        addStatusCodeMap(SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type");
        // WebDav Status Codes
        addStatusCodeMap(SC_MULTI_STATUS, "Multi-Status");
        addStatusCodeMap(SC_UNPROCESSABLE_ENTITY, "Unprocessable Entity");
        addStatusCodeMap(SC_INSUFFICIENT_SPACE_ON_RESOURCE,
                         "Insufficient Space On Resource");
        addStatusCodeMap(SC_METHOD_FAILURE, "Method Failure");
        addStatusCodeMap(SC_LOCKED, "Locked");
        addStatusCodeMap(SC_INSUFFICIENT_STORAGE, "Insufficient Storage");
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Returns the HTTP status text for the HTTP or WebDav status code
     * specified by looking it up in the static mapping.  This is a
     * static function.
     *
     * @param   nHttpStatusCode [IN] HTTP or WebDAV status code
     * @return  A string with a short descriptive phrase for the
     *                  HTTP status code (e.g., "OK").
     */
    public static String getStatusText(int nHttpStatusCode) {
        Integer intKey = Integer.valueOf(nHttpStatusCode);

        if (!mapStatusCodes.containsKey(intKey)) {
            return "";
        } else {
            return mapStatusCodes.get(intKey);
        }
    }


    // -------------------------------------------------------- Private Methods


    /**
     * Adds a new status code -> status text mapping.  This is a static
     * method because the mapping is a static variable.
     *
     * @param   nKey    [IN] HTTP or WebDAV status code
     * @param   strVal  [IN] HTTP status text
     */
    private static void addStatusCodeMap(int nKey, String strVal) {
        mapStatusCodes.put(Integer.valueOf(nKey), strVal);
    }

}


