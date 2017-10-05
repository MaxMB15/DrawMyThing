import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * 
 * @author Max Boksem
 *
 */

public class FileTextReader {
	  public static String ReadText(String fileName){
		  String output = "";
		  Scanner myScan = new Scanner(FileTextReader.class.getResourceAsStream(fileName));
		  while(myScan.hasNext()){
			  output += myScan.nextLine() + "\n";
		  }
		  output = output.substring(0,output.length()-1);
		  myScan.close();
		  return output;
	  }
	  
	  public static String[] ReadTextByLines(String fileName){
		  Scanner myScan = new Scanner(FileTextReader.class.getResourceAsStream(fileName));
		  ArrayList<String> o = new ArrayList<String>();
		  while(myScan.hasNext()){
			  o.add(myScan.nextLine());
		  }
		  String[] g = new String[o.size()];
		  for(int i = 0; i<o.size();i++){
			  g[i] = o.get(i);
		  }
		  myScan.close();
		  return g;
	  }
	  
	  public static void WriteText(String fileName, String content){
		  File WriteFile = new File("src/"+fileName+".txt");
		  try {
			FileOutputStream fos = new FileOutputStream(WriteFile);
			fos.write(content.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	  }
	  public static void WriteText(String fileName, String[] content){
		  String intoContent = "";
		  for(String s:content){
			  intoContent += (s + "\n");
		  }
		  WriteText(fileName,intoContent);
	  }
	  
}