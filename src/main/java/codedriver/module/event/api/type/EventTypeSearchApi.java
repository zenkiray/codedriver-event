package codedriver.module.event.api.type;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.event.dao.mapper.EventTypeMapper;
import codedriver.framework.event.dto.EventTypeVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class EventTypeSearchApi extends PrivateApiComponentBase {

    @Resource
    private EventTypeMapper eventTypeMapper;

    @Override
    public String getToken() {
        return "eventtype/search";
    }

    @Override
    public String getName() {
        return "查询事件类型";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword",
                    type = ApiParamType.STRING,
                    desc = "关键字",
                    xss = true),
            @Param(name = "parentId",
                    type = ApiParamType.LONG,
                    desc = "父类型id"),
            @Param(name = "currentPage",
                    type = ApiParamType.INTEGER,
                    desc = "当前页"),
            @Param(name = "pageSize",
                    type = ApiParamType.INTEGER,
                    desc = "每页数据条目"),
            @Param(name = "needPage",
                    type = ApiParamType.BOOLEAN,
                    desc = "是否需要分页，默认true")
    })
    @Output({
            @Param(name = "eventTypeList",
                    type = ApiParamType.JSONARRAY,
                    explode = EventTypeVo[].class,
                    desc = "事件类型"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "查询事件类型")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        EventTypeVo eventTypeVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<EventTypeVo>() {
        });
        JSONObject returnObj = new JSONObject();
        if (eventTypeVo.getNeedPage()) {
            int rowNum = eventTypeMapper.searchEventTypeCount(eventTypeVo);
            returnObj.put("pageSize", eventTypeVo.getPageSize());
            returnObj.put("currentPage", eventTypeVo.getCurrentPage());
            returnObj.put("rowNum", rowNum);
            returnObj.put("pageCount", PageUtil.getPageCount(rowNum, eventTypeVo.getPageSize()));
        }
        List<EventTypeVo> eventTypeList = eventTypeMapper.searchEventType(eventTypeVo);
        returnObj.put("eventTypeList", eventTypeList);
        return returnObj;
    }

}
