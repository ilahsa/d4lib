package net.d4.d4lib.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @goal genhandler
 * @phase compile
 * @requiresProject true
 */
public class GenerateHandler {

    private static final Logger log = LoggerFactory.getLogger(GenerateHandler.class);
    private static final String[] DEFAULT_INCLUDES = new String[]{"java"};
    //private String readPath;
    private String savePath;
    private Boolean isReq = true;
    /**
     * 项目根目录
     *
     * @parameter expression="${project.basedir}"
     * @required
     * @readonly
     */
    private File basedir;

    private File outFile;
    /**
     * 额外参数，由于没有配置expression，所以只能过通过pom.xml plugin->configuration配置获得
     *
     * @parameter
     */
    private String[] includes;

    public GenerateHandler(String readPath, String basedirPath, String savePath) {
        this.savePath = savePath;
        this.basedir = new File(readPath);
        this.outFile = new File(basedirPath + savePath);
    }

    public GenerateHandler(String readPath, String basedirPath, String savePath, Boolean isReq) {
        this.savePath = savePath;
        this.basedir = new File(readPath);
        this.outFile = new File(basedirPath + savePath);
        this.isReq = isReq;
    }

    public void execute() {
        if (includes == null) {
            includes = DEFAULT_INCLUDES;
        }

        List<File> rfFiles = new ArrayList<>();
        getRfFiles(rfFiles, basedir);
        for (File file : rfFiles) {
            try {
                log.info(getFilecounts(file).toString());
            } catch (FileNotFoundException ex) {
                log.error("找不到文件1", ex);
            } catch (IOException ex) {
                log.error("找不到文件2", ex);
            }
        }
    }

