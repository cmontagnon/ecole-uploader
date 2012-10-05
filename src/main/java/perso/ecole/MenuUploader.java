package perso.ecole;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class MenuUploader {

  public static void main(String[] args) throws IOException {
    try {
      // Construct data
      String data = URLEncoder.encode("day", "UTF-8") + "=" + URLEncoder.encode("2012-10-08", "UTF-8");
      data += "&" + URLEncoder.encode("menuEntry", "UTF-8") + "=" + URLEncoder.encode("Carotte râpée et oeuf", "UTF-8");
      data +=
          "&" + URLEncoder.encode("menuMainDish", "UTF-8") + "="
              + URLEncoder.encode("Rôti de veau farci au persil", "UTF-8");
      data += "&" + URLEncoder.encode("menuVegetables", "UTF-8") + "=" + URLEncoder.encode("Riz pilaf", "UTF-8");
      data += "&" + URLEncoder.encode("menuCheese", "UTF-8") + "=" + URLEncoder.encode("Saint Nectaire", "UTF-8");
      data += "&" + URLEncoder.encode("menuDessert", "UTF-8") + "=" + URLEncoder.encode("Flan au chocolat", "UTF-8");

      // Send data
      URL url = new URL("http://cyril-ecole.appspot.com/addMenu");
      URLConnection conn =
          url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("planetsurf.nxbp.fr", 8080)));
      conn.setDoOutput(true);
      OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
      wr.write(data);
      wr.flush();

      // Get the response
      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String line;
      while ((line = rd.readLine()) != null) {
        System.out.println(line);
      }
      wr.close();
      rd.close();
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }
}
