import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class ServerMember{
	public ObjectInputStream input;
	public ObjectOutputStream output;
	public String username;
	public Socket socket;
	public ImageSender imageSender;
	public int PlayerID;
	public int score = 0;
	public boolean guessedTheWord = false;
	public ServerMember(String usern,Socket sock, ObjectInputStream in, ObjectOutputStream out, int PORT, int PlayerNum){
		username = usern;
		socket = sock;
		input = in;
		output = out;
		PlayerID = PlayerNum;
		PORT = PortAlgorithm(PORT,PlayerNum);
		//System.out.println(PORT);
		imageSender = new ImageSender("JPEG",PORT,"SERVER");
	}
	static int PortAlgorithm(int OrPort, int ID){
		int temp = OrPort;
		OrPort = OrPort/100;
		OrPort*=100;
		int lastDig = Integer.parseInt((""+temp).substring((""+temp).length()-1, (""+temp).length()));
		lastDig*=10;
		OrPort+=lastDig;
		OrPort+=ID;
		return OrPort;
	}
}
