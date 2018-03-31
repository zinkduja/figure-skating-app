package vandy.cs4279.followfigureskating.dbClasses;

public class SkatingEvent {
    private String mTitle;
    private String mStartDate;
    private String mEndDate;
    private String mYear;

    public SkatingEvent(String title, String start, String end, String year) {
        mTitle = title;
        mStartDate = start;
        mEndDate = end;
        mYear = year;
    }

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
}
