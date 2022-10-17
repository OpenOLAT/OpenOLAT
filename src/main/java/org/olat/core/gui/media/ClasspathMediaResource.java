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

package org.olat.core.gui.media;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;

/**
 * The ClasspathMediaResource delivers files directly from the classpath, e.g.
 * to deliver a css file that is located in the _static directory in your
 * package (src dir or from jar). Normally you just use the JsAndCSSComponent or
 * the ClassPathStaticDispatcher
 * 
 * @author Felix Jost
 */
public class ClasspathMediaResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(ClasspathMediaResource.class);
	
	private final String location;
	private Long lastModified;
	private Long size;
	private URL url;
	// local cache to minimize access to jar content (expensive)
	private static final Map<String,Long> cachedJarResourceLastModified = new ConcurrentHashMap<>();
	private static final Map<String,Long> cachedJarResourceSize = new ConcurrentHashMap<>();

	/**
	 * Constructor that uses class loader of this (ClasspathMediaResource) class
	 * @param pakkage the package name (e.g. org.olat.core)
	 * @param location the relative file path (e.g. _static/my/file.css)
	 */
	public ClasspathMediaResource(Package pakkage, String location) {
		this.location = location;
		//fxdiff FXOLAT-185:fix loading of files in jar
		StringBuilder sb = new StringBuilder();
		sb.append('/').append(pakkage.getName().replace(".", "/"));
		if(!location.startsWith("/")) sb.append('/');
		sb.append(location);
		
		if(Settings.isDebuging() && StringHelper.containsNonWhitespace(WebappHelper.getSourcePath())) {
			try {
				this.url = new File(WebappHelper.getSourcePath(), sb.toString()).toURI().toURL();
			} catch (MalformedURLException e) {
				this.url = getClass().getResource(sb.toString());
			}
		} else {
			this.url = getClass().getResource(sb.toString());
		}
		init(pakkage.getName());
	}

	/**
	 * Internal helper to initialize everything for this file
	 * 
	 * @param packageName
	 */
	private void init(String packageName) {
		if (url != null) { // null in case of resources not found.			
			String fileName = url.getFile();
			if (url.getProtocol().equals("jar")){
				int pathDelim = fileName.indexOf("!");
				if (WebappHelper.getContextRoot().startsWith("/")) {
					// First lookup in cache
					size = cachedJarResourceSize.get(fileName);
					lastModified = cachedJarResourceLastModified.get(fileName);
					if (size == null || lastModified == null) {
						// Not found in cache, read from jar and add to cache
						// Unix style: path should look like //my/absolute/path
						String jarPath = "/" + fileName.substring(5, pathDelim);
						// Rel path must not start with "!/", remove it
						String relPath	= fileName.substring(pathDelim + 2);
						File jarFile = new File(jarPath);
						try(JarFile jar = new JarFile(jarFile)) {
							// Get last modified and file size form jar entry
							ZipEntry entry = jar.getEntry(relPath);
							if (entry == null) {
								log.warn("jar resource at location '"+location+"' and package " + packageName + " was not found, could not resolve entry relPath::" + relPath);
							} else {
								size = new Long(entry.getSize());
								// Last modified of jar - getTime on jar entry is not stable
								lastModified = Long.valueOf(jarFile.lastModified());							
								// Add to cache - no need to synchronize, just overwrite
								cachedJarResourceSize.put(fileName, size);
								cachedJarResourceLastModified.put(fileName, lastModified);
							}
						} catch (IOException e) {
							log.warn("jar resource at location '"+location+"' and package " + packageName + " was not found!", e);
						}
					}					
				} else {
					// For windows ignore the jar size and last modified, it
					// works also without those parameters. Windows should not
					// be used in production
					this.lastModified = WebappHelper.getTimeOfServerStartup();					
				}
			} else {
				// Get last modified and file size
				File f = new File(fileName);
				if (f.exists()) {
					size = new Long(f.length());
					long lm = f.lastModified();
					lastModified = (lm != 0? lm : WebappHelper.getTimeOfServerStartup());
				} else {
					url = null;
				}
			}			
			if (log.isDebugEnabled()) {
				log.debug("resource found at URL::" + this.url
						+ " for package::" + packageName + " and location::"
						+ location + ", filesize::" + size + " lastModified::"
						+ lastModified);
			}
		} else {
			// resource was not found
			log.warn("resource at location '"+location+"' and package " + packageName + " was not found!");
		}
	}
	
	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_ONE_DAY;
	}
	
	@Override
	public boolean acceptRanges() {
		return true;
	}

	@Override
	public String getContentType() {
		String mimeType = WebappHelper.getMimeType(location);
		if (mimeType == null) mimeType = "application/octet-stream";
		return mimeType;
	}

	@Override
	public Long getSize() {
		if (size != null && size > 0) return size;
		else return null;
	}

	@Override
	public InputStream getInputStream() {
		InputStream is = null;
		if (url == null) return null;
		try {
			is = url.openStream();
		} catch (UnknownHostException host) {
			//catch host exception here which can occur when brasato.src is wrongly configured
			log.warn("cannot get inputstream for url:"+url.toExternalForm()+"Unknown host which starts with windows path like \"C\" points to a wrong configured brasato.src in the olat.properties file");
		} catch (IOException e) {
			log.warn("cannot get inputstream for url:"+url.toExternalForm());
		}
		return is;
	}

	@Override
	public Long getLastModified() {
		return lastModified;
	}

	@Override
	public void release() {
		// void
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		//  
	}

	@Override
	public String toString() {
		return "ClasspathMediaResource:"+url;
	}

	/**
	 * @return true: resource exists and can be delivered; false: resource could
	 *         not be found, delivery will fail
	 */
	public boolean resourceExists() {
		return url != null;
	}
}
