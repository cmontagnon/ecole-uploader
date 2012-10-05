package perso.ecole;

import static com.google.common.collect.Lists.newArrayList;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import com.google.common.base.Splitter;

public class EcoleSaintJosephUtils {

  private static String pdfUrl =
      "http://ecolesaintjoseph.pagesperso-orange.fr/Menus%20du%206%20septembre%202012%20au%2026%20octobre%202012.pdf";
  private static InetSocketAddress proxyAddress = new InetSocketAddress("proxyusers.intranet", 8080);

  public static List<String> wordsThatShouldBeLowerCase = newArrayList("Brest", "Vichy", "Madagascar", "Créole",
      "Paulin", "Nectaire", "Président");

  private static String pdfFileName = "menu.pdf";
  private static String textFileName = "menu.txt";

  public static void downloadMenuPdf() throws MalformedURLException {
    // 1. Retrieve menu pdf from saint joseph we site.
    URLConnection conn = null;
    InputStream webStream = null;

    URL url = new URL(pdfUrl);
    try {
      if (proxyAddress == null) {
        conn = url.openConnection();
      } else {
        conn = url.openConnection(new Proxy(Proxy.Type.HTTP, proxyAddress));
      }
      int contentLength = conn.getContentLength();
      webStream = conn.getInputStream();
      InputStream in = new BufferedInputStream(webStream);
      byte[] data = new byte[contentLength];
      int bytesRead = 0;
      int offset = 0;
      while (offset < contentLength) {
        bytesRead = in.read(data, offset, data.length - offset);
        if (bytesRead == -1)
          break;
        offset += bytesRead;
      }
      in.close();

      if (offset != contentLength) {
        throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
      }

      FileOutputStream out = new FileOutputStream(pdfFileName);
      out.write(data);
      out.flush();
      out.close();
    } catch (IOException e) {
      System.out.println("failed trying to get content from '" + url + "' : " + e.getMessage());
    } finally {
      IOUtils.closeQuietly(webStream);
    }
  }

  public static void dumpPdfToTextFile() {
    try {
      PDFTextStripper stripper = new PDFTextStripper();
      PDDocument doc = PDDocument.load(pdfFileName);

      FileWriter writer = new FileWriter(new File(textFileName));
      stripper.writeText(doc, writer);
      writer.flush();
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static List<List<String>> parsePdf() throws IOException, ParseException {
    List<String> weekDays = newArrayList("LUNDI", "MARDI", "JEUDI", "VENDREDI");
    File file = new File(textFileName);
    BufferedReader buf = new BufferedReader(new FileReader(file));
    String line;
    List<List<String>> allWeeks = newArrayList();
    List<List<String>> weeksInPage = newArrayList();
    boolean isFirstLineOfSection = false;
    int nbDaysInCurrentPage = 0;
    int currentSection = 0;
    List<String> wordsInSection = newArrayList();

    while ((line = buf.readLine()) != null) {
      List<String> currentWords = newArrayList(Splitter.on(" ").omitEmptyStrings().split(line));

      if (weekDays.contains(currentWords.get(0))) {
        if (weeksInPage.size() > 0) {
          addCurrentSection(weeksInPage, nbDaysInCurrentPage, wordsInSection);
          System.out.println("current week :\n" + weeksInPage);
        }
        nbDaysInCurrentPage = currentWords.size();
        wordsInSection.clear();
        allWeeks.addAll(weeksInPage);
        weeksInPage = newArrayList();
        currentSection = 0;
        isFirstLineOfSection = true; // On indique au prochain passage que ca doit contenir des dates
      } else if (isFirstLineOfSection) {
        // On traite les jours différemment des autres lignes
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < currentWords.size(); i++) {
          weeksInPage.add(newArrayList(formatter.format(parser.parse(currentWords.get(i)))));
        }
        isFirstLineOfSection = false;
      } else {
        // On traite une ligne du menu
        if (line.startsWith("*")) {
          // 
          addCurrentSection(weeksInPage, nbDaysInCurrentPage, wordsInSection);
          currentSection++;
          wordsInSection.clear();
        } else {
          wordsInSection.addAll(currentWords);
        }
      }
    }
    if (weeksInPage.size() > 0) {
      addCurrentSection(weeksInPage, nbDaysInCurrentPage, wordsInSection);
      System.out.println("current week :\n" + weeksInPage);
    }
    allWeeks.addAll(weeksInPage);
    buf.close();
    return allWeeks;
  }

  public static void uploadMenu(List<List<String>> menus) {
    try {
      for (List<String> menu : menus) {
        // Construct data
        String data = URLEncoder.encode("day", "UTF-8") + "=" + URLEncoder.encode(menu.get(0), "UTF-8");
        data += "&" + URLEncoder.encode("menuEntry", "UTF-8") + "=" + URLEncoder.encode(menu.get(1), "UTF-8");
        data += "&" + URLEncoder.encode("menuMainDish", "UTF-8") + "=" + URLEncoder.encode(menu.get(2), "UTF-8");
        data += "&" + URLEncoder.encode("menuVegetables", "UTF-8") + "=" + URLEncoder.encode(menu.get(3), "UTF-8");
        data += "&" + URLEncoder.encode("menuCheese", "UTF-8") + "=" + URLEncoder.encode(menu.get(4), "UTF-8");
        data += "&" + URLEncoder.encode("menuDessert", "UTF-8") + "=" + URLEncoder.encode(menu.get(5), "UTF-8");

        // Send data
        URL url = new URL("http://cyril-ecole.appspot.com/addMenu");
        URLConnection conn =
            url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("planetsurf.nxbp.fr", 8080)));
        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(data);
        wr.flush();

        // Get the response
        conn.getInputStream();
        wr.close();
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

  private static void addCurrentSection(List<List<String>> currentWeek, int nbDaysInCurrentPage,
      List<String> wordsInSection) {
    List<String> dishByDay = newArrayList();
    String currentDish = null;
    for (String word : wordsInSection) {
      if (!wordsThatShouldBeLowerCase.contains(word) && Character.isUpperCase(word.charAt(0))) {
        if (currentDish != null) {
          dishByDay.add(currentDish);
        }
        currentDish = word;
      } else {
        currentDish += " " + word;
      }
    }
    dishByDay.add(currentDish);

    if (dishByDay.size() != nbDaysInCurrentPage) {
      System.err.println("Il n'y a pas le bon nombre de majuscule pour cette section : " + wordsInSection);
      System.exit(0);
    } else {
      for (int j = 0; j < nbDaysInCurrentPage; j++) {
        currentWeek.get(j).add(dishByDay.get(j));
      }
    }
  }
}
