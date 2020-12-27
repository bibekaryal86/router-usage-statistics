package router.usage.statistics.view;

import router.usage.statistics.model.ModelClass;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;

import static java.math.RoundingMode.UP;
import static java.util.Arrays.asList;

public class ViewClass {

    private ViewClass() {
        throw new IllegalStateException("View class");
    }

    private static final List<String> headers = asList("DATE", "UPLOAD", "DOWNLOAD", "TOTAL");
    private static final String BREAK = "<br>";
    private static final String DIV_CLOSE = "</div>";
    private static final String UPLOAD_SHORT = " | U: ";
    private static final String DOWNLOAD_SHORT = " | D: ";
    private static final String TOTAL_SHORT = " | T: ";

    public static String getDisplay(List<ModelClass> modelClassList, ModelClass modelClassTotal,
                                    String selected, Set<String> yearMonthSet) {
        String docTypeHtml = "<!DOCTYPE html>";
        String htmlOpen = "<html lang=\"en\">";
        String htmlClose = "</html>";
        return docTypeHtml +
                htmlOpen +
                getHead() +
                getBody(modelClassList, modelClassTotal, selected, yearMonthSet) +
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

    private static String getBody(List<ModelClass> modelClasses, ModelClass modelClassTotal,
                                  String selected, Set<String> yearMonthSet) {
        String bodyOpen = "<body>";
        String bodyClose = "</body>";

        return bodyOpen +
                getTotals(modelClassTotal) +
                BREAK +
                getAvailableDataTable(yearMonthSet) +
                currentMonthLink() +
                getDataUsageTable(modelClasses, selected) +
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

    private static String currentMonthLink() {
        String divOpen = "<div id=\"CURRENT\" style=\"font-weight: bold;\">";
        return divOpen +
                addLink("/", "BACK TO CURRENT MONTH") +
                DIV_CLOSE;
    }

    private static String getDataUsageTable(List<ModelClass> modelClasses, String selected) {
        String tableOpen = "<table>";
        String tableClose = "</table>";
        return tableOpen +
                getDataUsageTableHead(selected) +
                getDataUsageTableBody(modelClasses) +
                tableClose;
    }

    private static String getDataUsageTableHead(String selected) {
        String theadOpen = "<thead>";
        String theadClose = "</thead>";
        String headerOne = "<tr><th colspan=\"4\" style=\"text-align: center;\">DATA OF SELECTED MONTH (" + selected + ") </th></tr>";
        return theadOpen +
                headerOne +
                getDataUsageTableRow(true, null) +
                theadClose;
    }

    private static String getDataUsageTableBody(List<ModelClass> modelClasses) {
        String tbodyOpen = "<tbody>";
        String tbodyClose = "</tbody>";
        StringBuilder stringBuilder = new StringBuilder(tbodyOpen);
        modelClasses.forEach(modelClass -> stringBuilder.append(getDataUsageTableRow(false, modelClass)));
        stringBuilder.append(tbodyClose);
        return stringBuilder.toString();
    }

    private static String getDataUsageTableRow(boolean isHeader, ModelClass modelClass) {
        String trOpen = "<tr>";
        String trClose = "</tr>";
        StringBuilder stringBuilder = new StringBuilder(trOpen);

        if (isHeader) {
           headers.forEach(header -> stringBuilder.append(getTableColumn(true, header, false)));
        } else {
            stringBuilder.append(getTableColumn(false, getFormattedDate(modelClass.getDate(), modelClass.getDay()), false));
            stringBuilder.append(getTableColumn(false, getFormattedData(modelClass.getDataUpload()), false));
            stringBuilder.append(getTableColumn(false, getFormattedData(modelClass.getDataDownload()), false));
            stringBuilder.append(getTableColumn(false, getFormattedData(modelClass.getDataTotal()), false));
        }

        stringBuilder.append(trClose);
        return stringBuilder.toString();
    }

    private static String getAvailableDataTable(Set<String> yearMonthSet) {
        String tableOpen = "<table>";
        String tableClose = "</table>";
        return tableOpen +
                getAvailableDataTableBody(yearMonthSet) +
                tableClose;
    }

    private static String getAvailableDataTableBody(Set<String> yearMonthSet) {
        String tbodyOpen = "<tbody>";
        String tbodyClose = "</tbody>";
        String trOpen = "<tr>";
        String trClose = "</tr>";
        StringBuilder stringBuilder = new StringBuilder(tbodyOpen);
        stringBuilder.append(trOpen);
        stringBuilder.append(getTableColumn(true, "AVAILABLE", false));
        yearMonthSet.forEach(yearMonth -> stringBuilder.append(getTableColumn(false, yearMonth, true)));
        stringBuilder.append(trClose);
        stringBuilder.append(tbodyClose);
        return stringBuilder.toString();
    }

    private static String getTableColumn(boolean isHeader, String value, boolean isAddLink) {
        String thtdOpen = isHeader ? "<th>" : "<td>";
        String thtdClose = isHeader ? "</th>" : "</td>";

        if (isAddLink) {
            return thtdOpen +
                    addLink("/?selected="+value, value) +
                    thtdClose;
        } else {
            return thtdOpen +
                    value +
                    thtdClose;
        }
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

    private static String addLink(String link, String display) {
        return "<a href=\"" + link + "\">" + display + "</a>";
    }
}
