package vn.edu.hcmus.student.sv19127191.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * vn.edu.hcmus.student.sv19127191.server<br/>
 * Created by Ngo Van Anh Kiet - MSSV: 19127191<br/>
 * Date 8/1/2022 - 2:47 PM<br/>
 * Description: A handler thread for a client connection. To be used by the Server.<br/>
 */
public class ClientHandler implements Runnable {
	private Socket s;
	private DataInputStream dis;
	private DataOutputStream dos;

	private String ip;
	private String usr; // Username
	private String pw; // Password
	private boolean willClose;
	private final Object lock; // To lock the data output stream when the server is sending something
	private Server server;

	/**
	 * The main constructor of this class.
	 * @param s An opened socket of the client connection.
	 * @param username Client username
	 * @param password Client password
	 * @param lock A lock object to make sure the server and client thread don't send at the same time.
	 * @param server The server object to get the other target client.
	 */
	public ClientHandler(Socket s, String username, String password, Object lock, Server server) throws IOException {
		this.s = s;
		this.s.setKeepAlive(true);
		this.s.setSoTimeout(500);
		this.ip = s.getRemoteSocketAddress().toString();
		this.usr = username;
		this.pw = password;
		this.lock = lock;
		this.dis = new DataInputStream(s.getInputStream());
		this.dos = new DataOutputStream(s.getOutputStream());
		willClose = false;
		this.server = server;
	}

	/**
	 * Get the remote IP address of this client
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * Get the data input stream of this client
	 */
	public DataInputStream getDis() {
		return dis;
	}

	/**
	 * Get the data output stream of this client
	 */
	public DataOutputStream getDos() {
		return dos;
	}

	/**
	 * Get the username of this client
	 */
	public String getUsr() {
		return usr;
	}

	/**
	 * Get the password of this client
	 */
	public String getPw() {
		return pw;
	}

	/**
	 * Check whether the client thread will be closed in the next event loop
	 */
	public boolean willBeClosed() {
		return willClose;
	}

	/**
	 * Client thread event loop
	 */
	@Override
	public void run() {
		try {
			while (!willClose) {
				// Read a command from client
				String command = null;
				try {
					command = dis.readUTF();
				} catch (SocketTimeoutException timeoutException) {
					continue;
				} catch (SocketException socketException) {
					close();
					continue;
				}

				// Perform send text to another client
				if (command.equals("Text")) {
					String sendTo = dis.readUTF();
					String msg = dis.readUTF();
					ClientHandler target = server.getClient(sendTo);
					synchronized (server.getLock(sendTo)) {
						DataOutputStream out = target.getDos();
						out.writeUTF("Text");
						out.writeUTF(this.usr);
						out.writeUTF(msg);
						out.flush();
						System.out.println(usr + " sends a text to " + sendTo);
					}
				}
				// Perform send file to another client
				else if (command.equals("File")) {
					String sendTo = dis.readUTF();
					String filename = dis.readUTF();
					int fileSize = dis.readInt();
					int bufferSize = 4096;
					byte[] buffer = new byte[bufferSize];

					ClientHandler target = server.getClient(sendTo);
					synchronized (server.getLock(sendTo)) {
						DataOutputStream out = target.getDos();
						out.writeUTF("File");
						out.writeUTF(this.usr);
						out.writeUTF(filename);
						out.writeInt(fileSize);
						while (fileSize > 0) {
							// Read the file into buffer
							// Then send the buffer to the target one at a time
							int bRead = dis.read(buffer, 0, Math.min(fileSize, bufferSize));
							if (bRead < 1)
								break;
							out.write(buffer, 0, Math.min(fileSize, bufferSize));
							fileSize -= bRead;
						}
						out.flush();
						System.out.println(usr + " sends a file to " + sendTo);
					}
				}
				// Perform logout
				else if (command.equals("Logout")) {
					synchronized (lock) {
						dos.writeUTF("Logout success");
						dos.flush();
						close();
					}
				}
			}
		} catch (EOFException eofException) {
			System.out.println(usr + " closed connection.");
		} catch (Exception e) {
			System.out.println(usr + " encountered an error:");
			e.printStackTrace();
		} finally {
			// Close the client connection, remove it from the online list then signal other clients
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			server.removeOnline(usr);
			server.notifyOnline();
			System.out.println(usr + " has logged out.");
		}
	}

	/**
	 * Signal the client thread to be closed next event loop
	 */
	public void close() {
		willClose = true;
	}
}
