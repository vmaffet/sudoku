import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Square {
		
	private int hLine;
	private int vLine;
	private int box;
	private int idKey;
	
	private boolean found;
	private int value;
	private boolean[] maybe;
	
	public Square (int x, int y) {
		vLine= x;
		hLine= y;
		idKey= vLine+9*hLine;
		box= (int)(Math.floor(vLine/3)+3*Math.floor(hLine/3));
		found= false;
		maybe= new boolean[9];
		for (int i= 0; i<9; i++) {
			maybe[i]= true;
		}
	}
	
	public BufferedImage display () {
		BufferedImage vision= new BufferedImage(62, 62, BufferedImage.TYPE_INT_RGB);
		Graphics gVision= vision.getGraphics();
		
		if (found) {
			gVision.setColor(Color.white);
			gVision.fillRect(0, 0, 62, 62);
			gVision.setColor(Color.blue);
			gVision.setFont(new Font("monospace", 0, 30));
			FontMetrics fm= gVision.getFontMetrics();
			gVision.drawString(Integer.toString(value+1), (62-fm.stringWidth("0"))/2, 40);
		} else {
			gVision.setColor(Color.white);
			gVision.fillRect(0, 0, 62, 62);
			gVision.setColor(Color.black);
			gVision.setFont(new Font("monospace", 0, 15));
			FontMetrics fm= gVision.getFontMetrics();
			String s1= "";
			for (int i= 0; i<3; i++) {
				if (maybe[i]) {
					s1+= (i+1)+" ";
				} else {
					s1+= "   ";
				}
			}
			String s2= "";
			for (int i= 3; i<6; i++) {
				if (maybe[i]) {
					s2+= (i+1)+" ";
				} else {
					s2+= "   ";
				}
			}
			String s3= "";
			for (int i= 6; i<9; i++) {
				if (maybe[i]) {
					s3+= (i+1)+" ";
				} else {
					s3+= "   ";
				}
			}
			gVision.drawString(s1, (62-fm.stringWidth("0 0 0 "))/2, 15);
			gVision.drawString(s2, (62-fm.stringWidth("0 0 0 "))/2, 35);
			gVision.drawString(s3, (62-fm.stringWidth("0 0 0 "))/2, 55);
		}
		
		return vision;
	}
    
    public int getVal() {
        return value;
    }
    
    public int getBox() {
        return box;
    }
    
    public int getHLine() {
        return hLine;
    }
    
    public int getVLine() {
        return vLine;
    }
    
    public int getID() {
        return idKey;
    }
    
    public int getMaybeLength() {
        return maybe.length;
    }
    
    public boolean getMaybe(int k) {
        if (k >= 0 && k < maybe.length) {
            return maybe[k];
        }
        return false;
    }
    
    public boolean setMaybe(int k, boolean b) {
        if (k >= 0 && k < maybe.length) {
            boolean old = maybe[k];
            maybe[k] = b;
            return old != b;
        }
        return false;
    }
	
	public void foundIt (int val) {
		found= true;
		value= val;
		for (int i= 0; i<maybe.length; i++) {
			maybe[i]= false;
		}
		maybe[value]=true;
	}
	
	public void reset () {
		found= false;
		maybe= new boolean[9];
		for (int i= 0; i<9; i++) {
			maybe[i]= true;
		}
	}
	
	public boolean isFound () {
		return found;
	}
	
	public boolean isNotFound () {
		return !found;
	}
	
	public boolean isBox (int n) {
		return (n == box);
	}
    
    public boolean isSameBox (Square sq) {
		return (sq.getBox() == box);
	}
	
	public boolean isHLine (int n) {
		return (n == hLine);
	}
    
    public boolean isSameHLine (Square sq) {
		return (sq.getHLine() == hLine);
	}
	
	public boolean isVLine (int n) {
		return (n == vLine);
	}
    
    public boolean isSameVLine (Square sq) {
		return (sq.getVLine() == vLine);
	}
    
    public boolean isVal(int n) {
        return (n == value);
    }
	
	public boolean isSure () {
		int t= -1;
		for (int i= 0; i<maybe.length; i++) {
			if (maybe[i]) {
				if (t == -1) {
					t= i;
				} else {
					return false;
				}
			}
		}
		if (t != -1) {
			value= t;
			found= true;
			return true;
		} else {
			return false;
		}
	}
	
	public String toString () {
		return String.format("Square %d at %d;%d found : %b", idKey, vLine, hLine, found);
	}
	
}
