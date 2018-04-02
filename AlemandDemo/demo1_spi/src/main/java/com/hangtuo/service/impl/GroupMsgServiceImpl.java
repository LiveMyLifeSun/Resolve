package com.hangtuo.service.impl;


import com.google.common.base.Strings;

import com.hangtuo.Page;
import com.hangtuo.Pageable;
import com.hangtuo.common.Enum.XZ_ADMIN_TYPE;
import com.hangtuo.common.Enum.XZ_CLIENT_GROUP_STATUS;
import com.hangtuo.common.Enum.XZ_GROUP_DISMISS_STATUS;
import com.hangtuo.common.Enum.XZ_MODEL_TYPE;
import com.hangtuo.common.Enum.XZ_MSG_STATUS;
import com.hangtuo.common.NameHelper;
import com.hangtuo.common.response.BusinessException;
import com.hangtuo.common.response.ReturnCode;
import com.hangtuo.dao.group.GroupLocationDao;
import com.hangtuo.entity.Contacter.RepGroupMemItem;
import com.hangtuo.entity.Contacter.ReqRepConGroupMemModel;
import com.hangtuo.entity.Contacter.ReqRepGroupMemModelItem;
import com.hangtuo.entity.GroupMember;
import com.hangtuo.entity.GroupMsg;
import com.hangtuo.entity.HuanXin.HXGroup;
import com.hangtuo.entity.HuanXin.HXUser;
import com.hangtuo.entity.NewClient;
import com.hangtuo.entity.client.outer.ResTinyGroup;
import com.hangtuo.entity.config.MainConfig;
import com.hangtuo.entity.group.GroupDismissInfo;
import com.hangtuo.entity.group.GroupPermission;
import com.hangtuo.entity.group.GroupUnread;
import com.hangtuo.entity.group.RepConGroupInfoModel;
import com.hangtuo.entity.group.RepGroupCert;
import com.hangtuo.entity.group.RepGroupInfo;
import com.hangtuo.entity.group.ReqClientGroupID;
import com.hangtuo.entity.group.ReqGroupCreate;
import com.hangtuo.entity.group.ReqGroupDismiss;
import com.hangtuo.entity.group.ReqGroupUpdate;
import com.hangtuo.entity.group.ReqGroupZZBInfo;
import com.hangtuo.entity.group.ReqInviteSMS;
import com.hangtuo.entity.group.ReqInviteSMSItem;
import com.hangtuo.entity.group.ResGroupMsg;
import com.hangtuo.entity.group.location.GroupLocation;
import com.hangtuo.entity.group.location.LocationInfo;
import com.hangtuo.entity.group.location.NearbyGroupInfo;
import com.hangtuo.entity.group.location.ReqNearbyGroup;
import com.hangtuo.entity.message.SubMsgGroup;
import com.hangtuo.entity.pcmenu.PCMenuInfo;
import com.hangtuo.entity.pcmenu.response.ResTinyMenu;
import com.hangtuo.entity.plan.ResTinyGroupInfo;
import com.hangtuo.entity.weather.WeatherGetCityCodeEntity;
import com.hangtuo.persistence.GroupMsgMapper;
import com.hangtuo.service.GroupAdminService;
import com.hangtuo.service.GroupMemberService;
import com.hangtuo.service.GroupMsgService;
import com.hangtuo.service.GroupSendMsgService;
import com.hangtuo.service.UnreadService;
import com.hangtuo.service.VersionRecordService;
import com.hangtuo.service.WeatherService;
import com.hangtuo.service.annotation.GroupInfoCacheUpdate;
import com.hangtuo.service.client.NewClientService;
import com.hangtuo.service.group.GroupRedisCacheService;
import com.hangtuo.service.group.UserGroupRedisCacheService;
import com.hangtuo.service.mclound.MCloundService;
import com.hangtuo.service.menu.PcMenuService;
import com.hangtuo.util.EasemobIMUsers;
import com.hangtuo.util.HuanXin.EasemodGroupUtil;
import com.hangtuo.util.HuanXin.EasemodUserUtil;
import com.hangtuo.util.SmsUtil;
import com.hangtuo.util.location.MapDistinceUtil;
import com.hangtuo.util.skutls.ConstantDefine;
import com.hangtuo.util.skutls.MongoConnFactory;
import com.hangtuo.util.skutls.SKTools;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.xiaoleilu.hutool.log.Log;
import com.xiaoleilu.hutool.log.LogFactory;
import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.StrUtil;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

/**
 * Service - 小组
 *
 * @author 王存
 * @version create time：2016年4月19日 下午1:42:45
 * @Description: 类说明
 */
