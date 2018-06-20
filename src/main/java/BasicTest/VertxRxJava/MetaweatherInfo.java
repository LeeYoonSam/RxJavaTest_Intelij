package BasicTest.VertxRxJava;

public class MetaweatherInfo {
    private String title;
    private String location_type;
    private int woeid;
    private long latt_long;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation_type() {
        return location_type;
    }

    public void setLocation_type(String location_type) {
        this.location_type = location_type;
    }

    public int getWoeid() {
        return woeid;
    }

    public void setWoeid(int woeid) {
        this.woeid = woeid;
    }

    public long getLatt_long() {
        return latt_long;
    }

    public void setLatt_long(long latt_long) {
        this.latt_long = latt_long;
    }
}
