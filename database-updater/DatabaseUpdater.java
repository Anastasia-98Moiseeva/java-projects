import static com.google.common.io.Resources.getResource;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.io.Resources;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import ru.mipt.java2017.hw3.model.Author;
import ru.mipt.java2017.hw3.model.Book;
import ru.mipt.java2017.hw3.model.BookAuthor;

public class DatabaseUpdater {

  HibernateUtil hibernateUtil;
  ExcelUtil inputExcelUtil;
  String outputFile;

  public DatabaseUpdater(String url, String inputFile, String outputFile) {
    hibernateUtil = new HibernateUtil(url);
    inputExcelUtil = new ExcelUtil(inputFile);
    inputExcelUtil.findColumnOrder();
    this.outputFile = outputFile;
  }

  public static void main(String arg[]) {
    DatabaseUpdater databaseUpdater = new DatabaseUpdater(arg[0], arg[1], arg[2]);
    try {
      databaseUpdater.fixTypos();
      databaseUpdater.fillDatabase();
      databaseUpdater.fillExcelFile();
    } finally {
      databaseUpdater.Close();
    }
  }

  public void fixTypos() {
    XSSFWorkbook excelBook = inputExcelUtil.getExcelBook();
    XSSFSheet sheet = excelBook.getSheetAt(0);
    XSSFRow row;
    int isbnNum = inputExcelUtil.ISBNNum;
    int titleNum = inputExcelUtil.titleNum;
    Session session = hibernateUtil.getSessionFactory().openSession();
    session.beginTransaction();

    CriteriaBuilder builder = session.getCriteriaBuilder();
    for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
      row = sheet.getRow(i);
      CriteriaQuery<Book> query = builder.createQuery(Book.class);
      Root<Book> book = query.from(Book.class);
      String value = row.getCell(isbnNum).getStringCellValue();
      value = value.substring(8, 21);
      query.select(book).where(builder.equal(book.get("ISBN"), new BigInteger(value)));
      Query<Book> q = session.createQuery(query);
      try {
        Book result = q.getSingleResult();
        String correctTitle = row.getCell(titleNum).getStringCellValue();
        if (!result.getTitle().equals(correctTitle)) {
          result.setTitle(row.getCell(titleNum).getStringCellValue());
        }
      } catch (NoResultException e) {
        String correctTitle = row.getCell(titleNum).getStringCellValue();
        Book new_book = new Book(new BigInteger(value), correctTitle);
        session.save(new_book);
      } //!
    }

