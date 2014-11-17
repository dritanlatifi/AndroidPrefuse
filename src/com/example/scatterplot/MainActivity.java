package com.example.scatterplot;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import awt.java.awt.geom.Rectangle2D;

import java.text.NumberFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import prefuse.Constants;
import prefuse.PDisplay;
import prefuse.Visualization;
import prefuse.action.ActionList;
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
import prefuse.render.DefaultRendererFactory;
import prefuse.render.Renderer;
import prefuse.render.RendererFactory;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
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
		vis = new Visualization();
		display = new PDisplay(this, vis);
		display.disableHardwareAcceleration(); // disable hardware accelaration , otherwise some drawing code do not work. e.g. antialiasing on drawing with Path
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
			AbstractShapeRenderer sr = new ShapeRenderer(30);
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
		y_axis.setRangeModel(new NumberRangeModel(1, 40, 1, 40));

		// use square root scale for y axis
		y_axis.setScale(Constants.SQRT_SCALE);
		y_labels.setScale(Constants.SQRT_SCALE);

		// use a special format for y axis labels
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(1);
		nf.setMinimumFractionDigits(1);
		y_labels.setNumberFormat(nf);
		
		ColorAction color = new ColorAction("data", VisualItem.STROKECOLOR, ColorLib.rgb(100, 100, 255));

		int[] palette =
		{ Constants.SHAPE_STAR, Constants.SHAPE_ELLIPSE };
		DataShapeAction shape = new DataShapeAction("data", "Insult", palette);

		ActionList draw = new ActionList();
		draw.add(x_axis);
		draw.add(y_axis);
		draw.add(x_labels);
		draw.add(y_labels);
		draw.add(color);
		draw.add(shape);
		draw.add(new RepaintAction());
		vis.putAction("draw", draw);

		// --------------------------------------------------------------------
		// STEP 4: set up a display and controls

		display.setHighQuality(true);
		display.setSize(700, 450); // TODO for Dritan: this method is still not implemented

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
		

		// STEP 5: launching the visualization. The visualization must run after
		// the Display is ready (Android View)
		// TODO for Dritan: using dispay.post seems to be not a good solution.
		// Fix this before releasing the final solution
		display.post(new Runnable()
		{
			@Override
			public void run()
			{
				updateBounds(display, boundsData, boundsLabelsX, boundsLabelsY);
				vis.run("draw");
			}
		});

		return display;
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

		int paddingLeft = 30;
		int paddingTop = 15;
		int paddingRight = 30;
		int paddingBottom = 15;

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

	/**
	 * Phase 5 from the example http://www.ifs.tuwien.ac.at/~rind/w/doku.php/java/prefuse-scatterplot without user interaction and tooltip control
	 * 
	 * @param data
	 * @return
	 */

	private View createVisualizationV5(Table data)
	{
		final Visualization vis = new Visualization();
		PDisplay display = new PDisplay(this, vis);

		// STEP 1: setup the visualized data

		VisualTable vt = vis.addTable("data", data);

		// add a new column containing a label string
		vt.addColumn("label", "CONCAT('NBZ: ', [NBZ], '; BMI: ', FORMAT([BMI],1))");

		/* STEP 2: set up renderers for the visual data */
		vis.setRendererFactory(new RendererFactory()
		{
			AbstractShapeRenderer sr = new ShapeRenderer(11);
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

		AxisLabelLayout x_labels = new AxisLabelLayout("xlab", x_axis);

		AxisLabelLayout y_labels = new AxisLabelLayout("ylab", y_axis);

		// define the visible range for the y axis
		y_axis.setRangeModel(new NumberRangeModel(1, 40, 1, 40));

		// use square root scale for y axis
		y_axis.setScale(Constants.SQRT_SCALE);
		y_labels.setScale(Constants.SQRT_SCALE);

		// use a special format for y axis labels
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(1);
		nf.setMinimumFractionDigits(1);
		y_labels.setNumberFormat(nf);

		ColorAction color = new ColorAction("data", VisualItem.STROKECOLOR, ColorLib.rgb(100, 100, 255));

		int[] palette =
		{ Constants.SHAPE_STAR, Constants.SHAPE_ELLIPSE };
		DataShapeAction shape = new DataShapeAction("data", "Insult", palette);

		ActionList draw = new ActionList();
		draw.add(x_axis);
		draw.add(y_axis);
		draw.add(x_labels);
		draw.add(y_labels);
		draw.add(color);
		draw.add(shape);
		draw.add(new RepaintAction());
		vis.putAction("draw", draw);

		// --------------------------------------------------------------------
		// STEP 4: set up a display and controls

		display.setHighQuality(true);
		display.setSize(700, 450);

		display.setBorders(15, 30, 15, 30);

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

		// STEP 5: launching the visualization. The visualization must run after
		// the Display is ready (Android View)
		// TODO for Dritan: using dispay.post seems to be not a good solution.
		// Fix this before releasing the final solution
		display.post(new Runnable()
		{
			@Override
			public void run()
			{
				vis.run("draw");
			}
		});

		return display;
	}

	/**
	 * Phase 4 from the example http://www.ifs.tuwien.ac.at/~rind/w/doku.php/java/prefuse-scatterplot For now (07.11.2014) the interaction is not needed. Therefore I skip this phase => code of this
	 * phase is the same with phase 3
	 * 
	 * @param data
	 * @return
	 */

	private View createVisualizationV4(Table data)
	{
		final Visualization vis = new Visualization();
		PDisplay display = new PDisplay(this, vis);

		// STEP 1: setup the visualized data

		vis.add("data", data);

		/* STEP 2: set up renderers for the visual data */
		vis.setRendererFactory(new RendererFactory()
		{
			AbstractShapeRenderer sr = new ShapeRenderer(11);
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

		AxisLabelLayout x_labels = new AxisLabelLayout("xlab", x_axis);

		AxisLabelLayout y_labels = new AxisLabelLayout("ylab", y_axis);

		ColorAction color = new ColorAction("data", VisualItem.STROKECOLOR, ColorLib.rgb(100, 100, 255));

		int[] palette =
		{ Constants.SHAPE_STAR, Constants.SHAPE_ELLIPSE };
		DataShapeAction shape = new DataShapeAction("data", "Insult", palette);

		ActionList draw = new ActionList();
		draw.add(x_axis);
		draw.add(y_axis);
		draw.add(x_labels);
		draw.add(y_labels);
		draw.add(color);
		draw.add(shape);
		draw.add(new RepaintAction());
		vis.putAction("draw", draw);

		// --------------------------------------------------------------------
		// STEP 4: set up a display and controls

		display.setHighQuality(true);
		display.setSize(700, 450);

		display.setBorders(15, 30, 15, 30);

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

		// STEP 5: launching the visualization. The visualization must run after
		// the Display is ready (Android View)
		// TODO for Dritan: using dispay.post seems to be not a good solution.
		// Fix this before releasing the final solution
		display.post(new Runnable()
		{
			@Override
			public void run()
			{
				vis.run("draw");
			}
		});

		return display;
	}

	/**
	 * Phase 3 from the example http://www.ifs.tuwien.ac.at/~rind/w/doku.php/java/prefuse-scatterplot without tooltip control
	 * 
	 * @param data
	 * @return
	 */

	private View createVisualizationV3(Table data)
	{
		final Visualization vis = new Visualization();
		PDisplay display = new PDisplay(this, vis);

		// STEP 1: setup the visualized data

		vis.add("data", data);

		/* STEP 2: set up renderers for the visual data */
		vis.setRendererFactory(new RendererFactory()
		{
			AbstractShapeRenderer sr = new ShapeRenderer(11);
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

		AxisLabelLayout x_labels = new AxisLabelLayout("xlab", x_axis);

		AxisLabelLayout y_labels = new AxisLabelLayout("ylab", y_axis);

		ColorAction color = new ColorAction("data", VisualItem.STROKECOLOR, ColorLib.rgb(100, 100, 255));

		int[] palette =
		{ Constants.SHAPE_STAR, Constants.SHAPE_ELLIPSE };
		DataShapeAction shape = new DataShapeAction("data", "Insult", palette);

		ActionList draw = new ActionList();
		draw.add(x_axis);
		draw.add(y_axis);
		draw.add(x_labels);
		draw.add(y_labels);
		draw.add(color);
		draw.add(shape);
		draw.add(new RepaintAction());
		vis.putAction("draw", draw);

		// --------------------------------------------------------------------
		// STEP 4: set up a display and controls

		display.setHighQuality(true);
		display.setSize(700, 450);

		display.setBorders(15, 30, 15, 30);

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

		// STEP 5: launching the visualization. The visualization must run after
		// the Display is ready (Android View)
		// TODO for Dritan: using dispay.post seems to be not a good solution.
		// Fix this before releasing the final solution
		display.post(new Runnable()
		{
			@Override
			public void run()
			{
				vis.run("draw");
			}
		});

		return display;
	}

	/**
	 * Phase 2 from the example http://www.ifs.tuwien.ac.at/~rind/w/doku.php/java/prefuse-scatterplot without tooltip control
	 * 
	 * @param data
	 * @return
	 */

	private View createVisualizationV2(Table data)
	{
		final Visualization vis = new Visualization();
		PDisplay display = new PDisplay(this, vis);

		// STEP 1: setup the visualized data

		vis.add("data", data);

		/* STEP 2: set up renderers for the visual data */

		vis.setRendererFactory(new DefaultRendererFactory(new ShapeRenderer(12)));

		// STEP 3: create actions to process the visual data

		AxisLayout x_axis = new AxisLayout("data", "NBZ", Constants.X_AXIS, VisiblePredicate.TRUE);

		AxisLayout y_axis = new AxisLayout("data", "BMI", Constants.Y_AXIS, VisiblePredicate.TRUE);

		ColorAction color = new ColorAction("data", VisualItem.STROKECOLOR, ColorLib.rgb(100, 100, 255));

		int[] palette =
		{ Constants.SHAPE_STAR, Constants.SHAPE_ELLIPSE };
		DataShapeAction shape = new DataShapeAction("data", "Insult", palette);

		ActionList draw = new ActionList();
		draw.add(x_axis);
		draw.add(y_axis);
		draw.add(color);
		draw.add(shape);
		draw.add(new RepaintAction());
		vis.putAction("draw", draw);

		// --------------------------------------------------------------------
		// STEP 4: set up a display and controls
		display.setHighQuality(true);
		display.setSize(700, 450);

		// display.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
		// // TODO for Dritan: setBorder method was in JComponent. See if there
		// is a similar method in Android.View
		display.setBorders(15, 30, 15, 30);

		// STEP 5: launching the visualization. The visualization must run after
		// the Display is ready (Android View)
		// TODO for Dritan: using dispay.post seems to be not a good solution.
		// Fix this before releasing the final solution
		display.post(new Runnable()
		{
			@Override
			public void run()
			{
				vis.run("draw");
			}
		});

		return display;
	}

	/**
	 * Phase 1 from the example http://www.ifs.tuwien.ac.at/~rind/w/doku.php/java/prefuse-scatterplot
	 * 
	 * @param data
	 * @return
	 */
	private View createVisualizationV1(Table data)
	{
		final Visualization vis = new Visualization();
		PDisplay display = new PDisplay(this, vis);

		// --------------------------------------------------------------------
		// STEP 1: setup the visualized data

		vis.add("data", data);

		// --------------------------------------------------------------------
		// STEP 2: set up renderers for the visual data

		// --------------------------------------------------------------------
		// STEP 3: create actions to process the visual data

		AxisLayout x_axis = new AxisLayout("data", "NBZ", Constants.X_AXIS, VisiblePredicate.TRUE);

		AxisLayout y_axis = new AxisLayout("data", "BMI", Constants.Y_AXIS, VisiblePredicate.TRUE);

		ColorAction color = new ColorAction("data", VisualItem.STROKECOLOR, ColorLib.rgb(100, 100, 255));

		ActionList draw = new ActionList();
		draw.add(x_axis);
		draw.add(y_axis);
		draw.add(color);
		vis.putAction("draw", draw);

		// --------------------------------------------------------------------
		// STEP 4: set up a display and controls

		// --------------------------------------------------------------------
		// STEP 5: launching the visualization. The visualization must run after
		// the Display is ready (Android View)
		// TODO for Dritan: using dispay.post seems to be not a good solution.
		// Fix this before releasing the final solution
		display.post(new Runnable()
		{
			@Override
			public void run()
			{
				vis.run("draw");
			}
		});

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

		table.addRows(3);

		cal.set(2007, 11, 23);
		table.set(0, 0, cal.getTime());
		table.set(0, 1, 21.0);
		table.set(0, 2, 236.0);
		table.set(0, 3, "F");

		cal.set(2008, 6, 22);
		table.set(1, 0, cal.getTime());
		table.set(1, 1, 35.8);
		table.set(1, 2, 400.0);
		table.set(1, 3, "F");

		cal.set(2009, 3, 8);
		table.set(2, 0, cal.getTime());
		table.set(2, 1, 28.8);
		table.set(2, 2, 309.0);
		table.set(2, 3, "T");

		return table;
	}

}