    private void getRfFiles(List<File> files, File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            for (File f : listFiles) {
                getRfFiles(files, f);
            }
        } else {
            for (String include : includes) {
                if (file.getName().endsWith(include)) {
                    files.add(file);
                    break;
                }
            }

        }
    }

    private static final String packagePatten = "package ";
    private static final String messagePatten = "(.*)public static final class [Req|GL|LG](.*)Message extends(.*)";

    private FileCountsInfo getFilecounts(File file) throws FileNotFoundException, IOException {
        String packageName = "";
        List<String> classNames = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        int count = 0;
        try {
            while (br.ready()) {
                String readLine = br.readLine();
                if (packageName.equals("") && readLine.startsWith(packagePatten)) {
                    packageName = readLine.replace(packagePatten, "").replace(";", "");// + ".handler";
                    log.info("packageName:: " + packageName);
                } else if (readLine.matches(messagePatten)) {
                    int indexOf0 = readLine.indexOf("class ") + 6;
                    int indexOf1 = readLine.indexOf(" extends");
                    String className = readLine.substring(indexOf0, indexOf1);
                    if (isReq == null || (!className.startsWith("Res") && !className.startsWith("Req"))) {
                        classNames.add(className);
                    } else if (isReq && className.startsWith("Req")) {
                        classNames.add(className);
                    } else if (!isReq && className.startsWith("Res")) {
                        classNames.add(className);
                    }
                }
                count++;
            }
        } catch (IOException e) {
            log.error("getFilecounts", e);
        } finally {
            br.close();
        }

        if (!classNames.isEmpty()) {
            String protoName = file.getName().replace(".java", "");
            String module = protoName;
            module = module.replace("Message", "");
            module = module.toLowerCase();

            for (String className : classNames) {
                String fileName = className.replace("Message", "Handler.java"); // UserVersionMessageHandler.java
                // String filePath = sourceDirectory + "\\" + packageName.replace(".", "\\") + "\\handler\\" + module+ "\\"+ fileName; // E:\game\game\game-plugin\src\main\java\com\game\proto\handler\UserVersionMessageHandler.java
                String filePath = outFile + "\\" + module + "\\" + fileName;
                log.info("fileName " + fileName);
                log.info("filePath " + filePath);
                File newFile = new File(filePath);
                if (!newFile.exists()) {
                    createFile(newFile);
                    log.info("创建成功");

                    {
                        // 写入类容
                        // String packageName ;//"com.game.loginsr.proto"
                        // String protoName ;//"LoginMessage"
                        String reqClassName = className.replace("Message", "");//"ReqTokenLoginMessage"
                        String resClassName = null;//"ResTokenLoginMessage"
                        if (className.startsWith("Req")) {
                            resClassName = className;
                            resClassName = resClassName.replaceFirst("Req", "Res");
                        }
                        log.info("================");
                        log.info(newFile.toString());
                        log.info(packageName); // com.game.loginsr.proto
                        log.info(protoName); // LoginMessage
                        log.info(reqClassName); // ReqCreateSelectUserCmd
                        log.info(resClassName); // ResCreateSelectUserCmd
                        log.info("================");
                        genCodeTemplate(newFile, packageName, protoName, reqClassName, resClassName, module);
                    }
                } else {
                    log.info("已经存在");
                }
            }
        }
        return new FileCountsInfo(file, count);
    }

    public static void makeDir(File dir) {
        if (!dir.getParentFile().exists()) {
            makeDir(dir.getParentFile());
        }
        dir.mkdir();
    }

    public static boolean createFile(File file) throws IOException {
        if (!file.exists()) {
            makeDir(file.getParentFile());
        }
        return file.createNewFile();
    }

    /**
     *
     * @param packageName "com.game.loginsr.proto"
     * @param protoName "LoginMessage"
     * @param reqClassName "ReqTokenLogin"
     * @param resClassName "ResTokenLogin"
     * @param module "login"
     */
    private void genCodeTemplate(File file, String packageName, String protoName, String reqClassName, String resClassName, String module) {
        String importProto = packageName + "." + protoName;
        String reqMessageName = protoName + "." + reqClassName;
        String resMessageName = protoName + "." + resClassName;

        StringBuilder code = new StringBuilder();
        String packagename = savePath.replaceAll("[\\\\|/]", ".") + "." + module;
        code.append("package ").append(packagename).append(";\n\n");
        code.append("import net.sz.engine.io.nettys.NettyTcpHandler;\n");
        code.append("import ").append(importProto).append(";\n");
        code.append("import org.apache.log4j.Logger;\n");
        code.append("/**\n");
        code.append(" *\n");
        code.append(" * @author 失足程序员\n");
        code.append(" * @mail 492794628@qq.com\n");
        code.append(" * @phone 13882122019\n");
        code.append(" */\n");
        code.append("public final class ").append(reqClassName).append("Handler extends NettyTcpHandler {\n\n");
        code.append("    private static final Logger log = Logger.getLogger(").append(reqClassName).append("Handler.class);\n");
        code.append("    \n");
        code.append("    \n");
        code.append("    public ").append(reqClassName).append("Handler() {\n\n");
        code.append("       net.sz.engine.io.nettys.NettyPool.getInstance().registerHandlerMessage(\n");
        code.append("           0, //消息执行线程\n");
        code.append("           ").append(importProto).append(".xxx").append(", //消息消息id\n");
        code.append("           ").append(packagename).append(".").append(reqClassName).append("Handler.class, //消息执行的handler\n");
        code.append("           ").append(importProto).append(".").append(reqClassName).append("Message.newBuilder());//消息体\n");
        code.append("    }\n");
        code.append("    \n");
        code.append("    @Override\n");
        code.append("    public void run() {\n");
        code.append("        // TODO 处理").append(reqMessageName).append("消息\n");
        code.append("        ").append(reqMessageName).append("Message reqMessage = (").append(reqMessageName).append("Message) getMessage();\n");
        if (resClassName != null) {
            code.append("        ").append(resMessageName).append(".Builder builder4Res = ").append(resMessageName).append(".newBuilder();\n");
        }
        code.append("    }\n");
        code.append("}\n");

        if (file.canWrite()) {
            try {
                OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8");
                osw.write(code.toString());
                osw.flush();
            } catch (IOException ex) {
                log.error("写入代码模版到" + file.getName() + "失败!", ex);
            }
        }
    }
}

class FileCountsInfo {

    private File file;
    private int count;

    public FileCountsInfo(File file, int count) {
        this.file = file;
        this.count = count;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return file.toString() + " count: " + count;
    }
}
