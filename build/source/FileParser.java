import java.io.*;
import java.util.ArrayList;

/**
 * Class to read in the .data files.
 */
public class FileParser{
    public int numProcesses, numResources;
    public ArrayList<ProcessAction> actions;
    private String fileName;

    /**
     * Constructor for the FileParser class.
     * @param fileName String for the name of the file.
     */
    public FileParser(String fileName){
        this.fileName = fileName;
        actions = new ArrayList<ProcessAction>();
        readFile();
    }

    /**
     * Read in the file.
     */
    private void readFile(){
        try{
            File file = new File(fileName);

            BufferedReader br = new BufferedReader(new FileReader(file));

            int count = 0;
            String temp;
            while ((temp = br.readLine()) != null){
                temp = temp.toLowerCase();
                String[] splitString = temp.split(" ");

                //Grab processes.
                if (count == 0)
                    numProcesses = Integer.parseInt(splitString[0]);
                //Grab Resources.
                else if (count == 1)
                    numResources = Integer.parseInt(splitString[0]);
                //Grab action
                else{
                    actions.add(new ProcessAction(splitString));
                }
                count++;
            }
        }
        catch(Exception e){
            System.out.println("Error in ReadpenFile: " + e);
        }
    }

    /**
     * Get the actions loaded in from the file.
     * @return ArrayList of ProcessActions
     */
    public ArrayList<ProcessAction> getActions(){
        return actions;
    }

    public static void main(String[] args){
        FileParser fp = new FileParser("/Users/ryanwalt/Downloads/CODE/Java/Processing/ResourceManager/input3a.data");
        System.out.println(String.format("Processes: %d \nResources: %d", fp.numProcesses, fp.numResources));
    }
}

/**
 * Class to hold the action in the data file
 */
class ProcessAction{
    public String resource, process, action;

    /**
     * Constructor for the ProcessAction class
     * @param arr Array of Strings representing the actions
     */
    public ProcessAction(String[] arr){
        process = arr[0];
        action = arr[1];
        resource = arr[2];
    }
}