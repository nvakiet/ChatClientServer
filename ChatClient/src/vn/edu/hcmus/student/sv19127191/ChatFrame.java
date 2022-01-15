package vn.edu.hcmus.student.sv19127191;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

/**
 * vn.edu.hcmus.student.sv19127191<br/>
 * Created by Ngo Van Anh Kiet - MSSV: 19127191<br/>
 * Date 12/1/2022 - 7:48 AM<br/>
 * Description: Chat GUI for client program.<br/>
 */
public class ChatFrame extends JFrame {
	private final Receiver receiver;
	private Socket socket;
	private final DataInputStream dis;
	private final DataOutputStream dos;
	private String username;
	private JLabel currentTarget;
	private HTMLDocument currentDoc;
	private TreeMap<String, HTMLDocument> chatLogs;
	private JList<String> onlineList;
	private JScrollPane boxScroll;
	private JTextPane chatBox;
	private JTextArea chatInput;
	private JButton logoutBtn;
	private JButton sendBtn;
	private JButton fileBtn;
	private HashMap<String, FileHandler> filehandlers;
	private String cwd;

	/**
	 * Main constructor for this class
	 * @param s The socket connection that has been set up from the Login frame
	 * @param dis The data input stream for the socket
	 * @param dos The data output stream for the socket
	 * @param usr The username of the connected account
	 */
	public ChatFrame(Socket s, DataInputStream dis, DataOutputStream dos, String usr) {
		super("Chat Client: " + usr);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		socket = s;
		this.dis = dis;
		this.dos = dos;
		username = usr;
		currentTarget = null;
		currentDoc = null;
		chatLogs = new TreeMap<>();
		filehandlers = new HashMap<>();
		cwd = Paths.get(".").toAbsolutePath().normalize().toString();
		receiver = new Receiver();

		setLayout(new BorderLayout());
		setupOnlineUsersPanel();
		setupChatPanel();
		setupButtons();
		configOnlineList();
		configChatInputKey();
		pack();
		setMinimumSize(getSize());

		Thread t = new Thread(receiver);
		t.setDaemon(true);
		t.start();
	}

	/**
	 * A utility method to place UI component on a panel with GridBag Layout
	 * @param gbc The grid bag constraints for the inserted UI component
	 * @param panel The target UI panel
	 * @param comp The UI component to be inserted into the panel
	 * @param x The x-coordinate of the component's top-left corner on the grid
	 * @param y The y-coordinate of the component's top-left corner on the grid
	 * @param w The width of the component from the top-left corner
	 * @param h The height of the component from the top-left corner
	 */
	protected void placeComp(GridBagConstraints gbc, JPanel panel, Component comp, int x, int y, int w, int h) {
		if (gbc == null)
			gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = w;
		gbc.gridheight = h;
		gbc.insets = new Insets(5,5,5,5);
		panel.add(comp, gbc);
	}

	/**
	 * Set up the panel to show the list of currently online users
	 */
	private void setupOnlineUsersPanel() {
		JPanel leftPane = new JPanel(new GridBagLayout());

		// Panel title
		JLabel lbOnline = new JLabel("Online users:");
		lbOnline.setFont(new Font("Arial", Font.BOLD, 16));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		placeComp(gbc, leftPane, lbOnline, 0, 0, 1, 1);

		// A list wrapped in a scroll pane
		onlineList = new JList<String>();
		onlineList.setFont(new Font("Arial", Font.PLAIN, 14));
		JScrollPane listScroll = new JScrollPane(onlineList);
		lbOnline.setLabelFor(listScroll);
		listScroll.setPreferredSize(new Dimension(200,400));
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.5;
		placeComp(gbc, leftPane, listScroll, 0, 1, 1, 8);

		// Logout button
		logoutBtn = new JButton("Log Out");
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		placeComp(gbc, leftPane, logoutBtn, 0, 9, 1, 1);

		// Add this panel to the left of content panel
		add(leftPane, BorderLayout.LINE_START);
	}

