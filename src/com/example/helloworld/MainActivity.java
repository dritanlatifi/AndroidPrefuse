package com.example.helloworld;

import android.R.bool;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.Date;
import java.util.GregorianCalendar;

import prefuse.Constants;
import prefuse.PDisplay;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.AxisLayout;
import prefuse.data.Table;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.VisiblePredicate;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(createVisualization(generateTable()));
	}

	private Table generateTable() {
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
		table.set(1, 1,35.8);
		table.set(1, 2, 400.0);
		table.set(1, 3, "F");

		cal.set(2009, 3, 8);
		table.set(2, 0, cal.getTime());
		table.set(2, 1, 28.8);
		table.set(2, 2, 309.0);
		table.set(2, 3, "T");

		return table;
	}

	private View createVisualization(Table data) {
		Visualization vis = new Visualization();
		PDisplay display = new PDisplay(this, vis);

		// --------------------------------------------------------------------
		// STEP 1: setup the visualized data

		vis.add("data", data);

		// --------------------------------------------------------------------
		// STEP 2: set up renderers for the visual data

		// --------------------------------------------------------------------
		// STEP 3: create actions to process the visual data

		AxisLayout x_axis = new AxisLayout("data", "NBZ", Constants.X_AXIS,
				VisiblePredicate.TRUE);

		AxisLayout y_axis = new AxisLayout("data", "BMI", Constants.Y_AXIS,
				VisiblePredicate.TRUE);

		ColorAction color = new ColorAction("data", VisualItem.STROKECOLOR,
				ColorLib.rgb(100, 100, 255));

		ActionList draw = new ActionList();
		draw.add(x_axis);
		draw.add(y_axis);
		draw.add(color);
		vis.putAction("draw", draw);

		// --------------------------------------------------------------------
		// STEP 4: set up a display and controls

		// --------------------------------------------------------------------
		// STEP 5: launching the visualization
		vis.run("draw");

		return display;
	}
}
