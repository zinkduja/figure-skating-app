package vandy.cs4279.followfigureskating.dbClasses;

/**
 * This class represents a skating event for the app.  It contains basic information for the event,
 * such as the title, when it starts and ends, and the location.
 */

public class SkatingEvent {
    private String mTitle;
    private String mStartDate;
    private String mEndDate;
    private String mYear;
    private String mLocation;

    /**
     * Default constructor
     */
    public SkatingEvent(){}

    /**
     * Alternate constructor
     */
    public SkatingEvent(String title, String start, String end, String year, String location) {
        mTitle = title;
        mStartDate = start;
        mEndDate = end;
        mYear = year;
        mLocation = location;
    }

    // Getter Methods

    public String getTitle() {
        return mTitle;
    }

    public String getStart() {
        return mStartDate;
    }

    public String getEnd() {
        return mEndDate;
    }

    public String getYear() {
        return mYear;
    }

    public String getLocation() {
        return mLocation;
    }
}
