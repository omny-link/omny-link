package link.omny.analytics;

import link.omny.analytics.api.ReportDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.springframework.stereotype.Component;

@Component
public class TestDataSource implements ReportDataSource {

    private int row = 0;
    String[] columnNames = { "id", "name", "department", "email" };
    String[][] data = {
            { "111", "G Conger", " Orthopaedic", "jim@wheremail.com" },
            { "222", "A Date", "ENT", "adate@somemail.com" },
            { "333", "R Linz", "Paedriatics", "rlinz@heremail.com" },
            { "444", "V Sethi", "Nephrology", "vsethi@whomail.com" },
            { "555", "K Rao", "Orthopaedics", "krao@whatmail.com" },
            { "666", "V Santana", "Nephrology", "vsan@whenmail.com" },
            { "777", "J Pollock", "Nephrology", "jpol@domail.com" },
            { "888", "H David", "Nephrology", "hdavid@donemail.com" },
            { "999", "P Patel", "Nephrology", "ppatel@gomail.com" },
            { "101", "C Comer", "Nephrology", "ccomer@whymail.com" } };

    @Override
    public void init(String tenantId) {
        row = 0;
    }

    @Override
    public Object getFieldValue(JRField field) throws JRException {
        int col = 0;
        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i].equals(field.getName())) {
                col = i;
            }
        }
        return data[row][col];
    }

    @Override
    public boolean next() throws JRException {
        row++;
        if (row < data.length) {
            return true;
        } else {
            return false;
        }
    }

}
