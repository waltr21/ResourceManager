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
ArrayList<Resource> resources;
ArrayList<ProcessAction> actions;
int step;
String currentStep;

public void setup(){
    
    
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

public void draw(){
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

public void keyPressed(){
    if (key == ' ' && step < actions.size()){
        processStep();
        step++;
    }
}
public class HoldLine{
    float x1, x2, y1, y2, currentX, currentY;
    float incr, riseCount;
    boolean growing, wait;
    Resource resource;

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

    public void setWait(boolean b){
        wait = b;
    }

    public void reset(){
        growing = true;
        incr = 0.80f;
    }

    public void setResource(Resource r){
        resource = r;
    }

    public Resource getResource(){
        return resource;
    }

    public boolean isGrowing(){
        return growing;
    }

    private void grow(){
        if (incr < 1 && growing){
            // if (wait){
            //     // System.out.println("Y: " + y2 );
            //     // System.out.println("Y/2: " + y2/2 );
            //     stroke(30, 255, 30);
            //     currentX = lerp(x1, (x2 + x1)/2, incr);
            //     currentY = lerp(y1, (y2 + y1)/2, incr);
            // }
            // else{
            currentX = lerp(x1, x2, incr);
            currentY = lerp(y1, y2, incr);

            incr += 0.03f;
        }
        else{
            growing = false;
        }
    }

    private void rise(){
        if (!growing && riseCount < 75 && !wait){
            currentY -= 5.0f;
            riseCount += 5.0f;
            resource.setY(currentY);
        }
    }



    public void show(){
        grow();
        rise();
        if(!wait)
            stroke(30, 255, 30);
        else
            stroke(255, 30, 30);

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

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }

    public void setY(float tempY){
        y = tempY;
    }

    public void addLine(HoldLine hl, Resource r){
        lines.add(hl);
        hl.setResource(r);
    }

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

class Process extends Node{
    private ArrayList<Resource> heldResources;
    private ArrayList<Resource> waitResources;
    private PopMenu menu;

    public Process(String label, int x, int y){
        super(label, x, y);
        heldResources = new ArrayList<Resource>();
        waitResources = new ArrayList<Resource>();
        menu = new PopMenu(x, y - size * 2);
    }

    public void addResource(Resource r){
        if (!r.isHeld()){
            heldResources.add(r);
            addLine(new HoldLine(x, y, r.getX(), r.getY()), r);
            r.setHeld(true);
            r.setHolder(this);
        }
        else{
            // HoldLine temp = new HoldLine(x, y, r.getX(), r.getY());
            //heldResources.add(r);
            menu.addWait(r.getLabel());
            waitResources.add(r);
            // addLine(temp, r);
            // temp.setWait(true);
        }
        r.addProcess(this);
        // System.out.println("Resource added: " + r.getLabel());
    }

    public void notify(Resource r){
        addLine(new HoldLine(x, y, r.getOriginX(), r.getOriginY()), r);
        menu.removeWait(r.getLabel());
        waitResources.remove(r);
    }

    public void removeResource(Resource r){
        heldResources.remove(r);
        removeLine(r);
        r.setHeld(false);
        r.drop();
    }

    public ArrayList<Resource> getHeld(){
        return heldResources;
    }

    public ArrayList<Resource> getWait(){
        return waitResources;
    }


    public void show(){

        for (HoldLine hl : lines){
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

class Resource extends Node{
    private boolean held, drop;
    private float originX, originY;
    private Process holder;
    private ArrayList<Process> processList;

    public Resource(String label, int x, int y){
        super(label, x, y);
        originX = x;
        originY = y;
        drop = false;
        processList = new ArrayList<Process>();
        holder = null;
    }

    public boolean isHeld(){
        return held;
    }

    public void addProcess(Process p){
        processList.add(p);
    }

    public float getOriginX(){
        return originX;
    }

    public float getOriginY(){
        return originY;
    }

    public void setHeld(boolean b){
        held = b;
    }

    public Process getHolder(){
        return holder;
    }

    public void setHolder(Process p){
        holder = p;
    }

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

    public void displayDrop(){
        if (y < originY && drop){
            y += 5.0f;
        }
        else{
            drop = false;
        }
    }

    public void show(){
        stroke(255);
        super.show();
        displayDrop();
    }

}
public class PopMenu{
    private float x, y, w, h;
    ArrayList<String> labels;
    String text;

    public PopMenu(float x, float y){
        this.x = x;
        this.y = y;
        this.h = 120;
        this.w = 90;
        labels = new ArrayList<String>();
        this.text = "Waiting: \n";
    }

    public void addWait(String s){
        labels.add(s);
    }

    public void removeWait(String s){
        for (int i = 0; i < labels.size(); i++){
            String label = labels.get(i);
            if (s.equals(label)){
                labels.remove(i);
            }
        }
    }

    public boolean isWaiting(){
        return labels.size() > 0;
    }

    public void show(){
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
