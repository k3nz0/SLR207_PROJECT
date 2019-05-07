import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class DEPLOY {
	/*
	 * Reads machines from file, tries to connect
	 * for working machines, copy slave from /tmp/mezzeddine/SLAVE.jar to working machine with scp
	 * */
	static String slavePath = "/tmp/mezzeddine/SLAVE.jar";
	static String machinesPath = "/tmp/mezzeddine/machines";
	
	public static void main(String[] args) throws Exception {

	    ArrayList <String> machines = new ArrayList<String>();
	    ArrayList <String> workingMachines = new ArrayList<String>();
	    
	    machines = getMachines();
		
	    for(String machine : machines) {
	    	boolean res = tryConnect(machine);
	    	if(res) {
	    		workingMachines.add(machine);
		    	System.out.println("[OK] " + machine + " is up !");
	    	}
	    	else System.out.println("[X] " + machine + " down !");
	    }
		
	    copySlaves(workingMachines);

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
	
	private static void copySlaves(ArrayList <String> workingMachines) throws Exception {
		ProcessBuilder[] pb = new ProcessBuilder[workingMachines.size()]; 
		Process[] process = new Process[workingMachines.size()];
		
		for(int i = 0; i < workingMachines.size(); i++) {
			System.out.println("Copying slave from" + slavePath  + " to " + workingMachines.get(i));
			
			pb[i] = new ProcessBuilder("ssh", workingMachines.get(i), "mkdir -p /tmp/mezzeddine");
			process[i] = pb[i].start();
			int errCode = process[i].waitFor();
			if(errCode == 0) {
				pb[i] = new ProcessBuilder("scp", slavePath, workingMachines.get(i) + ":/tmp/mezzeddine");
				process[i] = pb[i].start();
//				int errCode2 = process[i].waitFor();
//				System.out.print(output(process.getErrorStream()));
//				return errCode2 == 0;
			}
//			else {
//				System.out.println(output(process[i].getErrorStream()));
//				return false;
//			}			
		}
		for(int i = 0; i < workingMachines.size(); i++) {
			process[i].waitFor();
		}
	}
	private static boolean tryConnect(String machine) throws Exception {
		ProcessBuilder pb = new ProcessBuilder("ssh", machine, "hostname");
		Process process = pb.start();
		boolean errCode = process.waitFor(10, TimeUnit.SECONDS);
		if(errCode == true) {
			String stdout = output(process.getInputStream());
			String stderr = output(process.getErrorStream());
//			System.out.print(stdout);
			if(stderr != null  && !stderr.isEmpty()) {
				errCode = false;
			}
		}
		else {
			System.out.println("Timeout :(");
//			System.out.println(output(process.getErrorStream()));	
			process.destroy();
		}
		return errCode;
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
