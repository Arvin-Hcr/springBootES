package com.aaa.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *Es的配置
 *@Configuration注解作用就是把该类标识为配置类,这个类就行当于之前的spring的xml
 *已经被application.xml导入<import />
 *  TransportClient:
 *  最终就是通过TransportClient对象对Es实现增删改查
 *  java对Es的增删改查的api就在这个TransportClient对象中体现
 *
 *  client.transport.sniff
 *  当ES集群中有新的节点加入，项目会自动发现这个节点，不需要在进行手动添加
 *  thread.pool.search.size
 *  Es的线程池
 */
@SpringBootApplication
public class EsConfig {
    //把EsProperties类导入进来
    @Autowired
    private EsPropertied esPropertied;

    @Bean("transportClient")
    public TransportClient getTransportClient(){
        //1.创建TransportClient对象，不用初始化
        TransportClient transportClient=null;
        try {
            //2.配置ES的集群信息
            Settings settings = Settings.builder()
                    .put("cluster.name", esPropertied.getClusterName())
                    .put("node.name", esPropertied.getNodeName())
                    .put("client.transport.sniff", true)
                    .put("thread_pool.search.size", esPropertied.getPool()).build();
            //3.对TransportClient对象初始化
            transportClient=new PreBuiltTransportClient(settings);
            //4.配置Es的连接信息(需要知道Es的IP地址和端口号)
            TransportAddress transportAddress=new TransportAddress(InetAddress.getByName(esPropertied.getIp()),Integer.parseInt(esPropertied.getPort()));
            //5.把ES的连接信息放入TransportClient对象中
            transportClient.addTransportAddress(transportAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        //6.返回TransportClient对象
        return transportClient;
    }
}
