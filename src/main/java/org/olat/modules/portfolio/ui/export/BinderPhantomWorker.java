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
package org.olat.modules.portfolio.ui.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 16.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderPhantomWorker {
	
	private static final OLog log = Tracing.createLoggerFor(BinderPhantomWorker.class);

	public BinderPhantomWorker() {
		//
	}

	public File fill(File indexHtml, File destinationDir, String filename) {
		try {
			File outputFile = new File(destinationDir, filename);
			//deploy script
			File rasterizePath = new File(destinationDir, "rasterize.js");
			try(InputStream inRasteriez = BinderPhantomWorker.class.getResourceAsStream("rasterize.js")) {
				Files.copy(inRasteriez, rasterizePath.toPath(), StandardCopyOption.REPLACE_EXISTING);	
			} catch(Exception e) {
				log.error("Can not read rasterize.js library for PhantomJS PDF generation", e);
			}
			
			List<String> cmds = new ArrayList<String>();
			cmds.add("phantomjs");
			cmds.add(rasterizePath.getAbsolutePath());
			cmds.add(indexHtml.getAbsolutePath());
			cmds.add(outputFile.getAbsolutePath());
			
			CountDownLatch doneSignal = new CountDownLatch(1);
			ProcessWorker worker = new ProcessWorker(cmds, doneSignal);
			worker.start();

			try {
				doneSignal.await(30000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				log.error("", e);
			}
			
			worker.destroyProcess();
			return outputFile;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

	private static class ProcessWorker extends Thread {
		
		private volatile Process process;

		private int exitValue = -1;
		private final List<String> cmd;
		private final CountDownLatch doneSignal;
		
		public ProcessWorker(List<String> cmd, CountDownLatch doneSignal) {
			this.cmd = cmd;
			this.doneSignal = doneSignal;
		}
		
		public void destroyProcess() {
			if (process != null) {
				process.destroy();
				process = null;
			}
		}

		@Override
		public void run() {
			try {
				if(log.isDebug()) {
					log.debug(cmd.toString());
				}
				
				ProcessBuilder builder = new ProcessBuilder(cmd);
				process = builder.start();
				executeProcess(process);
				doneSignal.countDown();
			} catch (IOException e) {
				log.error ("Could not spawn convert sub process", e);
				destroyProcess();
			}
		}
		
		private final void executeProcess(Process proc) {
			StringBuilder errors = new StringBuilder();
			StringBuilder output = new StringBuilder();
			String line;

			InputStream stderr = proc.getErrorStream();
			InputStreamReader iserr = new InputStreamReader(stderr);
			BufferedReader berr = new BufferedReader(iserr);
			line = null;
			try {
				while ((line = berr.readLine()) != null) {
					errors.append(line);
				}
			} catch (IOException e) {
				//
			}
			
			InputStream stdout = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(stdout);
			BufferedReader br = new BufferedReader(isr);
			line = null;
			try {
				while ((line = br.readLine()) != null) {
					output.append(line);
				}
			} catch (IOException e) {
				//
			}
			
			if(log.isDebug()) {
				log.debug("Error: " + errors.toString());
				log.debug("Output: " + output.toString());
			}

			try {
				exitValue = proc.waitFor();
				if (exitValue != 0) {
					log.warn("Problem with PhantomJS? " + exitValue);
				}
			} catch (InterruptedException e) {
				log.warn("Takes too long");
			}
		}
	}
}
