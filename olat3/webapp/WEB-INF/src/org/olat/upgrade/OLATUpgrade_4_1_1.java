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
* <p>
*/ 


package org.olat.upgrade;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.FileVisitor;

/**
 * Description:<br> 
 * - Creates all efficiency statements for all users for all
 * courses
 * <P>
 * Initial Date: 15.08.2005 <br>
 * 
 * @author gnaegi
 */
  public class OLATUpgrade_4_1_1 extends OLATUpgrade {
  OLog log = Tracing.createLoggerFor(this.getClass());
	private static final String VERSION = "OLAT_4.1.1";
	private static final String TASK_REPLACE_OLDINTERNALLINKS = "replace internal links with new form";

	/**
	 * @see org.olat.upgrade.OLATUpgrade#doPreSystemInitUpgrade(org.olat.upgrade.UpgradeManager)
	 */
	public boolean doPreSystemInitUpgrade(UpgradeManager upgradeManager) {
		// nothing to do here
		return false;
	}

	/**
	 * @see org.olat.upgrade.OLATUpgrade#doPostSystemInitUpgrade(org.olat.upgrade.UpgradeManager)
	 */
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else {
			if (uhd.isInstallationComplete()) return false;
		}
		
		// check all .htm and .html files in the coursefolder of all courses, and replace occurences of the form
		// "(../)*olatcmd/gotonode/<number>" (mostly used in the href attribute of an a tag
		// with
		// "javascript:parent.gotonode(<number>)"
		// Reason: The old links do not work anymore if the user detaches a singlepage into a new browser window 
		replaceOldInternalCourseLinksWithNewForm(upgradeManager, uhd);
		
		
		//
		uhd.setInstallationComplete(true);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		return true;
	}

	private void replaceOldInternalCourseLinksWithNewForm(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
	
		if (!uhd.getBooleanDataValue(TASK_REPLACE_OLDINTERNALLINKS)) {
			log.audit("+---------- replace internal links -----------+");
			final String DEFAULT_ENCODING = "iso-8859-1";
			final String MACINTOSH_ENCODING = "macintosh";
			final String MACROMAN_ENCODING = "macroman";
			final Pattern PATTERN_ENCTYPE = Pattern.compile("<meta.*?charset=([^\"]*)\"", Pattern.CASE_INSENSITIVE);
			final Pattern OLDLINK = Pattern.compile("(\\.\\./)*olatcmd/gotonode/(\\d*)");
			// \olat4head\bcroot\course\72623873096960\coursefolder\**
			// bcroot            course subfolders
			// -> accept all files in the coursefolder and subfolders with .htm or .html suffix 
			String bcrootPath = FolderConfig.getCanonicalRoot();
			File bcRootDir = new File(bcrootPath);
			File coursesRoot = new File(bcRootDir, "course");
			String[] courses = coursesRoot.list();
			for (int i = 0; i < courses.length; i++) {
				String course = courses[i];
				File courseRoot = new File(coursesRoot, course);
				File cFolder = new File(courseRoot, "coursefolder");
				log.audit("visiting "+cFolder.getAbsolutePath());
				FileVisitor fv = new FileVisitor() {

					public void visit(File file) {
						String rpage = null;
						String fname = file.getName();
						if ((file.isFile())&&(fname.endsWith(".htm") || fname.endsWith(".html"))) {
							// we can have html in all possible encodings
							String page = FileUtils.load(file, DEFAULT_ENCODING);
							// search for the <meta content="text/html; charset=utf-8"
							// http-equiv="Content-Type" /> tag
							// if none found, assume iso-8859-1
							String enc = DEFAULT_ENCODING;
							boolean useLoaded = false;
							// <meta.*charset=([^"]*)"
							Matcher m = PATTERN_ENCTYPE.matcher(page);
							boolean found = m.find();
							if (found) {
								// found an encoding definition
								String htmlcharset = m.group(1);
								enc = htmlcharset.toLowerCase();
								if (enc.equals(DEFAULT_ENCODING)) {
									// the found encoding is default encoding
									useLoaded = true;
								}else if(enc.equals(MACINTOSH_ENCODING)){
									enc = MACROMAN_ENCODING;
								}
							} else { 
								useLoaded = true;
							}
							
							if (useLoaded) {
								rpage = page;
							} else { // another encoding than default, have to reload the html-file using the founded encoding
								try {
									rpage = FileUtils.load(file, enc);
								} catch (Exception e) {
									log.audit("ERROR: could not load file "+file.getAbsolutePath()+" ENCTYPE: "+ enc);
									return;
								}
							}
							// (../)* "olatcmd/gotonode/<number>"
							// with
							// "javascript:parent.gotonode(<number>)"
							//rpage = "one <a href=\"olatcmd/gotonode/12345\">af</a> and two <a href=\"../../olatcmd/gotonode/6789\">af2</a> and...";
							Matcher ma = OLDLINK.matcher(rpage);
							StringBuffer sb = new StringBuffer();
							int changed = 0;
							StringBuilder links = new StringBuilder();
							while (ma.find()) {
								//int gp = ma.groupCount();
								String mm = ma.group(0);
								String id = ma.group(2);
								links.append(mm).append("="+id+";");
								ma.appendReplacement(sb, "javascript:parent.gotonode($2)");
								changed++;
							}
							if (changed > 0) {
								ma.appendTail(sb);
								String repl = sb.toString();
								FileUtils.save(file, repl, enc);
								log.audit("file "+file.getAbsolutePath()+" :: "+changed+" links changed :"+links);
							}
						}
					}
				};
				
				FileUtils.visitRecursively(cFolder, fv);
			}
			
			uhd.setBooleanDataValue(TASK_REPLACE_OLDINTERNALLINKS, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}
	
	public String getVersion() {
		return VERSION;
	}
	
	/**
	 * 
	 * @see org.olat.upgrade.OLATUpgrade#getAlterDbStatements()
	 */
	public String getAlterDbStatements() {
		return null; //till 6.1 was manual upgrade
	}

}
