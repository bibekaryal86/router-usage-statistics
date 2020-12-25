package router.usage.statistics.view;

import router.usage.statistics.model.ModelClass;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import static java.math.RoundingMode.UP;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;

public class ViewClass {

    private ViewClass() { throw new IllegalStateException("Utility class"); }

    private static final List<String> headers = asList("DATE", "UPLOAD", "DOWNLOAD", "TOTAL");
    private static final String BREAK = "<br>";
    private static final String DIV_CLOSE = "</div>";
    private static final String UPLOAD_SHORT = " | U: ";
    private static final String DOWNLOAD_SHORT = " | D: ";
    private static final String TOTAL_SHORT = " | T: ";

    public static String getDisplay(List<ModelClass> modelClassList, ModelClass modelClassTotal, ModelClass modelClassToday) {
        String docTypeHtml = "<!DOCTYPE html>";
        String htmlOpen = "<html lang=\"en\">";
        String htmlClose = "</html>";
        return docTypeHtml +
                htmlOpen +
                getHead() +
                getBody(modelClassList, modelClassTotal, modelClassToday) +
                htmlClose;
    }

    private static String getHead() {
        String headOpen = "<head>";
        String headClose = "</head>";
        return headOpen +
                getMetaTitle() +
                getStyle() +
                headClose;
    }

    private static String getMetaTitle() {
        return "<meta charset=\"UTF-8\">" +
                "<title>Internet Usage Statistics</title>";
    }

    private static String getStyle() {
        String styleOpen = "<style>";
        String styleClose = "</style>";
        return styleOpen +
                " body { font-family: arial, sans-serif; }" +
                " table { font-family: arial, sans-serif; border-collapse: collapse; width: auto; margin: 8px; }" +
                " td, th { border: 1px solid #dddddd; text-align: left; padding: 8px; }" +
                " tr:nth-child(even) { background-color: #dddddd; }" +
                styleClose;
    }

    private static String getBody(List<ModelClass> modelClasses, ModelClass modelClassTotal, ModelClass modelClassToday) {
        String bodyOpen = "<body>";
        String bodyClose = "</body>";
        return bodyOpen +
                getTotals(modelClassTotal) +
                BREAK +
                getToday(modelClassToday) +
                BREAK +
                getTotalsToday(modelClassTotal, modelClassToday) +
                BREAK +
                getTable(modelClasses) +
                bodyClose;
    }

    private static String getTotals(ModelClass modelClass) {
        String divOpen = "<div id=\"TOTALS\" style=\"font-weight: bold;\">";
        return divOpen +
                " | Totals:" +
                UPLOAD_SHORT +
                getFormattedData(modelClass.getDataUpload()) +
                DOWNLOAD_SHORT +
                getFormattedData(modelClass.getDataDownload()) +
                TOTAL_SHORT +
                getFormattedData(modelClass.getDataTotal()) +
                " | " +
                DIV_CLOSE;
    }

    private static String getToday(ModelClass modelClass) {
        String divOpen = "<div id=\"TODAY\" style=\"font-weight: bold;\">";
        return divOpen +
                " | Today (till " + getTodayUpToTime(now().getHour()) + "): " +
                UPLOAD_SHORT +
                getFormattedData(modelClass.getDataUpload()) +
                DOWNLOAD_SHORT +
                getFormattedData(modelClass.getDataDownload()) +
                TOTAL_SHORT +
                getFormattedData(modelClass.getDataTotal()) +
                " | " +
                DIV_CLOSE;
    }

    private static String getTotalsToday(ModelClass modelClassTotal, ModelClass modelClassToday) {
        String divOpen = "<div id=\"TOTALS_TODAY\" style=\"font-weight: bold;\">";

        return divOpen +
                " | Totals (/w Today): " +
                UPLOAD_SHORT +
                getFormattedData(new BigDecimal(modelClassTotal.getDataUpload()).add(new BigDecimal(modelClassToday.getDataUpload())).toString()) +
                DOWNLOAD_SHORT +
                getFormattedData(new BigDecimal(modelClassTotal.getDataDownload()).add(new BigDecimal(modelClassToday.getDataDownload())).toString()) +
                TOTAL_SHORT +
                getFormattedData(new BigDecimal(modelClassTotal.getDataTotal()).add(new BigDecimal(modelClassToday.getDataTotal())).toString()) +
                " | " +
                DIV_CLOSE;
    }

    private static String getTable(List<ModelClass> modelClasses) {
        String tableOpen = "<table>";
        String tableClose = "</table>";
        return tableOpen +
                getTableHead() +
                getTableBody(modelClasses) +
                tableClose;
    }

    private static String getTableHead() {
        String theadOpen = "<thead>";
        String theadClose = "</thead>";

        String oneTwoThree = "<tr><th colspan=\"4\" style=\"text-align: center;\">DATA OF SELECTED MONTH</th></tr>";

        return theadOpen + oneTwoThree +
                getTableRow(true, null) +
                theadClose;
    }

    private static String getTableBody(List<ModelClass> modelClasses) {
        String tbodyOpen = "<tbody>";
        String tbodyClose = "</tbody>";
        StringBuilder stringBuilder = new StringBuilder(tbodyOpen);
        modelClasses.forEach(modelClass -> stringBuilder.append(getTableRow(false, modelClass)));
        stringBuilder.append(tbodyClose);
        return stringBuilder.toString();
    }

    private static String getTableRow(boolean isHeader, ModelClass modelClass) {
        String trOpen = "<tr>";
        String trClose = "</tr>";
        StringBuilder stringBuilder = new StringBuilder(trOpen);

        if (isHeader) {
           headers.forEach(header -> stringBuilder.append(getTableColumn(true, header)));
        } else {
            stringBuilder.append(getTableColumn(false, getFormattedDate(modelClass.getDate(), modelClass.getDay())));
            stringBuilder.append(getTableColumn(false, getFormattedData(modelClass.getDataUpload())));
            stringBuilder.append(getTableColumn(false, getFormattedData(modelClass.getDataDownload())));
            stringBuilder.append(getTableColumn(false, getFormattedData(modelClass.getDataTotal())));
        }

        stringBuilder.append(trClose);
        return stringBuilder.toString();
    }

    private static String getTableColumn(boolean isHeader, String value) {
        String thtdOpen = isHeader ? "<th>" : "<td>";
        String thtdClose = isHeader ? "</th>" : "</td>";
        return thtdOpen + value + thtdClose;
    }

    private static String getFormattedDate(String date, String day) {
        return date + ", " + day;
    }

    private static String getFormattedData(String dataInput) {
        return "" +
                new DecimalFormat("#,###.##")
                .format(new BigDecimal(dataInput)
                .divide(new BigDecimal("1024"), 2, UP)
                .divide(new BigDecimal("1024"), 2, UP)
                .divide(new BigDecimal("1024"), 2, UP)) +
                " GB";
    }

    private static String getTodayUpToTime(int hour) {
        if (hour < 10) {
            return "0" + hour + ":00a";
        } else {
            if (hour == 11) return hour + ":00a";
            return hour + ":00p";
        }
    }
}
