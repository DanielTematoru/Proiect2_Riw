package Proiect_02;
import java.util.*;
import java.util.HashMap; 
import java.util.Map; 
import java.io.File;

public class CacheDns {

	public void insertTrie(TrieNode node, String[] ipAddressAdd, String[] urls)
	{
		for(int i=0;i<ipAddressAdd.length;++i)
		{
			this.insertUtil(node,ipAddressAdd[i],urls[i],0);
		}
		
		
	}
	public void insertUtil(TrieNode node, String ipAddr, String url, int pos)
	{
		TrieNode temp=null;
		if(node.child.containsKey(ipAddr.charAt(pos))) {
			temp=node.child.get(ipAddr.charAt(pos));
		}
		else
		{
			temp=new TrieNode();
			node.child.put(ipAddr.charAt(pos), temp);
		}
		if(pos==ipAddr.length()-1)
		{
			temp.url=url;
			return;
		}
		this.insertUtil(temp, ipAddr, url, pos+1);			
	}
	public String search(TrieNode node,String ipAddr, int pos)
	{
		TrieNode temp=null;
		if(pos==ipAddr.length()-1)
		{
			temp=node.child.get(ipAddr.charAt(pos));
			if(temp!=null)
			{
				return temp.url;
			}
		}
		if(node.child.containsKey(ipAddr.charAt(pos)))
		{
			temp=node.child.get(ipAddr.charAt(pos));
			return this.search(temp, ipAddr, pos+1);
		}
		return "Ip invalid";
	}
	

}
class TrieNode{
	Map<Character,TrieNode> child;
	String url;
	TrieNode(){
		this.child=new HashMap<>();
	}
	public String toString()
	{
		return child.toString()+" : "+url;
	}
}
