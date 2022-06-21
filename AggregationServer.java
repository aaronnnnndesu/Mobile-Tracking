import java.io.*;
import java.lang.ClassNotFoundException;
import java.net.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;


public class AggregationServer { 
    
    //static ServerSocket variable
    private static ServerSocket server;
    //socket server port on which it will listen
    private static int port = 4567;
    
    public static void main(String args[]) throws IOException, ClassNotFoundException{
        //create the socket server object
        server = new ServerSocket(port);
        //keep listens indefinitely until receives 'exit' call or program terminates
        while(true){
            Socket socket = null;
            try{
                System.out.println("Server Connected ...");
                //create socket and waiting for client connection
                socket = server.accept();
                System.out.println("A new connection : " + socket); 

                //create input and output streams 
                DataInputStream dis = new DataInputStream(socket.getInputStream()); 
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream()); 
                
                //create and start thread for each connection
                Thread t = new MultiClient(socket, dis, dos);
                t.start();
            }catch (Exception e){

                //if there is an error, then the socket will close and print the error message
                socket.close();
                e.printStackTrace(); 
            }
        }
    }   
}

//Implementation for threads
class MultiClient extends Thread {

    final Socket soc;
    final DataInputStream dis; 
    final DataOutputStream dos; 

    public MultiClient(Socket socket, DataInputStream dis, DataOutputStream dos){
        this.soc = socket; 
        this.dis = dis; 
        this.dos = dos;
    }

    //convert .xml to string
    public String ConvertFeed(String fileLocation){
        String submessage = "message";
        try{

            File fXmlFile = new File(fileLocation);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();

            Element docEle = doc.getDocumentElement();
            NodeList nList = docEle.getChildNodes();
            for (int temp = 0; temp < nList.getLength(); temp++) {

                //make sure it's element node
                if (nList.item(temp).getNodeType() == Node.ELEMENT_NODE){
                    Element el = (Element) nList.item(temp);
                    if(el.getNodeName().equals("title")){
                        submessage=("title:"+ el.getTextContent()+"\n");
                    }else if(el.getNodeName().equals("subtitle")){
                        submessage+=("subtitle:"+ el.getTextContent()+"\n");
                    }else if(el.getNodeName().equals("link")){
                        submessage+=("link:"+ el.getAttribute("href")+"\n");
                    }else if(el.getNodeName().equals("updated")){
                        submessage+=("updated:"+ el.getTextContent()+"\n");
                    }else if(el.getNodeName().equals("author")){
                        submessage+=("author:"+ el.getTextContent()+"\n");
                    }else if(el.getNodeName().equals("id")){
                        submessage+=("id:"+ el.getTextContent());
                    }else if(el.getNodeName().equals("entry")){
                        submessage+=("\n\n"+"entry"+"\n");
                        NodeList nList2 = el.getChildNodes();
                        for (int temp2 = 0; temp2 < nList2.getLength(); temp2++) {
                            if (nList2.item(temp2).getNodeType() == Node.ELEMENT_NODE){
                            el = (Element) nList2.item(temp2);
                                if(el.getNodeName().equals("title")){
                                    submessage+=("title:"+ el.getTextContent()+"\n");
                                }else if(el.getNodeName().equals("link")){
                                    submessage+=("link:"+ el.getAttribute("href")+"\n");
                                }else if(el.getNodeName().equals("id")){
                                    submessage+=("id:"+ el.getTextContent()+"\n");
                                }else if(el.getNodeName().equals("updated")){
                                    submessage+=("updated:"+ el.getTextContent()+"\n");
                                }else if(el.getNodeName().equals("summary")){
                                    submessage+=("summary:"+ el.getTextContent());
                                }
                            }
                        }
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return submessage;
    }

    public static String received = "received";
    public static String send = "send";
    public static String feed = "init";

    public void run(){

            try{
                //read from socket to ObjectInputStream object
                received = dis.readUTF();
                //System.out.println("received: "+received);

                if(received.equals("exit")) 
                {  
                    this.soc.close(); 
                    System.out.println("Connection closed"); 
                } 

                String check = received.substring(0,3);
                switch(check){
                    case "PUT":
                    feed = ConvertFeed("feed.xml");
                    dos.writeUTF("201 - HTTP_CREATED");
                    break;

                    case "GET":
                    send = feed;
                    dos.writeUTF(send);
                    break;
                }
            }catch (Exception e){
                e.printStackTrace(); 
            }

            try{
                //closing resources 
                this.dis.close(); 
                this.dos.close(); 
            }catch(IOException e){ 
                e.printStackTrace(); 
            } 

    }
}




















