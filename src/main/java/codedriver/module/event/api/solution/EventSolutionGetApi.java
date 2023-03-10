package codedriver.module.event.api.solution;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.event.dao.mapper.EventSolutionMapper;
import codedriver.framework.event.dto.EventSolutionVo;
import codedriver.framework.event.exception.core.EventSolutionNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class EventSolutionGetApi extends PrivateApiComponentBase {

    @Resource
    private EventSolutionMapper eventSolutionMapper;

    @Override
    public String getToken() {
        return "event/solution/get";
    }

    @Override
    public String getName() {
        return "获取单个解决方案";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "解决方案ID", isRequired = true)
    })
    @Output({
            @Param(name = "id", type = ApiParamType.LONG, desc = "解决方案ID"),
            @Param(name = "name", type = ApiParamType.STRING, desc = "解决方案名称"),
            @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "是否启用"),
            @Param(name = "content", type = ApiParamType.STRING, desc = "内容"),
            @Param(name = "fcu", type = ApiParamType.STRING, desc = "创建人ID"),
            @Param(name = "lcu", type = ApiParamType.STRING, desc = "更新人ID"),
            @Param(name = "fcd", desc = "创建时间"),
            @Param(name = "lcd", desc = "更新时间"),
            @Param(name = "eventTypeList", type = ApiParamType.JSONARRAY, desc = "关联的事件类型"),
    })
    @Description(desc = "获取单个解决方案")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject result = new JSONObject();
        Long id = jsonObj.getLong("id");
        if (eventSolutionMapper.checkSolutionExistsById(id) == null) {
            throw new EventSolutionNotFoundException(id);
        }
        EventSolutionVo solution = eventSolutionMapper.getSolutionById(id);
        result.put("solution", solution);
        return result;
    }
}
