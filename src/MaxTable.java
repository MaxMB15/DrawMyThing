import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.JLabel;
import javax.swing.JPanel;


public class MaxTable extends JPanel{
	
	private static final long serialVersionUID = 6683188908186216006L;
	private Container[][] TableContents;
	private GridBagConstraints cs;
	/**
	 *  C
	 *  C
	 *  C
	 *  C
	 * 
	 *  R R R R
	 *  
	 *  SSSSS
	 *  12222
	 *  12222
	 *  12222
	 *  12222
	 */
	public MaxTable(int Rows, int Columns, String[] Titles){
		super();
		TableContents = new Container[Rows+1][Columns];
		cs = new GridBagConstraints();
		cs.fill = GridBagConstraints.HORIZONTAL;
		this.setLayout(new GridBagLayout());
		for(int i = 0;i<Columns;i++){
			TableContents[0][i] = new JLabel(Titles[i]);
		}
	}
	public void _setRow(int Row, Container[] Contents){
		if(Contents.length==TableContents[0].length){
			for(int i = 0; i<Contents.length;i++){
				TableContents[Row+1][i] = Contents[i];
			}
			UpdateData();
			return;
		}
		System.out.println("WARNING!: TABLE ROW "+Row+" WAS NOT SET.\n"+Contents.length+" is greater than "+TableContents[0].length+"!");
		return;
	}
	public void _setColumn(int Column,Container[] Contents){
		if(Contents.length==TableContents.length-1){
			for(int i = 0; i<Contents.length;i++){
				TableContents[i+1][Column] = Contents[i];
			}
			UpdateData();
			return;
		}
		System.out.println("WARNING!: TABLE COLUMN "+Column+" WAS NOT SET.\n"+Contents.length+" is greater than "+(TableContents.length-1)+"!");
		return;
	}
	public void _setColumn(int Column,String[] Contents){
		if(Contents.length==TableContents.length-1){
			for(int i = 0; i<Contents.length;i++){
				TableContents[i+1][Column] = new JLabel(Contents[i]);
			}
			UpdateData();
			return;
		}
		System.out.println("WARNING!: TABLE COLUMN "+Column+" WAS NOT SET.\n"+Contents.length+" is greater than "+(TableContents.length-1)+"!");
		return;
	}
	public Container[] _getRow(int Row){
		return TableContents[Row+1];
	}
	private Container[] temp;
	public Container[] _getCol(int Column){
		temp = new Container[TableContents.length-1];
		for(int i = 1;i<TableContents.length;i++){
			temp[i] = TableContents[i][Column];
		}
		return temp;
	}
	public Container _getContent(int Row,int Col){
		return TableContents[Row+1][Col];
	}
	public void _setContent(int Row,int Col, Container obj){
		TableContents[Row+1][Col] = obj;
		UpdateData();
	}
	public void _setContent(int Row,int Col, String obj){
		TableContents[Row+1][Col] = new JLabel(obj);
		UpdateData();
	}
	public int _getRowSize(){
		return TableContents[0].length;
	}
	public int _getColSize(){
		return TableContents.length-1;
	}
	public int _getNumRow(){
		return _getColSize();
	}
	public int _getNumCol(){
		return _getRowSize();
	}
	public Point _isSpotEmpty(){
		for(int row = 1; row<_getNumRow();row++){
			for(int col = 0; col<_getNumCol();col++){
				if(TableContents[row][col]==null){
					return new Point(row,col);
				}
			}
		}
		return null;
		
	}
	public void _setInsets(Insets i){
		cs.insets = i;
		UpdateData();
	}
	private void CSToPoint(int Row,int Col){
		cs.gridx = Col;
		cs.gridy = Row;
	}
	private void UpdateData(){
		this.removeAll();
		for(int y = 0;y<_getNumRow()+1;y++){
			for(int x = 0;x<_getNumCol();x++){
				CSToPoint(y,x);
				if(TableContents[y][x]!=null){
					this.add(TableContents[y][x],cs);	
				}
			}
		}
	}
}
