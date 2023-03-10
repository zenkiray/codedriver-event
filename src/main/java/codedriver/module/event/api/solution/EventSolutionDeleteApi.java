package codedriver.module.event.api.solution;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.event.auth.label.EVENT_SOLUTION_MODIFY;
import codedriver.module.event.dao.mapper.EventSolutionMapper;
import codedriver.framework.event.exception.core.EventSolutionNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@AuthAction(action = EVENT_SOLUTION_MODIFY.class)
@Service
@OperationType(type = OperationTypeEnum.DELETE)
public class EventSolutionDeleteApi extends PrivateApiComponentBase {

    @Resource
    private EventSolutionMapper eventSolutionMapper;

    @Override
    public String getToken() {
        return "event/solution/delete";
    }

    @Override
    public String getName() {
        return "删除解决方案";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "解决方案ID", isRequired = true)
    })
    @Description(desc = "删除解决方案")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {

        Long id = jsonObj.getLong("id");
        if (eventSolutionMapper.checkSolutionExistsById(id) == null) {
            throw new EventSolutionNotFoundException(id);
        }
        eventSolutionMapper.deleteSolution(id);

        return null;
    }
}
