package com.swift;


import com.swift.compress.Compressor;
import com.swift.compress.impl.GzipCompressor;
import com.swift.discovery.RegisterConfig;
import com.swift.loadbalancer.LoadBalancer;
import com.swift.loadbalancer.impl.RoundRobinLoadBalancer;
import com.swift.serialize.Serializer;
import com.swift.serialize.impl.JdkSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * 全局的配置类，代码配置-->xml配置-->默认项
 *
 * @author it楠老师
 * @createTime 2023-07-11
 */
@Data
@Slf4j
public class Configuration {

    // 配置信息-->id发射器
    public IdGenerator idGenerator = new IdGenerator(1, 2);
    // 配置信息-->端口号
    private int port = 8094;
    // 配置信息-->应用程序的名字
    private String appName = "default";
    // 配置信息-->注册中心
    private RegisterConfig registryConfig = new RegisterConfig("zookeeper://127.0.0.1:2181");
    // 配置信息-->序列化协议
    private ProtocolConfig protocolConfig = new ProtocolConfig("jdk");
    // 配置信息-->序列化协议
    private String serializeType = "jdk";
    private Serializer serializer = new JdkSerializer();
    // 配置信息-->压缩使用的协议
    private String compressType = "gzip";
    private Compressor compressor = new GzipCompressor();
    // 配置信息-->负载均衡策略
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

    // 读xml，dom4j
    public Configuration() {
        // 读取xml获得上边的信息
        loadFromXml(this);

    }

    /**
     * 从配置文件读取配置信息,我们不使用dom4j，使用原生的api
     *
     * @param configuration 配置实例
     */
    private void loadFromXml(Configuration configuration) {
        try {
            // 1、创建一个document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // 禁用DTD校验：可以通过调用setValidating(false)方法来禁用DTD校验。
            factory.setValidating(false);
            // 禁用外部实体解析：可以通过调用setFeature(String name, boolean value)方法并将“http://apache.org/xml/features/nonvalidating/load-external-dtd”设置为“false”来禁用外部实体解析。
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("rpc.xml");
            Document doc = builder.parse(inputStream);

            // 2、获取一个xpath解析器
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            // 3、解析所有的标签
            configuration.setPort(resolvePort(doc, xpath));
            configuration.setAppName(resolveAppName(doc, xpath));

            configuration.setIdGenerator(resolveIdGenerator(doc, xpath));

            configuration.setRegistryConfig(resolveRegistryConfig(doc, xpath));

            configuration.setCompressType(resolveCompressType(doc, xpath));
            configuration.setCompressor(resolveCompressCompressor(doc, xpath));

            configuration.setSerializeType(resolveSerializeType(doc, xpath));
            configuration.setProtocolConfig(new ProtocolConfig(this.serializeType));

            configuration.setSerializer(resolveSerializer(doc, xpath));

            configuration.setLoadBalancer(resolveLoadBalancer(doc, xpath));

            // 如果有新增的标签，这里继续修改

        } catch (SAXException | IOException | ParserConfigurationException e) {
            log.info("If no configuration file is found or an exception occurs when parsing the configuration file, " +
                    "the default configuration is used.", e);
        }
    }


    /**
     * 解析端口号
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return 端口号
     */
    private int resolvePort(Document doc, XPath xpath) {
        String expression = "/configuration/port";
        String portString = parseString(doc, xpath, expression);
        return Integer.parseInt(portString);
    }

    /**
     * 解析应用名称
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return 应用名
     */
    private String resolveAppName(Document doc, XPath xpath) {
        String expression = "/configuration/appName";
        return parseString(doc, xpath, expression);
    }

    /**
     * 解析负载均衡器
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return 负载均衡器实例
     */
    private LoadBalancer resolveLoadBalancer(Document doc, XPath xpath) {
        String expression = "/configuration/loadBalancer";
        return parseObject(doc, xpath, expression, null);
    }

