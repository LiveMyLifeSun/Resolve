package com.hangtuo.service.impl;


import com.google.common.base.Strings;

import com.hangtuo.common.Enum.XZ_CONTACT_STATUS_TYPE;
import com.hangtuo.common.Enum.XZ_MSG_STATUS;
import com.hangtuo.common.response.BusinessException;
import com.hangtuo.common.response.ReturnCode;
import com.hangtuo.dao.contacts.ContactsDao;
import com.hangtuo.entity.Contacter.ContactAddressbookRelation;
import com.hangtuo.entity.Contacter.RepClientUpdateSetting;
import com.hangtuo.entity.Contacter.RepContactAddressbookInfo;
import com.hangtuo.entity.Contacter.ReqContactAddressbookUpdate;
import com.hangtuo.entity.Contacts;
import com.hangtuo.entity.NewClient;
import com.hangtuo.entity.message.SubMsgUser;
import com.hangtuo.persistence.ContactsMapper;
import com.hangtuo.service.AsyncService;
import com.hangtuo.service.ContactsService;
import com.hangtuo.service.annotation.ContactCacheUpdate;
import com.hangtuo.service.client.NewClientService;
import com.hangtuo.service.group.ContactRedisService;
import com.hangtuo.util.skutls.ConstantDefine;
import com.hangtuo.util.skutls.MongoConnFactory;
import com.hangtuo.util.skutls.SKTools;
import com.hangtuo.util.skutls.redisTools.Bridge.OYRedisTools;
import com.hangtuo.util.skutls.redisTools.impl.RedisToolsImpl;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.xiaoleilu.hutool.log.Log;
import com.xiaoleilu.hutool.log.LogFactory;
import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.RandomUtil;
import com.xiaoleilu.hutool.util.StrUtil;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

/**
 * Service - 联系人
 *
 * @author 王存
 * @version create time：2016年4月19日 下午1:42:45
 * @Description: 类说明
 */
