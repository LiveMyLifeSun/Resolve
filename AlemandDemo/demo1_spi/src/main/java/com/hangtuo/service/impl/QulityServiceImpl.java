package com.hangtuo.service.impl;

import com.google.common.base.Strings;

import com.hangtuo.common.Enum.XZ_BUILDING_COMMENT_TYPE;
import com.hangtuo.common.Enum.XZ_FILE_MODEL_TYPE;
import com.hangtuo.common.Enum.XZ_QULITY_DEGREE_TYPE;
import com.hangtuo.common.Enum.XZ_QULITY_REPLY_TYPE;
import com.hangtuo.common.Enum.XZ_QULITY_REQUEST_TYPE;
import com.hangtuo.common.Enum.XZ_QULITY_SEARCH_TYPE;
import com.hangtuo.common.Enum.XZ_QULITY_STATUS;
import com.hangtuo.common.Enum.XZ_QULITY_TO_TYPE;
import com.hangtuo.common.Enum.XZ_QULITY_TYPE;
import com.hangtuo.common.response.BusinessException;
import com.hangtuo.common.response.ReturnCode;
import com.hangtuo.dao.file.FileInfoDao;
import com.hangtuo.entity.Approval.NodeQulityInfo;
import com.hangtuo.entity.GroupMember;
import com.hangtuo.entity.GroupMsg;
import com.hangtuo.entity.NewClient;
import com.hangtuo.entity.Qulity.QulityClientItem;
import com.hangtuo.entity.Qulity.QulityClientRelation;
import com.hangtuo.entity.Qulity.QulityCommentItem;
import com.hangtuo.entity.Qulity.QulityItem;
import com.hangtuo.entity.Qulity.QulityReadUnreadItem;
import com.hangtuo.entity.Qulity.QulityReplyRectify;
import com.hangtuo.entity.Qulity.QulityZanItem;
import com.hangtuo.entity.Qulity.RepQulityListItem;
import com.hangtuo.entity.Qulity.ReqCreatQulity;
import com.hangtuo.entity.Qulity.ReqQulityList;
import com.hangtuo.entity.Qulity.ReqRectifyInfo;
import com.hangtuo.entity.Qulity.ReqSearchQulity;
import com.hangtuo.entity.config.MainConfig;
import com.hangtuo.entity.file.FileItem;
import com.hangtuo.entity.file.ReqFileItem;
import com.hangtuo.entity.message.file.DownloadMsgFile;
import com.hangtuo.service.GroupAdminService;
import com.hangtuo.service.GroupMemberService;
import com.hangtuo.service.GroupMsgService;
import com.hangtuo.service.GroupSendMsgService;
import com.hangtuo.service.MessageService;
import com.hangtuo.service.QulityService;
import com.hangtuo.service.UnreadService;
import com.hangtuo.service.client.NewClientService;
import com.hangtuo.util.idgenerator.IDGenerator;
import com.hangtuo.util.okhttputil.anewhttp.XZPostBuilder;
import com.hangtuo.util.okhttputil.anewhttp.exception.XZHTTPException;
import com.hangtuo.util.skutls.ConstantDefine;
import com.hangtuo.util.skutls.MongoConnFactory;
import com.hangtuo.util.skutls.SKTools;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.xiaoleilu.hutool.log.Log;
import com.xiaoleilu.hutool.log.LogFactory;
import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.StrUtil;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Resource;

/**
 * Created by jacksun on 17/1/4.
 *
 * 质量安全
 */

@Service("qulityServiceImpl")
public class QulityServiceImpl implements QulityService {

    private static final Log logger = LogFactory.get();
    private static MainConfig mainConfig = MainConfig.getInstance();
    private static String uploadURL = mainConfig.getCallNodeConfig().getExportURL() + "/qulity/create/file";
    private static String callBackUrl = mainConfig.getMustArriveConfig().getServerURL() + "/gouliaoweb-1.0/app/qulity/file/update";
    @Resource(name = "newClientServiceImpl")
    private NewClientService clientService;
    @Resource(name = "groupSendMsgServiceImpl")
    private GroupSendMsgService groupSendMsgService;
    @Resource(name = "groupMsgServiceImpl")
    private GroupMsgService groupMsgService;
    @Resource(name = "groupMemberServiceImpl")
    private GroupMemberService groupMemberService;
    @Resource(name = "groupAdminServiceImpl")
    private GroupAdminService groupAdminService;
    @Resource(name = "UnreadServiceImp")
    private UnreadService unreadService;
    @Resource(name = "fileInfoDaoImpl")
    private FileInfoDao fileInfoDao;
    @Resource(name = "messageServiceImpl")
    private MessageService messageService;
    @Resource(name = "newClientServiceImpl")
    private NewClientService newClientService;
    ////////////////////////////////////   创建   ///////////////////////////////////////////////////

    /**
     * 创建一个整改
     */
    @Override
    public void creatQulityRectify(ReqCreatQulity reqCreatQulity) throws BusinessException {

        // 1.创建索引数组
        ArrayList<QulityClientItem> ccClientList = new ArrayList<>();
        if (!CollectionUtil.isEmpty(reqCreatQulity.getCcClientIDS())) {
            for (String clientTmp : reqCreatQulity.getCcClientIDS()) {
                QulityClientItem qulityItem = new QulityClientItem.Builder()
                        .withClientID(clientTmp)
                        .build();
                ccClientList.add(qulityItem);
            }
        }

        long nowTime = SKTools.getNowTimeStamp();
        // 1.创建基本情况
        QulityItem qulityItem = new QulityItem.Builder()
                .withTitle(reqCreatQulity.getTitle())
                .withQulityType(reqCreatQulity.getQulityType())
                .withEndTime(reqCreatQulity.getEndTime()) // 整改器
                .withDetail(reqCreatQulity.getDetail())
                .withImgs(reqCreatQulity.getImgs())
                .withVerifyClientID(reqCreatQulity.getVerifyClientID())
                .withRectifyClientID(reqCreatQulity.getRectifyClientID())
                .withCcClientIDs(reqCreatQulity.getCcClientIDS())
                .withPostClientID(reqCreatQulity.getPostClientID())
                .withGroupID(reqCreatQulity.getGroupID())
                .withQulityStatus(XZ_QULITY_STATUS.Rectify_Wait.getValue()) // 首次为待整改状态
                .withIndexCCClientIDs(ccClientList)// 索引数组
                .withCreatTime(nowTime)
                .withLastReplyTime(0) // 第一次整整改就是0
                .withDegree(reqCreatQulity.getDegree()) // 严重等级1.3.3
                .build();
        // 2.保存基本情况
        String rectifyID = this.saveQulityItem(qulityItem);
        if (StrUtil.isEmpty(rectifyID)) {
            throw new BusinessException(ReturnCode.CODE_QULITY_CREAT_ERR, "创建失败");
        }
        // 更新验收ID
        qulityItem.setRectifyID(rectifyID);

        // 3.存抄送关系
        this.saveClientRelation(reqCreatQulity.getCcClientIDS(), reqCreatQulity.getGroupID(), rectifyID);

        //   4.更新未读数 除了创建者以外都是要+1
        // 验收者 未读不变
//        if (!qulityItem.getVerifyClientID().equals(qulityItem.getPostClientID())) {
//            // 创建人和验收人不相同才给验收者推送
//            unreadService.updateQulityUnreadNum(qulityItem.getVerifyClientID(), qulityItem.getGroupID(), rectifyID, XZ_QULITY_TO_TYPE.Verify);
//        }
        //  整改人
        unreadService.updateQulityUnreadNum(qulityItem.getRectifyClientID(), qulityItem.getGroupID(), rectifyID, XZ_QULITY_TO_TYPE.Rectify);
        // 抄送人
        for (String clientIDTmp : reqCreatQulity.getCcClientIDS()) {
            unreadService.updateQulityUnreadNum(clientIDTmp, qulityItem.getGroupID(), rectifyID, XZ_QULITY_TO_TYPE.CC);
        }

        // 5. 给整改人的
        groupSendMsgService.sendQulityMsgToRectifyClient(qulityItem);
        // 6. 给抄送人的
        groupSendMsgService.sendQulityMsgToCCClient(qulityItem);
    }

