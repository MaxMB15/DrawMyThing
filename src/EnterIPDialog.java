import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class EnterIPDialog extends JDialog{

	private static final long serialVersionUID = -8125482354075708597L;

	public EnterIPDialog(final JFrame f){
		super(f,"Enter IP Address Prompt", true);
		JPanel panel = new JPanel(new GridBagLayout());
		
	    GridBagConstraints cs = new GridBagConstraints();
	 
	    cs.fill = GridBagConstraints.HORIZONTAL;
	    JLabel lab = new JLabel("IP Address: ");
	    cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(lab,cs);
        
        final JTextField input = new JTextField(15);
        cs.gridx = 1;
        cs.gridwidth = 2;
        panel.add(input,cs);
        input.setDocument(new JTextFieldLimit(15,true));
        
        JButton Trybut = new JButton("Connect");
        Trybut.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				GameSelect gs = new GameSelect(f,input.getText());
				gs.setVisible(true);
			}
        	
        });
        JButton Cancelbut = new JButton("Cancel");
        Cancelbut.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel bp = new JPanel();
        bp.add(Trybut);
        bp.add(Cancelbut);
 
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);
 
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
	}
}
@SuppressWarnings("serial")
class JTextFieldLimit extends PlainDocument {
	  private int limit;
	  private boolean IP;
	  JTextFieldLimit(int limit) {
	    super();
	    this.limit = limit;
	  }

	  JTextFieldLimit(int limit, boolean IP) {
	    super();
	    this.limit = limit;
	    this.IP = IP;
	  }

	  public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
	    if (str == null)
	      return;
	    if(IP){
	    	int pCount = 0;
	    	int pSince = 0;
	    	char temp;
	    	for( int i = 0; i < getLength(); i++ )
	    	{
	    	    temp = getText(0,getLength()).charAt(i);
	    	    pSince++;
	    	    if(temp == '.'){
	    	    	pCount++;
	    	    	pSince = 0;
	    	    }	
	    	}
	    	if(pCount>=3 && str.equals(".")){
	    		return;
	    	}
	    	if(((str.charAt(0)<=57&&str.charAt(0)>=48)&&pSince<3)||(str.charAt(0)==46&&getLength()!=0&&pSince!=0)){
    			if ((getLength() + str.length()) <= limit) {
    				super.insertString(offset, str, attr);
    			}
	    	}
	    	return;
	    }
	    
	    if ((getLength() + str.length()) <= limit) {
	    	super.insertString(offset, str, attr);
	    }
	  }
	}
