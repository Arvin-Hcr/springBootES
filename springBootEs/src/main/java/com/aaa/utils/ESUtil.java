package com.aaa.utils;


import com.aaa.status.StatusEnum;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;


//用@Component标识，成为一个组件类
@Component
//工具类
public class ESUtil{
    //使用@Autowired注解把TransportClient对象注入进来
    @Autowired
    private TransportClient transportClient;

    /**
     * 因为该工具类中所有的方法都必须要是静态方法，静态方法只能使用静态变量，
     * 所有当使用@Autowired所注入的对象就不能在静态方法中使用
     * 所以通过@PostConstruct注解来解决以上的问题
     */
    private static TransportClient client;

    /**
     * 当spring容器初始化的时候会调取一个init方法
     * 然后把@Autowired所标识的对象赋值给静态对象
     * 这样就能实现一个静态对象的转换
     */
    @PostConstruct
    public void init(){
        client=this.transportClient;
    }

//定义Map对象，统一返回
   public static Map<String,Object> result=new HashMap<String, Object>();
    /**
     *创建ES的index索引
     * @return
     */
    public static Map<String,Object> createIndex(String index){
        //1.首先判断es中是否已经存在这个index，如果已经存在那么最终就不能创建，会报错
        //判断inIndexExies返回boolean类型看是true还是false,true说明存在，false不存在就可以继续使用
        //规定：如果为true说明不存在，如果为false说明存在
        if(!inIndexExies(index)){
            result.put("code", StatusEnum.EXIST.getCode());
            result.put("msg", StatusEnum.EXIST.getMsg());
        }
        //2.在ES中创建该index
        CreateIndexResponse createIndexResponse = client.admin().indices().prepareCreate(index).execute().actionGet();
        //3.再次判断index是否创建成功
        //createIndexResponse.isAcknowledged()返回值true：成功/false：失败
        if (createIndexResponse.isAcknowledged()){
            result.put("code", StatusEnum.OPRATION_SUCCESS.getCode());
            result.put("msg", StatusEnum.OPRATION_SUCCESS.getMsg());
        }else {
            result.put("code", StatusEnum.OPRATION_FAILED.getCode());
            result.put("msg", StatusEnum.OPRATION_FAILED.getMsg());
        }
        return result;
    }

    /**
     * 删除index
     * @param index
     * @return
     */
        public static Map<String,Object> deleteIndex(String index){
            if(!inIndexExies(index)){
                result.put("code", StatusEnum.EXIST.getCode());
                result.put("msg", StatusEnum.EXIST.getMsg());
            }
            DeleteIndexResponse deleteIndexResponse = client.admin().indices().prepareDelete(index).execute().actionGet();
            if (deleteIndexResponse.isAcknowledged()){
                result.put("code", StatusEnum.OPRATION_SUCCESS.getCode());
                result.put("msg", StatusEnum.OPRATION_SUCCESS.getMsg());
            }else {
                result.put("code", StatusEnum.OPRATION_FAILED.getCode());
                result.put("msg", StatusEnum.OPRATION_FAILED.getMsg());
            }
            return result;
        }
    /**
     * 判断index是否存在
     * @param index
     * @return
     */
    public static boolean inIndexExies(String index){
        //exists所接收的是一个IndicesExistsRequest对象，最终是把index创建出来，拿到一个返回值
        IndicesExistsResponse indicesExistsResponse = client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet();
        return indicesExistsResponse.isExists();
    }

    /**
     * 判断指定index下的type是否存在
     * @param index
     * @param type
     * @return
     */
    public static boolean isTypeExist(String index,String type){
        return  inIndexExies(index) ? client.admin().indices().prepareTypesExists(index).setTypes(type).execute().actionGet().isExists():false;
    }

    /**
     * 添加数据
     * @param mapObj :所需要添加数据
     * @param index ：数据所在的index
     * @param type ：数据所在的type
     * @param id :数据的id
     * @return
     */
    public static Map<String,Object> addData(Map<String,Object> mapObj,String index,String type,String id){
        IndexResponse indexResponse = client.prepareIndex(index, type, id).setSource(mapObj).get();
        String response = indexResponse.status().toString();
        /**
         *做封装判断
         * indexResponse.status().toString()：如果添加成功就是OK
         * 因为所使用的是equals,所以OK和ok是返回的false
         *toUpperCase()在这里就是吧返回的OK强转成大写OK；
         */

        if ("OK".equals(response.toUpperCase())){
        //说明添加成功
            result.put("code", StatusEnum.OPRATION_SUCCESS.getCode());
            result.put("msg", StatusEnum.OPRATION_SUCCESS.getMsg());
        }else {
            result.put("code", StatusEnum.OPRATION_FAILED.getCode());
            result.put("msg", StatusEnum.OPRATION_FAILED.getMsg());
        }
        return result;
    }

    /**
     * 也是添加数据（不过在这里不指定id,使用uuid进行添加）
     * @param mapObj
     * @param index
     * @param type
     * @return
     */
    public static Map<String,Object> addData(Map<String,Object> mapObj,String index,String type){
        //比如：UUID:312312-5433fds-314cs--转换为>3123125433fds314cs（他把中间的”-“去掉）--》在把小写字母转换为大写，这样比较严谨
        return addData(mapObj, index, type, UUID.randomUUID().toString().replaceAll(".","").toUpperCase());
    }

