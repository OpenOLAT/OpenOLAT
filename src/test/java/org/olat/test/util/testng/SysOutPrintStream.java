package org.olat.test.util.testng;

import java.io.OutputStream;
import java.io.PrintStream;

public class SysOutPrintStream extends PrintStream {

	private final PrintStream originalSysout_;

	public SysOutPrintStream(OutputStream out, PrintStream originalSysout) {
		super(out);
		originalSysout_ = originalSysout;
	}
	
	protected PrintStream getOriginalSysOut() {
		return originalSysout_;
	}

	@Override
	public String toString() {
		return "a SysOutPrintStream[origSysOut="+originalSysout_+"]";
	}
}
