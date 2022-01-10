package vn.edu.hcmus.student.sv19127191;

import vn.edu.hcmus.student.sv19127191.ui.MainFrame;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

/**
 * vn.edu.hcmus.student.sv19127191<br/>
 * Created by Ngo Van Anh Kiet - MSSV: 19127191<br/>
 * Date 8/1/2022 - 2:43 PM<br/>
 * Description: ...<br/>
 */
public class Main {
	public static void main(String[] args) {
		try {
			MainFrame mainFrame = new MainFrame();
			mainFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
			mainFrame.pack();
			mainFrame.setResizable(false);
			mainFrame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
