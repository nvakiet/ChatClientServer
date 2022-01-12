package vn.edu.hcmus.student.sv19127191;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * vn.edu.hcmus.student.sv19127191<br/>
 * Created by Ngo Van Anh Kiet - MSSV: 19127191<br/>
 * Date 12/1/2022 - 7:48 AM<br/>
 * Description: ...<br/>
 */
public class ChatFrame extends JFrame {
	Socket s;
	DataInputStream dis;
	DataOutputStream dos;


	public ChatFrame() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
}
