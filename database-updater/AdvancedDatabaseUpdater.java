import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import ru.mipt.java2017.hw3.model.Book;

public class AdvancedDatabaseUpdater {

  public static String downloadedFile = "YandexBook.xlsx";
  private String outputFile;
  Properties properties;
  private Sardine sardine;
  private DatabaseUpdater databaseUpdater;
  private SessionFactory sessionFactory;
  OkHttpClient client;

  public AdvancedDatabaseUpdater(String url, String yandexFile, String outputFile,
      String propertiesFile)
      throws IOException {
    this.outputFile = outputFile;
    properties = new Properties();
    properties.load(new FileInputStream(propertiesFile));
    sardine = SardineFactory
        .begin(properties.getProperty("yandexUser"), properties.getProperty("yandexPassword"));
    downloadFile(yandexFile, downloadedFile);
    //databaseUpdater.main(new String[]{url, "YandexBook.xlsx", outputFile});
    databaseUpdater = new DatabaseUpdater(url, "YandexBook.xlsx", outputFile);
    //databaseUpdater.Close();
    sessionFactory = databaseUpdater.hibernateUtil.getSessionFactory();
  }

  public static void main(String arg[]) throws IOException {
    AdvancedDatabaseUpdater advancedDatabaseUpdater = new AdvancedDatabaseUpdater(arg[0], arg[1], arg[2], arg[3]);
    try {
      advancedDatabaseUpdater.handleDatabase();
      advancedDatabaseUpdater.fillCovers();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      advancedDatabaseUpdater.databaseUpdater.Close();
    }
  }

  private void downloadFile(String source, String dest) throws IOException {
    File newFile = new File(dest);
    if (!newFile.createNewFile()) {
      //throw new IOException("Cannot create new file!");
    }
    try (InputStream is = sardine.get(source); OutputStream os = new FileOutputStream(newFile)) {
      byte[] buffer = new byte[1024];
      int length;
      while ((length = is.read(buffer)) > 0) {
        os.write(buffer, 0, length);
      }
    }
  }

  public void handleDatabase() {
    databaseUpdater.fixTypos();
    databaseUpdater.fillDatabase();
    databaseUpdater.fillExcelFile();
  }

  public String run(String url) throws IOException {
    client = new OkHttpClient();
    Request request = new Request.Builder()
        .url(url)
        .build();

    Response response = client.newCall(request).execute();
    return response.body().string();
  }

  public void fillCovers() throws IOException {
    Session session = sessionFactory.openSession();
    session.beginTransaction();

    CriteriaBuilder builder = session.getCriteriaBuilder();
    CriteriaQuery<Book> query = builder.createQuery(Book.class);
    Root<Book> bookRoot = query.from(Book.class);

    XSSFWorkbook excelBook = new XSSFWorkbook(new FileInputStream(outputFile));
    XSSFSheet excelSheet = excelBook.getSheetAt(0);
    for (int i = 1; i < excelSheet.getPhysicalNumberOfRows(); i++) {
      XSSFRow row = excelSheet.getRow(i);
      XSSFCell titleCell = row.getCell(2);

      JsonElement imageElement = getImageUrls(titleCell.getStringCellValue());
      JsonObject imageObject = imageElement.getAsJsonObject();
      String imageUrl = imageObject.get("link").getAsString();

      XSSFCell coverCell = row.getCell(3);
      coverCell.setCellValue(imageUrl);

      query.select(bookRoot).where(builder.equal(bookRoot.get("title"), titleCell.getStringCellValue()));
      Query<Book> q = session.createQuery(query);
      Book book = q.getSingleResult();
      book.setCover(imageUrl);
    }

    excelBook.write(new FileOutputStream(outputFile));
    excelBook.close();
    session.getTransaction().commit();
    session.close();

    databaseUpdater.Close();
  }

  public JsonElement getImageUrls(String bookName) throws IOException {
    String url = "https://www.googleapis.com/customsearch/v1?searchType=image&key="
        + properties.getProperty("googleApiKey")
        + "&cx=" + properties.getProperty("googleSearchContext")
        + "&q=" + bookName;
    String result = run(url);

    JsonParser parser = new JsonParser();
    JsonObject jsonObject = parser.parse(result).getAsJsonObject();
    JsonArray imageUrls = jsonObject.getAsJsonArray("items");

    return imageUrls.get(0);
  }

}