    /**
     * 获取质量安全的List
     */
    @Override
    public ArrayList<RepQulityListItem> getQulityList(ReqQulityList reqCreatQulity) throws BusinessException {

        ArrayList<RepQulityListItem> requlityList = new ArrayList<>();

        // 计算 offset
        int page = (reqCreatQulity.getPage() >= 0) ? reqCreatQulity.getPage() : 0;
        int count = (reqCreatQulity.getCount() >= 0) ? reqCreatQulity.getCount() : 12;

        // 如果page小于1 偏移就是0,如果大于1 则为成绩
        int offSet = (page < 1) ? 0 : page * count;

        // 基本查询信息
        DBObject searchCond = new BasicDBObject();
        searchCond.put("groupID", reqCreatQulity.getGroupID());

        // 严重等级
        if (reqCreatQulity.getDegree() != XZ_QULITY_DEGREE_TYPE.ALL.getValue()) {
            searchCond.put("degree", reqCreatQulity.getDegree());
        }

        // 0全部 1 质量 2安全  XZ_QULITY_TYPE
        if (reqCreatQulity.getQulityType() != XZ_QULITY_TYPE.ALL.getValue()) {
            searchCond.put("qulityType", reqCreatQulity.getQulityType());
        }

        long startTimeTmp = (reqCreatQulity.getStartTime() != 0) ? reqCreatQulity.getStartTime() : 0;
        long endTimeTmp = (reqCreatQulity.getEndTime() != 0) ? reqCreatQulity.getEndTime() : SKTools.getNowTimeStamp();

        // 对数据进行排序
        if (startTimeTmp > endTimeTmp) {
            long tmpTime = endTimeTmp;
            endTimeTmp = startTimeTmp;
            startTimeTmp = tmpTime;
        }

        // 因为Mongodb 不支持多or的条件，所以我们这快把or的数据传到最里面进行合并OR ，在最终查询的时候添加上即可
        BasicDBList orValues = new BasicDBList();
        orValues.add(new BasicDBObject("creatTime", new BasicDBObject("$gte", startTimeTmp)));
        orValues.add(new BasicDBObject("creatTime", new BasicDBObject("$lte", endTimeTmp)));

        // 判断
        XZ_QULITY_REQUEST_TYPE rqtype = XZ_QULITY_REQUEST_TYPE.valueOf(reqCreatQulity.getRequestType());

        //  待我整改 我整改的
        if (rqtype == XZ_QULITY_REQUEST_TYPE.Rectify_Wait ||
                rqtype == XZ_QULITY_REQUEST_TYPE.Rectify_DONE) {

            requlityList = getRectifyList(reqCreatQulity, offSet, count, searchCond, orValues);
        } else if (rqtype == XZ_QULITY_REQUEST_TYPE.Verify_Wait ||
                rqtype == XZ_QULITY_REQUEST_TYPE.Verify_DONE) {

            requlityList = getVerifyList(reqCreatQulity, offSet, count, searchCond, orValues);
        } else if (rqtype == XZ_QULITY_REQUEST_TYPE.Launch) {
            requlityList = getLanuchList(reqCreatQulity, offSet, count, searchCond, orValues);
        } else if (rqtype == XZ_QULITY_REQUEST_TYPE.CC) {
            requlityList = getCCList(reqCreatQulity, offSet, count, searchCond, orValues);
        } else {
            throw new BusinessException(ReturnCode.CODE_FAIL, "不支持的搜索");
        }

        // 组装数据
        for (RepQulityListItem itemTmp : requlityList) {

            String clientID = reqCreatQulity.getClientID();
            NewClient clientTmp = clientService.findById(Integer.valueOf(clientID));
            if (clientTmp != null) {
                itemTmp.setPostClientName(clientTmp.getUserName());
            }

            //  获取各种未读数据  是否为新， 是否评论 未读数 需要根据类型进行判断
            int commentUnreadNum = unreadService.getQulityCommentUnreadNum(clientID, itemTmp.getGroupID(), itemTmp.getRectifyID());
            // 这个只关心这个人所有的未读不区分任何数据
            int qulityUnreadNum = unreadService.getQulityUnreadNum(clientID, itemTmp.getGroupID(), itemTmp.getRectifyID(), null);

            //  增加具体的评论数目
            ArrayList<QulityCommentItem> commentList = getCommentList(itemTmp.getRectifyID());
            int commentNum = commentList.size();

            itemTmp.setCommentUnreadNum(commentUnreadNum);
            itemTmp.setUnreadNum(qulityUnreadNum);
            itemTmp.setCommentNum(commentNum);

            long lastReplayTime = itemTmp.getLastReplyTime();
            if (lastReplayTime == 0) {
                ArrayList<QulityReplyRectify> relyList = getRelyList(itemTmp.getRectifyID());
                if (CollectionUtil.isNotEmpty(relyList)) {
                    lastReplayTime = relyList.get(relyList.size() - 1).getCreatTime();
                    itemTmp.setLastReplyTime(lastReplayTime);
                }
            }

        }

        return requlityList;
    }

    /**
     * 获取质量安全的List
     */
    @Override
    public ArrayList<RepQulityListItem> searchQulityList(ReqSearchQulity reqSearchQulity) throws BusinessException {

        ArrayList<RepQulityListItem> repQulityList = new ArrayList<>();
        if (StrUtil.isEmpty(reqSearchQulity.getQueryStr())) {
            return repQulityList;
        }

        String queryWork = reqSearchQulity.getQueryStr();
        Pattern pattern = Pattern.compile("^.*" + queryWork + ".*$");
        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_QULITY);

        BasicDBObject searchCond = new BasicDBObject();
        searchCond.append("groupID", reqSearchQulity.getGroupID()); // 不需要ID
        // 严重等级
        if (reqSearchQulity.getDegree() != XZ_QULITY_DEGREE_TYPE.ALL.getValue()) {
            searchCond.append("degree", reqSearchQulity.getDegree());
        }

//        searchCond.append("title", pattern); // 同时搜索
//        searchCond.append("detail", pattern); // 同时搜索

        BasicDBList orValues = new BasicDBList();
        orValues.add(new BasicDBObject("title", pattern));
        orValues.add(new BasicDBObject("detail", pattern));

        searchCond.append("$or", orValues);

        XZ_QULITY_SEARCH_TYPE requestType = XZ_QULITY_SEARCH_TYPE.valueOf(reqSearchQulity.getSearchType());

