package net.d4.d4lib;

import java.io.File;
import net.d4.d4lib.plugin.GenerateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 *参考 http://www.cnblogs.com/ty408/p/5337549.html
 */
public class CreateHandler {

    private static final Logger log = LoggerFactory.getLogger(CreateHandler.class);

    public CreateHandler(String pathString, Boolean isReq) {
        try {
            String userPath = System.getProperty("user.dir");
            File directory = new File(userPath + pathString);//设定为当前文件夹
            String protoPath = directory.getCanonicalPath();//获取标准的路径
            log.error(protoPath);
            GenerateHandler handler = new GenerateHandler(
                    protoPath + "\\src\\main\\messages\\net\\sz\\game\\protomessage",
                    userPath + "\\src\\main\\handlers\\",
                    "net\\sz\\game\\protohandler", isReq);
            handler.execute();
        } catch (Exception e) {
            log.error("", e);
        }
    }

}
