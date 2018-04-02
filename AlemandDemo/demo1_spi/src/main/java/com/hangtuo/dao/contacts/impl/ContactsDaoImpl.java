package com.hangtuo.dao.contacts.impl;

import com.google.common.base.Strings;

import com.hangtuo.common.Enum.XZ_MSG_STATUS;
import com.hangtuo.common.Enum.XZ_MSG_TYPE;
import com.hangtuo.dao.contacts.ContactsDao;
import com.hangtuo.entity.Contacter.ContactAddressbookRelation;
import com.hangtuo.entity.message.SubMsgUser;
import com.hangtuo.util.skutls.ConstantDefine;
import com.hangtuo.util.skutls.MongoConnFactory;
import com.hangtuo.util.skutls.SKTools;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.xiaoleilu.hutool.util.StrUtil;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;


/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/21
 */
@Repository("contactsDaoImpl")
public class ContactsDaoImpl implements ContactsDao {

    /**
     *当类加载的时候就将创建MongoDb的链接
     */
    private static DBCollection getDBCollection() {
        return MongoConnFactory.getDBCollectionWithName(ConstantDefine.MONGO_COLLECTION_USERAPPLY);
    }


    @Override
    public boolean hasApplying(SubMsgUser contacts) {
        boolean hasApplying;
        DBCollection dbCollection = getDBCollection();

        BasicDBObject queryObj = new BasicDBObject();
        queryObj.put(ConstantDefine.MONGO_USERAPPLY_FIELD_ClientID, contacts.getClientID());
        queryObj.put(ConstantDefine.MONGO_USERAPPLY_FIELD_FriendID, contacts.getFriendID());

        List<DBObject> convid_Array = dbCollection.find(queryObj).toArray(); // 根据conv_ident查询对应的回话

        if (convid_Array.size() > 0) {
            hasApplying = true;
        } else {
            hasApplying = false;
        }
        return hasApplying;
    }

    @Override
    public void delApplying(SubMsgUser contacts) {
        boolean hasApplying;
        DBCollection dbCollection = getDBCollection();

        BasicDBObject queryObj = new BasicDBObject();
        queryObj.put(ConstantDefine.MONGO_USERAPPLY_FIELD_ClientID, contacts.getClientID());
        queryObj.put(ConstantDefine.MONGO_USERAPPLY_FIELD_FriendID, contacts.getFriendID());
        queryObj.put("status", XZ_MSG_STATUS.Untreated.getValue());

        dbCollection.remove(queryObj);
    }

    @Override
    public void delApplyingInMsg(SubMsgUser contacts) {
        // 1.直接更新子文档
        DBCollection dbCollection = MongoConnFactory.getDBCollectionWithName(ConstantDefine.MONGO_COLLECTION_Msg);

        BasicDBObject searchCond = new BasicDBObject();
        searchCond.put("Content.clientID", contacts.getClientID());
        searchCond.put("Content.friendID", contacts.getFriendID());
        searchCond.put("MessageType", XZ_MSG_TYPE.CONTACT_APPLY.getValue());
        searchCond.put("Content.status", XZ_MSG_STATUS.Untreated.getValue());// 只删除未读的

        dbCollection.remove(searchCond);
    }

    @Override
    public String saveContactApplying(SubMsgUser subMsgUser) {
        DBObject queryObj = SKTools.convertBeanToDBObject(subMsgUser);

        // 保存
        DBCollection dbCollection = getDBCollection();
        dbCollection.save(queryObj);

        String requestID = queryObj.get("_id").toString();

        return requestID;
    }

    @Override
    public DBCursor find(String requestID) {
        ObjectId objectId = new ObjectId(requestID);
        BasicDBObject searchCond = new BasicDBObject().append("_id", objectId);
        DBCollection dbCollection = getDBCollection();
        // 查询
        DBCursor cursor = dbCollection.find(searchCond);
        return cursor;
    }

    @Override
    public void delRequestIDInMongodb(String requestID) {
        ObjectId objectId = new ObjectId(requestID);
        BasicDBObject delCond = new BasicDBObject().append("_id", objectId);
        DBCollection dbCollection = getDBCollection();
        dbCollection.remove(delCond);
    }