        switch (requestType) {
            case Rectify: {
                //我整改
                searchCond.append("rectifyClientID", reqSearchQulity.getClientID()); // 同时搜索
                break;
            }
            case Verify: {
                // 我验收
                searchCond.append("verifyClientID", reqSearchQulity.getClientID()); // 同时搜索

                break;
            }
            case Launch: {
                // 我发起的
                searchCond.append("postClientID", reqSearchQulity.getClientID()); // 同时搜索

                break;
            }
            case CC: {
                // 抄送给我的
                searchCond.append("indexCCClientIDs.clientID", reqSearchQulity.getClientID()); // 同时搜索
                break;
            }
            default: {
                break;
            }
        }

        BasicDBObject sordCond = new BasicDBObject();
        sordCond.append("creatTime", 1);

        DBCursor cursor = null;
        if (dbCollection != null) {
            cursor = dbCollection.find(searchCond).sort(sordCond);
        }

        // 查询
        List<DBObject> rectifyListTmp = MongoConnFactory.toList(cursor);

        for (DBObject dbOBJTmp : rectifyListTmp) {

            RepQulityListItem reqItem = null;

            reqItem = (RepQulityListItem) SKTools.convertDBObjectToBean(dbOBJTmp, RepQulityListItem.class);
            if (reqItem != null) {

                reqItem.setRectifyID(dbOBJTmp.get("_id").toString());
                repQulityList.add(reqItem);
            }
        }

        // 组装数据s
        for (RepQulityListItem itemTmp : repQulityList) {

            String clientID = itemTmp.getPostClientID();
            NewClient clientTmp = clientService.findById(Integer.valueOf(clientID));
            if (clientTmp != null) {
                itemTmp.setPostClientName(clientTmp.getUserName());
            }
            //  获取各种未读数据
            int commentUnreadNum = unreadService.getQulityCommentUnreadNum(clientID, itemTmp.getGroupID(), itemTmp.getRectifyID());
            // 这个只关心这个人所有的未读不区分任何数据
            int qulityUnreadNum = unreadService.getQulityUnreadNum(clientID, itemTmp.getGroupID(), itemTmp.getRectifyID(), null);

            itemTmp.setCommentUnreadNum(commentUnreadNum);
            itemTmp.setUnreadNum(qulityUnreadNum);
            long lastReplayTime = itemTmp.getLastReplyTime();
            if (lastReplayTime == 0) {
                ArrayList<QulityReplyRectify> relyList = getRelyList(itemTmp.getRectifyID());
                if (CollectionUtil.isNotEmpty(relyList)) {
                    lastReplayTime = relyList.get(relyList.size() - 1).getCreatTime();
                    itemTmp.setLastReplyTime(lastReplayTime);
                }
            }

        }

