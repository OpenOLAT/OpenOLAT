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
*/
package org.olat.commons.servlets;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.olat.commons.servlets.pathhandlers.PathHandler;
import org.olat.commons.servlets.util.ResourceDescriptor;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * @author Mike Stock Comment:
 *         <p>
 *         This class use to be the StaticsServlet.class. It has been refactored
 *         to implement the dispatcher interface. Please note that this statics
 *         dispatcher is legacy and should not be used to deliver static
 *         resources anymore. See the deprecated comments for more information.
 *         <p>
 *         This servlet extracts a handlerName from the first sub-path of the
 *         request's relative path. StaticsModule provides a handler class based
 *         on that name. Handler classes are configured in jpublish-xml's config
 *         for the StaticsModule. Handlers must implement the PathHandler
 *         interface. See FilePathHandler for an example of a PathHandler which
 *         resolves files in a filesystem. A handler is called by this servlet
 *         with the remaining path as argument. The handlers know how to get to
 *         the resource themselves.
 *         <p>
 * @deprecated Please use GlobalMapperRegistry if you need to provide an url for
 *             e.g. static resources which are shared by all users
 */
public class StaticsLegacyDispatcher implements Dispatcher {
	private static final Logger log = Tracing.createLoggerFor(StaticsLegacyDispatcher.class);

    /**
     * Default constructor.
     */
    public StaticsLegacyDispatcher() {
        super();
    }
		
	/**
     * @see org.olat.core.dispatcher.Dispatcher#execute(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
	@Override
	public void execute(HttpServletRequest req, HttpServletResponse resp) {
		try {
    		String method = req.getMethod();
	        if (method.equals("GET")) {
	            doGet(req, resp);
	        } else if (method.equals("HEAD")) {
	            doHead(req, resp);
	        } else {
	        	DispatcherModule.sendNotFound(req.getRequestURI(), resp);
	        }
		} catch (IOException e) {
			/*
			 * silently ignore forward errors (except in debug mode), since IE
			 * causes tons of such messages by its double GET request
			 */
			if (log.isDebugEnabled()) {
				log.debug("could not execute legacy statics method:" + e.toString() + ",msg:" + e.getMessage());
			}
		}
    }

    /**
     * Process a GET request for the specified resource.
     * 
     * @param request
     *            The servlet request we are processing
     * @param response
     *            The servlet response we are creating
     * 
     * @exception IOException
     *                if an input/output error occurs
     */
    protected void doGet(HttpServletRequest request,  HttpServletResponse response) throws IOException {
        // just to indicate that method must return if false is returned
        if (!serveResource(request, response, true)) {
        	return;
        }
    }

    /**
     * Process a HEAD request for the specified resource.
     * 
     * @param request
     *            The servlet request we are processing
     * @param response
     *            The servlet response we are creating
     * 
     * @exception IOException
     *                if an input/output error occurs
     */
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // just to indicate that method must return if false is returned
        if (!serveResource(request, response, false)) {
        	return;
        }
    }

    /**
     * Serve the requested resource.
     * 
     * @param request
     * @param response
     * @param copyContent
     * @return False if serving the resource failed/was aborted.
     * @throws IOException
     */
    private boolean serveResource(HttpServletRequest request, HttpServletResponse response, boolean copyContent)
            throws IOException {
        // just another internal forward or even a direct call
        String path = getRelativePath(request);
        if (path.indexOf("/secstatic/") == 0) {
        	path = path.substring(10, path.length());
        }
        PathHandler handler = null;
        String relPath = null;
        String handlerName = null;
        long start = 0;
        
        boolean logDebug = log.isDebugEnabled();
        if (logDebug) start = System.currentTimeMillis();
        try {
            relPath = path.substring(1);
            int index = relPath.indexOf('/');
            if (index != -1) {
                handlerName = relPath.substring(0, index);
                relPath = relPath.substring(index);
            }

            if (handlerName != null) {
                    handler = StaticsModule.getInstance(handlerName);
            }
        } catch (IndexOutOfBoundsException e) {
            // if some problem with the url, we assign no handler
        }

        if (handler == null || relPath == null) {
            // no handler found or relPath incomplete
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return false;
        }

        ResourceDescriptor rd = handler.getResourceDescriptor(request, relPath);
        if (rd == null) {
            // no handler found or relPath incomplete
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return false;
        }

        // check if modified since
        long ifModifiedSince = request.getDateHeader("If-Modified-Since");
        long lastMod = rd.getLastModified();
        if (lastMod != -1L && ifModifiedSince >= lastMod) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return false;
        }

        // server the resource
        if (copyContent) {
            InputStream is = handler.getInputStream(request, rd);
            if (is == null) {
                // resource not found or access denied
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return false;
            }
            StaticMediaResource smr = new StaticMediaResource(is, rd);
            ServletUtil.serveResource(request, response, smr);
            if (logDebug) {
                long stop = System.currentTimeMillis();  
                log.debug("Serving resource '" + relPath + "' ("+rd.getSize()+" bytes) in "+ (stop-start) +"ms with handler '" + handlerName + "'.");
            }
        }
        return true;
    }
    
    /**
     * Return the relative path associated with this servlet.
     * 
     * @param request
     *            The servlet request we are processing
     */
    private String getRelativePath(HttpServletRequest request) {

        String result = request.getPathInfo();
        if ((result == null) || (result.equals(""))) {
            result = "/";
        }
        return ServletUtil.normalizePath(result);
    }
    
    private static class StaticMediaResource implements MediaResource {
    	
    	private final InputStream inStream;
    	private final ResourceDescriptor rd;
    	
    	public StaticMediaResource(InputStream inStream, ResourceDescriptor rd) {
    		this.inStream = inStream;
    		this.rd = rd;
    	}
    	
    	@Override
    	public boolean acceptRanges() {
    		return false;
    	}

		@Override
		public String getContentType() {
			return rd.getContentType();
		}

		@Override
		public long getCacheControlDuration() {
			return ServletUtil.CACHE_ONE_DAY;
		}

		@Override
		public Long getSize() {
			return rd.getSize();
		}

		@Override
		public InputStream getInputStream() {
			return inStream;
		}

		@Override
		public Long getLastModified() {
			return rd.getLastModified();
		}

		@Override
		public void prepare(HttpServletResponse hres) {
			//
		}

		@Override
		public void release() {
			IOUtils.closeQuietly(inStream);
		}
    }
}