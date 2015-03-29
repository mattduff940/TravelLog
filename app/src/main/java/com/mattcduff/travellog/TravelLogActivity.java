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
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class TravelLogActivity extends ActionBarActivity implements OnItemSelectedListener {

    CheckBox chkFT;
    Date datDate;
    EditText etDate;
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

        etDate = (EditText) findViewById(R.id.editTextDate);
        etDate.setText(strDate);

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

        Button btnCapture = (Button)findViewById(R.id.btnCaptureTime);
        btnCapture.hasFocus();
        btnCapture.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        EditText edtCapture=
                                (EditText) findViewById(R.id.editTextTime);
                        String timeNow=DateFormat.getTimeInstance().format(Calendar.getInstance().getTime());
                        edtCapture.setText(timeNow);
                    }
                }
        );

        Button btnRecord = (Button)findViewById(R.id.btnRecordTime);
        btnRecord.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        EditText edtTime = (EditText) findViewById(R.id.editTextTime);
                        EditText etComments = (EditText) findViewById(R.id.editTextComments);
                        CheckBox chkFT = (CheckBox) findViewById(R.id.checkBoxFastTrain);
                        strComments=etComments.getText().toString();

                        String time=edtTime.getText().toString();
                        String strFT;

                        if (tbWorkHome.isChecked()) {
                            strDirection="To Work";
                        } else {
                            strDirection="To Home";
                        }

                        switch (strLocation){
                            case "Home":
                                if (tbWorkHome.isChecked()) {
                                    myDb.addRowDeparted(strDate,strLocation,strDirection,time,"",strComments);
                                    break;
                                } else {
                                    //rowID = myDb.rowExists(strDate, strLocation, "To Work");
                                    //myDb.updateRowArrived(rowID,time,strComments);
                                    myDb.addRowArrived(strDate, strLocation, "To Home", time, strComments);
                                    break;
                                }
                            case "Work":
                                if (tbWorkHome.isChecked()) {
                                    myDb.addRowArrived(strDate,strLocation,"To Work",time,strComments);
                                    break;
                                } else {
                                    rowID = myDb.rowExists(strDate, strLocation, "To Work");
                                    myDb.updateRowDeparted(rowID, time, "", strComments);
                                    break;
                                }
                            default:
                                rowID = myDb.rowExists(strDate, strLocation, strDirection);
                                if (rowID<0 && (!tbArrDep.isChecked())) {
                                    myDb.addRowArrived(strDate,strLocation,strDirection,time,strComments);
                                }
                                if (rowID<0 && tbArrDep.isChecked()){
                                    if (chkFT.isChecked()) {
                                        strFT="Yes";
                                    } else {
                                        strFT="No";
                                    }
                                    myDb.addRowDeparted(strDate,strLocation,strDirection,time,strFT,strComments);
                                }
                                if (rowID>=0 && (!tbArrDep.isChecked())){
                                    myDb.updateRowArrived(rowID,time,strComments);
                                }
                                if (rowID>=0 && tbArrDep.isChecked()){
                                    if (chkFT.isChecked()) {
                                        strFT="Yes";
                                    } else {
                                        strFT="No";
                                    }
                                    myDb.updateRowDeparted(rowID, time, strFT, strComments);
                                }
                        }
                        populateListView();
                    }
                }
        );

        tbWorkHome.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                fastTrainCheckBoxStatus();
            }
        });

        tbArrDep.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                fastTrainCheckBoxStatus();
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        strLocation = parent.getItemAtPosition(position).toString();

        fastTrainCheckBoxStatus();
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
            case "Home":
            case "Westminster":
            case "Work":
                chkFT.setChecked(false);
                chkFT.setEnabled(false);
                break;
            case "Ruislip":
            case "Ruislip Manor":
            case "H-o-t-H":
                if (tbWorkHome.isChecked() && tbArrDep.isChecked()) {
                    chkFT.setEnabled(true);
                } else {
                    chkFT.setChecked(false);
                    chkFT.setEnabled(false);
                }
                break;
            case "Finchley Road":
                if ((!tbWorkHome.isChecked()) && tbArrDep.isChecked()) {
                    chkFT.setEnabled(true);
                } else {
                    chkFT.setChecked(false);
                    chkFT.setEnabled(false);
                }
                break;

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

            int dbidcol = csrTodaysEntries.getColumnIndex(DBAdapter.COLUMN_ROWID);
            long dbid = csrTodaysEntries.getLong(dbidcol);
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
            TextView tvDbID = (TextView) lvDisplayInfo.findViewById(R.id.textViewDbID);
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


            tvDbID.setText(String.valueOf(dbid));
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