package router.usage.statistics.connector;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.jsoup.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import router.usage.statistics.model.ModelClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.MongoClients.create;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Sorts.descending;
import static java.util.Base64.getEncoder;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.jsoup.Jsoup.connect;
import static router.usage.statistics.util.UtilClass.*;

public class ConnectorClass {

    private ConnectorClass() { throw new IllegalStateException("Utility class"); }

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorClass.class);
    private static final String MONGODB_URI = "mongodb+srv://%s:%s@%s.2sj3v.mongodb.net/<dbname>?retryWrites=true&w=majority";
    private static final String COLLECTION_NAME = "model_class";

    private static String dbName = null;
    private static String dbUsr = null;
    private static String dbPwd = null;
    private static String jsUsr = null;
    private static String jsPwd = null;
    private static String loginAuth = null;

    private static void init() {
        if (dbName == null || dbUsr == null || dbPwd == null || jsUsr == null || jsPwd == null) {
            dbName = getSystemEnvProperty(MONGODB_DATABASE);
            dbUsr = getSystemEnvProperty(MONGODB_USERNAME);
            dbPwd = getSystemEnvProperty(MONGODB_PASSWORD);
            jsUsr = getSystemEnvProperty(JSOUP_USERNAME);
            jsPwd = getSystemEnvProperty(JSOUP_PASSWORD);
            loginAuth = loginAuthorization();
        }
    }

    private static String loginAuthorization() {
        return getEncoder().encodeToString(jsUsr.concat(":").concat(jsPwd).getBytes());
    }

    public static Connection.Response connectionResponse(String url, Map<String, String> cookies, String referrer,
                                                         Map<String, String> data, Connection.Method connectionMethod,
                                                         String userAgent) {
        LOGGER.info("Connection Request: {} | {} | {} | {} | {} | {}", url, cookies, referrer, data, connectionMethod, userAgent);

        try {
            init();
            data.put("login_authorization", loginAuth);

            return connect(url)
                    .cookies(cookies)
                    .referrer(referrer)
                    .data(data)
                    .method(connectionMethod)
                    .userAgent(userAgent)
                    .execute();
        } catch (Exception ex) {
            LOGGER.error("Connection Error: ", ex);
            return null;
        }
    }

    private static MongoClientSettings getMongoClientSettings() {
        init();
        String mongodbUri = String.format(MONGODB_URI, dbUsr, dbPwd, dbName);
        ConnectionString connectionString = new ConnectionString(mongodbUri);
        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder()
                .automatic(true)
                .build());
        CodecRegistry codecRegistry = fromRegistries(getDefaultCodecRegistry(), pojoCodecRegistry);

        return MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(codecRegistry)
                .build();
    }

    private static MongoCollection<ModelClass> getMongoCollectionDataUsage(MongoClient mongoClient) {
        return mongoClient.getDatabase(dbName).getCollection(COLLECTION_NAME, ModelClass.class);
    }

    public static void insertDailyDataUsage(ModelClass modelClass, List<ModelClass> modelClassList) {
        try (MongoClient mongoClient = create(getMongoClientSettings())) {
            MongoCollection<ModelClass> mongoCollectionModelClass = getMongoCollectionDataUsage(mongoClient);

            if (modelClass != null) {
                InsertOneResult insertOneResult = mongoCollectionModelClass.insertOne(modelClass);
                LOGGER.info("Insert One Result: {}", insertOneResult);
            } else {
                InsertManyResult insertManyResult = mongoCollectionModelClass.insertMany(modelClassList, new InsertManyOptions().ordered(false));
                LOGGER.info("Insert Many Result: {}", insertManyResult);
            }
        }
    }

    public static List<ModelClass> retrieveDailyDataUsage(List<String> years) {
        List<ModelClass> modelClassList = new ArrayList<>();

        try (MongoClient mongoClient = create(getMongoClientSettings())) {
            MongoCollection<ModelClass> mongoCollectionModelClass = getMongoCollectionDataUsage(mongoClient);
            FindIterable<ModelClass> findIterableModelClass = mongoCollectionModelClass.find(in("year", years), ModelClass.class)
                    .sort(descending("date"));
            findIterableModelClass.forEach(modelClassList::add);
        }

        return modelClassList;
    }

    public static List<String> retrieveUniqueDates() {
        List<String> dateList = new ArrayList<>();

        try (MongoClient mongoClient = create(getMongoClientSettings())) {
            MongoCollection<ModelClass> mongoCollectionModelClass = getMongoCollectionDataUsage(mongoClient);
            DistinctIterable<String> distinctIterableString = mongoCollectionModelClass.distinct("date", String.class);
            distinctIterableString.forEach(dateList::add);
        }

        return dateList;
    }
}
