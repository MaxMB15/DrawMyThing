import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class MainMenu {
	JFrame Frame;
	JPanel Pane;
	public static void main(String[] ars){
		new MainMenu();
	}
	public MainMenu(){
		Frame = new JFrame("Draw My Thing");
		Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Frame.setBounds(300, 400, 250, 250);
		Pane = new JPanel(new GridLayout(2,0,50,10));
		JButton ServerSelect = new JButton("Connect to LAN Sever");
		ServerSelect.addActionListener(new ActionListener(){
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				GameSelect gs = new GameSelect(Frame);
				gs.setVisible(true);
			}
			
		});
		JButton ManualAddress = new JButton("Connect to IP Address");
		ManualAddress.addActionListener(new ActionListener(){
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				EnterIPDialog dia = new EnterIPDialog(Frame);
				dia.setVisible(true);
			}
			
		});
		Pane.add(ServerSelect);
		Pane.add(ManualAddress);
		Frame.add(Pane);
		Frame.setVisible(true);
		
	}
}
