package vandy.cs4279.followfigureskating.dbClasses;

public class Skater {
    private String mName;
    private String mDob;
    private String mHometown;
    private String mHeight;
    private String mCoach;
    private String mChoreographer;
    private String mFormerCoaches;
    private String mNation;

    public Skater(){}

    public Skater(String name, String dob, String height, String hometown, String coach,
            String choreographer, String formerCoaches, String nation) {
        mName = name;
        mDob = dob;
        mHeight = height;
        mHometown = hometown;
        mCoach = coach;
        mChoreographer = choreographer;
        mFormerCoaches = formerCoaches;
        mNation = nation;
    }

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

}
