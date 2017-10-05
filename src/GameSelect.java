import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GameSelect extends JDialog{
	
	private String IP = "localhost";
	private final JFrame Parent;
	private int PortAmount = 5;
	private Socket[] connections = new Socket[PortAmount];
	private ObjectInputStream[] input = new ObjectInputStream[PortAmount];
	private ObjectOutputStream[] output = new ObjectOutputStream[PortAmount];
	private JLabel[] LabelArr = new JLabel[PortAmount];
	private JLabel[] PaneArr = new JLabel[PortAmount];
	private JButton[] ButArr = new JButton[PortAmount];
	private int[] gameSizes = new int[PortAmount];
	private boolean ServerCreated = false;
	private int otherUsers = 0;
	private int BasePort = 3159;

	public GameSelect(JFrame f){
		super(f,"Server Select", true);
		Parent = f;
		init();
	}
	public GameSelect(JFrame f,String IP){
		super(f,"Server Select", true);
		Parent = f;
		this.IP = IP;
		init();
	}
	private void init(){
		JPanel mPane = new JPanel(new GridBagLayout());
		
		GridBagConstraints cs = new GridBagConstraints();
		cs.fill = GridBagConstraints.HORIZONTAL;
		
		
		
		
		BufferedImage bi = new BufferedImage(210,50,BufferedImage.TYPE_INT_ARGB);
		Graphics g;
		String inputHold = "";
		for(int i = 0;i<PortAmount;i++){
			try {
				connections[i] = new Socket(IP,Integer.parseInt(BasePort+""+i));
				//make output and input stream array...
				output[i] = new ObjectOutputStream(connections[i].getOutputStream());
				output[i].flush();
				input[i] = new ObjectInputStream(connections[i].getInputStream());
				System.out.println("Requesting Info...");
				inputHold = ReadObject(input[i]);
				if(inputHold.equals("STARTED")){
					
				}
				else if(inputHold.equals("FULL")){
					otherUsers = 5;
				}
				else{
					otherUsers = Integer.parseInt(inputHold);
				}
				System.out.println(otherUsers);
				System.out.println("Completed");
			} catch (NumberFormatException e) {
				
			} catch (UnknownHostException e) {
				
			} catch (IOException e) {
				
			}
			//set amount of Users per game
			gameSizes[i] = otherUsers;
			//Label
			LabelArr[i] = new JLabel("Server "+(1+i) +": Port "+BasePort+"" + i + " ");
			cs.gridx = 0;
			cs.gridy = i;
			cs.gridwidth = 1;
			mPane.add(LabelArr[i],cs);
			//Image Label
			bi = new BufferedImage(210,50,BufferedImage.TYPE_INT_ARGB);
			g = bi.getGraphics();
			g.setColor(this.getBackground());
			g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
			System.out.println((5-i));
			for(int z = 0, x = 10;z<otherUsers;z++,x+=(30+10)){
				if(inputHold.equals("STARTED")||inputHold.equals("FULL")){
					g.setColor(Color.RED);
				}
				else{
					g.setColor(Color.GREEN);	
				}
				g.fillRect(x, 5, 30, 40);
			}
			PaneArr[i] = new JLabel(new ImageIcon(bi));
			cs.gridx = 1;
			cs.gridwidth = 5;
			mPane.add(PaneArr[i],cs);
			//Button
			
			if(inputHold.equals("STARTED")){
				ButArr[i] = new JButton("In-Game");
				ButArr[i].setEnabled(false);
			}
			else if(inputHold.equals("FULL")){
				ButArr[i] = new JButton("Full");
				ButArr[i].setEnabled(false);
			}
			else if(otherUsers !=0){
				ButArr[i] = new JButton("Connect");
				ButArr[i].addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent arg0) {
						Parent.dispose();
						ServerCreated = true;				
						ButtonLogic((JButton)arg0.getSource());
					}
					
				});
			}
			else{
				ButArr[i] = new JButton("Create Server");
				ButArr[i].addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent arg0) {
						Parent.dispose();
						ServerCreated = false;
						ButtonLogic((JButton)arg0.getSource());
					}
					
				});
			}
			cs.gridx = 6;
			cs.gridwidth = 1;
			mPane.add(ButArr[i],cs);
			otherUsers = 0;
			inputHold = "";
		}
		JPanel bPane = new JPanel();
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
			
		});
		bPane.add(cancel);
		this.getContentPane().add(mPane,BorderLayout.CENTER);
		this.getContentPane().add(bPane, BorderLayout.PAGE_END);
		
		for(int i = 0;i<5;i++){
			System.out.println(connections[i]);
		}
		pack();
		
		this.setLocationRelativeTo(null);
		this.setResizable(false);
	}
	private void ButtonLogic(JButton b){
		int i = 0;
		if(b.equals(ButArr[0])){
			i = 0;
			MakeServerLogic(i);
		}
		else if (b.equals(ButArr[1])){
			i = 1;
			MakeServerLogic(i);
		}
		else if (b.equals(ButArr[2])){
			i = 2;
			MakeServerLogic(i);
		}
		else if (b.equals(ButArr[3])){
			i = 3;
			MakeServerLogic(i);
		}
		else if (b.equals(ButArr[4])){
			i = 4;	
			MakeServerLogic(i);
		}
	}
	private void MakeServerLogic(int i){
		if(!ServerCreated){
			new GameServer(Integer.parseInt(BasePort+""+i));
			try {
				connections[i] = new Socket("localhost",Integer.parseInt(BasePort+""+i));
				output[i] = new ObjectOutputStream(connections[i].getOutputStream());
				output[i].flush();
				input[i] = new ObjectInputStream(connections[i].getInputStream());
				ReadObject(input[i]);
				System.out.println("got Junk");
				SendObject("CONFIRM",output[i]);
				
			} catch (NumberFormatException e) {
				
			} catch (UnknownHostException e) {
				
			} catch (IOException e) {
				
			}
		}
		else{
			SendObject("CONFIRM",output[i]);
		}
		for(int z = 0;z<5;z++){
			if(z!=i){
				if(connections[z]!= null){
					System.out.println("Denying socket: "+z);
					SendObject("DENY",output[z]);
					try {
						input[z] = null;
						output[z] = null;
						connections[z].close();
						connections[z] = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		if(gameSizes[i] == 0){
			new WaitForGame(input[i],output[i],true,Integer.parseInt(BasePort+""+i),IP);
		}else{
			new WaitForGame(input[i],output[i],false,Integer.parseInt(BasePort+""+i),IP);
		}
	}
	private void SendObject(String obj,ObjectOutputStream out){
		try {
			out.writeObject(obj);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private String ReadObject(ObjectInputStream in){
		try {
			try {
				return (String)in.readObject();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
