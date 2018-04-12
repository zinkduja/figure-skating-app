package vandy.cs4279.followfigureskating.dbClasses;

/**
 * This class represents a single skater for the app.  It contains information for the
 * skater necessary to populate the skater biography page, such as name, birth date, and height.
 */

public class Skater {
    private String mName;
    private String mDob;
    private String mHometown;
    private String mHeight;
    private String mCoach;
    private String mChoreographer;
    private String mFormerCoaches;
    private String mNation;
    private String mShortProgram;
    private String mFreeProgram;
    private String mBestTop;
    private String mBestTopComp;
    private String mBestShort;
    private String mBestShortComp;

    /**
     * Default constructor
     */
    public Skater(){}

    /**
     * Alternate constructor
     */
    public Skater(String name, String dob, String height, String hometown, String coach,
            String choreographer, String formerCoaches, String nation, String shortP, String freeP,
                  String bestTop, String bestTopComp, String bestShort, String bestShortComp) {
        mName = name;
        mDob = dob;
        mHeight = height;
        mHometown = hometown;
        mCoach = coach;
        mChoreographer = choreographer;
        mFormerCoaches = formerCoaches;
        mNation = nation;
        mShortProgram = shortP;
        mFreeProgram = freeP;
        mBestShort = bestShort;
        mBestShortComp = bestShortComp;
        mBestTop = bestTop;
        mBestTopComp = bestTopComp;

    }

    // Getter Methods

    public String getmName() {
        return mName;
    }

    public String getmDob() {
        return mDob;
    }

    public String getmHometown() {
        return mHometown;
    }

    public String getmHeight() {
        return mHeight;
    }

    public String getmCoach() {
        return mCoach;
    }

    public String getmChoreographer() {
        return mChoreographer;
    }

    public String getmFormerCoaches() {
        return mFormerCoaches;
    }

    public String getmNation() { return mNation; }

    public String getmShortProgram() { return mShortProgram; }

    public String getmFreeProgram() { return mFreeProgram; }

    public String getmBestTop() { return mBestTop; }

    public String getmBestTopComp() { return mBestTopComp; }

    public String getmBestShort() { return mBestShort; }

    public String getmBestShortComp() { return mBestShortComp; }

}
