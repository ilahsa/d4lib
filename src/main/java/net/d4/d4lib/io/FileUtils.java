package net.d4.d4lib.io;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 可以使用 common-io 替代这个工具类
 * @author baibq
 *
 */
public class FileUtils {
	/** 
     * Mapped File  way 
     * MappedByteBuffer 可以在处理大文件时，提升性能 
     * @param fileName 
     * @return 
     * @throws IOException 
     */  
    @SuppressWarnings("resource")
	public static byte[] readFileToByte(String fileName)throws IOException{  
          
        FileChannel fc = null;  
        try{  
            fc = new RandomAccessFile(fileName,"r").getChannel();  
            MappedByteBuffer byteBuffer = fc.map(MapMode.READ_ONLY, 0, fc.size()).load();  
            System.out.println(byteBuffer.isLoaded());  
            byte[] result = new byte[(int)fc.size()];  
            if (byteBuffer.remaining() > 0) {  
                byteBuffer.get(result, 0, byteBuffer.remaining());  
            }  
            return result;  
        }catch (IOException e) {  
            e.printStackTrace();  
            throw e;  
        }finally{  
            try{  
                fc.close();  
            }catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
    }
    /**
     * 读取文件所有行  list 安装add 的顺序排序
     * 
     * @param fileName
     * @return
     * @throws IOException
     */
    public static String[] readByBufferedReader(String fileName) throws IOException{
    	  //BufferedReader是可以按行读取文件  
        FileInputStream inputStream = new FileInputStream(fileName);  
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));  
        List<String> lst = new ArrayList<String>();      
        String str = null;  
        while((str = bufferedReader.readLine()) != null)  
        {  
            lst.add(str);
        }  
              
        //close  
        inputStream.close();  
        bufferedReader.close();  
        return lst.toArray(new String[lst.size()]);
    }
    
    /**
     *  向文件中写数据（FileReader）【注意使用FileReader（“path”，true）可以往文件后面追加内容，否则就直接覆盖了】
     * @param fileName
     * @param data
     * @throws IOException
     */
    public static void writeByFileReader(String fileName,String content) throws IOException {  
        File file = new File(fileName);  
        // if file doesnt exists, then create it  
        if (!file.exists()) {  
            file.createNewFile();  
        }  
        
        // true = append file  
        FileWriter fileWritter = new FileWriter(file.getName(), true);  
        fileWritter.write(content);  
        fileWritter.close();  
    } 
    
    /**
     * 采用BufferedWriter向文件中写数据
     * @param fileName
     * @param content
     * @throws IOException
     */
    public static void writeByBufferedReader(String fileName ,String content) throws IOException {  
            File file = new File(fileName);  
            // if file doesnt exists, then create it  
            if (!file.exists()) {  
                file.createNewFile();  
            }  
            FileWriter fw = new FileWriter(file, true);  
            BufferedWriter bw = new BufferedWriter(fw);  
            bw.write(content);  
            bw.flush();  
            bw.close();  
    }  
}
