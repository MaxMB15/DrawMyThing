import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class EnterUsernameDialog extends JDialog{

	private static final long serialVersionUID = 265257976511338758L;
	
	private JTextField entry;
	ObjectInputStream input;
	ObjectOutputStream output;
	public EnterUsernameDialog(JFrame f,ObjectInputStream in, ObjectOutputStream out){
		super(f,"Enter IP Address Prompt", true);
		input = in;
		output = out;
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		JPanel MainPane = new JPanel(new GridBagLayout());
		GridBagConstraints cs = new GridBagConstraints();
		cs.fill = GridBagConstraints.VERTICAL;
		cs.gridx = 0;
		cs.gridy = 0;
		MainPane.add(new JLabel("Username: "), cs);
		cs.gridx = 1;
		entry = new JTextField("Enter Username Here");
		entry.addKeyListener(new KeyListener(){

			@Override
			public void keyPressed(KeyEvent arg0) {}
			@Override
			public void keyReleased(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER){
					PassUsername();
				}
			}
			@Override
			public void keyTyped(KeyEvent arg0) {}
			
		});
		MainPane.add(entry, cs);
		cs.gridx = 0;
		cs.gridy = 1;
		cs.gridwidth = 2;
		JButton submit = new JButton("Pick Username");
		submit.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				PassUsername();
			}
			
		});
		MainPane.add(submit, cs);
		
		this.setContentPane(MainPane);
		this.setLocationRelativeTo(null);
		this.setSize(200, 90);
		this.setResizable(false);
		entry.selectAll();
	}
	public void PassUsername(){
		SendObject("!@#.NAMECHANGE."+entry.getText());
		SendObject("!@#.GETSAMENAME");
		String temp = (String)ReadObject();
		int q = 1;
		if(entry.getText().equals("[EMPTY]")||entry.getText().replaceAll(" ", "").equals("")){
			temp = "FALSE";
		}
		if(temp.equals("TRUE")){
			if(entry.getText().equals("Enter Username Here")){
				while(true){
					SendObject("!@#.NAMECHANGE."+"Player "+q);
					SendObject("!@#.GETSAMENAME");
					temp = (String)ReadObject();
					if(temp.equals("TRUE")){
						break;
					}else if(temp.equals("FALSE")){
						q++;
						continue;
					}
					System.out.println("BIIIGGGG EERRRROOORR");
					break;
				}
			}
			
			this.dispose();
		}else{
			entry.setBorder(BorderFactory.createLineBorder(Color.RED, 1, true));
			while(true){
				SendObject("!@#.NAMECHANGE."+"Player "+q);
				SendObject("!@#.GETSAMENAME");
				temp = ReadObject();
				if(temp.equals("TRUE")){
					break;
				}else if(temp.equals("FALSE")){
					q++;
					continue;
				}
				System.out.println("BIIIGGGG EERRRROOORR");
				break;
			}
		}
	}
	private void SendObject(String obj){
		try {
			System.out.println(obj);
			if(output==null){
				System.out.println("null error coming");
			}
			output.writeObject(obj);
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private String ReadObject(){
		try {
			return (String)input.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
