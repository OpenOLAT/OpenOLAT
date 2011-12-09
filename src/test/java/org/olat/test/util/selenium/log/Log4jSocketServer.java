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
package org.olat.test.util.selenium.log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.net.SocketNode;

public class Log4jSocketServer {
	
	static {
    	System.out.println("==============================");
    	System.out.println("Log4jSocketServer<static-init>");
    	System.out.println("==============================");    	
	}

	static int port = 18081;
	private static Thread thread_;
	private static ServerSocket serverSocket_;
	private static Object syncObj_ = new Object();
	private static boolean stop_ = false;
	
  /**
   * 
   * @param log4JConfigFilename
   */
	public static void setLog4JConfigFilename(String log4JConfigFilename) {		
		System.out.println("Log4jSocketServer - setLog4JConfigFilename: " + log4JConfigFilename);
		PropertyConfigurator.configure(log4JConfigFilename);
	}

	public static boolean isStarted() {
		return thread_!=null;
	}
	
	/**
	 * Call setLog4JConfigFilename before!!!
	 */
	public static void start() {
		if (thread_!=null) {
	    	System.out.println("=========================");
	    	System.out.println("Log4jSocketServer.start(): already active");
	    	System.out.println("========================");
	    	stop();
//			throw new IllegalArgumentException("Log4jSocketServer already started (make sure to call super.tearDown() in your tests if you overwrite tearDown())");
		}
    	System.out.println("=========================");
    	System.out.println("Log4jSocketServer.start()");
    	System.out.println("=========================");
    	stop_ = false;
    	serverSocket_ = null;
		thread_ = new Thread(new Runnable() {

			private List<Socket> socketNodeSockets_ = new LinkedList<Socket>();
			
			public void run() {
				try {
					System.out.println("[Log4jSocketServer] Listening on port " + port);
					//System.setProperty("log4j.debug", "true");
					serverSocket_ = new ServerSocket(port);
					while (true) {
						synchronized (syncObj_) {
							if (stop_) {
								return;
							}
						}
						System.out.println("[Log4jSocketServer] Waiting to accept a new client.");
						Socket socket = serverSocket_.accept();
						System.out.println("[Log4jSocketServer] Connected to client at "
								+ socket.getInetAddress());
						System.out.println("[Log4jSocketServer] Starting new socket node.");
						SocketNode socketNode = new SocketNode(socket, LogManager
								.getLoggerRepository());
						Thread thread = new Thread(socketNode, "SimpleSocketServer-"
								+ port);
						thread.setDaemon(true);
						socketNodeSockets_.add(socket);
						thread.start();
					}
				} catch (Exception e) {
					synchronized (syncObj_) {
						if (stop_) {
							return;
						}
					}
					e.printStackTrace();
				} finally {
					System.out.println("[Log4jSocketServer] Cleaning up Log4jSocketServer mess... ("+socketNodeSockets_.size()+" sockets)");
					thread_ = null;
					for (Iterator<Socket> it = socketNodeSockets_.iterator(); it
							.hasNext();) {
						Socket socket = it.next();
						try {
							System.out.println("[Log4jSocketServer] Shutting down socket "+socket+"...");
							socket.shutdownInput();
							socket.shutdownOutput();
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		thread_.setDaemon(true);
		thread_.start();
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void stop() {
		if (serverSocket_ == null) {
	    	System.out.println("========================");
	    	System.out.println("Log4jSocketServer.stop(): not active");
	    	System.out.println("========================");
			return;
		}
    	System.out.println("========================");
    	System.out.println("Log4jSocketServer.stop()");
    	System.out.println("========================");
		synchronized(syncObj_) {
			stop_ = true;
		}
		try {
			serverSocket_.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
