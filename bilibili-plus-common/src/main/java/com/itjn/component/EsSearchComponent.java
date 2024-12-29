package com.itjn.component;

import com.itjn.entity.config.AppConfig;
import com.itjn.entity.dto.VideoInfoEsDto;
import com.itjn.entity.enums.PageSize;
import com.itjn.entity.enums.SearchOrderTypeEnum;
import com.itjn.entity.po.UserInfo;
import com.itjn.entity.po.VideoInfo;
import com.itjn.entity.query.SimplePage;
import com.itjn.entity.query.UserInfoQuery;
import com.itjn.entity.vo.PaginationResultVO;
import com.itjn.exception.BusinessException;
import com.itjn.mappers.UserInfoMapper;
import com.itjn.utils.CopyTools;
import com.itjn.utils.JsonUtils;
import com.itjn.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("esSearchUtils")
@Slf4j
public class EsSearchComponent {

    @Resource
    private AppConfig appConfig;

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Resource
    private UserInfoMapper userInfoMapper;

    private Boolean isExistIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(appConfig.getEsIndexVideoName());
        return restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
    }

    /**
     * 如果索引库easylive_video不存在则初始化索引库
     */
    public void createIndex() {
        try {
            Boolean existIndex = isExistIndex();
            if (existIndex) {
                return;
            }
            CreateIndexRequest request = new CreateIndexRequest(appConfig.getEsIndexVideoName());
            //自定义的分词器：根据逗号分割    (这个自定义分词器用于索引库的tags字段)
            request.settings(
                    "{\"analysis\": {\n" +
                            "      \"analyzer\": {\n" +
                            "        \"comma\": {\n" +
                            "          \"type\": \"pattern\",\n" +
                            "          \"pattern\": \",\"\n" +
                            "        }\n" +
                            "      }\n" +
                            "    }}", XContentType.JSON);

            request.mapping(
                    "{\"properties\": {\n" +
                            "      \"videoId\":{\n" +
                            "        \"type\": \"text\",\n" +
                            "        \"index\": false\n" +
                            "      },\n" +
                            "      \"userId\":{\n" +
                            "        \"type\": \"text\",\n" +
                            "        \"index\": false\n" +
                            "      },\n" +
                            "      \"videoCover\":{\n" +
                            "        \"type\": \"text\",\n" +
                            "        \"index\": false\n" +
                            "      },\n" +
                            "      \"videoName\":{\n" +
                            "        \"type\": \"text\",\n" +
                            "        \"analyzer\": \"ik_max_word\"\n" +
                            "      },\n" +
                            "      \"tags\":{\n" +
                            "        \"type\": \"text\",\n" +
                            "        \"analyzer\": \"comma\"\n" +
                            "      },\n" +
                            "      \"playCount\":{\n" +
                            "        \"type\":\"integer\",\n" +
                            "        \"index\":false\n" +
                            "      },\n" +
                            "      \"danmuCount\":{\n" +
                            "        \"type\":\"integer\",\n" +
                            "        \"index\":false\n" +
                            "      },\n" +
                            "      \"collectCount\":{\n" +
                            "        \"type\":\"integer\",\n" +
                            "        \"index\":false\n" +
                            "      },\n" +
                            "      \"createTime\":{\n" +
                            "        \"type\":\"date\",\n" +
                            "        \"format\": \"yyyy-MM-dd HH:mm:ss\",\n" +
                            "        \"index\": false\n" +
                            "      }\n" +
                            " }}", XContentType.JSON);

            CreateIndexResponse createIndexResponse =
                    restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
            boolean acknowledged = createIndexResponse.isAcknowledged();
            if (!acknowledged) {
                throw new BusinessException("初始化es失败");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("初始化es失败", e);
            throw new BusinessException("初始化es失败");
        }
    }

    private Boolean docExist(String id) throws IOException {
        GetRequest getRequest = new GetRequest(appConfig.getEsIndexVideoName(), id);
        // 执行查询
        GetResponse response = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        return response.isExists();

    }

    /**
     * 往索引库easylive_video里添加一条文档数据
     * @param videoInfo
     */
    public void saveDoc(VideoInfo videoInfo) {
        try {
            if (docExist(videoInfo.getVideoId())) {
                updateDoc(videoInfo);
            } else {
                VideoInfoEsDto videoInfoEsDto = CopyTools.copy(videoInfo, VideoInfoEsDto.class);
                videoInfoEsDto.setCollectCount(0);
                videoInfoEsDto.setPlayCount(0);
                videoInfoEsDto.setDanmuCount(0);
                IndexRequest request = new IndexRequest(appConfig.getEsIndexVideoName());
                String json = JsonUtils.convertObj2Json(videoInfoEsDto);
                request.id(videoInfo.getVideoId()).source(json, XContentType.JSON);
                restHighLevelClient.index(request, RequestOptions.DEFAULT);
            }
        } catch (Exception e) {
            log.error("新增视频到es失败", e);
            throw new BusinessException("保存失败");
        }
    }


    private void updateDoc(VideoInfo videoInfo) {
        try {
            //时间不更新
            videoInfo.setLastUpdateTime(null);
            videoInfo.setCreateTime(null);
            Map<String, Object> dataMap = new HashMap<>();
            Field[] fields = videoInfo.getClass().getDeclaredFields();
            for (Field field : fields) {
                String methodName = "get" + StringTools.upperCaseFirstLetter(field.getName());
                Method method = videoInfo.getClass().getMethod(methodName);
                Object object = method.invoke(videoInfo);
                if (object != null && object instanceof String && !StringTools.isEmpty(object.toString()) || object != null && !(object instanceof String)) {
                    dataMap.put(field.getName(), object);
                }
            }
            if (dataMap.isEmpty()) {
                return;
            }
            UpdateRequest updateRequest = new UpdateRequest(appConfig.getEsIndexVideoName(), videoInfo.getVideoId());
            updateRequest.doc(dataMap);
            restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("新增视频到es失败", e);
            throw new BusinessException("保存失败");
        }
    }

    public void updateDocCount(String videoId, String fieldName, Integer count) {
        try {
            UpdateRequest updateRequest = new UpdateRequest(appConfig.getEsIndexVideoName(), videoId);
            Script script = new Script(ScriptType.INLINE, "painless", "ctx._source." + fieldName + " += params.count", Collections.singletonMap("count", count));
            updateRequest.script(script);
            restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("更新数量到es失败", e);
            throw new BusinessException("保存失败");
        }
    }

    public void delDoc(String videoId) {
        try {
            DeleteRequest deleteRequest = new DeleteRequest(appConfig.getEsIndexVideoName(), videoId);
            restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("从es删除视频失败", e);
            throw new BusinessException("删除视频失败");
        }

    }

    public PaginationResultVO<VideoInfo> search(Boolean highlight, String keyword, Integer orderType, Integer pageNo, Integer pageSize) {
        try {

            SearchOrderTypeEnum searchOrderTypeEnum = SearchOrderTypeEnum.getByType(orderType);

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            //关键字
            searchSourceBuilder.query(QueryBuilders.multiMatchQuery(keyword, "videoName", "tags"));

            if (highlight) {
                //高亮
                HighlightBuilder highlightBuilder = new HighlightBuilder();
                highlightBuilder.field("videoName"); // 替换为你想要高亮的字段名
                highlightBuilder.preTags("<span class='highlight'>");
                highlightBuilder.postTags("</span>");
                searchSourceBuilder.highlighter(highlightBuilder);
            }


            //排序
            searchSourceBuilder.sort("_score", SortOrder.ASC); // 第一个排序字段，升序
            if (orderType != null) {
                searchSourceBuilder.sort(searchOrderTypeEnum.getField(), SortOrder.DESC); // 第一个排序字段，升序
            }
            pageNo = pageNo == null ? 1 : pageNo;
            //分页查询
            pageSize = pageSize == null ? PageSize.SIZE20.getSize() : pageSize;
            searchSourceBuilder.size(pageSize);
            searchSourceBuilder.from((pageNo - 1) * pageSize);

            SearchRequest searchRequest = new SearchRequest(appConfig.getEsIndexVideoName());
            searchRequest.source(searchSourceBuilder);

            // 执行查询
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            // 处理查询结果
            SearchHits hits = searchResponse.getHits();
            Integer totalCount = (int) hits.getTotalHits().value;

            List<VideoInfo> videoInfoList = new ArrayList<>();

            List<String> userIdList = new ArrayList<>();
            for (SearchHit hit : hits.getHits()) {
                VideoInfo videoInfo = JsonUtils.convertJson2Obj(hit.getSourceAsString(), VideoInfo.class);
                if (hit.getHighlightFields().get("videoName") != null) {
                    videoInfo.setVideoName(hit.getHighlightFields().get("videoName").fragments()[0].string());
                }
                videoInfoList.add(videoInfo);

                userIdList.add(videoInfo.getUserId());
            }
            UserInfoQuery userInfoQuery = new UserInfoQuery();
            userInfoQuery.setUserIdList(userIdList);
            List<UserInfo> userInfoList = userInfoMapper.selectList(userInfoQuery);
            Map<String, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(item -> item.getUserId(), Function.identity(), (data1, data2) -> data2));
            videoInfoList.forEach(item -> {
                UserInfo userInfo = userInfoMap.get(item.getUserId());
                if (userInfo != null) {
                    item.setNickName(userInfo.getNickName());
                }
            });
            SimplePage page = new SimplePage(pageNo, totalCount, pageSize);
            PaginationResultVO<VideoInfo> result = new PaginationResultVO(totalCount, page.getPageSize(), page.getPageNo(), page.getPageTotal(), videoInfoList);
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询视频到es失败", e);
            throw new BusinessException("查询失败");
        }
    }

}
