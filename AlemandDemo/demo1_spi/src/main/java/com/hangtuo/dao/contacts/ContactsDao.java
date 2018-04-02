package com.hangtuo.dao.contacts;

import com.hangtuo.common.Enum.XZ_MSG_STATUS;
import com.hangtuo.entity.Contacter.ContactAddressbookRelation;
import com.hangtuo.entity.message.SubMsgUser;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import java.util.ArrayList;


/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/21
 */
public interface ContactsDao {
    /**
     *是否正在申请
     */
    public boolean hasApplying(SubMsgUser contacts);

    /**
     *删除申请
     */
    public void delApplying(SubMsgUser contacts);
    /**
     * 删除申请申请在消息
     */
    public void delApplyingInMsg(SubMsgUser contacts);
    /**
     *保存
     */
    public String saveContactApplying(SubMsgUser subMsgUser);
    /**
     *查询
     *
     */
    public DBCursor find(String requestID);
    /**
     * 删除这个用户的申请
     */
    public void delRequestIDInMongodb(String requestID);
    /**
     *
     */
    public String updateAddressItem(ContactAddressbookRelation relationTmp);
    /**
     *更新用户申请状态
     */
    public void updateGroupApplyMsg(String messageID, XZ_MSG_STATUS msgStatus);

    /**
     * 更新邀请时间
     */
    public void updateInviteTimeDB(ContactAddressbookRelation relationTmp);
    /**
     * 查出这个人，他被好友的人，也就是存在其他的人
     */
    public ArrayList<ContactAddressbookRelation> getFriendPhoneList(String phoneNum);
    /**
     * 获取来的Qulity
     */
    public ContactAddressbookRelation getOldAddressItem(DBObject searchCondTmp);

}
