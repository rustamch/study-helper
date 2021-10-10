package events.StudyTimeEvent;

public class StudyTimeLeaderboardRow {
    private long time;
    private int position;

    public StudyTimeLeaderboardRow(long time, int position){
        this.time = time;
        this.position = position;
    }

    public long getTime(){
        return time;
    }

    public int getPosition(){
        return position;
    }
}
