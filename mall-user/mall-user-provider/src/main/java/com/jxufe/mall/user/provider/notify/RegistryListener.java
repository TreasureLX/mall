package com.jxufe.mall.user.provider.notify;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Service;

/**
 * 腾讯课堂搜索 咕泡学院
 * 加群获取视频：608583947
 * 风骚的Michael 老师
 */
@Service
public class RegistryListener implements MessageListener<Integer,String> {


    @Override
    public void onMessage(ConsumerRecord<Integer, String> integerStringConsumerRecord) {
        System.out.println("收到消息了");
        System.out.println(integerStringConsumerRecord.value());
    }
}