        return repQulityList;
    }

    /**
     * 获取质量安全的List
     */
    @Override
    public ArrayList<QulityClientItem> getQulityClient(ArrayList<String> clients, String groupID) throws BusinessException {

        ArrayList<QulityClientItem> clinetIDs = new ArrayList<>();
        if (CollectionUtil.isEmpty(clients) || StrUtil.isEmpty(groupID)) {
            return clinetIDs;
        }
        // 去重
        SKTools.removeDuplicate(clients);

        for (String clientIDTmp : clients) {
            NewClient clientTmp = clientService.findById(Integer.valueOf(clientIDTmp));
            if (clientTmp != null) {

                // 工序名称和添加职位
                GroupMember queryGroupMem = new GroupMember();
                queryGroupMem.setGroupId(groupID);
                queryGroupMem.setGmsId(Integer.valueOf(clientIDTmp));
                GroupMember reultGroupmem = groupMemberService.findByGroupIDAndClientID(queryGroupMem);

                String postionTmp = "";
                if (reultGroupmem != null) {
                    postionTmp = (reultGroupmem.getName() == null) ? "" : reultGroupmem.getName();
                }

                QulityClientItem qulityClient = new QulityClientItem.Builder()
                        .withClientID(clientIDTmp)
                        .withImg(clientTmp.getImg())
                        .withUserName(clientTmp.getUserName())
                        .withPosition(postionTmp)
                        .build();

                clinetIDs.add(qulityClient);
            }
        }

        return clinetIDs;
    }

    /**
     * 获取质量安全具体信息
     */
    @Override
    public QulityItem getQulityInfo(ReqRectifyInfo reqRectifyInfo) throws BusinessException {

        // 1.先查出具体的数据

        String rectityID = reqRectifyInfo.getRectifyID();
        QulityItem qulityItem = getQulityItem(rectityID);
        if (qulityItem == null) {
            throw new BusinessException(ReturnCode.CODE_QULITY_NOT_FOUND_ERR, "不存在的整改");
        }

        // 2.查zanlist
        ArrayList<QulityZanItem> zanList = getZanList(rectityID);
        qulityItem.setZan(zanList);

        // 3.查comment数据
        ArrayList<QulityCommentItem> commentList = getCommentList(rectityID);
        qulityItem.setComment(commentList);

        // 4.整改跟帖
        ArrayList<QulityReplyRectify> relyList = getRelyList(rectityID);
        qulityItem.setReplyRectifyList(relyList);

        String clientID = reqRectifyInfo.getClientID();
        String groupID = reqRectifyInfo.getGroupID();
        //外部访问时，万能的clientID 和 groupID
        if (!ConstantDefine.WECHAT_SCAN_CLIENTID.equals(clientID) && !ConstantDefine.WECHAT_SCAN_GROUPID.equals(groupID)) {
            // 5. 清除各种未读数 直接清除不需要判断属于哪一个类型， 数组库自动删除
            unreadService.deleteQulityUnreadNum(clientID, groupID, rectityID, null);
            // 清除评论的未读
            unreadService.deleteQulityCommentUnreadNum(clientID, groupID, rectityID);
        }
        // 6.查询所有的client信息
        ArrayList<String> clientIDSTmp = new ArrayList<>();
        clientIDSTmp.add(qulityItem.getPostClientID());
        clientIDSTmp.add(qulityItem.getVerifyClientID());
        clientIDSTmp.add(qulityItem.getRectifyClientID());

        if (!CollectionUtil.isEmpty(qulityItem.getCcClientIDs())) {
            clientIDSTmp.addAll(qulityItem.getCcClientIDs());
        }

        ArrayList<QulityClientItem> clientList = getQulityClient(clientIDSTmp, reqRectifyInfo.getGroupID());
        qulityItem.setClientIDS(clientList);

        return qulityItem;
    }

    /**
     * 添加整改回复
     */
    @Override
    public void addReplyRectify(QulityReplyRectify qulityReplyRectify) throws BusinessException {

        if (qulityReplyRectify == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "内部错误");
        }

        // 不存在的整改
        String rectityID = qulityReplyRectify.getRectifyID();
        QulityItem qulityItem = getQulityItem(rectityID);
        if (qulityItem == null) {
            throw new BusinessException(ReturnCode.CODE_QULITY_NOT_FOUND_ERR, "不存在的整改");
        }

        long nowTime = SKTools.getNowTimeStamp();

        if (qulityReplyRectify.getReplyType() == XZ_QULITY_REPLY_TYPE.Rectify.getValue()) {
            // 整改回复

            // 判断具体的整改人和整改的问题
            if (!qulityReplyRectify.getPostClientID().equals(qulityItem.getRectifyClientID())) {
                throw new BusinessException(ReturnCode.CODE_QULITY_NOT_FOUND_ERR, "整改人不对");
            }

            NewClient pushClientTmp = clientService.findById(Integer.valueOf(qulityReplyRectify.getPostClientID()));

            if (pushClientTmp == null) {
                throw new BusinessException(ReturnCode.CODE_CONTACTER_VIREF_ERR, "用户ID有问题");
            }
            qulityReplyRectify.setPostClientName(pushClientTmp.getUserName());

            // 更新最后一次整改回复的时间
            qulityItem.setLastReplyTime(nowTime);
            //保存信息
            this.saveQulityItem(qulityItem);

        } else {
            // 验收回复

            // 判断具体的整改人和整改的问题
            if (!qulityReplyRectify.getPostClientID().equals(qulityItem.getVerifyClientID())) {
                throw new BusinessException(ReturnCode.CODE_QULITY_NOT_FOUND_ERR, "验收人不对");
            }

            NewClient pushClientTmp = clientService.findById(Integer.valueOf(qulityReplyRectify.getPostClientID()));

            if (pushClientTmp == null) {
                throw new BusinessException(ReturnCode.CODE_CONTACTER_VIREF_ERR, "用户ID有问题");
            }
            qulityReplyRectify.setPostClientName(pushClientTmp.getUserName());
        }

        // 设置时间
        qulityReplyRectify.setCreatTime(nowTime);

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_QULITY_REPLY);
        DBObject queryObj = SKTools.convertBeanToDBObject(qulityReplyRectify);

        if (dbCollection != null) {
            dbCollection.save(queryObj);
        }

        String requestID = queryObj.get("_id").toString();

        if (Strings.isNullOrEmpty(requestID)) {
            throw new BusinessException(ReturnCode.CODE_ZAN_SAVE_ERROR, "保存失败");
        }

        //  更新整改的状态
        if (qulityReplyRectify.getQulityStatus() == XZ_QULITY_STATUS.Unqualified.getValue()) {
            //验收不合格
            this.updateRectifyStatus(qulityReplyRectify.getRectifyID(), XZ_QULITY_STATUS.Rectify_Wait.getValue());

            // 先改成不合格，发消息，然后在更新状态。否则消息会出现错误
            qulityItem.setQulityStatus(XZ_QULITY_STATUS.Unqualified.getValue());
            //  发消息说重新整改
            groupSendMsgService.sendQulityRejectMsgToRectifyClient(qulityItem, qulityReplyRectify.getDetail());
            // 待验收
            qulityItem.setQulityStatus(XZ_QULITY_STATUS.Rectify_Wait.getValue());


        } else if (qulityReplyRectify.getQulityStatus() == XZ_QULITY_STATUS.Qualified.getValue()) {
            //验收合格 直接就是合格了
            this.updateRectifyStatus(qulityReplyRectify.getRectifyID(), XZ_QULITY_STATUS.Qualified.getValue());
            // 待验收
            qulityItem.setQulityStatus(XZ_QULITY_STATUS.Qualified.getValue());

            // 整改人和抄送人回复
            groupSendMsgService.sendQulityVerfiedMsgToRectifyClient(qulityItem, qulityReplyRectify.getDetail());
            groupSendMsgService.sendQulityVerfiedMsgToCCClient(qulityItem, qulityReplyRectify.getDetail());

        } else if (qulityReplyRectify.getQulityStatus() == XZ_QULITY_STATUS.Verify_Wait.getValue()) {
            //  整改回复    就变成，待验收
            this.updateRectifyStatus(qulityReplyRectify.getRectifyID(), XZ_QULITY_STATUS.Verify_Wait.getValue());

            // 待验收
            qulityItem.setQulityStatus(XZ_QULITY_STATUS.Verify_Wait.getValue());
            //  给验收人发消息 等待验收了
            groupSendMsgService.sendQulityRectifiedMsgToVerifyClient(qulityItem, qulityReplyRectify.getDetail());

        } else {
            //验收合格
            throw new BusinessException(ReturnCode.CODE_ZAN_SAVE_ERROR, "请检查状态");
        }

        // 更改未读数
        // 判断整改人还是验收人
        if (qulityReplyRectify.getReplyType() == XZ_QULITY_REPLY_TYPE.Rectify.getValue()) {

            // 整改回复
            // 验收者 未读+1
            if (!qulityItem.getVerifyClientID().equals(qulityItem.getPostClientID())) {
                // 创建人和验收人不相同才给验收者推送
                unreadService.updateQulityUnreadNum(qulityItem.getVerifyClientID(), qulityItem.getGroupID(), rectityID, XZ_QULITY_TO_TYPE.Verify);
            }
            //  发起人+1
            unreadService.updateQulityUnreadNum(qulityItem.getPostClientID(), qulityItem.getGroupID(), rectityID, XZ_QULITY_TO_TYPE.Launch);
            // 抄送人
            for (String clientIDTmp : qulityItem.getCcClientIDs()) {
                unreadService.updateQulityUnreadNum(clientIDTmp, qulityItem.getGroupID(), rectityID, XZ_QULITY_TO_TYPE.CC);
            }
        } else if (qulityReplyRectify.getReplyType() == XZ_QULITY_REPLY_TYPE.Verify.getValue()) {
            // 验收回复

            //  发起人+1
            if (!qulityItem.getVerifyClientID().equals(qulityItem.getPostClientID())) {
                unreadService.updateQulityUnreadNum(qulityItem.getPostClientID(), qulityItem.getGroupID(), rectityID, XZ_QULITY_TO_TYPE.Launch);
            }
            //  整改人 +1
            unreadService.updateQulityUnreadNum(qulityItem.getRectifyClientID(), qulityItem.getGroupID(), rectityID, XZ_QULITY_TO_TYPE.Rectify);
            // 抄送人 +1
            for (String clientIDTmp : qulityItem.getCcClientIDs()) {
                unreadService.updateQulityUnreadNum(clientIDTmp, qulityItem.getGroupID(), rectityID, XZ_QULITY_TO_TYPE.CC);
            }
        }

    }

    /**
     * 获取整改回复list
     */
    @Override
    public ArrayList<QulityReplyRectify> getRelyList(String rectifyID) throws BusinessException {

        ArrayList<QulityReplyRectify> replyList = new ArrayList<>();

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_QULITY_REPLY);

        DBObject queryObj = new BasicDBObject();
        queryObj.put("rectifyID", rectifyID);

        // 时间倒序拍
        BasicDBObject sort = new BasicDBObject().append("creatTime", 1);

        // 查询
        DBCursor cursor = dbCollection.find(queryObj).sort(sort);
        List<DBObject> rectifyListTmp = MongoConnFactory.toList(cursor);

        for (DBObject dbOBJTmp : rectifyListTmp) {

            QulityReplyRectify qulityReplyRectify = null;

            qulityReplyRectify = (QulityReplyRectify) SKTools.convertDBObjectToBean(dbOBJTmp, QulityReplyRectify.class);
            if (qulityReplyRectify != null) {
                qulityReplyRectify.setRectifyID(dbOBJTmp.get("_id").toString());
                replyList.add(qulityReplyRectify);
            }
        }

        return replyList;
    }

    /**
     * 添加赞
     */
    @Override
    public String addZan(QulityZanItem qulityZanItem) throws BusinessException {

        if (qulityZanItem == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "内部错误");
        }

        String clientIDTmp = qulityZanItem.getPushClientID();
        NewClient clientTmp = clientService.findById(Integer.valueOf(clientIDTmp));

        if (clientTmp == null) {
            throw new BusinessException(ReturnCode.CODE_CONTACTER_VIREF_ERR, "用户ID有问题");
        }

        // 老的赞obj
        QulityZanItem oldZanObj = getZanObject(qulityZanItem);
        if (oldZanObj != null && oldZanObj.getZanID() != null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "不能重复点赞");
        }

        // 设置用户名称 时间
        qulityZanItem.setPushClientName(clientTmp.getUserName());
        qulityZanItem.setCreatTime(SKTools.getNowTimeStamp());

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_QULITY_ZAN);

        DBObject queryObj = SKTools.convertBeanToDBObject(qulityZanItem);

        if (dbCollection != null) {
            dbCollection.save(queryObj);
        }

        String requestID = queryObj.get("_id").toString();

        if (Strings.isNullOrEmpty(requestID)) {
            throw new BusinessException(ReturnCode.CODE_ZAN_SAVE_ERROR, "保存失败");
        }

        return requestID;
    }

    /**
     * 获取赞的list
     */
    @Override
    public ArrayList<QulityZanItem> getZanList(String rectifyID) throws BusinessException {

        ArrayList<QulityZanItem> zanList = new ArrayList<>();

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_QULITY_ZAN);

        DBObject queryObj = new BasicDBObject();
        queryObj.put("rectifyID", rectifyID);

        // 时间倒序拍
        BasicDBObject sort = new BasicDBObject().append("creatTime", 1);

        // 查询
        DBCursor cursor = dbCollection.find(queryObj).sort(sort);
        List<DBObject> rectifyListTmp = MongoConnFactory.toList(cursor);

        for (DBObject dbOBJTmp : rectifyListTmp) {

            QulityZanItem qulityZanItem = null;

            qulityZanItem = (QulityZanItem) SKTools.convertDBObjectToBean(dbOBJTmp, QulityZanItem.class);
            if (qulityZanItem != null) {

                qulityZanItem.setZanID(dbOBJTmp.get("_id").toString());
                zanList.add(qulityZanItem);
            }
        }

        return zanList;
    }

    /**
     * 添加评论，此处不需要判断是否评论过，这块直接加上
     */
    @Override
    public QulityCommentItem addComment(QulityCommentItem qulityCommentItem) throws BusinessException {

        if (qulityCommentItem == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "内部错误");
        }
        int type = qulityCommentItem.getType();

        NewClient fromClientTmp = clientService.findById(Integer.valueOf(qulityCommentItem.getFromID()));
        NewClient toClientTmp = null;
        if (!StrUtil.isEmpty(qulityCommentItem.getToID())) {
            toClientTmp = clientService.findById(Integer.valueOf(qulityCommentItem.getToID()));
            // 更新名称
            qulityCommentItem.setToName(toClientTmp.getUserName());
        } else {
            qulityCommentItem.setToName("");
        }

        if (fromClientTmp == null) {
            throw new BusinessException(ReturnCode.CODE_CONTACTER_VIREF_ERR, "用户ID有问题");
        }
        // 更新名称
        qulityCommentItem.setFromName(fromClientTmp.getUserName());

        // 回复的话需要对数据进行判断
        if (type == XZ_BUILDING_COMMENT_TYPE.Reply.getValue()) {

            if (toClientTmp == null) {
                throw new BusinessException(ReturnCode.CODE_CONTACTER_VIREF_ERR, "用户ID有问题");
            }
        }

        // 更新时间
        qulityCommentItem.setCreatTime(SKTools.getNowTimeStamp());

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_QULITY_COMMENT);
        DBObject queryObj = SKTools.convertBeanToDBObject(qulityCommentItem);

        if (dbCollection != null) {
            dbCollection.save(queryObj);
        }

        String requestID = queryObj.get("_id").toString();

        if (Strings.isNullOrEmpty(requestID)) {
            throw new BusinessException(ReturnCode.CODE_ZAN_SAVE_ERROR, "保存失败");
        }

        //  没看评论的人全部未读+1
        String rectityID = qulityCommentItem.getRectifyID();
        QulityItem qulityItem = getQulityItem(rectityID);
        if (qulityItem == null) {
            throw new BusinessException(ReturnCode.CODE_QULITY_NOT_FOUND_ERR, "不存在的整改");
        }
        // 6.查询所有的client信息
        ArrayList<String> clientIDSTmp = new ArrayList<>();
        clientIDSTmp.add(qulityItem.getPostClientID());
        clientIDSTmp.add(qulityItem.getVerifyClientID());
        //当验收人没有收到验收消息时，评论不发送给他,这里通过状态不是待整改,才给验收人评论消息
        if (!(qulityItem.getQulityStatus() == XZ_QULITY_STATUS.Rectify_Wait.getValue())) {
            clientIDSTmp.add(qulityItem.getRectifyClientID());
        }
        if (!CollectionUtil.isEmpty(qulityItem.getCcClientIDs())) {
            clientIDSTmp.addAll(qulityItem.getCcClientIDs());
        }

        SKTools.removeDuplicate(clientIDSTmp);

        for (String clientIDTmp : clientIDSTmp) {
            // 如果发帖人相同就直接跳过了
            if (qulityCommentItem.getFromID().equals(clientIDTmp)) {
                continue;
            }
            // 所有人直接+1
            unreadService.updateQulityCommentUnreadNum(clientIDTmp, qulityItem.getGroupID(), qulityItem.getRectifyID());
        }

        return qulityCommentItem;
    }

    /**
     * 获取评论的list
     */
    @Override
    public ArrayList<QulityCommentItem> getCommentList(String rectifyID) throws BusinessException {

        ArrayList<QulityCommentItem> commentList = new ArrayList<>();

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_QULITY_COMMENT);

        DBObject queryObj = new BasicDBObject();
        queryObj.put("rectifyID", rectifyID);

        // 时间倒序拍
        BasicDBObject sort = new BasicDBObject().append("creatTime", 1);

        // 查询
        DBCursor cursor = dbCollection.find(queryObj).sort(sort);
        List<DBObject> rectifyListTmp = MongoConnFactory.toList(cursor);

        for (DBObject dbOBJTmp : rectifyListTmp) {

            QulityCommentItem qulityCommentItem = null;

            qulityCommentItem = (QulityCommentItem) SKTools.convertDBObjectToBean(dbOBJTmp, QulityCommentItem.class);
            if (qulityCommentItem != null) {

                qulityCommentItem.setRectifyID(dbOBJTmp.get("_id").toString());
                commentList.add(qulityCommentItem);
            }
        }

        return commentList;
    }

    /**
     * 获取未读列表
     */
    @Override
    public QulityReadUnreadItem getQulityReadUnreadClient(String rectifyID) throws BusinessException {

        if (StrUtil.isEmpty(rectifyID)) {
            throw new BusinessException(ReturnCode.CODE_QULITY_NOT_FOUND_ERR, "不存在的整改");
        }

        QulityItem qulityItem = getQulityItem(rectifyID);

        if (qulityItem == null) {
            throw new BusinessException(ReturnCode.CODE_QULITY_NOT_FOUND_ERR, "不存在的整改");
        }

        ArrayList<String> unreadClientIDList = new ArrayList<>();
        ArrayList<String> readClientIDList = new ArrayList<>();

        // 6.查询所有的client信息
        ArrayList<String> clientIDSTmp = new ArrayList<>();
        clientIDSTmp.add(qulityItem.getPostClientID());
        clientIDSTmp.add(qulityItem.getVerifyClientID());
        clientIDSTmp.add(qulityItem.getRectifyClientID());
        if (!CollectionUtil.isEmpty(qulityItem.getCcClientIDs())) {
            clientIDSTmp.addAll(qulityItem.getCcClientIDs());
        }

        // 是否已读未读
        for (String clientIDTmp : clientIDSTmp) {

            // 如果是创建人
            if (qulityItem.getPostClientID().equals(clientIDTmp)) {
                continue;
            }

            boolean isUnRead = unreadService.isQulityUnRead(clientIDTmp, qulityItem.getGroupID(), qulityItem.getRectifyID());

            if (isUnRead) {
                unreadClientIDList.add(clientIDTmp);
            } else {
                readClientIDList.add(clientIDTmp);
            }
        }

        ArrayList<QulityClientItem> unreadClientTmp = getQulityClient(unreadClientIDList, qulityItem.getGroupID());
        ArrayList<QulityClientItem> readClientTmp = getQulityClient(readClientIDList, qulityItem.getGroupID());

        QulityReadUnreadItem qulityUnread = new QulityReadUnreadItem();
        qulityUnread.setUnreadList(unreadClientTmp);
        qulityUnread.setReadList(readClientTmp);

        return qulityUnread;
    }

    /**
     * 调用node,生成excle
     */
    @Override
    public void makeQulityExcel(ReqRectifyInfo reqRectifyInfo) throws BusinessException {
        if (null == reqRectifyInfo) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "对象不能为空");
        }
        String rectifyID = reqRectifyInfo.getRectifyID();
        QulityItem qulityItem = this.getQulityInfo(reqRectifyInfo);
        NodeQulityInfo nodeQulityInfo = this.makeNodeQulityInfo(rectifyID, reqRectifyInfo.getClientID(), reqRectifyInfo.getGroupID());
        XZPostBuilder postBuilder = new XZPostBuilder()
                .addRequestURL(uploadURL)
                .addTag(QulityServiceImpl.class)
                .addJsonData(nodeQulityInfo);
        logger.info("调用node生成地址为{},回调地址为{}", uploadURL, callBackUrl);
        try {
            // 同步调用
            String result = postBuilder.syncOutRequest(String.class);
        } catch (XZHTTPException e) {
            logger.error(e.toString());
            throw new BusinessException(ReturnCode.CODE_FAIL, "请求失败");
        }
    }

    /**
     * node回传数据,发送消息给客户端
     */
    @Override
    public String saveUploadFileAndSendMsg(ReqFileItem reqFileItem) throws BusinessException {
        if (null == reqFileItem) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "对象不能为空");
        }
        long nowTime = SKTools.getNowTimeStamp();
        int status = reqFileItem.getStatus();
        if (status == -1) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "导出失败,请稍后再试");

        }

        //默认7天有效期
        String rectifyID = reqFileItem.getRectifyID();
        QulityItem qulityItem = this.getQulityItem(rectifyID);

        if (qulityItem == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "不存在整改");

        }

        String fileName = StrUtil.format("{}{}", qulityItem.getTitle(), IDGenerator.getNextID());

        long deadTime = nowTime + 7 * 24 * 60 * 60 * 1000;
        FileItem fileItem = reqFileItem.makeFileItem();

        String downloadUrl = fileItem.getDownloadURL();
        logger.info("node回调downloadURL为{}", downloadUrl);
        fileName = StrUtil.format("{}{}", fileName, SKTools.getFileTypeByStr(downloadUrl));
        fileItem.setCreateDate(nowTime);
        fileItem.setDeadTime(deadTime);
        fileItem.setFileName(fileName);
        fileItem.setTitle(fileName);
        String fileID = fileInfoDao.saveFileInfo(fileItem);
        fileItem.setFileID(fileID);

        qulityItem.setFileID(fileID);
        this.saveQulityItem(qulityItem);
        /**
         * http://dcsapi.com?k=180862506&url=http://glimgdev.gouliao.cn/plan_6f3d22055cc6c65b5d8f84a38bda4368.xlsx
         */

        String shareURL = StrUtil.format("{}/files/{}", mainConfig.getMustArriveConfig().getQrcodeCallback(), fileID);
        String key = "180862506";
        String previewURL = StrUtil.format("http://dcsapi.com?k={}&url={}", key, downloadUrl);

        fileItem.setShareURL(shareURL);
        fileItem.setPreviewURL(previewURL);
        fileInfoDao.saveFileInfo(fileItem);
        String postClientID = fileItem.getPostClientID();
        NewClient sendToclient = newClientService.findById(Integer.parseInt(postClientID));

        //发送文件消息
        String groupID = qulityItem.getGroupID();
        GroupMsg groupMsg = groupMsgService.findByGroupId(groupID);
        DownloadMsgFile downloadMsgFile = new DownloadMsgFile.Builder()
                .withFileID(fileID)
                .withPostClientID(postClientID)
                .withCardDetail("")
                .withCardTitle(fileName)
                .withFileName(fileName)
                .withFileType(fileItem.getFileType())
                .withSize(fileItem.getSize())
                .withModelType(XZ_FILE_MODEL_TYPE.XZ_MODULE_TYPE_QUALITY_SAFE.getValue())
                .build();

        messageService.sendFileMsg(downloadMsgFile, groupMsg, sendToclient);

        return fileID;
    }

    /**
     * 获取文件信息
     */
    @Override
    public FileItem getFileItem(ReqFileItem reqFileItem) throws BusinessException {
        String fileID = reqFileItem.getFileID();
        if (StrUtil.isBlank(fileID)) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "fileID 不能为空");

        }
        FileItem fileItem = fileInfoDao.getFileInfo(fileID);
        return fileItem;
    }

    private NodeQulityInfo makeNodeQulityInfo(String rectifyID, String clientID, String groupID) throws BusinessException {
        if (StrUtil.isBlank(rectifyID)) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "不存在的整改");

        }
        long nowTime = SKTools.getNowTimeStamp();
        String excelName = StrUtil.format("{}{}", rectifyID, nowTime);
        String md5Str = SKTools.getMD5(excelName);
        String finalStr = StrUtil.format("{}{}", "rectify_", md5Str);

        // String callBackUrl = "http://192.168.1.126:8080"+ "/gouliaoweb-1.0/app/qulity/file/update";

        return new NodeQulityInfo.Builder()
                .withCallbackURL(callBackUrl)
                .withFileName(finalStr)
                .withRectifyID(rectifyID)
                .withPostClientID(clientID)
                .withGroupID(groupID)
                .build();
    }


    ////////////////////////////////////  列表查询   ///////////////////////////////////////////////////


    /**
     * 待我整改 我整改的   待我整改传值，如果是全部的话  QulityStatus传0 其他不变
     */
    private ArrayList<RepQulityListItem> getRectifyList(ReqQulityList reqCreatQulity, int offset, int count, DBObject searchCond, BasicDBList orValues) throws BusinessException {

        if (searchCond == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "搜索对象为空");
        }

        ArrayList<RepQulityListItem> requlityList = new ArrayList<>();

        // 设置整改人ID
        searchCond.put("rectifyClientID", reqCreatQulity.getClientID());

        // XZ_QULITY_STATUS 如果是 0  就是根据待我整改还是我整改的  如果是1 待整改 包括待整改和验证失败 如果是2 待验收也直接弄一个就行，  如果是4 验收合格

        // XZ_QULITY_STATUS  // 0.全部 不添加
        if (reqCreatQulity.getQulityStatus() == XZ_QULITY_STATUS.ALL.getValue()) {

            // 根据待我整改  分为待整改和验证失败的
            if (reqCreatQulity.getRequestType() == XZ_QULITY_REQUEST_TYPE.Rectify_Wait.getValue()) {

                BasicDBList statusValues = new BasicDBList();
                statusValues.add(new BasicDBObject("qulityStatus", XZ_QULITY_STATUS.Unqualified.getValue()));
                statusValues.add(new BasicDBObject("qulityStatus", XZ_QULITY_STATUS.Rectify_Wait.getValue()));

                BasicDBList andValues = new BasicDBList();
                andValues.add(new BasicDBObject("$or", statusValues));
                andValues.add(new BasicDBObject("$and", orValues));

                // 把or添加进来
                searchCond.put("$and", andValues);

            } else if (reqCreatQulity.getRequestType() == XZ_QULITY_REQUEST_TYPE.Rectify_DONE.getValue()) {
                // 我整改的 分为待验收和验收合格的

                BasicDBList statusValues = new BasicDBList();
                statusValues.add(new BasicDBObject("qulityStatus", XZ_QULITY_STATUS.Verify_Wait.getValue()));
                statusValues.add(new BasicDBObject("qulityStatus", XZ_QULITY_STATUS.Qualified.getValue()));

                BasicDBList andValues = new BasicDBList();
                andValues.add(new BasicDBObject("$or", statusValues));
                andValues.add(new BasicDBObject("$and", orValues));

                // 把or添加进来
                searchCond.put("$and", andValues);

            } else {
                throw new BusinessException(ReturnCode.CODE_FAIL, "不支持的搜索");
            }
        } else if (reqCreatQulity.getQulityStatus() == XZ_QULITY_STATUS.Rectify_Wait.getValue()) {
            //  待我整改 分为待整改和验证失败的

            BasicDBList statusValues = new BasicDBList();
            statusValues.add(new BasicDBObject("qulityStatus", XZ_QULITY_STATUS.Unqualified.getValue()));
            statusValues.add(new BasicDBObject("qulityStatus", XZ_QULITY_STATUS.Qualified.getValue()));

            BasicDBList andValues = new BasicDBList();
            andValues.add(new BasicDBObject("$or", statusValues));
            andValues.add(new BasicDBObject("$and", orValues));

            // 把or添加进来
            searchCond.put("$and", andValues);

        } else if (reqCreatQulity.getQulityStatus() == XZ_QULITY_STATUS.Verify_Wait.getValue() ||
                reqCreatQulity.getQulityStatus() == XZ_QULITY_STATUS.Qualified.getValue()) {
            // 我整改的 待验收的 直接区分就行
            searchCond.put("qulityStatus", reqCreatQulity.getQulityStatus());
        }

        // 把or添加进来
