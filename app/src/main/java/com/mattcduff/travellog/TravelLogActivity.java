package com.mattcduff.travellog;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class TravelLogActivity extends ActionBarActivity implements OnItemSelectedListener {

    CheckBox chkFT;
    Date datDate;
    String strDate, strLocation, strDirection, strComments;
    SimpleDateFormat sdfMyDateFormat;
    DBAdapter myDb;
    long rowID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_log);

        final ToggleButton tbWorkHome= (ToggleButton) findViewById(R.id.toggleButtonWorkHome);
        final ToggleButton tbArrDep= (ToggleButton) findViewById(R.id.toggleButtonArrDep);

        tbWorkHome.setChecked(true);
        tbArrDep.setChecked(true);

        sdfMyDateFormat=new SimpleDateFormat("dd MMM yy");
        datDate = Calendar.getInstance().getTime();
        strDate=sdfMyDateFormat.format(datDate);

        TextClock tcDate = (TextClock) findViewById(R.id.textClockDate);
        tcDate.setText(strDate);

        chkFT = (CheckBox)findViewById(R.id.checkBoxFastTrain);
        chkFT.setEnabled(false);

        String[] strLocationNames=new String[]{"Home", "Ruislip", "Ruislip Manor", "H-o-t-H",
                "Finchley Road", "Westminster", "Work"};

        final Spinner spnLocationNames= (Spinner) findViewById(R.id.spnLocationNames);

        ArrayAdapter<String> adpLocationsNames=new ArrayAdapter<>(this, R.layout.spinner_item,strLocationNames);
        spnLocationNames.setAdapter(adpLocationsNames);
        spnLocationNames.setOnItemSelectedListener(this);

        strLocation="Home";

        openDB();
        populateListView();
        logEntryItemLongClick();

        Button btnRecord = (Button)findViewById(R.id.btnRecordTime);
        btnRecord.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        String strFT, strTime, strManualTime;
                        EditText etArrTime = (EditText) findViewById(R.id.editTextArrivedTime);
                        EditText etDepTime = (EditText) findViewById(R.id.editTextDepartedTime);
                        EditText etComments = (EditText) findViewById(R.id.editTextComments);
                        CheckBox chkFT = (CheckBox) findViewById(R.id.checkBoxFastTrain);
                        strComments=etComments.getText().toString();


                        /*Check if the appropriate editText has a time in it; if not take it from the textClockTime*/
                        if (tbArrDep.isChecked() ) {
                            if (etDepTime.getText().length()==0) {
                                strTime = DateFormat.getTimeInstance().format(Calendar.getInstance().getTime());
                            } else {
                                strTime=etDepTime.getText().toString();
                            }
                        } else {
                            if (etArrTime.getText().length()==0) {
                                strTime = DateFormat.getTimeInstance().format(Calendar.getInstance().getTime());
                            } else {
                                strTime=etArrTime.getText().toString();
                            }
                        }


                        if (tbWorkHome.isChecked()) {
                            strDirection="To Work";
                        } else {
                            strDirection="To Home";
                        }

                        switch (strLocation){
                            case "Home":
                                if (tbWorkHome.isChecked()) {
                                    myDb.addRowDeparted(strDate, strLocation, strDirection, strTime, "", strComments);
                                    break;
                                } else {
                                    myDb.addRowArrived(strDate, strLocation, "To Home", strTime, strComments);
                                    //Toggle the button to 'To Work' so it ready for when I travel the next day
                                    tbWorkHome.setChecked(true);
                                    break;
                                }
                            case "Work":
                                if (tbWorkHome.isChecked()) {
                                    myDb.addRowArrived(strDate, strLocation, "To Work", strTime, strComments);
                                    //Toggle the button to 'To Home' so it ready for when I travel home
                                    tbWorkHome.setChecked(false);
                                    break;
                                } else {
                                    rowID = myDb.rowExists(strDate, strLocation, "To Work");
                                    myDb.updateRowDeparted(rowID, strTime, "", strComments);
                                    break;
                                }
                            default:
                                rowID = myDb.rowExists(strDate, strLocation, strDirection);
                                if (rowID<0 && (!tbArrDep.isChecked())) {
                                    myDb.addRowArrived(strDate,strLocation,strDirection,strTime,strComments);
                                }
                                if (rowID<0 && tbArrDep.isChecked()) {
                                    if (chkFT.isChecked()) {
                                        strFT = "Yes";
                                    } else {
                                        if (chkFT.isEnabled()) {
                                            strFT = "No";
                                        } else {
                                            strFT = "";
                                        }
                                        myDb.addRowDeparted(strDate, strLocation, strDirection, strTime, strFT, strComments);
                                    }
                                }
                                if (rowID >= 0 && (!tbArrDep.isChecked())) {
                                    myDb.updateRowArrived(rowID, strTime, strComments);
                                }
                                if (rowID >= 0 && tbArrDep.isChecked()) {
                                    if (chkFT.isChecked()) {
                                        strFT = "Yes";
                                    } else {
                                        if (chkFT.isEnabled()) {
                                            strFT = "No";
                                        } else {
                                            strFT = "";
                                        }
                                    }
                                    myDb.updateRowDeparted(rowID, strTime, strFT, strComments);
                                }
                            }
                        /*Automatically change the status of the Arrived/Departed toggle button once the log
                        entry is made so it ready for the next entry*/
                        if (tbArrDep.isChecked()) {
                            tbArrDep.setChecked(false);
                        } else {
                            tbArrDep.setChecked(true);
                        }
                        
                        //Re-populate the List View so it displays the new information
                        populateListView();
                    }
                }
        );

        tbWorkHome.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                /*Whether the Fast Train checkbox is available or not is dependent on this toggle button so it
                changes I need to change the status of the checkbox*/
                fastTrainCheckBoxStatus();
            }
        });

        tbArrDep.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //Change which time entry box is visible depending on the whether this button is checked or not
                manualTimeEntryStatus(isChecked);

                /*Whether the Fast Train checkbox is available or not is dependent on this toggle button so it
                changes I need to change the status of the checkbox*/
                fastTrainCheckBoxStatus();
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        ToggleButton tbWorkHome= (ToggleButton) findViewById(R.id.toggleButtonWorkHome);
        EditText etComments = (EditText) findViewById(R.id.editTextComments);

        strLocation = parent.getItemAtPosition(position).toString();
        
        //Only have the To Work/To Home toggle button available when at Work or Home
        switch (strLocation) {
            case "Home":
            case "Work":
                tbWorkHome.setEnabled(true);
                break;
            default:
                tbWorkHome.setEnabled(false);
        }
        
        /*Whether the Fast Train checkbox is available or not is dependent on the location so when the location
        changes I need to change the status of the checkbox*/
        fastTrainCheckBoxStatus();

        etComments.setText("");
    }

    public void onNothingSelected(AdapterView<?> arg0){

    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //final TextView txtCapture=(TextView) findViewById(R.id.txtCapturedTime);
        //CharSequence capturedTime=txtCapture.getText();
        //outState.putCharSequence("savedTime", capturedTime);
    }

    @Override
    protected void onRestoreInstanceState (@NonNull Bundle inState){
        super.onRestoreInstanceState(inState);
        //final TextView txtCapture=(TextView) findViewById(R.id.txtCapturedTime);
        //CharSequence capturedTime=inState.getCharSequence("savedTime");
        //txtCapture.setText(capturedTime);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_travel_log, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        // }

        switch (item.getItemId())
        {
            case R.id.exit_app:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openDB(){
        myDb = new DBAdapter(this);
        myDb.open();
    }

    private void closeDB(){
        myDb.close();
    }
    
    private void populateListView(){

        Cursor csrTodaysEntries=myDb.getAllTodaysRows(strDate);
        LogEntriesCursorAdapter adpDisplayInfo=new LogEntriesCursorAdapter(this,csrTodaysEntries );
        ListView lvDisplayInfo=(ListView)findViewById(R.id.listViewDisplayInfo);
        lvDisplayInfo.setAdapter(adpDisplayInfo);
    }

    private void fastTrainCheckBoxStatus() {

        ToggleButton tbWorkHome= (ToggleButton) findViewById(R.id.toggleButtonWorkHome);
        ToggleButton tbArrDep= (ToggleButton) findViewById(R.id.toggleButtonArrDep);

        switch (strLocation) {
            case "Ruislip":
            case "Ruislip Manor":
            case "H-o-t-H":
                if (tbWorkHome.isChecked() && tbArrDep.isChecked()) {
                    chkFT.setVisibility(View.VISIBLE);
                    chkFT.setEnabled(true);
                } else {
                    chkFT.setChecked(false);
                    chkFT.setEnabled(false);
                    chkFT.setVisibility(View.INVISIBLE);
                }
                break;
            case "Finchley Road":
                if ((!tbWorkHome.isChecked()) && tbArrDep.isChecked()) {
                    chkFT.setVisibility(View.VISIBLE);
                    chkFT.setEnabled(true);
                } else {
                    chkFT.setChecked(false);
                    chkFT.setEnabled(false);
                    chkFT.setVisibility(View.INVISIBLE);
                }
                break;
            default:
                chkFT.setChecked(false);
                chkFT.setEnabled(false);
                chkFT.setVisibility(View.INVISIBLE);
        }
    }

    private void manualTimeEntryStatus(boolean isChecked) {

        EditText etArrTime = (EditText) findViewById(R.id.editTextArrivedTime);
        EditText etDepTime = (EditText) findViewById(R.id.editTextDepartedTime);

        if (isChecked) {
            etArrTime.setEnabled(false);
            etArrTime.setVisibility(View.INVISIBLE);
            etDepTime.setVisibility(View.VISIBLE);
            etDepTime.setEnabled(true);
            etDepTime.setText(null);
        } else {
            etArrTime.setVisibility(View.VISIBLE);
            etArrTime.setEnabled(true);
            etArrTime.setText(null);
            etDepTime.setEnabled(false);
            etDepTime.setVisibility(View.INVISIBLE);
        }
    }

    public class LogEntriesCursorAdapter extends CursorAdapter {

        public LogEntriesCursorAdapter (Context context, Cursor c) {
            
            super(context, c, 0);
        }

        @Override
        public View newView (Context context, Cursor cursor, ViewGroup parent) {

            return LayoutInflater.from(context).inflate(R.layout.listview_displayinfo, parent,false);
        }

        public void bindView (View lvDisplayInfo, Context context, Cursor csrTodaysEntries) {

            int locationcol = csrTodaysEntries.getColumnIndex(DBAdapter.COLUMN_LOCATION);
            String location = csrTodaysEntries.getString(locationcol);
            int arrivedncol = csrTodaysEntries.getColumnIndex(DBAdapter.COLUMN_ARRIVED);
            String arrived = csrTodaysEntries.getString(arrivedncol);
            int departedcol = csrTodaysEntries.getColumnIndex(DBAdapter.COLUMN_DEPARTED);
            String departed = csrTodaysEntries.getString(departedcol);
            int commentscol = csrTodaysEntries.getColumnIndex(DBAdapter.COLUMN_COMMENTS);
            String comments = csrTodaysEntries.getString(commentscol);
            int fasttraincol = csrTodaysEntries.getColumnIndex(DBAdapter.COLUMN_FASTTRAIN);
            String fasttrain = csrTodaysEntries.getString(fasttraincol);

            ImageView ivIcon = (ImageView) lvDisplayInfo.findViewById(R.id.imageViewIcon);
            TextView tvLocation = (TextView) lvDisplayInfo.findViewById(R.id.textViewDisplayLocation);
            TextView tvArrived = (TextView) lvDisplayInfo.findViewById(R.id.textViewDisplayArrived);
            TextView tvDeparted = (TextView) lvDisplayInfo.findViewById(R.id.textViewDisplayDeparted);
            TextView tvFastTrain = (TextView) lvDisplayInfo.findViewById(R.id.textViewDisplayFastTrain);
            TextView tvComments = (TextView) lvDisplayInfo.findViewById(R.id.textViewDisplayComments);

            switch (location) {
                case "Home":
                    ivIcon.setImageResource(R.drawable.ic_home);
                    break;
                case "Work":
                    ivIcon.setImageResource(R.drawable.ic_work);
                    break;
                default:
                    ivIcon.setImageResource(R.drawable.ic_underground);
            }

            tvLocation.setText(location);
            tvArrived.setText(arrived);
            tvDeparted.setText(departed);
            tvFastTrain.setText(fasttrain);
            tvComments.setText(comments);
        }
    }

    private void logEntryItemLongClick() {
        ListView lvDisplayInfo = (ListView) findViewById(R.id.listViewDisplayInfo);
        lvDisplayInfo.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                myDb.deleteRow(id);
                populateListView();
                return false;
            }
        });
    }

}
