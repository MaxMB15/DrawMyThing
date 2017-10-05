import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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
import javax.swing.*;


public class ImageSenderTest implements Runnable, WindowListener{
	private int FrameRate = 80;
	private ServerSocket server = null;
	public static void main(String[] args){
		new ImageSenderTest();
	}
	public ImageSenderTest(){
		//Server
		Thread serv = new Thread(this);
		serv.start();
		try {
			Robot rob = new Robot();
			@SuppressWarnings("resource")
			Socket client = new Socket("localhost",12222);
			ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
			Rectangle rect = new Rectangle(0,0,1200,800);
			while(true){
				Thread.sleep(FrameRate);
				out.writeObject(ImageToBytes(rob.createScreenCapture(rect)));
				out.flush();
				System.gc();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		try {
			server = new ServerSocket(12222);
			Socket client = server.accept();
			ObjectInputStream in = new ObjectInputStream(client.getInputStream());
			JFrame frame = new JFrame("APP TEST");
			JPanel pane = new JPanel();
			frame.setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(pane);
			frame.setVisible(true);
			while(true){
				Thread.sleep(FrameRate);
				pane.getGraphics().drawImage(BytesToImage((byte[])(in.readObject())), 50, 50, null);
				System.gc();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public static byte[] ImageToBytes(BufferedImage bi){
	    try{
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        ImageIO.write(bi, "PNG", baos);
	        return baos.toByteArray();
	    } catch (IOException e){
	    	e.printStackTrace();
	    }
	    return null;
	}
	private BufferedImage BytesToImage(byte[] data){
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			return ImageIO.read(bais);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowClosed(WindowEvent arg0) {
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}