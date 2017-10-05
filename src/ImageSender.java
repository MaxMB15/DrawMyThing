import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;


public class ImageSender {
	private String ImageType = "JPEG";
	private ObjectInputStream in = null;
	private ObjectOutputStream out = null;
	private ByteArrayInputStream BAIS = null;
	private ByteArrayOutputStream BAOS = null;
	//Client
	public ImageSender(String Quality,String IP, int PORT, String TYPE){
		setQuality(Quality);
		System.out.println("CLIENT PORT: "+PORT);
		boolean connected = false;
		while(!connected){
			try {
				@SuppressWarnings("resource")
				Socket client = new Socket(IP,PORT);
				out = new ObjectOutputStream(client.getOutputStream());
				out.flush();
				in = new ObjectInputStream(client.getInputStream());
				connected = true;
				System.out.println("\n\n\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! - TRYING TO CONNECT TO PORT: "+ PORT+"\n\n");
			} catch (UnknownHostException e) {
				connected = false;
				System.out.println("Cannot Connect to IP/PORT: ("+IP+", "+PORT+")");
			} catch (IOException e) {
				connected = false;
				System.out.println("Cannot Connect to IP/PORT: ("+IP+", "+PORT+")");
			}
		}
	}
	//Server
	public ImageSender(String Quality, final int PORT, String TYPE){
		setQuality(Quality);
		System.out.println("SERVER PORT: "+PORT);
		new Thread(new Runnable() {
		    @Override
		    public void run() {
		        try {
		        	@SuppressWarnings("resource")
					ServerSocket server = new ServerSocket(PORT);
					Socket client = server.accept();
					out = new ObjectOutputStream(client.getOutputStream());
					out.flush();
					in = new ObjectInputStream(client.getInputStream());
					System.out.println("\n\n\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! - SYNCED TO PORT: "+PORT+"\n\n");
				} catch (IOException e) {
					e.printStackTrace();
				}  
		    }
		}).start();
	}
	public void setQuality(String Quality){
		Quality = Quality.toUpperCase();
		if(Quality.equals("JPEG")){
			Quality = "JPEG";
		}
		else if(Quality.equals("PNG")){
			Quality = "PNG";
		}
		else if(Quality.equals("JPG")){
			Quality = "JPEG";
		}
	}
	public void SendImage(BufferedImage bi){
		try {
			Thread.sleep(100);
			BAOS = new ByteArrayOutputStream();
			ImageIO.write(bi, ImageType, BAOS);
			out.writeObject(BAOS.toByteArray());
			out.flush();
			System.gc();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public BufferedImage ReceiveImage(){
		try {
			Thread.sleep(100);
			Object o = in.readObject();
			System.out.println(o);
			BAIS = new ByteArrayInputStream((byte[])(o));
			System.gc();
			return ImageIO.read(BAIS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
