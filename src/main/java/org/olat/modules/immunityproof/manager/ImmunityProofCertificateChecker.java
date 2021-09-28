package org.olat.modules.immunityproof.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.modules.immunityproof.ImmunityProofContext;

public class ImmunityProofCertificateChecker extends Thread {

	private static final Logger log = Tracing.createLoggerFor(ImmunityProofCertificateChecker.class);

	private volatile Process process;
	private volatile ImmunityProofContext context;

	private final List<String> cmd;
	private final CountDownLatch doneSignal;

	public ImmunityProofCertificateChecker(ImmunityProofContext context, List<String> cmd, CountDownLatch doneSignal) {
		this.cmd = cmd;
		this.context = context;
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
			if (log.isDebugEnabled()) {
				log.debug(cmd.toString());
			}

			ProcessBuilder builder = new ProcessBuilder(cmd);
			process = builder.start();
			context = executeProcess(context, process);
			doneSignal.countDown();
		} catch (IOException e) {
			log.error("Could not spawn convert sub process", e);
			destroyProcess();
		}
	}

	private final ImmunityProofContext executeProcess(ImmunityProofContext context, Process proc) {

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

		if (log.isDebugEnabled()) {
			log.debug("Error: {}", errors.toString());
			log.debug("Output: {}", output.toString());
		}

		try {
			int exitValue = proc.waitFor();

			if (exitValue == 0) {
				if (output.length() == 0) {
					context.setCertificateFound(false);
				} else {
					context.setCertificateFound(true);
				}
			}
		} catch (InterruptedException e) {
			//
		}

		context.setOutput(output);
		context.setErrors(errors);

		return context;
	}

	public ImmunityProofContext getContext() {
		return context;
	}
}
