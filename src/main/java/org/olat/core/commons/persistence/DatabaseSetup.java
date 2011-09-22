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

package org.olat.core.commons.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.MappingException;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.core.io.Resource;

/**
 * Initial Date:  25.10.2002
 * @author Florian Gnaegi
 *
 * Comment: Use this class to create the initial OLAT database
 *
 */
public class DatabaseSetup {
	private static final OLog log = Tracing.createLoggerFor(DatabaseSetup.class);

	private static Configuration cf;	
	/**
	 * Method datastoreConfiguration is the central mapping definition of all OLAT
	 * objects and their mapping to a relational database. 
	 * 
	 * When a new mapping is accomplished, it has to be added here.
	 * 
	 * @return Datastore
	 * 
	 * @throws MappingException
	 */
	public static Configuration datastoreConfiguration() throws MappingException {
		cf = new Configuration();
		List<String> filesAlreadyAdded = new ArrayList<String>();
		List<String> ignoredFiles = new ArrayList<String>();
		
		//TODO
		//loading hbm files out of a jar needs an start root directory. So change it to:
		//CoreSpringFactory.getResources("classpath*:org/**/*.hbm.xml");
		//If you do the fix above you also need to ensure that no *hbm.xml files are
		//present both in .jars and in WEB-INF/classes, otherwise the startup process
		//will fail.
		
		ignoredFiles.add("LoggingObject.hbm.xml");
		ignoredFiles.add("UserCommentImpl.hbm.xml");
		ignoredFiles.add("UserRatingImpl.hbm.xml");
		addHibernateFilesMatching("classpath*:**/*.hbm.xml", filesAlreadyAdded, ignoredFiles);
		
		//OLAT-4109 : the LoggingAction.hbm.xml file is in the core jar and is not found
		//            since it's not in the classes directory but in the jar.
		//            as documented above, when you want to have hbm.xml files
		//            found by the CoreSpringFactory.getResources() method
		//            you can't just specify "classpath*:**/*.hbm.xml"
		//            but need to specify at least the first path segment,
		//            i.e. it will work when you do this: "classpath*:org/**/*.hbm.xml"
		//            Now the problem with this is that it will find too
		//            many - i.e. it will find duplicates if you have some in a jar
		//            and some in the classpath
		//            To fix this we now just restrict it to loggingObject -
		//            plus adding the ignoredFiles "trick"
		//TODO
		//            Might be an idea to generalize this whole way we deal with this
		//            i.e. we have the core jar which can contain any htm.xml or spring.xml files
		//            so for production we want to add load them here - but for
		//            development we have the core java directory mounted so we have
		//            under classes as well - hence the whole problem
		addHibernateFilesMatching("classpath*:org/**/LoggingObject.hbm.xml", filesAlreadyAdded, new ArrayList<String>());
		addHibernateFilesMatching("classpath*:org/**/UserCommentImpl.hbm.xml", filesAlreadyAdded, new ArrayList<String>());
		addHibernateFilesMatching("classpath*:org/**/UserRatingImpl.hbm.xml", filesAlreadyAdded, new ArrayList<String>());
		
		return cf;
	}


	private static void addHibernateFilesMatching(final String resourcePath, List<String> filesAlreadyAdded, List<String> ignoredFiles) {
		Resource[] ress = CoreSpringFactory.getResources(resourcePath);
		for (int i = 0; i < ress.length; i++) {
			Resource res = ress[i];
			InputStream is;
			String fileName = res.getFilename();
			if (ignoredFiles.contains(fileName)) {
				// then ignore it - dont log either
				continue;
			}
			if (!filesAlreadyAdded.contains(fileName)) {
				filesAlreadyAdded.add(fileName);
				try {
					log.info("Start adding hibernate mapping (xml mapping stream): "+ res.getDescription());
					is = res.getInputStream();
					cf.addInputStream(is);
					log.info("Loaded hibernate mapping (xml mapping stream): "+ res.getDescription());
				} catch (IOException e) {
					throw new AssertException("i/o error while getting inputstream of resource:"+ res.getDescription());
				}
			} else {
				log.warn("Douplicate hibernate mapping file::" + fileName+ " found on classpath, skipping " + res.toString());
			}
		}
	}


	/**
	 * Execute commands from the command line:
	 * 'setup' or 'drop' currently available 
	 * @param args One of createTables, dropTables, toFile, updateDDL
	 * @throws Exception
	 * e.g. DatabaseSetup org.hibernate.dialect.MySQLDialect createScript
	 */
	public static void main( String[] args ) throws Exception {
		if (args.length == 2) {
			cf = datastoreConfiguration();
			cf.setProperty("hibernate.dialect", args[0]);
			if (args[1].equals("createTables"))	createTables();
			else if (args[1].equals("dropTables")) dropTables();
			else if (args[1].equals("createScript")) exportDDLtoFile();
			else if (args[1].equals("updateDDL")) updateDatabaseDDL();
		}	else { // write to file as default see database/setupDatabase.sql
			System.out.println("Usage: DatabaseSetup DIALECT ACTION\nwhere ACTION is one of: createTables | dropTables | createScript | updateDLL");
		}
	}

	/**
	 * Setup OLAT database. This will drop all tables first if available.
	 * @throws Exception
	 */
	public static void createTables() throws Exception {
		log.info("Creating tables");
		new SchemaExport(cf).create(false, true); // set first bolean to true for debugging.
	}

	/**
	 * Drop all OLAT tables
	 * @throws Exception
	 */
	public static void dropTables() throws Exception {
		log.info("Dropping tables");
		new SchemaExport(cf).drop(false, true);
	}

	/**
	 * Generates alter database DDL and EXECUTES it.
	 */
	private static void updateDatabaseDDL() {
		boolean printToOut = true;  // write to System.out 
		boolean updateDatabase = false;
		try {
			new SchemaUpdate(cf).execute(printToOut, updateDatabase);
		} catch (Exception e) {
			log.error("DDL export to file failed: Reason: ", e);
		}		
	}

	/**
	 * Write database configuration to file.
	 * Includes differences to existing database.
	 * Filename: "database/setupDatabase.sql"
	 */
	private static void exportDDLtoFile() {
		String outputFile = "database/setupDatabase.sql";

		boolean script = true;  // write DDL 
		boolean export = false; // don't update databse		
		try {
			SchemaExport se = new SchemaExport(cf);
			se.setOutputFile(outputFile);
			se.setDelimiter(";");
			se.create(script, export);
		} catch (Exception e) {
			log.error("DDL export to file failed: Reason: ", e);
		}
	}
}
