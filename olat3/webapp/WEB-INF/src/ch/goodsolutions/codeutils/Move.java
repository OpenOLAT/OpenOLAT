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
package ch.goodsolutions.codeutils;

import java.io.File;

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
public class Move {

	// even entries: source, uneven indexes: target
	private final String[] movePKGHistory = new String[]{"org.olat.gui", "org.olat.core.gui",
			"org.olat.core.gui.control.generic.portal", "org.olat.gui.control.generic.portal",
			"org.olat.dispatcher","org.olat.core.dispatcher",
			"org.olat.servlets","org.olat.core.servlets",
			"org.olat.configuration", "org.olat.core.configuration",
			"org.olat.logging","org.olat.core.logging",
			"org.olat.core.servlets", "org.olat.commons.servlets",
			"org.olat.util", "org.olat.core.util",
			"org.olat.core.gui.control.generic.htmleditor", "org.olat.core.commons.editor.htmleditor",
			"org.olat.core.gui.control.generic.filechooser", "org.olat.core.commons.file.filechooser",
			"org.olat.extensions", "org.olat.core.extensions",
			"org.olat.modules.sp","org.olat.core.commons.modules.singlepage",
			"org.olat.core.commons.file.filechooser", "org.olat.commons.file.filechooser"
	}; 

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Quick and dirty!
		//final String base = "C:/development/workspace/olathead/webapp";
		//final String base = "C:/home/patrick/workspace/olat3/webapp";
		final String base = "C:/development/eclipseworkspace31/head/webapp";
		
		final String srcbase = base + "/WEB-INF/src";


		// moved org.olat.core.gui.control.StatusDescription to org.olat.course.editor
		// moved org.olat.basesecurity.Identity to org.olat.core.id.Identity
		// moved org.olat.resource.OLATResourceable to org.olat.core.id
		// moved org.olat.core.dispatcher.jumpin.RepoJumpInHandlerFactory to org.olat.repository
		// moved org.olat.core.dispatcher.jumpin.SubscriptionJumpInHandlerFactory to org.olat.notifications
		// moved org.olat.core.logging.IMAppender and IMEvaluator to org.olat.instantMessaging
		// moved org.olat.core.util.prefs.IMPreferences to o.o.instantMessaging
		// moved "org.olat.modules.sp" to "org.olat.core.commons.modules.singlepage"
		// moved org.olat.core.configuration.ConfigurationManager -> org.olat.configuration.ConfigurationManager
		// moved org.olat.core.commons.file.filechooser -> org.olat.commons.file.filechooser
		
		final String packageFrom = "org.olat.core.commons.file.filechooser";
		final String packageTo = "org.olat.commons.file.filechooser";
		
		
		/*
		 * FIXME:fj:a also search in webapp/WEB-INF/ to replace strings in config.xml.in files.
		 */
		
		// move from to to with all subpackages and everything
		// ignore: test cvs here....
		
		String todir = srcbase + "/" + packageTo.replaceAll("\\.", "/");
		File todirF = new File(todir);
		todirF.mkdirs();

		String fromdir = srcbase + "/" + packageFrom.replaceAll("\\.", "/");
		File fromDir = new File(fromdir);

		System.out.println("copy from old to new place... you need to manually delete the old place (for eclipse's sake)");
		
		// leave original files intact and delete them with the navigator view in eclipse (otherwise the cvs info gets lost)
		
		FileUtils.copyDirContentsToDir(fromDir, todirF, false, "Move Packages");
		//FileUtils.deleteDirsAndFiles(fromDir, true, true);
		
		// now adjust some stuff like the package, and the import within the java
		// sources
		// package org.olat.admin(.xxx) -> package org.abc.def.ghi(.xxx)
		// and import org.olat.admin(.xxx) -> import org.abc.def.ghi(.xxx)
		// in headers

		// clear the cvs stuff
		FileVisitor fcvs = new FileVisitor() {

			public void visit(File file) {
				if (file.getParentFile().isDirectory() && file.getParentFile().getName().equals("CVS")) {
					FileUtils.deleteDirsAndFiles(file, true, true);
				}
			}

		};
		FileUtils.visitRecursively(todirF, fcvs);

		System.out.println("adjusting file contents......");
		
		final String regexp3 = packageFrom.replaceAll("\\.", "\\\\.");
		
		FileVisitor fv = new MF(regexp3, packageTo);

		FileUtils.visitRecursively(new File(srcbase), fv);

	}

}

class MF implements FileVisitor {
	private final String regexp;
	private final String replacement;
	
	MF(String regexp, String replacement) {
		this.regexp = regexp;
		this.replacement = replacement;
	}
	
	public void visit(File file) {
		// TODO Auto-generated method stub
		String fname = file.getName();
		if (!file.isDirectory() && (fname.endsWith(".java") || fname.endsWith(".xml")) && !fname.equals("Move.java")) {
			//System.out.println("file "+fname);
			// java src
			String data = FileUtils.load(file, "utf-8");
			String ndata = data.replaceAll(regexp, replacement);
			
			if (!data.equals(ndata)) {
				FileUtils.save(file, ndata, "utf-8");
				System.out.println("changed: "+file.getAbsolutePath());
			}
			data = null; ndata = null;
		} 
	}
}
