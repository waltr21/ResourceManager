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
