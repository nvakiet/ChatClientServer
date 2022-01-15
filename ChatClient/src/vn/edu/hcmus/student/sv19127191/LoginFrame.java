package vn.edu.hcmus.student.sv19127191;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * vn.edu.hcmus.student.sv19127191<br/>
 * Created by Ngo Van Anh Kiet - MSSV: 19127191<br/>
 * Date 12/1/2022 - 7:48 AM<br/>
 * Description: Login GUI of the chat client.<br/>
 */
public class LoginFrame extends JFrame {
	private Socket s = null;
	private DataInputStream dis = null;
	private DataOutputStream dos = null;
	private JTextField ipField;
	private JTextField portField;
	private JTextField usrField;
	private JPasswordField pwField;
	private JButton loginBtn;
	private JButton regBtn;

	/**
	 * Main constructor of this class
	 */
	public LoginFrame() {
		super("Chat Client");
		setupUI();
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// Add listeners
		loginBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String username = usrField.getText();
				String password = hashPassword(pwField.getPassword());
				if (username != null && password != null) {
					login(username, password);
				}
			}
		});



		regBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String username = usrField.getText();
				String password = hashPassword(pwField.getPassword());
				Pattern pattern = Pattern.compile("^[ A-Za-z0-9]+$");
				Matcher matcher = pattern.matcher(username);
				if (!matcher.matches()) {
					JOptionPane.showMessageDialog(getContentPane(),
							"Username can only contain: letters (uppercase and lowercase), numbers and whitespaces");
					return;
				}
				if (username != null && password != null) {
					register(username, password);
				}
			}
		});

		// Set login button as default for Enter key press
		getRootPane().setDefaultButton(loginBtn);
	}

	/**
	 * Set up the GUI components of the login frame
	 */
	private void setupUI() {
		JPanel mainPane = new JPanel(new GridBagLayout());

		// Set up UI components
		// Title label
		GridBagConstraints gbc = new GridBagConstraints();
		JLabel title = new JLabel("Connect to Server", SwingConstants.CENTER);
		title.setFont(new Font("Arial", Font.BOLD, 20));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5,5,5,5);
		mainPane.add(title, gbc);

		// IP input
		gbc = new GridBagConstraints();
		JLabel lbServer = new JLabel("Server IP:", SwingConstants.LEFT);
		lbServer.setFont(new Font("Arial", Font.BOLD, 16));
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5,5,5,5);
		mainPane.add(lbServer, gbc);

		gbc = new GridBagConstraints();
		ipField = new JTextField(13);
		ipField.setText("localhost");
		ipField.setFont(new Font("Arial", Font.PLAIN, 14));
		lbServer.setLabelFor(ipField);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5,5,5,5);
		mainPane.add(ipField, gbc);

		// Port input
		gbc = new GridBagConstraints();
		JLabel lbPort = new JLabel("Port:", SwingConstants.LEFT);
		lbPort.setFont(new Font("Arial", Font.BOLD, 16));
		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5,5,5,5);
		mainPane.add(lbPort, gbc);

		gbc = new GridBagConstraints();
		portField = new JTextField(7);
		portField.setText("7191");
		portField.setFont(new Font("Arial", Font.PLAIN, 14));
		lbPort.setLabelFor(portField);
		gbc.gridx = 4;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5,5,5,5);
		mainPane.add(portField, gbc);

		// Username input
		gbc = new GridBagConstraints();
		JLabel lbUsr = new JLabel("Username:", SwingConstants.LEFT);
		lbUsr.setFont(new Font("Arial", Font.BOLD, 16));
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5,5,5,5);
		mainPane.add(lbUsr, gbc);

		gbc = new GridBagConstraints();
		usrField = new JTextField(15);
		usrField.setText("admin");
		usrField.setFont(new Font("Arial", Font.PLAIN, 14));
		lbUsr.setLabelFor(usrField);
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 4;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5,5,5,5);
		mainPane.add(usrField, gbc);

		// Password input
		gbc = new GridBagConstraints();
		JLabel lbPw = new JLabel("Password:", SwingConstants.LEFT);
		lbPw.setFont(new Font("Arial", Font.BOLD, 16));
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5,5,5,5);
		mainPane.add(lbPw, gbc);

		gbc = new GridBagConstraints();
		pwField = new JPasswordField(15);
		pwField.setText("admin");
		pwField.setFont(new Font("Arial", Font.PLAIN, 14));
		lbPw.setLabelFor(pwField);
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.gridwidth = 4;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5,5,5,5);
		mainPane.add(pwField, gbc);

		// Register button
		gbc = new GridBagConstraints();
		regBtn = new JButton("Register");
		regBtn.setFont(new Font("Arial", Font.BOLD, 16));
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5,5,5,5);
		mainPane.add(regBtn, gbc);

		// Login button
		gbc = new GridBagConstraints();
		loginBtn = new JButton("Log In");
		loginBtn.setFont(new Font("Arial", Font.BOLD, 16));
		gbc.gridx = 3;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5,5,5,5);
		mainPane.add(loginBtn, gbc);

		// Add main panel to content panel
		add(mainPane);
		setResizable(false);
	}

	/**
	 * Create a client socket then connect to the server. This function is used in the login and register button listeners
	 */
	private void connect() {
		try {
			if (s != null) {
				s.close();
			}
			s = new Socket();
			s.setSoTimeout(500);
			// Connect with 10s timeout
			s.connect(new InetSocketAddress(ipField.getText(), Integer.parseInt(portField.getText())), 10000);
			dis = new DataInputStream(s.getInputStream());
			dos = new DataOutputStream(s.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Can't connect to server.");
		}
	}

	/**
	 * Connect to the server then log in with a username and password
	 * @param username The account username in the server's account list
	 * @param password The account password in the server's account list
	 */
	private void login(String username, String password) {
		enableUI(false);
		SwingUtilities.invokeLater(() -> {
			connect();
			if (!s.isConnected()) {
				enableUI(true);
				return;
			}
			synchronized (dos) {
				try {
					dos.writeUTF("Login");
					dos.writeUTF(username);
					dos.writeUTF(password);
					dos.flush();

					String response = dis.readUTF();
					if (response.equals("Login success")) {
						JOptionPane.showMessageDialog(this,
								"Log in succesfully.");
						ChatFrame chatFrame = new ChatFrame(s, dis, dos, username);
						chatFrame.setVisible(true);
						this.dispose();
					} else {
						JOptionPane.showMessageDialog(this, response);
						enableUI(true);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(this,
							"An error has occurred: " + ex.getMessage());
				}
			}
		});
	}

	/**
	 * Connect to the server then register a new account with a username and password.<br>
	 * The username must contains only letters (uppercase or lowercase), numbers and whitespaces.
	 * @param username The new account username not in the server's account list
	 * @param password The new account's password
	 */
	private void register(String username, String password) {
		enableUI(false);
		SwingUtilities.invokeLater(() -> {
			connect();
			if (!s.isConnected()) {
				enableUI(true);
				return;
			}
			synchronized (dos) {
				try {
					dos.writeUTF("Register");
					dos.writeUTF(username);
					dos.writeUTF(password);
					dos.flush();

					String response = dis.readUTF();
					if (response.equals("Register success")) {
						JOptionPane.showMessageDialog(this,
								"Register succesfully. You'll now be logged in.");
						ChatFrame chatFrame = new ChatFrame(s, dis, dos, username);
						chatFrame.setVisible(true);
						this.dispose();
					} else {
						JOptionPane.showMessageDialog(this, response);
						enableUI(true);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(this,
							"An error has occurred: " + ex.getMessage());
				}
			}
		});
	}

	/**
	 * Convert an UTF-8 character array to byte array
	 * @param arr The character array to be converted
	 * @return The result byte array
	 */
	private static byte[] toBytes(char[] arr) {
		CharBuffer charBuffer = CharBuffer.wrap(arr);
		ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
		byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
				byteBuffer.position(), byteBuffer.limit());
		Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
		return bytes;
	}

	/**
	 * Hash a password returned by the password field into an SHA256-hashed string
	 * @param original The original text password as UTF-8 character array
	 * @return The hashed password string
	 */
	private static String hashPassword(char[] original) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] bytes = toBytes(original);
			String hashed = String.format("%064x", new BigInteger(1, digest.digest(bytes)));
			Arrays.fill(original, '\u0000'); // clear sensitive data
			Arrays.fill(bytes, (byte) 0); // clear sensitive data
			return hashed;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Set the UI state to enable or disable. The user cannot interact with UI components (except closing the frame) if it's disable.
	 * @param enable a flag to signal the UI to be enabled
	 */
	private void enableUI(boolean enable) {
		ipField.setEnabled(enable);
		portField.setEnabled(enable);
		usrField.setEnabled(enable);
		pwField.setEnabled(enable);
		regBtn.setEnabled(enable);
		loginBtn.setEnabled(enable);
	}
}
