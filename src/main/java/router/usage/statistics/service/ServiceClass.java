package router.usage.statistics.service;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import router.usage.statistics.model.ModelClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;

import static java.util.Arrays.asList;
import static java.util.Collections.reverse;
import static java.util.Collections.reverseOrder;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static org.eclipse.jetty.util.LazyList.isEmpty;
import static org.jsoup.Jsoup.parse;
import static org.slf4j.LoggerFactory.getLogger;
import static router.usage.statistics.connector.ConnectorClass.*;
import static router.usage.statistics.util.UtilClass.getLongerDate;
import static router.usage.statistics.util.UtilClass.getShorterDate;

public class ServiceClass {

    private ServiceClass() {
        throw new IllegalStateException("Service class");
    }

    private static final Logger LOGGER = getLogger(ServiceClass.class);

    private static Map<String, String> cookies = null;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, " +
            "like Gecko) Chrome/87.0.4280.88 Safari/537.36";
    private static final String LOGIN_ACTION_URL = "http://router.asus.com/login.cgi";
    private static final String LOGIN_URL = "http://router.asus.com/Main_Login.asp";
    private static final String GET_TRAFFIC_WAN_URL = "http://router.asus.com/getWanTraffic.asp";
    private static final String TRAFFIC_ANALYZER_URL = "http://router.asus.com/TrafficAnalyzer_Statistic.asp";

    public static void insertDataUsages() {
        LOGGER.info("Start Insert Data Usages");

        LocalDateTime localDateTime = LocalDateTime.now();
        ModelClass modelClassJsoup = getWanTraffic(localDateTime);
        ModelClass modelClassMongo = retrieveDataUsage(localDateTime);

        dailyDataInsert(modelClassJsoup, modelClassMongo);

        LOGGER.info("Finish Insert Data Usages");
    }

    public static Set<String> retrieveUniqueDatesOnly() {
        List<String> uniqueDates = retrieveUniqueDates();
        return uniqueDates.stream()
                .map(uniqueDate -> uniqueDate.substring(0, 7))
                .sorted(reverseOrder())
                .collect(toCollection(LinkedHashSet::new));
        // unsorted.stream().sorted(nullsLast(comparing(ClassName::getMethodName, nullsLast(naturalOrder())))).collect(toList());   // NOSONAR
    }

    public static List<ModelClass> retrieveDataUsages(List<String> years, List<String> months) {
        List<ModelClass> modelClassList = retrieveDailyDataUsage(years, null);

        if (isEmpty(months)) {
            return modelClassList;
        } else {
            return filterDataUsageListByMonth(modelClassList, months);
        }
    }

    public static ModelClass retrieveDataUsage(LocalDateTime localDateTime) {
        String date = getModelClassDate(localDateTime);
        List<ModelClass> modelClassList = retrieveDailyDataUsage(null, date);

        if (isEmpty(modelClassList)) {
            return null;
        } else {
            return modelClassList.get(0);
        }
    }

    public static ModelClass calculateTotalDataUsage(List<ModelClass> modelClassList) {
        LocalDateTime localDateTime = LocalDateTime.now();
        BigDecimal totalUploads = new BigDecimal("0.00");
        BigDecimal totalDownloads = new BigDecimal("0.00");
        BigDecimal totalTotals = new BigDecimal("0.00");

        for (ModelClass modelClass : modelClassList) {
            totalUploads = totalUploads.add(new BigDecimal(modelClass.getDataUpload()));
            totalDownloads = totalDownloads.add(new BigDecimal(modelClass.getDataDownload()));
            totalTotals = totalTotals.add(new BigDecimal(modelClass.getDataTotal()));
        }

        return new ModelClass(null, localDateTime.toLocalDate().toString(), String.valueOf(localDateTime.getYear()),
                localDateTime.getDayOfWeek().toString(), totalUploads.toString(), totalDownloads.toString(),
                totalTotals.toString());
    }

    private static ModelClass getWanTraffic(LocalDateTime localDateTime) {
        LOGGER.info("Start Get Wan Traffic");

        if (!isLoggedIn()) {
            login();
        }

        Connection.Response wanTrafficResponse = wanTraffic();

        if (wanTrafficResponse == null) {
            return null;
        }

        try {
            Document document = parse(wanTrafficResponse.parse().html());

            if (!isLoggedIn(document)) {
                login();
                wanTrafficResponse = wanTraffic();

                if (wanTrafficResponse == null) {
                    return null;
                } else {
                    document = parse(wanTrafficResponse.parse().html());
                }
            }

            return convertDataUsage(document.body().text(), localDateTime);
        } catch (Exception ex) {
            LOGGER.error("Get Wan Traffic Error", ex);
            return null;
        }
    }

