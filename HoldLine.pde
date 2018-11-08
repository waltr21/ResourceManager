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
        incr = 0.01;
        riseCount = 0.0;
        growing = true;
        wait = false;
    }

    public void setWait(boolean b){
        wait = b;
    }

    public void reset(){
        growing = true;
        incr = 0.80;
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

            incr += 0.03;
        }
        else{
            growing = false;
        }
    }

    private void rise(){
        if (!growing && riseCount < 75 && !wait){
            currentY -= 5.0;
            riseCount += 5.0;
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