	/**
	 * Set up the main chat panel of the Chat frame
	 */
	private void setupChatPanel() {
		try {
			JPanel centerPanel = new JPanel(new GridBagLayout());

			// Chat box labels
			JLabel lbChat = new JLabel("Chatting with:");
			lbChat.setFont(new Font("Arial", Font.BOLD, 16));
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			placeComp(gbc, centerPanel, lbChat, 0, 0, 1, 1);

			currentTarget = new JLabel("<Choose another online user>");
			currentTarget.setFont(new Font("Arial", Font.BOLD, 16));
			gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			placeComp(gbc, centerPanel, currentTarget, 1, 0, 1, 1);

			// Chat box
			chatBox = new JTextPane();
			chatBox.setEditable(false);
			HTMLEditorKit kit = new HTMLEditorKit();
			StyleSheet sheet = new StyleSheet();
			sheet.importStyleSheet(new File("resources/style.css").toURI().toURL());
			kit.setStyleSheet(sheet);
			chatBox.setEditorKit(kit);
			boxScroll = new JScrollPane(chatBox);
			boxScroll.setPreferredSize(new Dimension(600, 400));
			gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.5;
			gbc.weighty = 1.5;
			placeComp(gbc, centerPanel, boxScroll, 0, 1, 8, 7);

			// Chat input area
			chatInput = new JTextArea(2, 30);
			JScrollPane textScroll = new JScrollPane(chatInput);
			textScroll.setPreferredSize(new Dimension(400, 50));
			gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.5;
			gbc.weighty = 1.5;
			placeComp(gbc, centerPanel, textScroll, 0, 8, 7, 2);

			// File send button
			Icon fileIcon = new ImageIcon("resources/folder.png");
			fileBtn = new JButton(fileIcon);
			fileBtn.setToolTipText("Send a file");
			gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weighty = 1.1;
			placeComp(gbc, centerPanel, fileBtn, 7, 8, 1, 1);

			// Text send button
			Icon sendIcon = new ImageIcon("resources/send.png");
			sendBtn = new JButton(sendIcon);
			sendBtn.setToolTipText("Send text message");
			gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weighty = 1.1;
			placeComp(gbc, centerPanel, sendBtn, 7, 9, 1, 1);

			// Add this panel to the center of content panel
			add(centerPanel, BorderLayout.CENTER);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Can't launch chat window.\n" +
							"Error: " + e.getMessage());
			try {
				socket.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			LoginFrame frame = new LoginFrame();
			frame.setVisible(true);
			this.dispose();
		}
	}

