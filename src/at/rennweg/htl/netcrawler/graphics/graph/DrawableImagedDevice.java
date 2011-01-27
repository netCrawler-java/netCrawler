package at.rennweg.htl.netcrawler.graphics.graph;

import graphics.GraphicsUtil;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import math.Rectangle;
import math.Vector2d;


public class DrawableImagedDevice extends DrawableNetworkDevice {
	
	private BufferedImage image;
	
	
	public DrawableImagedDevice(Object coveredVertex, BufferedImage image) {
		super(coveredVertex);
		
		this.image = image;
	}
	
	
	@Override
	public Rectangle drawingRect() {
		return new Rectangle(getPosition(), new Vector2d(
				image.getWidth(),
				image.getHeight()
		));
	}
	
	
	@Override
	public void drawDevice(Graphics g) {
		Rectangle drawingRect = drawingRect();
		
		GraphicsUtil.drawImage(g, image, drawingRect.leftTop());
	}
	
}