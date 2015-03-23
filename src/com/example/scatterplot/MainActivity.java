package com.example.scatterplot;

import android.app.Activity;
import java.util.Random;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import awt.java.awt.Font;
//import android.view.WindowManager;
import awt.java.awt.geom.Rectangle2D;

import java.text.NumberFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import prefuse.Constants;
import prefuse.PDisplay;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.ItemAction;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataShapeAction;
import prefuse.action.layout.AxisLabelLayout;
import prefuse.action.layout.AxisLayout;
import prefuse.controls.PanControl;
import prefuse.controls.ToolTipControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Table;
import prefuse.data.query.NumberRangeModel;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.AxisRenderer;
import prefuse.render.Renderer;
import prefuse.render.RendererFactory;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.io.LogTime;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import prefuse.visual.expression.VisiblePredicate;
import prefuse.visual.sort.ItemSorter;

public class MainActivity extends Activity
{
	Visualization vis;
	PDisplay display;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Remove notification bar
		// this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(createVisualizationV6(generateTable()));
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    display.setDamageRedraw(true);
	}
	
	@Override
	protected void onResume() {
	    super.onPause();
	    display.setDamageRedraw(true);
	}
	
	/**
	 * Phase 6 from the example http://www.ifs.tuwien.ac.at/~rind/w/doku.php/java/prefuse-scatterplot without user interaction and tooltip control
	 * 
	 * @param data
	 * @return
	 */

	private View createVisualizationV6(Table data)
	{
		LogTime.log("ALL-START");
		vis = new Visualization();
		display = new PDisplay(this, vis);
		display.setNumberThreads(4); // create 4 threads to render items
		display.disableHardwareAcceleration(); // disable hardware acceleration , otherwise some drawing code do not work. e.g. antialiasing on drawing with Path
		final Rectangle2D boundsData = new Rectangle2D.Double();
		final Rectangle2D boundsLabelsX = new Rectangle2D.Double();
		final Rectangle2D boundsLabelsY = new Rectangle2D.Double();

		// STEP 1: setup the visualized data

		VisualTable vt = vis.addTable("data", data);

		// add a new column containing a label string
		vt.addColumn("label", "CONCAT('NBZ: ', [NBZ], '; BMI: ', FORMAT([BMI],1))");

		/* STEP 2: set up renderers for the visual data */
		vis.setRendererFactory(new RendererFactory()
		{
			AbstractShapeRenderer sr = new ShapeRenderer(20);
			Renderer arY = new AxisRenderer(Constants.FAR_LEFT, Constants.CENTER);
			Renderer arX = new AxisRenderer(Constants.CENTER, Constants.FAR_BOTTOM);

			public Renderer getRenderer(VisualItem item)
			{
				return item.isInGroup("ylab") ? arY : item.isInGroup("xlab") ? arX : sr;
			}
		});

		// STEP 3: create actions to process the visual data

		AxisLayout x_axis = new AxisLayout("data", "NBZ", Constants.X_AXIS, VisiblePredicate.TRUE);

		AxisLayout y_axis = new AxisLayout("data", "BMI", Constants.Y_AXIS, VisiblePredicate.TRUE);

		x_axis.setLayoutBounds(boundsData);
		y_axis.setLayoutBounds(boundsData);

		AxisLabelLayout x_labels = new AxisLabelLayout("xlab", x_axis, boundsLabelsX);

		AxisLabelLayout y_labels = new AxisLabelLayout("ylab", y_axis, boundsLabelsY);

		// define the visible range for the y axis
		y_axis.setRangeModel(new NumberRangeModel(0, 40, 0, 40));

		// use square root scale for y axis
		y_axis.setScale(Constants.SQRT_SCALE);
		y_labels.setScale(Constants.SQRT_SCALE);

		// use a special format for y axis labels
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(1);
		nf.setMinimumFractionDigits(1);
		y_labels.setNumberFormat(nf);
		
		ColorAction color = new ColorAction("data", VisualItem.STROKECOLOR, ColorLib.rgb(100, 100, 255));

		int[] palette =	{ Constants.SHAPE_STAR, Constants.SHAPE_ELLIPSE };
		DataShapeAction shape = new DataShapeAction("data", "Insult", palette);
		ActionList draw = new ActionList();
		// this code was added to eliminate the call of the method "updateBounds" at display.post()
		draw.add(new Action(){
			@Override
			public void run(double frac)
			{
				PDisplay display = getVisualization().getDisplay(0);
				updateBounds(display, boundsData, boundsLabelsX, boundsLabelsY);
			}
		});		
		draw.add(x_axis);
		draw.add(y_axis);
		draw.add(x_labels);
		draw.add(y_labels);
		draw.add(new ItemAction()
		{
			//change the font size of the axis labels
			@Override
			public void process(VisualItem item, double frac)
			{
				if( !item.isInGroup("xlab") && !item.isInGroup("ylab") ) // process only the items of the axis
					return;
		        Font font = FontLib.getFont("SansSerif",Font.PLAIN,25);
				item.setFont(font);
			}
		});			
		draw.add(color);
		draw.add(shape);
		

		draw.add(new RepaintAction());

		vis.putAction("draw", draw);

		// --------------------------------------------------------------------
		// STEP 4: set up a display and controls

		display.setHighQuality(true);
		

		// show data items in front of axis labels
		display.setItemSorter(new ItemSorter()
		{
			public int score(VisualItem item)
			{
				int score = super.score(item);
				if (item.isInGroup("data"))
					score++;
				return score;
			}
		});

		ToolTipControl ttc = new ToolTipControl("label");
		display.addControlListener(ttc);
		
		display.addControlListener(new PanControl());
		display.addControlListener(new ZoomControl());
		display.addControlListener(new ZoomToFitControl());

		vis.run( "draw" );

		return display;
	}

	private Table generateTable()
	{
		Table table = new Table();

		// use a calendar for input of human-readable dates
		GregorianCalendar cal = new GregorianCalendar();

		// set up table schema
		table.addColumn("Date", Date.class);
		table.addColumn("BMI", double.class);
		table.addColumn("NBZ", int.class);
		table.addColumn("Insult", String.class);
		int items = 2500;
		table.addRows(items);
		
		Random randomGenerator = new Random();
		for(int i = 0; i < items / 2; i++)
		{
			table.set(i, 0, cal.getTime());
			table.set(i, 1, randomGenerator.nextDouble() * 40);
			table.set(i, 2, randomGenerator.nextDouble() * 400);
			table.set(i, 3, "T");
		}

		for(int i = items/2; i<items; i++)
		{
			table.set(i, 0, cal.getTime());
			table.set(i, 1, randomGenerator.nextDouble() * 40);
			table.set(i, 2, randomGenerator.nextDouble() * 400);
			table.set(i, 3, "F");
		}
		

 /**  add items outside of visible area (for performance check) **/ 		
//		for(int j = 5000; j<items; j++)
//		{
//			String insult = randomGenerator.nextBoolean() ? "T" : "F";
//			table.set(j, 0, cal.getTime());
//			table.set(j, 1, 400);
//			table.set(j, 2, randomGenerator.nextDouble() * 400);
//			table.set(j, 3, insult);
//		}		

		return table;
	}

	/**
	 * update bounds. this method is used at phase 6
	 * 
	 * @param display
	 * @param boundsData
	 * @param boundsLabelsX
	 * @param boundsLabelsY
	 */
	private static void updateBounds(PDisplay display, Rectangle2D boundsData, Rectangle2D boundsLabelsX, Rectangle2D boundsLabelsY)
	{

		int paddingLeft = 50;
		int paddingTop = 15;
		int paddingRight = 30;
		int paddingBottom = 35;

		int axisWidth = 20;
		int axisHeight = 10;

		awt.java.awt.Insets i = display.getInsets();

		int left = i.left + paddingLeft;
		int top = i.top + paddingTop;
		int innerWidth = display.getWidth() - i.left - i.right - paddingLeft - paddingRight;
		int innerHeight = display.getHeight() - i.top - i.bottom - paddingTop - paddingBottom;

		boundsData.setRect(left + axisWidth, top, innerWidth - axisWidth, innerHeight - axisHeight);
		boundsLabelsX.setRect(left + axisWidth, top + innerHeight - axisHeight, innerWidth - axisWidth, axisHeight);
		boundsLabelsY.setRect(left, top, innerWidth + paddingRight, innerHeight - axisHeight);
	}
	
}
