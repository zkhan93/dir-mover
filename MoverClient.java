import java.io.*;
import java.net.*;
class MoverClient{
	public static String SAVE_FILENAME="received";
	public static int BUFFER_SIZE=16*1024;
	public static int B_TO_MB=1024*1024;
	public static int PORT=8932;
	public static void main(String args[]){
		String serverIp=null;
		try{
			serverIp=args[0];
		}catch(Exception ex){
			System.out.println("pass server IP address as first parameter");
		}
		if(serverIp!=null){
			Socket socket=connectToServer(serverIp);
			if(socket!=null)
				saveFile(socket);
		}
	}
	public static void saveFile(Socket socket){
		InputStream in=null;
		OutputStream out=null;
		try{
			float total=0,processed=0;
			in=socket.getInputStream();
			out=new FileOutputStream(new File(SAVE_FILENAME));
			byte[] buffer=new byte[BUFFER_SIZE];
			int bytesRead;
			while((bytesRead=in.read(buffer))>0){
				out.write(buffer,0,bytesRead);
				processed+=bytesRead;
				System.out.print("\r                         \r");
				System.out.print(String.format("%.2f MB of %.2f MB",processed/B_TO_MB,total/B_TO_MB));
			}
		}catch(Exception ex){
			System.out.println("something bad happened");
			ex.printStackTrace();
		}finally{
			try{
				if(out!=null)
					out.close();
				if(in!=null)
					in.close();
				if(socket!=null)
					socket.close();
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	public static Socket connectToServer(String IP){
		Socket socket=null;
		try{
			socket=new Socket(IP,PORT);
		}catch(Exception ex){
			System.out.println("Cannot connect to server");
		}
		return socket;
	}
	public static void createDir(String name){
		if(name!=null && name.trim().length()>0){
			File file=new File(name);
			file.mkdirs();
		}
	}
}