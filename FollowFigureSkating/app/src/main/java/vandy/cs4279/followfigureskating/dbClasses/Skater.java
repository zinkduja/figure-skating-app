package vandy.cs4279.followfigureskating.dbClasses;

public class Skater {
    private String mName;
    private String mDob;
    private String mHometown;
    private String mHeight;
    private String mCoach;
    private String mChoreographer;
    private String mFormerCoaches;

    public Skater(){}

    public Skater(String name, String dob, String hometown, String height, String coach,
            String choreographer, String formerCoaches) {
        mName = name;
        mDob = dob;
        mHometown = hometown;
        mHeight = height;
        mCoach = coach;
        mChoreographer = choreographer;
        mFormerCoaches = formerCoaches;
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

}
