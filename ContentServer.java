import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.net.*;
import java.util.*;

public class ContentServer { 

    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException, ParserConfigurationException, TransformerException{
        
        //get user input
        Scanner input = new Scanner(System.in);
        System.out.println("Enter the server name and port number (in URL format): ");  
        String url = input.nextLine();
        String hostname = url.substring(7,url.length()-5);
        int port = Integer.parseInt(url.substring(url.length()-4));
        System.out.println("Enter the location of a file: ");  
        String filename = input.nextLine();

        Socket socket = null;
        DataOutputStream dos = null;
        DataInputStream dis = null;

        //establish socket connection to server
        socket = new Socket(hostname, port);

        //read feed from text file
        File file = new File(filename);
        // String output;
        Scanner scnr = new Scanner(file);

        //xml parser
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        boolean flag = false;
        boolean entryFlag = false;
        Element feed;
        feed = doc.createElement("feed");
        doc.appendChild(feed);

        Attr lang = doc.createAttribute("xml:lang");
        lang.setValue("en-US");
        feed.setAttributeNode(lang);
        Attr xmlns = doc.createAttribute("xmlns");
        xmlns.setValue("http://www.w3.org/2005/Atom");
        feed.setAttributeNode(xmlns);

        Element entry;
        entry = doc.createElement("entry");
        // while(true){
            //read the file line by line
            try{
                while ( scnr.hasNext() ){
                    String str = scnr.nextLine();
                    //seperate each input by space and store the input into tolens array 
                    String[] line = str.split(":",2);  

                    for (String word : line){
                        if(flag == false){
                            switch(word){
                            
                                case "title":
                                    Element title = doc.createElement("title");
                                    title.appendChild(doc.createTextNode(line[1]));
                                    feed.appendChild(title);
                                break;

                                case "subtitle":
                                    Element subtitle = doc.createElement("subtitle");
                                    subtitle.appendChild(doc.createTextNode(line[1]));
                                    feed.appendChild(subtitle);
                                break;

                                case "link":
                                    Element link = doc.createElement("link");
                                    link.setAttribute("href",line[1]);
                                    link.setAttribute("rel","self");
                                    feed.appendChild(link);
                                break;

                                case "updated":
                                    Element updated = doc.createElement("updated");
                                    updated.appendChild(doc.createTextNode(line[1]));
                                    feed.appendChild(updated);
                                break;

                                case "author":
                                    Element author = doc.createElement("author");
                                    feed.appendChild(author);
                                    Element name = doc.createElement("name");
                                    name.appendChild(doc.createTextNode(line[1]));
                                    author.appendChild(name);
                                break;

                                case "id":
                                    Element id = doc.createElement("id");
                                    id.appendChild(doc.createTextNode(line[1]));
                                    feed.appendChild(id);
                                break;

                                case "entry":
                                    flag = true;
                                    entryFlag = true;
                                break;
                            }

                        }else{

                            if(entryFlag == true){
                                entry = doc.createElement("entry");
                                feed.appendChild(entry);
                                entryFlag = false;
                            }

                            switch(word){

                                case "title":
                                    Element title = doc.createElement("title");
                                    title.appendChild(doc.createTextNode(line[1]));
                                    entry.appendChild(title);
                                break;

                                case "link":
                                    Element link = doc.createElement("link");
                                    link.setAttribute("type","text/html");
                                    link.setAttribute("href",line[1]);
                                    entry.appendChild(link);
                                break;

                                case "id":
                                    Element id = doc.createElement("id");
                                    id.appendChild(doc.createTextNode(line[1]));
                                    entry.appendChild(id);
                                break;

                                case "updated":
                                    Element updated = doc.createElement("updated");
                                    updated.appendChild(doc.createTextNode(line[1]));
                                    entry.appendChild(updated);
                                break;

                                case "author":
                                    Element author = doc.createElement("author");
                                    entry.appendChild(author);
                                    Element name = doc.createElement("name");
                                    name.appendChild(doc.createTextNode(line[1]));
                                    author.appendChild(name);
                                break;

                                case "summary":
                                    Element summary = doc.createElement("summary");
                                    summary.appendChild(doc.createTextNode(line[1]));
                                    entry.appendChild(summary);
                                break;

                                case "entry":
                                    entryFlag = true;
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Client exception: " + e.toString());
                e.printStackTrace();
            }

            //transform to output file
            DOMSource source = new DOMSource(doc);
            StringWriter writer = new StringWriter();

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StreamResult result = new StreamResult(new File("feed.xml"));
            transformer.transform(source, result);
            result = new StreamResult(writer);
            transformer.transform(source, result);
            String outputString = writer.toString();


            //write to socket using ObjectOutputStream
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            //send a PUT request to the aggregation server
            System.out.println("Sending PUT request to Aggregation Server");
            dos.writeUTF("PUT /atom.xml HTTP/1.1\nUser-Agent: ATOMClient/1/0\nContent-Type: application/atom+xml\nContent-Length: "+outputString.length()+"\n"+outputString);

            String response = dis.readUTF();
            System.out.println("response: "+response);
            dis.close();
            dos.close();
            input.close();

        //}
    }
}
