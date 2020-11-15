import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class testNative {
    public List<drawnThing> t = new ArrayList<>();

    JFrame frame = new JFrame();

    public static void main(String[] args) {
        testNative y = new testNative();
        y.t.add(new bluerect(0,0,100,100));
    }

    public testNative () {
        frame.add(new JPanel(){
            public void paintComponent(Graphics g){
                super.paintComponent(g);

                for(var thing : t)
                    thing.draw(g);

                g.setColor(Color.black);
                g.drawString("" + t.size(), 20, 20);

                repaint();
            }
        });

        frame.setSize(500, 500);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void addthing(drawnThing d){
        t.add(d);
    }
}

interface drawnThing {
    public void draw(Graphics g);

    public Color getcol();
}

class rect implements drawnThing{
    int x,y,w,h;

    public rect(int x, int y, int w, int h){
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;

    }

    @Override
    public Color getcol() {
        return new Color (100, 200, 100);
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(getcol());
        g.fillRect(x,y, w, h);
    }
}

class bluerect extends rect{
    public bluerect(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    @Override
    public Color getcol() {
        return new Color(0, 0, 255);
    }
}
