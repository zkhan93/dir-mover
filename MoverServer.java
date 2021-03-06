import java.io.*;
import java.net.*;
import java.util.*;
class MoverServer{
	public static int PORT=8932;
	public static int BUFFER_SIZE=16*1024;
	public static int B_TO_MB=1024*1024;
	public static void main(String args[]){
		String dirName=null;
		try{
			dirName=args[0];
		}catch(Exception ex){
			System.out.println("pass directory name as first parameter");
		}
		if(dirName!=null){
			File dirFile=new File(dirName);
			if(dirFile.exists()){					
				if(dirFile.isDirectory()){
					System.out.println("serving a directory..");
					serverDir(dirFile);
				}else if(dirFile.isFile()){
					System.out.println("serving a file..");
					serverFile(dirFile);
				}
			}else{
				System.out.println("File or Directory do not exist! Aborting");
			}
		}
	}
	public static void serverDir(File dir){
		int fileCount=0;
		float totalSize=0;
		Socket socket=null;
		DataOutputStream out=null;
		Stack<File> stack=new Stack<File>();
		ArrayList<File> files=new ArrayList<File>();
		try{
			socket=listenForClient();
			out=new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			File file=null;
			stack.push(dir);
			while(!stack.empty()){
				file=stack.pop();
				if(file.isFile()){
					files.add(file);
					totalSize+=file.length();
				}
				if(file.isDirectory()){
					for(File f:file.listFiles())
						stack.push(f);
				}
			}
			fileCount=files.size();
			out.writeUTF(dir.getName());
			out.writeInt(fileCount);
			System.out.println(fileCount+" files and total: "+totalSize+" Bytes");
			for(File f:files){
				out.writeUTF(dir.toURI().relativize(f.toURI()).getPath());
				out.writeLong(f.length());
				InputStream in=new FileInputStream(f);
				byte[] buffer=new byte[BUFFER_SIZE];
				int bytesRead;
				while((bytesRead=in.read(buffer))>0){
					out.write(buffer,0,bytesRead);
				}
				out.flush();
				in.close();
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			try{
				if(out!=null)
					out.close();
				if(socket!=null)
					socket.close();
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	public static void serverFile(File file){
		Socket socket=null;
		InputStream in=null;
		OutputStream out=null;
		try{
			float total=0,processed=0;
			socket=listenForClient();
			in=new FileInputStream(file);
			out=socket.getOutputStream();
			total=file.length();
			byte[] buffer=new byte[BUFFER_SIZE];
			int bytesRead;
			while((bytesRead=in.read(buffer))>0){
				out.write(buffer,0,bytesRead);
				processed+=bytesRead;
				System.out.print("\r                         /r");
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
	public static Socket listenForClient(){
		Socket socket=null;
		try{
			ServerSocket serverSocket=new ServerSocket(PORT);
			socket=serverSocket.accept();
		}catch(Exception ex){
			System.out.println("server cannot listen"+ex);
		}
		return socket;
	}
}