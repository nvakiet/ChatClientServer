package vn.edu.hcmus.student.sv19127191.ui;

import vn.edu.hcmus.student.sv19127191.server.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * vn.edu.hcmus.student.sv19127191.ui<br/>
 * Created by Ngo Van Anh Kiet - MSSV: 19127191<br/>
 * Date 8/1/2022 - 2:49 PM<br/>
 * Description: ...<br/>
 */
public class MainFrame extends JFrame {
	private JTextField ipInput;
	private JTextField portInput;
	private JButton startBtn;
	private final Thread socketThread;
	private final Server server;

	public MainFrame() throws Exception {
		super("Chat Server");
		setupServerPanel();
		server = new Server();
		socketThread = new Thread(server);
		setupListeners();
	}

	private void setupServerPanel() {
		JPanel mainPane = new JPanel();
		mainPane.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		JLabel header = new JLabel("Server Listens On", SwingConstants.CENTER);
		header.setFont(new Font("Times New Roman", Font.BOLD, 22));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.insets = new Insets(5,10,5,10);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPane.add(header, gbc);

		gbc = new GridBagConstraints();
		JLabel ipLabel = new JLabel("IP Address:", SwingConstants.RIGHT);
		ipLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(5,10,5,10);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPane.add(ipLabel, gbc);

		gbc = new GridBagConstraints();
		ipInput = new JTextField("localhost", 15);
		ipInput.setFont(new Font("Arial", Font.PLAIN, 16));
		ipLabel.setLabelFor(ipInput);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(5,0,5,10);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.5;
		gbc.weighty = 1.5;
		mainPane.add(ipInput, gbc);

		gbc = new GridBagConstraints();
		JLabel portLabel = new JLabel("Port:", SwingConstants.RIGHT);
		portLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.insets = new Insets(5,10,5,10);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		mainPane.add(portLabel, gbc);

		gbc = new GridBagConstraints();
		portInput = new JTextField("7191", 15);
		portInput.setFont(new Font("Arial", Font.PLAIN, 16));
		portLabel.setLabelFor(portInput);
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(5,0,5,10);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.5;
		gbc.weighty = 1.5;
		mainPane.add(portInput, gbc);

		gbc = new GridBagConstraints();
		startBtn = new JButton("Start");
		startBtn.setFont(new Font("Times New Roman", Font.BOLD, 18));
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(5,10,5,10);
		gbc.fill = GridBagConstraints.BOTH;
		mainPane.add(startBtn, gbc);

		setLayout(new BorderLayout());
		add(mainPane, BorderLayout.CENTER);
	}

	private void setupListeners() {
		startBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					server.setBindAddr(ipInput.getText(), Integer.parseInt(portInput.getText()));
					ipInput.setEditable(false);
					portInput.setEditable(false);
					startBtn.setEnabled(false);
					socketThread.start();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				server.stopServing();
				try {
					socketThread.join();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				System.out.println("Stopping Server");
				e.getWindow().dispose();
			}
		});
	}
}
