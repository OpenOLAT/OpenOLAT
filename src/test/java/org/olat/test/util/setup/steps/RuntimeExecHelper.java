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
*/
package org.olat.test.util.setup.steps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class RuntimeExecHelper {

	private static final long DEFAULT_MAX_TIMEOUT_IN_MILLIS = 10*60*1000; // 10 min
	
	static class ReaderRunnable implements Runnable {

		private final BufferedReader reader_;
		private final String prefix_;
		private final Thread thread_;
		private transient boolean stop_ = false;

		public ReaderRunnable(String prefix, InputStream in) {
			if (prefix==null) {
				throw new IllegalArgumentException("prefix must not be null");
			}
			if (in==null) {
				throw new IllegalArgumentException("in must not be null");
			}
			reader_ = new BufferedReader(new InputStreamReader(in));
			prefix_ = prefix;
			thread_ = new Thread(this);
			thread_.setDaemon(true);
			thread_.start();
		}
		
		public void run() {
			try {
				while(true) {
					synchronized(this) {
						if (stop_) {
							return;
						}
					}
					String line = reader_.readLine();
					if (line==null) {
						return;
					}
					System.out.println(prefix_+line);
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			} finally {
				if (reader_!=null) {
					try {
						reader_.close();
					} catch (IOException e) {
						// silence in the library
					}
				}
			}
		}
		
		public void close() {
			synchronized(this) {
				stop_ = true;
			}
			if (reader_!=null) {
				try {
					reader_.close();
				} catch (IOException e) {
					// silence in the library
				}
			}
		}
		
	}
	
	static class WaitForWithTimeout implements Runnable {

		private final Process p_;
		private Thread thread_ = null;
		private boolean success_ = false;
		private int result_;
		
		public WaitForWithTimeout(Process p) {
			if (p==null) {
				throw new IllegalArgumentException("p must not be null");
			}
			p_ = p;
		}
		
		public int waitFor(long timeout) throws TimeoutException {
			if (thread_!=null) {
				throw new IllegalStateException("already called waitFor");
			}
			thread_ = new Thread(this);
			thread_.setDaemon(true);
			thread_.start();
			final long end = System.currentTimeMillis() + timeout;
			synchronized(this) {
				while(!success_) {
					long diff = end - System.currentTimeMillis();
					if (diff<=0) {
						break;
					} else {
						try {
							wait(diff);
						} catch (InterruptedException e) {
							// ignore
						}
					}
				}
				if (success_) {
					return result_;
				} else {
					thread_.interrupt();
					throw new TimeoutException();
				}
			}
		}
		
		public void run() {
			try {
				int result = p_.waitFor();
				synchronized(this) {
					result_ = result;
					success_ = true;
					notifyAll();
				}
			} catch (InterruptedException e) {
				// ignore
			}
		}
		
	}

	public static String execWithStdOut(String cmd) throws Exception {
		System.out.println("Executing the following command: "+cmd);
		Process process = Runtime.getRuntime().exec(cmd);
		
		ReaderRunnable outReader = new ReaderRunnable("[STDOUT:] ", process.getInputStream());
		ReaderRunnable errReader = new ReaderRunnable("[STDERR:] ", process.getErrorStream());
		try{
			int errorCode = new WaitForWithTimeout(process).waitFor(DEFAULT_MAX_TIMEOUT_IN_MILLIS);
			
			BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuffer sb = new StringBuffer();
			while(true) {
				String stdOutLine = stdOutReader.readLine();
				if (stdOutLine==null) {
					break;
				}
				if (sb.length()!=0) {
					sb.append(System.getProperty("line.separator"));
				}
				sb.append(stdOutLine);
				System.out.println("[STDOUT:] "+stdOutLine);
			}
			BufferedReader stdErrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			while(true) {
				String stdErrLine = stdErrReader.readLine();
				if (stdErrLine==null) {
					break;
				}
				System.out.println("[STDERR:] "+stdErrLine);
			}
			
			if (errorCode!=0) {
				System.out.println("Exec of '"+cmd+"' returned errorcode: "+errorCode);
				throw new AssertionError("cmd failed. errorcode="+errorCode+", cmd="+cmd);
			} else {
				return sb.toString();
			}
		} finally {
			outReader.close();
			errReader.close();
			System.out.println("Done with execution of command: "+cmd);
		}
	}
	
	public static void exec(String cmd, boolean failOnError) throws Exception {
		int errorCode = exec(cmd);
		System.out.println("Exec of '"+cmd+"' returned errorcode: "+errorCode);
		if (errorCode!=0 && failOnError) {
			throw new AssertionError("cmd failed. errorcode="+errorCode+", cmd="+cmd);
		}
	}
	
	public static int exec(String cmd) throws Exception {
		System.out.println("Executing the following command: "+cmd);
		Process process = Runtime.getRuntime().exec(cmd);
		ReaderRunnable outReader = new ReaderRunnable("[STDOUT:] ", process.getInputStream());
		ReaderRunnable errReader = new ReaderRunnable("[STDERR:] ", process.getErrorStream());
		try{
			return new WaitForWithTimeout(process).waitFor(DEFAULT_MAX_TIMEOUT_IN_MILLIS);
		} finally {
			outReader.close();
			errReader.close();
			System.out.println("Done with execution of command: "+cmd);
		}
	}
}
