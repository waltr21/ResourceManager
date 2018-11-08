FileParser reader;
ArrayList<Process> processes;
ArrayList<Resource> resources, lockedResources;
ArrayList<ProcessAction> actions;
int step;
String currentStep, lockString;
boolean locked;

/**
 * Basically a Constructor for the processing draw screen.
 */
void setup(){
    //Size/render library for the sketch.
    size(900, 800, OPENGL);
    smooth(4);
    //Had a glitch with the lines. Dont exactly know why this fixes it.
    hint(DISABLE_OPTIMIZED_STROKE);
    reset();
}

public void reset(){
    step = 0;
    locked = false;

    //Load the data from the file.
    String path = "/Users/ryanwalt/Downloads/CODE/Java/Processing/ResourceManager/";
    String file = "input3b.data";
    reader = new FileParser(path + file);

    processes = new ArrayList<Process>();
    resources = new ArrayList<Resource>();
    lockedResources = new ArrayList<Resource>();

    //Get the actions from the loaded file.
    actions = reader.getActions();
    currentStep = "";
    lockString = "";

    //Draw the Process nodes at these XY coordinates
    int origin = 80;
    int xOffset = 120;
    int yOffset = 250;
    for (int i = 0; i < reader.numProcesses; i++){
        processes.add(new Process("p" + i, origin, yOffset));
        origin += xOffset;
    }

    //Draw the Resource nodes at these XY coordinates
    origin = 80;
    xOffset = 90;
    yOffset = 550;
    for (int i = 0; i < reader.numResources; i++){
        resources.add(new Resource("r" + i, origin, yOffset));
        origin += xOffset;
    }
}

/**
 * The main animation loop. (Updates at 60 fps)
 */
void draw(){
    //Background
    background(51);
    //Display all nodes
    showNodes();

    //Display actions / deadlock text.
    fill(255);
    textSize(20);
    textAlign(CENTER);
    text(currentStep, width - 100, 50);
    textSize(30);
    text(lockString, width/2, height - 100);

    //Loop through each process checking for a deadlock.
    for (Process p : processes){
        lockedResources.clear();
        //If we have a deadlock
        if (checkDeadlock(p, null)){
            lockString = "Deadlock!\n";
            locked = true;
            // Loop through and set all the lines to locked.
            for(Resource r : lockedResources){
                lockLines(r);
            }
        }
    }

}

/**
 * Display all nodes
 */
private void showNodes(){
    for (Node n : processes){
        n.show();
    }

    for (Node n : resources){
        n.show();
    }
}

/**
 * Get the resource based on its label name.
 * @param  label Resource label
 * @return       Resource found
 */
private Resource getResource(String label){
    for (Resource r : resources){
        if (r.getLabel().equals(label)){
            return r;
        }
    }

    return null;
}

/**
 * Get the process based on its label name.
 * @param  label Process label
 * @return       Process found
 */
private Process getProcess(String label){
    for (Process p : processes){
        if (p.getLabel().equals(label)){
            return p;
        }
    }

    return null;
}

/**
 * Go through the next step in our actions from the .data file.
 */
private void processStep(){
    //Current process action
    ProcessAction currentAction = actions.get(step);
    //Process involved in this action
    Process currentProcess = getProcess(currentAction.process);
    //Resource involved in this action.
    Resource currentResource = getResource(currentAction.resource);
    //Display the current step.
    currentStep += String.format("%s %s %s\n", currentAction.process, currentAction.action, currentAction.resource);

    //Process if the action is request of release.
    if (currentAction.action.equals("requests")){
        currentProcess.addResource(currentResource);
    }
    else if (currentAction.action.equals("releases")) {
        currentProcess.removeResource(currentResource);
    }
    else{
        System.out.println("Error in .data file");
    }
}

/**
 * Recursive function to check for deadlock.
 * @param  start   Starting process.
 * @param  current Current process this level of recursion is on.
 * @return         True if there is a dead lock. Otherwise false.
 */
private boolean checkDeadlock(Process start, Process current){
    //If this process is not waiting then we know this process is not locked.
    if (start.getWait().size() < 1){
        return false;
    }
    //If this is not the first itteration of recursion.
    if (current != null){
        //If we are back where we started then we have a cycle.
        if (start == current){
            return true;
        }
    }
    //If this is the first itteration.
    else{
        lockedResources.add(start.getWait().get(0));
        current = start.getWait().get(0).getHolder();
    }
    //If the current process is not waiting, then we know there is no deadlock.
    if (current.getWait().size() < 1){
        return false;
    }

    //Temporarily add this process to the locked list.
    lockedResources.add(current.getWait().get(0));

    //Grab the next process in the cycle.
    Process nextProcess = current.getWait().get(0).getHolder();

    //Recursion....
    return checkDeadlock(start, nextProcess);
}

/**
 * Lock lines with the resource r.
 * @param r Resource that a line holds.
 */
void lockLines(Resource r){
    //Loop through the process lines
    for (Process p : processes){
        //grab the held lines.
        for (HoldLine hl : p.getLines()){
            if (hl.getResource() == r){
                hl.setLocked(true);
            }
        }

        //Grab the waited lines.
        for (HoldLine hl : p.getWaitLines()){
            if (hl.getResource() == r){
                hl.setLocked(true);
            }
        }
    }
}

/**
 * Key press detection.
 */
void keyPressed(){
    if (key == ' ' && step < actions.size()){
        processStep();
        step++;
    }

    if (key == 'r'){
        reset();
    }
}
