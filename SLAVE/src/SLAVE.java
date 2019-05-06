import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class SLAVE {
	static String mapsPath = "/tmp/mezzeddine/maps/";
    static String reducesPath = "/tmp/mezzeddine/reduces/";
    
	public static void main(String[] args) throws Exception {		
		int mode = -1;
		String splitFile = "";
		if(args.length >= 2) {
			mode = Integer.parseInt(args[0]);
		}
		else {
			System.out.println("Wrong params in SLAVE");
			return;
		}
		
		String word, SMFile, RMFile;
		ArrayList <String> umFiles = new ArrayList <String>();
		
		switch(mode) {
			case 0: // MAP
				splitFile = args[1];
				map(splitFile);
				break;
			case 1: // SHUFFLE
				// We assume mapping is already done
				// We assume map directory exists
				// java -jar slave.jar 1 Car SMx list_UMx

				word = args[1];
				SMFile = args[2];
				
				for(int i = 3; i < args.length; i++) {
					umFiles.add(args[i]);
				}
				shuffle(word, SMFile, umFiles);
				break;
			case 2: // REDUCE
				word = args[1];
				SMFile = args[2];
				RMFile = args[3];
				reduce(word, SMFile, RMFile);
				break;
			default:
				System.out.println("Nothing to do");
				break;
		}
	}
	
	public static void map(String splitFile) throws Exception {
	   	File file = new File(splitFile);
	    Scanner input = new Scanner(file);
	    ArrayList <String> words = new ArrayList<String>();
	    String mapFileName = getMapFileNameFomPath(splitFile);
	    
	    // create folder maps
	    createDirectory(mapsPath);    
	    
	    FileWriter fileWriter = new FileWriter(mapsPath + mapFileName);
	    PrintWriter printWriter = new PrintWriter(fileWriter);
	    while (input.hasNext()) {
		      String word  = input.next();
		      printWriter.print(word + " 1\n");
		      System.out.println(word);
	    }
	    printWriter.close();
	    
	}
	public static void shuffle(String word, String SMFile, ArrayList <String> umFiles) throws Exception {
	    System.out.println("[~] Starting shuffle for word : " + word);
		FileWriter fileWriter = new FileWriter(SMFile);
	    PrintWriter printWriter = new PrintWriter(fileWriter); 
	    for(String umFile : umFiles) {
	    	File file = new File(umFile);
	    	Scanner input = new Scanner(file);
		    while (input.hasNext()) {
			      String w  = input.next();
			      if(w.equals(word)) {
			    	  printWriter.println(word + " 1");
			      }
		    }   	
	    }
	    printWriter.close();
	    System.out.println("[OK] End of shuffle for word : " + word + " !");
	}
	public static void reduce(String word, String SMFile, String RMFile) throws Exception {
	   	// TODO : we can remove comparison here, just count the number of lines of SMFile
		System.out.println("[~] Starting reduce for word : " + word);
		File file = new File(SMFile);
	    Scanner input = new Scanner(file);
	    // create folder reduces
	    createDirectory(reducesPath);
	    FileWriter fileWriter = new FileWriter(RMFile);
	    PrintWriter printWriter = new PrintWriter(fileWriter);
	    int count = 0;
	    while (input.hasNext()) {
		      String w  = input.next();
		      if(w.equals(word))
		    	  count++;
		}
	    printWriter.print(word + " " + count + "\n");
	    printWriter.close();  
	    System.out.println("[OK] End of reduce for word : " + word);
	}
	
	public static String getMapFileNameFomPath(String path) {
		String[] parts = path.split("/");
		String fileName = parts[parts.length - 1];
		fileName = "UM" + fileName.substring(1, fileName.length());
		return fileName;
	}
	
	public static void createDirectory(String dirName) throws Exception {
//		System.out.println("Creating directory " + dirName);
		ProcessBuilder pb = new ProcessBuilder("mkdir", "-p", dirName);
		pb.inheritIO(); // redirect error to stdout
		Process process = pb.start();
		int errCode = process.waitFor();
		if(errCode == 0) {
//			System.out.println("Directory " + dirName + " created successfully !");
		}
		else {
			System.out.println("Cannot create directory " + dirName);
		}
	}
	
}