    session.getTransaction().commit();
    session.close();
  }

  public void fillDatabase() {
    XSSFWorkbook excelBook = inputExcelUtil.getExcelBook();
    XSSFSheet sheet = excelBook.getSheetAt(0);
    XSSFRow row;
    int authorsNum = inputExcelUtil.authorsNum;
    int titleNum = inputExcelUtil.titleNum;
    Session session = hibernateUtil.getSessionFactory().openSession();
    session.beginTransaction();
    CriteriaBuilder builder = session.getCriteriaBuilder();
    Author result;
    for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
      row = sheet.getRow(i);
      char c = 160;
      String sc = Character.toString(c);
      String[] authors = row.getCell(authorsNum).getStringCellValue().split(",(\\s*|" + sc +")");
      String bookName = row.getCell(titleNum).getStringCellValue();

      CriteriaQuery<Book> bookQuery = builder.createQuery(Book.class);
      Root<Book> bookRoot = bookQuery.from(Book.class);
      bookQuery.select(bookRoot).where(builder.equal(bookRoot.get("title"), bookName));
      Query<Book> qBook = session.createQuery(bookQuery);
      Book bookResult = qBook.getSingleResult();

      CriteriaQuery<Author> query = builder.createQuery(Author.class);
      Root<Author> author = query.from(Author.class);
      for (int j = 0; j < authors.length; j++) {
        query.select(author).where(builder.equal(author.get("name"), authors[j]));
        Query<Author> authorQuery = session.createQuery(query);
        try {
          result = authorQuery.getSingleResult();
        } catch (NoResultException e) {
          result = new Author(authors[j]);
        }
        BookAuthor bookAuthor = new BookAuthor(bookResult, result, j + 1);
        session.save(bookAuthor);
      }
    }
    session.getTransaction().commit();
    session.close();
  }

  public void fillExcelFile() {
    XSSFWorkbook excelBook = new XSSFWorkbook();

    Sheet sheet = excelBook.createSheet("Books");
    Row row = sheet.createRow(0);
    Cell cell = row.createCell(0);
    cell.setCellValue("ID");
    cell = row.createCell(1);
    cell.setCellValue("ISBN");
    cell = row.createCell(2);
    cell.setCellValue("title");
    cell = row.createCell(3);
    cell.setCellValue("cover");

    SessionFactory sessionFactory = hibernateUtil.getSessionFactory();

    Session session = sessionFactory.openSession();
    session.beginTransaction();

    CriteriaBuilder builder = session.getCriteriaBuilder();
    CriteriaQuery<Book> bookCriteriaQuery = builder.createQuery(Book.class);
    Root<Book> bookRoot = bookCriteriaQuery.from(Book.class);
    bookCriteriaQuery.select(bookRoot);
    Query<Book> bookQuery = session.createQuery(bookCriteriaQuery);
    List<Book> bookList = bookQuery.getResultList();
    for (int i = 0; i < bookList.size(); i++) {
      row = sheet.createRow(i + 1);
      String[] fields = bookList.get(i).getFields();
      for (int j = 0; j < fields.length; j++) {
        cell = row.createCell(j);
        cell.setCellValue(fields[j]);
      }
    }

    sheet = excelBook.createSheet("Authors");
    row = sheet.createRow(0);
    cell = row.createCell(0);
    cell.setCellValue("ID");
    cell = row.createCell(1);
    cell.setCellValue("name");

    CriteriaQuery<Author> authorCriteriaQuery = builder.createQuery(Author.class);
    Root<Author> authorRoot = authorCriteriaQuery.from(Author.class);
    authorCriteriaQuery.select(authorRoot);
    Query<Author> authorQuery = session.createQuery(authorCriteriaQuery);
    List<Author> authorList = authorQuery.getResultList();
    for (int i = 0; i < authorList.size(); i++) {
      row = sheet.createRow(i + 1);
      String[] fields = authorList.get(i).getFields();
      for (int j = 0; j < fields.length; j++) {
        cell = row.createCell(j);
        cell.setCellValue(fields[j]);
      }
    }

    sheet = excelBook.createSheet("Books_Authors");
    row = sheet.createRow(0);
    cell = row.createCell(0);
    cell.setCellValue("ID");
    cell = row.createCell(1);
    cell.setCellValue("books_id");
    cell = row.createCell(2);
    cell.setCellValue("authors_id");
    cell = row.createCell(3);
    cell.setCellValue("num");

    CriteriaQuery<BookAuthor> bookAuthorCriteriaQuery = builder.createQuery(BookAuthor.class);
    Root<BookAuthor> bookAuthorRoot = bookAuthorCriteriaQuery.from(BookAuthor.class);
    bookAuthorCriteriaQuery.select(bookAuthorRoot);
    Query<BookAuthor> bookAuthorQuery = session.createQuery(bookAuthorCriteriaQuery);
    List<BookAuthor> bookAuthorList = bookAuthorQuery.getResultList();
    for (int i = 0; i < bookAuthorList.size(); i++) {
      row = sheet.createRow(i + 1);
      String[] fields = bookAuthorList.get(i).getFields();
      for (int j = 0; j < fields.length; j++) {
        cell = row.createCell(j);
        cell.setCellValue(fields[j]);
      }
    }

    session.getTransaction().commit();
    session.close();

    try {
      excelBook.write(new FileOutputStream(outputFile));
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      excelBook.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void Close() {
    hibernateUtil.getSessionFactory().close();
    try {
      inputExcelUtil.getExcelBook().close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}