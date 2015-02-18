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
package org.olat.test;

import java.io.File;
import java.io.FileFilter;

import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class ArquillianDeployments {

	public static final String MAIN_JAVA = "src/main/java";
	public static final String MAIN_RSRC = "src/main/resources";
	public static final String WEBAPP    = "src/main/webapp";
	public static final String WEBINF    = "src/main/webapp/WEB-INF";
	public static final String WEBINF_TOMCAT = "src/main/webapp-tomcat/WEB-INF";
	public static final String TEST_RSRC = "src/test/resources";
	public static final String LIB_DIR   = "target/openolat-lms-10.3-SNAPSHOT/WEB-INF/lib";

	
	public static WebArchive createDeployment() {
		return createDeployment("openolat.war");
	}
		

	public static WebArchive createDeployment(String name) {
		WebArchive archive = ShrinkWrap.create(WebArchive.class, name);

		addClasses(archive);
		addLibraries(archive);
		addWebInfResources(archive);
		addResourceRecursive(new File(MAIN_JAVA), null, new JavaResourcesFilter(), archive);
		addResourceRecursive(new File(MAIN_RSRC), null, new AllFileFilter(), archive);
		addWebResourceRecursive(new File(WEBAPP), "static", new StaticFileFilter(), archive);
		
		archive.addAsResource(new File("src/test/profile/mysql", "olat.local.properties"), "olat.local.properties");
		archive.setWebXML(new File(WEBINF_TOMCAT, "web.xml"));
		return archive;
	}
	
	public static WebArchive addLibraries(WebArchive archive) {
		File libDir = new File(LIB_DIR);
		File[] libs = libDir.listFiles(new LibrariesFilter());
		return archive.addAsLibraries(libs);
	}
	
	public static WebArchive addClasses(WebArchive archive) {
		return archive
				.addPackages(true, new FilterUnusedPackage(), "org.olat", "de.bps", "de.tuchemnitz.wizard");
	}
	
	public static WebArchive addWebInfResources(WebArchive archive) {
		return archive
				.addAsWebInfResource(new File(WEBINF, "olat_portals_links.xsd"), "olat_portals_links.xsd")
				.addAsWebInfResource(new File(WEBINF, "olat_portals_links.xml"), "olat_portals_links.xml")
				.addAsWebInfResource(new File(WEBINF, "olat_portals_institution.xml"), "olat_portals_institution.xml")
				.addAsWebInfResource(new File(WEBINF, "sun-jaxws.xml"), "sun-jaxws.xml");
	}
	
	public static WebArchive addWebResourceRecursive(File root, String startPath, FileFilter filter, WebArchive archive) {
		File startDir = startPath == null ? root : new File(root, startPath);
		if(startPath == null) {
			startPath = "/";
		} else if(!startPath.endsWith("/")) {
			startPath += "/";
		}
		for(File resource:startDir.listFiles(filter)) {
			if(resource.isHidden()) {
				continue;
			} else if(resource.isFile()) {
				archive.addAsWebResource(resource, startPath + resource.getName());
			} else if(resource.isDirectory()) {
				String nextPath = startPath + resource.getName() + "/";
				addWebResourceRecursive(root, nextPath, filter, archive);
			}
		}
		return archive;
	}
	
	
	public static WebArchive addResourceRecursive(File root, String startPath, FileFilter filter, WebArchive archive) {
		File startDir = startPath == null ? root : new File(root, startPath);
		if(startPath == null) {
			startPath = "/";
		} else if(!startPath.endsWith("/")) {
			startPath += "/";
		}
		for(File resource:startDir.listFiles(filter)) {
			if(resource.isHidden()) {
				continue;
			} else if(resource.isFile()) {
				archive.addAsResource(resource, startPath + resource.getName());
			} else if(resource.isDirectory()) {
				String nextPath = startPath + resource.getName() + "/";
				addResourceRecursive(root, nextPath, filter, archive);
			}
		}
		return archive;
	}
	
	private static class JavaResourcesFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			String filename = pathname.getName();
			if(filename.endsWith(".properties")) {
				if(filename.endsWith("_ar.properties")
						|| filename.endsWith("_bg.properties")
						|| filename.endsWith("_cs.properties")
						|| filename.endsWith("_da.properties")
						|| filename.endsWith("_el.properties")
						|| filename.endsWith("_es.properties")
						|| filename.endsWith("_fa.properties")
						|| filename.endsWith("_fr.properties")
						|| filename.endsWith("_it.properties")
						|| filename.endsWith("_jp.properties")
						|| filename.endsWith("_lt.properties")
						|| filename.endsWith("_nl_NL.properties")
						|| filename.endsWith("_pl.properties")
						|| filename.endsWith("_pt_BR.properties")
						|| filename.endsWith("_pt_PT.properties")
						|| filename.endsWith("_ru.properties")
						|| filename.endsWith("_sq.properties")
						|| filename.endsWith("_bg.properties")
						|| filename.endsWith("_zh_CN.properties")
						|| filename.endsWith("_zh_TW.properties")) {
					return false;
				}
			} else if (filename.endsWith(".java")
					|| filename.endsWith(".vsd")
					|| filename.endsWith(".odg")
					|| filename.endsWith(".odt")
					|| filename.endsWith(".pdf")) {
				return false;
			}
			return true;
		}
	}
	
	private static class LibrariesFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			return true;
		}
	}
	
	private static class StaticFileFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			String path = pathname.toString();
			boolean exclude = (path.contains("/static/themes/openolat/")
					|| path.contains("/static/themes/openolatexample")
					|| path.endsWith(".scss")
					|| path.endsWith(".psd")
					|| path.endsWith(".pxml")
					|| path.endsWith(".sh")
					|| path.endsWith(".scss")
					|| path.endsWith(".zip")
					|| path.endsWith(".pxm"));
			return !exclude;
		}
	}
	
	private static class AllFileFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			return true;
		}
	}
						
	private static class FilterUnusedPackage implements Filter<ArchivePath> {
		@Override
		public boolean include(ArchivePath path) {
			String pathStr = path.toString();
			boolean exclude = (pathStr.contains("/org/olat/core/test")
					|| pathStr.contains("/org/olat/selenium")
					|| pathStr.contains("/org/olat/test/")
					|| pathStr.endsWith("Test.class]"));
			return !exclude;
		}
	}
}
