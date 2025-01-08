package com.client;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.io.*;
import java.util.*;

public class XmlHandler {
    // 读取 XML 文件并返回用户列表
    public static List<User> readXml(String filePath) throws Exception {
        List<User> users = new ArrayList<>();
        File xmlFile = new File(filePath);

        // 创建文档构建器
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // 解析 XML 文件
        Document document = builder.parse(xmlFile);
        NodeList nodeList = document.getElementsByTagName("user");

        // 遍历每个 user 节点，提取用户名和密码
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String username = element.getElementsByTagName("username").item(0).getTextContent();
                String password = element.getElementsByTagName("password").item(0).getTextContent();
                users.add(new User(username, password));
            }
        }

        return users;
    }

    // 将用户列表写入 XML 文件
    public static void writeXml(String filePath, List<User> users) throws Exception {
        File xmlFile = new File(filePath);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document;
        Element root;

        if (xmlFile.exists() && xmlFile.length() > 0) {
            // 如果文件存在且文件大小大于 0，解析文件并获取根元素
            document = builder.parse(xmlFile);
            root = document.getDocumentElement();
        } else {
            // 如果文件不存在或者文件为空，创建新的文档和根元素
            document = builder.newDocument();
            root = document.createElement("data");
            document.appendChild(root);
        }

        // 遍历用户列表并创建对应的 XML 元素
        for (User user : users) {
            Element userElement = document.createElement("user");

            Element usernameElement = document.createElement("username");
            usernameElement.appendChild(document.createTextNode(user.getUsername()));
            userElement.appendChild(usernameElement);

            Element passwordElement = document.createElement("password");
            passwordElement.appendChild(document.createTextNode(user.getPassword()));
            userElement.appendChild(passwordElement);

            root.appendChild(userElement);
        }

        // 写入 XML 文件
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

        // 转换文档并写入文件
        StreamResult result = new StreamResult(new FileWriter(filePath, false));  // 使用 false 确保覆盖写入
        transformer.transform(new DOMSource(document), result);
    }

    // 判断用户是否存在
    public static boolean userExists(String filePath, String username, String password) throws Exception {
        List<User> users = readXml(filePath);
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return true; // 用户存在
            }
        }
        return false; // 用户不存在
    }
}
