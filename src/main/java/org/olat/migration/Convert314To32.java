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

package org.olat.migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.olat.core.util.FileUtils;
import org.olat.core.util.FileVisitor;
import org.olat.modules.cp.CPOfflineReadableManager;

/**
 * Initial Date: Dec 22, 2004
 * @author Alexander Schneider
 */
public class Convert314To32 {
	private String fromEnc = "iso-8859-1";
	private String toEnc = "utf-8";

	/**
	 * @param pathToCPOfflineMatDir
	 * @param pathToRepository
	 */

	public void addMenuToZip(String pathToCPOfflineMatDir, String pathToRepository) {
		final File cpOfflineMatDir = new File(pathToCPOfflineMatDir);
		File pathToRepositoryDir = new File(pathToRepository);

		FileUtils.visitRecursively(pathToRepositoryDir, new FileVisitor() {
			private CPOfflineReadableManager corm = CPOfflineReadableManager.getInstance();

			public void visit(File file) {
				if (file.getName().equals("imsmanifest.xml")) {
					File unzippedDir = file.getParentFile();
					System.out.println("> >> >>>\nimsmanifest.xml found in: \n" + unzippedDir.getPath());
					File cpDir = unzippedDir.getParentFile();
					File[] files = cpDir.listFiles();
					int foundZip = 0;
					for (int i = 0; i < files.length; i++) {
						if (files[i].isFile() && files[i].getName().toLowerCase().endsWith(".zip")) {
							File targetZip = files[i];
							System.out.println("zip-file found in: " + targetZip.getPath());
							foundZip++;
							if (foundZip == 1) corm.makeCPOfflineReadable(unzippedDir, targetZip, cpOfflineMatDir);
						}
					}
					if (foundZip != 1) System.out.println("ERROR: no or more than one zip-file found in: " + cpDir.getPath());

				}
			}
		});
	}

	/**
	 * @param args
	 */
	public static void main(String args[]) {
		if (args.length == 3) {
			String pathToCPOfflineMatDir = args[0];
			String pathToRepositoryHome = args[1];
			String pathToCourseHome = args[2];
			System.out.println("'"+pathToCPOfflineMatDir+"'\n'"+pathToRepositoryHome+"'\n'"+pathToCourseHome+"'\n\nis this ok (type 'yes')");
			if (readOneLineFromConsole().equals("yes")) {			
				Convert314To32 conv = new Convert314To32();
				System.out.println("Adding offline files to content packaging");
				conv.addMenuToZip(pathToCPOfflineMatDir, pathToRepositoryHome);
				System.out.println("\n\n\nAdding header to internal course xml files");
				conv.convertRunAndEditorStructureToUtf8(pathToCourseHome);
			}
			else {
				System.out.println("cancelled");
			}
		} else {
			System.out.println("usage:\n1. param: pathToCPOfflineMatDir like maybe: C:/eclipsedata/olat3/webapp/static/cp_offline_menu_mat");
			System.out.println("2. param: pathToRepositoryHome, e.g. C:/olatdata3/bcroot/repository");
			System.out.println("3. param: pathToCourseHome, e.g. C:/olatdata3/bcroot/course");
		}
	}

	/**
	 * 
	 */
	private void convertRunAndEditorStructureToUtf8(String pathToCourseHome) {
		final File courseHome = new File(pathToCourseHome);
		FileUtils.visitRecursively(courseHome, new FileVisitor() {
			public void visit(File file) {
				//System.out.println("visiting file " + file.getAbsolutePath());
				String fName = file.getName();
				// all files named editortreemodel.xml and runstructure.xml in a
				// subfolder of the course home
				if ((fName.equals("editortreemodel.xml") || fName.equals("runstructure.xml"))
						&& file.getParentFile().getParentFile().equals(courseHome)) {
					System.out.println("converting " + file.getAbsolutePath());
					String inh = FileUtils.load(file, fromEnc);
					if (inh.startsWith("<?xml")) { // encoding given, convert to utf-8
						// xml file -> adjust the encoding
						// e.g. <?xml version="1.0" encoding="utf-8"?>
						int pos = inh.indexOf('>');
						if (pos == -1) throw new RuntimeException("error: wrong xml header");
						String rest = inh.substring(pos + 1);
						inh = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + rest;
					} else { // no encoding yet, prepend xml header
						inh = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + inh;
					}
					// write inh back
					FileUtils.save(file, inh, toEnc);
				}
			}
		});
		// TODO Auto-generated method stub

	}
	
	private static String readOneLineFromConsole() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line = null;
		try {
			line = br.readLine();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			throw new RuntimeException("");
		}
		return line;
	}

}