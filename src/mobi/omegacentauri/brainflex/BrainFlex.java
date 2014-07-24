/**
*
* Copyright (c) 2014 Alexander Pruss
* Distributed under the GNU GPL v3 or later. For full terms see the file COPYING.
*
*/

package mobi.omegacentauri.brainflex;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class BrainFlex extends JFrame {
	private static final long serialVersionUID = 1L;
	static final int PACKET_NO = -1;
	static final int PACKET_MAYBE = 0;
	public static final String[] POWER_NAMES = { "delta", "theta", "low-alpha", "high-alpha", "low-beta", "high-beta",
		"low-gamma", "mid-gamma"
	};
	private List<Data> data;
	private List<Mark> marks;
	public long t0;
	private int lastSignal;
	private Data curData;
	private long signalCount;
	private boolean rawMode;
	private long lastPaintTime;
	public static final int MODE_NORMAL = 0;
	public static final int MODE_RAW = 0x02;
        private int mode = MODE_NORMAL;
    public boolean done;

	public BrainFlex() {
		done = false;
		t0 = System.currentTimeMillis();
		signalCount = 0;
		data = new ArrayList<Data>();
		marks = new ArrayList<Mark>();
		lastSignal = 100;

		setSize(640,480);
//		setLayout(new BorderLayout());
//		MyPanel graph = new MyPanel();
//		graph.setLayout(new FlowLayout(FlowLayout.RIGHT));
//		JButton markButton = new JButton("!");
//		markButton.addActionListener(new ActionListener() {		
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				Mark mark = new Mark(System.currentTimeMillis()-t0, signalCount);
//				System.out.println("Mark "+mark.t+ " "+mark.count);
//				marks.add(mark);
//			}
//		}); 	
//		graph.add(markButton);
//		add(graph,BorderLayout.SOUTH);
//		add(new MyPanel());

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		MyPanel graph = new MyPanel();
		add(graph);
		JButton markButton = new JButton("Mark");
		markButton.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				Mark mark = new Mark(System.currentTimeMillis()-t0, signalCount);
				System.out.println("Mark "+mark.t+ " "+mark.count);
				marks.add(mark);
			}
		}); 	
		JButton exitButton = new JButton("Exit");
		exitButton.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				done = true;
			}
		}); 	

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(exitButton);
		buttonPanel.add(markButton);
		
		add(buttonPanel);
		
		setVisible(true);
	}

	private class MyPanel extends JPanel {
		private static final long serialVersionUID = -1055183524854368685L;
		private static final int GRAPH_SPACING = 3;

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			Graphics2D g2 = (Graphics2D) g;
			Dimension s = getContentPane().getSize();

			if (rawMode) {
				drawRaw(g2, s);
			}
			else {
				drawPower(g2, s);
			}
		}
		
		private void drawRaw(Graphics2D g2, Dimension s) {
			int n = data.size();
			if (n<2)
				return;
			double tSize = Math.pow(2, Math.ceil(log2(data.get(n-1).count + 16)));
			double tScale = s.getWidth() / tSize;

			double ySize = 0;
			for (Data d: data) {
				if (d.raw > ySize)
					ySize = d.raw;
				else if (-d.raw > ySize)
					ySize = d.raw;
			}
			
			ySize *= 2;
			
			double yScale = s.getHeight() / ySize;
			
			g2.setColor(Color.BLUE);
			for (Mark m: marks) {
				Line2D lin = new Line2D.Double(m.count * tScale, 0,
						m.count * tScale, s.getHeight());
				g2.draw(lin);
			}

			g2.setColor(Color.BLACK);
			Data d0 = null;

			for (int i=0; i<n; i++) {
				Data d1 = data.get(i);
				if (0<i && d0.haveRaw && d1.haveRaw) { 
					Line2D lin = new Line2D.Double(d0.count * tScale, 
							(ySize / 2 - d0.raw) * yScale,
							d1.count * tScale, 
							(ySize / 2 - d1.raw) * yScale);
					g2.draw(lin);					
				}
				d0 = d1;
			}
		}

		private void drawPower(Graphics2D g2, Dimension s) {
			int n = data.size();
			if (n<2)
				return;
			double tSize = Math.pow(2, Math.ceil(log2(data.get(n-1).t + 1000 )));
			double tScale = s.getWidth() / tSize;
			double ySize = 0;
			for (Data d: data) 
				for (double y: d.power)
					if (y > ySize)
						ySize = y;

			double subgraphContentHeight = (s.getHeight() - GRAPH_SPACING * (1+POWER_NAMES.length) ) / (2+POWER_NAMES.length);
			double subgraphHeight = subgraphContentHeight + GRAPH_SPACING;
			double yScale = subgraphContentHeight / ySize;

			g2.setColor(Color.BLUE);
			for (Mark m: marks) {
				Line2D lin = new Line2D.Double(m.t * tScale, 0,
						m.t * tScale, s.getHeight());
				g2.draw(lin);
			}

			g2.setColor(Color.GREEN);
			for (int j = 0 ; j < POWER_NAMES.length + 1 ; j++) {
				Line2D lin = new Line2D.Double(0, subgraphContentHeight * ( j + 1 ) + GRAPH_SPACING / 2,
						s.getWidth(), subgraphContentHeight * ( j + 1 ) + GRAPH_SPACING / 2);
				g2.draw(lin);
			}
			
			for (int j = 0 ; j < POWER_NAMES.length ; j++) {
				g2.drawChars(POWER_NAMES[j].toCharArray(), 0, POWER_NAMES[j].length(), 
						0, (int)(j * subgraphHeight + ySize * .5 * yScale));
			}
			g2.drawChars("Attention".toCharArray(), 0, "Attention".length(), 
					0, (int)(POWER_NAMES.length * subgraphHeight + ySize * .5 * yScale));
			g2.drawChars("Meditation".toCharArray(), 0, "Meditation".length(), 
					0, (int)((1+POWER_NAMES.length) * subgraphHeight + ySize * .5 * yScale));

			g2.setColor(Color.BLACK);
			Data d0 = null;

			for (int i=0; i<n; i++) {
				Data d1 = data.get(i);
				if (0<i) { 
					if (d0.havePower && d1.havePower) { 
						for (int j=0; j<POWER_NAMES.length; j++) {
							Line2D lin = new Line2D.Double(d0.t * tScale, 
									(ySize - d0.power[j]) * yScale + j * subgraphHeight,
									d1.t * tScale, 
									(ySize - d1.power[j]) * yScale + j * subgraphHeight);
							g2.draw(lin);
						}
					}
					if (d0.haveAttention && d1.haveAttention) {
						Line2D lin = new Line2D.Double(d0.t * tScale, 
								(1 - d0.attention) * subgraphContentHeight + POWER_NAMES.length * subgraphHeight,
								d1.t * tScale, 
								(1 - d1.attention) * subgraphContentHeight + POWER_NAMES.length * subgraphHeight);
						g2.draw(lin);
					}
					if (d0.haveMeditation && d1.haveMeditation) {
						Line2D lin = new Line2D.Double(d0.t * tScale, 
								(1 - d0.meditation) * subgraphContentHeight + (1+POWER_NAMES.length) * subgraphHeight,
								d1.t * tScale, 
								(1 - d1.meditation) * subgraphContentHeight + (1+POWER_NAMES.length) * subgraphHeight);
						g2.draw(lin);
					}
				}
				d0 = d1;
			}
		}
	}


	public static void main(final String[] args) throws Exception
	{
		BrainFlex bf = new BrainFlex();
		bf.readData();
	}

	public double log2(double d) {
		return Math.log(d)/Math.log(2);
	}

	void readData() throws IOException {
		String comPort;

		comPort = JOptionPane.showInputDialog(null, "Brainlink serial port?");

		byte[] buffer = new byte[0];

		BrainLinkSerialLinkLL dataLink;

		System.out.println("CONNECTING");
		dataLink = new BrainLinkSerialLinkLL(comPort); 
		dataLink.start(9600);
		if (mode != MODE_NORMAL) {
		    dataLink.transmit(mode); 
		    if (mode >= 0x02)
		       dataLink.start(57600);
                    else if (mode == 0x01)
                       dataLink.start(1200);
		}
		//dataLink.start(57600);
		// 0x00 : 9600 : normal
		// 0x01 : 1200
		// 0x02 : 57600 : RAW
		// 0x03 : 57600 : lots of 0x82 signals, 4 bytes
		//		sleep(100);
		//dataLink.start(1200);
		//		sleep(100);
		System.out.println("CONNECTED");

		while (!done) {
			sleep(50);
			byte[] data = dataLink.receiveBytes();
			if (data.length > 0) {
				//brainLink.setFullColorLED(Color.BLUE);
				buffer = concat(buffer, data);
				int skipTo = 0;
				for (int i = 0; i < buffer.length; i++) {
					int length = detectPacket(buffer, i);
					if (length == PACKET_MAYBE) {
						byte[] newBuffer = new byte[buffer.length - i];
						System.arraycopy(buffer, i, newBuffer, 0, buffer.length - i);
						buffer = newBuffer;
						skipTo = 0;
						break;
					}
					else if (length > 0) {
						parsePacket(buffer, i, length);
						i += length - 1;
					}
					skipTo = i + 1;
				}

				if (skipTo > 0) {
					byte[] newBuffer = new byte[buffer.length - skipTo];
					System.arraycopy(buffer, skipTo, newBuffer, 0, buffer.length - skipTo);
					buffer = newBuffer;
				}
			}
		} 

		dataLink.stop();
	}

	private void parsePacket(byte[] buffer, int pos, int packetLength) {
		curData = new Data(System.currentTimeMillis()-t0);

		//System.out.println("Asked to parse "+pos+" "+packetLength+" of "+buffer.length);
		int end = pos + packetLength - 1;
		pos += 3;

		System.out.println("TIME "+System.currentTimeMillis());

		while((pos = parseRow(buffer, pos, end)) < end);

		if (curData.haveRaw || ( lastSignal == 0 && ( curData.havePower || curData.haveAttention || curData.haveMeditation ) ) ) {
			data.add(curData);
			if (System.currentTimeMillis() - lastPaintTime > 60) {
				lastPaintTime = System.currentTimeMillis();
				repaint();
			}
		}
	}

	public static void dumpData(byte[] buffer) {
		String out = "";
		for (byte b: buffer) {
			out += String.format("%02X ", 0xFF&(int)b);
		}
		System.out.println(out);
	}	

	private int parseRow(byte[] buffer, int pos, int end) {
		int excodeLevel = 0;
		while (pos < end && buffer[pos] == (byte)0x55) {
			excodeLevel++;
			pos++;
		}
		if (pos >= end)
			return end;
		byte code = buffer[pos];
		pos++;
		if (pos >= end)
			return end;
		int dataLength = 1;
		if ((code&(byte)0x80) == (byte)0x00) {
			dataLength = 1;
		}
		else {
			dataLength = 0xFF & (int)buffer[pos];
			pos++;
		}
		if (pos + dataLength > end)
			return end;
		parseData(excodeLevel, code, buffer, pos, dataLength);
		return pos + dataLength;
	}

	private void parseData(int excodeLevel, byte code, byte[] buffer, int pos, int dataLength) {
		int v;

		if (excodeLevel > 0) {
			System.out.println("UNPARSED "+excodeLevel+" "+code);
			return;
		}

		switch(code) {
		case (byte)0x02:
			System.out.println("POOR_SIGNAL "+(0xFF&(int)buffer[pos]));
		lastSignal = (0xFF)&(int)buffer[pos];
		break;
		case (byte)0x03:
			System.out.println("HEART_RATE "+(0xFF&(int)buffer[pos]));
		break;
		case (byte)0x04:
			v = 0xFF&(int)buffer[pos];
		System.out.println("ATTENTION "+v);
		curData.attention = v / 100.;
		curData.haveAttention = true;
		break;
		case (byte)0x05:
			v = 0xFF&(int)buffer[pos];
		System.out.println("MEDITATION "+v);
		curData.meditation = v / 100.;
		curData.haveMeditation = true;
		break;
		case (byte)0x06:
			System.out.println("8BIT_RAW "+(0xFF&(int)buffer[pos]));
		break;
		case (byte)0x07:
			System.out.println("RAW_MARKER "+(0xFF&(int)buffer[pos]));
		break;
		case (byte)0x80:
			curData.raw = (short)(((0xFF&(int)buffer[pos])<<8) | ((0xFF&(int)buffer[pos+1])));
			curData.haveRaw = true;
			System.out.println("RAW " + curData.raw);
		break;
		case (byte)0x81:
			System.out.println("EEG_POWER unsupported");
		break;
		case (byte)0x82:
			System.out.println("0x82 "+getUnsigned32(buffer,pos));
		break;
		case (byte)0x83:
			parseASIC_EEG_POWER(buffer, pos);
		break;
		case (byte)0x86:
			System.out.println("RRINTERVAL "+(((0xFF&(int)buffer[pos])<<8) | ((0xFF&(int)buffer[pos+1]))) );
		break;
		default:
			System.out.println("UNPARSED "+excodeLevel+" "+code);
			break;
		}
	}

	private long getUnsigned32(byte[] buffer, int pos) {
		return ((0xFF&(long)buffer[pos]) << 24) |
				((0xFF&(long)buffer[pos+1]) << 16) |
				((0xFF&(long)buffer[pos+2]) << 8) |
				((0xFF&(long)buffer[pos+3]));
	}

	private void parseASIC_EEG_POWER(byte[] buffer, int pos) {
		double sum = 0;
		for (int i=0; i<POWER_NAMES.length; i++) {
			int v = getUnsigned24(buffer, pos + 3 * i);
			System.out.println(POWER_NAMES[i]+" "+v);
			curData.power[i] = v;
			sum += v;
		}
		for (int i=0; i<POWER_NAMES.length; i++)
			curData.power[i] /= sum;
		curData.havePower = true;
	}

	private static int getUnsigned24(byte[] buffer, int pos) {
		return ((0xFF & (int)buffer[pos+0])<<16) |
				((0xFF & (int)buffer[pos+1])<<8) |
				((0xFF & (int)buffer[pos+2]));
	}

	private int detectPacket(byte[] buffer, int i) {
		if (buffer.length <= i)
			return PACKET_MAYBE;
		if (buffer[i] != (byte)0xAA)
			return PACKET_NO;
		if (buffer.length <= i+1)
			return PACKET_MAYBE;
		if (buffer[i+1] != (byte)0xAA)
			return PACKET_NO;
		if (buffer.length <= i+2)
			return PACKET_MAYBE;
		int pLength = 0xFF & (int)buffer[i+2];
		if (pLength > 169)
			return PACKET_NO;
		if (buffer.length < i+4+pLength)
			return PACKET_MAYBE;
		byte sum = 0;
		for (int j=0; j<pLength; j++)
			sum += buffer[i+3+j];
		sum ^= (byte)0xFF;
		signalCount++;
		if (sum != buffer[i+3+pLength]) {
			System.out.println("CSUMERROR "+sum+" vs "+buffer[i+3+pLength]);
			return PACKET_NO;
		}
		return 4+pLength;
	}

	private static byte[] concat(byte[] a, byte[] b) {
		int total = a.length + b.length;
		byte[] out = Arrays.copyOf(a, total);
		System.arraycopy(b, 0, out, a.length, b.length);
		return out;
	}

	private static void sleep(final int millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch (InterruptedException e)
		{
			System.out.println("Error while sleeping: " + e);
		}
	}

	public class Data {
		long t;
		long count;
		double[] power = new double[BrainFlex.POWER_NAMES.length];
		boolean havePower;
		boolean haveMeditation;
		boolean haveAttention;
		boolean haveRaw;
		double meditation;
		double attention;
		int raw;

		public Data(long t) {
			this.t = t;
			this.count = BrainFlex.this.signalCount - 1;
		}
	}
	
	public class Mark {
		long t;
		long count;
		
		public Mark(long t, long count) {
			this.t = t;
			this.count = count;
		}
	}
}
