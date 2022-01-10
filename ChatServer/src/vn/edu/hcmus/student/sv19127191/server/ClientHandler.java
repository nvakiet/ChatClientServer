package vn.edu.hcmus.student.sv19127191.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * vn.edu.hcmus.student.sv19127191.server<br/>
 * Created by Ngo Van Anh Kiet - MSSV: 19127191<br/>
 * Date 8/1/2022 - 2:47 PM<br/>
 * Description: ...<br/>
 */
public class ClientHandler implements Runnable {
	private Socket s;
	private DataInputStream dis;
	private DataOutputStream dos;

	private String ip;
	private String usr; // Username
	private String pw; // Password
	private boolean willClose;
	private final Object lock;
	private Server server;

	public ClientHandler(Socket s, String username, String password, Object lock, Server server) throws IOException {
		this.s = s;
		this.ip = s.getRemoteSocketAddress().toString();
		this.usr = username;
		this.pw = password;
		this.lock = lock;
		this.dis = new DataInputStream(s.getInputStream());
		this.dos = new DataOutputStream(s.getOutputStream());
		willClose = false;
		this.server = server;
	}

	public String getIp() {
		return ip;
	}

	public DataInputStream getDis() {
		return dis;
	}

	public DataOutputStream getDos() {
		return dos;
	}

	public String getUsr() {
		return usr;
	}

	public String getPw() {
		return pw;
	}

	public boolean willBeClosed() {
		return willClose;
	}

	@Override
	public void run() {
		try {
			while (!willClose) {
				// Read a command from client
				String command = dis.readUTF();

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
					}
				}
				// Perform send file to another client
				else if (command.equals("File")) {
					String sendTo = dis.readUTF();
					String filename = dis.readUTF();
					int fileSize = dis.readInt();
					int bufferSize = 2048;
					byte[] buffer = new byte[bufferSize];

					ClientHandler target = server.getClient(sendTo);
					synchronized (server.getLock(sendTo)) {
						DataOutputStream out = target.getDos();
						out.writeUTF("File");
						out.writeUTF(this.usr);
						out.writeUTF(filename);
						out.writeLong(fileSize);
						while (fileSize > 0) {
							// Read the file into buffer
							// Then send the buffer to the target one at a time
							int bRead = dis.read(buffer, 0, Math.min(fileSize, bufferSize));
							if (bRead == -1)
								break;
							out.write(buffer, 0, Math.min(fileSize, bufferSize));
							fileSize -= bRead;
						}
						out.flush();
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
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			server.removeOnline(usr);
			server.notifyOnline();
		}
	}

	public void close() {
		willClose = true;
	}
}