	/**
	 * Set up the button listeners
	 */
	private void setupButtons() {
		// Logout button
		logoutBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logout();
			}
		});

		// Send text button
		sendBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					String target = currentTarget.getText();
					if (!target.startsWith("<")) {
						HTMLDocument doc = chatLogs.get(target);
						String text = chatInput.getText();
						displayNewText(doc, username, text);
						sendText(target, text);
					}
					chatInput.setText("");
				} catch (NullPointerException ignored) {
					// Do nothing here
				}
			}
		});

		// Send file button
		fileBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentTarget.getText().startsWith("<"))
					return;
				JFileChooser fChoose = new JFileChooser();
				int result = fChoose.showOpenDialog(getContentPane());
				if (result == JFileChooser.APPROVE_OPTION) {
					File file = fChoose.getSelectedFile();
					sendFile(currentTarget.getText(), file);
				}
			}
		});
	}

	/**
	 * Configure the listener for the online user list
	 */
	private void configOnlineList() {
		onlineList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					// If user select a name of other user on the list
					if (onlineList.getSelectedIndex() > 0) {
						// Set the current chat log
						currentTarget.setText(onlineList.getSelectedValue());
						currentDoc = chatLogs.get(currentTarget.getText());
						// If there's no chat log for the selected target, create a new chat log
						if (currentDoc == null) {
							currentDoc =(HTMLDocument) ((HTMLEditorKit) chatBox.getEditorKit()).createDefaultDocument();
							chatLogs.put(currentTarget.getText(), currentDoc);
							currentDoc.addDocumentListener(new DocumentListener() {
								@Override
								public void insertUpdate(DocumentEvent e) {
									boxScroll.getVerticalScrollBar().setValue(boxScroll.getVerticalScrollBar().getMaximum());
								}

								@Override
								public void removeUpdate(DocumentEvent e) {
									// Do nothing
								}

								@Override
								public void changedUpdate(DocumentEvent e) {
									// Do nothing
								}
							});
						}
						chatBox.setStyledDocument(currentDoc);
						boxScroll.getVerticalScrollBar().setValue(boxScroll.getVerticalScrollBar().getMaximum());
					} else if (onlineList.getSelectedIndex() == 0) {
						currentTarget.setText("<Choose another online user>");
						currentDoc = null;
						chatBox.setStyledDocument((StyledDocument) chatBox.getEditorKit().createDefaultDocument());
					}

				}
			}
		});
	}

	/**
	 * Configure the chat text input area keymap: press Shift-Enter to make a new line; Enter to send the text.
	 */
	private void configChatInputKey() {
		InputMap input = chatInput.getInputMap();
		KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
		KeyStroke shiftEnter = KeyStroke.getKeyStroke("shift ENTER");
		input.put(shiftEnter, "insert-break");
		input.put(enter, "text-submit");

		ActionMap actions = chatInput.getActionMap();
		actions.put("text-submit", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendBtn.doClick();
			}
		});
	}

	/**
	 * Send logout command to the server
	 */
	private void logout() {
		SwingUtilities.invokeLater(() -> {
			synchronized (dos) {
				try {
					dos.writeUTF("Logout");
					dos.flush();
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(getContentPane(),
							"An error has occurred: " + e.getMessage());
				}
			}
		});
	}

	/**
	 * Send a text message to the target client
	 * @param target The target username
	 * @param msg The text message to be sent
	 */
	private void sendText(String target, String msg) {
		SwingUtilities.invokeLater(() -> {
			synchronized (dos) {
				try {
					dos.writeUTF("Text");
					dos.writeUTF(target);
					dos.writeUTF(msg);
					dos.flush();
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(getContentPane(),
							"An error has occurred: " + e.getMessage());
				}
			}
		});
	}

	/**
	 * Send a file to the target client
	 * @param target The target username
	 * @param file The file to be sent
	 */
	private void sendFile(String target, File file) {
		SwingUtilities.invokeLater(() -> {
			try (BufferedInputStream fin = new BufferedInputStream(new FileInputStream(file));
					ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
				synchronized (dos) {
					dos.writeUTF("File");
					dos.writeUTF(target);
					dos.writeUTF(file.getName());
					dos.writeInt((int) file.length());

					int count;
					byte[] buffer = new byte[4096];
					while ((count = fin.read(buffer)) > 0) {
						dos.write(buffer, 0, count);
						bout.write(buffer, 0, count);
					}
					dos.flush();

					displayNewFileName(chatLogs.get(target), username, file.getName(), bout.toByteArray());
				}
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(getContentPane(),
						"An error has occurred: " + e.getMessage());
			}
		});
	}

	/**
	 * Add new text message on the chat box
	 * @param doc The target chat box's document to insert new message
	 * @param messenger The name of the sender
	 * @param message The text message to be inserted
	 */
	private synchronized void displayNewText(HTMLDocument doc, String messenger, String message) {
		try {
			if (doc == null)
				return;

			// Get the html style sheet from css file
			String className = messenger.equals(username)? "user" : "target";
			HTMLEditorKit kit = (HTMLEditorKit) chatBox.getEditorKit();

			// Insert the messenger name and message into the document
			String s = "<p class=\"" + className + "\">" + messenger + ":</p>";
			kit.insertHTML(doc, doc.getLength(), s, 0,0,null);
			s = "<p>" + message + "</p>";
			kit.insertHTML(doc, doc.getLength(), s, 0,0,null);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(getContentPane(),
					"An error has occurred: " + e.getMessage());
		}
	}

	/**
	 * Add new file message on the chat box.
	 * @param doc The target chat box's document to insert new message
	 * @param messenger The name of the sender
	 * @param filename The file name
	 * @param fileData The file data as byte array
	 */
	private synchronized void displayNewFileName(HTMLDocument doc, String messenger, String filename, byte[] fileData) {
		try {
			if (doc == null)
				return;
			boolean isSender = messenger.equals(username);

			// Get the html style sheet from css file
			String className = isSender? "user" : "target";
			HTMLEditorKit kit = (HTMLEditorKit) chatBox.getEditorKit();
			StyleSheet sheet = kit.getStyleSheet();

			// Add the new file to the file handler of this chat session
			String session = isSender? currentTarget.getText() : messenger;
			FileHandler handler = filehandlers.get(session);
			if (handler == null) {
				handler = new FileHandler(session);
				filehandlers.put(session, handler);
			}
			handler.addFile(filename, fileData);

			// Insert the messenger name and file name into the document
			String s = "<p class=\"" + className + "\">" + messenger + ":</p>";
			kit.insertHTML(doc, doc.getLength(), s, 0,0,null);
			StringBuilder hrefFile = new StringBuilder();
			hrefFile.append(cwd).append("/")
					.append(handler.getDataDir()).append("/")
					.append(filename.replace(" ", "%20"));
			s = "<a class=\"filename\" href=\"file:///" + hrefFile.toString() + "\">" + filename + "</a>";
			kit.insertHTML(doc, doc.getLength(), s, 0,0,null);

			if (chatBox.getHyperlinkListeners() == null || chatBox.getHyperlinkListeners().length == 0) {
				chatBox.addHyperlinkListener(new HyperlinkListener() {
					@Override
					public void hyperlinkUpdate(HyperlinkEvent e) {
						if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
							try {
								File file = new File(e.getURL().toURI());
								System.out.println(file.getName());
								filehandlers.get(currentTarget.getText()).handleFile(file);
							} catch (URISyntaxException ex) {
								ex.printStackTrace();
								JOptionPane.showMessageDialog(getContentPane(),
										"An error has occurred: " + ex.getMessage());
							}
						}
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(getContentPane(),
					"An error has occurred: " + e.getMessage());
		}
	}

	/**
	 * A thread for constantly receiving messages from the Server
	 */
	class Receiver implements Runnable {
		private boolean willClose = false;

		/**
		 * The event loop thread of this Receiver
		 */
		@Override
		public void run() {
			try {
				while (!willClose) {
					String response = null;
					try {
						response = dis.readUTF();
					} catch (SocketTimeoutException timeoutException) {
						continue;
					}

					// Receive logout response
					if (response.equals("Logout success")) {
						JOptionPane.showMessageDialog(getContentPane(), "Logged out successfully.");
						close();
					}
					// Receive online user list
					else if (response.equals("Online")) {
						response = dis.readUTF();
						ArrayList<String> onlineUsers = new ArrayList<String>(Arrays.asList(response.split(";")));
						int i = onlineUsers.indexOf(username);
						onlineUsers.set(i, onlineUsers.get(0));
						onlineUsers.set(0, ">> " + username);
						Collections.sort(onlineUsers.subList(1, onlineUsers.size()));
						DefaultListModel<String> model = new DefaultListModel<>();
						model.addAll(onlineUsers);
						onlineList.setModel(model);
					}
					// Receive text message
					else if (response.equals("Text")) {
						String fromUsr = dis.readUTF();
						String msg = dis.readUTF();
						putTargetDocument(fromUsr);
						displayNewText(chatLogs.get(fromUsr), fromUsr, msg);
					}
					// Receive file message
					else if (response.equals("File")) {
						String fromUsr = dis.readUTF();
						String filename = dis.readUTF();
						int fileSize = dis.readInt();
						int bufferSize = 4096;
						byte[] buffer = new byte[bufferSize];
						ByteArrayOutputStream bout = new ByteArrayOutputStream();

						while (fileSize > 0) {
							int bRead = dis.read(buffer, 0, Math.min(bufferSize, fileSize));
							if (bRead < 1)
								break;
							bout.write(buffer, 0, Math.min(bufferSize, fileSize));
							fileSize -= bRead;
						}

						// Display new file name
						putTargetDocument(fromUsr);
						displayNewFileName(chatLogs.get(fromUsr), fromUsr, filename, bout.toByteArray());
						bout.close();
					}
				}

			} catch (SocketException | EOFException socketException) {
				JOptionPane.showMessageDialog(getContentPane(), "Server has disconnected. Please log in again.");
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(getContentPane(),
						"An error has occurred: " + e.getMessage());
			} finally {
				try {
					// Close the connection to the server then return to Login frame
					if (!socket.isClosed()) {
						socket.close();
					}
					LoginFrame frame = new LoginFrame();
					frame.setVisible(true);
					dispose();
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(getContentPane(),
							"An error has occurred: " + ex.getMessage());
				}
			}
		}

		/**
		 * Signal that the Receiver thread will be closed on next event loop
		 */
		public void close() {
			willClose = true;
		}

		/**
		 * Put a new target document to the chat log mapping
		 * @param target The target username
		 */
		private void putTargetDocument(String target) {
			synchronized (chatLogs) {
				if (chatLogs.get(target) == null) {
					HTMLDocument doc = (HTMLDocument) ((HTMLEditorKit) chatBox.getEditorKit()).createDefaultDocument();;
					chatLogs.put(target, doc);
					doc.addDocumentListener(new DocumentListener() {
						@Override
						public void insertUpdate(DocumentEvent e) {
							boxScroll.getVerticalScrollBar().setValue(boxScroll.getVerticalScrollBar().getMaximum());
						}

						@Override
						public void removeUpdate(DocumentEvent e) {
							// Do nothing
						}

						@Override
						public void changedUpdate(DocumentEvent e) {
							// Do nothing
						}
					});
				}
			}
		}
	}

	/**
	 * A class for handling the file data in a chat session
	 */
	class FileHandler {
		private final String session;
		private final String dataDir;
		private final HashMap<String, byte[]> files;
		private final LinkedList<String> fileList;
		private final int limit;

		/**
		 * Get the file data directory of this chat session
		 * @return The path to the data directory for this session
		 */
		public String getDataDir() {
			return dataDir;
		}

		/**
		 * Main constructor for this class. Create a new file handler associated with a chat session
		 * @param sessionName The name of the chat session, which is the target username
		 */
		public FileHandler(String sessionName) {
			session = sessionName;
			dataDir = "data/" + username.replace(" ", "%20")
					+ "/" + session.replace(" ", "%20");
			files = new HashMap<>();
			fileList = new LinkedList<>();
			limit = 20;
		}

		/**
		 * Add new file data to this handler, also remove the oldest file if the total number of files passes 20
		 * @param filename The name of the new file to be added
		 * @param data The data of the new file
		 */
		public synchronized void addFile(String filename, byte[] data) {
			files.put(filename, data);
			fileList.add(filename);
			removeOverLimit();
		}

		/**
		 * Check if the total number of files in this handler is over 20, then remove the oldest file if it's over the limit
		 */
		public synchronized void removeOverLimit() {
			if (fileList.size() > limit) {
				files.remove(fileList.remove());
			}
		}

		/**
		 * Handle a file sent in this chat session
		 * @param file The file object to be saved
		 */
		public synchronized void handleFile(File file) {
			try {
				// If the session directory doesn't exist, create it
				File dir = file.getParentFile();
				if (dir != null)
					dir.mkdirs();

				// If the file doesn't exist, save it to session data directory
				// Otherwise, open the file in file explorer
				if (!(file.exists() && !file.isDirectory())) {
					if (!files.containsKey(file.getName())) {
						// If the file data no longer exist (due to overlimit)
						// The sender need to send it again
						JOptionPane.showMessageDialog(getContentPane(),
								"The file \"" + file.getName() + "\"is outdated for this chat session.\n"
										+ "You or " + currentTarget.getText() + " should send it again.");
						return;
					}
					saveFile(file, files.get(file.getName()));
				}

				// Ask if the user wants to open the file with Desktop API
				String[] options = new String[] {"Open", "Show in Directory", "Cancel"};
				int result = JOptionPane.showOptionDialog(getContentPane(),
						"The file is saved in:\n" +
								"\"" + file.getPath() + "\"\n" +
								"Do you want to open it?",
						"Open this file?",
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null, options, options[0]);
				if (result == 0) {
					Thread t = new Thread(() -> {
						try {
							Desktop.getDesktop().open(file);
						} catch (UnsupportedOperationException unsupportedOperationException) {
							JOptionPane.showMessageDialog(getContentPane(),
									"Your computer platform doesn't support this action.\n" +
											"You can manually view the file at:\n" +
											"\"" + file.getPath() + "\"");
						} catch (Exception e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(getContentPane(),
									"An error has occurred while handling file \"" + file.getName() + "\":\n"
											+ e.getMessage());
						}
					});
					t.setDaemon(true);
					t.start();
				} else if (result == 1) {
					Thread t = new Thread(() -> {
						try {
							Desktop.getDesktop().open(file.getParentFile());
						} catch (UnsupportedOperationException unsupportedOperationException) {
							JOptionPane.showMessageDialog(getContentPane(),
									"Your computer platform doesn't support this action.\n" +
											"You can manually view the file at:\n" +
											"\"" + file.getPath() + "\"");
						} catch (Exception e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(getContentPane(),
									"An error has occurred while handling file \"" + file.getName() + "\":\n"
											+ e.getMessage());
						}
					});
					t.setDaemon(true);
					t.start();
				}
			} catch (UnsupportedOperationException unsupportedOperationException) {
				JOptionPane.showMessageDialog(getContentPane(),
						"Your computer platform doesn't support this action.\n" +
								"You can manually view the file at:\n" +
								"\"<Program directory>/" + dataDir + "/" + file.getName() + "\"");
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(getContentPane(),
						"An error has occurred while handling file \"" + file.getName() + "\":\n"
								+ e.getMessage());
			}
		}

		/**
		 * Save the file data to a file path in the File object
		 * @param file The file object to be saved
		 * @param data The file data to be saved
		 */
		private void saveFile(File file, byte[] data) {
			try (BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(file))) {
				bout.write(data);
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(getContentPane(),
						"An error has occurred while handling file \"" + file.getName() + "\":\n"
								+ e.getMessage());
			}
		}
	}
}
