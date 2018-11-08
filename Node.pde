
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
            y += 5.0;
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
