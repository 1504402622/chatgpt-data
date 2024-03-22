package cn.glfs.chatgpt.data.types.sdk.weixin;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.SAXParser;
import java.io.InputStream;
import java.io.Writer;
import java.util.*;

/**
 * xml工具类
 */
public class XmlUtil {

    /**
     * 解析微信发来的请求（xml）转化为map
     */
    public static Map<String, String> xmlToMap(HttpServletRequest request) throws Exception {
        //从request中取得输入流
        try (InputStream inputStream = request.getInputStream()) {
            // 将解析结果存储在hashmap中
            Map<String, String> map = new HashMap<>();
            // 读取输入流
            SAXReader reader = new SAXReader();
            // 得到xml文档
            Document document = reader.read(inputStream);
            // 得到xml根元素
            Element rootElement = document.getRootElement();
            // 得到根元素的所有子结点
            List<Element> element = rootElement.elements();
            // 遍历所有子结点
            for (Element e : element) map.put(e.getName(), e.getText());
            // 释放元素
            inputStream.close();
            ;
            return map;
        }
    }

    /**
     * 将map转化成xml响应给微信服务器（调用）
     */
    static String mapToXML(Map map) {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        mapToXML2(map, sb);
        sb.append("</xml>");
        try {
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将 Map 对象转换成 XML 格式(方法体)
     * 如果值的类型是 ArrayList，则将其逐个转换为 XML 字符串并拼接到结果中；
     * 如果值的类型是 HashMap，则递归调用 mapToXML2 方法将其转换为 XML 字符串并拼接到结果中；
     * 如果值的类型是其他类型，则直接将其转换为 XML 字符串并拼接到结果中
     *
     * @param map
     * @param sb
     */
    private static void mapToXML2(Map map, StringBuffer sb) {
        Set set = map.keySet();
        for (Object o : set) {
            String key = (String) o;
            Object value = map.get(key);
            if (null == value) value = "";
            if (value.getClass().getName().equals("java.util.ArrayList")) {
                ArrayList list = (ArrayList) map.get(key);
                sb.append("<").append(key).append(">");
                for (Object o1 : list) {
                    HashMap hm = (HashMap) o1;
                    mapToXML2(hm, sb);
                }
                sb.append("</").append(key).append(">");

            } else {
                if (value instanceof HashMap) {
                    sb.append("<").append(key).append(">");
                    mapToXML2((HashMap) value, sb);
                    sb.append("</").append(key).append(">");
                } else {
                    sb.append("<").append(key).append("><![CDATA[").append(value).append("]]></").append(key).append(">");
                }
            }
        }
    }

    //xstream扩展,bean转xml自动加上![CDATA[]]
    //保留文本内容的原始格式：CDATA 标记可以确保文本内容（包括 HTML、XML 片段等）在保留原格式的情况下被包含在
    // XML 中，而不被解析为 XML 标记
    /*
     * 这个方法是创建一个XStream的对象传参为一个匿名继承XppDriver的对象，
     * 而其中重写了xppDriver父类AbstractXppDriver的createWriter方法
     * 返回一个自定义继承了PrettyPrintWriter的匿名类对象重写了writeText和startNode两个方法
     * 这两个方法自定义的写入器会在XStream序列化对象时被内部调用-》为out赋值-》为XStream赋值
     * */
    public static XStream getMyXStream() {
        return new XStream(new XppDriver() {
            @Override
            public HierarchicalStreamWriter createWriter(Writer out) {
                return new PrettyPrintWriter(out) {
                    // 对所有xml节点都增加CDATA标记
                    boolean cdata = true;

                    @Override
                    public void startNode(String name, Class clazz) {
                        super.startNode(name, clazz);
                    }

                    @Override
                    protected void writeText(QuickWriter writer, String text) {
                        // 非数字就加这个防止被xml解析
                        if (cdata && !StringUtils.isNumeric(text)) {
                            writer.write("<![CDATA[");
                            writer.write(text);
                            writer.write("]]>");
                        } else {
                            writer.write(text);
                        }
                    }
                };
            }
        });
    }

    /**
     * bean转成微信的xml消息格式
     */
    public static String beanToXml(Object object) {
        XStream xStream = getMyXStream();
        xStream.alias("xml", object.getClass());
        xStream.processAnnotations(object.getClass());
        String xml = xStream.toXML(object);
        if (!StringUtils.isEmpty(xml)) {
            return xml;
        } else {
            return null;
        }
    }

    /**
     * 将XML字符串转换为bean
     *
     * @param resultXml XML字符串
     * @param clazz     要转换的对象类型
     * @return 转换后的对象
     */
    public static <T> T xmlToBean(String resultXml, Class clazz) {
        // 创建XStream对象并设置默认安全防护，同时设置允许的类
        XStream stream = new XStream(new DomDriver());
        XStream.setupDefaultSecurity(stream);
        stream.allowTypes(new Class[]{clazz}); // 设置允许的类
        stream.processAnnotations(new Class[]{clazz}); // 处理注解
        stream.setMode(XStream.NO_REFERENCES); // 设置模式为不处理引用
        stream.alias("xml", clazz); // 给根节点取别名为"xml"
        // 从XML字符串中解析并返回转换后的对象
        return (T) stream.fromXML(resultXml);
    }
}
