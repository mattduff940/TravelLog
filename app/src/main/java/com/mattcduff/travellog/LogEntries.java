package com.mattcduff.travellog;

import android.text.format.Time;

import java.util.Date;

/**
 * Created by Matt on 09/03/2015.
 */
public class LogEntries {
    private int _id;
    private String _date;
    private String _location;
    private String _arrived;
    private String _departed;
    private String _fastTrain;
    private String _comments;

    public LogEntries() {

    }

    public LogEntries (String date, String location, String arrived, String departed, String fastTrain, String comments ) {
        this._date = date;
        this._location = location;
        this._arrived = arrived;
        this._departed = departed;
        this._fastTrain = fastTrain;
        this._comments = comments;
    }

    //public void setID(int id) {
    //    this._id = id;
    //}

    public int getID() {
        return this._id;
    }

    public void setDate(String date) {
        this._date = date;
    }

    public String getDate() {
        return this._date;
    }

    public void setLocation(String location) {
        this._location = location;
    }

    public String getLocation () {
        return this._location;
    }

    public void setArrived(String arrived) {
        this._arrived = arrived;
    }

    public String getArrived() {
        return this._arrived;
    }

    public void setDeparted(String departed) {
        this._departed = departed;
    }

    public String getDeparted() {
        return this._departed;
    }

    public void setFastTrain(String fastTrain) {
        this._fastTrain = fastTrain;
    }

    public String getFastTrain() {
        return this._fastTrain;
    }

    public void setComments(String comments) {
        this._comments = comments;
    }

    public String getComments() {
        return this._comments;
    }
}
