package router.usage.statistics.service;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import router.usage.statistics.model.ModelClass;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static java.time.LocalTime.MIDNIGHT;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;
import static org.eclipse.jetty.util.LazyList.isEmpty;
import static org.jsoup.Jsoup.parse;
import static router.usage.statistics.connector.ConnectorClass.*;
import static router.usage.statistics.util.UtilClass.getLongerDate;
import static router.usage.statistics.util.UtilClass.getShorterDate;

public class ServiceClass {

    private ServiceClass() { throw new IllegalStateException("Utility class"); }

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceClass.class);

    private static Map<String, String> cookies = null;
    public static final String LOCAL_DATE_TIME_PATTERN = "yyyy-MM-dd HH:00 a";
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, " +
            "like Gecko) Chrome/87.0.4280.88 Safari/537.36";
    public static final String LOGIN_ACTION_URL = "http://router.asus.com/login.cgi";
    public static final String LOGIN_URL = "http://router.asus.com/Main_Login.asp";
    public static final String GET_TRAFFIC_WAN_URL = "http://router.asus.com/getWanTraffic.asp";
    public static final String TRAFFIC_ANALYZER_URL = "http://router.asus.com/TrafficAnalyzer_Statistic.asp";

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

    public static ModelClass getWanTrafficTodayOnly() {
        List<ModelClass> modelClassList = getWanTraffic("all", "hour", "24");
        List<ModelClass> filteredModelClassList = filterDataUsageListByToday(modelClassList);
        return calculateTotalDataUsage(filteredModelClassList);
    }

    public static List<ModelClass> getWanTraffic(String client, String mode, String dura) {
        if (!isLoggedIn()) {
            login();
        }

        Connection.Response wanTrafficResponse = wanTraffic(client, mode, dura);

        if (wanTrafficResponse == null) {
            return emptyList();
        }

        try {
            Document document = parse(wanTrafficResponse.parse().html());

            if (!isLoggedIn(document)) {
                login();
                wanTrafficResponse = wanTraffic(client, mode, dura);

                if (wanTrafficResponse == null) {
                    return emptyList();
                } else {
                    document = parse(wanTrafficResponse.parse().html());
                }
            }

            Element body = document.body();
            String bodyText = body.text();
            return convertDataUsage(mode, Long.parseLong(dura), bodyText);
        } catch (Exception ex) {
            LOGGER.error("Get Wan Traffic", ex);
            return emptyList();
        }
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
            LOGGER.error("Is Logged In", ex);
            return false;
        }
    }

    private static Connection.Response wanTraffic(String client, String mode, String dura) {
        Map<String, String> formData = new HashMap<>();
        formData.put("client", client);
        formData.put("mode", mode);
        formData.put("dura", dura);
        formData.put("date", getShorterDate());
        formData.put("_", getLongerDate());

        return connectionResponse(GET_TRAFFIC_WAN_URL, cookies, TRAFFIC_ANALYZER_URL, formData,
                Connection.Method.GET, USER_AGENT);
    }

    private static List<ModelClass> convertDataUsage(String mode, long dura, String bodyTexts) {
        List<ModelClass> modelClassList = new ArrayList<>();

        int beginIndex = bodyTexts.indexOf("[[");
        int endIndex = bodyTexts.indexOf("]]");
        bodyTexts = bodyTexts.substring(beginIndex + 1, endIndex).replace("[", "").replace(" ", "");
        List<String> bodyTextList = asList(bodyTexts.split("],"));
        AtomicLong count = new AtomicLong(1);

        bodyTextList.forEach(bodyText -> {
            String[] bodyTextArr = bodyText.split(",");
            modelClassList.add(getDataUsage(count.get(), dura, mode, bodyTextArr[0], bodyTextArr[1]));
            count.getAndIncrement();
        });

        return modelClassList;
    }

    private static ModelClass getDataUsage(long currentCount, long totalCount, String mode, String upload, String download) {
        String total = new BigDecimal(upload).add(new BigDecimal(download)).toString();

        if ("hour".equals(mode)) {
            LocalDateTime localDateTime = LocalDateTime.now().minusHours(totalCount - currentCount);
            return new ModelClass(null, localDateTime.format(ofPattern(LOCAL_DATE_TIME_PATTERN)), String.valueOf(localDateTime.getYear()),
                    localDateTime.getDayOfWeek().toString(), upload, download, total);
        } else {
            LocalDate localDate = LocalDate.now().minusDays(totalCount - currentCount);
            return new ModelClass(null, localDate.toString(), String.valueOf(localDate.getYear()),
                    localDate.getDayOfWeek().toString(), upload, download, total);
        }
    }

    public static void insertDataUsages() {
        List<ModelClass> modelClassListJsoup = getWanTraffic("all", "day", "31");

        if (modelClassListJsoup.isEmpty()) {
            LOGGER.error("Data Usage List Jsoup to Insert is Empty");
        } else {
            LocalDate localDate = LocalDate.now();

            List<String> years = new ArrayList<>();
            years.add(String.valueOf(localDate.getYear()));

            List<String> months = new ArrayList<>();
            months.add(String.valueOf(localDate.getMonthValue()));
            months.add(String.valueOf(localDate.minusMonths(1).getMonthValue()));
            months.add(String.valueOf(localDate.minusMonths(2).getMonthValue()));

            if (localDate.getMonthValue() < 3) {
                years.add(String.valueOf(localDate.minusYears(1).getYear()));
            }

            List<ModelClass> modelClassListMongo = retrieveDataUsages(years, months);
            List<ModelClass> modelClassListToInsert = filterDataUsageListToInsert(modelClassListJsoup, modelClassListMongo);

            if (modelClassListToInsert.isEmpty()) {
                LOGGER.error("Data Usage List to Insert is Empty");
            } else {
                if (modelClassListToInsert.size() == 1) {
                    insertDailyDataUsage(modelClassListToInsert.get(0), null);
                } else {
                    insertDailyDataUsage(null, modelClassListToInsert);
                }
            }
        }
    }

    public static List<ModelClass> retrieveDataUsages(List<String> years, List<String> months) {
        List<ModelClass> modelClassList = retrieveDailyDataUsage(years);

        if (isEmpty(months)) {
            return modelClassList;
        } else {
            return filterDataUsageListByMonth(modelClassList, months);
        }
    }

    public static ModelClass calculateTotalDataUsage(List<ModelClass> modelClassList) {
        BigDecimal totalUploads = new BigDecimal("0.00");
        BigDecimal totalDownloads = new BigDecimal("0.00");
        BigDecimal totalTotals = new BigDecimal("0.00");

        for (ModelClass modelClass : modelClassList) {
            totalUploads = totalUploads.add(new BigDecimal(modelClass.getDataUpload()));
            totalDownloads = totalDownloads.add(new BigDecimal(modelClass.getDataDownload()));
            totalTotals = totalTotals.add(new BigDecimal(modelClass.getDataTotal()));
        }

        return new ModelClass(null, null, null, null, totalUploads.toString(), totalDownloads.toString(), totalTotals.toString());
    }

    private static List<ModelClass> filterDataUsageListByToday(List<ModelClass> modelClassList) {
        LocalDateTime midnightHour = LocalDateTime.of(LocalDate.now(), MIDNIGHT);
        return modelClassList.stream()
                .filter(modelClass -> LocalDateTime.parse(modelClass.getDate(), ofPattern(LOCAL_DATE_TIME_PATTERN)).isAfter(midnightHour))
                .collect(Collectors.toList());
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

    private static List<ModelClass> filterDataUsageListToInsert(List<ModelClass> modelClassListJsoup, List<ModelClass> modelClassListMongos) {
        List<String> modelClassListMongosDates = modelClassListMongos.stream()
                .map(ModelClass::getDate)
                .collect(toList());

        return modelClassListJsoup.stream()
                // do not insert for today's date (insert today's tomorrow)
                .filter(dataUsageJ -> !dataUsageJ.getDate().equals(LocalDate.now().toString()))
                // do not insert if record exists in database for given date
                .filter(dataUsageJ -> !modelClassListMongosDates.contains(dataUsageJ.getDate()))
                .collect(toList());
    }
}
