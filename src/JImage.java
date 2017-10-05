import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;


public class JImage extends JLabel{

	private static final long serialVersionUID = -2896828645234386923L;
	
	private BufferedImage image;
	
	public JImage(){
		super();
	}
	public JImage(BufferedImage bi){
		super();
		image = bi;
	}
	public BufferedImage getImage(){
		return image;
	}
	public void setImage(BufferedImage bi){
		image = bi;
		repaint();
	}
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.drawImage(image,0,0,getWidth(),getHeight(),null);
		g.dispose();
	}
}
