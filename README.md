# router-usage-statistics

TODO: Add Tests

The project started as a POC for web scraping using JSoup where the objective as to be able to scrape internet data usage from router page.
The reason for need being that the router's UI was only displaying the last three days of data usage only. 

The App logs into Asus Router, gets daily data usage and saves to MongoDb repository on a scheduled time.

The App also provides a way to query the saved data and display using servlet.

The App does not use any frameworks or any view layer technology, it is all backend.

The App runs on embedded Jetty, uses Quartz, JSoup and MongoDb driver.

MongoDb supports BigDecimal objects, should have used that for dataUpload/dataDownload/dataTotals instead of String
to avoid all the conversions during calculations

And, of course, Java should not be used to create html code!
