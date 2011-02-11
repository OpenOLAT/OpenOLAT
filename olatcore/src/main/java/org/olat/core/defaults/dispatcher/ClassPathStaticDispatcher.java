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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.defaults.dispatcher;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherAction;
import org.olat.core.gui.media.ClasspathMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.LogDelegator;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;

/**
 * Description:<br>
 * Allows to register static mappers. Here you can create urls which are valid
 * for all users and can be cached by browsers. The delivered resources must be
 * static files in the _static directory of your code.
 * <p>
 * In mod-jk mode this static files can be delivered directly from apache or
 * even from another file server
 * <p>
 * If you need global mappers that provide dynamic content, then you mus use the
 * GlobalMapperRegistry.java
 * <p>
 * If you need urls that are only accessible for one user, use the
 * MapperRegistry.java
 * <P>
 * Initial Date: 6.10.2008 <br>
 * 
 * @author Florian Gnaegi
 */
public class ClassPathStaticDispatcher extends LogDelegator implements Dispatcher {
	static final String STATIC_DIR_NAME = "_static";

	private static String PATH_CLASSPATH_STATIC;
	private static boolean copyStaticFiles;
	private static ClassPathStaticDispatcher INSTANCE;

	/**
	 * Constructor, only used by spring. Use getInstance instead!
	 * 
	 * @param copyStaticFilesConfig
	 *            true: copy static files and deliver via StaticMediaDispatcher;
	 *            false: don't copy files and deliver from classpath
	 * @param dispatcherPath
	 *            : path where to dispatch files ( e.g. '/classpath/')
	 */
	public ClassPathStaticDispatcher(boolean copyStaticFilesConfig, String dispatcherPath) {
		PATH_CLASSPATH_STATIC = dispatcherPath;
		INSTANCE = this;
		copyStaticFiles = copyStaticFilesConfig;
		if (copyStaticFiles) {
			copyStaticClassPathFiles();
		}
	}

	/**
	 * @return MapperRegistry
	 */
	public static ClassPathStaticDispatcher getInstance() {
		return INSTANCE;
	}

	/**
	 * Create a path for the _static directory for the given class. Resources that
	 * are in this directory can be addressed relatively to the generated mapper
	 * path. The resources can also be stored in a jar file.
	 * 
	 * 
	 * @param globalNameClass class for the name of the mapper. the name of the
	 *          mapper is the name of the package name for this class
	 * @return the path under which this mapper will be called, without / at the
	 *         end, e.g. /olat/classpath/612/org.olat.demo.tabledemo (the 612
	 *         here is the versionId to guarantee the uniqueness across releases
	 *         to trick out buggy css browser caches)
	 */
	public String getMapperBasePath(Class clazz) {
		return getMapperBasePath(clazz, true);
	}

	/**
	 * Create a path for the _static directory for the given class. Resources that
	 * are in this directory can be addressed relatively to the generated mapper
	 * path. The resources can also be stored in a jar file.
	 * 
	 * @param clazz The package name of this class is used for the path
	 * @param addVersionID true: the build version is added to the URL to force
	 *          browser reload the resource when releasing a new version; false:
	 *          don't add version (but allow browsers to cache even when resource
	 *          has changed). Only use false when really needed
	 * @return the path under which this mapper will be called, without / at the
	 *         end, e.g. /olat/classpath/612/org.olat.demo.tabledemo (the 612 here
	 *         is the versionId to guarantee the uniqueness across releases to
	 *         trick out buggy css browser caches)
	 */
	public String getMapperBasePath(Class clazz, boolean addVersionID) {
		StringBuffer sb = new StringBuffer();
		sb.append(WebappHelper.getServletContextPath());
		// When mod jk is enabled, the files are deliverd by apache. During
		// startup in the classpath that live in a _static directory are copied to
		// the webapp/static/classpath/ directory.
		// Thus, when mod_jk is enabled, the path is different then when delivered
		// by tomcat.
		// Examples:
		// mod_jk disabled: /olat/classpath/61x/org.olat.core/myfile.css
		// -> dispatched by DispatcherAction, delivered by ClassPathStaticDispatcher
		// mod_jk enabled: /olat/raw/61x/classpath/org.olat.core/myfile.css
		// -> /olat/raw is unmounted in apache config, files delivered by apache
		if (copyStaticFiles) {
			sb.append(StaticMediaDispatcher.getStaticMapperPath());
			if (addVersionID) {
				// version before classpath, compatible with StaticMediaDiapatcher
				sb.append(Settings.getBuildIdentifier()); 				
			} else {
				sb.append(StaticMediaDispatcher.NOVERSION);
			}
			sb.append(PATH_CLASSPATH_STATIC.substring(0, PATH_CLASSPATH_STATIC.length())); 
		} else {
			sb.append(PATH_CLASSPATH_STATIC);
			if (addVersionID) {
				// version after classpath, compatible with ClassPathStaticDispatcher
				sb.append(Settings.getBuildIdentifier());			
			} else {
				sb.append(StaticMediaDispatcher.NOVERSION);
			}
			sb.append("/");
		}
		String className = clazz.getName();
		int ls = className.lastIndexOf('.');
		// post: ls != -1, since we don't use the java default package
		String pkg = className.substring(0, ls);
		// using baseClass.getPackage() would add unneeded inefficient and synchronized code
		if (pkg != null) sb.append(pkg);
		return sb.toString();			
	}
	
