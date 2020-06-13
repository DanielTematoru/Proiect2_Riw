package Proiect_02;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;

public class DnsClient {

	public void createRequest(byte[] request, String domain) {
		for(int i=0; i<12+ domain.length() + 6; i++) {
			request[i]=0x0;
		}
		
        Random r = new Random();
        int identifier = r.nextInt(1 << 16 - 1);
        //System.out.println("Identifier : " +identifier);
        request[0]=(byte)((identifier&0XFF00)>>8);
        request[1]=(byte) (identifier&0xFF);
        request[2]=0x01;
        request[5]=0x01;
        
        char[] charsDomain = domain.toCharArray();
        int contor=0;
        int poz=12;
        for(int i=0; i<domain.length(); i++) {
        	if(charsDomain[i]!='.') {
        		contor+=1;
        		request[contor+poz]=(byte) charsDomain[i];
        	}else {
        		request[poz]=(byte)contor;
        		poz+=contor+1;
        		contor=0;
        	}
        	if(i==domain.length()-1) {
        		request[poz]=(byte)contor;
        		poz+=contor;
        		contor=0;
        	}
        }
        
        request[poz+3]=0x01;
        request[poz+5]=0x01;
	}
	
	public void printArrayByte(byte[] input) {
		for (int i = 0; i < input.length; ++i) {
           // System.out.print('\t');
            //System.out.print(String.format("[0x%02X]", input[i]));
            if ((i + 1) % 8 == 0) {
               // System.out.println();
            }
        }
		 //System.out.println();
	}
	
	public void getResponse(byte[] request, byte[] responseFromRequest) throws IOException {
		try {
			DatagramSocket datagramSocket = new DatagramSocket();
            InetAddress IP = InetAddress.getByName(Restrictii.dnsValue);
            //System.out.print(IP);
            DatagramPacket requestPacket = new DatagramPacket(request, request.length, IP, Restrictii.portUDP);
            datagramSocket.send(requestPacket);
            //System.out.println("S-a trimis pachetul");
            DatagramPacket responsePacket = new DatagramPacket(responseFromRequest, 512);
            datagramSocket.receive(responsePacket);
            
            //System.out.println("S-a primit pachetul");
            datagramSocket.close();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public String checkResponse(byte[] response, byte[] request, String domain) {
		StringBuilder iPv4=new StringBuilder();
        if (response[0] == request[0] && response[1]==request[1]) {
            //System.out.println("Identificatorii se potrivesc");
        }

        if ((response[3] & 0x0F) == 0x00)
        {
           // System.out.println("Nicio eroare produsa: RCode 0 -> OK");
        } else {
            //int errorCode = response[3] & 0x0F;
            //System.out.println("S-a produs o eroare: RCode = " + errorCode);
        }
        int numberResponse = ((0xFF & response[6]) << 8) | (0xFF & response[7]);
        //System.out.println("Numarul de raspunsuri primite: " + numberResponse);
        int responseID=0, indexPosition=12+domain.length()+6;
        while(responseID<numberResponse){
        	//System.out.println(responseID);
        	iPv4=new StringBuilder();
        	String resourceName=getPointer(indexPosition,response);
        	if((response[indexPosition] & 0xFF)<192){
        		indexPosition+=resourceName.length()+1;
        	}else {
        		indexPosition+=2;
        	}
        	//System.out.println("Response ["+responseID+"] : " + resourceName);
        	//System.out.println();
        	byte higher, lower;
        	
        	//get Record Type
        	higher=response[indexPosition++];
        	lower=response[indexPosition++];
        	int typeOfRecord= (((0xFF) & higher) << 8) | (0xFF & lower);
        	if(typeOfRecord==1) {
        		//System.out.println("Type Record: IPv4");
        	}else if(typeOfRecord==5) {
        		//System.out.println("Type Record: Nume canonic");
        	}
        	
        	//get Record Class
        	higher=response[indexPosition++];
        	lower=response[indexPosition++];
        	int classOfRecord= (((0xFF) & higher) << 8) | (0xFF & lower);
        	if(classOfRecord==1) {
        		//System.out.println("Record Class: Internet");
        	}
        	
        	//Avoid 4 bytes(TTL bytes)
        	indexPosition+=4;
        	
        	//get data Length
        	higher=response[indexPosition++];
        	lower=response[indexPosition++];
        	int lengthOfData= (((0xFF) & higher) << 8) | (0xFF & lower);
        	//System.out.println("Data length : " + lengthOfData);
        	//System.out.println();
        	
        	if(lengthOfData==4 && typeOfRecord==1) {

        		for(int i=0; i<lengthOfData; i++) {
        			iPv4.append(response[indexPosition++] & 0xFF);
        			iPv4.append('.');
        			//System.out.println(iPv4);
        		}
        		iPv4.deleteCharAt(iPv4.length()-1);
        		//System.out.println("Adresa IPv4: " + iPv4 );
        	}else if(typeOfRecord==5) {
        		String nameReturn=getPointer(indexPosition,response);
        		indexPosition+=lengthOfData;
        		//System.out.println("Nume canonic : " + nameReturn);
        	}else {
        		//System.out.println("Unknown Record Type and Data Length");
        	}
        	responseID++;
        }
        return iPv4.toString();
	}
	
    public String getPointer(int indexForPointer, byte[] buffer)
    {
        if ((buffer[indexForPointer] & 0xFF) == 0x0)
        {
            return "";
        }

        if ((buffer[indexForPointer] & 0xFF) >= 192) 
        {
            int newPointerIndex = ((buffer[indexForPointer] & 0x3F) << 8) | (buffer[indexForPointer + 1] & 0xFF);
            return getPointer(newPointerIndex, buffer);
        }

        int currentNumberOfCharacters = buffer[indexForPointer++] & 0xFF;
        StringBuilder currentElement = new StringBuilder();
        for (int i = 0; i < currentNumberOfCharacters; ++i)
        {
            currentElement.append((char)(buffer[indexForPointer + i] & 0xFF));
        }
        indexForPointer += currentNumberOfCharacters;
        return (currentElement.toString() + "." + getPointer(indexForPointer, buffer));
    }
}