    @Override
    public String updateAddressItem(ContactAddressbookRelation relationTmp) {
        if (null == relationTmp) {
            return "";
        }

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_CONTACT_ADDRESS_RELATION);
        DBObject queryObj = SKTools.convertBeanToDBObject(relationTmp);
        // 如果更新直接保存
        if (relationTmp.getRelationID() != null) {
            queryObj.put("_id", new ObjectId(relationTmp.getRelationID()));
        }

        if (dbCollection != null) {
            dbCollection.save(queryObj);
        }

        return queryObj.get("_id").toString();
    }

    @Override
    public void updateGroupApplyMsg(String messageID, XZ_MSG_STATUS msgStatus) {
        if (Strings.isNullOrEmpty(messageID)) {
            return;
        }


        // 1.直接更新子文档
        ObjectId objectId = new ObjectId(messageID);
        DBCollection dbCollection = MongoConnFactory.getDBCollectionWithName(ConstantDefine.MONGO_COLLECTION_Msg);

        BasicDBObject searchCond = new BasicDBObject();
        searchCond.put("_id", objectId);

        BasicDBObject statusCond = new BasicDBObject().append("Content.status", msgStatus.getValue());
        BasicDBObject updateCond = new BasicDBObject().append("$set", statusCond);

        // 更新整个消息
        dbCollection.update(searchCond, updateCond, false, true);
    }

    @Override
    public void updateInviteTimeDB(ContactAddressbookRelation relationTmp) {
        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_CONTACT_ADDRESS_RELATION);
        String clientID = relationTmp.getHostClientID();
        String phoneNum = relationTmp.getFriendPhoneNum();
        BasicDBObject searchCond = new BasicDBObject();
        searchCond.put("hostClientID", clientID);
        searchCond.put("friendPhoneNum", phoneNum);
        BasicDBObject timeCond = new BasicDBObject().append("inviteTime", SKTools.getNowTimeStamp());
        BasicDBObject updateCond = new BasicDBObject().append("$set", timeCond);

        dbCollection.update(searchCond, updateCond, false, true);
    }

    @Override
    public ArrayList<ContactAddressbookRelation> getFriendPhoneList(String phoneNum) {
        ArrayList<ContactAddressbookRelation> relationArrayList = new ArrayList<>();
        if (StrUtil.isEmpty(phoneNum)) {
            return relationArrayList;
        }

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_CONTACT_ADDRESS_RELATION);
        DBObject searchCondTmp = new BasicDBObject();
        searchCondTmp.put("friendPhoneNum", phoneNum);

        try {
            DBCursor cursor = dbCollection.find(searchCondTmp);
            List<DBObject> resAry = MongoConnFactory.toList(cursor);

            for (DBObject dbOBJTmp : resAry) {

                ContactAddressbookRelation relationTmp = null;

                relationTmp = (ContactAddressbookRelation) SKTools.convertDBObjectToBean(dbOBJTmp, ContactAddressbookRelation.class);
                if (relationTmp != null) {

                    relationTmp.setRelationID(dbOBJTmp.get("_id").toString());
                    relationArrayList.add(relationTmp);
                }
            }

        } catch (Exception exp) {
            exp.printStackTrace();
        }

        return relationArrayList;
    }

    @Override
    public ContactAddressbookRelation getOldAddressItem(DBObject searchCondTmp) {
        if (null == searchCondTmp) {
            return null;
        }

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_CONTACT_ADDRESS_RELATION);

        ContactAddressbookRelation relationTmp = null;
        try {
            DBCursor cursor = dbCollection.find(searchCondTmp).limit(1);
            List<DBObject> resAry = MongoConnFactory.toList(cursor);
            if (resAry.size() > 0) {

                DBObject dbOBJTmp = resAry.get(0);

                relationTmp = (ContactAddressbookRelation) SKTools.convertDBObjectToBean(dbOBJTmp, ContactAddressbookRelation.class);
                relationTmp.setRelationID(dbOBJTmp.get("_id").toString());
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }

        return relationTmp;
    }

}
