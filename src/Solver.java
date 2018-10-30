import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;


import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class Solver extends JFrame implements MouseListener, MouseWheelListener, KeyListener {

	HashMap<Integer, Square> puzzle;
	BufferedImage paper;
	Graphics gPaper;
	int scroll;
	int xSelect, ySelect;
	boolean solving;
	
	public Solver () {
		super("Sudoku Solver");
		setSize(600, 600);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		puzzle= new HashMap<Integer, Square>();
		
		for (int i= 0; i<9; i++) {
			for (int j= 0; j<9; j++) {
				puzzle.put(j+i*9, new Square(j, i));
			}
		}
		
		paper= new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);
		gPaper= paper.getGraphics();
		scroll= 0;
		xSelect= 0;
		ySelect= 0;
		solving= false;
		
		addMouseListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
		
		setVisible(true);
		setSize(getWidth()+getInsets().left+getInsets().right, getHeight()+getInsets().top+getInsets().bottom);
        
        JOptionPane.showMessageDialog(this, "Welcome to the sudoku solver!\nPress H to see the key bindings", "Sudoku Solver", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static void main (String[] args) {
		new Solver();
	}
	
	public void paint (Graphics g) {
		gPaper.setColor(Color.white);
		gPaper.fillRect(0, 0, 600, 600);
		gPaper.setColor(Color.black);
		int s= 68;
		for (int i= 0; i<6; i++) {
			gPaper.fillRect(s, 0, 3, 600);
			gPaper.fillRect(0, s, 600, 3);
			s+= 65+(i%2)*68;
		}
		if (solving) {
            gPaper.setColor(Color.orange);
        } else {
            gPaper.setColor(Color.red);
        }
		for (int i= 0; i<4; i++) {
			gPaper.fillRect(i*198, 0, 6, 600);
			gPaper.fillRect(0, i*198, 600, 6);
		}
		int xIndent;
		int yIndent= 6;
		for (int i= 0; i<9; i++) {
			xIndent= 6;
			for (int j= 0; j<9; j++) {
				gPaper.drawImage(puzzle.get(j+9*i).display(), xIndent, yIndent, this);
				if (xSelect == j && ySelect == i && !solving) {
					gPaper.setColor(Color.green);
					gPaper.fillRect(xIndent, yIndent, 5, 62);
					gPaper.fillRect(xIndent, yIndent+62-4, 62, 4);
				}
				xIndent+= 62+((3*(j%3)*(j%3)-3*(j%3))/2+3);
			}
			yIndent+= 62+((3*(i%3)*(i%3)-3*(i%3))/2+3);
		}
		g.drawImage(paper, getInsets().left, getInsets().top, this);
	}
	
	public void startSolve () {
        if (!isValidSudoku()) {
            JOptionPane.showMessageDialog(this, "Entered soduku is not valid", "Sudoku Solver", JOptionPane.ERROR_MESSAGE);
            solving = false;
            return;
        }
		Set<Square> userInput= puzzle.values().stream().filter(Square::isFound).collect(Collectors.toSet());
		crossAndBoxKilling(userInput);
		repaint();
	}
	
	public void instantSolve () {
		removeKeyListener(this);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        Set<Integer> input = puzzle.keySet().stream().filter(k -> puzzle.get(k).isFound()).collect(Collectors.toSet());
        int current = 0;
        boolean needToFix = false;
		while (current < 9*9) {
            
            if (!needToFix && isValidSudoku()) {
                if (puzzle.get(current).isFound()) {
                    current++;
                } else {
                    puzzle.get(current).foundIt(0);
                }
            } else {
                needToFix = true;
                if (puzzle.get(current).isFound() && !input.contains(current)) {
                    if (puzzle.get(current).getVal() == 8) {
                        puzzle.get(current).reset();
                    } else {
                        puzzle.get(current).foundIt(puzzle.get(current).getVal()+1);
                        needToFix = false;
                    }
                } else {
                    current--;
                }
            }
		} 
        repaint();
        this.setCursor(Cursor.getDefaultCursor());
        JOptionPane.showMessageDialog(this, "Done", "Sudoku Solver", JOptionPane.INFORMATION_MESSAGE);
		addKeyListener(this);
	}
	
	public void humanSolving () {
        removeKeyListener(this);
		boolean found = findingTechniques();
        boolean update = updateAndSolidify();
        
        if (!update && !found) {
            JOptionPane.showMessageDialog(this, "Human like solver is not able to find anything", "Sudoku Solver", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "One round of human like solving has been done", "Sudoku Solver", JOptionPane.INFORMATION_MESSAGE);
        }
        
		repaint();
        addKeyListener(this);
	}
    
    public boolean findingTechniques() {
        boolean found = false;
        boolean buff;
        
        buff = lonelySquares();
		found |= buff;
        
        buff = lazerBoxLine();
        found |= buff;
        
        return found;
    }
    
    //turns sures in founds
    public boolean updateAndSolidify() {
        Set<Square> newFounds= puzzle.values().stream().filter(Square::isNotFound).filter(Square::isSure).collect(Collectors.toSet());
		crossAndBoxKilling(newFounds);
        return newFounds.size() > 0;
    }
	
	private void crossAndBoxKilling(Set<Square> data) {
		Iterator<Square> ite= data.iterator();
		Set<Square> boxFriends, hLineFriends, vLineFriends;
		while (ite.hasNext()) {
			final Square current= ite.next();
            //Get box of current and remove posibility of value in it
            boxFriends= puzzle.values().stream().filter(x -> x.isBox(current.box)).filter(Square::isNotFound).collect(Collectors.toSet());
            boxFriends.stream().forEach(elmnt -> elmnt.maybe[current.value]= false);
            
            //Get horizontal line of current and remove posibility of value int it
            hLineFriends= puzzle.values().stream().filter(x -> x.isHLine(current.hLine)).filter(Square::isNotFound).collect(Collectors.toSet());
            hLineFriends.stream().forEach(elmnt -> elmnt.maybe[current.value]= false);
            
            //Get vertical line of current and remove posibility of value int it
            vLineFriends= puzzle.values().stream().filter(x -> x.isVLine(current.vLine)).filter(Square::isNotFound).collect(Collectors.toSet());
            vLineFriends.stream().forEach(elmnt -> elmnt.maybe[current.value]= false);
		}
	}
	
    /**
     * Looks if a number is the only one of it's kind in the square and line
     **/
	public boolean lonelySquares () {
		boolean changed = false;
        
		List<Set<Square>> boxAndLineFriends= new ArrayList<Set<Square>>();
		Iterator<Square> iteboss;
		int p;
		Square proc;
		
		for (int i= 0; i<9; i++) { 
			//Lines and Boxes loop
			final int n= i;
			boxAndLineFriends.add(0, puzzle.values().stream().filter(x -> x.isBox(n)).filter(Square::isNotFound).collect(Collectors.toSet())); //Retrieves box i
			boxAndLineFriends.add(1, puzzle.values().stream().filter(x -> x.isHLine(n)).filter(Square::isNotFound).collect(Collectors.toSet())); //Retrieves horizontal line i
			boxAndLineFriends.add(2, puzzle.values().stream().filter(x -> x.isVLine(n)).filter(Square::isNotFound).collect(Collectors.toSet())); //Retrieves vertival line i
			for (int j= 0; j<9; j++) {
				//Numbers loop
				for (int x= 0; x<boxAndLineFriends.size(); x++) {
					iteboss= boxAndLineFriends.get(x).iterator();
					p= -1; //not found any
					while (iteboss.hasNext()) {
						proc= iteboss.next();
						if (proc.maybe[j]) {
							if (p == -1) {
								p= proc.idKey;
							} else {
								p= -2; //found 2
							}
						}
					}
					if (p >= 0) {
						proc= puzzle.get(p);
						for (int k= 0; k<proc.maybe.length; k++) {
                            if (k != j && proc.maybe[k]) {
                                changed = true;
                                proc.maybe[k]= false;
                            }
						}
					}
				}
			}
			boxAndLineFriends.clear();
		}
        return changed;
	}
	
    /**
     * If there is two or three instances of a number in a box forming a line we delete this number from the line in other boxes
     * 
     **/
	public boolean lazerBoxLine () {
        boolean changed = false;
		Set<Square> candidates, burned;
		Square[] selection;
		int hPos, vPos;
		for (int i= 0; i<9; i++) {
			//For each cube
			final int c= i;
			for (int j= 0; j<9; j++) {
				//for each number
				final int n= j;
				candidates = puzzle.values().stream().filter(Square::isNotFound).filter(sq -> sq.isBox(c)).filter(sq -> sq.maybe[n]).collect(Collectors.toSet());
				if (candidates.size() == 2 || candidates.size() == 3) {
					selection= candidates.toArray(new Square[0]);
					hPos= selection[0].hLine;
					vPos= selection[0].vLine;
					for (int k= 1; k<selection.length; k++) {
						if (hPos != selection[k].hLine) {
							hPos= -1;
						}
						if (vPos != selection[k].vLine) {
							vPos= -1;
						}
					}
                    if (hPos != -1 || vPos != -1) {
                        if (hPos != -1) {
                            final int pos= hPos;
                            burned= puzzle.values().stream().filter(Square::isNotFound).filter(sq -> sq.isHLine(pos)).filter(sq -> !sq.isBox(c)).filter(sq -> sq.maybe[n]).collect(Collectors.toSet());
                        } else {
                            final int pos= vPos;
                            burned= puzzle.values().stream().filter(Square::isNotFound).filter(sq -> sq.isVLine(pos)).filter(sq -> !sq.isBox(c)).filter(sq -> sq.maybe[n]).collect(Collectors.toSet());
                        }
                        burned.stream().forEach(sq -> sq.maybe[n]= false);
                        changed |= burned.size() > 0;
                    }
					
				}
			}
		}
        return changed;
	}
    
    public boolean isValidSudoku() {
        
        List<Set<Square>> boxAndLineFriends= new ArrayList<Set<Square>>();
        
        for (int i= 0; i<9; i++) { 
			//Lines and Boxes loop
			final int n= i;
			boxAndLineFriends.add(0, puzzle.values().stream().filter(x -> x.isBox(n)).filter(Square::isFound).collect(Collectors.toSet())); //Retrieves box i
			boxAndLineFriends.add(1, puzzle.values().stream().filter(x -> x.isHLine(n)).filter(Square::isFound).collect(Collectors.toSet())); //Retrieves horizontal line i
			boxAndLineFriends.add(2, puzzle.values().stream().filter(x -> x.isVLine(n)).filter(Square::isFound).collect(Collectors.toSet())); //Retrieves vertival line i
            for (int j= 0; j<9; j++) {
                final int m = j;
                for (Set<Square> blF : boxAndLineFriends) {                    
                    Set<Square> instances = blF.stream().filter(x -> x.isVal(m)).collect(Collectors.toSet());
                    if (instances.size() > 1) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }

	@Override
	public void mouseClicked(MouseEvent arg0) {
		if (!solving) {
			if (arg0.getButton() == MouseEvent.BUTTON1) {
				xSelect= (int)Math.floor((arg0.getX()-getInsets().left)/67);
				ySelect= (int)Math.floor((arg0.getY()-getInsets().top)/67);
				repaint();
			} 
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) {}

	@Override
	public void mouseReleased(MouseEvent arg0) {}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		if (!solving) {
			scroll+= arg0.getWheelRotation();
			if (Math.abs(scroll)%10 != 9) {
				puzzle.get(xSelect+9*ySelect).foundIt(Math.abs(scroll)%10);
			} else {
				puzzle.get(xSelect+9*ySelect).reset();
			}
			repaint();
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
        if (arg0.getKeyCode() == KeyEvent.VK_H) {
            JOptionPane.showMessageDialog(this, "HELP\n\nEnter numbers with the key pad or mouse wheel\nSelect numbers with mouse or arrows\n\nPress Enter to start solving mode\nPress I for an exhaustive instant find\nPress P for one round of human like solving", "Sudoku Solver", JOptionPane.QUESTION_MESSAGE);
        }
        
		if (!solving) {
			switch (arg0.getKeyCode()) {
				case KeyEvent.VK_NUMPAD0:
					puzzle.get(xSelect+9*ySelect).reset();
					break;
				case KeyEvent.VK_NUMPAD1:
					puzzle.get(xSelect+9*ySelect).foundIt(0);
					break;
				case KeyEvent.VK_NUMPAD2:
					puzzle.get(xSelect+9*ySelect).foundIt(1);
					break;
				case KeyEvent.VK_NUMPAD3:
					puzzle.get(xSelect+9*ySelect).foundIt(2);
					break;
				case KeyEvent.VK_NUMPAD4:
					puzzle.get(xSelect+9*ySelect).foundIt(3);
					break;
				case KeyEvent.VK_NUMPAD5:
					puzzle.get(xSelect+9*ySelect).foundIt(4);
					break;
				case KeyEvent.VK_NUMPAD6:
					puzzle.get(xSelect+9*ySelect).foundIt(5);
					break;
				case KeyEvent.VK_NUMPAD7:
					puzzle.get(xSelect+9*ySelect).foundIt(6);
					break;
				case KeyEvent.VK_NUMPAD8:
					puzzle.get(xSelect+9*ySelect).foundIt(7);
					break;
				case KeyEvent.VK_NUMPAD9:
					puzzle.get(xSelect+9*ySelect).foundIt(8);
					break;
				case KeyEvent.VK_ENTER:
					solving= true;
					startSolve();
					break;
				case KeyEvent.VK_DOWN:
					ySelect= (ySelect+1)%9; 
					break;
				case KeyEvent.VK_UP:
					ySelect= ySelect==0?8:ySelect-1;
					break;
				case KeyEvent.VK_LEFT:
					xSelect= xSelect==0?8:xSelect-1; 
					break;
				case KeyEvent.VK_RIGHT:
					xSelect= (xSelect+1)%9;
					break;
				default:
					return;
			} 
			repaint();
		} else {
            switch (arg0.getKeyCode()) {
                case KeyEvent.VK_I:
                    instantSolve();
                    break;
                case KeyEvent.VK_P:
                    humanSolving();;
                    break;
            }
		}
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {}

	@Override
	public void keyTyped(KeyEvent arg0) {}
}