//        searchCond.put("$or", orValues);

        return getQulityList(searchCond, count, offset);
    }


    /**
     * 待我验收 我验收的
     */
    private ArrayList<RepQulityListItem> getVerifyList(ReqQulityList reqCreatQulity, int offset, int count, DBObject searchCond, BasicDBList orValues) throws BusinessException {

        if (searchCond == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "搜索对象为空");
        }

        ArrayList<RepQulityListItem> requlityList = new ArrayList<>();

        // 设置验收人ID
        searchCond.put("verifyClientID", reqCreatQulity.getClientID());

        // XZ_QULITY_STATUS  // 0.全部 不添加
        if (reqCreatQulity.getQulityStatus() == XZ_QULITY_STATUS.ALL.getValue()) {

            // 待我验收
            if (reqCreatQulity.getRequestType() == XZ_QULITY_REQUEST_TYPE.Verify_Wait.getValue()) {

                searchCond.put("qulityStatus", XZ_QULITY_STATUS.Verify_Wait.getValue());

            } else if (reqCreatQulity.getRequestType() == XZ_QULITY_REQUEST_TYPE.Verify_DONE.getValue()) {
                // 我验收的的 分为待验收和验收合格的
                BasicDBList statusValues = new BasicDBList();
                statusValues.add(new BasicDBObject("qulityStatus", XZ_QULITY_STATUS.Verify_Wait.getValue()));
                statusValues.add(new BasicDBObject("qulityStatus", XZ_QULITY_STATUS.Qualified.getValue()));

                BasicDBList andValues = new BasicDBList();
                andValues.add(new BasicDBObject("$or", statusValues));
                andValues.add(new BasicDBObject("$and", orValues));

                // 把or添加进来
                searchCond.put("$and", andValues);
            } else {
                throw new BusinessException(ReturnCode.CODE_FAIL, "不支持的搜索");
            }
        } else if (reqCreatQulity.getQulityStatus() == XZ_QULITY_STATUS.Verify_Wait.getValue()) {
            // 待我验收的
            searchCond.put("qulityStatus", XZ_QULITY_STATUS.Verify_Wait.getValue());
        } else if (reqCreatQulity.getQulityStatus() == XZ_QULITY_STATUS.Qualified.getValue()) {
            // 我验收的
            BasicDBList statusValues = new BasicDBList();
//            statusValues.add(new BasicDBObject("qulityStatus", XZ_QULITY_STATUS.Rectify_Wait.getValue()));
            statusValues.add(new BasicDBObject("qulityStatus", XZ_QULITY_STATUS.Qualified.getValue()));

            BasicDBList andValues = new BasicDBList();
            andValues.add(new BasicDBObject("$or", statusValues));
            andValues.add(new BasicDBObject("$and", orValues));

            // 把or添加进来
            searchCond.put("$and", andValues);
        } else {
            throw new BusinessException(ReturnCode.CODE_FAIL, "不支持的搜索");
        }