    /**
     * 通过id删除数据
     * @param index
     * @param type
     * @param id
     * @return
     */
    public static Map<String,Object> deleteById(String index,String type,String id){
        DeleteResponse deleteResponse = client.prepareDelete(index, type, id).execute().actionGet();
        if ("OK".equals(deleteResponse.status().toString().toUpperCase())){
            result.put("code", StatusEnum.OPRATION_SUCCESS.getCode());
            result.put("msg", StatusEnum.OPRATION_SUCCESS.getMsg());
        }else {
            result.put("code", StatusEnum.OPRATION_FAILED.getCode());
            result.put("msg", StatusEnum.OPRATION_FAILED.getMsg());
        }
        return result;
    }

    /**
     * 通过id修改数据
     * @param mapObj ：所要修改的数据
     * @param index
     * @param type
     * @param id
     * @return
     */
    public static Map<String,Object>updateById(Map<String,Object> mapObj,String index,String type,String id){
        UpdateRequest updateRequest=new UpdateRequest();
        updateRequest.index(index).type(type).id(id).doc(mapObj);
        ActionFuture<UpdateResponse> update = client.update(updateRequest);
        if("OK".equals(update.actionGet().status().toString().toUpperCase())){
            result.put("code", StatusEnum.OPRATION_SUCCESS.getCode());
            result.put("msg", StatusEnum.OPRATION_SUCCESS.getMsg());
        }else {
            result.put("code", StatusEnum.OPRATION_FAILED.getCode());
            result.put("msg", StatusEnum.OPRATION_FAILED.getMsg());
        }
        return result;
    }

    /**
     * 通过id查询数据
     * @param index
     * @param type
     * @param id
     * @param fileId ：所需要显示的字段，如果有多个字段需要显示则使用逗号","隔开，如果所有字段都需要显示，直接设置为null
     * @return
     */
    public static Map<String,Object> selectById(String index,String type,String id,String fileId){
        GetRequestBuilder getRequestBuilder = client.prepareGet(index, type, id);
        /**
         * 在这里有两种判断方式
         * 1. StringUtil.isNullOrEmpty(fileId) 这里返回的也是boolean类型，方法内部有判断,返回的是空
         * 2. null !=fileId && !"".equals(fileId)
         */

        if (null !=fileId && !"".equals(fileId)){
            //excludes所需要排除的字段，直接设置为null就行
            getRequestBuilder.setFetchSource(fileId.split(","),null);
        }
        //提交
        GetResponse documentFields = getRequestBuilder.execute().actionGet();
        //这里默认返回的map对象
        return documentFields.getSource();
    }

    /**
     *分词全文检索
     * @param index
     * @param type
     * @param fileId：所需要显示的字段，如果有多个字段需要显示则使用逗号","隔开，如果所有字段都需要显示，直接设置为null
     * @param sourField：所要被排序的字段
     * @param highlightField：所要被高亮显示的字段
     * @param query：最终使用这个对象查询数据
     * @param size：显示多少条
     * @return
     */
    public static List<Map<String,Object>> selectAllData(String index, String type, String fileId, String sourField, String highlightField, QueryBuilder query, Integer size){
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index);//准备搜索
        //判断Type不为空处理
        if (null !=type && !"".equals(type)){
            //这里的setTypes()是个可变参
                searchRequestBuilder.setTypes(type.split(","));
        }
        //判断高亮显示字段不为空处理
        if (null !=highlightField && !"".equals(highlightField)){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            //设置需要高亮显示的字段
            highlightBuilder.field(highlightField);
            searchRequestBuilder.highlighter(highlightBuilder);
        }
        //查询数据
        searchRequestBuilder.setQuery(query);
        //判断fileId需要显示的字段不为空处理
        if (null !=fileId && !"".equals(fileId)){
            searchRequestBuilder.setFetchSource(fileId.split(","),null);
        }
        //判断sourField(排序)字段不为空处理
        if (null !=sourField && !"".equals(sourField)){
            //这里排序默认使用 SortOrder.DESC 倒序
            searchRequestBuilder.addSort(sourField, SortOrder.DESC);
        }
        //判断size所显示的条数不为空处理
        if (null !=size && 0 < size){
                searchRequestBuilder.setSize(size);
        }
        //提交
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
        /**
         * 在这里有两种判断方式
         * 1. if ("OK".equals(searchResponse.status().toString().toUpperCase())){}这里返回的也是boolean类型，方法内部有判断,返回的是空
         * 2. if (searchResponse.status().getStatus() == 200){} 返回的是int类型，如果成功就会返回200
         */
        if (searchResponse.status().getStatus() == 200){
            //如果他是200需要解析对象（需要解析高亮显示的结果）
            //searchResponse查询返回的结果集
            return setSelect(searchResponse,highlightField);
        }
        return null;
    }

    /**
     * 处理高亮显示
     * @param searchResponse
     * @param highlightField
     * @return
     */
    private static List<Map<String,Object>> setSelect(SearchResponse searchResponse,String highlightField){
        List<Map<String,Object>> sourceList=new ArrayList<Map<String,Object>>();
        //线程安全
        StringBuffer stringBuffer=new StringBuffer();
        //遍历searchResponse结果集
        for (SearchHit searchHit :searchResponse.getHits().getHits()){
            searchHit.getSourceAsMap().put("id",searchHit.getId());
            if (null !=highlightField && !"".equals(highlightField)){
                //返回的是一个Text[]数组
                Text[] text = searchHit.getHighlightFields().get(highlightField).getFragments();
                //判断拿到的对象是否为空
                if(text!=null){
                    for (Text str : text) {
                        stringBuffer.append(str.toString());
                    }
                    //遍历高亮显示的结果集
                    searchHit.getSourceAsMap().put(highlightField,stringBuffer.toString());
                }
            }
            sourceList.add(searchHit.getSourceAsMap());

        }
        return sourceList;
    }
}
