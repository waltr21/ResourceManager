FileParser reader;
ArrayList<Process> processes;
ArrayList<Resource> resources;
ArrayList<ProcessAction> actions;
int step;
String currentStep;

void setup(){
    size(900, 800, OPENGL);
    smooth(4);
    hint(DISABLE_OPTIMIZED_STROKE);
    step = 0;
    String path = "/Users/ryanwalt/Downloads/CODE/Java/Processing/ResourceManager/";
    String file = "input5.data";
    reader = new FileParser(path + file);

    processes = new ArrayList<Process>();
    resources = new ArrayList<Resource>();
    actions = reader.getActions();
    currentStep = "";


    //int origin = (width/2) - ((reader.numProcesses * (60/2)) + ((reader.numProcesses-1) * (90/2)))/2;
    int origin = 80;
    int xOffset = 120;
    int yOffset = 250;
    for (int i = 0; i < reader.numProcesses; i++){
        processes.add(new Process("p" + i, origin, yOffset));
        origin += xOffset;
    }

    //origin = (width/2) - ((reader.numResources * (60/2)) + ((reader.numResources - 1) * (90/2)))/2;
    origin = 80;
    xOffset = 120;
    yOffset = 550;
    for (int i = 0; i < reader.numResources; i++){
        resources.add(new Resource("r" + i, origin, yOffset));
        origin += xOffset;
    }
    //n = new Node("p0", width/2, height/2);
}

void draw(){
    background(51);
    showNodes();
    fill(255);
    textSize(20);
    textAlign(CENTER);
    text(currentStep, width - 100, 50);

    for (Process p : processes){
        if (checkDeadlock(p, null)){
            currentStep = "Deadlock!";
        }
    }
}

private void showNodes(){
    for (Node n : processes){
        n.show();
    }

    for (Node n : resources){
        n.show();
    }
}

private Resource getResource(String label){
    for (Resource r : resources){
        if (r.getLabel().equals(label)){
            return r;
        }
    }

    return null;
}

private Process getProcess(String label){
    for (Process p : processes){
        if (p.getLabel().equals(label)){
            return p;
        }
    }

    return null;
}

private void processStep(){
    ProcessAction currentAction = actions.get(step);
    Process currentProcess = getProcess(currentAction.process);
    Resource currentResource = getResource(currentAction.resource);
    currentStep += String.format("%s %s %s\n", currentAction.process, currentAction.action, currentAction.resource);
    //System.out.println(String.format("%s - %s - %s", currentAction.process, currentAction.action, currentAction.resource));
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

private boolean checkDeadlock(Process start, Process current){
    //If this process is not waiting then we know this process is not locked.
    if (start.getWait().size() < 1){
        return false;
    }
    if (current != null){
        //If we are back where we started then we have a cycle.
        if (start == current){
            return true;
        }
    }
    else{
        current = start.getWait().get(0).getHolder();
    }
    if (current.getWait().size() < 1){
        return false;
    }

    Process nextProcess = current.getWait().get(0).getHolder();

    return checkDeadlock(start, nextProcess);



    // return false;
}

void keyPressed(){
    if (key == ' ' && step < actions.size()){
        processStep();
        step++;
    }
}
