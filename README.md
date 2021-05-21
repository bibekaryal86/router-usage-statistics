# router-usage-statistics-save

TODO: Add Tests

The project started as a POC for web scraping using JSoup where the objective as to be able to scrape internet data usage from router page.
The reason for need being that the router's UI was only displaying the last three days of data usage only. 

The App logs into Asus Router, gets daily data usage and saves to MongoDb repository on a scheduled time.

The App also provides a way to query the saved data and display using servlet.

The App does not use any frameworks or any view layer technology.

The App runs on embedded Jetty, uses Quartz, JSoup and MongoDb driver.

MongoDb supports BigDecimal objects, should have used that for dataUpload/dataDownload/dataTotals instead of String
to avoid all the conversions during calculations

And, of course, Java should not be used to create html code!

When running the app (on local machine which can assess the router), the following environment variables are needed: (1) website login username (2) website login password 
(3) mongodb database name (4) mongodb username (5) mongodb password

For example: java -jar -DPORT=8005 -DDBNAME=mongodb_database_name -DDBUSR=database_username -DDBPWD=database_password 
-DJSUSR=username_to_login_page -DJSPWD=password_to_login_page "C:\Users\ba5g3\JAVA\jarswars\router-usage-statistics.jar"

- The app is one of the three repos used to save-retrieve-display data:
  - https://github.com/bibekaryal86/router-usage-statistics-save (save data) (this)
  - https://github.com/bibekaryal86/router-usage-statistics-java (retrieve data)
  - https://github.com/bibekaryal86/router-usage-statistics-spa (view data)
 