//        // 把or添加进来
//        searchCond.put("$or", orValues);

        return getQulityList(searchCond, count, offset);
    }


    /**
     * 我发起的
     */
    private ArrayList<RepQulityListItem> getLanuchList(ReqQulityList reqCreatQulity, int offset, int count, DBObject searchCond, BasicDBList orValues) throws BusinessException {

        if (searchCond == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "搜索对象为空");
        }

        // 设置验收人ID
        searchCond.put("postClientID", reqCreatQulity.getClientID());

        // XZ_QULITY_STATUS  0 是全部的话全部都进行搜索
        if (reqCreatQulity.getQulityStatus() != XZ_QULITY_STATUS.ALL.getValue()) {
            searchCond.put("qulityStatus", reqCreatQulity.getQulityStatus());
        }

        // 把or添加进来
        searchCond.put("$and", orValues);

        return getQulityList(searchCond, count, offset);
    }


    /**
     * 抄送给我的
     */
    private ArrayList<RepQulityListItem> getCCList(ReqQulityList reqCreatQulity, int offset, int count, DBObject searchCond, BasicDBList orValues) throws BusinessException {

        if (searchCond == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "搜索对象为空");
        }

        // 设置抄送人ID
        searchCond.put("indexCCClientIDs.clientID", reqCreatQulity.getClientID());

        // XZ_QULITY_STATUS  0 是全部的话全部都进行搜索
        if (reqCreatQulity.getQulityStatus() != XZ_QULITY_STATUS.ALL.getValue()) {
            searchCond.put("qulityStatus", reqCreatQulity.getQulityStatus());
        }

        // 把or添加进来
        searchCond.put("$and", orValues);

        return getQulityList(searchCond, count, offset);
    }


