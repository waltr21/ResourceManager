import java.io.*;
import java.util.ArrayList;

public class FileParser{
    public int numProcesses, numResources;
    public ArrayList<ProcessAction> actions;
    private String fileName;

    public FileParser(String fileName){
        this.fileName = fileName;
        actions = new ArrayList<ProcessAction>();
        readFile();
    }

    public void readFile(){
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

    public ArrayList<ProcessAction> getActions(){
        return actions;
    }

    public static void main(String[] args){
        FileParser fp = new FileParser("/Users/ryanwalt/Downloads/CODE/Java/Processing/ResourceManager/input3a.data");
        System.out.println(String.format("Processes: %d \nResources: %d", fp.numProcesses, fp.numResources));
    }
}

class ProcessAction{
    public String resource, process, action;

    public ProcessAction(String[] arr){
        process = arr[0];
        action = arr[1];
        resource = arr[2];
    }
}
