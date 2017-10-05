import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class WaitForGame implements Runnable,WindowListener{
	
	/**
	 *When you chose the game, you will be prompted with this screen where you have to wait for others to join 
	 *or just start the game when ever you want.
	 * 
	 *The server will be run/hosted by the person who hits the "Create Server" button 
	 * 
	 *There will be a button that gives over start server privileges
	 *
	 *There must also be a kick function that allows the moderator to control who is in the game
	 * 
	 * When the game starts, The server will initiate its game logic where all input/output is sent to the other
	 * clients
	 * 
	 * There will be no need to have a update function inside the DrawMyThingMain Class
	 * 
	 * The server will be in charge of updating the users
	 * 
	 * Ready-up function
	 * 
	 * @param Host
	 */
	//CONSTRUCTOR VARS
	private JFrame Frame;
	private JPanel CenterPane;
	private JPanel BottomPane;
	private JPanel MainPane;
	private String[] usernameStrings;
	private JButton readyButton;
	private String[] readyStrings;
	private boolean ready = false;
	private JButton startButton;
	private JButton[] kickButtons;
	private MaxTable table;
	private int currentCol = 0;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private String inputholder = "ERROR";
	private boolean Host = false;
	private int PORT;
	private String IP;
	//UPDATE VARS
	private boolean ServerActive = true;
	
	public WaitForGame(ObjectInputStream in, ObjectOutputStream out, boolean host, int port, String ip){
		//SET TO GLOBALS
		Host = host;
		output = out;
		input = in;
		PORT = port;
		IP = ip;
		
		//MAKE NEW FRAME
		Frame = new JFrame("Waiting for Players...");
		Frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		Frame.addWindowListener(this);
		Frame.setLocationRelativeTo(null);
		Frame.setResizable(false);
		
		//MAKE NEW JPANELS
		CenterPane = new JPanel();
		BottomPane = new JPanel();
		MainPane = new JPanel();
		
		//GET THE USER'S NAME
		//THIS SHOULD SEND OVER USERNAME TO SERVER DIRECTLY
		EnterUsernameDialog getplayername = new EnterUsernameDialog(Frame,input,output);
		getplayername.setVisible(true);
		
		//MAKE TABLE
		if(Host){
			String[] Titles = {"USERNAME", "READY","KICK"};
			table = new MaxTable(5,3,Titles);
			table._setInsets(new Insets(2,5,2,5));
			Frame.setSize(400, 240);
		}
		else{
			String[] Titles = {"USERNAME", "READY"};
			table = new MaxTable(5,2,Titles);	
			table._setInsets(new Insets(6,5,6,5));
			Frame.setSize(350, 240);
		}
		
		//USERNAMES
		System.out.println("Getting usernames...");
		SendObject("!@#.GETUSERNAMES");
		
		//TRANSFER USERNAMES TO STRING ARRAY HOLDER
		inputholder = ReadObject();
		System.out.println("ALL THE USERNAMES: "+inputholder+"\n");
		usernameStrings = inputholder.split("%");
		table._setColumn(0, usernameStrings);
		
		//ADD TABLE TO COMPONENTS
		CenterPane.add(table);
		table.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		//GET MY ROW
		SendObject("!@#.GETMYUSERNAME");
		inputholder = ReadObject();
		for(int i = 0;i<usernameStrings.length;i++){
			if(usernameStrings[i].equals(inputholder)){
				currentCol = i;
				break;
			}
		}
		table._getContent(currentCol, 0).setForeground(new Color(153,0,153));
		
		//READYS
		System.out.println("Getting ready status from all players...");
		SendObject("!@#.GETALLUSERREADY");
		
		//TRANSFER READY STATUS FROM ALL PLAYERS TO STRING ARRAY HOLDER
		inputholder = ReadObject();
		System.out.println("ALL THE READYS: "+inputholder+"\n");
		readyStrings = inputholder.split("%");
		table._setColumn(1, readyStrings);
		
		//CHANGE READY COLOR
		for(int i = 0;i<readyStrings.length;i++){
			if(readyStrings[i].startsWith("True")){
				table._getContent(i, 1).setForeground(Color.GREEN);
			}
			else if(readyStrings[i].startsWith("False")){
				table._getContent(i, 1).setForeground(Color.RED);
			}
		}
		
		//MAKE READY BUTTON
		readyButton = new JButton("Ready");
		readyButton.setForeground(Color.RED);
		readyButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ReadyButtonLogic();
			}
		});
		
		//ADD BUTTON TO BOTTOM PANE
		BottomPane.add(readyButton);
		
		//HOST SPECIAL ABILTIES
		if(Host){
			//MAKE KICK BUTTONS
			kickButtons = new JButton[5];
			for(int i = 0;i<5;i++){
				kickButtons[i] = new JButton("KICK");
				kickButtons[i].addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						for(int z = 0;z<kickButtons.length;z++){
							if(e.getSource().equals(kickButtons[z])){
								KickButtonLogic(z);	
								break;
							}
						}
					}
				});
				kickButtons[i].setEnabled(false);
			}
			table._setColumn(2, kickButtons);
			
			//MAKE START BUTTON
			startButton = new JButton("Start");
			startButton.setEnabled(false);
			startButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					StartLogic();
				}
			});
			
			//ADD BUTTON TO BOTTOM PANE
			BottomPane.add(startButton);
		}
		
		//SET THE CONTENT PANE OF FRAME
		Frame.setContentPane(MainPane);
		
		//ADD PANES TO CONTENT PANE
		MainPane.add(CenterPane,BorderLayout.NORTH);
		MainPane.add(BottomPane,BorderLayout.SOUTH);
		
		//MAKE FRAME VISABLE
		Frame.setVisible(true);
		
		//START UPDATING
		Thread UpdateUsers = new Thread(this);
		UpdateUsers.start();
	}
	private void KickButtonLogic(int row){
		SendObject("!@#.KICKUSER."+usernameStrings[row]);
	}
	private void StartLogic(){
		SendObject("!@#.STARTGAME");
	}
	private void ReadyButtonLogic(){
		if(ready){
			readyButton.setForeground(Color.RED);
			ready = false;
			SendObject("!@#.SETUSERREADY.FALSE");
		}
		else{
			readyButton.setForeground(Color.GREEN);
			ready = true;
			SendObject("!@#.SETUSERREADY.TRUE");
		}
	}
	private void Close(){
		if(Host){
			SendObject("!@#.DESTROYSERVER");
		}
		else{
			SendObject("!@#.GETMYUSERNAME");
			SendObject("!@#.KICKUSER."+ReadObject());
		}
		Frame.dispose();
	}
	@Override
	public void windowActivated(WindowEvent arg0) {}
	@Override
	public void windowClosed(WindowEvent arg0) {}
	@Override
	public void windowClosing(WindowEvent arg0) {
		ServerActive = false;
		Close();
	}
	@Override
	public void windowDeactivated(WindowEvent arg0) {}
	@Override
	public void windowDeiconified(WindowEvent arg0) {}
	@Override
	public void windowIconified(WindowEvent arg0) {}
	@Override
	public void windowOpened(WindowEvent arg0) {}
	private void SendObject(String obj){
		try {
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
		System.out.println("ERROR! CANNOT READ OBJECT! CLOSING NOW!");
		Close();
		return null;
	}
	@Override
	public void run() {
		//RUN WHILE SERVER OPEN
		while(ServerActive){
			//SLEEP EVERY LOOP
			ThreadSleep(100);
			
			//CHECK IF GAME HAS STARTED
			SendObject("!@#.GETGAMESTART");
			inputholder = ReadObject();
			if(inputholder.startsWith("TRUE")){
				inputholder = ReadObject();
				Frame.dispose();
				Thread transferThread = new Thread(new DrawMyThingMain(input, output,PORT,IP,Integer.parseInt(inputholder)));
				transferThread.start();
				return;
			}
			
			//CHECK FOR ANY UPDATES
			SendObject("!@#.GETUSERDIFF");
			inputholder = ReadObject();
			
			//IF USER NEEDS UPDATE
			if(inputholder.startsWith("TRUE")){
				//REFRESH USERNAMES
				SendObject("!@#.GETUSERNAMES");
				inputholder = ReadObject();
				usernameStrings = inputholder.split("%");
				table._setColumn(0, usernameStrings);
				
				//REFRESH CURRENT ROW
				SendObject("!@#.GETMYUSERNAME");
				inputholder = ReadObject();
				for(int i = 0;i<usernameStrings.length;i++){
					if(usernameStrings[i].equals(inputholder)){
						currentCol = i;
						break;
					}
				}
				table._getContent(currentCol, 0).setForeground(new Color(153,0,153));
				
				//REFRESH READYS
				SendObject("!@#.GETALLUSERREADY");
				inputholder = ReadObject();
				readyStrings = inputholder.split("%");
				table._setColumn(1, readyStrings);
				
				//CHANGE READY COLOR
				for(int i = 0;i<readyStrings.length;i++){
					if(readyStrings[i].startsWith("True")){
						table._getContent(i, 1).setForeground(Color.GREEN);
					}
					else if(readyStrings[i].startsWith("False")){
						table._getContent(i, 1).setForeground(Color.RED);
					}
				}
				
				//REFRESH HOST PRIVS
				if(Host){
					
					//REFRESH KICK BUTTONS
					for(int i = 1;i<usernameStrings.length;i++){
						if(usernameStrings[i].equals("[EMPTY]")){
							kickButtons[i].setEnabled(false);
						}
						else{
							kickButtons[i].setEnabled(true);
						}
					}
					
					//REFRESH START BUTTON
					if(AbleToStart()){
						startButton.setEnabled(true);
					}
					else{
						startButton.setEnabled(false);
					}
				}
				table.revalidate();
				ThreadSleep(400);
			}
			
			//CHECK IF KICKED
			SendObject("!@#.GETUSERGONE");
			inputholder = ReadObject();
			if(inputholder.startsWith("TRUE")){
				if(ServerActive){
					ServerActive = false;
					Frame.dispose();
					JOptionPane.showMessageDialog(null,"You have been kicked!");
					new MainMenu();
				}
			}
			else if(inputholder.startsWith("SERVCLOSE")){
				ServerActive = false;
				Frame.dispose();
				JOptionPane.showMessageDialog(null,"The server has been closed!");
				new MainMenu();
			}
		}
	}
	private void ThreadSleep(int i){
		try {
			Thread.sleep((long)i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	int NumOfPlayers = 0;
	int ReadyCheck = 0;
	private boolean AbleToStart(){
		ReadyCheck = 0;
		NumOfPlayers = 0;
		for(int i = 0;i<usernameStrings.length;i++){
			if(!usernameStrings[i].startsWith("[EMPTY]")){
				ReadyCheck++;
				NumOfPlayers++;
				if(readyStrings[i].startsWith("True")){
					ReadyCheck--;
				}
			}
		}
		if((ReadyCheck==0)&&(NumOfPlayers>=2)){
			return true;
		}
		else{
			return false;
		}
	}
}
