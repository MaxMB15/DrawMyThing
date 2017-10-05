import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


class InputHandler implements Runnable{
	private Socket socket;
	private String temp = null;
	public ObjectInputStream input;
	public ObjectOutputStream output;
	public boolean NEWUSER = false;
	public String USERNAME = "NO ONE";
	public boolean READY = false;
	public boolean STARTREADY = false;
	public String KICKED = "FALSE";
	private GameServer gameServ;
	public InputHandler(Socket s,ObjectInputStream in, ObjectOutputStream out,GameServer gs){
		socket = s;
		gameServ = gs;
		output = out;
		input = in;
	}
	@Override
	public void run() {
		while(!socket.isClosed()){
			while(temp == null){
				if(socket.isClosed()){
					break;
				}
				//System.out.println("COMMAND");
				temp = ReadObject();
				if(temp.startsWith("null")){
					temp = null;
					System.out.println("Never Mind...");
					continue;
				}
				if(temp.startsWith("!@#.")){
					temp = temp.substring(4);
					//LOGIC HERE
					//SET
					if(temp.startsWith("NAMECHANGE.")){
						temp = temp.substring(11);
						USERNAME = temp;
						System.out.println("NEW USERNAME: " +USERNAME);
						AllSocketUpdate();
						temp = null;
						continue;
					}
					//SET
					if(temp.startsWith("SETUSERREADY.")){
						temp = temp.substring(13);
						if(temp.equals("TRUE")){
							READY = true;
							System.out.println(USERNAME + " IS NOW READY!");
						}
						else{
							READY = false;
							System.out.println(USERNAME + " IS NOW NOT READY!");
						}
						AllSocketUpdate();
						temp = null;
						continue;
					}
					//SET
					if(temp.startsWith("KICKUSER.")){
						temp = temp.substring(9);
						System.out.println("Kicking: "+temp);
						for(int s = gameServ.members.size()-1;s>=0;s--){
							if(gameServ.members.get(s).USERNAME.equals(temp)){
								gameServ.members.get(s).KICKED = "TRUE";
								gameServ.members.remove(s);
							}
						}
						AllSocketUpdate();
						temp = null;
						continue;
					}
					//SYSTEM DESTROY
					if(temp.startsWith("DESTROYSERVER")){
						System.out.println("GOOD BYE :(");
						gameServ.KeepGoing = false;
						for(int i = gameServ.members.size()-1;i>=0;i--){
							gameServ.members.get(i).KICKED = "SERVCLOSE";
							gameServ.members.remove(i);
						}
						gameServ.ServerClose();
						return;
					}
					//SYSTEM START
					if(temp.startsWith("STARTGAME")){
						System.out.println("\n\nSTARTING GAME!\n");
						gameServ.GameStarted = true;
						Thread waitForEveryone = new Thread(new Runnable(){
							@Override
							public void run() {
								while(!AllReadyCheck()){
									System.out.println("WAITING FOR PLAYERS TO BEGIN STARTING PROCCESS!!!\n");
								}
								ServerMember[] sm = new ServerMember[gameServ.members.size()];
								for(int z = 0;z<sm.length;z++){
									sm[z] = new ServerMember(gameServ.members.get(z).USERNAME,gameServ.members.get(z).socket,gameServ.members.get(z).input, gameServ.members.get(z).output,gameServ.portNum,z);
								}
								Thread gamerun = new Thread(new ServerGameLogic(sm));
								gamerun.start();
							}
						});
						waitForEveryone.start();
						temp = null;
						continue;
					}
					//BOOLEAN
					if(temp.startsWith("GETGAMESTART")){
						if(gameServ.GameStarted==true){
							SendObject("TRUE");
							//SEND PORT NUM
							STARTREADY = true;
							return;
						}
						else{
							SendObject("FALSE");
						}
						temp = null;
						continue;
					}
					//STRING
					if(temp.startsWith("GETMYUSERNAME")){
						SendObject(USERNAME);
						temp = null;
						continue;
					}
					//BOOLEAN
					if(temp.startsWith("GETSAMENAME")){
						boolean Different = true;
						for(int i = 0;i<gameServ.members.size();i++){
							if(USERNAME.equals(gameServ.members.get(i).USERNAME)&&!equals(gameServ.members.get(i))){
								Different = false;
							}
						}
						if(Different){
							SendObject("TRUE");
						}
						else{
							SendObject("FALSE");
						}
						temp = null;
						continue;
					}
					//INT
					if(temp.startsWith("GETPLAYERNUM")){
						for(int z = 0;z<gameServ.members.size();z++){
							if(gameServ.members.get(z).equals(this)){
								SendObject(""+z);
							}
						}
					}
					//BOOLEAN[]
					if(temp.startsWith("GETALLUSERREADY")){
						String AllReady = "";
						for(int y = 0;y<gameServ.members.size();y++){
							if(gameServ.members.get(y).READY){
								AllReady+="True";
							}
							else{
								AllReady+="False";
							}
							AllReady+="%";
						}
						for(int y = 0;y<5-gameServ.members.size();y++){
							AllReady +=" %";
						}
						AllReady = AllReady.substring(0, AllReady.toCharArray().length-1);
						SendObject(AllReady);
						temp = null;
						continue;
					}
					//BOOLEAN
					if(temp.startsWith("GETUSERDIFF")){
						if(NEWUSER){
							SendObject("TRUE");
							NEWUSER = false;
						}
						else{
							SendObject("FALSE");
						}
						temp = null;
						continue;
					}
					//BOOLEAN
					if(temp.startsWith("GETUSERGONE")){
						SendObject(KICKED);
						if(KICKED.startsWith("TRUE")||KICKED.startsWith("SERVCLOSE")){
							try {
								this.socket.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
							return;
						}
						temp = null;
						continue;
					}
					//STRING[]
					if(temp.startsWith("GETUSERNAMES")){
						String AllNames = "";
						for(int y = 0;y<gameServ.members.size();y++){
							AllNames+=gameServ.members.get(y).USERNAME;
							AllNames+="%";
						}
						for(int y = 0;y<5-gameServ.members.size();y++){
							AllNames +="[EMPTY]%";
						}
						AllNames = AllNames.substring(0, AllNames.toCharArray().length-1);
						SendObject(AllNames);
						temp = null;
						continue;
					}
					System.out.println("No result? Data was: "+temp);
					temp = null;
					continue;
				}
				System.out.println("No COM result? Data was: "+temp);
				
			}
			
		}
	}
	private void AllSocketUpdate(){
		for(int y = 0;y<gameServ.members.size();y++){
			gameServ.members.get(y).NEWUSER = true;
		}
	}
	public boolean equals(InputHandler h){
		return h.socket.equals(this.socket);
	}
	
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
				Object o = input.readObject();
				return (String)o;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		return null;
	}
	private int allReady = 0;
	private boolean AllReadyCheck(){
		allReady = 0;
		for(int i = 0;i<gameServ.members.size();i++){
			allReady++;
			if(gameServ.members.get(i).STARTREADY){
				allReady--;
			}
		}
		if(allReady==0){
			return true;
		}
		return false;
	}
}