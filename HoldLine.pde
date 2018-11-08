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
        incr = 0.01;
        riseCount = 0.0;
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

            incr += 0.03;
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
            currentY -= 5.0;
            riseCount += 5.0;
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
