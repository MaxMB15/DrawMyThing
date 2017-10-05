import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class GameServer implements Runnable{
	private ServerSocket server;
	private Socket user;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	public int portNum = 0;
	public boolean KeepGoing = true;
	public boolean GameStarted = false;
	public ArrayList<InputHandler> members = new ArrayList<InputHandler>();
	public GameServer(int Port){
		portNum = Port;
		Thread FindClients = new Thread(this);
		FindClients.start();
	}
	@Override
	public void run() {
		while(portNum == 0){
			System.out.println("runn");
		}
		System.out.println("Done");
		try {
			server = new ServerSocket(portNum);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//add players
		InputHandler tempHandle;
		String inputholder = "null";
		while(KeepGoing){
			
				try {
					user = server.accept();
					input = new ObjectInputStream(user.getInputStream());
					output = new ObjectOutputStream(user.getOutputStream());
					System.out.println("One Connected");
					System.out.println("Through");
					tempHandle = new InputHandler(user,input,output,this);
					if(GameStarted){
						SendObject(tempHandle,"STARTED");
					}
					else if(members.size()==5){
						SendObject(tempHandle,"FULL");
					}
					else{
						SendObject(tempHandle,(String)(""+members.size()));
					}
					inputholder = null;
					while(inputholder == null){
						System.out.println("Waiting");
						inputholder = ReadObject(tempHandle);
						if(inputholder.startsWith("null")){
							inputholder = null;
						}
					}
					System.out.println("DATA:");
					System.out.println(inputholder);
					if(inputholder.startsWith("CONFIRM")){
						System.out.println("IIITITITITIGETSSSHEERERE!!!");
						members.add(tempHandle);
						Thread Handler = new Thread(members.get(members.size()-1));
						Handler.start();
					}
					else if(inputholder.startsWith("DENY")){
						System.out.println("DENIED");
					}
					

				} catch (IOException e1) {
				
				}
				
		}
	}
	public void ServerClose(){
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void SendObject(InputHandler sm, String obj){
		try {
			sm.output.writeObject(obj);
			sm.output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private String ReadObject(InputHandler sm){
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
}