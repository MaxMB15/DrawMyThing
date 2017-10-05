import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class Tester {

	public static void main(String[] args) {
		int PlayerNum = 3;
		int PORT = 35514;
		int temp = PORT;
		PORT = PORT/100;
		PORT*=100;
		int lastDig = Integer.parseInt((""+temp).substring((""+temp).length()-1, (""+temp).length()));
		System.out.println(PORT);
		System.out.println(temp);
		System.out.println(lastDig);
		lastDig*=10;
		PORT+=lastDig;
		PORT+=PlayerNum;
		System.out.println(PORT);
		JPanel testCell = new JPanel();
		testCell.setSize(100, 20);
		testCell.setBackground(Color.WHITE);
		String[] titles = {"aaas", "bbbs", "cccs"};
		MaxTable table = new MaxTable(2,3,titles);
		String[] ttemp = {"aaa", "bbb"};
		JButton[] buts = {new JButton("test"), new JButton("longggggg test")};
		table._setColumn(0, ttemp);
		table._setColumn(1, ttemp);
		table._setColumn(2, buts);
		table._setContent(1,1, testCell);
		table._setContent(0, 1, "test teste tes tes est");
		buts[1].setEnabled(false);
		JFrame j = new JFrame();
		j.setSize(555, 444);
		j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel pane = new JPanel();
		j.setContentPane(pane);
		pane.add(table);
		j.setVisible(true);
	}

}
