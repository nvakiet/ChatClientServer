package vn.edu.hcmus.student.sv19127191;

/**
 * vn.edu.hcmus.student.sv19127191<br/>
 * Created by Ngo Van Anh Kiet - MSSV: 19127191<br/>
 * Date 8/1/2022 - 2:44 PM<br/>
 * Description: Main class of the Client program. Creates the login frame on startup.<br/>
 */
public class Main {
	public static void main(String[] args) {
		try {
			LoginFrame frame = new LoginFrame();
			frame.pack();
			frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
