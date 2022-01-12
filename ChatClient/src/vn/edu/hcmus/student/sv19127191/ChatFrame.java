package vn.edu.hcmus.student.sv19127191;

import javax.swing.*;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * vn.edu.hcmus.student.sv19127191<br/>
 * Created by Ngo Van Anh Kiet - MSSV: 19127191<br/>
 * Date 12/1/2022 - 7:48 AM<br/>
 * Description: ...<br/>
 */
public class ChatFrame extends JFrame {
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	private String username;
	private JLabel currentTarget;
	private StyledDocument currentDoc;
	private TreeMap<String, StyledDocument> chatLogs;
	private DefaultListModel<String> model;
	private JList<String> onlineList;
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

		setLayout(new BorderLayout());
		setupOnlineUsersPanel();
		setupChatPanel();
		pack();
		setMinimumSize(getSize());
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
		model = new DefaultListModel<String>();
		onlineList = new JList<String>(model);
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
		JScrollPane boxScroll = new JScrollPane(chatBox);
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
}
