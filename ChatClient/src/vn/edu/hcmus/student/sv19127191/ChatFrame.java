package vn.edu.hcmus.student.sv19127191;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeMap;

/**
 * vn.edu.hcmus.student.sv19127191<br/>
 * Created by Ngo Van Anh Kiet - MSSV: 19127191<br/>
 * Date 12/1/2022 - 7:48 AM<br/>
 * Description: ...<br/>
 */
public class ChatFrame extends JFrame {
	private final Receiver receiver;
	private Socket socket;
	private final DataInputStream dis;
	private final DataOutputStream dos;
	private String username;
	private JLabel currentTarget;
	private StyledDocument currentDoc;
	private TreeMap<String, StyledDocument> chatLogs;
	private JList<String> onlineList;
	private JScrollPane boxScroll;
	private JTextPane chatBox;
	private JTextArea chatInput;
	private JButton logoutBtn;
	private JButton sendBtn;
	private JButton fileBtn;

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

	private void setupChatPanel() {
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
		Icon fileIcon = new ImageIcon("icons/folder.png");
		fileBtn = new JButton(fileIcon);
		fileBtn.setToolTipText("Send a file");
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.1;
		placeComp(gbc, centerPanel, fileBtn, 7, 8, 1, 1);

		// Text send button
		Icon sendIcon = new ImageIcon("icons/send.png");
		sendBtn = new JButton(sendIcon);
		sendBtn.setToolTipText("Send text message");
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.1;
		placeComp(gbc, centerPanel, sendBtn, 7, 9, 1, 1);

		// Add this panel to the center of content panel
		add(centerPanel, BorderLayout.CENTER);
	}

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
						StyledDocument doc = chatLogs.get(target);
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

	private void configOnlineList() {
		onlineList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					if (onlineList.getSelectedIndex() > 0) {
						currentTarget.setText(onlineList.getSelectedValue());
						currentDoc = chatLogs.get(currentTarget.getText());
						if (currentDoc == null) {
							currentDoc = new DefaultStyledDocument();
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
					} else {
						currentTarget.setText("<Choose another online user>");
						currentDoc = null;
						chatBox.setStyledDocument(new DefaultStyledDocument());
					}

				}
			}
		});
	}

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

	private void sendFile(String target, File file) {
		SwingUtilities.invokeLater(() -> {
			try (BufferedInputStream fin = new BufferedInputStream(new FileInputStream(file))) {
				synchronized (dos) {
					dos.writeUTF("File");
					dos.writeUTF(target);
					dos.writeUTF(file.getName());
					dos.writeInt((int) file.length());

					int count;
					byte[] buffer = new byte[4096];
					while ((count = fin.read(buffer)) > 0) {
						dos.write(buffer, 0, count);
					}
					dos.flush();

					displayNewFileName(chatLogs.get(target), username, file.getName(), null);
				}
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(getContentPane(),
						"An error has occurred: " + e.getMessage());
			}
		});
	}

	private void displayNewText(StyledDocument doc, String messenger, String message) {
		try {
			if (doc == null)
				return;

			// Define the display style of messenger name
			Style messengerStyle = null;
			if (messenger.equals(username)) {
				messengerStyle = doc.getStyle("User");
				if (messengerStyle == null) {
					messengerStyle = doc.addStyle("User", null);
					StyleConstants.setBold(messengerStyle, true);
					StyleConstants.setFontSize(messengerStyle, 14);
					StyleConstants.setForeground(messengerStyle, Color.BLUE);
				}
			} else {
				messengerStyle = doc.getStyle("Target");
				if (messengerStyle == null) {
					messengerStyle = doc.addStyle("Target", null);
					StyleConstants.setBold(messengerStyle, true);
					StyleConstants.setFontSize(messengerStyle, 14);
					StyleConstants.setForeground(messengerStyle, Color.RED);
				}
			}


			// Define the display style of message
			Style messageStyle = doc.getStyle("Message");
			if (messageStyle == null) {
				messageStyle = doc.addStyle("Message", null);
				StyleConstants.setBold(messageStyle, false);
				StyleConstants.setFontSize(messageStyle, 14);
				StyleConstants.setForeground(messageStyle, Color.BLACK);
			}

			// Insert the messenger name and message into the document
			doc.insertString(doc.getLength(), messenger + ":\n", messengerStyle);
			doc.insertString(doc.getLength(), message + "\n", messageStyle);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(getContentPane(),
					"An error has occurred: " + e.getMessage());
		}
	}

	private void displayNewFileName(StyledDocument doc, String messenger, String filename, byte[] fileData) {
		try {
			if (doc == null)
				return;

			// Define the display style of messenger name
			Style messengerStyle = null;
			if (messenger.equals(username)) {
				messengerStyle = doc.getStyle("User");
				if (messengerStyle == null) {
					messengerStyle = doc.addStyle("User", null);
					StyleConstants.setBold(messengerStyle, true);
					StyleConstants.setFontSize(messengerStyle, 14);
					StyleConstants.setForeground(messengerStyle, Color.BLUE);
				}
			} else {
				messengerStyle = doc.getStyle("Target");
				if (messengerStyle == null) {
					messengerStyle = doc.addStyle("Target", null);
					StyleConstants.setBold(messengerStyle, true);
					StyleConstants.setFontSize(messengerStyle, 14);
					StyleConstants.setForeground(messengerStyle, Color.RED);
				}
			}


			// Define the display style of file name
			Style messageStyle = doc.getStyle("Filename");
			if (messageStyle == null) {
				messageStyle = doc.addStyle("Filename", null);
				StyleConstants.setBold(messageStyle, false);
				StyleConstants.setItalic(messageStyle, true);
				StyleConstants.setUnderline(messageStyle, true);
				StyleConstants.setFontSize(messageStyle, 14);
				StyleConstants.setForeground(messageStyle, Color.GREEN);
			}

			// Insert the messenger name and file name into the document
			doc.insertString(doc.getLength(), messenger + ":\n", messengerStyle);
			doc.insertString(doc.getLength(), filename + "\n", messageStyle);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(getContentPane(),
					"An error has occurred: " + e.getMessage());
		}
	}

	class Receiver implements Runnable {
		private boolean willClose = false;

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
						synchronized (chatLogs) {
							if (chatLogs.get(fromUsr) == null) {
								StyledDocument doc = new DefaultStyledDocument();
								chatLogs.put(fromUsr, doc);
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
						displayNewText(chatLogs.get(fromUsr), fromUsr, msg);
					}
				}
			} catch (SocketException socketException) {
				JOptionPane.showMessageDialog(getContentPane(), "Server has disconnected. Please log in again.");
			} catch (EOFException eofException) {
				JOptionPane.showMessageDialog(getContentPane(), "Server has disconnected. Please log in again.");
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(getContentPane(),
						"An error has occurred: " + e.getMessage());
			} finally {
				try {
					if (!socket.isClosed()) {
						socket.close();
					}
					LoginFrame frame = new LoginFrame();
					frame.pack();
					frame.setVisible(true);
					dispose();
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(getContentPane(),
							"An error has occurred: " + ex.getMessage());
				}
			}
		}

		public void close() {
			willClose = true;
		}
	}
}
