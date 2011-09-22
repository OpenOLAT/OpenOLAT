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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 2005-2006 by JGS goodsolutions GmbH, Switzerland<br>
 * http://www.goodsolutions.ch All rights reserved.
 * <p>
 */
package ch.unizh.codeutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;

import org.olat.core.util.FileUtils;
import org.olat.core.util.FileVisitor;

/**
 * Description:<br>
 * TODO: Felix Class Description for Copy
 * <P>
 * Initial Date: 21.04.2006 <br>
 * 
 * @author Felix
 */
public class CopyAndRenameI18nToSrcWithRespectToMovedPackages {

	static HashMap pathTranslation;
	static{
		/*
		 * all moved directories, including some special move backs and aways of some
		 * special java classes.
		 */
		pathTranslation = new HashMap();
		pathTranslation.put("/org/olat/gui", "/org/olat/core/gui");
		pathTranslation.put("/org/olat/gui/components","/org/olat/core/gui/components");
		pathTranslation.put("/org/olat/gui/components/choice","/org/olat/core/gui/components/choice");
		pathTranslation.put("/org/olat/gui/components/delegating","/org/olat/core/gui/components/delegating");
		pathTranslation.put("/org/olat/gui/components/form","/org/olat/core/gui/components/form");
		pathTranslation.put("/org/olat/gui/components/htmlheader","/org/olat/core/gui/components/htmlheader");
		pathTranslation.put("/org/olat/gui/components/image","/org/olat/core/gui/components/image");
		pathTranslation.put("/org/olat/gui/components/panel","/org/olat/core/gui/components/panel");
		pathTranslation.put("/org/olat/gui/components/progressbar","/org/olat/core/gui/components/progressbar");
		pathTranslation.put("/org/olat/gui/components/tabbedpane","/org/olat/core/gui/components/tabbedpane");
		pathTranslation.put("/org/olat/gui/components/table","/org/olat/core/gui/components/table");
		pathTranslation.put("/org/olat/gui/components/tree","/org/olat/core/gui/components/tree");
		pathTranslation.put("/org/olat/gui/components/velocity","/org/olat/core/gui/components/velocity");
		pathTranslation.put("/org/olat/gui/control","/org/olat/core/gui/control");
		//pathTranslation.put("/org/olat/core/gui/control/StatusDescription", "/org/olat/course/editor");//see next row
		pathTranslation.put("/org/olat/gui/control/StatusDescription", "/org/olat/course/editor");//file
		pathTranslation.put("/org/olat/gui/control/generic","/org/olat/core/gui/control/generic");
		pathTranslation.put("/org/olat/gui/control/generic/clone","/org/olat/core/gui/control/generic/clone");
		pathTranslation.put("/org/olat/gui/control/generic/closablewrapper","/org/olat/core/gui/control/generic/closablewrapper");
		pathTranslation.put("/org/olat/gui/control/generic/dialog","/org/olat/core/gui/control/generic/dialog");
		pathTranslation.put("/org/olat/gui/control/generic/docking","/org/olat/core/gui/control/generic/docking");
		pathTranslation.put("/org/olat/gui/control/generic/dtabs","/org/olat/core/gui/control/generic/dtabs");
		/*
		 * START move arounds
		 */
		//pathTranslation.put("/org/olat/gui/control/generic/filechooser","/org/olat/core/gui/control/generic/filechooser");
		//pathTranslation.put("/org/olat/gui/control/generic/htmleditor","/org/olat/core/gui/control/generic/htmleditor");
		//pathTranslation.put("/org/olat/core/gui/control/generic/htmleditor", "/org/olat/core/commons/editor/htmleditor");
		//pathTranslation.put("/org/olat/core/gui/control/generic/filechooser", "/org/olat/core/commons/file/filechooser");
		//pathTranslation.put("/org/olat/core/commons/file/filechooser", "/org/olat/commons/file/filechooser");
		pathTranslation.put("/org/olat/gui/control/generic/filechooser","/org/olat/commons/file/filechooser");
		pathTranslation.put("/org/olat/gui/control/generic/htmleditor","/org/olat/core/commons/editor/htmleditor");
		/*
		 * END move arounds
		 */
		pathTranslation.put("/org/olat/gui/control/generic/iframe","/org/olat/core/gui/control/generic/iframe");
		pathTranslation.put("/org/olat/gui/control/generic/layout","/org/olat/core/gui/control/generic/layout");
		pathTranslation.put("/org/olat/gui/control/generic/layouter","/org/olat/core/gui/control/generic/layouter");
		pathTranslation.put("/org/olat/gui/control/generic/lock","/org/olat/core/gui/control/generic/lock");
		//pathTranslation.put("/org/olat/gui/control/generic/portal","/org/olat/core/gui/control/generic/portal");
		//pathTranslation.put("/org/olat/core/gui/control/generic/portal", "/org/olat/gui/control/generic/portal");
		pathTranslation.put("/org/olat/gui/control/generic/spacesaver","/org/olat/core/gui/control/generic/spacesaver");
		pathTranslation.put("/org/olat/gui/control/generic/tabbable","/org/olat/core/gui/control/generic/tabbable");
		pathTranslation.put("/org/olat/gui/control/generic/tool","/org/olat/core/gui/control/generic/tool");
		pathTranslation.put("/org/olat/gui/control/generic/wizard","/org/olat/core/gui/control/generic/wizard");
		pathTranslation.put("/org/olat/gui/control/info","/org/olat/core/gui/control/info");
		pathTranslation.put("/org/olat/gui/control/locks","/org/olat/core/gui/control/locks");
		pathTranslation.put("/org/olat/gui/css","/org/olat/core/gui/css");
		pathTranslation.put("/org/olat/gui/dev","/org/olat/core/gui/dev");
		pathTranslation.put("/org/olat/gui/exception","/org/olat/core/gui/exception");
		pathTranslation.put("/org/olat/gui/formelements","/org/olat/core/gui/formelements");
		pathTranslation.put("/org/olat/gui/media","/org/olat/core/gui/media");
		pathTranslation.put("/org/olat/gui/render","/org/olat/core/gui/render");
		pathTranslation.put("/org/olat/gui/render/velocity","/org/olat/core/gui/render/velocity");
		pathTranslation.put("/org/olat/gui/translator","/org/olat/core/gui/translator");
		//
		pathTranslation.put("/org/olat/dispatcher","/org/olat/core/dispatcher");
		pathTranslation.put("/org/olat/dispatcher/jumpin","/org/olat/core/dispatcher/jumpin");
		pathTranslation.put("/org/olat/dispatcher/mapper","/org/olat/core/dispatcher/mapper");
		//pathTranslation.put("/org/olat/core/dispatcher/jumpin/RepoJumpInHandlerFactory","/org/olat/repository");//see later
		//pathTranslation.put("/org/olat/core/dispatcher/jumpin/SubscriptionJumpInHandlerFactory","/org/olat/notifications")//see later
		pathTranslation.put("/org/olat/dispatcher/jumpin/RepoJumpInHandlerFactory","/org/olat/repository");//file
		pathTranslation.put("/org/olat/dispatcher/jumpin/SubscriptionJumpInHandlerFactory","/org/olat/notifications");//file
		//
		//
		//pathTranslation.put("/org/olat/servlets","/org/olat/core/servlets");
		//pathTranslation.put("/org/olat/core/servlets", "/org/olat/commons/servlets");
		pathTranslation.put("/org/olat/servlets","/org/olat/commons/servlets");
		//
		pathTranslation.put("/org/olat/configuration", "/org/olat/core/configuration");
		//pathTranslation.put("/org/olat/core/configuration/ConfigurationManager","/org/olat/configuration/ConfigurationManager");//see next line
		pathTranslation.put("/org/olat/configuration/ConfigurationManager","/org/olat/configuration");
		//
		pathTranslation.put("/org/olat/logging","/org/olat/core/logging");
		//pathTranslation.put("/org/olat/core/logging/IMAppender","/org/olat/instantMessaging");
		//pathTranslation.put("/org/olat/core/logging/IMEvaluator","/org/olat/instantMessaging");
		pathTranslation.put("/org/olat/logging/IMAppender","/org/olat/instantMessaging");
		pathTranslation.put("/org/olat/logging/IMEvaluator","/org/olat/instantMessaging");
		//
		pathTranslation.put("/org/olat/util", "/org/olat/core/util");
		pathTranslation.put("/org/olat/util/bulk","/org/olat/core/util/bulk");
		pathTranslation.put("/org/olat/util/cache","/org/olat/core/util/cache");
		pathTranslation.put("/org/olat/util/component","/org/olat/core/util/component");
		pathTranslation.put("/org/olat/util/controller","/org/olat/core/util/controller");
		pathTranslation.put("/org/olat/util/event","/org/olat/core/util/event");
		pathTranslation.put("/org/olat/util/locks","/org/olat/core/util/locks");
		pathTranslation.put("/org/olat/util/mail","/org/olat/core/util/mail");
		pathTranslation.put("/org/olat/util/memento","/org/olat/core/util/memento");
		pathTranslation.put("/org/olat/util/nodes","/org/olat/core/util/nodes");
		pathTranslation.put("/org/olat/util/prefs","/org/olat/core/util/prefs");
		//pathTranslation.put("/org/olat/core/util/prefs/IMPreferences","/org/olat/instantMessaging");
		pathTranslation.put("/org/olat/util/prefs/IMPreferences","/org/olat/instantMessaging");
		pathTranslation.put("/org/olat/util/radeox","/org/olat/core/util/radeox");
		pathTranslation.put("/org/olat/util/rss","/org/olat/core/util/rss");
		pathTranslation.put("/org/olat/util/storage","/org/olat/core/util/storage");
		pathTranslation.put("/org/olat/util/storage/test","/org/olat/core/util/storage/test");
		pathTranslation.put("/org/olat/util/traversal","/org/olat/core/util/traversal");
		pathTranslation.put("/org/olat/util/tree","/org/olat/core/util/tree");
		pathTranslation.put("/org/olat/util/vfs","/org/olat/core/util/vfs");
		pathTranslation.put("/org/olat/util/vfs/callbacks","/org/olat/core/util/vfs/callbacks");
		pathTranslation.put("/org/olat/util/vfs/filters","/org/olat/core/util/vfs/filters");
		pathTranslation.put("/org/olat/util/vfs/util","/org/olat/core/util/vfs/util");
		pathTranslation.put("/org/olat/util/xml","/org/olat/core/util/xml");
		//		
		pathTranslation.put("/org/olat/extensions", "/org/olat/core/extensions");
		pathTranslation.put("/org/olat/extensions/action","/org/olat/core/extensions/action");
		pathTranslation.put("/org/olat/extensions/css","/org/olat/core/extensions/css");
		pathTranslation.put("/org/olat/extensions/globalmapper","/org/olat/core/extensions/globalmapper");
		pathTranslation.put("/org/olat/extensions/helpers","/org/olat/core/extensions/helpers");
		pathTranslation.put("/org/olat/extensions/hibernate","/org/olat/core/extensions/hibernate");
		pathTranslation.put("/org/olat/extensions/sitescreator","/org/olat/core/extensions/sitescreator");
		//
		pathTranslation.put("/org/olat/modules/sp","/org/olat/core/commons/modules/singlepage");
		//
		pathTranslation.put("/org/olat/basesecurity/Identity","/org/olat/core/id");
		pathTranslation.put("/org/olat/resource/OLATResourceable","/org/olat/core/id");
		
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//String base = "C:/home/patrick/workspace/olat3RefactoredHead/webapp";
		//
		// could copy/move from different projects
		String baseFrom ="C:/home/patrick/workspace/olat3/webapp";
		String baseTo = "C:/home/patrick/workspace/olat3/webapp";
		// copy language files from project to project
		final String i18nbase = baseFrom + "/i18n/default";
		final String srcbase = baseTo + "/WEB-INF/src";

		final boolean doIt = args != null && args.length>0 && args[0].equals("DOIT");
		//if true -> copy files otherwise show only what would be done.
		
		
		/**
		 * prepare step
		 
		FileVisitor prep = new FileVisitor(){
		
			public void visit(File file) {
				if(file.isDirectory() && file.getName().length()>0 &&!file.getName().startsWith("CVS") && !file.getName().startsWith("i18n") && !file.getName().startsWith("raw") && !file.getName().startsWith("content")){
					String rel = file.getAbsolutePath().substring(srcbase.length());
					rel = rel.replaceAll("\\\\",".");
					if(!rel.equals("") && rel.charAt(0)=='.'){
						rel = rel.substring(1,rel.length());
					}
					String core = rel.replaceFirst("/org/olat","/org/olat.core");
					System.out.println("pathTranslation.put(\""+rel+"\",\""+core+"\");");
				}
			}
		
		};
		FileUtils.visitRecursively(new File(srcbase),prep, true);
		System.out.println("END");
		if(true) return;
		**/
		
		
		FileVisitor fv2 = new FileVisitor() {
			
			public void visit(File file) {
				// TODO Auto-generated method stub
				String fname = file.getName();
				if (!file.isDirectory() && fname.equals("LocalStrings.properties")) { // /de/org/olat/admin/cache/LocalStrings.properties
					String rel = file.getParentFile().getAbsolutePath().substring(i18nbase.length());
					rel = rel.substring(1);
					
					rel = rel.replaceAll("\\\\","/");
					
					String lang = rel.substring(0, rel.indexOf("/")); // de
					String pack = rel.substring(rel.indexOf("/"));  // /org/olat/admin/cache
				
					String tarDir;
					if(pathTranslation.containsKey(pack)){
						//it is one of the moved packages
						tarDir = srcbase + ((String)pathTranslation.get(pack)) +"/_i18n";
					}else{
						tarDir = srcbase+pack + "/_i18n";
					}
					
					File tarDirF = new File(tarDir);
					// should already exist tarDirF.mkdirs();
					File target = new File(tarDirF, "LocalStrings_"+lang+".properties");
					System.out.println("=====+++++++");
					System.out.println("FROM:"+file.getAbsolutePath());
					System.out.println("("+tarDirF.exists()+") TO:"+target.getAbsolutePath());
					if (doIt) {
						FileInputStream fis;
						try {
							fis = new FileInputStream(file);
							FileOutputStream fos = new FileOutputStream(target);
							FileUtils.copy(fis, fos);
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}
			}
		};

		FileUtils.visitRecursively(new File(i18nbase), fv2);

	}

}
