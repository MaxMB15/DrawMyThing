import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultCaret;
import javax.imageio.ImageIO;

public class DrawMyThingMain implements MouseListener, MouseMotionListener,ChangeListener,KeyListener,Runnable{
	/**COMPONENTS**/
		//Main Frame and Panel
		private JFrame Frame;
		private JPanel MainPane;
		//Tools Pane
		private JPanel ToolPanel;
			private JButton[] SelectorButtons;
				private JPanel ButtonPanel;
			private List<BufferedImage> ImageHistory;
			private List<BufferedImage> ImageHistoryCopy;
			private BufferedImage CurrentImage;
			private JSlider BrushSize;
			private int BrushRad;
			private String CurrentTool;
			private JImage ColorSelectorComponent;
				private BufferedImage ColorSelectorImage;
				private Rectangle ColorSelectorScreenBounds;
			private JImage ColorPickerComponent;
				private BufferedImage ColorPickerImage;
				private Color ColorPicker;
			Queue<Point> queue = new LinkedList<Point>();
				private boolean[][] PointCheck;
				private Point currentQueue = new Point();
				Color ColorOverlap = new Color(0,0,0,1);
		//Draw Pane
		private JPanel DrawPanel;
			private JImage DrawPaneComponent;
				private Point DrawPaneSize;
				private BufferedImage DrawPaneImage;
				private Rectangle DrawPanelScreenBounds;
		//Chat Pane
		private JPanel TalkPanel;
			private Rectangle TalkPanelScreenBounds;
			private JTextArea TextArea;
			private JTextField WriteTextField;
			private JScrollPane ScrollPane;
			private JTextArea ScoreImage;
			private JTextArea WordToGuessImage;
			private JTextArea TimerImage;
	
	/**System Properties:**/
		private boolean CurrentPlayer = false;
		private Rectangle ScreenSize;
		private Point2D.Float ScreenScaling;
		private Color BackColor;
		private boolean DEBUG = false;
		private Point MousePos;
			private Point MouseRelitiveToDrawPane;
			private Point MouseRelitiveToColorSelector;
			private Point MouseRelitiveToDrawPaneOld;
		private Point PrevMousePos;
		private boolean GameisRunning = true;
	
	/**Buffers**/
		private BufferedImage ImageBuffer;
		
	/**Graphics**/
		private Graphics g;
		
	/**Layouts**/
		private GridBagConstraints cs;
		
	/**Sockets**/
		private ObjectInputStream input;
		private ObjectOutputStream output;
			private String SendBuffer = "";
		private int ImagePORT;
		private ImageSender imageSender;
		
	/**Draw Logic Variables**/
		private float m1, m2, x1, x2, y1, y2;
	
