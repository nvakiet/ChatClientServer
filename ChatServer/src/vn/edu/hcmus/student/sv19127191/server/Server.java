package vn.edu.hcmus.student.sv19127191.server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * vn.edu.hcmus.student.sv19127191.server<br/>
 * Created by Ngo Van Anh Kiet - MSSV: 19127191<br/>
 * Date 8/1/2022 - 2:48 PM<br/>
 * Description: ...<br/>
 */
public class Server implements Runnable {
	private ServerSocket ss;
	private HashMap<String, ClientHandler> clients; // Clients who have logged in
	private HashMap<String, Object> locks;
	private ArrayList<Thread> threads;
	private ArrayList<Account> accounts;
	private boolean willClose;
	private PrintWriter writer;

	public Server() throws IOException {
		ss = null;
		clients = new HashMap<>();
		locks = new HashMap<>();
		threads = new ArrayList<>();
		accounts = new ArrayList<>();
		willClose = false;
		loadAccounts();
		File file = new File("data/accounts.txt");
		if (!(file.exists() && !file.isDirectory())) {
			file.createNewFile();
			file = new File("data/accounts.txt");
		}
		writer = new PrintWriter(new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8, true)));
	}

	public void setBindAddr(String ip, int port) throws IOException {
		ss = new ServerSocket(port, 10, InetAddress.getByName(ip));
		ss.setReuseAddress(true);
		ss.setSoTimeout(500);
	}

	private void loadAccounts() {
		try {
			File file = new File("data/accounts.txt");
			if (!(file.exists() && !file.isDirectory()))
				return;
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));

			accounts.clear();
			String line = null;
			while ((line = br.readLine()) != null && !line.isEmpty()) {
				String[] token = line.split(";");
				Account acc = new Account();
				acc.username = token[0];
				acc.password = token[1];
				accounts.add(acc);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveAccounts() {
		try {
			for (Account acc : accounts) {
				writer.println(acc.username + ";" + acc.password);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopServing() {
		willClose = true;
	}

	public void notifyOnline() {
		String msg = String.join(";", clients.keySet());
		for (String usr : clients.keySet()) {
			synchronized (locks.get(usr)) {
				try {
					if (clients.get(usr).willBeClosed())
						continue;
					clients.get(usr).getDos().writeUTF("Online");
					clients.get(usr).getDos().writeUTF(msg);
					clients.get(usr).getDos().flush();
				} catch (Exception e) {
					System.out.println("Can't send to " + usr + ". The connection is interrupted.");
					removeOnline(usr);
				}
			}
		}
	}

	@Override
	public void run() {
		try {
			while (!willClose) {
				// Wait for a client to connect
				Socket socket = null;
				try {
					socket = ss.accept();
				} catch (SocketTimeoutException timeout) {
					continue;
				}

				// Serve client requests: Login or Register
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
				String req = dis.readUTF();
				String username = dis.readUTF();
				String password = dis.readUTF();

				// Login request
				if (req.equals("Login")) {
					// Check if the account is already online
					if (clients.containsKey(username)) {
						dos.writeUTF("This account already logged in.");
						dos.flush();
						socket.close();
					}
					else {
						// 0 = not found, 1 = success, 2 = wrong password
						int status = 0;
						for (Account acc : accounts) {
							if (acc.username.equals(username)) {
								if (acc.password.equals(password)) {
									dos.writeUTF("Login success");
									dos.flush();
									Object lock = new Object();
									locks.put(username, lock);
									ClientHandler newClient = new ClientHandler(socket, username, password, lock, this);
									clients.put(username, newClient);
									Thread clientThread = new Thread(newClient);
									threads.add(clientThread);
									clientThread.start();
									status = 1;
									notifyOnline();
									System.out.println(acc.username + " has logged in.");
								}
								else {
									dos.writeUTF("Wrong password");
									dos.flush();
									status = 2;
									socket.close();
								}
								break;
							}
						}
						if (status == 0) {
							dos.writeUTF("Account doesn't exist");
							dos.flush();
							socket.close();
						}
					}
				}
				// Register request: Also immediately login after register success
				else if (req.equals("Register")) {
					// Check existing account
					boolean found = false;
					for (Account acc : accounts) {
						if (acc.username.equals(username)) {
							dos.writeUTF("Account already exists");
							dos.flush();
							found = true;
							socket.close();
							break;
						}
					}
					if (!found) {
						Account acc = new Account();
						acc.username = username;
						acc.password = password;
						accounts.add(acc);
						writer.println(acc.username + ";" + acc.password);
						writer.flush();
						dos.writeUTF("Register success");
						dos.flush();
						Object lock = new Object();
						locks.put(username, lock);
						ClientHandler newClient = new ClientHandler(socket, username, password, lock, this);
						clients.put(username, newClient);
						Thread clientThread = new Thread(newClient);
						threads.add(clientThread);
						clientThread.start();
						notifyOnline();
						System.out.println(acc.username + " is registered and logged in.");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ss.close();
				for (String acc : clients.keySet()) {
					clients.get(acc).close();
				}
				for (Thread t : threads)
					t.join();
				writer.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public synchronized void removeOnline(String usr) {
		clients.remove(usr);
		locks.remove(usr);
	}

	public ClientHandler getClient(String usr) {
		return clients.get(usr);
	}

	public Object getLock(String usr) {
		return locks.get(usr);
	}
}

class Account {
	public String username;
	public String password;
}