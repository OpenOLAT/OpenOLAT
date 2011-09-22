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

package ch.unizh.codeutils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.util.FileUtils;
import org.olat.modules.wiki.WikiManager;
import org.olat.modules.wiki.gui.components.wikiToHtml.FilterUtil;

/**
 * 
 * 
 * @author Christian Guretzki
 */
public class WikiMigration_5_1_0 {

	private static boolean testMode = false;
	private static boolean debugMode = false;
	private static Hashtable fileRenamingList ;
	
	public WikiMigration_5_1_0() {
		
	}
	
	public static void main(String[] args) {
		System.out.println("> WikiMigration_5_1_0  V1.0.6  19.02.2007");
		System.out.println("> -------------------");
		if (testMode) {
			doTest();
			System.exit(0);
		}
		// 1. Read Filelist
		if (args.length == 0) {
			System.err.println("Missing argument filename. java ch.unizh.codeutils.WikiMigration_5_1_0 inputFileName");
			System.err.println("  Options : -DEBUGMODE java ch.unizh.codeutils.WikiMigration_5_1_0 inputFileName -DEBUGMODE");
			System.exit(1);
		}
		// check for argumwnt debug Mode
		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("-DEBUGMODE") ) {
				debugMode = true;
			}
		}

		try {
			String inputFileName = args[0];
			RandomAccessFile inputFile = new RandomAccessFile(inputFileName, "r");
			// 2. Loop over all files
			String path = null;
			fileRenamingList = new Hashtable();
			
			while ( (path = inputFile.readLine()) != null) {
				testOut("process path=" + path);
				// get pagename from wiki.properties file
				String wikiPropertiesFileName = path.substring(0,path.length() - ".wp".length()) + ".properties";
				Properties wikiProperties = new Properties();
				wikiProperties.load(new FileInputStream(wikiPropertiesFileName));
				String pageName = wikiProperties.getProperty("pagename");
				log("migrate wiki with pagename=" + pageName, path);
				// 2.1. Read File Content
				FileInputStream fis = new FileInputStream(path);
				BufferedInputStream bis = new BufferedInputStream(fis); 
				String content = FileUtils.load(bis, "utf-8");
				String migratedContent = doMigrate(content, path);
				// 2.2 Write migrated Content to File
				FileUtils.save( new File(path),migratedContent , "utf-8");
			}
			// ok now all file-content is migrated, now we can rename wiki files => loop over all files in fileRenamingList
			logDebug("TEST: fileRenamingList.size()=" + fileRenamingList.size(), "");
		  Enumeration enumeration = fileRenamingList.keys();
		  while (enumeration.hasMoreElements()) {
				String oldWikiFileName = (String) enumeration.nextElement();
				String newWikiFileName = (String)fileRenamingList.get(oldWikiFileName);
  		  renameFile(oldWikiFileName, newWikiFileName);			
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String doMigrate(String content, String path) {
		testOut("Input content:" + content);
		testOut("---");

		// 2.2 Migrate content
		String migratedContentColon = doMigrateColon(content, path);
		testOut("migratedContentColon:" + migratedContentColon);
		testOut("---");
		String migratedContentImage = doMigrateImageTag(migratedContentColon, path);
		testOut("migratedContentImage:" + migratedContentImage);
		testOut("---");
		String migratedDoubleQuote  = doMigrateDoubleQuote(migratedContentImage, path);
		testOut("migratedDoubleQuote:" + migratedDoubleQuote);
		testOut("---");
		String migratedContent = doMigrateAmpersand(migratedDoubleQuote, path);
		testOut("Migrated content:" + migratedContent);
		testOut("---");
		String migratedQuestionMark = doMigrateQuestionmark(migratedContent, path);
		testOut("Migrated content:" + migratedQuestionMark);
		testOut("---");
		return migratedQuestionMark;
	}

	private static String doMigrateAmpersand(String content, String path) {
		Pattern WIKI_LINK_PATTERN = Pattern.compile("\\[\\[[^\\]]*[^\\]]*\\]\\]");
		Matcher m = WIKI_LINK_PATTERN.matcher(content);
		List links = new ArrayList();
	
		while(m.find()) {
		  String link = content.substring(m.start(), m.end());
		  if (!link.startsWith("[[Image:") && !link.startsWith("[[Media:") && 
		  		(link.indexOf("http://") == -1) && (link.indexOf('&') != -1) ) {
		  	testOut("Migrate ampersand in: "+link);
		    links.add(link); // no image, link has '&'
		  }
		}
		for (Iterator iter = links.iterator(); iter.hasNext();) {
		  String link = (String) iter.next();
		  if (link.indexOf("&amp;") == -1) {
		    String newWikiLink = link.replace("&","&amp;");
		    content = content.replace(link, newWikiLink);
		    log("Replace '&' in wiki link old='" + link + "' new='" + newWikiLink + "'", path);
		  } else {
		  	log("Replace '&' : link=" + link + "  has already '&amp;' !", path);
		  }
		}
		return content;
	}

	private static String doMigrateColon(String content, String path) {
		Pattern WIKI_LINK_PATTERN = Pattern.compile("\\[\\[[^\\]]*[^\\]]*\\]\\]");
		Matcher m = WIKI_LINK_PATTERN.matcher(content);
		List links = new ArrayList();
	
		while(m.find()) {
		  String link = content.substring(m.start(), m.end());
		  if (!link.startsWith("[[Image:") && !link.startsWith("[[Media:") && 
		  		(link.indexOf("http://") == -1) && (link.indexOf(':') != -1) ) {
		  	testOut("Migrate colon in: "+link);
		    links.add(link); // no image, link has ':'
		  }
		}
		for (Iterator iter = links.iterator(); iter.hasNext();) {
		  String link = (String) iter.next();
		  String newWikiLink = link.replace(':','/');
		  content = content.replace(link, newWikiLink);
		  log("Replace ':'  in wiki link old='" + link + "' new='" + newWikiLink + "'", path);
		}
		return content;
	}

	/**
	 * New Wiki parser default alignment for images is left. In version 5.0 is was per default right.
	 * Search for all [[Image:xxxx.yyy]] and migrate this to [[Image:xxxx.yyy|right]]
	 * @param content
	 * @return migrated content
	 */
	private static String doMigrateImageTag(String content, String path) {
		Pattern WIKI_LINK_PATTERN = Pattern.compile("\\[\\[[^\\]]*Image:[^\\]]*\\]\\]");
		Matcher m = WIKI_LINK_PATTERN.matcher(content);
		List links = new ArrayList();
	
		while(m.find()) {
		  String link = content.substring(m.start(), m.end());
		  if (!link.endsWith("|right]]") && !link.endsWith("|left]]") && !link.endsWith("|center]]") )  {
		    testOut("ImageTag to be replaced: "+link);
		    links.add(link);
		  }
		}
		for (Iterator iter = links.iterator(); iter.hasNext();) {
		  String link = (String) iter.next();
		  String newWikiLink = link.substring(0, link.length()-2)+"|right]]";
		  content = content.replace(link, newWikiLink);
		  log("Append '|right' in wiki link old='" + link + "' new='" + newWikiLink + "'", path);
		}
		return content;
	} 
	
	private static String doMigrateDoubleQuote(String content, String path) {
		Pattern WIKI_LINK_PATTERN = Pattern.compile("\\[\\[[^\\]]*[^\\]]*\\]\\]");
		
		Matcher m = WIKI_LINK_PATTERN.matcher(content);
	
		List links = new ArrayList();
	
		while(m.find()) {
		  String link = content.substring(m.start(), m.end());
		  if (!link.startsWith("[[Image:") && !link.startsWith("[[Media:") && (link.indexOf('"') != -1) ) {
		  	testOut("Migrate DoubleQuote in: "+link);
        // no image and link with doubleQuote
		  	// check for '|' => [[link|text]] extract link 
		  	if (link.indexOf('|') != -1) {
		  		link = link.substring("[[".length(),link.indexOf('|'));
		  		if (link.indexOf('"') != -1) {
		  			links.add(link);// doubleQuote in link
		  		} else {
		  			// doubleQuote in text => do not add link
		  		}
		  	} else {
		      links.add(link); // no '|' => doubleQuote in link
		  	}
		  }
		}
		for (Iterator iter = links.iterator(); iter.hasNext();) {
		  String link = (String) iter.next(); // link = [[Name]]
		  
		  String newWikiWord = link.replace('"','\'');		  
		  content = content.replace(link, newWikiWord );
		  log("Replace '\"' in wiki link old='" + link + "' new='" + newWikiWord + "'", path);

		  // Replace " with '&quot;' to generate wiki filename
		  link = link.replaceAll("\"", "&quot;");
		  String oldWikiFileName = generateWikiFileName(link);
		  String oldWikiPropertiesFileName = generateWikiPropertiesFileName(link);
		  String newWikiFileName = generateWikiFileName(newWikiWord);
		  String newWikiPropertiesFileName = generateWikiPropertiesFileName(newWikiWord);
		  log("Old Wiki word='" + link + "' new Wiki word='" + newWikiWord + "'", path);
		  String dirPath = path.substring(0,path.lastIndexOf("/"));
		  renamePageNameInPropertiesFile(dirPath + File.separator + oldWikiPropertiesFileName, newWikiWord);
		  fileRenamingList.put(dirPath + File.separator + oldWikiFileName, dirPath + File.separator + newWikiFileName);
		  fileRenamingList.put(dirPath + File.separator + oldWikiPropertiesFileName, dirPath + File.separator + newWikiPropertiesFileName);
		  logDebug("fileRenamingList put key='" + dirPath + File.separator + oldWikiFileName + "' value='" + dirPath + File.separator + newWikiFileName + "'", "");
		}
		return content;
	}
	
	private static String doMigrateQuestionmark(String content, String path) {
		Pattern WIKI_LINK_PATTERN = Pattern.compile("\\[\\[[^\\]]*[^\\]]*\\]\\]");
		
		Matcher m = WIKI_LINK_PATTERN.matcher(content);
	
		List links = new ArrayList();
	
		while(m.find()) {
		  String link = content.substring(m.start(), m.end());
		  if (!link.startsWith("[[Image:") && !link.startsWith("[[Media:") && (link.indexOf("?") != -1) ) {
		  	testOut("Migrate Questionmark in: "+link);
        // no image and link with doubleQuote
		  	// check for '|' => [[link|text]] extract link 
		  	if (link.indexOf('|') != -1) {
		  		link = link.substring("[[".length(),link.indexOf('|'));
		  		if (link.indexOf("?") != -1) {
		  			links.add(link);// Questionmark in link
		  		} else {
		  			// Questionmark in text => do not add link
		  		}
		  	} else {
		      links.add(link); // no '|' => doubleQuote in link
		  	}
		  }
		}
		for (Iterator iter = links.iterator(); iter.hasNext();) {
		  String link = (String) iter.next(); // link = [[Name]]
		  
		  String newWikiWord = link.replace("?","");		  
		  content = content.replace(link, newWikiWord );
		  log("Replace '?' in wiki link old='" + link + "' new='" + newWikiWord + "'", path);

		  //link = link.replaceAll("\"", "&quot;");
		  String oldWikiFileName = generateWikiFileName(link);
		  String oldWikiPropertiesFileName = generateWikiPropertiesFileName(link);
		  String newWikiFileName = generateWikiFileName(newWikiWord);
		  String newWikiPropertiesFileName = generateWikiPropertiesFileName(newWikiWord);
		  log("Old Wiki word='" + link + "' new Wiki word='" + newWikiWord + "'", path);
		  String dirPath = path.substring(0,path.lastIndexOf("/"));
		  renamePageNameInPropertiesFile(dirPath + File.separator + oldWikiPropertiesFileName, newWikiWord);
		  fileRenamingList.put(dirPath + File.separator + oldWikiFileName, dirPath + File.separator + newWikiFileName);
		  fileRenamingList.put(dirPath + File.separator + oldWikiPropertiesFileName, dirPath + File.separator + newWikiPropertiesFileName);
		  logDebug("fileRenamingList put key='" + dirPath + File.separator + oldWikiFileName + "' value='" + dirPath + File.separator + newWikiFileName + "'", "");
		}
		return content;
	}
	

	private static void renamePageNameInPropertiesFile(String oldWikiPropertiesFileName, String newWikiWord) {
		Properties p = new Properties();
		try {
			FileInputStream fis = new FileInputStream(new File(oldWikiPropertiesFileName));
			p.load(fis);
			fis.close();
			p.setProperty(WikiManager.PAGENAME, removeLinkTags(newWikiWord) );
			logDebug("TEST.renamePageNameInPropertiesFile: oldWikiPropertiesFileName=" + oldWikiPropertiesFileName + "  newWikiWord=" + newWikiWord, "");
			FileOutputStream fos = new FileOutputStream(new File(oldWikiPropertiesFileName));
			p.store(fos, "wiki page meta properties");
			fos.close();
		} catch (IOException e) {
			log("WARN: Wiki properties couldn't be read! Pagename:" + oldWikiPropertiesFileName,"");
		}
	}

	private static void renameFile(String oldWikiFileName, String newWikiFileName) {
		log("RenameFile oldWikiFileName='" + oldWikiFileName + "'  newWikiFileName='" + newWikiFileName + "'", "");
		File existingWikiFile = new File(oldWikiFileName);
		if (existingWikiFile.exists()) {
			File renamedWikiFile = new File(newWikiFileName);
			if (renamedWikiFile.exists()) {
			  log("WARN: New Wiki File already exists; Rename wiki file from '" + oldWikiFileName + "' to '" + newWikiFileName + "'", "");
			}
		  existingWikiFile.renameTo(renamedWikiFile);
		} else {
			log("File oldWikiFileName='" + oldWikiFileName + "' does not exit", "");
		}
	}
	
	private static String generateWikiFileName(String wikiLink) {
		return generatePageId(wikiLink) + ".wp";
	}

	private static String generateWikiPropertiesFileName(String wikiLink) {
		return generatePageId(wikiLink) + ".properties";
	}

	private static String generatePageId(String wikiLink) {
		String wikiWord = removeLinkTags(wikiLink);
		String pageId = WikiManager.generatePageId(FilterUtil.normalizeWikiLink(wikiWord));
		logDebug("TEST.generatePageId wikiWord='" + wikiWord + "'  pageId=" + pageId, "");
		return pageId;
	}

	private static void logDebug(String message, String path) {
		if (debugMode) {
			log(message, path);
		}
	}

	private static String removeLinkTags(String wikiLink) {
		if (wikiLink.startsWith("[[")) {
			wikiLink = wikiLink.substring("[[".length(), wikiLink.length());
		}
		if (wikiLink.endsWith("]]")) {
			wikiLink = wikiLink.substring(0,  wikiLink.length()-"]]".length());
		}
    return wikiLink;
	}
  
	private static void testOut(String output) {
		if (testMode) {
			System.out.println(">"+output);
		}
	}

	private static void log(String output, String path) {
		System.out.println(path + ":" + output);
	}

	private static void doTest() {
		String content = "In diesem Winter hat es [[Davos]] und [[Unter:Engadin:Sent]] wenig Schnee [[Image:snow.jpg]] aber ev." +
	  " kommt er noch\n[[Image:blabla_bli.jpg]]. Weiter Wiki Woerter mit [[\"Doppelten\"Anfuehrungszeichen]] und so fort [[\"Zitat\"]]" +
		" und so fort [[StandardWort]]";
		
		String migratedContent = doMigrate(content, "test");
		
	}

}
