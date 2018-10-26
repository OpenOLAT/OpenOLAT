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
package org.olat.search.service.document.file.pdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.search.SearchModule;
import org.olat.search.service.document.file.DocumentAccessException;

/**
 * The extractor call an extern process: command pdf txt
 * 
 * 
 * Initial date: 19.07.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PdfExternalExtractor implements PdfExtractor {
	
	private static final OLog log = Tracing.createLoggerFor(PdfExternalExtractor.class);
	
	private SearchModule searchModule;

	/**
	 * [used by Spring]
	 * @param searchModule
	 */
	public void setSearchModule(SearchModule searchModule) {
		this.searchModule = searchModule;
	}

	@Override
	public void extract(VFSLeaf document, File bufferFile)
	throws IOException, DocumentAccessException {
		if(!(document instanceof LocalFileImpl)) {
			log.warn("Can only index local file");
			return;
		}
		
		List<String> cmds = new ArrayList<>();
		cmds.add(searchModule.getPdfExternalIndexerCmd());
		cmds.add(((LocalFileImpl)document).getBasefile().getAbsolutePath());
		cmds.add(bufferFile.getAbsolutePath());

		CountDownLatch doneSignal = new CountDownLatch(1);

		ProcessWorker worker = new ProcessWorker(cmds, doneSignal);
		worker.start();

		try {
			doneSignal.await(3000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			log.error("", e);
		}
		
		worker.destroyProcess();
	}

	private final void executeProcess(Process proc) {
		StringBuilder errors = new StringBuilder();
		StringBuilder output = new StringBuilder();

		InputStream stderr = proc.getErrorStream();
		InputStreamReader iserr = new InputStreamReader(stderr);
		BufferedReader berr = new BufferedReader(iserr);
		String line = null;
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

		try {
			int exitValue = proc.waitFor();
			if(log.isDebug()) {
				log.info("PDF extracted: " + exitValue);
			}
		} catch (InterruptedException e) {
			//
		}
		
		if(log.isDebug()) {
			log.error(errors.toString());
			log.info(output.toString());
		}
	}

	private class ProcessWorker extends Thread {
		
		private volatile Process process;

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
	}
}
