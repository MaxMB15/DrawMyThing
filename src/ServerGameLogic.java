import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Timer;


public class ServerGameLogic implements Runnable{
	private ServerMember[] members;
	private String[] AllWords;
	private String WordToGuess = "";
	private int SecondsLeft = 60;
	private Timer Countdown;
	//private boolean GameRunning = true;
	private int NumberOfRounds = 2;
	public ServerGameLogic(ServerMember[] membs){
		members = membs;
		for(ServerMember sm : members){
			SendObject(sm,""+sm.PlayerID);
		}
		RandomizeMemberOrder();
		AllWords = FileTextReader.ReadTextByLines("WordList.txt");
		WordToGuess = AllWords[Math.round((float)(Math.random()*(AllWords.length-1)))];
		System.out.println("First Word: "+WordToGuess);
		Countdown = new Timer(1000,new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ClockEvent();
			}
		});
		Countdown.setRepeats(true);
		Countdown.start();
	}
	private void RandomizeMemberOrder(){
		ArrayList<Integer> order = new ArrayList<Integer>();
		ServerMember[] temp = new ServerMember[members.length];
		for(int i = 0;i<members.length;i++){
			order.add(Math.round((float)(Math.random()*i)),i);
			temp[i] = members[i];
		}
		for(int z = 0;z<members.length;z++){
			members[z] = temp[order.get(z)];
		}
	}
	private void ClockEvent(){
		if(SecondsLeft>0){
			SecondsLeft--;
			System.out.println(SecondsLeft);
		}
	}
	@Override
	public void run() {
		for(int i = 0;i<NumberOfRounds;i++){
			Round();
		}
	}
	private void Round(){
		String InputHolder = null;
		BufferedImage ScreenToProject = null;
		
		//For Every Player
		for(int playerUp = 0;playerUp<members.length;playerUp++){
			//Tells User who is up
			for(int player = 0;player<members.length;player++){
				if(player == playerUp){
					SendObject(members[player],"!@#.YOURUP");
					members[player].guessedTheWord = true;
				}
				else{
					members[player].guessedTheWord = false;
					SendObject(members[player],"!@#.YOURNOTUP");
				}
			}
			//For Drawer:
			SendObject(members[playerUp],"!@#.SETWORDTOGUESS."+WordToGuess);
			//Till Time Runs Out
			while(SecondsLeft >0){
				sleep(100);
				//Update Every Player
				for(int player = 0;player<members.length;player++){
					//Get Image from current Player
					if(player == playerUp){
						//Get Image
						SendObject(members[player],"!@#.GIVEDISPLAY");
						ScreenToProject = members[player].imageSender.ReceiveImage();
					}else{
						if(ScreenToProject!=null){
							//Give Image
							SendObject(members[player],"!@#.GETDISPLAY");
							members[player].imageSender.SendImage(ScreenToProject);
						}
					}
					SendObject(members[player],"!@#.UPDATETIME."+SecondsLeft);
					SendObject(members[player],"!@#.GETSENDBUFFER");
					InputHolder = ReadObject(members[player]);
					if(!InputHolder.equals("")){
						if(InputHolder.equalsIgnoreCase(WordToGuess)){
							if(!members[player].username.equals(members[playerUp].username)&&!members[player].guessedTheWord){
								members[player].score++;
								members[player].guessedTheWord = true;
								if(SecondsLeft>30){
									SecondsLeft = 20;
								}
								//If all players guessed word
								if(AllPlayersGotWord()){
									SecondsLeft = 0;
								}
								for(ServerMember sender : members){
									SendObject(sender,"!@#.SENDMESSAGE."+members[player].username+" has guessed the word!\nTheir Score is:"+members[player].score);
								}
							}
						}
						else{
							for(ServerMember sender : members){
								SendObject(sender,"!@#.SENDMESSAGE."+members[player].username+"."+InputHolder);
							}
						}
					}
					InputHolder = "";
				}
			}
			//After Player
			SecondsLeft = 60;
			Countdown.stop();
			WordToGuess = AllWords[Math.round((float)(Math.random()*(AllWords.length-1)))];
			//Send Score msgs
			for(ServerMember sender : members){
				SendObject(sender,"!@#.SENDMESSAGE.~~~NEXT PLAYER UP IN 5 SECONDS!~~~\nTHE SCORES ARE:\n");
				for(int p = 0;p<members.length;p++){
					SendObject(sender,"!@#.SENDMESSAGE."+members[p].username+" ~ "+members[p].score);			
				}
				SendObject(sender,"!@#.SENDMESSAGE.\n\n" );
			}
			//Wait 5 seconds
			sleep(5000);
			Countdown.start();
		}
		//Game over
		for(ServerMember sender : members){
			SendObject(sender,"!@#.SENDMESSAGE.~~~GAME OVER!~~~\nTHE SCORES FINAL SCORES ARE:\n");
			for(int p = 0;p<members.length;p++){
				SendObject(sender,"!@#.SENDMESSAGE."+members[p].username+" ~ "+members[p].score);			
			}
		}
		Countdown.stop();
	}
	private void SendObject(ServerMember sm, String obj){
		try {
			sm.output.writeObject(obj);
			sm.output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private String ReadObject(ServerMember sm){
		try {
			try {
				return (String)sm.input.readObject();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	private void sleep(int i){
		try {
			Thread.sleep((long)i);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	private boolean AllPlayersGotWord(){
		for(int i = 0;i<members.length;i++){
			if(!members[i].guessedTheWord){
				return false;
			}
		}
		return true;
	}
}