    /**
     * 解析id发号器
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return id发号器实例
     */
    private IdGenerator resolveIdGenerator(Document doc, XPath xpath) {
        String expression = "/configuration/idGenerator";
        String aClass = parseString(doc, xpath, expression, "class");
        String dataCenterId = parseString(doc, xpath, expression, "dataCenterId");
        String machineId = parseString(doc, xpath, expression, "MachineId");

        try {
            Class<?> clazz = Class.forName(aClass);
            Object instance = clazz.getConstructor(new Class[]{long.class, long.class})
                    .newInstance(Long.parseLong(dataCenterId), Long.parseLong(machineId));
            return (IdGenerator) instance;
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析注册中心
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return RegistryConfig
     */
    private RegisterConfig resolveRegistryConfig(Document doc, XPath xpath) {
        String expression = "/configuration/registry";
        String url = parseString(doc, xpath, expression, "url");
        return new RegisterConfig(url);
    }


    /**
     * 解析压缩的具体实现
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return Compressor
     */
    private Compressor resolveCompressCompressor(Document doc, XPath xpath) {
        String expression = "/configuration/compressor";
        return parseObject(doc, xpath, expression, null);
    }

    /**
     * 解析压缩的算法名称
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return 压缩算法名称
     */
    private String resolveCompressType(Document doc, XPath xpath) {
        String expression = "/configuration/compressType";
        return parseString(doc, xpath, expression, "type");
    }

    /**
     * 解析序列化的方式
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return 序列化的方式
     */
    private String resolveSerializeType(Document doc, XPath xpath) {
        String expression = "/configuration/serializeType";
        return parseString(doc, xpath, expression, "type");
    }

    /**
     * 解析序列化器
     *
     * @param doc   文档
     * @param xpath xpath解析器
     * @return 序列化器
     */
    private Serializer resolveSerializer(Document doc, XPath xpath) {
        String expression = "/configuration/serializer";
        return parseObject(doc, xpath, expression, null);
    }


    /**
     * 获得一个节点文本值   <port>7777</>
     *
     * @param doc        文档对象
     * @param xpath      xpath解析器
     * @param expression xpath表达式
     * @return 节点的值
     */
    private String parseString(Document doc, XPath xpath, String expression) {
        try {
            XPathExpression expr = xpath.compile(expression);
            // 我们的表达式可以帮我们获取节点
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return targetNode.getTextContent();
        } catch (XPathExpressionException e) {
            log.error("An exception occurred while parsing the expression.", e);
        }
        return null;
    }


    /**
     * 获得一个节点属性的值   <port num="7777"></>
     *
     * @param doc           文档对象
     * @param xpath         xpath解析器
     * @param expression    xpath表达式
     * @param AttributeName 节点名称
     * @return 节点的值
     */
    private String parseString(Document doc, XPath xpath, String expression, String AttributeName) {
        try {
            XPathExpression expr = xpath.compile(expression);
            // 我们的表达式可以帮我们获取节点
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return targetNode.getAttributes().getNamedItem(AttributeName).getNodeValue();
        } catch (XPathExpressionException e) {
            log.error("An exception occurred while parsing the expression.", e);
        }
        return null;
    }

    /**
     * 解析一个节点，返回一个实例
     *
     * @param doc        文档对象
     * @param xpath      xpath解析器
     * @param expression xpath表达式
     * @param paramType  参数列表
     * @param param      参数
     * @param <T>        泛型
     * @return 配置的实例
     */
    private <T> T parseObject(Document doc, XPath xpath, String expression, Class<?>[] paramType, Object... param) {
        try {
            XPathExpression expr = xpath.compile(expression);
            // 我们的表达式可以帮我们获取节点
            Node targetNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            String className = targetNode.getAttributes().getNamedItem("class").getNodeValue();
            Class<?> aClass = Class.forName(className);
            Object instant = null;
            if (paramType == null) {
                instant = aClass.getConstructor().newInstance();
            } else {
                instant = aClass.getConstructor(paramType).newInstance(param);
            }
            return (T) instant;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException | XPathExpressionException e) {
            log.error("An exception occurred while parsing the expression.", e);
        }
        return null;
    }
}