    private static void dailyDataInsert(ModelClass modelClassJsoup, ModelClass modelClassMongo) {
        LOGGER.info("Daily Data Insert: {} | {}", modelClassJsoup, modelClassMongo);

        if (modelClassJsoup == null) {
            LOGGER.error("Data Usage Jsoup to Insert is Null");
        } else {
            if (modelClassMongo == null) {
                insertDailyDataUsage(modelClassJsoup);
            } else {
                updateDailyDataUsage(modelClassJsoup, modelClassJsoup.getDate());
            }
        }
    }

    private static void login() {
        Map<String, String> formData = new HashMap<>();
        formData.put("group_id", "");
        formData.put("action_mode", "");
        formData.put("action_script", "");
        formData.put("action_wait", "");
        formData.put("current_page", "Main_Login.asp");
        formData.put("next_page", "index.asp");
        formData.put("login_captcha", "");

        Connection.Response connectionResponse = connectionResponse(LOGIN_ACTION_URL, new HashMap<>(), LOGIN_URL, formData,
                Connection.Method.POST, USER_AGENT);

        if (connectionResponse != null) {
            cookies = connectionResponse.cookies();
        }
    }

    private static String getModelClassDate(LocalDateTime localDateTime) {
        return localDateTime.getHour() == 0
                ? localDateTime.toLocalDate().minusDays(1).toString()
                : localDateTime.toLocalDate().toString();
    }

    private static String getModelClassYear(LocalDateTime localDateTime) {
        return localDateTime.getHour() == 0 && localDateTime.getMonthValue() == 1 && localDateTime.getDayOfMonth() == 1
                ? String.valueOf(localDateTime.toLocalDate().minusDays(1).getYear())
                : String.valueOf(localDateTime.toLocalDate().getYear());
    }

    private static String getModelClassDay(LocalDateTime localDateTime) {
        return localDateTime.getHour() == 0
                ? localDateTime.toLocalDate().minusDays(1).getDayOfWeek().toString()
                : localDateTime.toLocalDate().getDayOfWeek().toString();
    }

    private static boolean isLoggedIn() {
        return cookies != null && cookies.containsKey("asus_token");
    }

    private static boolean isLoggedIn(Document document) {
        try {
            Element head = document.head();
            Elements scripts = head.select("script");
            return scripts.isEmpty();
        } catch (Exception ex) {
            LOGGER.error("Is Logged In Error", ex);
            return false;
        }
    }

    private static Connection.Response wanTraffic() {
        Map<String, String> formData = new HashMap<>();
        formData.put("client", "all");
        formData.put("mode", "hour");
        formData.put("dura", "24");
        formData.put("date", getShorterDate());
        formData.put("_", getLongerDate());

        return connectionResponse(GET_TRAFFIC_WAN_URL, cookies, TRAFFIC_ANALYZER_URL, formData,
                Connection.Method.GET, USER_AGENT);
    }

    private static ModelClass convertDataUsage(String bodyTexts, LocalDateTime localDateTime) {
        int beginIndex = bodyTexts.indexOf("[[");
        int endIndex = bodyTexts.indexOf("]]");
        bodyTexts = bodyTexts.substring(beginIndex + 1, endIndex).replace("[", "").replace(" ", "");
        List<String> bodyTextList = asList(bodyTexts.split("],"));
        reverse(bodyTextList);

        int numberOfHours = localDateTime.getHour() == 0 ? bodyTextList.size() : localDateTime.getHour();
        BigDecimal upload = new BigDecimal("0.00");
        BigDecimal download = new BigDecimal("0.00");

        for (int i = 0; i < numberOfHours; i++) {
            String[] bodyTextArr = bodyTextList.get(i).split(",");
            upload = upload.add(new BigDecimal(bodyTextArr[0]));
            download = download.add(new BigDecimal(bodyTextArr[1]));
        }

        BigDecimal total = upload.add(download);
        String date = getModelClassDate(localDateTime);
        String year = getModelClassYear(localDateTime);
        String day = getModelClassDay(localDateTime);
        return new ModelClass(null, date, year, day, upload.toString(), download.toString(), total.toString());
    }

    private static List<ModelClass> filterDataUsageListByMonth(List<ModelClass> modelClassList, List<String> months) {
        return modelClassList.stream()
                .filter(modelClass -> months.stream()
                        .anyMatch(month -> {
                            Matcher matcher = compile("-(\\w+)-").matcher(modelClass.getDate());
                            return matcher.find() && month.equals(matcher.group(1));
                        }))
                .collect(toList());
    }
}