	public DrawMyThingMain(ObjectInputStream in, ObjectOutputStream out, int PORT,String IP,int ID){
		
		input = in;
		output = out;
		ImagePORT = ServerMember.PortAlgorithm(PORT, ID);
		//printf(ImagePORT);
		imageSender = new ImageSender("JPEG",IP,ImagePORT,"CLIENT");
		
		/**Instantiate System Properties**/
		
		ScreenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		System.out.println(ScreenSize);
		ScreenScaling = new Point2D.Float(Toolkit.getDefaultToolkit().getScreenSize().width/1920.f, Toolkit.getDefaultToolkit().getScreenSize().height/1080.f);
		System.out.println(ScreenScaling);
		BackColor = new Color(0.f,0.8f,1.f);
		MousePos = new Point(0,0);
		PrevMousePos = new Point(0,0);
		MouseRelitiveToDrawPane = new Point(0,0);
		MouseRelitiveToDrawPaneOld = new Point(0,0);
		MouseRelitiveToColorSelector = new Point(0,0);
		ColorSelectorScreenBounds = new Rectangle(0,0,50,50);
		DrawPanelScreenBounds = new Rectangle(0,0,50,50);
		
		
		/**Instantiate Frame and Main Pane**/
	
		Frame = new JFrame("Draw My Thing");
		Frame.setBounds(ScreenSize);
		Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Frame.setResizable(false);
		Frame.setUndecorated(true);
		Frame.addMouseListener(this);
		Frame.addMouseMotionListener(this);
		Frame.setLayout(new GridBagLayout());
		
		MainPane = new JPanel();
		MainPane.setBounds(ScreenSize);
		MainPane.setLayout(new GridBagLayout());

		
		/**Instantiate Tool Pane**/
		
		ToolPanel = new JPanel(new GridBagLayout());
		if(DEBUG)
			ToolPanel.setBackground(Color.GREEN);
		else
			ToolPanel.setBackground(new Color(0,0,0,0));
		ImageHistory = new ArrayList<BufferedImage>();
		ImageHistoryCopy = new ArrayList<BufferedImage>();
		CurrentTool = "BRUSH";
		
		ColorPicker = new Color(1.f,0.f,0.f,1.f);
		try {ColorSelectorImage = ImageIO.read(this.getClass().getResourceAsStream("ColorPicker.png"));} catch (IOException e) {e.printStackTrace();}
		ColorSelectorComponent = new JImage(ColorSelectorImage);
		cs = new GridBagConstraints();
		cs.fill = GridBagConstraints.BOTH;
		cs.gridx = 0;
		cs.gridy = 0;
		cs.weightx = .6;
		ToolPanel.add(ColorSelectorComponent, cs);
		
		ColorPickerImage = new BufferedImage(50,ColorSelectorImage.getHeight(),BufferedImage.TYPE_INT_ARGB);
		g = ColorPickerImage.getGraphics();
		g.setColor(ColorPicker);
		g.fillRect(0, 0, ColorPickerImage.getWidth(), ColorPickerImage.getHeight());
		g.dispose();
		ColorPickerComponent = new JImage(ColorPickerImage);
		cs.gridx = 1;
		cs.gridy = 0;
		cs.weightx = .03;
		ToolPanel.add(ColorPickerComponent, cs);
		
		ButtonPanel = new JPanel(new GridLayout(2,3,2,2));
		SelectorButtons = new JButton[6];
		MakeButtons();
		cs.gridx = 2;
		cs.gridy = 0;
		cs.weightx = .2;
		ToolPanel.add(ButtonPanel, cs);
		
		BrushSize = new JSlider(JSlider.VERTICAL, 4,30,8);
		BrushSize.setMinorTickSpacing(1);
		BrushSize.setMajorTickSpacing(7);
		BrushSize.addChangeListener(this);
		try {BrushSize.setBackground(new Color(ImageIO.read(this.getClass().getResourceAsStream("Brush.png")).getRGB(0, 0)));} catch (IOException e) {System.out.println("Slider BackColor Failure");}
		BrushRad = 8;
		cs.gridx = 3;
		cs.gridy = 0;
		cs.weightx = .03;
		ToolPanel.add(BrushSize, cs);
		
		
		/**Instantiate Draw Pane**/
		
		DrawPanel = new JPanel(new GridBagLayout());
		if(DEBUG)
			DrawPanel.setBackground(Color.RED);
		else
			DrawPanel.setBackground(new Color(0,0,0,0));
		DrawPaneSize = new Point(sx(1514),sy(759));
		DrawPaneImage = new BufferedImage(DrawPaneSize.x,DrawPaneSize.y,BufferedImage.TYPE_INT_ARGB);
		g = DrawPaneImage.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, DrawPaneSize.x, DrawPaneSize.y);
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, DrawPaneSize.x-1, DrawPaneSize.y-1);
		g.dispose();
		DrawPaneComponent = new JImage(DrawPaneImage);
		//DrawPaneComponent.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		
		cs = new GridBagConstraints();
		cs.insets = new Insets(sy(50),sx(100),sy(10),sx(100));
		cs.fill = GridBagConstraints.BOTH;
		
		cs.gridx = 0;
		cs.gridy = 0;
		cs.weightx = 1;
		cs.weighty = 1;
		DrawPanel.add(DrawPaneComponent, cs);
		
		
		/**Instantiate Chat Pane**/ 
		
		TalkPanel = new JPanel(new GridBagLayout());
		if(DEBUG)
			TalkPanel.setBackground(Color.BLUE);
		else
			TalkPanel.setBackground(new Color(0,0,0,0));
		TalkPanelScreenBounds = new Rectangle();
		WordToGuessImage = new JTextArea();
		WordToGuessImage.setEnabled(false);
		WordToGuessImage.setEditable(false);
		WordToGuessImage.setLineWrap(true);
		WordToGuessImage.setDisabledTextColor(Color.CYAN);
		WordToGuessImage.setAlignmentX(.5f);
		cs = new GridBagConstraints();
		cs.fill = GridBagConstraints.BOTH;
		cs.insets = new Insets(sy(50),0,0,sx(20));
		cs.gridx = 0;
		cs.gridy = 0;
		cs.weightx = .8;
		cs.weighty = .02;
		TalkPanel.add(new JScrollPane(WordToGuessImage), cs);
		
		TimerImage = new JTextArea();
		TimerImage.setEnabled(false);
		TimerImage.setEditable(false);
		TimerImage.setLineWrap(true);
		TimerImage.setDisabledTextColor(Color.CYAN);
		TimerImage.setAlignmentX(.5f);
		cs.gridx = 1;
		cs.gridy = 0;
		cs.weightx = .2;
		cs.weighty = .02;
		TalkPanel.add(new JScrollPane(TimerImage), cs);
		
		ScoreImage = new JTextArea();
		ScoreImage.setEnabled(false);
		ScoreImage.setEditable(false);
		ScoreImage.setDisabledTextColor(Color.CYAN);
		cs.insets = new Insets(sy(10),0,0,sx(20));
		cs.gridx = 0;
		cs.gridy = 1;
		cs.gridwidth = 2;
		cs.weightx = 1;
		cs.weighty = .28;
		TalkPanel.add(new JScrollPane(ScoreImage), cs);
		
		TextArea = new JTextArea();
		TextArea.setEditable(false);
		TextArea.setEnabled(false);
		TextArea.setLineWrap(true);
		TextArea.setDisabledTextColor(Color.BLACK);
		DefaultCaret caret = (DefaultCaret)TextArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		ScrollPane = new JScrollPane(TextArea);
		cs.gridx = 0;
		cs.gridy = 2;
		cs.gridwidth = 2;
		cs.weightx = 1;
		cs.weighty = .68;
		TalkPanel.add(ScrollPane,cs);
		
		WriteTextField = new JTextField(20);
		WriteTextField.addKeyListener(this);
		cs.gridx = 0;
		cs.gridy = 3;
		cs.gridwidth = 2;
		cs.weightx = 1;
		cs.weighty = .02;
		TalkPanel.add(WriteTextField,cs);

		
		/**Add Components to Frame**/
		
		cs = new GridBagConstraints();
		cs.fill = GridBagConstraints.BOTH;
		cs.gridx = 0;
		cs.gridy = 0;
		cs.weightx = .95;
		cs.weighty = .8;
		MainPane.add(DrawPanel, cs);
		cs.gridx = 0;
		cs.gridy = 1;
		cs.insets = new Insets(sy(0),sx(100),sy(0),sx(100));
		cs.gridx = 0;
		cs.weighty = .1;
		MainPane.add(ToolPanel, cs);
		cs.insets = new Insets(0,0,sy(45),sx(50));
		cs.gridx = 1;
		cs.gridy = 0;
		cs.gridheight = 2;
		cs.weightx = .05;
		MainPane.add(TalkPanel, cs);
		if(DEBUG)	
			MainPane.setBackground(Color.ORANGE);
		else
			MainPane.setBackground(BackColor);
		
		Frame.setContentPane(MainPane);
		Frame.setVisible(true);
		
		
		/**After Instantiate**/ 
		
		//set bounds
		ColorSelectorScreenBounds.setLocation(ColorSelectorComponent.getLocationOnScreen());
		ColorSelectorScreenBounds.setSize(ColorSelectorComponent.getBounds().width, ColorSelectorComponent.getBounds().height);
		DrawPanelScreenBounds.setLocation(DrawPaneComponent.getLocationOnScreen());
		DrawPanelScreenBounds.setSize(DrawPaneComponent.getBounds().width, DrawPaneComponent.getBounds().height);
		TalkPanelScreenBounds.setLocation(TalkPanel.getLocationOnScreen());
		TalkPanelScreenBounds.setSize(TalkPanel.getSize());
		TalkPanel.setPreferredSize(TalkPanelScreenBounds.getSize());
		
		//Re-make SelectorImage to Appropriate size
		ImageBuffer = new BufferedImage(ColorSelectorScreenBounds.width,ColorSelectorScreenBounds.height,BufferedImage.TYPE_INT_ARGB);
		g = ImageBuffer.getGraphics();
		g.drawImage(ColorSelectorImage,0,0,ColorSelectorScreenBounds.width,ColorSelectorScreenBounds.height,null);
		ColorSelectorImage = new BufferedImage(ColorSelectorScreenBounds.width,ColorSelectorScreenBounds.height,BufferedImage.TYPE_INT_ARGB);
		g = ColorSelectorImage.getGraphics();
		g.drawImage(ImageBuffer, 0, 0, ImageBuffer.getWidth(), ImageBuffer.getHeight(), null);
		ColorSelectorComponent.setImage(ColorSelectorImage);

		//Re-make SelectorImage to Appropriate size
		ImageBuffer = new BufferedImage(DrawPanelScreenBounds.width,DrawPanelScreenBounds.height,BufferedImage.TYPE_INT_ARGB);
		g = ImageBuffer.getGraphics();
		g.drawImage(DrawPaneImage,0,0,DrawPanelScreenBounds.width,DrawPanelScreenBounds.height,null);
		DrawPaneImage = new BufferedImage(DrawPanelScreenBounds.width,DrawPanelScreenBounds.height,BufferedImage.TYPE_INT_ARGB);
		g = DrawPaneImage.getGraphics();
		g.drawImage(ImageBuffer, 0, 0, ImageBuffer.getWidth(), ImageBuffer.getHeight(), null);
		DrawPaneComponent.setImage(DrawPaneImage);
	}
	private int sx(int Val){
		return (int)(Val*ScreenScaling.x);
	}
	private int sy(int Val){
		return (int)(Val*ScreenScaling.y);
	}
	@Override
	public void mouseClicked(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
	@Override
	public void mousePressed(MouseEvent e){
		if(CurrentPlayer){
			//set mouse points
			MousePos.setLocation(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
			MouseRelitiveToDrawPane.setLocation(MousePos.x-DrawPanelScreenBounds.getBounds().x, MousePos.y-DrawPanelScreenBounds.getBounds().y);
			MouseRelitiveToColorSelector.setLocation(MousePos.x-ColorSelectorScreenBounds.getBounds().x, MousePos.y-ColorSelectorScreenBounds.getBounds().y);
			//color chooser
			//if mouse in color selector
			if(ColorSelectorScreenBounds.contains(MousePos)){
				//get new color
				ColorPicker = new Color(ColorSelectorImage.getRGB(MouseRelitiveToColorSelector.x, MouseRelitiveToColorSelector.y));
				//set component based off of color
				g = ColorPickerImage.getGraphics();
				g.setColor(ColorPicker);
				g.fillRect(0, 0, ColorPickerImage.getWidth(), ColorPickerImage.getHeight());
				g.dispose();
				ColorPickerComponent.setImage(ColorPickerImage);
			}
			//Draw Logic
			//if mouse in draw panel
			if(DrawPanelScreenBounds.contains(MousePos)){
				//add history image
				try{
					ImageHistory.add(new BufferedImage(DrawPanelScreenBounds.width,DrawPanelScreenBounds.height,BufferedImage.TYPE_INT_ARGB));		
				}catch(OutOfMemoryError o){
					g = ImageHistory.get(0).getGraphics();
					g.drawImage(ImageHistory.remove(1), 0, 0, null);
					g.dispose();
					System.out.println("Over Mem Lim");
				}
				CurrentImage = ImageHistory.get(ImageHistory.size()-1);
				//if mode is brush
				if(CurrentTool.equals("BRUSH")){
					DrawLogic(CurrentImage,ColorPicker,MouseRelitiveToDrawPane.x,MouseRelitiveToDrawPane.y);
				}
				//if mode is eraser 
				else if(CurrentTool.equals("ERASER")){
					DrawLogic(CurrentImage,Color.WHITE,MouseRelitiveToDrawPane.x,MouseRelitiveToDrawPane.y);
					
				}
				//if mode is fill bucket 
				else if(CurrentTool.equals("BUCKET")){
					FillBucket();
				}
			}
			//draw latest image to draw panel
			g = DrawPaneImage.getGraphics();
			g.drawImage(CurrentImage, 0, 0, null);
			g.dispose();
			DrawPaneComponent.setImage(DrawPaneImage);
			//set previous point
			PrevMousePos.setLocation(MousePos.x, MousePos.y);
		}
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		if(CurrentPlayer){
			if(DrawPanelScreenBounds.contains(MousePos)){
				ImageHistoryCopy.clear();
				for(int i = 0;i<ImageHistory.size();i++){
					ImageHistoryCopy.add(ImageHistory.get(i));
				}
			}
		}
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if(CurrentPlayer){
			//set mouse points
			MousePos.setLocation(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
			MouseRelitiveToDrawPane.setLocation(MousePos.x-DrawPanelScreenBounds.getBounds().x, MousePos.y-DrawPanelScreenBounds.getBounds().y);
			MouseRelitiveToDrawPaneOld.setLocation(PrevMousePos.x-DrawPanelScreenBounds.getBounds().x, PrevMousePos.y-DrawPanelScreenBounds.getBounds().y);
			//Draw Logic
			//if mouse in draw panel
			if(DrawPanelScreenBounds.contains(MousePos)){
				CurrentImage = ImageHistory.get(ImageHistory.size()-1);
				//if mode is brush
				if(CurrentTool.equals("BRUSH")){
					DrawLogic(CurrentImage,ColorPicker,MouseRelitiveToDrawPane.x,MouseRelitiveToDrawPane.y,MouseRelitiveToDrawPaneOld.x,MouseRelitiveToDrawPaneOld.y);
				}
				//if mode is eraser 
				else if(CurrentTool.equals("ERASER")){
					DrawLogic(CurrentImage,Color.WHITE,MouseRelitiveToDrawPane.x,MouseRelitiveToDrawPane.y,MouseRelitiveToDrawPaneOld.x,MouseRelitiveToDrawPaneOld.y);
				}
				//draw latest image to draw panel
				g = DrawPaneImage.getGraphics();
				g.drawImage(CurrentImage, 0, 0, null);
				g.dispose();
				DrawPaneComponent.setImage(DrawPaneImage);
			}
			//set previous point
			PrevMousePos.setLocation(MousePos.x, MousePos.y);
		}
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		
		
	}
	@Override
	public void stateChanged(ChangeEvent arg0) {
		BrushRad = BrushSize.getValue();
		WriteTextField.requestFocusInWindow();
	}
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_ENTER){
			SendBuffer = WriteTextField.getText();
			WriteTextField.setText("");
		}
		
	}
	@Override
	public void keyReleased(KeyEvent arg0) {}
	@Override
	public void keyTyped(KeyEvent arg0) {}
	
	//Point fill sphere
	private void DrawLogic(BufferedImage bi, Color c, int x, int y){
		g = bi.getGraphics();
		g.setColor(c);
		g.fillOval(x-BrushRad, y-BrushRad, BrushRad*2, BrushRad*2);
		g.dispose();
	}
	//Line Point fill sphere
	private void DrawLogic(BufferedImage bi, Color c, float xx1, float yy1, float xx2, float yy2){
		if(PrevMousePos.x!=0&&PrevMousePos.y!=0){
			x1 = xx1<xx2 ? xx1 : xx2;
			x2 = xx1>=xx2 ? xx1 : xx2;
			y1 = xx1<xx2 ? yy1 : yy2;
			y2 = xx1>=xx2 ? yy1 : yy2;
			m1 = (1.f*(y1)-(y2))/(1.f*(x1)-(x2));
			m2 = (1.f*(x1)-(x2))/(1.f*(y1)-(y2));
			
			if(m1<=1&&m1>=-1){
				for(int i = (int)x1;i<(int)x2;i++,y1+=m1){
					DrawLogic(bi,c,i,(int)y1);
				}
			}
			else{
				if(y1<y2){
					for(int i = (int)y1;i<(int)y2;i++,x1+=m2){
						DrawLogic(bi,c,(int)x1,i);
					}
				}
				else{
					for(int i = (int)y2;i<(int)y1;i++,x2+=m2){
						DrawLogic(bi,c,(int)x2,i);
					}
				}
			}
		}
	}
	//Button Press
	private void SelectorButtonPress(JButton z){
		if(z.equals(SelectorButtons[0])){
			//Brush
			CurrentTool = "BRUSH";
		}else if(z.equals(SelectorButtons[1])){
			//Undo
			if(ImageHistory.size()>1){
			ImageHistory.remove(ImageHistory.size()-1);
				for(int i = 0; i<ImageHistory.size();i++){
					g = ImageBuffer.getGraphics();
					g.drawImage(ImageHistory.get(i), 0, 0, null);
					g.dispose();
				}
				g = DrawPaneImage.getGraphics();
				g.drawImage(ImageBuffer, 0, 0, null);
				g.dispose();
				DrawPaneComponent.setImage(DrawPaneImage);
			}
		}else if(z.equals(SelectorButtons[2])){
			//Redo
			if(ImageHistory.size()<ImageHistoryCopy.size()){
				ImageHistory.add(ImageHistoryCopy.get(ImageHistory.size()));
			}
			for(int i = 0; i<ImageHistory.size();i++){
				g = ImageBuffer.getGraphics();
				g.drawImage(ImageHistory.get(i), 0, 0, null);
				g.dispose();
			}
			g = DrawPaneImage.getGraphics();
			g.drawImage(ImageBuffer, 0, 0, null);
			g.dispose();
			DrawPaneComponent.setImage(DrawPaneImage);
		}else if(z.equals(SelectorButtons[3])){
			//Clear
			ClearLogic();
		}else if(z.equals(SelectorButtons[4])){
			//Eraser
			CurrentTool = "ERASER";
		}else if(z.equals(SelectorButtons[5])){
			//Fill Bucket
			CurrentTool = "BUCKET";
		}
		ButtonRepaint();
		WriteTextField.requestFocusInWindow();
	}
	private void ButtonRepaint(){
		for(int i = 0;i<6;i++){
			SelectorButtons[i].repaint();
			if(CurrentTool.equals("BRUSH")&& i == 0){
				continue;
			}
			else if(CurrentTool.equals("ERASER")&& i == 4){
				continue;
			}
			else if(CurrentTool.equals("BUCKET")&& i == 5){
				continue;
			}
		}
	}
	private void FillBucket(){
		for(int i = 0; i<ImageHistory.size()-1;i++){
			g = ImageBuffer.getGraphics();
			g.drawImage(ImageHistory.get(i), 0, 0, null);
			g.dispose();
		}
		ColorOverlap = new Color(ImageBuffer.getRGB(MouseRelitiveToDrawPane.x, MouseRelitiveToDrawPane.y));
		PointCheck = new boolean[DrawPanelScreenBounds.width][DrawPanelScreenBounds.height];
		//PointCheck[MouseRelitiveToDrawPane.x][MouseRelitiveToDrawPane.y]= true;
		queue.add(MouseRelitiveToDrawPane);
		FillLogic(ImageBuffer,ColorOverlap,ColorPicker);
		
		CurrentImage = ImageHistory.get(ImageHistory.size()-1);
		g = CurrentImage.getGraphics();
		g.drawImage(ImageBuffer, 0, 0, null);
		g.dispose();
		
		g = DrawPaneImage.getGraphics();
		g.drawImage(ImageBuffer, 0, 0, null);
		g.dispose();
		DrawPaneComponent.setImage(DrawPaneImage);
	}
	
	private void ClearLogic(){
		ImageHistory.add(new BufferedImage(DrawPanelScreenBounds.width,DrawPanelScreenBounds.height,BufferedImage.TYPE_INT_ARGB));
		CurrentImage = ImageHistory.get(ImageHistory.size()-1);
		g = ImageHistory.get(ImageHistory.size()-1).getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, CurrentImage.getWidth(), CurrentImage.getHeight());
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, CurrentImage.getWidth()-1, CurrentImage.getHeight()-1);
		g.dispose();
		g = DrawPaneImage.getGraphics();
		g.drawImage(CurrentImage, 0, 0, null);
		g.dispose();
		DrawPaneComponent.setImage(DrawPaneImage);
		ImageHistoryCopy.clear();
		for(int i = 0;i<ImageHistory.size();i++){
			ImageHistoryCopy.add(ImageHistory.get(i));
		}
	}
	//Pixel by Pixel fill Logic
	private void FillLogic(BufferedImage bi,Color oc,Color fc){
		while(!queue.isEmpty()){
			currentQueue = queue.remove();
			try{
				if(!PointCheck[currentQueue.x][currentQueue.y]){
					if(bi.getRGB(currentQueue.x, currentQueue.y) == oc.getRGB()){
						bi.setRGB(currentQueue.x, currentQueue.y, fc.getRGB());
						try{
							queue.add(new Point(currentQueue.x+1,currentQueue.y));
						}catch(Exception e){}
						try{
							queue.add(new Point(currentQueue.x-1,currentQueue.y));
							}catch(Exception e){}
						try{
							queue.add(new Point(currentQueue.x,currentQueue.y+1));
							}catch(Exception e){}
						try{
							queue.add(new Point(currentQueue.x,currentQueue.y-1));
							}catch(Exception e){}
						PointCheck[currentQueue.x][currentQueue.y] = true;
					}
				}
			}catch(ArrayIndexOutOfBoundsException a){
				continue;
			}
		}
	}
	//Initialize Buttons for tool panel
	private void MakeButtons(){
		int z = 0;
		BufferedImage ButtonImage = new BufferedImage(34,26,BufferedImage.TYPE_INT_ARGB);
		for(z=0;z<6;z++){
			switch(z){
			//BRUSH BUTTON
			case 0:
				try {
					ButtonImage=ImageIO.read(DrawMyThingMain.class.getResourceAsStream("Brush.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				SelectorButtons[z] = new JButton(new ImageIcon(ButtonImage));
				break;
			//UNDO BUTTON
			case 1:
				try {
					ButtonImage=ImageIO.read(DrawMyThingMain.class.getResourceAsStream("Undo.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				SelectorButtons[z] = new JButton(new ImageIcon(ButtonImage));

				break;
			//REDO BUTTON
			case 2:
				try {
					ButtonImage=ImageIO.read(DrawMyThingMain.class.getResourceAsStream("Redo.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				SelectorButtons[z] = new JButton(new ImageIcon(ButtonImage));

				break;
			//CLEAR BUTTON
			case 3:
				try {
					ButtonImage=ImageIO.read(DrawMyThingMain.class.getResourceAsStream("Clear.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				SelectorButtons[z] = new JButton(new ImageIcon(ButtonImage));

				break;
			//ERASER BUTTON
			case 4:
				try {
					ButtonImage=ImageIO.read(DrawMyThingMain.class.getResourceAsStream("Eraser.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				SelectorButtons[z] = new JButton(new ImageIcon(ButtonImage));

				break;
			//FILL BUCKET BUTTON
			case 5:
				try {
					ButtonImage=ImageIO.read(DrawMyThingMain.class.getResourceAsStream("PaintBucket.png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				SelectorButtons[z] = new JButton(new ImageIcon(ButtonImage));
				
				break;
			default:
				System.out.println("ERROR");
				break;
			}
			SelectorButtons[z].setBackground(new Color(ButtonImage.getRGB(0, 0)));
			SelectorButtons[z].addActionListener(new ActionListener(){
				
				@Override
				public void actionPerformed(ActionEvent e) {
					SelectorButtonPress((JButton)e.getSource());					
				}
				
			});
			ButtonPanel.add(SelectorButtons[z]);
		}
	}
	@Override
	public void run() {
		String Command = null;
		String[] RecMessage;
		BufferedImage ShowingImage = null;
		while(GameisRunning){
			while(Command == null){
				Command = ReadObject();
				if(Command.equals("null")||Command==null){
					Command = null;
					continue;
				}
				if(Command.startsWith("!@#.")){
					Command = Command.substring(4);
					//Commands Here:
					if(Command.startsWith("UPDATETIME.")){
						//COMMAND: UPDATETIME.60
						Command = Command.substring("UPDATETIME.".toCharArray().length);
						TimerImage.setText(Command);
					}
					else if(Command.startsWith("SENDMESSAGE.")){
						//COMMAND: SENDMESSAGE.[USERNAME].MESSAGE
						Command = Command.substring("SENDMESSAGE.".toCharArray().length);
						RecMessage = Command.split("\\.");
						if(RecMessage.length>2){
							for(int g = 2;g<RecMessage.length;g++){
								RecMessage[1]+=RecMessage[g];
							}
							TextArea.setText(TextArea.getText()+"\n"+RecMessage[0]+": "+RecMessage[1]);
						}
						else if(RecMessage.length==2){
							TextArea.setText(TextArea.getText()+"\n"+RecMessage[0]+": "+RecMessage[1]);
						}
						else if(RecMessage.length==1){
							TextArea.setText(TextArea.getText()+"\n"+RecMessage[0]);
						}
						SendBuffer = "";
					}
					else if(Command.startsWith("GETSENDBUFFER")){
						//COMMAND: GETSENDBUFFER
						//returns SendBuffer
						SendObject(SendBuffer);
					}
					else if(Command.startsWith("SETWORDTOGUESS.")){
						Command = Command.substring("SETWORDTOGUESS.".toCharArray().length);
						WordToGuessImage.setText(Command);
					}
					else if(Command.startsWith("YOURUP")){
						//COMMAND: YOURUP
						CurrentPlayer = true;
						RefreshControl();
					}
					else if(Command.startsWith("YOURNOTUP")){
						//COMMAND: YOURNOTUP
						CurrentPlayer = false;
						RefreshControl();
					}
					else if(Command.startsWith("GETDISPLAY")){
						//COMMAND: GETDISPLAY
						//System.out.println("GETTING DISPLAY");
						ShowingImage = imageSender.ReceiveImage();
						DrawPaneComponent.setImage(ShowingImage);
					}
					else if(Command.startsWith("GIVEDISPLAY")){
						//COMMAND: GIVEDISPLAY
						//System.out.println("GIVING DISPLAY");
						for(int i = 0; i<ImageHistory.size();i++){
							g = ImageBuffer.getGraphics();
							g.drawImage(ImageHistory.get(i), 0, 0, null);
							g.dispose();
						}
						imageSender.SendImage(ImageBuffer);
					}
					else{
						//Fail
						System.out.println("Wierd Command?: !@#."+Command);
					}
					
					Command = null;
					continue;
				}
				else{
					//Fail
					System.out.println("Wierd Command?: "+Command);
				}
				Command = null;
			}				
		}
	}
	private void RefreshControl(){
		if(CurrentPlayer){
			ToolPanel.setEnabled(true);
			DrawPaneComponent.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		}
		else{
			ToolPanel.setEnabled(false);
			DrawPaneComponent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		WordToGuessImage.setText("");
		ClearLogic();
		DrawPaneComponent.repaint();
		ToolPanel.repaint();
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
			try {
				return (String)input.readObject();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}