package mobi.omegacentauri.brainflex;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollBar;

public abstract class GraphPanel extends JPanel {
	private static final long serialVersionUID = -7210732205864749654L;
	double startT;
	double endT;
	double maxT;
	double tScale;
	double yScale;
	double subgraphHeight;
	private JScrollBar scrollBar;
	protected BrainFlexGUI gui;
	protected MindFlexReader mfr;
	protected ViewerWindow w;
	protected List<?> origData;
	
	public GraphPanel(BrainFlexGUI gui, ViewerWindow w, List<?> data) {
		super();
		this.w = w;
		this.gui = gui;
		this.mfr = gui.getMindFlexReader();
		this.scrollBar = w.scrollBar;
		this.origData = data;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		draw((Graphics2D)g, getSize(), gui.getMarksCopy());
	}
	
	protected abstract void draw(Graphics2D g2, Dimension s, List<BrainFlexGUI.Mark> marks);
	
	double scaleT(double t) {
		return (t - startT) * tScale;
	}
	
	void calculateTSize( Dimension s, double currentSize, double visibleLimit, double minVisible, double scrollBarScale ) {
		maxT = currentSize;
	
		double tSize;
		
		if (maxT >= visibleLimit) {
			tSize = visibleLimit;
			int sbMax = (int)(scrollBarScale * maxT);
			int sbVis = (int)(scrollBarScale * visibleLimit);

			if (! scrollBar.isVisible() || scrollBar.getMaximum() != sbMax || scrollBar.getVisibleAmount() != sbVis) {
				scrollBar.setMaximum(sbMax);
				scrollBar.setVisibleAmount(sbVis);
				scrollBar.setMinimum(0);
				scrollBar.setVisible(true);
				scrollBar.setValue((int)((maxT - visibleLimit) * scrollBarScale ));
				startT = maxT-visibleLimit;
			}
			else {					
				startT = scrollBar.getValue() / scrollBarScale;
			}
		}
		else {
			scrollBar.setVisible(false);
			startT = 0;
			tSize = Math.pow(2, Math.ceil(log2(Math.max(maxT - startT, 16.))));
		}
		endT = startT + tSize;
		tScale = s.getWidth() / tSize;
	}
	
	void scaledLine(Graphics2D g2, double t1, double y1, double t2, double y2, int subgraph) {
		if (subgraph == 5 && t1 < 1000) {
			System.out.println(""+t1+","+y1+","+t2+","+y2+" "+scaleT(t1)+","+ 
					(y1 * yScale + subgraphHeight * subgraph)+","+
					scaleT(t2)+","+ 
					(y2 * yScale + subgraphHeight * subgraph));
		}
		g2.draw(new Line2D.Double(scaleT(t1), 
				y1 * yScale + subgraphHeight * subgraph,
				scaleT(t2), 
				y2 * yScale + subgraphHeight * subgraph));
	}

	public double log2(double d) {
		return Math.log(d)/Math.log(2);
	}	
}