@Service("contactsServiceImpl")
public class ContactsServiceImpl extends BaseServiceImpl<Contacts, Integer>
        implements ContactsService {

    private static final Log logger = LogFactory.get();

    @Autowired
    private ContactsMapper contactsMapper;

    @Resource(name = "messageServiceImpl")
    private MessageServiceImpl messageService;

    @Resource(name = "newClientServiceImpl")
    private NewClientService newClientService;

    @Resource(name = "asyncServiceImpl")
    private AsyncService asyncService;

    @Resource(name = "contactRedisServiceImpl")
    private ContactRedisService contactRedisService;
    @Resource(name = "contactsDaoImpl")
    private ContactsDao contactsDao;
    @Autowired
    public void setBaseMapper(ContactsMapper contactsMapper) {
        super.setBaseMapper(contactsMapper);
    }

    @Override
    public Contacts findByFriendId(int firendId) {
        return contactsMapper.findByFriendId(firendId);
    }

    @Override
    public List<Contacts> findCountByClientId(Contacts contacts) {
        return contactsMapper.findCountByClientId(contacts);
    }

    @Override
    public Contacts findByFriendIDAndUserID(Contacts contacts) {
        return contactsMapper.findByFriendIDAndUserID(contacts);
    }

    @Override
    @ContactCacheUpdate
    public void save(Contacts entity) {
        super.save(entity);
    }

    @Override
    public void removeByUserId(Integer clientId) {
        contactsMapper.removeByUserId(clientId);
    }

    @Override
    public Set<String> findContactIDByClientID(String clientID) throws BusinessException {
        if (StrUtil.isBlank(clientID)) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "clientID不能为空");
        }
        Set<String> contactIDs = contactRedisService.findByClientID(clientID);

        return contactIDs;
    }

    @Override
    public int findCountByUserId(Contacts contact) {
        return contactsMapper.findCountByUserId(contact);
    }

    @Override
    @ContactCacheUpdate
    public void delete(Integer integer) {
        super.delete(integer);
    }

    /**
     * 是否正在添加中
     */
    @Override
    public boolean isContactApplying(SubMsgUser contact) {

       return contactsDao.hasApplying(contact);
    }

    /**
     * 删除这个添加
     */
    @Override
    public void deleteContactApplying(SubMsgUser contact) {
        //   1.删除好友申请
        contactsDao.delApplying(contact);
        // 2.删除这条消息
        contactsDao.delApplyingInMsg(contact);

    }

    /**
     * 保存
     */
    @Override
    public String saveContactApplying(SubMsgUser subMsgUser) {
        return contactsDao.saveContactApplying(subMsgUser);
    }

    /**
     * 获取联系人申请的 修改
     */
    @Override
    public SubMsgUser getContactApplying(String requestID) {
        DBCursor cursor = contactsDao.find(requestID);
        List<DBObject> convidArray = MongoConnFactory.toList(cursor);
        SubMsgUser subMsgGroup = new SubMsgUser();
        if (convidArray.size() > 0) {

            DBObject objTmp = (DBObject) convidArray.get(0);

            ObjectId mongoID = (ObjectId) objTmp.get("_id");
            subMsgGroup = (SubMsgUser) SKTools.convertDBObjectToBean(objTmp, SubMsgUser.class);
            subMsgGroup.setRequestID(mongoID.toString());
        }

        return subMsgGroup;
    }

    /**
     * 同意联系人申请
     */
    @Override
    public void agreeContactApplying(String requestID, String messageID) {

        if (Strings.isNullOrEmpty(requestID)) {
            return;
        }
        // 1.删除消息
        contactsDao.delRequestIDInMongodb(requestID);

        // 2.去改变数据库的字
        contactsDao.updateGroupApplyMsg(messageID,XZ_MSG_STATUS.Agree);
    }

    /**
     * 拒绝联系人申请
     */
    @Override
    public void rejectContactApplying(String requestID, String messageID) {

        // 1.删除消息
        contactsDao.delRequestIDInMongodb(requestID);

        // 2.去改变数据库的字
        contactsDao.updateGroupApplyMsg(messageID, XZ_MSG_STATUS.Reject);

    }

    /**
     * 获取这个人的联系人信息 上
     */
    @Override
    public List<Contacts> findContactWithUserID(int userId) {
        return contactsMapper.findContactWithUserID(userId);
    }

    /**
     * 获取这个人的联系人信息
     */
    @SuppressWarnings("unchecked")
    @Override
    public Contacts findContactWithUserIDAndFriendID(int userId, int friendId) {
        Map queryMap = new HashMap();
        queryMap.put("userId", userId);
        queryMap.put("friendId", friendId);
        return contactsMapper.findContactWithUserIDAndFriendID(queryMap);
    }

    /**
     * 更新备注信息
     */
    @Override
    public void updateInfo(RepClientUpdateSetting repClientUpdateSetting) throws BusinessException {
        String contactIDTmp = repClientUpdateSetting.getContactsID();

        if (StrUtil.isEmpty(contactIDTmp)) {
            throw new BusinessException(ReturnCode.CODE_CONTACTER_VIREF_ERR, "请求联系ID不能为空");
        }

        Contacts contTmp = contactsMapper.findById(Integer.valueOf(contactIDTmp));

        if (contTmp == null) {
            throw new BusinessException(ReturnCode.CODE_CONTACTER_VIREF_ERR, "请求联系ID不能为空");
        }

        if (repClientUpdateSetting.getNewRemark() != null) {
            contTmp.setRemark(repClientUpdateSetting.getNewRemark());
        }
        contTmp.setIsMute(repClientUpdateSetting.getIsMute());
        contTmp.setIsTop(repClientUpdateSetting.getIsTop());

        //  更新
        contactsMapper.update(contTmp);
    }

    /**
     * 更新邀请时间,邀请人短信24小时后可重新发送
     */
    @Override
    public void updateInviteTime(ContactAddressbookRelation relationTmp) throws BusinessException {
        String clientID = relationTmp.getHostClientID();
        String phoneNum = relationTmp.getFriendPhoneNum();
        if (StrUtil.isBlank(clientID) || StrUtil.isBlank(phoneNum)) {
            logger.debug("clientID or friendPhoneNum can not be empty");
        }
        contactsDao.updateInviteTimeDB(relationTmp);
    }

    /**
     * 更新这个人的具体地址薄，同时返回具体的数据
     */
    @Override
    public ArrayList<RepContactAddressbookInfo> updateAddressbook(ReqContactAddressbookUpdate reqContactAddressbookUpdate) throws BusinessException {

        ArrayList<RepContactAddressbookInfo> returnList = new ArrayList<>();
        String hostClinetID = reqContactAddressbookUpdate.getClientID();
        String hostPhoneNum = reqContactAddressbookUpdate.getPhoneNum();
        NewClient hostClient = newClientService.findById(Integer.valueOf(hostClinetID));
        if (!hostClient.getLoginName().equals(hostPhoneNum)) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "手机号与存储手机号不一致");
        }

        ArrayList<ContactAddressbookRelation> addressbookList = reqContactAddressbookUpdate.getAddressbookList();

        // 如果为空直接返回数据
        if (CollectionUtil.isEmpty(addressbookList)) {
            return returnList;
        }

        //  异步保存 去保存具体的数据 -- 这块时间可以做
        for (ContactAddressbookRelation relationTmp : addressbookList) {
            relationTmp.setHostClientID(hostClinetID);
            relationTmp.setHostPhoneNum(hostClient.getLoginName());
        }
        // 异步保存
        asyncService.saveAddressInfo(this, addressbookList);


        // 先过滤存在的用户
        ConcurrentHashMap<String, ContactAddressbookRelation> allPhoneNumber = new ConcurrentHashMap<>(addressbookList.size());
        for (ContactAddressbookRelation relationTmp : addressbookList) {
            allPhoneNumber.put(relationTmp.getFriendPhoneNum(), relationTmp);
        }

        LinkedList<String> exitsKeys = this.fidleClientNotInStore(CollectionUtil.newArrayList(allPhoneNumber.keySet()));

        ArrayList<ContactAddressbookRelation> exitsRelation = new ArrayList<>(exitsKeys.size());

        // 获取最终要进行查询的关系
        for (String phoneNumber : exitsKeys) {
            ContactAddressbookRelation relationTmp = allPhoneNumber.get(phoneNumber);
            if (relationTmp != null) {
                exitsRelation.add(relationTmp);
            }
            // 移除已经存在的数据
            allPhoneNumber.remove(phoneNumber);
        }

        // 2.进行筛选
        for (ContactAddressbookRelation relationTmp : exitsRelation) {
            RepContactAddressbookInfo infoTmp = makeRepContactAddressbookInfo(relationTmp, hostClient);
            returnList.add(infoTmp);
        }

        // 3.不是协筑的用户原封返回
        ArrayList<String> allUsers = CollectionUtil.newArrayList(allPhoneNumber.keySet());

        for (ContactAddressbookRelation relationTmp : allPhoneNumber.values()) {

            RepContactAddressbookInfo repContactAddressbookInfo = new RepContactAddressbookInfo.Builder()
                    .withAddressbookName(relationTmp.getName())
                    .withPhoneNum(relationTmp.getFriendPhoneNum())
                    .withImg(relationTmp.getImg())
                    .withInviteTime(0)
                    .build();
            repContactAddressbookInfo.setIsXZMember(0);
            repContactAddressbookInfo.setStatus(XZ_CONTACT_STATUS_TYPE.NotFriend.getValue());
            repContactAddressbookInfo.setRegisterTime(0L);
            repContactAddressbookInfo.setUserName("");
            repContactAddressbookInfo.setImg("");
            repContactAddressbookInfo.setClientID("");

            returnList.add(repContactAddressbookInfo);
        }

        // 3.返回
        return returnList;
    }


    /**
     * 过滤不在Store的用户 set过滤 http://www.cnblogs.com/always-online/p/4121116.html
     */
    private LinkedList<String> fidleClientNotInStore(List<String> allList) {
        LinkedList<String> inClient = new LinkedList<>();

        if (CollectionUtil.isEmpty(allList)) {
            return inClient;
        }

        // 利用 Redis 的集合过滤

        // 1.load所有的数据
        this.loadAllClientWithOutInRedis();

        // 2.存一个数据进去
        String keyTmp = "ATMPKEY_" + RandomUtil.randomString(5);
        String resultList = "ATMPKEY_" + RandomUtil.randomString(5);

        RedisToolsImpl redisTool = OYRedisTools.getSingleton().getRedisTools();

        try {
            // 利用redis 求并集 。以后用户多的话，需要进行hash 分表
            redisTool.deleteKey(keyTmp);
            redisTool.deleteKey(resultList);

            String[] allKeys = allList.toArray(new String[0]);
            redisTool.setAddMember(keyTmp, allKeys);

            redisTool.setGetSinterstore(resultList, keyTmp, ConstantDefine.ALLLOGNAMES_NAME);

            ArrayList<String> reusltList = redisTool.getAllSetMember(resultList);

            if (CollectionUtil.isNotEmpty(reusltList)) {
                inClient.addAll(reusltList);
            }
            redisTool.deleteKey(keyTmp);
            redisTool.deleteKey(resultList);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return inClient;
    }

    /**
     * load 所有用户到我们的数据库
     */
    private void loadAllClientWithOutInRedis() {

        // 1.查看reids 是否有这个key
        RedisToolsImpl redisTool = OYRedisTools.getSingleton().getRedisTools();

        try {
            boolean isExit = redisTool.exists(ConstantDefine.ALLLOGNAMES_NAME);
            if (!isExit) {
                List<String> allLogNameList = newClientService.listOfAllLogName();
                String[] allKeys = allLogNameList.toArray(new String[0]);
                redisTool.setAddMember(ConstantDefine.ALLLOGNAMES_NAME, allKeys);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 告诉通讯录的好友有人新加入了
     */
    @Override
    public void sendNewFriendAddXiezhu(NewClient newAddClient) throws BusinessException {

        if (newAddClient == null) {
            return;
        }

        // 1. 取出这个人的所有的好友数据
        ArrayList<ContactAddressbookRelation> contactAddressbookRelationArrayList = contactsDao.getFriendPhoneList(newAddClient.getLoginName());

        for (ContactAddressbookRelation relationTmp : contactAddressbookRelationArrayList) {

            // sendMsg
            NewClient friendClient = newClientService.findByLoginName(relationTmp.getHostPhoneNum());
            SubMsgUser subMsgUser = new SubMsgUser.Builder()
                    .withClientID(String.valueOf(friendClient.getClientID()))
                    .withClientName(friendClient.getUserName())
                    .withClientImg(friendClient.getImg())
                    .withContent("新的好友推荐")
                    .build();
            messageService.sendNewContactFriendAdd(subMsgUser, friendClient);
        }
    }

    /**
     * 保存地址，异步执行
     */
    @Override
    public void saveAddressList(ArrayList<ContactAddressbookRelation> relationArrayList) {

        if (CollectionUtil.isEmpty(relationArrayList)) {
            return;
        }

        for (ContactAddressbookRelation relationTmp : relationArrayList) {
            this.saveAddressItem(relationTmp);
        }
    }

    /**
     * 保存Quity
     */
    private String saveAddressItem(ContactAddressbookRelation relationTmp) {

        // 只要创建就直接保存,不存在重复，因为这里是直接根据ID
        DBObject queryObj = new BasicDBObject();
        queryObj.put("hostPhoneNum", relationTmp.getHostPhoneNum());
        queryObj.put("friendPhoneNum", relationTmp.getFriendPhoneNum());

        ContactAddressbookRelation oldRealtion = contactsDao.getOldAddressItem(queryObj);

        if (oldRealtion != null) {
            relationTmp.setRelationID(oldRealtion.getRelationID());

            long inviteTime = oldRealtion.getInviteTime();
            relationTmp.setInviteTime(inviteTime);
        } else {
            relationTmp.setInviteTime(0L);
        }
        relationTmp.setUpdatetime(SKTools.getNowTimeStamp());
        return contactsDao.updateAddressItem(relationTmp);
    }

    /**
     * 转换address的数据
     */
    private RepContactAddressbookInfo makeRepContactAddressbookInfo(ContactAddressbookRelation relationTmp, NewClient hostClient) throws BusinessException {

        if (relationTmp == null) {
            return null;
        }

        NewClient friendClient = newClientService.findByLoginName(relationTmp.getFriendPhoneNum());


        DBObject searchObj = new BasicDBObject();
        searchObj.put("hostClientID", relationTmp.getHostClientID());
        searchObj.put("friendPhoneNum", relationTmp.getFriendPhoneNum());
        ContactAddressbookRelation relationDb = contactsDao.getOldAddressItem(searchObj);

        long inviteTime = 0L;
        if (relationDb != null) {
            inviteTime = relationDb.getInviteTime();
        }

        RepContactAddressbookInfo repContactAddressbookInfo = new RepContactAddressbookInfo.Builder()
                .withAddressbookName(relationTmp.getName())
                .withPhoneNum(relationTmp.getFriendPhoneNum())
                .withImg(relationTmp.getImg())
                .withInviteTime(inviteTime)
                .build();

        if (friendClient == null) {
            repContactAddressbookInfo.setIsXZMember(0);
            repContactAddressbookInfo.setStatus(XZ_CONTACT_STATUS_TYPE.NotFriend.getValue());
            repContactAddressbookInfo.setRegisterTime(0L);
            repContactAddressbookInfo.setUserName("");
            repContactAddressbookInfo.setImg("");
            repContactAddressbookInfo.setClientID("");
        } else {
            repContactAddressbookInfo.setIsXZMember(1);

            // 查询是否为好友
            Contacts contact = new Contacts();
            contact.setUserId(hostClient.getClientID());
            contact.setFriendId(friendClient.getClientID());
            int count = contactsMapper.findCountByUserId(contact);

            if (count > 0) {
                repContactAddressbookInfo.setStatus(XZ_CONTACT_STATUS_TYPE.AlreadyFriend.getValue());
            } else {
                // 是否申请中

                SubMsgUser contacts = new SubMsgUser();
                contacts.setClientID(relationTmp.getHostClientID());
                contacts.setFriendID(String.valueOf(friendClient.getClientID()));
                boolean isApplying = isContactApplying(contacts);

                if (isApplying) {
                    // 获取状态
                    repContactAddressbookInfo.setStatus(XZ_CONTACT_STATUS_TYPE.Sending.getValue());
                } else {
                    repContactAddressbookInfo.setStatus(XZ_CONTACT_STATUS_TYPE.NotFriend.getValue());
                }
            }
            repContactAddressbookInfo.setRegisterTime(friendClient.getCreateDate());
            repContactAddressbookInfo.setUserName(friendClient.getUserName());
            repContactAddressbookInfo.setImg(friendClient.getImg());
            repContactAddressbookInfo.setClientID(String.valueOf(friendClient.getClientID()));
        }

        return repContactAddressbookInfo;
    }
}
