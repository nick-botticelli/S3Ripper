package website.nickb.s3ripper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class S3Ripper
{
    public static final String ROOT_FILE = "s3root.xml";
    public static final String OUTPUT_FILE = "s3ripper_urls.txt";

    private static List<String> urls;

    public static void main(String[] args)
    {
        // Init
        System.out.println("=== S3Ripper ===\n\n");
        Scanner scanner = new Scanner(System.in);
        urls = new LinkedList<>();

        // URL input
        System.out.print("Enter URL of root S3 bucket to rip from: ");
        final String url = scanner.nextLine().trim();

        // Main logic
        downloadRootXML(url);
        parseXML();
        ripUrls(url);

        System.out.println("S3Ripper has finished.");
    }

    private static void downloadRootXML(String url)
    {
        System.out.println("Downloading S3 root xml...");

        try
        {
            if (new File(ROOT_FILE).delete())
                System.out.println("Overwriting old \"" + ROOT_FILE + "\".");

            URL website = new URL(url);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(ROOT_FILE);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
        }

        System.out.println("Done!");
    }

    private static void parseXML()
    {
        System.out.println("Parsing XML and collecting URL's...");

        try
        {
            File fXmlFile = new File(ROOT_FILE);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("Contents");

            for (int temp = 0; temp < nList.getLength(); temp++)
            {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element eElement = (Element) nNode;
                    String urlSuffix = eElement.getElementsByTagName("Key").item(0).getTextContent();
                    System.out.println("Key: \"" + urlSuffix + "\"");
                    urls.add(urlSuffix);
                }
            }
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
        }

        System.out.println("Done!");
    }

    private static void ripUrls(String url)
    {
        // For now, save the full URL's line-by-line in a text file
        System.out.println("Writing URL's to file...");

        try (FileWriter writer = new FileWriter(OUTPUT_FILE))
        {
            for (String urlSuffix : urls)
                writer.write((url + '/' + urlSuffix) + System.lineSeparator());
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }

        System.out.println("Done!");
    }
}
