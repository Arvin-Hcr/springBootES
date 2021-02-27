package com.aaa.controller;

import com.aaa.utils.ESUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class SearchController {
    /**
     * 创建索引（index）
     * @return
     */
    @RequestMapping("/create")
    public Map<String,Object> createIndex(){
        return ESUtil.createIndex("test_index20");
    }

    /**
     * 添加一条数据
     * @return
     */
    @RequestMapping("/add")
    public Map<String,Object> addDate(){
        Map<String,Object> mapObj=new HashMap<String,Object>();
        mapObj.put("username","shunliu");
        mapObj.put("passwd",123456);
        mapObj.put("age",40);
        return ESUtil.addData(mapObj,"hcr","test","7");
    }
}
