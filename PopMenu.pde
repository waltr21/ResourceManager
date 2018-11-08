
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
