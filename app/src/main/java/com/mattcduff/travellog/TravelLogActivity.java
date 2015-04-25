package com.mattcduff.travellog;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class TravelLogActivity extends ActionBarActivity {

    ArrayAdapter<String> adpLocationNames;
    AutoCompleteTextView actvLocationNames;
    Boolean blnWorkHome, blnArrDep, blnFT;
    Button btnSaveLogEntry, btnSaveChanges, btnCancelChanges;
    CheckBox chkFT;
    EditText etArrTime, etDepTime, etComments;
    ListView lvDisplayInfo;
    Long editRowID;
    TextClock tcClock, tcDate;
    ToggleButton tbWorkHome, tbArrDep;
    Date datDate;
    String[] strLocationNames=new String[]{"Home", "Ruislip", "H-o-t-H",
            "Finchley Road", "Westminster", "Work"};
    String strDate, strLocation, strDirection, strComments;
    String strCurrentLocation,strCurrentArrTime,strCurrentDepTime ,strCurrentComments;
    SimpleDateFormat sdfMyDateFormat;
    DBAdapter myDb;
    long rowID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_log);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adpLocationNames = new ArrayAdapter<>(this, R.layout.spinner_item,strLocationNames);

        actvLocationNames = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewLocationNames);
        actvLocationNames.setThreshold(1);
        actvLocationNames.setAdapter(adpLocationNames);
        actvLocationNames.setText(adpLocationNames.getItem(0));
        actvLocationNames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                strLocation = parent.getItemAtPosition(position).toString();

                workHomeToggleButtonStatus(strLocation);

                /*Whether the Fast Train checkbox is available or not is dependent on the location so when the location
                changes I need to change the status of the checkbox*/
                fastTrainCheckBoxStatus(strLocation);

                etComments.setText("");
            }

            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        btnSaveLogEntry = (Button) findViewById(R.id.btnSaveLogEntry);
        btnSaveChanges = (Button) findViewById(R.id.btnSaveChanges);
        btnSaveChanges.setEnabled(false);
        btnSaveChanges.setVisibility(View.INVISIBLE);
        btnCancelChanges = (Button) findViewById(R.id.btnCancelChanges);
        btnCancelChanges.setEnabled(false);
        btnCancelChanges.setVisibility(View.INVISIBLE);

        chkFT = (CheckBox)findViewById(R.id.checkBoxFastTrain);
        chkFT.setEnabled(false);

        etArrTime = (EditText) findViewById(R.id.editTextArrivedTime);
        etDepTime = (EditText) findViewById(R.id.editTextDepartedTime);
        etComments = (EditText) findViewById(R.id.editTextComments);

        lvDisplayInfo = (ListView) findViewById(R.id.listViewDisplayInfo);

        strLocation="Home";

        tcDate = (TextClock) findViewById(R.id.textClockDate);
        tcDate.setText(strDate);

        tbWorkHome= (ToggleButton) findViewById(R.id.toggleButtonWorkHome);
        tbArrDep= (ToggleButton) findViewById(R.id.toggleButtonArrDep);

        tbWorkHome.setChecked(true);
        tbArrDep.setChecked(true);

        sdfMyDateFormat = new SimpleDateFormat("dd MMM yy");
        datDate = Calendar.getInstance().getTime();
        strDate=sdfMyDateFormat.format(datDate);

        openDB();
        populateListView();
        logEntryItemClick();
        logEntryItemLongClick();
        saveLogEntryClick();



        tbWorkHome.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                /*Whether the Fast Train checkbox is available or not is dependent on this toggle button so it
                changes I need to change the status of the checkbox*/
                fastTrainCheckBoxStatus(strLocation);
            }
        });

        tbArrDep.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //Change which time entry box is visible depending on the whether this button is checked or not
                manualTimeEntryStatus(isChecked);

                /*Whether the Fast Train checkbox is available or not is dependent on this toggle button so it
                changes I need to change the status of the checkbox*/
                fastTrainCheckBoxStatus(strLocation);
            }


        });

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDB();
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickSaveChanges(View v) {

        String strEditLocation,strEditArrTime,strEditDepTime ,strEditFastTrain, strEditComments;

        strEditLocation=actvLocationNames.getText().toString();

        strEditArrTime=etArrTime.getText().toString();
        strEditDepTime=etDepTime.getText().toString();
        if (chkFT.isChecked()) {
            strEditFastTrain="Yes";
        } else {
            strEditFastTrain="No";
        }
        strEditComments=etComments.getText().toString();

        myDb.updateEditedRow(editRowID,strEditLocation,strEditArrTime, strEditDepTime, strEditFastTrain, strEditComments);

        resetViewStatus();
        populateListView();
    }

    public void onClickCancelChanges(View v) {

        resetViewStatus();
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
        lvDisplayInfo.setAdapter(adpDisplayInfo);
    }

    private void fastTrainCheckBoxStatus(String strLoc) {

        switch (strLoc) {
            case "Ruislip":
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

    private void workHomeToggleButtonStatus (String strLoc) {
        //Only have the To Work/To Home toggle button available when at Work or Home
        switch (strLoc) {
            case "Home":
            case "Work":
                //tbWorkHome.setVisibility(View.VISIBLE);
                tbWorkHome.setEnabled(true);
                break;
            default:
                tbWorkHome.setEnabled(false);
                //tbWorkHome.setVisibility(View.INVISIBLE);
        }
    }

    private void resetViewStatus () {
        //Enable or Disable the various buttons based upon the saved current location
        workHomeToggleButtonStatus(strCurrentLocation);
        fastTrainCheckBoxStatus(strCurrentLocation);

        //Restore the current values into the various views
        tbWorkHome.setChecked(blnWorkHome);
        tbArrDep.setChecked(blnArrDep);
        manualTimeEntryStatus(blnArrDep);
        chkFT.setChecked(blnFT);

        if (blnArrDep) {
            etDepTime.setText(strCurrentDepTime);
        } else {
            etArrTime.setText(strCurrentArrTime);
        }

        actvLocationNames.setText(strCurrentLocation);

        etComments.setText(strCurrentComments);

        tbArrDep.setVisibility(View.VISIBLE);
        tbArrDep.setEnabled(true);

        blnArrDep=null;
        blnFT=null;
        blnWorkHome=null;
        strCurrentArrTime=null;
        strCurrentLocation=null;
        strCurrentDepTime=null;
        strCurrentComments=null;

        btnSaveLogEntry.setVisibility(View.VISIBLE);
        btnSaveLogEntry.setEnabled(true);
        btnSaveChanges.setEnabled(false);
        btnSaveChanges.setVisibility(View.INVISIBLE);
        btnCancelChanges.setEnabled(false);
        btnCancelChanges.setVisibility(View.INVISIBLE);

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

    private void saveLogEntryClick() {
        btnSaveLogEntry.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        String strFT, strTime, strManualTime;
                        EditText etArrTime = (EditText) findViewById(R.id.editTextArrivedTime);
                        EditText etDepTime = (EditText) findViewById(R.id.editTextDepartedTime);
                        EditText etComments = (EditText) findViewById(R.id.editTextComments);
                        CheckBox chkFT = (CheckBox) findViewById(R.id.checkBoxFastTrain);

                        strComments = etComments.getText().toString();


                        /*Check if the appropriate editText has a time in it; if not take it from the textClockTime*/
                        if (tbArrDep.isChecked()) {
                            if (etDepTime.getText().length() == 0) {
                                strTime = DateFormat.getTimeInstance().format(Calendar.getInstance().getTime());
                            } else {
                                strTime = etDepTime.getText().toString();
                            }
                        } else {
                            if (etArrTime.getText().length() == 0) {
                                strTime = DateFormat.getTimeInstance().format(Calendar.getInstance().getTime());
                            } else {
                                strTime = etArrTime.getText().toString();
                            }
                        }


                        if (tbWorkHome.isChecked()) {
                            strDirection = "To Work";
                        } else {
                            strDirection = "To Home";
                        }

                        switch (strLocation) {
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
                                if (rowID < 0 && (!tbArrDep.isChecked())) {
                                    myDb.addRowArrived(strDate, strLocation, strDirection, strTime, strComments);
                                }
                                if (rowID < 0 && tbArrDep.isChecked()) {
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

                        if (tbWorkHome.isChecked() && tbArrDep.isChecked()) {
                            switch (strLocation) {
                                case "Home":
                                    strLocation="Rusilip";
                                    break;
                                case "Ruislip":
                                    strLocation="H-o-t-H";
                                    break;
                                case "H-o-t-H":
                                    strLocation="Finchley Road";
                                    break;
                                case "Finchley Road":
                                    strLocation="Westminster";
                                    break;
                                case "Westminster":
                                    strLocation="Work";
                                    break;
                            }

                        }
                        if (!tbWorkHome.isChecked() && tbArrDep.isChecked()) {
                            switch (strLocation) {
                                case "Ruislip":
                                    strLocation="Home";
                                    break;
                                case "Finchley Road":
                                    strLocation="Ruislip";
                                    break;
                                case "H-o-t-H":
                                    strLocation="Ruislip";
                                    break;
                                case "Work":
                                    strLocation="Westminster";
                                    break;
                                case "Westminster":
                                    strLocation="Finchley Road";
                                    break;
                            }
                        }
                        actvLocationNames.setText(strLocation);

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
    }

    private void logEntryItemClick() {

        lvDisplayInfo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                Cursor csrEditLogEntry;
                int edcol,elcol, eatcol, edtcol, eftcol, eccol;

                String strEditDirection, strEditLocation,strEditArrTime,strEditDepTime ,strEditFastTrain, strEditComments;

                editRowID=id;

                tbArrDep.setEnabled(false);
                tbArrDep.setVisibility(View.INVISIBLE);

                //Save the current values in the various views
                blnWorkHome=tbWorkHome.isChecked();
                blnArrDep=tbArrDep.isChecked();
                blnFT=chkFT.isChecked();


                strCurrentLocation=strLocation;
                strCurrentArrTime=etArrTime.getText().toString();
                strCurrentDepTime=etDepTime.getText().toString();
                strCurrentComments=etComments.getText().toString();

                tbArrDep.setEnabled(false);
                btnSaveLogEntry.setEnabled(false);
                btnSaveLogEntry.setVisibility(View.INVISIBLE);
                btnSaveChanges.setVisibility(View.VISIBLE);
                btnSaveChanges.setEnabled(true);
                btnCancelChanges.setVisibility(View.VISIBLE);
                btnCancelChanges.setEnabled(true);

                csrEditLogEntry=myDb.getEditRow(id);

                elcol=csrEditLogEntry.getColumnIndexOrThrow(DBAdapter.COLUMN_LOCATION);
                strEditLocation=csrEditLogEntry.getString(elcol);
                actvLocationNames.setText(strEditLocation);

                edcol=csrEditLogEntry.getColumnIndexOrThrow(DBAdapter.COLUMN_DIRECTION);
                strEditDirection=csrEditLogEntry.getString(edcol);
                //only enable the Work/Home toggle button if it normally available for the location being edited
                workHomeToggleButtonStatus(strEditLocation);
                if (strEditDirection=="To Work") {
                    tbWorkHome.setChecked(true);
                } else {
                    tbWorkHome.setChecked(false);
                }
                eatcol=csrEditLogEntry.getColumnIndexOrThrow(DBAdapter.COLUMN_ARRIVED);
                strEditArrTime=csrEditLogEntry.getString(eatcol);
                //only show the arrival time box if the entry to be edited contains an entry time
                if (strEditArrTime != null) {
                    etArrTime.setVisibility(View.VISIBLE);
                    etArrTime.setEnabled(true);
                    etArrTime.setText(strEditArrTime);
                } else {
                    etArrTime.setEnabled(false);
                    etArrTime.setVisibility(View.INVISIBLE);
                }

                edtcol=csrEditLogEntry.getColumnIndexOrThrow(DBAdapter.COLUMN_DEPARTED);
                strEditDepTime=csrEditLogEntry.getString(edtcol);
                //only show the departure time box if the entry to be edited contains an entry time
                if (strEditDepTime != null) {
                    etDepTime.setVisibility(View.VISIBLE);
                    etDepTime.setEnabled(true);
                    etDepTime.setText(strEditDepTime);
                } else {
                    etDepTime.setEnabled(false);
                    etDepTime.setVisibility(View.INVISIBLE);
                }

                eftcol=csrEditLogEntry.getColumnIndexOrThrow(DBAdapter.COLUMN_FASTTRAIN);
                strEditFastTrain=csrEditLogEntry.getString(eftcol);

                //Only show the fast train checkbox if it is valid for the location being edited
                if (strEditFastTrain != null) {
                    if (strEditFastTrain.length()!= 0) {
                        chkFT.setVisibility(View.VISIBLE);
                        chkFT.setEnabled(true);
                        if (strEditFastTrain=="Yes") {
                            chkFT.setChecked(true);
                        } else {
                            chkFT.setChecked(false);
                        }
                    } else {
                        chkFT.setEnabled(false);
                        chkFT.setVisibility(View.INVISIBLE);
                    }
                } else {
                    chkFT.setEnabled(false);
                    chkFT.setVisibility(View.INVISIBLE);
                    }

                eccol=csrEditLogEntry.getColumnIndexOrThrow(DBAdapter.COLUMN_COMMENTS);
                strEditComments=csrEditLogEntry.getString(eccol);
                etComments.setText(strEditComments);



            }
        });
    }

    private void logEntryItemLongClick() {

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
