import java.io.FileInputStream;
import java.io.IOException;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtil {
  public int titleNum;
  public int authorsNum;
  public int ISBNNum;
  private XSSFWorkbook excelBook;

  public ExcelUtil(String file) {
    try {
      excelBook = new XSSFWorkbook(new FileInputStream(file));
    } catch (IOException e) {
      e.printStackTrace();
    }
    //findColumnOrder();
  }

  public XSSFWorkbook getExcelBook() {
    return excelBook;
  }

  public void findColumnOrder() {
    XSSFSheet myExcelSheet = excelBook.getSheetAt(0);
    XSSFRow row = myExcelSheet.getRow(0);
    XSSFCell cell = row.getCell(0);
    if (cell.getStringCellValue().equals("Title")) {
      titleNum = 0;
      cell = row.getCell(1);
      if (cell.getStringCellValue().equals("Authors")) {
        authorsNum = 1;
        ISBNNum = 2;
      } else {
        authorsNum = 2;
        ISBNNum = 1;
      }
    } else if (cell.getStringCellValue().equals("Authors")) {
      authorsNum = 0;
      cell = row.getCell(1);
      if (cell.getStringCellValue().equals("Title")) {
        titleNum = 1;
        ISBNNum = 2;
      } else {
        titleNum = 2;
        ISBNNum = 1;
      }
    } else {
      ISBNNum = 0;
      cell = row.getCell(1);
      if (cell.getStringCellValue().equals("Authors")) {
        authorsNum = 1;
        titleNum = 2;
      } else {
        authorsNum = 2;
        titleNum = 1;
      }
    }
  }

}
