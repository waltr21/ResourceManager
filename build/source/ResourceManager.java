import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class ResourceManager extends PApplet {

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
public void setup(){
    //Size/render library for the sketch.
    
    
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
public void draw(){
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
public void lockLines(Resource r){
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
public void keyPressed(){
    if (key == ' ' && step < actions.size()){
        processStep();
        step++;
    }

    if (key == 'r'){
        reset();
    }
}
/**
 * Class for displaying the lines that connect a process to a resource.
 */
public class HoldLine{
    float x1, x2, y1, y2, currentX, currentY;
    float incr, riseCount;
    boolean growing, wait, locked;
    Resource resource;

    /**
     * Constructor for the HoldLine class
     * @param x1 Starting x val for the line
     * @param y1 Starting y val for the line
     * @param x2 Ending x val for the line
     * @param y2 Ending y val for the line
     */
    public HoldLine(float x1, float y1, float x2, float y2){
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.currentX = x1;
        this.currentY = y1;
        incr = 0.01f;
        riseCount = 0.0f;
        growing = true;
        wait = false;
    }

    /**
     * Is this line waiting?
     */
    public void setWait(boolean b){
        wait = b;
    }

    /**
     * Is this line locked?
     */
    public void setLocked(boolean b){
        locked = b;
    }

    /**
     * Give a line a resource to reference
     * @param r Resource this line is connected to.
     */
    public void setResource(Resource r){
        resource = r;
    }

    /**
     * Get the resource this line is referencing.
     * @return Resource
     */
    public Resource getResource(){
        return resource;
    }

    /**
     * Is this line growing out? (Animation use)
     */
    public boolean isGrowing(){
        return growing;
    }

    /**
     * Draw the line so it expands out (Animation use)
     */
    private void grow(){
        if (incr < 1 && growing){
            currentX = lerp(x1, x2, incr);
            currentY = lerp(y1, y2, incr);

            incr += 0.03f;
        }
        else{
            growing = false;
        }
    }

    /**
     * Pull the resource up with the line (Animation use)
     */
    private void rise(){
        if (!growing && riseCount < 75 && !wait){
            currentY -= 5.0f;
            riseCount += 5.0f;
            resource.setY(currentY);
        }
    }


    /**
     * Display the line on the screen (Animation use)
     */
    public void show(){
        grow();
        rise();

        //Color depends on the state of the line.
        if(locked)
            stroke(255, 30, 30);
        else if (wait)
            stroke(30, 30, 255);
        else
            stroke(30, 255, 30);

        line(x1, y1, currentX, currentY);
    }
}

/**
 * Parent class for the processes and resources.
 */
public class Node{
    String label;
    float x, y, size;
    ArrayList<HoldLine> lines;

    /**
     * Constructor for the node class.
     * @param label Label for the node
     * @param x     x position
     * @param y     y position
     */
    public Node(String label, float x, float y){
        this.label = label;
        this.x = x;
        this.y = y;
        this.size = 60;
        lines = new ArrayList<HoldLine>();
    }

    /**
     * Get the label of this node.
     * @return Label as a string.
     */
    public String getLabel(){
        return label;
    }

    /**
     * Get the X position of this node.
     * @return X pos
     */
    public float getX(){
        return x;
    }

    /**
     * Get the Y position of this node.
     * @return Y pos
     */
    public float getY(){
        return y;
    }

    /**
     * Set the Y pos of this node.
     * @param tempY Y pos to set.
     */
    public void setY(float tempY){
        y = tempY;
    }

    /**
     * Add a line attacted to this node.
     * @param hl HoldLine object
     * @param r  Resource it is attached to.
     */
    public void addLine(HoldLine hl, Resource r){
        lines.add(hl);
        hl.setResource(r);
    }

    /**
     * Remove line from this node.
     * @param r Resource the line to remove is attached to.
     */
    public void removeLine(Resource r){
        for (HoldLine h : lines){
            if(r == h.getResource()){
                lines.remove(h);
                break;
            }
        }
    }

    /**
     * Used for drawing to the screen.
     */
    public void show(){
        for (HoldLine hl : lines){
            hl.show();
        }
        //Draw circle
        ellipseMode(CENTER);
        fill(100);
        strokeWeight(3);
        ellipse(x, y, size, size);

        //Draw label
        fill(255);
        textSize(25);
        textAlign(CENTER);
        text(label, x, y + 10);
    }
}

/**
 * Class for the process.
 */
class Process extends Node{
    private ArrayList<Resource> heldResources;
    private ArrayList<Resource> waitResources;
    private ArrayList<HoldLine> waitLines;

    private PopMenu menu;

    /**
     * Constructor for the Process class.
     * @param label Name of this process
     * @param x     X pos
     * @param y     Y pos
     */
    public Process(String label, int x, int y){
        //Call parent constructor
        super(label, x, y);
        heldResources = new ArrayList<Resource>();
        waitResources = new ArrayList<Resource>();
        waitLines = new ArrayList<HoldLine>();


        menu = new PopMenu(x, y - size * 2);
    }

    /**
     * Add a resource to this process.
     * @param r Resource to add.
     */
    public void addResource(Resource r){
        //If the resource is not currently being held. Simply just add it.
        if (!r.isHeld()){
            heldResources.add(r);
            //Line for display.
            addLine(new HoldLine(x, y, r.getX(), r.getY()), r);
            r.setHeld(true);
            r.setHolder(this);
        }
        //Else we have to wait for this resource.
        else{
            //Display menu
            menu.addWait(r.getLabel());
            waitResources.add(r);
            //Line for display.
            HoldLine temp = new HoldLine(x, y, r.getX(), r.getY());
            waitLines.add(temp);
            temp.setResource(r);
            temp.setWait(true);
        }
        r.addProcess(this);
    }

    /**
     * Notify this process that it is able to grab this resource. (Waiting on
     * this resource.)
     * @param r Resource to grab.
     */
    public void notify(Resource r){
        addLine(new HoldLine(x, y, r.getOriginX(), r.getOriginY()), r);
        menu.removeWait(r.getLabel());
        waitResources.remove(r);
        waitLines.clear();
    }

    /**
     * Release a resource for this process.
     * @param r Resource to release.
     */
    public void removeResource(Resource r){
        heldResources.remove(r);
        removeLine(r);
        r.setHeld(false);
        r.drop();
    }

    /**
     * Get the held resources
     * @return ArrayList of resources
     */
    public ArrayList<Resource> getHeld(){
        return heldResources;
    }

    /**
     * Get the resources that the process is waiting on.
     * @return ArrrayList of resources.
     */
    public ArrayList<Resource> getWait(){
        return waitResources;
    }

    /**
     * Get the lines that this process is attached with.
     * @return ArrayList of HoldLines
     */
    public ArrayList<HoldLine> getLines(){
        return lines;
    }

    /**
     * Get the wait lines that this process is attached with.
     * @return ArrayList of HoldLines
     */
    public ArrayList<HoldLine> getWaitLines(){
        return waitLines;
    }

    /**
     * Display this node. (had to override parent because of some color issues.)
     */
    public void show(){

        for (HoldLine hl : lines){
            hl.show();
        }

        for (HoldLine hl : waitLines){
            hl.show();
        }


        stroke(255);
        if(menu.isWaiting())
            stroke(247, 244, 81);

        //Draw circle
        ellipseMode(CENTER);
        fill(100);
        strokeWeight(3);
        ellipse(x, y, size, size);

        //Draw label
        fill(255);
        textSize(25);
        textAlign(CENTER);
        text(label, x, y + 10);
        menu.show();
    }
}

/**
 * Class for the resources
 */
class Resource extends Node{
    private boolean held, drop;
    private float originX, originY;
    private Process holder;
    private ArrayList<Process> processList;

    /**
     * Contructor for the resource class.
     * @param label Name of the resource.
     * @param x     X pos of the resource.
     * @param y     Y pos of the resource.
     */
    public Resource(String label, int x, int y){
        //Call parent constructor
        super(label, x, y);
        originX = x;
        originY = y;
        drop = false;
        processList = new ArrayList<Process>();
        holder = null;
    }

    /**
     * Is this resource being held by another process?
     * @return True if held. False otherwise.
     */
    public boolean isHeld(){
        return held;
    }

    /**
     * Add a process that is requesting this resource.
     * @param p [description]
     */
    public void addProcess(Process p){
        processList.add(p);
    }

    /**
     * Get the original x pos
     * @return Float x
     */
    public float getOriginX(){
        return originX;
    }

    /**
     * Get the original Y pos
     * @return Float Y
     */
    public float getOriginY(){
        return originY;
    }

    /**
     * Set this resource to held.
     * @param b True if want held. Else false.
     */
    public void setHeld(boolean b){
        held = b;
    }

    /**
     * Get the current holder of this resource.
     * @return [description]
     */
    public Process getHolder(){
        return holder;
    }

    /**
     * Set the holder of this resource.
     * @param p Process that holds this resource.
     */
    public void setHolder(Process p){
        holder = p;
    }

    /**
     * Drop the resource. Reset the holder and notify the next process that this
     * resource can now be used.
     */
    public void drop(){
        drop = true;
        processList.remove(0);
        holder = null;

        if(processList.size() > 0){
            processList.get(0).notify(this);
            held = true;
            holder = processList.get(0);
        }
    }

    /**
     * Drop this resource (Animation use.)
     */
    public void displayDrop(){
        if (y < originY && drop){
            y += 5.0f;
        }
        else{
            drop = false;
        }
    }

    /**
     * Show this resource.
     */
    public void show(){
        stroke(255);
        super.show();
        displayDrop();
    }

}

/**
 * Class for displaying the resource that a current process is waiting on.
 */
public class PopMenu{
    private float x, y, w, h;
    ArrayList<String> labels;
    String text;

    /**
     * Contructor for the PopMenu class.
     * @param x [description]
     * @param y [description]
     */
    public PopMenu(float x, float y){
        this.x = x;
        this.y = y;
        this.h = 120;
        this.w = 90;
        labels = new ArrayList<String>();
        this.text = "Waiting: \n";
    }

    /**
     * Add a resource to wait on.
     * @param s Label of resource.
     */
    public void addWait(String s){
        labels.add(s);
    }

    /**
     * Remove a process wait.
     * @param s Label of resource
     */
    public void removeWait(String s){
        for (int i = 0; i < labels.size(); i++){
            String label = labels.get(i);
            if (s.equals(label)){
                labels.remove(i);
            }
        }
    }

    /**
     * Does this Menu contain any labels?
     */
    public boolean isWaiting(){
        return labels.size() > 0;
    }

    /**
     * Display the Menu.
     */
    public void show(){
        //Only display if our process is waiting.
        if (labels.size() > 0){
            rectMode(CENTER);
            noFill();
            stroke(255);
            rect(x, y, w, h, 7);

            text = "Waiting: \n";
            for (String label : labels){
                text += label + "\n";
            }

            textSize(18);
            text(text, x, y - (h/3));
        }
    }
}
    public void settings() {  size(900, 800, OPENGL);  smooth(4); }
    static public void main(String[] passedArgs) {
        String[] appletArgs = new String[] { "ResourceManager" };
        if (passedArgs != null) {
          PApplet.main(concat(appletArgs, passedArgs));
        } else {
          PApplet.main(appletArgs);
        }
    }
}