////////////////////////////////////   MONGODB   ///////////////////////////////////////////////////

    /**
     * 保存抄送
     */
    private void saveClientRelation(ArrayList<String> ccClients, String groupID, String rectifyID) {

        if (CollectionUtil.isEmpty(ccClients) ||
                StrUtil.isEmpty(groupID) ||
                StrUtil.isEmpty(rectifyID)) {

            return;
        }

        long creatTime = SKTools.getNowTimeStamp();

        for (String ccClientIDTmp : ccClients) {

            QulityClientRelation qulityClientRe = new QulityClientRelation.Builder()
                    .withClientID(ccClientIDTmp)
                    .withCreatTime(creatTime)
                    .withUpdateTime(creatTime)
                    .withGroupID(groupID)
                    .withRectifyID(rectifyID)
                    .withRelationType(2)
                    .build();

            saveQulityCCRelation(qulityClientRe);
        }
    }

    /**
     * 删除关系
     */
    private void delCCClient(String rectifyID, String delClientID, int type) {

        DBObject dbObjecj = new BasicDBObject();

        dbObjecj.put("rectifyID", rectifyID);
        dbObjecj.put("delClientID", delClientID);
        dbObjecj.put("relationType", type);
        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_QULITY_CC);
        // 如果更新直接保存
        if (dbCollection != null) {
            dbCollection.remove(dbObjecj);
        }
    }

    /**
     * 保存关系
     */
    private void saveQulityCCRelation(QulityClientRelation qulityReItem) {

        if (null == qulityReItem) {
            return;
        }

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_QULITY_CC);
        DBObject queryObj = SKTools.convertBeanToDBObject(qulityReItem);
        // 如果更新直接保存
        if (dbCollection != null) {
            dbCollection.save(queryObj);
        }
    }


    /**
     * 保存Quity
     */
    private String saveQulityItem(QulityItem qulityItem) {

        // 只要创建就直接保存,不存在重复，因为这里是直接根据ID
        return updateQulityItem(qulityItem);
    }

    /**
     * 获取质量list
     */
    private ArrayList<RepQulityListItem> getQulityList(DBObject dbOBj, int count, int offset) {

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_QULITY);

        ArrayList<RepQulityListItem> requlityList = new ArrayList<>();

        // 时间倒序拍
        BasicDBObject sort = new BasicDBObject().append("creatTime", -1);

        // 查询
        DBCursor cursor = dbCollection.find(dbOBj).sort(sort).limit(count).skip(offset);
        List<DBObject> rectifyListTmp = MongoConnFactory.toList(cursor);

        for (DBObject dbOBJTmp : rectifyListTmp) {

            RepQulityListItem reqItem = null;

            reqItem = (RepQulityListItem) SKTools.convertDBObjectToBean(dbOBJTmp, RepQulityListItem.class);
            if (reqItem != null) {

                reqItem.setRectifyID(dbOBJTmp.get("_id").toString());
                requlityList.add(reqItem);
            }
        }

        return requlityList;
    }

    /**
     * 获取单个计划的ID
     */
    private QulityItem getQulityItem(String rectifyID) {

        if (StrUtil.isEmpty(rectifyID)) {
            return null;
        }

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_QULITY);

        DBObject searchOBj = new BasicDBObject();
        searchOBj.put("_id", new ObjectId(rectifyID));

        // 查询
        DBCursor cursor = dbCollection.find(searchOBj);
        List<DBObject> rectifyListTmp = MongoConnFactory.toList(cursor);

        QulityItem reqItem = null;

        if (rectifyListTmp.size() > 0) {
            try {
                DBObject dbOBJTmp = rectifyListTmp.get(0);
                reqItem = (QulityItem) SKTools.convertDBObjectToBean(dbOBJTmp, QulityItem.class);
                if (reqItem != null) {
                    reqItem.setRectifyID(dbOBJTmp.get("_id").toString());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return reqItem;
    }

    /**
     * 更新整改的状态
     */
    private void updateRectifyStatus(String rectifyID, int qulityStatus) {

        if (StrUtil.isEmpty(rectifyID)) {
            return;
        }
        // 1.直接更新子文档
        ObjectId objectId = new ObjectId(rectifyID);
        DBCollection dbCollection = MongoConnFactory.getDBCollectionWithName(ConstantDefine.MONGO_COLLECTION_QULITY);

        BasicDBObject searchCond = new BasicDBObject();
        searchCond.put("_id", new ObjectId(rectifyID));

        BasicDBObject statusCond = new BasicDBObject().append("qulityStatus", qulityStatus);
        BasicDBObject updateCond = new BasicDBObject().append("$set", statusCond);

        // 更新
        dbCollection.update(searchCond, updateCond, false, true);
    }

    /**
     * 更新质量安全的Item
     */
    private String updateQulityItem(QulityItem qulityItem) {

        if (null == qulityItem) {
            return "";
        }

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_QULITY);
        DBObject queryObj = SKTools.convertBeanToDBObject(qulityItem);
        // 如果更新直接保存
        if (qulityItem.getRectifyID() != null) {
            queryObj.put("_id", new ObjectId(qulityItem.getRectifyID()));
        }

        if (dbCollection != null) {
            dbCollection.save(queryObj);
        }

        return queryObj.get("_id").toString();
    }

    /**
     * 获取来的Qulity
     */
    private QulityItem getOldQulityItem(DBObject searchCondTmp) {

        if (null == searchCondTmp) {
            return null;
        }

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_QULITY);

        QulityItem qulityItem = null;
        try {
            DBCursor cursor = dbCollection.find(searchCondTmp).limit(1);
            List<DBObject> resAry = MongoConnFactory.toList(cursor);
            if (resAry.size() > 0) {

                DBObject dbOBJTmp = resAry.get(0);

                qulityItem = (QulityItem) SKTools.convertDBObjectToBean(dbOBJTmp, QulityItem.class);
                qulityItem.setRectifyID(dbOBJTmp.get("_id").toString());
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }

        return qulityItem;
    }

    /**
     * 获取单个赞
     */
    private QulityZanItem getZanObject(QulityZanItem qulityZanItem) {

        String clientID = qulityZanItem.getPushClientID();
        String rectifyID = qulityZanItem.getRectifyID();

        if (StrUtil.isEmpty(clientID) ||
                StrUtil.isEmpty(rectifyID)) {
            return null;
        }

        DBObject dbObject = new BasicDBObject();

        dbObject.put("pushClientID", clientID);
        dbObject.put("rectifyID", rectifyID);

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_QULITY_ZAN);

        QulityZanItem qulityZanItemTmp = null;
        try {
            DBCursor cursor = dbCollection.find(dbObject).limit(1);
            List<DBObject> resAry = MongoConnFactory.toList(cursor);
            if (resAry.size() > 0) {

                DBObject dbOBJTmp = resAry.get(0);

                qulityZanItemTmp = (QulityZanItem) SKTools.convertDBObjectToBean(dbOBJTmp, QulityZanItem.class);
                qulityZanItemTmp.setZanID(dbOBJTmp.get("_id").toString());
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }

        return qulityZanItemTmp;
    }


}
