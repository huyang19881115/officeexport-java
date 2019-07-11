package com.core.word;


import com.core.utils.FileUtils;
import com.core.utils.StringUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * @Auther: SunBC
 * @Date: 2019/6/18 11:09
 * @Description:
 */
public class WordXmlModelHandlerImpl implements XmlModelHandler{
    @Override
    public  void VerifyModel(String xmlPath)throws DocumentException {
        String  errorInfo = null;
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(xmlPath));
        Element rootElement = document.getRootElement();
        //校验段落
        List ParagList = document.selectNodes(".//w:p");
        StringBuilder wpStr = new StringBuilder();
        for (int i = 0; i < ParagList.size(); i++) {
            Node node = (Node)ParagList.get(i);
            List TextNodeList = node.selectNodes(".//w:t");
            for (int j = 0; j < TextNodeList.size(); j++) {
                Node TextNode = (Node)TextNodeList.get(j);
                String text = TextNode.getText();
                wpStr.append(StringUtil.removeInvisibleChar(text));
            }
        }
        errorInfo= XmlParserUtils.VarifySyntax(wpStr.toString());
        if (errorInfo != null && errorInfo.length() != 0) throw new SyntaxException(errorInfo);
        return ;
    }

    @Override
    public String ConverToFreemaker(String xmlPath,String ftlOutputPath)throws DocumentException,IOException {
        XMLWriter writer = null;
        try {
            SAXReader reader = new SAXReader();
            File file = new File(xmlPath);
            Document document = reader.read(file);
            List list = document.selectNodes("//w:p");
            for (int i = 0; i <list.size() ; i++) {
                Node WPNode = (Node)list.get(i);
                XmlParserUtils.PlaceHodlerHandle(WPNode);
            }
            //转换[ 到list标签
            XmlParserUtils.BracketToListConversion(document);
            if (ftlOutputPath == null) ftlOutputPath = xmlPath.replace(".xml", ".ftl");
            String name = file.getName();
            ftlOutputPath = ftlOutputPath + name +".ftl";
            FileWriter fileWiter = new FileWriter(ftlOutputPath);
            writer = new XMLWriter(fileWiter);
            writer.write( document );
            writer.flush();
            return ftlOutputPath;
        }finally {
            if (writer != null )
                writer.close();
        }
    }

    @Override
    public void XmlPlaceHolderHandler(String xmlFtlPath) throws IOException{
        XMLWriter writer = null;
        FileOutputStream out = null;
        try {
            String xmModelStr = FileUtils.readToStringByFilepath(xmlFtlPath);
            xmModelStr = XmlParserUtils.IfTagHandle(xmModelStr);
            xmModelStr = XmlParserUtils.ListTagHandle(xmModelStr);
            xmModelStr = XmlParserUtils.BraceTagHandle(xmModelStr);
            out = new FileOutputStream(xmlFtlPath);
            out.write(xmModelStr.getBytes());
            out.flush();
        }finally {
            if(out != null){
                out.close();
            }
        }
    }



    public String WordXmlModelHandle(String xmlPath,String ActualModelPath) throws IOException,DocumentException{
        VerifyModel(xmlPath);
        String xmlFtlpath = ConverToFreemaker(xmlPath,ActualModelPath);
        XmlPlaceHolderHandler(xmlFtlpath);
        return xmlFtlpath;
    }
}
