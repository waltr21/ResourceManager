
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
            y += 5.0;
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