	/**
	 * @param hreq
	 * @param hres
	 */
	public void execute(HttpServletRequest hreq, HttpServletResponse hres, String pathInfo) {
		String path = hreq.getPathInfo();
		// e.g. /olat/classpath/612/org.olat.demo.tabledemo/blu.html
		// or /olat/classpath/_noversion_/org.olat.demo.tabledemo/blu.html
		String prefix;
		if (path.indexOf(PATH_CLASSPATH_STATIC + Settings.getBuildIdentifier()) != -1) {
			prefix = PATH_CLASSPATH_STATIC + Settings.getBuildIdentifier() + "/";
		} else if (path.indexOf(PATH_CLASSPATH_STATIC + StaticMediaDispatcher.NOVERSION) != -1) {
			prefix = PATH_CLASSPATH_STATIC + StaticMediaDispatcher.NOVERSION + "/";
		} else {
			logWarn("Invalid static path::" + path + " - sent 404", null);
			DispatcherAction.sendNotFound(hreq.getRequestURI(), hres);
			return;						
		}
		String subInfo = path.substring(prefix.length());
		int slashPos = subInfo.indexOf('/');
		if (slashPos == -1) {
			logWarn("Invalid static path::" + path + " - sent 404", null);
			DispatcherAction.sendNotFound(hreq.getRequestURI(), hres);
			return;
		}
		// packageName e.g. org.olat.demo
		String packageName = subInfo.substring(0, slashPos);
		String relPath = subInfo.substring(slashPos);
		// brasato:: can this happen at all, or does tomcat filter out - till now never reached - needs some little cpu cycles
		if (relPath.indexOf("..") != -1) {
			logWarn("ClassPathStatic resource path contained '..': relpath::" + relPath + " - sent 403", null);
			DispatcherAction.sendForbidden(hreq.getRequestURI(), hres);
		}
		// /bla/blu.html
		Package pakkage;
		pakkage = Package.getPackage(packageName);
		MediaResource mr = createClassPathStaticFileMediaResourceFor(pakkage, relPath);
		ServletUtil.serveResource(hreq, hres, mr);
	}
	
	/**
	 * Create a static class path media resource form a given base class
	 * @param baseClass
	 * @param relPath
	 * @return
	 */
	public MediaResource createClassPathStaticFileMediaResourceFor(Class baseClass, String relPath) {
		return new ClasspathMediaResource(baseClass, STATIC_DIR_NAME + relPath);
	}

	/**
	 * Create a static class path media resource form a given package
	 * @param baseClass
	 * @param relPath
	 * @return
	 */
	public MediaResource createClassPathStaticFileMediaResourceFor(Package pakkage, String relPath) {
		return new ClasspathMediaResource(pakkage, STATIC_DIR_NAME + relPath);
	}

	/**
	 * Helper method to copy all class path static files to the
	 * webapp/static/classpath/ directory for direct delivery via apache.
	 * <p>
	 * This method should only be called once at startup. To speed up things it
	 * checks on the last modified date of the files and copies only new files.
	 */
	public void copyStaticClassPathFiles() {
		StringBuffer path = new StringBuffer();
		path.append(WebappHelper.getContextRoot());
		path.append(StaticMediaDispatcher.STATIC_DIR_NAME);
		path.append(PATH_CLASSPATH_STATIC.substring(0, PATH_CLASSPATH_STATIC.length())); 
		File classPathStaticDir = new File(path.toString());
		
		// 1)  copy files from compiled web app classpath
		String srcPath = WebappHelper.getContextRoot() + "/WEB-INF/classes";
		logInfo("Copying static file from webapp source path::" + srcPath + " - be patient, this can take a while the first time when you hava jsmath files installed", null);
		ClassPathStaticDirectoriesVisitor srcVisitor = new ClassPathStaticDirectoriesVisitor(srcPath, classPathStaticDir);
		FileUtils.visitRecursively(new File(srcPath), srcVisitor);
		String brasatoSrcPath = WebappHelper.getCoreSourcePath();
		// 2) When the brasato source path is defined, add also copy the static files form there
		// But first check that the brasato source path is not configured within the application
		// source code (as it is on nightly server setup)
		if (brasatoSrcPath != null && !brasatoSrcPath.startsWith(srcPath)) {
			logInfo("Copying static file from brasato source path::" + brasatoSrcPath, null);
			ClassPathStaticDirectoriesVisitor coreSrcVisitor = new ClassPathStaticDirectoriesVisitor(brasatoSrcPath, classPathStaticDir);
			FileUtils.visitRecursively(new File(brasatoSrcPath), coreSrcVisitor);
		}
		// 3) Search in libs directory
		String libDirPath = WebappHelper.getContextRoot() + "/WEB-INF/lib";
		logInfo("Copying static file from jar files from dir::" + libDirPath, null);
		ClassPathStaticDirectoriesVisitor libVisitor = new ClassPathStaticDirectoriesVisitor(libDirPath, classPathStaticDir);
		FileUtils.visitRecursively(new File(libDirPath), libVisitor);
	}

}


















