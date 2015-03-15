package sumeetkumar.in.wearsense.data;

/**
 * Created by sumeet on 3/14/15.
 */
public class BLEData {

    private String macAddress= "";
    private String tagName = "";
    private int signalStrength;
    private String phoneNumber= "";

    public BLEData(String macAddress, String tagName, int signalStrength, String phoneNumber){
        this.macAddress = macAddress;
        this.tagName = tagName;
        this.signalStrength = signalStrength;
        this.phoneNumber = phoneNumber;
    }

    public String getMacAddress() {
        return macAddress;
    }
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
    public  String gettagName() {
        return tagName;
    }
    public  void settagName(String tagName) {
        this.tagName = tagName;
    }
    public int getSignalStrength() {
        return signalStrength;
    }
    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}