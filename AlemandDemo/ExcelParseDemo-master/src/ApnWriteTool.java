import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ApnWriteTool {
    private static final String SEP_CHAR = "\"";
    private static final String DOUBLE_SPACE = "  ";
    private static final String FOUR_SPACE = "    ";

    void write(String filePath, List<ApnModel> list) throws IOException{
        File file = new File(filePath);

        boolean newFileCreated = false;
        if (!file.exists()) {
            newFileCreated = file.createNewFile();
        }

        if (newFileCreated) {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));

            writeLine(out, "<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            writeLine(out, "");

            writeLine(out, "<apns version=\"8\">");
            writeLine(out, "");

            for (ApnModel apnModel : list) {
                writeLine(out, DOUBLE_SPACE + "<apn " + "carrier=" + SEP_CHAR + apnModel.getCarrier() + SEP_CHAR);
                writeLine(out, FOUR_SPACE + "mcc=" + SEP_CHAR + apnModel.getMcc() + SEP_CHAR);
                writeLine(out, FOUR_SPACE + "mnc=" + SEP_CHAR + apnModel.getMnc() + SEP_CHAR);
                writeLine(out, FOUR_SPACE + "apn=" + SEP_CHAR + apnModel.getApn() + SEP_CHAR);

                if (validString(apnModel.getProxy()))
                    writeLine(out, FOUR_SPACE + "proxy=" + SEP_CHAR + apnModel.getProxy() + SEP_CHAR);

                if (validString(apnModel.getPort()))
                    writeLine(out, FOUR_SPACE + "port=" + SEP_CHAR + apnModel.getPort() + SEP_CHAR);

                if (validString(apnModel.getMmsc()))
                    writeLine(out, FOUR_SPACE + "mmsc=" + SEP_CHAR + apnModel.getMmsc() + SEP_CHAR);

                if (validString(apnModel.getMmsproxy()))
                    writeLine(out, FOUR_SPACE + "mmsproxy=" + SEP_CHAR + apnModel.getMmsproxy() + SEP_CHAR);

                if (validString(apnModel.getMmsport()))
                    writeLine(out, FOUR_SPACE + "mmsport=" + SEP_CHAR + apnModel.getMmsport() + SEP_CHAR);

                if (validString(apnModel.getUser()))
                    writeLine(out, FOUR_SPACE + "user=" + SEP_CHAR + apnModel.getUser() + SEP_CHAR);

                if (validString(apnModel.getPassword()))
                    writeLine(out, FOUR_SPACE + "password=" + SEP_CHAR + apnModel.getPassword() + SEP_CHAR);

                if (validString(apnModel.getProtocol())) {
                    String tmp = apnModel.getProtocol();

                    if (tmp.contains("4") && tmp.contains("6")) {
                        tmp = "IPV4V6";
                    } else if (tmp.contains("4")) {
                        tmp = "IPV4";
                    } else if (tmp.contains("6")) {
                        tmp = "IPV6";
                    } else {
                        System.out.println("WTF, bad protocol: " + tmp);
                        tmp = null;
                    }

                    if (tmp != null)
                        writeLine(out, FOUR_SPACE + "protocol=" + SEP_CHAR + tmp + SEP_CHAR);
                }

                if (validString(apnModel.getAuthtype()))
                    writeLine(out, FOUR_SPACE + "authtype=" + SEP_CHAR + apnModel.getAuthtype() + SEP_CHAR);

                if (validString(apnModel.getType()))
                    writeLine(out, FOUR_SPACE + "type=" + SEP_CHAR + apnModel.getType() + SEP_CHAR);

                //In fact, it may need to change here, my apn-excel only have spn type
                if (validString(apnModel.getMvnoMatchData())) {
                    writeLine(out, FOUR_SPACE + "mvno_type=" + SEP_CHAR + "spn" + SEP_CHAR);
                    writeLine(out, FOUR_SPACE + "mvno_match_data=" + SEP_CHAR + apnModel.getMvnoMatchData() + SEP_CHAR);
                }

                writeLine(out, DOUBLE_SPACE + "/>");
                writeLine(out, "");
            }

            writeLine(out, "</apns>");

            out.close();
        }
    }

    private boolean validString(String str) {
        return str != null && !str.isEmpty();
    }

    private void writeLine(BufferedWriter out, String str) throws IOException{
        out.write(str + "\n");
    }
}
