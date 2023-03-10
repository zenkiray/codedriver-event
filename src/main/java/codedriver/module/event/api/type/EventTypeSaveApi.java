package codedriver.module.event.api.type;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.AuthorityVo;
import codedriver.framework.event.auth.label.EVENT_TYPE_MODIFY;
import codedriver.framework.event.dto.EventTypeVo;
import codedriver.framework.event.exception.core.EventTypeNameRepeatException;
import codedriver.framework.event.exception.core.EventTypeNotFoundException;
import codedriver.framework.lrcode.LRCodeManager;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.RegexUtils;
import codedriver.module.event.dao.mapper.EventTypeMapper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;


@AuthAction(action = EVENT_TYPE_MODIFY.class)
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class EventTypeSaveApi extends PrivateApiComponentBase {

    @Resource
    private EventTypeMapper eventTypeMapper;

    @Override
    public String getToken() {
        return "eventtype/save";
    }

    @Override
    public String getName() {
        return "保存事件类型信息";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "事件类型ID"),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, desc = "事件类型名称", isRequired = true, xss = true),
            @Param(name = "parentId", type = ApiParamType.LONG, desc = "父类型id"),
            @Param(name = "authorityList", type = ApiParamType.JSONARRAY, desc = "授权对象，可多选，格式[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\"]")
    })
    @Output({@Param(name = "eventTypeId", type = ApiParamType.LONG, desc = "保存的事件类型ID")})
    @Description(desc = "保存事件类型信息")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        EventTypeVo eventType = new EventTypeVo();
        eventType.setName(jsonObj.getString("name"));
        Integer solutionCount = null;
        if (id != null) {
            EventTypeVo typeVo = eventTypeMapper.getEventTypeById(id);
            if (typeVo == null) {
                throw new EventTypeNotFoundException(id);
            }
            Long parentId = typeVo.getParentId();
            typeVo.setName(eventType.getName());
            EventTypeVo parent = eventTypeMapper.getEventTypeById(parentId);
            checkNameIsRepeat(typeVo, parent, parentId, typeVo.getLayer());
            eventType.setId(id);
            eventTypeMapper.updateEventTypeNameById(eventType);
        } else {
            Integer parentLayer = 0;
            Long parentId = jsonObj.getLong("parentId");
            EventTypeVo parent = null;
            if (parentId == null) {
                parentId = EventTypeVo.ROOT_ID;
            } else if (!EventTypeVo.ROOT_ID.equals(parentId)) {
                parent = eventTypeMapper.getEventTypeById(parentId);
                if (parent == null) {
                    throw new EventTypeNotFoundException(parentId);
                }
                parentLayer = parent.getLayer();
            }
            checkNameIsRepeat(eventType, parent, parentId, parentLayer + 1);
            //更新插入位置右边的左右编码值
            int lft = LRCodeManager.beforeAddTreeNode("event_type", "id", "parent_id", parentId);
            eventType.setParentId(parentId);
            eventType.setLft(lft);
            eventType.setRht(lft + 1);
            //计算层级
//			int layer = eventTypeMapper.calculateLayer(eventType.getLft(), eventType.getRht());
            eventType.setLayer(parentLayer + 1);
            eventTypeMapper.insertEventType(eventType);
            /** 查询关联的解决方案数量，确保页面回显的数据正确 */
            EventTypeVo count = eventTypeMapper.getEventTypeSolutionCountByLftRht(eventType.getLft(), eventType.getRht());
            solutionCount = count.getSolutionCount();

            /** 保存授权信息 */
            JSONArray authorityArray = jsonObj.getJSONArray("authorityList");
            if (CollectionUtils.isNotEmpty(authorityArray)) {
                List<String> authorityList = authorityArray.toJavaList(String.class);
                eventType.setAuthorityList(authorityList);
                List<AuthorityVo> authorityVoList = eventType.getAuthorityVoList();
                if (CollectionUtils.isNotEmpty(authorityVoList)) {
                    for (AuthorityVo authorityVo : authorityVoList) {
                        eventTypeMapper.insertEventTypeAuthority(authorityVo, eventType.getId());
                    }
                }
            }
        }

        JSONObject returnObj = new JSONObject();
        returnObj.put("eventTypeId", eventType.getId());
        returnObj.put("solutionCount", solutionCount);
        return returnObj;
    }

    /**
     * 校验名称是否重复，规则：同一层级中不能出现重名事件类型
     *
     * @param target   待校验的事件类型
     * @param parent   待校验类型的父类型
     * @param parentId 父类型id
     * @param layer    层级
     */
    private void checkNameIsRepeat(EventTypeVo target, EventTypeVo parent, Long parentId, Integer layer) {
        EventTypeVo searchVo = new EventTypeVo();
        if (!Objects.equals(parentId, EventTypeVo.ROOT_ID)) {
            searchVo.setId(target.getId());
            searchVo.setLft(parent.getLft());
            searchVo.setRht(parent.getRht());
            searchVo.setLayer(layer);
            searchVo.setName(target.getName());
            if (eventTypeMapper.checkEventTypeNameIsRepeatByLRAndLayer(searchVo) > 0) {
                throw new EventTypeNameRepeatException();
            }
        } else {
            searchVo.setId(target.getId());
            searchVo.setName(target.getName());
            searchVo.setParentId(EventTypeVo.ROOT_ID);
            if (eventTypeMapper.checkEventTypeNameIsRepeatByParentId(searchVo) > 0) {
                throw new EventTypeNameRepeatException();
            }
        }
    }