@Service("groupMsgServiceImpl")
public class GroupMsgServiceImpl extends BaseServiceImpl<GroupMsg, Integer>
        implements GroupMsgService {

    private static final Log logger = LogFactory.get();
    private static final double MAX_NEAR_INSTANCE = 3000;
    private static MainConfig mainConfig = MainConfig.getInstance();
    @Resource(name = "versionRecordServiceImpl")
    private VersionRecordService versionRecordService;
    @Autowired
    private GroupMsgMapper groupMsgMapper;
    @Resource(name = "weatherServiceImpl")
    private WeatherService weatherService;
    @Resource(name = "newClientServiceImpl")
    private NewClientService clientService;
    @Resource(name = "groupMemberServiceImpl")
    private GroupMemberService groupMemberService;
    @Resource(name = "groupAdminServiceImpl")
    private GroupAdminService groupAdminService;
    @Resource(name = "UnreadServiceImp")
    private UnreadService unreadService;
    @Resource(name = "groupSendMsgServiceImpl")
    private GroupSendMsgService groupSendMsgService;
    @Resource(name = "groupLocationDaoImpl")
    private GroupLocationDao groupLocationDao;
    @Resource(name = "MCloundServiceImpl")
    private MCloundService mCloundService;
    @Resource(name = "pcMenuServiceImpl")
    private PcMenuService pcMenuService;
    @Resource(name="userGroupRedisCacheServiceImpl")
    private UserGroupRedisCacheService userGroupRedisCacheService;
    @Resource(name = "groupRedisCacheServiceImpl")
    private GroupRedisCacheService groupRedisCacheService;
    //private static Properties sysConfig = SKTools.readPropertyWithName("sysprofile");

    public void setBaseMapper(GroupMsgMapper groupMsgMapper) {
        super.setBaseMapper(groupMsgMapper);
    }

    @Override
    public void updateByGroupId(GroupMsg g) {
        groupMsgMapper.updateByGroupId(g);
    }

    @Override
    public void changeNumber(GroupMsg groupMsg) {
        groupMsgMapper.changeNumber(groupMsg);
    }


    @Transactional(readOnly = true)
    @Override
    public Page<GroupMsg> findPages(GroupMsg groupMsg, Pageable pageable) {

        if (pageable == null) {
            pageable = new Pageable();
        }
        List<GroupMsg> page = groupMsgMapper.findForPages(groupMsg, pageable);
        int pageCount = groupMsgMapper.findForPageCounts(groupMsg);
        return new Page<GroupMsg>(page, pageCount, pageable);
    }

    @Override
    public GroupMsg findByGroupId(String groupId) {
        return groupMsgMapper.findByGroupId(groupId);
    }

    /**
     * 根据groupID查询GroupMsgID
     */
    @Override
    public String getGroupMsgIDByGrouId(String groupID) throws BusinessException {
        return groupMsgMapper.findByGroupId(groupID).getGroupMsgId().toString();
    }

    @Override
    public GroupMsg findInfoById(Integer groupMsgId) {
        return groupMsgMapper.findInfoById(groupMsgId);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<GroupMsg> findPageMsg(GroupMsg groupMsg, Pageable pageable) {

        if (pageable == null) {
            pageable = new Pageable();
        }
        List<GroupMsg> page = groupMsgMapper.findForPageMsg(groupMsg, pageable);
        int pageCount = groupMsgMapper.findForPageCountMsg(groupMsg);
        return new Page<GroupMsg>(page, pageCount, pageable);
    }

    @Override
    public GroupMsg findByCreateUId(Integer groupCreateUId) {
        return groupMsgMapper.findByCreateUId(groupCreateUId);
    }

    @Override
    public List<GroupMsg> findListByUserId(GroupMsg g) {
        return groupMsgMapper.findListByUserId(g);
    }

    @Override
    public int getSystemGroupMsgNumber() throws BusinessException {
        return groupMsgMapper.getSystemGroupMsgNumber();
    }

    @Override
    public int getTodayAddGroupMsgNumber() throws BusinessException {
        return groupMsgMapper.getTodayAddGroupMsgNumber();
    }

    /**
     * 根据groupID 获取项目部基本信息
     */
    @Override
    public GroupMsg findBaseGroupMsgInfoByGroupID(String groupID) throws BusinessException {
        if (StrUtil.isBlank(groupID)) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "groupID is Empty");
        }
        GroupMsg groupMsg = groupMsgMapper.findGroupMsgByGroupID(groupID);
        return groupMsg;

    }

    /**
     * 通过cityCode查询
     */
    @Override
    public List<GroupMsg> findListByCityCode(String cityCode) {
        return groupMsgMapper.findListByCityCode(cityCode);
    }

    /**
     * 是否为群组成员
     */
    @Override
    public boolean isGroupMember(GroupMember groupMember) {
        int cNum = groupMemberService.findCountByUserId(groupMember);

        return cNum > 0;
    }

    @Override
    public void saveGroupCertApply(RepGroupCert repGroupCert) {


        if (StrUtil.isEmpty(repGroupCert.getGroupID())) {
            logger.error("saveGroupCertApply error ,GroupID is Empty");
            return;
        }

        //  1.更新项目部的状态
        GroupMsg updateGroupMsgTmp = groupMsgMapper.findByGroupId(repGroupCert.getGroupID());

        if (updateGroupMsgTmp != null) {
            // 目前只有这个状态
            updateGroupMsgTmp.setCertLevel(1);
            // 保存数据
            groupMsgMapper.update(updateGroupMsgTmp);
        }


        DBObject queryObj = SKTools.convertBeanToDBObject(repGroupCert);

        // 保存
        DBCollection dbCollection = MongoConnFactory.getDBCollectionWithName(ConstantDefine.MONGO_COLLECTION_CROUP_CERT);
        dbCollection.save(queryObj);
    }

    /**
     * 获取项目部认证相关信息
     */
    @Override
    public RepGroupCert getGroupCert(String groupID) throws BusinessException {
        if (StrUtil.isBlank(groupID)) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "groupID is Empty");
        }
        DBCollection dbCollection = MongoConnFactory.getDBCollectionWithName(ConstantDefine.MONGO_COLLECTION_CROUP_CERT);
        DBObject searchObj = new BasicDBObject().append("groupID", groupID);
        DBCursor cursor = dbCollection.find(searchObj);
        List<DBObject> resList = MongoConnFactory.toList(cursor);
        RepGroupCert repGroupCert = null;
        if (resList.size() > 0) {
            DBObject objTmp = resList.get(resList.size() - 1);
            repGroupCert = (RepGroupCert) SKTools.convertDBObjectToBean(objTmp, RepGroupCert.class);
        }
        return repGroupCert;
    }

    // 保存用户申请
    @Override
    public String saveGroupAddRequest(NewClient reqClient, GroupMsg groupMsg) {

        SubMsgGroup subMsgGroup = new SubMsgGroup();
        subMsgGroup.setGroupID(groupMsg.getGroupId());
        subMsgGroup.setClientID(String.valueOf(reqClient.getClientID())); // 保存用户ID
        subMsgGroup.setCreateUID(String.valueOf(groupMsg.getGroupCreateUId()));

        DBObject queryObj = SKTools.convertBeanToDBObject(subMsgGroup);

        // 保存
        DBCollection dbCollection = MongoConnFactory.getDBCollectionWithName(ConstantDefine.MONGO_COLLECTION_GROUPAPPLY);
        dbCollection.save(queryObj);

        String requestID = queryObj.get("_id").toString();
        return requestID;
    }

    // 保存用户邀请申请
    @Override
    public String saveGroupInviteAddRequest(NewClient reqClient, NewClient inviteClient, GroupMsg groupMsg) {

        SubMsgGroup subMsgGroup = new SubMsgGroup();
        subMsgGroup.setGroupID(groupMsg.getGroupId());
        subMsgGroup.setClientID(String.valueOf(reqClient.getClientID())); // 保存用户ID
        subMsgGroup.setCreateUID(String.valueOf(groupMsg.getGroupCreateUId()));
        subMsgGroup.setInviteUserID(String.valueOf(inviteClient.getClientID()));// 邀请人ID

        DBObject queryObj = SKTools.convertBeanToDBObject(subMsgGroup);

        // 保存
        DBCollection dbCollection = getDBCollection();
        dbCollection.save(queryObj);

        String requestID = queryObj.get("_id").toString();
        return requestID;
    }

    // 获取申请加入的数据
    @Override
    public SubMsgGroup getapplyAddGroupObj(String requestID) {

        if (Strings.isNullOrEmpty(requestID)) {
            return new SubMsgGroup();
        }

        ObjectId objectId = new ObjectId(requestID);
        BasicDBObject searchCond = new BasicDBObject().append("_id", objectId);
        DBCollection dbCollection = getDBCollection();
        // 查询
        DBCursor cursor = dbCollection.find(searchCond);

        List<DBObject> convid_Array = MongoConnFactory.toList(cursor);
        SubMsgGroup subMsgGroup = new SubMsgGroup();
        if (convid_Array.size() > 0) {

            DBObject objTmp = (DBObject) convid_Array.get(0);

            ObjectId mongoID = (ObjectId) objTmp.get("_id");
            subMsgGroup = (SubMsgGroup) SKTools.convertDBObjectToBean(objTmp, SubMsgGroup.class);
            subMsgGroup.setRequestID(mongoID.toString());
        }

        return subMsgGroup;
    }


    /**
     * 是否正在申请
     */
    @Override
    public boolean hasApplying(NewClient reqClient, GroupMsg groupMsg) throws BusinessException {

        boolean hasApplying;
        SubMsgGroup subMsgGroup = new SubMsgGroup();
        subMsgGroup.setGroupID(groupMsg.getGroupId());
        subMsgGroup.setClientID(String.valueOf(reqClient.getClientID())); // 保存用户ID

        DBObject queryObj = SKTools.convertBeanToDBObject(subMsgGroup);

        DBCollection dbCollection = getDBCollection();

        List<DBObject> convid_Array = dbCollection.find(queryObj).toArray(); // 根据conv_ident查询对应的回话

        if (convid_Array.size() > 0) {
            hasApplying = true;
        } else {
            hasApplying = false;
        }

        return hasApplying;
    }

    // 同意用户加入
    @Override
    public void applyAddGroupAgree(String requestID, String messageID) {

        // 1.删除这个用户的申请,
        delRequestIDInMongodb(requestID);

        // 2.改变messageId的状态
        updateGroupApplyMsg(messageID, XZ_MSG_STATUS.Agree);
    }

    // 拒绝用户加入
    @Override
    public void rejectAddGroupAgree(String requestID, String messageID) {

        // 1.删除这个用户的申请,
        delRequestIDInMongodb(requestID);

        // 2.改变messageId的状态
        updateGroupApplyMsg(messageID, XZ_MSG_STATUS.Reject);

    }

    // 更新用户申请状态
    @Override
    public void updateGroupApplyMsg(String msgID, XZ_MSG_STATUS msgStatus) {

        if (Strings.isNullOrEmpty(msgID)) {
            return;
        }
        // 1.直接更新子文档
        ObjectId objectId = new ObjectId(msgID);
        DBCollection dbCollection = MongoConnFactory.getDBCollectionWithName(ConstantDefine.MONGO_COLLECTION_Msg);

        BasicDBObject searchCond = new BasicDBObject();
        searchCond.put("_id", objectId);

        BasicDBObject statusCond = new BasicDBObject().append("Content.status", msgStatus.getValue());
        BasicDBObject updateCond = new BasicDBObject().append("$set", statusCond);

        // 更新整个消息
        dbCollection.update(searchCond, updateCond, false, true);
    }


    /**
     * 删除这个用户的申请
     */
    private void delRequestIDInMongodb(String requestID) {
        if (Strings.isNullOrEmpty(requestID)) {
            return;
        }
        ObjectId objectId = new ObjectId(requestID);
        BasicDBObject delCond = new BasicDBObject().append("_id", objectId);
        DBCollection dbCollection = getDBCollection();
        dbCollection.remove(delCond);
    }


    // 保存解散申请
    @Override
    public String saveDismissRequest(SubMsgGroup subMsgGroup) {

        DBObject queryObj = SKTools.convertBeanToDBObject(subMsgGroup);
        // 保存
        DBCollection dbCollection = MongoConnFactory.getDBCollectionWithName(ConstantDefine.MONGO_COLLECTION_GROUPDISSOLVE);
        dbCollection.save(queryObj);

        String requestID = queryObj.get("_id").toString();
        return requestID;
    }

    // 同意解散申请 ,只用处理状态即可
    @Override
    public void agreeDismis(String requestID, String messageID, String groupID, String dismissID) throws BusinessException {
        // .先同意这个会话
        if (Strings.isNullOrEmpty(requestID)) {
            return;
        }
        updateDismissStatue(requestID, XZ_MSG_STATUS.Agree);
        updateGroupApplyMsg(messageID, XZ_MSG_STATUS.Agree);

        // 2.获取这个群组现在通过多少个人.
        int agressCount = this.getDismissAgreeCount(groupID, dismissID);

        // 更新群组同意的人数
        this.updateDismissCount(groupID, dismissID, agressCount);

        GroupMsg groupMsg = groupMsgMapper.findByGroupId(groupID);
        List groupMemberList = groupMsg.getGroupMemberList();

        // 如果过半那就解散吧
        if (agressCount > (groupMemberList.size() / 2)) {
            // 解散了.
            groupMsg.setStatus(1);
            groupMsgMapper.update(groupMsg);

            // 更新解散成功
            GroupDismissInfo groupDissmissInfo = getDissmissInfo(dismissID);
            if (groupDissmissInfo == null) {
                throw new BusinessException(ReturnCode.CODE_CONTACTER_GROUPID_ERR, "解散组不同");
            }
            groupDissmissInfo.setDismissStatus(XZ_GROUP_DISMISS_STATUS.Success.getValue());
            saveDissmissInfo(groupDissmissInfo);


            for (GroupMember groupMemberTmp : groupMsg.getGroupMemberList()) {

                // 从环信删除这个人
                EasemobIMUsers.deleteUserFromGroup(groupID, groupMemberTmp.getGmsId() + "");
            }

            // 发送真正的解散消息
            groupSendMsgService.sendAlreadyDismissMsg(groupMsg, agressCount, dismissID);
        }
    }

    /**
     * 拒绝解散申请,只用处理状态即可
     */
    @Override
    public void rejectDismis(String requestID, String messageID, String groupID, String dismissID) throws BusinessException {

        //只用处理状态即可
        if (Strings.isNullOrEmpty(requestID)) {
            return;
        }
        updateDismissStatue(requestID, XZ_MSG_STATUS.Reject);
        updateGroupApplyMsg(messageID, XZ_MSG_STATUS.Reject);

        // Todo 如果大于一般人不同意就直接gg

        // 2.获取这个群组现在通过多少个人.
        int dismissAgreeCount = this.getDismissRejectCount(groupID, dismissID);

        // 更新群组同意的人数
        this.updateDismissCount(groupID, dismissID, dismissAgreeCount);

        GroupMsg groupMsg = groupMsgMapper.findByGroupId(groupID);
        List groupMemberList = groupMsg.getGroupMemberList();

        // 获取老的dissmissInfo
        GroupDismissInfo groupDissmissInfo = getDissmissInfo(dismissID);

        // 如果过半 失败
        if (dismissAgreeCount > (groupMemberList.size() / 2)) {

            groupDissmissInfo.setDismissStatus(XZ_GROUP_DISMISS_STATUS.Fail.getValue());
            saveDissmissInfo(groupDissmissInfo);

            // 发送真正的解散消息--》失败
            groupSendMsgService.sendDismissFailMsg(groupMsg, dismissID);
        }

    }

    /**
     * 获取解散具体信息,只要拿到管理员发起的就行了
     */
    @Override
    public SubMsgGroup getDissmisInfo(String groupID, String clientID, String dismissID) throws BusinessException {

        SubMsgGroup subMsgGroup = null;

        DBCollection dbCollection = MongoConnFactory.getDBCollectionWithName(ConstantDefine.MONGO_COLLECTION_GROUPDISSOLVE);

        DBObject searchCond = new BasicDBObject();
        searchCond.put("groupID", groupID);
        searchCond.put("clientID", clientID);
        searchCond.put("dismissID", dismissID);

        // 查询
        DBCursor cursor = dbCollection.find(searchCond);

        List<DBObject> convid_Array = MongoConnFactory.toList(cursor);
        try {

            DBObject dbOBJTmp = convid_Array.get(0);
            subMsgGroup = (SubMsgGroup) SKTools.convertDBObjectToBean(dbOBJTmp, SubMsgGroup.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return subMsgGroup;
    }


    /**
     * 获取解散同意的数据
     */
    @Override
    public int getDismissAgreeCount(String groupID, String dismissID) throws BusinessException {

        return getDismissCount(groupID, dismissID, XZ_MSG_STATUS.Agree);
    }

    /**
     * 获取解散同意的数据
     */
    @Override
    public int getDismissRejectCount(String groupID, String dismissID) throws BusinessException {
        return getDismissCount(groupID, dismissID, XZ_MSG_STATUS.Reject);
    }

    // 更新dismiss 的数据
    private void updateDismissStatue(String requestID, XZ_MSG_STATUS status) {

        if (Strings.isNullOrEmpty(requestID)) {
            return;
        }

        // 1.直接 文档
        ObjectId objectId = new ObjectId(requestID);
        DBCollection dbCollection = MongoConnFactory.getDBCollectionWithName(ConstantDefine.MONGO_COLLECTION_GROUPDISSOLVE);

        BasicDBObject searchCond = new BasicDBObject();
        searchCond.put("_id", objectId);

        BasicDBObject statusCond = new BasicDBObject().append("status", status.getValue());
        BasicDBObject updateCond = new BasicDBObject().append("$set", statusCond);

        // 更新数据
        dbCollection.update(searchCond, updateCond, false, true);
    }

    /**
     * 获取 同意或者拒绝的数据
     */
    private int getDismissCount(String groupID, String dismissID, XZ_MSG_STATUS status) {
        if (Strings.isNullOrEmpty(groupID)) {
            return 0;
        }

        BasicDBObject searchCond = new BasicDBObject();
        searchCond.append("groupID", groupID);
        searchCond.append("status", status.getValue());
        searchCond.append("dismissID", dismissID);

        DBCollection dbCollection = MongoConnFactory.getDBCollectionWithName(ConstantDefine.MONGO_COLLECTION_GROUPDISSOLVE);
        // 查询
        DBCursor cursor = dbCollection.find(searchCond);

        List<DBObject> convid_Array = MongoConnFactory.toList(cursor);

        return convid_Array.size();
    }

    /**
     * 更新
     */
    @Override
    public void updateDismissCount(String groupID, String dismissID, int agreeCount) {

        // 1.先查出 所有的requestID
        DBCollection dbCollection = MongoConnFactory.getDBCollectionWithName(ConstantDefine.MONGO_COLLECTION_GROUPDISSOLVE);

        BasicDBObject groupCond = new BasicDBObject().append("groupID", groupID);
        groupCond.put("dismissID", dismissID);
        // 查询
        DBCursor cursor = dbCollection.find(groupCond);

        List<DBObject> convid_Array = MongoConnFactory.toList(cursor);

        for (DBObject dbObject : convid_Array) {
            String requestIDTmp = String.valueOf(dbObject.get("_id").toString());

            // 2.更新对应的子文档
            updateDismissMsg(requestIDTmp, agreeCount);
        }

    }


    // 更新解散的消息
    private void updateDismissMsg(String requestID, int agreeCount) {
        if (Strings.isNullOrEmpty(requestID)) {
            return;
        }
        // 1.直接更新子文档
        DBCollection dbCollection = MongoConnFactory.getDBCollectionWithName(ConstantDefine.MONGO_COLLECTION_Msg);

        BasicDBObject searchCond = new BasicDBObject();
        searchCond.put("Content.requestID", requestID);

        BasicDBObject statusCond = new BasicDBObject().append("Content.agreeDisoveCount", agreeCount);
        BasicDBObject updateCond = new BasicDBObject().append("$set", statusCond);

        // 更新整个消息
        dbCollection.update(searchCond, updateCond, false, true);
    }


    /**
     * 是否正在正在解散
     */
    @Override
    public boolean groupIsDismissing(String groupID) {

        if (Strings.isNullOrEmpty(groupID)) {
            return false;
        }

        // 解散状态查询
        DBCollection dbCollection = MongoConnFactory.getDBCollectionWithName(ConstantDefine.MONGO_COLLECTION_GROUPDISSOLVE_RECORD);

        BasicDBObject searchCond = new BasicDBObject();
        searchCond.append("groupID", groupID);
        searchCond.append("dismissStatus", XZ_GROUP_DISMISS_STATUS.Dismissing.getValue());

        Long count = dbCollection.count(searchCond);

        return count > 0;
    }

    /**
     * 判断解散是否失败
     */
    @Override
    public XZ_GROUP_DISMISS_STATUS getDisMissStatus(String dismissID, String groupID) throws BusinessException {

        // 默认是解散中
        XZ_GROUP_DISMISS_STATUS status = XZ_GROUP_DISMISS_STATUS.Dismissing;

        // 1.判断是否过期
        GroupDismissInfo groupDissmissInfo = getDissmissInfo(dismissID);

        if (groupDissmissInfo == null) {

            status = XZ_GROUP_DISMISS_STATUS.Dismissing;
            return status;
        }

        long nowTime = SKTools.getNowTimeStamp();
        long oldDismissTime = groupDissmissInfo.getDismissTime();

        // 时间超过24小时
        if ((nowTime - oldDismissTime) > 86400000) {
            // 判断是否超时
            GroupMsg groupMsg = groupMsgMapper.findByGroupId(groupID);
            if (groupMsg == null) {
                throw new BusinessException(ReturnCode.CODE_CONTACTER_GROUPID_ERR, "group不存在");
            }
            // 现在还是没解散，那么解散失败
            if (groupMsg.getStatus() == 0) {
                // 更新解散状态，由第一个人处罚
                if (groupDissmissInfo.getDismissStatus() == XZ_GROUP_DISMISS_STATUS.Dismissing.getValue()) {

                    groupDissmissInfo.setDismissStatus(XZ_GROUP_DISMISS_STATUS.Fail.getValue());
                    saveDissmissInfo(groupDissmissInfo);
                    status = XZ_GROUP_DISMISS_STATUS.Fail;

                    // 发送真正的解散消息
                    groupSendMsgService.sendDismissFailMsg(groupMsg, dismissID);

                } else {
                    status = XZ_GROUP_DISMISS_STATUS.valueOf(groupDissmissInfo.getDismissStatus());
                }
            } else {
                // 更新成功
                groupDissmissInfo.setDismissStatus(XZ_GROUP_DISMISS_STATUS.Success.getValue());
                saveDissmissInfo(groupDissmissInfo);
                status = XZ_GROUP_DISMISS_STATUS.Success;
            }
        } else {
            status = XZ_GROUP_DISMISS_STATUS.valueOf(groupDissmissInfo.getDismissStatus());
        }

        return status;
    }

    /**
     * 查找所有的城市code
     */
    @Override
    public List<GroupMsg> findAllCityCode() {
        return groupMsgMapper.findAllCityCode();
    }

    /**
     * 查询用户所属群组 联系人模块
     */
    @Override
    public List<RepConGroupInfoModel> findClientOwnGroup(String clientID) {

        ArrayList<RepConGroupInfoModel> reusltAry = new ArrayList<>();

        if (Strings.isNullOrEmpty(clientID)) {
            return reusltAry;
        }
        int requestID = Integer.valueOf(clientID);
        // 1.
        List<GroupMsg> groupMsgList = groupMsgMapper.findUserGroupListWithUserID(requestID);

        for (GroupMsg groumsgTmp : groupMsgList) {
            RepConGroupInfoModel infoTmp = RepConGroupInfoModel.makeRepConGroupInfoModel(groumsgTmp);
            int creatClientID = groumsgTmp.getGroupCreateUId();
            if (requestID == creatClientID) {
                infoTmp.setIsAdmin(1);
            } else {
                infoTmp.setIsAdmin(0);
            }

            infoTmp.setUpdateTime(versionRecordService.getGroupMemUpdateTime(groumsgTmp.getGroupId()));

            reusltAry.add(infoTmp);
        }

        return reusltAry;
    }

    /**
     * 查询用户所属所有群组 包括选人聊   群组模块
     */
    @Override
    public List<RepConGroupInfoModel> findClientOwnAllGroup(String clientID) {

        ArrayList<RepConGroupInfoModel> reusltAry = new ArrayList<>();

        if (Strings.isNullOrEmpty(clientID)) {
            return reusltAry;
        }
        int requestID = Integer.valueOf(clientID);

        List<GroupMsg> groupMsgList = groupMsgMapper.findUserGroupAndSubGroupListWithUserID(requestID);

        for (GroupMsg groumsgTmp : groupMsgList) {
            RepConGroupInfoModel infoTmp = RepConGroupInfoModel.makeRepConGroupInfoModel(groumsgTmp);
            int creatClientID = groumsgTmp.getGroupCreateUId();
            if (requestID == creatClientID) {
                infoTmp.setIsAdmin(1);
            } else {
                infoTmp.setIsAdmin(0);
            }

            List<GroupMember> groupMembers = groupMemberService.findListByGroupId(groumsgTmp.getGroupId());
            if (CollectionUtil.isEmpty(groupMembers)) {
                continue;
            }
            // 设置组员数据
            infoTmp.setGroupNumber(groupMembers.size());
            ArrayList<String> memberIDList = new ArrayList<>();

            for (GroupMember memTmp : groupMembers) {
                memberIDList.add(String.valueOf(memTmp.getGmsId()));
            }
            // 组员所有的ID
            infoTmp.setMemberClientIDList(memberIDList);

         /*   if (groumsgTmp.getType() == 1) {
                if (groupMembers.size() == 1) {
                    continue;
                }
                infoTmp.setGroupImg(groupMemberService.makeSubGroupImage(groupMembers));
            }*/

            String groupImg = groumsgTmp.getGroupImg();
            //如果图片为空的,传回拼接的图片
            if (StrUtil.isBlank(groupImg)) {
                groupImg = groupMemberService.makeSubGroupImage(groupMembers);
            }
            infoTmp.setGroupImg(groupImg);

            infoTmp.setUpdateTime(versionRecordService.getGroupMemUpdateTime(groumsgTmp.getGroupId()));
            String groupName = infoTmp.getGroupName();
            if (StrUtil.isBlank(groupName)) {
                groupName = makeGroupNameByGroupMembers(groupMembers);
                infoTmp.setGroupName(groupName);
            }
            reusltAry.add(infoTmp);
        }

        return reusltAry;
    }

    private String makeGroupNameByGroupMembers(List<GroupMember> members) {
        String name = "";
        if (CollectionUtil.isNotEmpty(members)) {
            ArrayList<String> names = new ArrayList<>();
            for (GroupMember groupMember : members) {
                String username = groupMember.getClient().getUserName();
                names.add(username);
            }
            name = StrUtil.join("、", names);
        }

        return name;
    }

    /**
     * 查询群组里面的人
     */
    @Override
    public ReqRepConGroupMemModel findGroupMemWithList(ReqRepConGroupMemModel reqRepConGroupMemModel) throws BusinessException {

        if (null == reqRepConGroupMemModel) {
            throw new BusinessException(ReturnCode.CODE_CONTACTER_GROUPID_ERR, "请求列表GroupID不能为空");
        }

        ReqRepConGroupMemModel returnModel = new ReqRepConGroupMemModel();
        List<ReqRepGroupMemModelItem> returnList = new ArrayList<ReqRepGroupMemModelItem>();

        List<ReqRepGroupMemModelItem> groupMemModelItemList = reqRepConGroupMemModel.getGroupList();

        // 去处理每一个group的数据
        for (ReqRepGroupMemModelItem reqmodelItemTmp : groupMemModelItemList) {

            String groupIDTmp = reqmodelItemTmp.getGroupId();
            long serverUpdatetimeTmp = versionRecordService.getGroupMemUpdateTime(groupIDTmp);

            List<RepGroupMemItem> groupItemListTmp = new ArrayList<>();
            if (serverUpdatetimeTmp == 0 || serverUpdatetimeTmp > reqmodelItemTmp.getLastUpdateTime()) {

                groupItemListTmp = getGroupMemberList(groupIDTmp);

                // 如果时间为0，则直接刷新下时间，更新服务器的版本更新时间
                if (serverUpdatetimeTmp == 0) {
                    long updateTimeTmp = SKTools.getNowTimeStamp();
                    serverUpdatetimeTmp = updateTimeTmp;
                    versionRecordService.updateGroupMemUpdateTime(groupIDTmp, updateTimeTmp);
                }

            } else {
                // 如果是相同的话 直接过了
                continue;
            }

            // 如果没有人直接过了
            if (groupItemListTmp.size() == 0) {
                continue;
            }

            ReqRepGroupMemModelItem retrunReModelTmp = new ReqRepGroupMemModelItem.Builder()
                    .withGroupId(groupIDTmp)
                    .withGroupList(groupItemListTmp)
                    .withUpdateTime(serverUpdatetimeTmp)
                    .build();

            returnList.add(retrunReModelTmp);
        }

        returnModel.setGroupList(returnList);

        return returnModel;
    }

    /**
     * 创建群组
     */
    @Override
    public String creatGroup(ReqGroupCreate reqGroupCreat) throws BusinessException {
        long nowTime = SKTools.getNowTimeStamp();
        // 1.去环信创建.并获取GroupID 同时注册环信助手 ，
        HXGroup hxGroup = new HXGroup.Builder()
                .with_public(false)
                .withGroupname(reqGroupCreat.getGroupName())
                .withApproval(true)
                .withDesc("")
                .withMaxusers(333)
                .withOwner(reqGroupCreat.getGroupCreateUId())
                .withMembers(new ArrayList<String>())
                .build();

        EasemodGroupUtil easemodGroupUtil = new EasemodGroupUtil();
        String groupIDTmp = easemodGroupUtil.creatHXGroup(hxGroup);

        if (Strings.isNullOrEmpty(groupIDTmp)) {
            throw new BusinessException(ReturnCode.CODE_GROUP_CREAT_ERR, "创建群组失败,HX");
        }

        // 进行项目部消息推送用户注册
        String gpAssistant = NameHelper.makeGroupAssistantName(groupIDTmp);
        HXUser hxUserTmp = new HXUser.Builder()
                .withUsername(gpAssistant)
                .withNickname(reqGroupCreat.getGroupName())
                .withPassword(ConstantDefine.MSG_ASSISANT_PASSWORD)
                .build();
        EasemodUserUtil userUtil = new EasemodUserUtil();
        boolean regSuc = userUtil.registerHXSingleUser(hxUserTmp);

        if (!regSuc) {
            throw new BusinessException(ReturnCode.CODE_GROUP_CREAT_ERR, "创建群组失败,HX");
        }

        // 添加群组
        easemodGroupUtil = new EasemodGroupUtil();
        easemodGroupUtil.addGroupMember(groupIDTmp, new ArrayList<String>(reqGroupCreat.getGroupMember()));

        // 2.去保存信息
        GroupMsg groupMsg = reqGroupCreat.convertGroup();

        // 获取地址
        WeatherGetCityCodeEntity getCityCodeEntity = new WeatherGetCityCodeEntity.Builder()
                .withCity(reqGroupCreat.getCity())
                .withDistricts(reqGroupCreat.getDistrict())
                .withProvince(reqGroupCreat.getProvince())
                .build();
        String cityCodeTmp = weatherService.getCityCode(getCityCodeEntity);

        // 更新创建时的所有信息
        groupMsg.setCityCode(cityCodeTmp);
        groupMsg.setGroupId(groupIDTmp);
        groupMsg.setCreateDate(SKTools.getNowDate());
        groupMsgMapper.save(groupMsg);

        ArrayList<String> memList = new ArrayList<>(reqGroupCreat.getGroupMember());
        memList.add(reqGroupCreat.getGroupCreateUId());
        groupMemberService.addGroupMem(memList, groupIDTmp);

      /*
       激活时发送消息
      // 发送具体消息--必须赶在其他成员消息之后才发送这样才行
        groupSendMsgService.sendWelcomeMsgWhenCreate(groupMsg);

        // 3. 添加成员-->管理员、加所有成员--》同时给环信加数据
        ArrayList<String> memList = new ArrayList<>(reqGroupCreat.getGroupMember());
        memList.add(reqGroupCreat.getGroupCreateUId());
        groupMemberService.addGroupMem(memList, groupIDTmp);

        ArrayList<String> adminList = new ArrayList<>();
        adminList.add(reqGroupCreat.getGroupCreateUId());
        // 工程进度和质量安全的
        groupAdminService.addGroupAdminPermission(adminList, groupIDTmp, XZ_ADMIN_TYPE.Progress);
        groupAdminService.addGroupAdminPermission(adminList, groupIDTmp, XZ_ADMIN_TYPE.Quality);
        groupAdminService.addGroupAdminPermission(adminList, groupIDTmp, XZ_ADMIN_TYPE.Notice);

        // 总管理员
        groupAdminService.addGroupAdminLevel(groupIDTmp, reqGroupCreat.getGroupCreateUId(), XZ_ADMIN_TYPE.GroupTopAdmin);

        // 删除创建人
        memList.remove(reqGroupCreat.getGroupCreateUId());
        if (!CollectionUtil.isEmpty(memList)) {
            //  加入项目部
            groupSendMsgService.inviteAddInGroupSendToGroup(groupIDTmp, memList, reqGroupCreat.getGroupCreateUId());
        }
        */
        /**
         * 保存项目部的经纬度信息
         */
        Double longitude = reqGroupCreat.getLongitude();
        Double latitude = reqGroupCreat.getLatitude();
        if (latitude != null && longitude != null) {
            LocationInfo locationInfo = new LocationInfo.Builder()
                    .withLatitude(latitude)
                    .withLongitude(longitude)
                    .build();

            GroupLocation groupLocation;
            String address = StrUtil.format("{}{}{}{}", groupMsg.getProvince(), groupMsg.getCity(), groupMsg.getDistrict(), groupMsg.getAddress());
            String locationID;
            groupLocation = new GroupLocation.Builder()
                    .withGroupID(groupMsg.getGroupId())
                    .withLocation(locationInfo)
                    .withCreateDate(nowTime)
                    .withClientID(reqGroupCreat.getGroupCreateUId())
                    .withAddress(address)
                    .build();
            locationID = groupLocationDao.saveLocation(groupLocation);
            if (locationID != null) {
                groupLocation.setLocationID(locationID);
            }
            groupLocationDao.saveLocation(groupLocation);
        }


        return groupIDTmp;
    }

    /**
     * 激活群组
     */
    @Override
    @GroupInfoCacheUpdate
    public void activateGroup(ReqGroupZZBInfo reqGroupZZBInfo) throws BusinessException {
        String groupID = reqGroupZZBInfo.getGroupID();
        int isActivate = reqGroupZZBInfo.getIsActivate();
        GroupMsg groupMsg = groupMsgMapper.findByGroupId(groupID);
        if (groupMsg == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "不存在的项目部");

        }
        int postActivated = groupMsg.getPastActivated();

        //只有当以前没有激活过时,才需要发送消息
        if (postActivated == 0) {
            //1.4.0 脉盘添加 创建项目部脉盘文件
            mCloundService.createGroupAllFolder(groupMsg);
            List<GroupMember> groupList = groupMsg.getGroupMemberList();
            ArrayList<String> memList = new ArrayList<>();
            String groupCreateClientID = String.valueOf(groupMsg.getGroupCreateUId());
            memList.add(groupCreateClientID);
            if (CollectionUtil.isNotEmpty(groupList)) {
                for (GroupMember groupMember : groupList) {
                    memList.add(String.valueOf(groupMember.getGmsId()));
                }
            }
            SKTools.removeDuplicate(memList);
            // 发送具体消息--必须赶在其他成员消息之后才发送这样才行
            groupSendMsgService.sendWelcomeMsgWhenCreate(groupMsg);

            ArrayList<String> adminList = new ArrayList<>();
            adminList.add(groupCreateClientID);
            // 工程进度和质量安全的
            groupAdminService.addGroupAdminPermission(adminList, groupID, XZ_ADMIN_TYPE.Progress);
            groupAdminService.addGroupAdminPermission(adminList, groupID, XZ_ADMIN_TYPE.Quality);
            groupAdminService.addGroupAdminPermission(adminList, groupID, XZ_ADMIN_TYPE.Notice);

            // 总管理员
            groupAdminService.addGroupAdminLevel(groupID, groupCreateClientID, XZ_ADMIN_TYPE.GroupTopAdmin);

            // 删除创建人
            memList.remove(groupCreateClientID);
            if (!CollectionUtil.isEmpty(memList)) {
                //  加入项目部
                groupSendMsgService.inviteAddInGroupSendToGroup(groupID, memList, groupCreateClientID);
            }
        }

        //更新状态
        groupMsg.setIsActivate(isActivate);
        groupMsg.setPastActivated(1);
        groupMsgMapper.updateByGroupId(groupMsg);

    }

    /**
     * 更新组
     */
    @Override
    @GroupInfoCacheUpdate
    public void updateGroup(ReqGroupUpdate reqGroupUpdate) throws BusinessException {

        GroupMsg groupMsg = groupMsgMapper.findByGroupId(reqGroupUpdate.getGroupID());
        boolean needUpdateMcloudName = false;

        Long nowTime = SKTools.getNowTimeStamp();
        if (!Strings.isNullOrEmpty(reqGroupUpdate.getGroupImg())) {
            groupMsg.setGroupImg(reqGroupUpdate.getGroupImg());
        }
        if (!Strings.isNullOrEmpty(reqGroupUpdate.getGroupName())) {
            needUpdateMcloudName = groupMsg.getGroupName().equals(reqGroupUpdate.getGroupName()) ? false : true;
            groupMsg.setGroupName(reqGroupUpdate.getGroupName());
        }
        if (reqGroupUpdate.getGroupType() != 0) {
            groupMsg.setGroupType(reqGroupUpdate.getGroupType());
        }
        if (reqGroupUpdate.getSlogan() != null) {
            groupMsg.setSlogan(reqGroupUpdate.getSlogan());
        }

        if (reqGroupUpdate.getNotice() != null) {
            groupMsg.setNotice(reqGroupUpdate.getNotice());
        }

        if (!Strings.isNullOrEmpty(reqGroupUpdate.getProvince())) {
            groupMsg.setProvince(reqGroupUpdate.getProvince());
        }
        if (!Strings.isNullOrEmpty(reqGroupUpdate.getCity())) {
            groupMsg.setCity(reqGroupUpdate.getCity());
        }
        if (!Strings.isNullOrEmpty(reqGroupUpdate.getDistrict())) {
            groupMsg.setDistrict(reqGroupUpdate.getDistrict());
        }
        if (!Strings.isNullOrEmpty(reqGroupUpdate.getAddress())) {
            groupMsg.setAddress(reqGroupUpdate.getAddress());
        }
        if (reqGroupUpdate.getCertLevel() != 0) {
            groupMsg.setCertLevel(reqGroupUpdate.getCertLevel());
        }
        if (reqGroupUpdate.getSyncDataToMCloud() != null) { //是否同步数据到脉盘
            groupMsg.setSyncDataToMCloud(reqGroupUpdate.getSyncDataToMCloud());
        }

        // 1.3.2 项目部是否免验证拉入
        groupMsg.setAddIsFreeVerifi(reqGroupUpdate.getAddIsFreeVerifi());
        groupMsgMapper.updateByGroupId(groupMsg);

        //1.3.13 保存或者更新项目部的经纬度信息
        Double longitude = reqGroupUpdate.getLongitude();
        Double latitude = reqGroupUpdate.getLatitude();
        if (latitude != null && longitude != null) {
            LocationInfo locationInfo = new LocationInfo.Builder()
                    .withLatitude(latitude)
                    .withLongitude(longitude)
                    .build();

            GroupLocation groupLocation = groupLocationDao.getGroupLocation(groupMsg.getGroupId());
            String address = StrUtil.format("{}{}{}{}", groupMsg.getProvince(), groupMsg.getCity(), groupMsg.getDistrict(), groupMsg.getAddress());
            String locationID = null;
            if (groupLocation != null) {
                groupLocation.setUpdateDate(nowTime);
                groupLocation.setAddress(address);
                groupLocation.setClientID(reqGroupUpdate.getClientID());
                groupLocation.setLocation(locationInfo);
            } else {
                groupLocation = new GroupLocation.Builder()
                        .withGroupID(groupMsg.getGroupId())
                        .withLocation(locationInfo)
                        .withCreateDate(nowTime)
                        .withClientID(reqGroupUpdate.getClientID())
                        .withAddress(address)
                        .build();
                locationID = groupLocationDao.saveLocation(groupLocation);
            }
            if (locationID != null) {
                groupLocation.setLocationID(locationID);
            }
            groupLocationDao.saveLocation(groupLocation);
        }
        //  更新脉盘的名字
        mCloundService.updateGroupOrSubGroupFolderName(String.valueOf(groupMsg.getGroupCreateUId()), groupMsg);
    }

    /**
     * 更新组
     */
    @Override
    @GroupInfoCacheUpdate
    public void updateAddFreeVerifiGroup(ReqGroupUpdate reqGroupUpdate) throws BusinessException {

        GroupMsg groupMsg = groupMsgMapper.findByGroupId(reqGroupUpdate.getGroupID());

        // 1.3.2 项目部是否免验证拉入
        groupMsg.setAddIsFreeVerifi(reqGroupUpdate.getAddIsFreeVerifi());
        groupMsg.setSyncDataToMCloud(reqGroupUpdate.getSyncDataToMCloud());
        groupMsgMapper.updateByGroupId(groupMsg);
    }

    /**
     * 获取用户的所有的项目部
     */
    @Override
    public ArrayList<ResTinyGroup> listGroupByClientID(Integer clientID) throws BusinessException {
        if (clientID == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "clientID can not be null");

        }
        List<GroupMsg> list = groupMsgMapper.findGroupMsgByClientID(clientID);
        ArrayList<ResTinyGroup> returnList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(list)) {
            for (GroupMsg groupMsg : list) {
                ResTinyGroup resTinyGroup = new ResTinyGroup.Builder()
                        .withGroupID(groupMsg.getGroupId())
                        .withGroupImage(groupMsg.getGroupName())
                        .withGroupName(groupMsg.getGroupName())
                        .build();
                returnList.add(resTinyGroup);
            }
        }
        return returnList;
    }

    /**
     * 获取用户群组
     */
    @Override
    public ArrayList<RepGroupInfo> getUserGroupList(String clientID) throws BusinessException {

        if (Strings.isNullOrEmpty(clientID)) {
            throw new BusinessException(ReturnCode.CODE_GROUP_CLIENT_EMPTY_ERR, "client为空");

        }
        ArrayList<RepGroupInfo> repList = new ArrayList<>();

        List<GroupMsg> groupMsgList = new ArrayList<>();
        //获得是一个所有的群组
        Set<String> groups= userGroupRedisCacheService.findByClientID(clientID);
        if(CollectionUtil.isNotEmpty(groups)){
            for (String groupID:groups) {
                GroupMsg groupMsg = groupRedisCacheService.findByGroupID(groupID);
                groupMsgList.add(groupMsg);
            }
            List<GroupMsg> fileterGroupMsgList = fitleGroupMsg(groupMsgList);

            for (GroupMsg groupTmp : fileterGroupMsgList) {

                RepGroupInfo repGroupInfo = getOneGroup(groupTmp, clientID);

                if (repGroupInfo != null) {
                    repList.add(repGroupInfo);
                }
            }
        }
        return repList;
    }

    private List<GroupMsg> fitleGroupMsg(List<GroupMsg> groupMsgList) {
        List<GroupMsg> list = new ArrayList<GroupMsg>();

        HashMap<String, String> keyMap = new HashMap<>();

        for (GroupMsg groupMsg : groupMsgList) {

            String groupIDTmp = groupMsg.getGroupId();

            if (!keyMap.containsKey(groupIDTmp)) {
                list.add(groupMsg);
                keyMap.put(groupIDTmp, "1");
            }
        }
        return list;
    }


    /**
     * 获取用户群组
     */
    @Override
    public RepGroupInfo getOneGroup(String groupID, String clientID) throws BusinessException {

        GroupMsg groupMsg = groupMsgMapper.findByGroupId(groupID);

        if (groupMsg == null) {
            throw new BusinessException(ReturnCode.CODE_GROUP_NOTEXITS_ERR, "群组不存在");
        }

        return getOneGroup(groupMsg, clientID);
    }

    /**
     * 发送邀请短信
     */
    @Override
    public void sendInviteSms(ReqInviteSMS reqInviteSMS) throws BusinessException {

        if (CollectionUtil.isEmpty(reqInviteSMS.getSmsList())) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "检查参数");
        }

        for (ReqInviteSMSItem reqInviteSmsItem : reqInviteSMS.getSmsList()) {

            String pramar = StrUtil.format("{},{}", reqInviteSmsItem.getUserName(), reqInviteSMS.getGroupName());
            try {
                String inviteAddGroup = mainConfig.getYunzhixunConfig().getInviteAddGroup();
                SmsUtil.sendMsgInfo(reqInviteSmsItem.getPhoneNum(), inviteAddGroup, pramar);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 查询所有的群组倒序排列，0 未激活 1 激活 2 全部
     */
    @Override
    public ArrayList<RepConGroupInfoModel> findGroupMsgByZZB(int offset, int isActivate) {

        ArrayList<RepConGroupInfoModel> reusltAry = new ArrayList<>();

        List<GroupMsg> groupMsgList = groupMsgMapper.findGroupMsgByZZB(offset, isActivate);

        for (GroupMsg groumsgTmp : groupMsgList) {
            RepConGroupInfoModel infoTmp = RepConGroupInfoModel.makeRepConGroupInfoModel(groumsgTmp);
            int creatClientID = groumsgTmp.getGroupCreateUId();
            NewClient client = clientService.findById(creatClientID);
            infoTmp.setUpdateTime(versionRecordService.getGroupMemUpdateTime(groumsgTmp.getGroupId()));
            infoTmp.setCreateClientName(client.getUserName());

            reusltAry.add(infoTmp);
        }


        return reusltAry;
    }

    /**
     * 掌中宝获取项目组相关信息
     */
    @Override
    public ResGroupMsg getZZBGroupMsg(String groupID) throws BusinessException {
        if (StrUtil.isBlank(groupID)) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "不存在的项目部");
        }
        GroupMsg groupMsg = groupMsgMapper.findByGroupId(groupID);

        if (groupMsg == null) {
            throw new BusinessException(ReturnCode.CODE_CONTACTER_GROUPID_ERR, "没有这个Group");
        }

        int clientID = groupMsg.getGroupCreateUId();
        NewClient client = clientService.findById(clientID);

        RepGroupCert repGroupCert = getGroupCert(groupID);
        Date createDate = groupMsg.getCreateDate();
        long createTime = SKTools.convertDateToLong(createDate);
        return new ResGroupMsg.Builder()
                .withGroupID(groupMsg.getGroupId())
                .withCreateClientName(client.getUserName())
                .withGroupMsgID(String.valueOf(groupMsg.getGroupMsgId()))
                .withGroupCreateUId(String.valueOf(groupMsg.getGroupCreateUId()))
                .withGroupImg(groupMsg.getGroupImg())
                .withGroupName(groupMsg.getGroupName())
                .withIntroduction(groupMsg.getMemo())
                .withProvince(groupMsg.getProvince())
                .withCity(groupMsg.getCity())
                .withCreateTime(createTime)
                .withDistrict(groupMsg.getDistrict())
                .withAddress(groupMsg.getAddress())
                .withCityCode(groupMsg.getCityCode())
                .withGroupType(groupMsg.getGroupType())
                .withSlogan(groupMsg.getSlogan())
                .withNotice(groupMsg.getNotice())
                .withIsActivate(groupMsg.getIsActivate())
                .withGroupCert(repGroupCert)
                .build();
    }

    /**
     * 解散群组
     */
    @Override
    public void dismissGroup(ReqGroupDismiss reqGroupDismiss) throws BusinessException {

        String groupID = reqGroupDismiss.getGroupID();

        GroupMsg groupMsg = groupMsgMapper.findByGroupId(groupID);

        if (groupMsg == null) {
            throw new BusinessException(ReturnCode.CODE_CONTACTER_GROUPID_ERR, "没有这个Group");
        }

        if (groupMsg.getStatus().equals(1)) {
            throw new BusinessException(ReturnCode.CODE_GROUP_DISMISS_ERR, "该群组已经解散了");
        }

        //如果群组人数小于2 直接OK
        if (groupMsg.getGroupMemberList() != null &&
                groupMsg.getGroupMemberList().size() < 3) {
            // 已经删除了
            groupMsg.setStatus(1);
            groupMsgMapper.update(groupMsg);
            return;
        }

        // 是否正在解散
        boolean isDismissing = this.groupIsDismissing(groupID);
        if (isDismissing) {
            throw new BusinessException(ReturnCode.CODE_GROUP_DISMISS_ERR, "解散申请已经发送,请等待....");
        }

        long dismissTime = SKTools.getNowTimeStamp();

        //  需要记录解散时间
        GroupDismissInfo groupDismissInfo = new GroupDismissInfo.Builder()
                .withDismissMsg(reqGroupDismiss.getDismissMsg())
                .withDismissStatus(XZ_GROUP_DISMISS_STATUS.Dismissing.getValue())
                .withDismissTime(dismissTime)
                .withGroupID(groupID)
                .build();

        String dismissID = this.saveDissmissInfo(groupDismissInfo);

        if (StrUtil.isEmpty(dismissID)) {
            throw new BusinessException(ReturnCode.CODE_GROUP_DISMISS_ERR, "服务器内部错误，保存失败");
        }
        // 解散
        groupSendMsgService.sendDismissMsg(groupMsg, reqGroupDismiss.getDismissMsg(), dismissID, dismissTime);
    }

    /**
     * 附近项目部信息
     */
    @Override
    public ArrayList<NearbyGroupInfo> listNearbyGroup(ReqNearbyGroup reqNearbyGroup) throws BusinessException {
        if (reqNearbyGroup == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "对象不能为空");
        }

        Double longitude = reqNearbyGroup.getLongitude();
        Double latitude = reqNearbyGroup.getLatitude();
        if (latitude == null || longitude == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "经纬度不能为空");

        }
        String clientID = reqNearbyGroup.getClientID();

        ArrayList<GroupLocation> list = groupLocationDao.listNearbyGroup(longitude, latitude, MAX_NEAR_INSTANCE);
        ArrayList<NearbyGroupInfo> returnList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(list)) {
            for (GroupLocation groupLocation : list) {
                String groupID = groupLocation.getGroupID();
                GroupMsg groupMsg = groupMsgMapper.findByGroupId(groupID);
                //如果项目部没有激活的话,就跳过
                int isActive = groupMsg.getIsActivate();
                if (isActive == 0) {
                    continue;
                }
                List<GroupMember> memberList = groupMsg.getGroupMemberList();
                ArrayList<String> clientIDList = new ArrayList<>();
                for (GroupMember groupMember : memberList) {
                    clientIDList.add(groupMember.getGmsId() + "");
                }
                double longitude2 = groupLocation.getLocation().getLongitude();
                double latitude2 = groupLocation.getLocation().getLatitude();
                //计算距离
                double distance = MapDistinceUtil.calculateDistance(longitude, latitude, longitude2, latitude2);
                NearbyGroupInfo nearbyGroupInfo = groupLocation.makeNearbyGroupInfo(groupMsg);
                nearbyGroupInfo.setDistance(distance);
                //查看此人是否是此项目部的成员 默认不在,没有传递clientID,也不在此项目部
                int status = XZ_CLIENT_GROUP_STATUS.NOT_IN.getValue();
                if (StrUtil.isNotBlank(clientID)) {
                    NewClient client = clientService.findById(Integer.parseInt(clientID));
                    //判断此人是否在项目部
                    if (clientIDList.contains(clientID)) {
                        status = XZ_CLIENT_GROUP_STATUS.ALREADY_IN.getValue();
                    } else {
                        boolean isApply = hasApplying(client, groupMsg);
                        if (isApply) {
                            status = XZ_CLIENT_GROUP_STATUS.APPLYING.getValue();
                        }
                    }

                }
                nearbyGroupInfo.setStatus(status);
                returnList.add(nearbyGroupInfo);
            }
        }

        return returnList;
    }

    /**
     * 是否脉盘管理员
     */
    @Override
    public boolean isMcloudAdmin(String groupID, String clientID) throws BusinessException {

        if (StrUtil.isBlank(groupID) || StrUtil.isBlank(clientID)) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "clientID or groupID is empty");
        }


        boolean result = false;
        GroupPermission groupPermission = groupAdminService.getGroupPremission(groupID, clientID);
        if (groupPermission != null) {
            result = groupPermission.getMcloudLevel() > 0;
        }


        return result;
    }

    /**
     * 获取用户所有的项目部
     */
    @Override
    public ArrayList<ResTinyGroupInfo> listClientTinyGroupMsgInfo(String clientID) throws BusinessException {
        if (StrUtil.isBlank(clientID)) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "clientID can not be null");
        }
        ArrayList<ResTinyGroupInfo> resTinyGroupInfoList = new ArrayList<>();
        List<GroupMsg> list = groupMsgMapper.findUserGroupListWithUserID(Integer.parseInt(clientID));
        if (CollectionUtil.isNotEmpty(list)) {
            for (GroupMsg groupMsg : list) {
                String groupID = groupMsg.getGroupId();
                String position = "";
                ResTinyGroupInfo resTinyGroupInfo = new ResTinyGroupInfo.Builder()
                        .withGroupID(groupID)
                        .withGroupName(groupMsg.getGroupName())
                        .withGroupImg(groupMsg.getGroupImg())
                        .withPosition(position)
                        .build();
                resTinyGroupInfoList.add(resTinyGroupInfo);
            }
        }

        return resTinyGroupInfoList;
    }

    /**
     * 获取所有的项目部ID
     */
    @Override
    public List<String> listAllGroupIDs() {
        return groupMsgMapper.getAllGroupIDs();
    }

    /**
     * 获取项目部菜单
     */
    @Override
    public ArrayList<ResTinyMenu> getGroupShowMenuList(ReqClientGroupID reqClientGroupID) throws BusinessException {
        String clientID = reqClientGroupID.getClientID();
        String groupID = reqClientGroupID.getGroupID();
        if (StrUtil.isBlank(clientID) || StrUtil.isBlank(groupID)) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "client or groupID is empty");
        }
        ArrayList<ResTinyMenu> list = new ArrayList<>();
        //当前模块只有 5 施工计划 8 日志  7 签到
        ArrayList<Integer> showList = new ArrayList<>();
        showList.add(XZ_MODEL_TYPE.PLANING.getValue());
        showList.add(XZ_MODEL_TYPE.LOGGER.getValue());
        showList.add(XZ_MODEL_TYPE.SIGN_IN.getValue());

        ArrayList<PCMenuInfo> menuList = pcMenuService.listMenuByGroupID(groupID);
        for (PCMenuInfo menuInfo : menuList) {
            int modelType = menuInfo.getType();
            if (showList.contains(modelType)) {
                //获取是否为对应模块的管理员
                boolean isAdmin = groupAdminService.isGroupModelAdmin(clientID, groupID, modelType);
                if (isAdmin) {
                    ResTinyMenu resTinyMenu = new ResTinyMenu.Builder()
                            .withMenuImg(menuInfo.getMenuImg())
                            .withMenuName(menuInfo.getMenuName())
                            .withModelType(menuInfo.getType())
                            .withUnreadNum(0)
                            .withVersion(menuInfo.getVersion())
                            .build();
                    list.add(resTinyMenu);
                }
            }
        }
        SKTools.removeDuplicateKeepOrder(list);
        return list;
    }

    /**
     * 获取一个成员
     */
    private RepGroupInfo getOneGroup(GroupMsg groupTmp, String clientID) {

        RepGroupInfo repGroupInfo = RepGroupInfo.convertGroupMsgToRepGroupInfo(groupTmp);

        // 1.查询
        try {
            String groupIDTmp = groupTmp.getGroupId();

            // 职位
            GroupMember groupMemberTmp = groupMemberService.findByGroupIDAndClientID(groupIDTmp, clientID);
            if (groupMemberTmp != null) {
                repGroupInfo.setPosition(groupMemberTmp.getName());
            } else {
                repGroupInfo.setPosition("");
            }

            //  等级
            XZ_ADMIN_TYPE adminType = groupAdminService.getGroupAdminType(groupIDTmp, clientID);
            repGroupInfo.setAdminLevel(adminType.getValue());// 0 普通成员  10管理员  20 总管理员

            // 成员总数
            int groupMemberNum = groupMemberService.countByGroupId(groupTmp.getGroupId());
            repGroupInfo.setGroupMemNum(groupMemberNum);

            String posotionTmp = "";
            int isTopTmp = 0;
            int isMute = 0;

            // 免打扰等信息
            GroupMember groupMemTmp = groupMemberService.findByGroupIDAndClientID(groupIDTmp, clientID);
            if (groupMemTmp != null) {
                posotionTmp = groupMemTmp.getName();
                isTopTmp = groupMemTmp.getIsTop();
                isMute = groupMemTmp.getIsMute();
            }
            repGroupInfo.setPosition(posotionTmp);
            repGroupInfo.setIsTop(isTopTmp);
            repGroupInfo.setIsMute(isMute);

            // 用户权限
            GroupPermission groupPermissionTmp = groupAdminService.getGroupPremission(groupIDTmp, clientID);
            repGroupInfo.setPermission(groupPermissionTmp);

            // 未读数
            GroupUnread groupUnread = unreadService.getGroupAllUnread(groupIDTmp, clientID);
            repGroupInfo.setUnread(groupUnread);

        } catch (Exception exp) {
            exp.printStackTrace();
        }

        return repGroupInfo;
    }

    /**
     * 获取联系人item
     */
    private List<RepGroupMemItem> getGroupMemberList(String groupIDTmp) {
        List<RepGroupMemItem> returnList = new ArrayList<>();
        if (Strings.isNullOrEmpty(groupIDTmp)) {
            return returnList;
        }
        // 去获取最新的数据
        List<GroupMember> groupMemberList = groupMemberService.findListByGroupId(groupIDTmp);

        for (GroupMember groupMemTmp : groupMemberList) {

            RepGroupMemItem repGrpipMemItemTmp = RepGroupMemItem.makeRepGroupMemItem(groupMemTmp);
            // TODO  这块需要对管理员权限进行设计，等新版项目部搞完以后再做。
            if (null != repGrpipMemItemTmp) {
                repGrpipMemItemTmp.setAdminRole(0); //先是默认的
                returnList.add(repGrpipMemItemTmp);
            }
        }

        return returnList;
    }


    /**
     * 获取解散消息
     */
    @Override
    public GroupDismissInfo getDissmissInfo(String dismissID) {

        GroupDismissInfo groupDismiss = null;

        DBObject queryObj = new BasicDBObject();
        if (!StrUtil.isEmpty(dismissID)) {
            queryObj.put("_id", new ObjectId(dismissID));
        }
        // 保存
        DBCollection dbCollection = MongoConnFactory.getDBCollectionWithName(ConstantDefine.MONGO_COLLECTION_GROUPDISSOLVE_RECORD);
        // 查询
        DBCursor cursor = dbCollection.find(queryObj);

        List<DBObject> convid_Array = MongoConnFactory.toList(cursor);
        try {
            DBObject dbOBJTmp = convid_Array.get(0);
            groupDismiss = (GroupDismissInfo) SKTools.convertDBObjectToBean(dbOBJTmp, GroupDismissInfo.class);
            groupDismiss.setDismissID(dbOBJTmp.get("_id").toString());

        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return groupDismiss;
    }


    /**
     * 保存解散消息
     */
    private String saveDissmissInfo(GroupDismissInfo groupDismissInfo) {

        DBObject queryObj = SKTools.convertBeanToDBObject(groupDismissInfo);

        if (!StrUtil.isEmpty(groupDismissInfo.getDismissID())) {
            queryObj.put("_id", new ObjectId(groupDismissInfo.getDismissID()));
        }

        // 保存
        DBCollection dbCollection = MongoConnFactory.getDBCollectionWithName(ConstantDefine.MONGO_COLLECTION_GROUPDISSOLVE_RECORD);
        dbCollection.save(queryObj);
        String requestID = queryObj.get("_id").toString();

        return requestID;
    }


    /**
     * 获取一个MONGO
     */
    private DBCollection getDBCollection() {
        return MongoConnFactory.getDBCollectionWithName(ConstantDefine.MONGO_COLLECTION_GROUPAPPLY);
    }


}
