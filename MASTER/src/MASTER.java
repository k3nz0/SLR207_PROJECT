import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MASTER {
	
	static String slavePath = "/tmp/mezzeddine/SLAVE.jar";
	static String splitsPath = "/tmp/mezzeddine/splits/";
	static String mapsPath =  "/tmp/mezzeddine/maps/";
	static String reducesPath = "/tmp/mezzeddine/reduces/";
	static String machinesPath = "/tmp/mezzeddine/machines";
	static HashMap <String, HashSet <String> > mapWordsToUM = new HashMap <String, HashSet <String> >();
	//{Car=[UM1, UM2], River=[UM0, UM1], Deer=[UM0, UM2], Beer=[UM0, UM2]}
	
	static HashMap <String, String> mapUMToMachine = new HashMap <String, String>();
	
	static ArrayList <ArrayList <String> > machineWords = new ArrayList <ArrayList <String>>(); 
	// machineWords[i] is array list of words to be shuffled and reduced
	static ArrayList <HashSet <String> > machinesToUM = new ArrayList <HashSet <String> >();
	static ArrayList <String> allWords = new ArrayList <String>();
	
	public static void main(String[] args) throws Exception {
		System.out.println("Running MASTER");
		String machine = "c126-08";
		ArrayList <String> machines = getMachines();
	
		System.out.println(machines);
		
        long startTime;
        long endTime; 
        long totalTime;
        
		
        
		copySplitsToSlaves(machines);
		
		startTime = System.currentTimeMillis();
		runSlavesMap(machines);
		endTime = System.currentTimeMillis();
		System.out.println("[+] TIME SPENT IN MAP : " + (endTime - startTime) + "ms !");
		
		startTime = System.currentTimeMillis();
		runSlavesShuffle(machines);
		endTime = System.currentTimeMillis();
		System.out.println("[+] TIME SPENT IN SHUFFLE : " + (endTime - startTime) + "ms !");
		
		startTime = System.currentTimeMillis();
		runSlavesReduce(machines);
		endTime = System.currentTimeMillis();
		System.out.println("[+] TIME SPENT IN REDUCE : " + (endTime - startTime) + "ms !");
		
		getFinalResult(machines);
	}
	
	private static void runSlavesMap(ArrayList <String> machines) throws Exception {
		// Run slaves 
		// Store in mapWordsToUM such that we get (word => list of UMx)
		// Store in mapUMToMachine such that we get (UMx => machine)

		
		System.out.println("\n[~] Starting map phase !");

		ProcessBuilder[] pb = new ProcessBuilder[machines.size()]; 
		Process[] processes = new Process[machines.size()];
		

		for(int i = 0; i < machines.size(); i++) {
			String command = "chmod +x " + slavePath + " && " + slavePath;
			command += " 0 " + splitsPath + "S" + i + ".txt";
//			System.out.println(command);
			pb[i] = new ProcessBuilder("ssh", machines.get(i), command);
			processes[i] = pb[i].start();	
		}
		// wait for the machines 
		for(int i = 0; i < machines.size(); i++) {
			int errCode = processes[i].waitFor();
			if(errCode == 0) {
				String res = output(processes[i].getInputStream());
				String words[] = res.split("\n");
				for(String word : words) {
					if(mapWordsToUM.get(word) == null) {
						HashSet <String> ums = new HashSet <String> ();
						ums.add("UM" + i);
						mapWordsToUM.put(word, ums);
					}
					else {
						mapWordsToUM.get(word).add("UM" + i);
					}
				}
			}
		}
//		System.out.println(mapWordsToUM.toString());
		// show dictionary 
		// “UMx - machines”
		for(int i = 0; i < machines.size(); i++) {
//			System.out.println("UM" + i + " - " + machines.get(i));
			mapUMToMachine.put("UM" + i, machines.get(i));
		}
//		System.out.println(mapUMToMachine);
		System.out.println("[OK] MAP phase terminated !");
	}
	
	private static void runSlavesShuffle(ArrayList <String> machines) throws Exception {
		// prepare shuffle phase
		// For each word we assign a slave (modulo)

		
		System.out.println("\n[~] Preparing slaves shuffle !");
		
		// allocate memory for machineWords
		for(int i = 0; i < machines.size(); i++ ) {
			machineWords.add(new ArrayList <String>());
			machinesToUM.add(new HashSet <String>());
		}
		
		int idx = 0;
		int n = machines.size();
        int nbWords = 0;
		for (Map.Entry e : mapWordsToUM.entrySet()) { 
            String word = (String) e.getKey();
            HashSet <String> ums = (HashSet <String>) e.getValue();
        	// Copy 
        	int i = idx % n;
        	for(String um : ums) {
        		if(machinesToUM.get(i).add(um)) { // machine i doesn't have um 
	        		String machineFrom = mapUMToMachine.get(um);
	        		String machineTo = machines.get(i);
	        		if(!machineFrom.equals(machineTo)) {
	        			copyUMToTarget(machineFrom, machineTo, um);
	        		}
        		}
        	}
        	
        	machineWords.get(i).add(word);
        	idx++;
        	nbWords++;
        }
//		System.out.println(machineWords);
		
		
		ProcessBuilder[] pb = new ProcessBuilder[nbWords + 1]; 
		Process[] processes = new Process[nbWords + 1];
		
		
		System.out.println("[~] Starting slaves shuffle !");
		
		int idx_word = 0;
		for(int i = 0; i < machines.size(); i++) {
			for(String word: machineWords.get(i)) {
				allWords.add(word);
				String SMFile = mapsPath + "SM" + idx_word + ".txt";
				String command = "java -jar " + slavePath + " 1 " + word + " " + SMFile;
				for(String um : machinesToUM.get(i)) {
					command += " " + mapsPath + um + ".txt";
				}
//				System.out.println(command);
				pb[idx_word] = new ProcessBuilder("ssh", machines.get(i), command);
//				pb[i].inheritIO(); // redirect error to stdout
				processes[idx_word] = pb[idx_word].start();
				idx_word++;
			}
		}
		for(int i = 0; i < nbWords; i++) {
			int errCode = processes[i].waitFor();
			if(errCode == 0) {
//				String res = output(processes[i].getInputStream());
//				System.out.println(res);
			}
		}
		System.out.println("[OK] Shuffle phase terminated !");
		
	}
	
	private static void runSlavesReduce(ArrayList <String> machines) throws Exception {
		System.out.println("\n[~] Running reduce !");
		
		ProcessBuilder[] pb = new ProcessBuilder[allWords.size()]; 
		Process[] processes = new Process[allWords.size()];
		
		int idx_word = 0;
		for(int i = 0; i < machines.size(); i++) {
			ArrayList <String> words = machineWords.get(i);
			for(String word: words) {
				String SMFile = mapsPath + "SM" + idx_word + ".txt";
				String RMFile = reducesPath + "RM" + idx_word + ".txt";
				String command = "java -jar " + slavePath + " 2 " + word + " " + SMFile + " " + RMFile;
				
//				System.out.println(command);
				pb[idx_word] = new ProcessBuilder("ssh", machines.get(i), command);
//				pb[i].inheritIO(); // redirect error to stdout
				processes[idx_word] = pb[idx_word].start();
				idx_word++;
			}
		}
		for(int i = 0; i < allWords.size(); i++) {
			int errCode = processes[i].waitFor();
			if(errCode == 0) {
//				String res = output(processes[i].getInputStream());
//				System.out.println(res);
			}
		}
	}
	
	private static void getFinalResult(ArrayList <String> machines) throws Exception {
		System.out.println("\n[OK] Final result : ");
		ProcessBuilder[] pb = new ProcessBuilder[machines.size()]; 
		Process[] processes = new Process[machines.size()];
		

		for(int i = 0; i < machines.size(); i++) {
			String command = "cat " + reducesPath + "*";
//			System.out.println(command);
			pb[i] = new ProcessBuilder("ssh", machines.get(i), command);
			pb[i].inheritIO(); // redirect error to stdout
			processes[i] = pb[i].start();	
		}
		// wait for the machines 
		for(int i = 0; i < machines.size(); i++) {
			int errCode = processes[i].waitFor();
		}
	}
	
	private static void copyUMToTarget(String machineFrom, String machineTo, String um) throws Exception {
		// TODO:  make this parallel 
		String umFile = mapsPath + um + ".txt";
		// /tmp/mezzeddine/UM1.txt
		
		ProcessBuilder pb = new ProcessBuilder("scp", machineFrom + ":" + umFile, machineTo + ":" + umFile);
		Process process = pb.start();
		int errCode = process.waitFor();
		if(errCode == 0) {
//			System.out.println("[OK] Copy " + umFile + " from " + machineFrom + " to " + machineTo);
		}
		else {
			System.out.println("[X] FAIL COPY " + umFile + " from " + machineFrom + " to " + machineTo);
//			System.out.println(output(process.getErrorStream()));
		}
		
		
	}
		
	private static boolean copySplitsToSlaves(ArrayList <String> machines) throws Exception {
		ProcessBuilder[] pb = new ProcessBuilder[machines.size()]; 
		Process[] process = new Process[machines.size()];

		for(int i = 0; i < machines.size(); i++) {
			System.out.println("Copying splits to " + machines.get(i));
			pb[i] = new ProcessBuilder("ssh", machines.get(i), "mkdir -p " + splitsPath);
			process[i] = pb[i].start();
			int errCode = process[i].waitFor();
			if(errCode == 0) {
				pb[i] = new ProcessBuilder("scp", splitsPath + "S" + i + ".txt", machines.get(i) + ":" + splitsPath);
				process[i] = pb[i].start();
//				int errCode2 = process.waitFor();
	//			System.out.print(output(process.getErrorStream()));
			}
			else {
				System.out.println(output(process[i].getErrorStream()));
			}
		}
		for(int i = 0; i < machines.size(); i++) {
			process[i].waitFor();
		}
		return true;
	}
	
	private static void runLocalSlave() throws Exception {
		ProcessBuilder pb = new ProcessBuilder("/tmp/mezzeddine/SLAVE.jar");
		pb.inheritIO(); // redirect error to stdout
		Process process = pb.start();
		boolean errCode = process.waitFor(2, TimeUnit.SECONDS);
		if(errCode == true) {
			System.out.println(output(process.getInputStream()));
//			System.out.println(output(process.getErrorStream()));	
		}
		else {
			System.out.println("Timeout :(");
			process.destroy();
		}
	}
	
	private static ArrayList <String> getMachines() throws Exception {
		String fileName = machinesPath;
	   	File file = new File(fileName);
	    Scanner input = new Scanner(file); 
	    
		ArrayList <String> m = new ArrayList <String>();
	    while (input.hasNext()) {
	      String word  = input.next();
	      m.add(word);
	    }
	    return m;
	}
	
    private static String output(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + System.getProperty("line.separator"));
            }
        } finally {
            br.close();
        }
        return sb.toString();
    }
}
