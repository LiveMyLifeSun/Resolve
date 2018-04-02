import java.util.Comparator;

class ApnModel {
    private String mMcc;
    private String mMnc;
    private String mCarrier;
    private String mApn;
    private String mUser;
    private String mPassword;
    private String mProxy;
    private String mProtocol;
    private String mPort;
    private String mMmsc;
    private String mMmsproxy;
    private String mMmsport;
    private String mAuthtype;
    private String mType;
    private String mMvnoType;
    private String mMvnoMatchData;

    public String getMcc() {
        return mMcc;
    }

    public void setMcc(String mcc) {
        mMcc = mcc;
    }

    public String getMnc() {
        return mMnc;
    }

    public void setMnc(String mMnc) {
        this.mMnc = mMnc;
    }

    public String getCarrier() {
        return mCarrier;
    }

    public void setCarrier(String mCarrier) {
        this.mCarrier = mCarrier;
    }

    public String getApn() {
        return mApn;
    }

    public void setApn(String mApn) {
        this.mApn = mApn;
    }

    public String getUser() {
        return mUser;
    }

    public void setUser(String mUser) {
        this.mUser = mUser;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String mPassword) {
        this.mPassword = mPassword;
    }

    public String getProxy() {
        return mProxy;
    }

    public void setProxy(String proxy) {
        mProxy = proxy;
    }

    public String getProtocol() {
        return mProtocol;
    }

    public void setProtocol(String protocol) {
        mProtocol = protocol;
    }

    public String getPort() {
        return mPort;
    }

    public void setPort(String port) {
        mPort = port;
    }

    public String getMmsc() {
        return mMmsc;
    }

    public void setMmsc(String mmsc) {
        mMmsc = mmsc;
    }

    public String getMmsproxy() {
        return mMmsproxy;
    }

    public void setMmsproxy(String mmsproxy) {
        mMmsproxy = mmsproxy;
    }

    public String getMmsport() {
        return mMmsport;
    }

    public void setMmsport(String mmsport) {
        mMmsport = mmsport;
    }

    public String getAuthtype() {
        return mAuthtype;
    }

    public void setAuthtype(String authtype) {
        mAuthtype = authtype;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getMvnoType() {
        return mMvnoType;
    }

    public void setMvnoType(String mMvnoType) {
        this.mMvnoType = mMvnoType;
    }

    public String getMvnoMatchData() {
        return mMvnoMatchData;
    }

    public void setMvnoMatchData(String mMvnoMatchData) {
        this.mMvnoMatchData = mMvnoMatchData;
    }

    @Override
    public boolean equals(Object object) {
        return (this == object) ||
                ((object instanceof ApnModel)
                        && equals(mMcc, ((ApnModel) object).mMcc)
                        && equals(mMnc, ((ApnModel) object).mMnc)
                        && equals(mCarrier, ((ApnModel) object).mCarrier)
                        && equals(mApn, ((ApnModel) object).mApn)
                        && equals(mUser, ((ApnModel) object).mUser)
                        && equals(mPassword, ((ApnModel) object).mPassword)
                        && equals(mProxy, ((ApnModel) object).mProxy)
                        && equals(mProtocol, ((ApnModel) object).mProtocol)
                        && equals(mPort, ((ApnModel) object).mPort)
                        && equals(mMmsc, ((ApnModel) object).mMmsc)
                        && equals(mMmsproxy, ((ApnModel) object).mMmsproxy)
                        && equals(mMmsport, ((ApnModel) object).mMmsport)
                        && equals(mAuthtype, ((ApnModel) object).mAuthtype)
                        && equals(mType, ((ApnModel) object).mType))
                        && equals(mMvnoType, ((ApnModel) object).getMvnoType())
                        && equals(mMvnoMatchData, ((ApnModel) object).getMvnoMatchData());
    }

    private boolean equals(String s1, String s2) {
        return (s1 == null && s2 == null)
                || ((s1 != null) && s1.equals(s2));
    }

    @Override
    public String toString() {
        return mCarrier + "\t" + mMcc + "\t" + mMnc;
    }
}