//	private Object backup(JSONObject jsonObj) throws Exception {
//		JSONObject returnObj = new JSONObject();
//		Long id = jsonObj.getLong("id");
//		EventTypeVo eventType = new EventTypeVo();
//		eventType.setName(jsonObj.getString("name"));
//		Integer solutionCount = null;
//		if(id != null){
//			if(eventTypeMapper.checkEventTypeIsExists(id) == 0){
//				throw new EventTypeNotFoundException(id);
//			}
//			eventType.setId(id);
//			eventTypeMapper.updateEventTypeNameById(eventType);
//		}else{
//			eventTypeMapper.getEventTypeCountOnLock();
//			if(eventTypeMapper.checkLeftRightCodeIsWrong() > 0) {
//				eventTypeService.rebuildLeftRightCode();
//			}
//			Long parentId = jsonObj.getLong("parentId");
//			if (parentId == null){
//				parentId = EventTypeVo.ROOT_ID;
//			}
//			EventTypeVo parentEventType;
//			if(EventTypeVo.ROOT_ID.equals(parentId)){
//				parentEventType = eventTypeService.buildRootEventType();
//			}else{
//				parentEventType = eventTypeMapper.getEventTypeById(parentId);
//				if(parentEventType == null) {
//					throw new EventTypeNotFoundException(parentId);
//				}
//			}
//			eventType.setParentId(parentId);
//			eventType.setLft(parentEventType.getRht());
//			eventType.setRht(eventType.getLft() + 1);
//			//更新插入位置右边的左右编码值
//			eventTypeMapper.batchUpdateEventTypeLeftCode(eventType.getLft(), 2);
//			eventTypeMapper.batchUpdateEventTypeRightCode(eventType.getLft(), 2);
//
//			//计算层级
////			int layer = eventTypeMapper.calculateLayer(eventType.getLft(), eventType.getRht());
//			eventType.setLayer(parentEventType.getLayer() + 1);
//			eventTypeMapper.insertEventType(eventType);
//			/** 查询关联的解决方案数量，确保页面回显的数据正确 */
//			EventTypeVo count = eventTypeMapper.getEventTypeSolutionCountByLftRht(eventType.getLft(), eventType.getRht());
//			solutionCount = count.getSolutionCount();
//
//			/** 保存授权信息 */
//			JSONArray authorityArray = jsonObj.getJSONArray("authorityList");
//			if(CollectionUtils.isNotEmpty(authorityArray)){
//				List<String> authorityList = authorityArray.toJavaList(String.class);
//				eventType.setAuthorityList(authorityList);
//				List<AuthorityVo> authorityVoList = eventType.getAuthorityVoList();
//				if(CollectionUtils.isNotEmpty(authorityVoList)){
//					for(AuthorityVo authorityVo : authorityVoList) {
//						eventTypeMapper.insertEventTypeAuthority(authorityVo,eventType.getId());
//					}
//				}
//			}
//		}
//		returnObj.put("eventTypeId",eventType.getId());
//		returnObj.put("solutionCount",solutionCount);
//		return returnObj;
//	}
}
