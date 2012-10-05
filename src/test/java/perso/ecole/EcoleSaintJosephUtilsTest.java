package perso.ecole;

import static perso.ecole.EcoleSaintJosephUtils.downloadMenuPdf;
import static perso.ecole.EcoleSaintJosephUtils.dumpPdfToTextFile;
import static perso.ecole.EcoleSaintJosephUtils.parsePdf;
import static perso.ecole.EcoleSaintJosephUtils.uploadMenu;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.List;

import org.junit.Test;

public class EcoleSaintJosephUtilsTest {

  public void getPdfTest() {
    try {
      downloadMenuPdf();
      dumpPdfToTextFile();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void parsePdfTest() throws IOException, ParseException {
    List<List<String>> currentWeek = parsePdf();
    uploadMenu(currentWeek);
  }
}
