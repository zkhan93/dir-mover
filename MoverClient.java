import java.io.*;
import java.net.*;
class MoverClient{
	public static String SAVE_FILENAME="received";
	public static int BUFFER_SIZE=16*1024;
	public static int B_TO_KB=1024;
	public static int B_TO_MB=B_TO_KB*1024;
	public static int B_TO_GB=B_TO_MB*1024;
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
				saveDir(socket);
		}
	}
	public static void saveDir(Socket socket){
		try{
			DataInputStream in=new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			String newFilename=null;
			long newFileSize=0;
			String root=in.readUTF();
			System.out.println(root);
			File rootFile=new File(root);
			File newFile;
			rootFile.mkdirs();
			int totalFiles=in.readInt();
			for(int i=0;i<totalFiles;i++){
				newFilename=in.readUTF();
				newFileSize=in.readLong();
				newFile=new File(rootFile.getCanonicalPath(),newFilename);
				
				if(newFile.getParentFile().mkdirs())
					;//System.out.println("created directories for file"+newFile.getName());
				else
					;//System.out.println("Directories already exist for file"+newFile.getName());
				newFile.createNewFile();
				OutputStream out=new FileOutputStream(newFile);
				byte[] buffer=new byte[BUFFER_SIZE];
				int bytesRead;
				long toRead=newFileSize;
				while(toRead>0){
					bytesRead=in.read(buffer,0,(int)(toRead<BUFFER_SIZE?toRead:BUFFER_SIZE));
					out.write(buffer,0,bytesRead);
					toRead-=bytesRead;
					if(newFileSize<B_TO_KB){
						System.out.print("receiving : "+newFile.getName()+" : "+String.format("%d Bytes of %d Bytes",newFileSize-toRead,newFileSize));
					}else if (newFileSize<B_TO_MB){
						System.out.print("receiving : "+newFile.getName()+" : "+String.format("%.2f KB of %.2f KB",((float)(newFileSize-toRead))/B_TO_KB,((float)newFileSize)/B_TO_KB));
					}
					else if(newFileSize<B_TO_GB){
						System.out.print("receiving : "+newFile.getName()+" : "+String.format("%.2f MB of %.2f MB",((float)(newFileSize-toRead))/B_TO_MB,((float)newFileSize)/B_TO_MB));
					}else{
						System.out.print("receiving : "+newFile.getName()+" : "+String.format("%.2f GB of %.2f GB",((float)(newFileSize-toRead))/B_TO_GB,((float)newFileSize)/B_TO_GB));
					}
				}
				out.flush();
				out.close();
				System.out.println("");
				
			}
		}catch(Exception ex){
			ex.printStackTrace();
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