package com.hangtuo.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import com.hangtuo.common.Enum.XZ_ADMIN_TYPE;
import com.hangtuo.common.Enum.XZ_DEVICE_RES;
import com.hangtuo.common.Enum.XZ_DEVICE_STATUS;
import com.hangtuo.common.Enum.XZ_FILE_MODEL_TYPE;
import com.hangtuo.common.Enum.XZ_MUSTARRIVED_SEND_TYPE;
import com.hangtuo.common.Enum.XZ_MUSTARRIVED_TIMEDELAY_TYPE;
import com.hangtuo.common.Enum.XZ_MUSTARRIVE_TYPE;
import com.hangtuo.common.Enum.XZ_QRCODE_TYPE;
import com.hangtuo.common.response.BusinessException;
import com.hangtuo.common.response.ReturnCode;
import com.hangtuo.dao.file.FileInfoDao;
import com.hangtuo.entity.GroupMember;
import com.hangtuo.entity.GroupMsg;
import com.hangtuo.entity.MustArrive.MustArriveItem;
import com.hangtuo.entity.MustArrive.subModel.SubMustArriveDevice;
import com.hangtuo.entity.NewClient;
import com.hangtuo.entity.TimerNewJob;
import com.hangtuo.entity.comman.TinyClientItem;
import com.hangtuo.entity.config.MainConfig;
import com.hangtuo.entity.device.BreakdownInfo;
import com.hangtuo.entity.device.DeviceCommentItem;
import com.hangtuo.entity.device.DeviceInfo;
import com.hangtuo.entity.device.DeviceInfoComparator;
import com.hangtuo.entity.device.DeviceRelationInfo;
import com.hangtuo.entity.device.DeviceStatusItem;
import com.hangtuo.entity.device.DeviceStatusList;
import com.hangtuo.entity.device.DisableInfo;
import com.hangtuo.entity.device.ExitInfo;
import com.hangtuo.entity.device.FinishInfo;
import com.hangtuo.entity.device.MaintenanceInfo;
import com.hangtuo.entity.device.NodeFileDevice;
import com.hangtuo.entity.device.PollingInfo;
import com.hangtuo.entity.device.ReqDeviceItem;
import com.hangtuo.entity.device.ReqDeviceSearchItem;
import com.hangtuo.entity.device.ReqOwnSaveBreakdown;
import com.hangtuo.entity.device.ResBreakdownInfo;
import com.hangtuo.entity.device.ResBreakdownItem;
import com.hangtuo.entity.device.ResCommentItem;
import com.hangtuo.entity.device.ResDeviceInfo;
import com.hangtuo.entity.device.ResDeviceInfoComparator;
import com.hangtuo.entity.device.ResDeviceItem;
import com.hangtuo.entity.device.ResDisableInfo;
import com.hangtuo.entity.device.ResDisableItem;
import com.hangtuo.entity.device.ResDisableList;
import com.hangtuo.entity.device.ResFinishItem;
import com.hangtuo.entity.device.ResFixBreakdownItem;
import com.hangtuo.entity.device.ResFixItem;
import com.hangtuo.entity.device.ResMaintenanceItem;
import com.hangtuo.entity.device.ResMaintenanceList;
import com.hangtuo.entity.device.ResOwnFixInfo;
import com.hangtuo.entity.device.ResPollingItem;
import com.hangtuo.entity.device.ResPollingList;
import com.hangtuo.entity.device.ResRepairInfo;
import com.hangtuo.entity.device.ResScrapItem;
import com.hangtuo.entity.device.ScrapInfo;
import com.hangtuo.entity.file.FileItem;
import com.hangtuo.entity.file.ReqFileItem;
import com.hangtuo.entity.group.GroupPermission;
import com.hangtuo.entity.message.SubMsgDevice;
import com.hangtuo.entity.message.file.DownloadMsgFile;
import com.hangtuo.entity.qrcode.QrcodeInfo;
import com.hangtuo.persistence.GroupMsgMapper;
import com.hangtuo.service.DeviceService;
import com.hangtuo.service.GroupAdminService;
import com.hangtuo.service.GroupMemberService;
import com.hangtuo.service.GroupMsgService;
import com.hangtuo.service.GroupSendMsgService;
import com.hangtuo.service.MessageService;
import com.hangtuo.service.QrcodeService;
import com.hangtuo.service.client.NewClientService;
import com.hangtuo.util.OKHTTPUtil;
import com.hangtuo.util.QRUtils.ZXingPic;
import com.hangtuo.util.QiNiu.QiniuUtil;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

/**
 * @author Shawn
 * @since 2017/5/24
 */
@Service("deviceServiceImpl")
public class DeviceServiceImpl implements DeviceService {

    private static final Log log = LogFactory.get();
    private static MainConfig mainConfig = MainConfig.getInstance();
    private static String timerURL = mainConfig.getTimerConfig().getTimerURL() + "/timer/app/timer/create";
    private static String callBackURL = mainConfig.getMustArriveConfig().getMustArrivedURL() + "/gouliaoweb-1.0/app/device/callback";
    @Resource(name = "newClientServiceImpl")
    private NewClientService clientService;
    @Resource(name = "groupMemberServiceImpl")
    private GroupMemberService groupMemberService;
    @Resource(name = "groupMsgServiceImpl")
    private GroupMsgService groupMsgService;
    @Resource(name = "groupSendMsgServiceImpl")
    private GroupSendMsgService groupSendMsgService;
    @Resource(name = "groupAdminServiceImpl")
    private GroupAdminService groupAdminService;
    @Resource(name = "mustArriveServiceImpl")
    private MustArriveServiceImpl mustArriveService;
    @Autowired
    private GroupMsgMapper groupMsgMapper;

    @Resource(name = "qrcodeServiceImpl")
    private QrcodeService qrcodeService;
    @Resource(name = "fileInfoDaoImpl")
    private FileInfoDao fileInfoDao;
    @Resource(name = "newClientServiceImpl")
    private NewClientService newClientService;
    @Resource(name = "messageServiceImpl")
    private MessageService messageService;

    /**
     * 1.创建一个设备
     */
    @Override
    public String createDevice(DeviceInfo deviceInfo) throws BusinessException {
        if (deviceInfo == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        long nowTime = SKTools.getNowTimeStamp();

        if (CollectionUtil.isEmpty(deviceInfo.getImgs())) {
            deviceInfo.setImgs(new ArrayList<String>());
        }
        if (CollectionUtil.isEmpty(deviceInfo.getDisableList())) {
            deviceInfo.setDisableList(new ArrayList<DisableInfo>());
        }
        if (CollectionUtil.isEmpty(deviceInfo.getMaintenanceList())) {
            deviceInfo.setMaintenanceList(new ArrayList<String>());
        }
        if (CollectionUtil.isEmpty(deviceInfo.getPollingList())) {
            deviceInfo.setPollingList(new ArrayList<String>());
        }

        deviceInfo.setCreateTime(nowTime);

        //设置设备编号
        String groupID = deviceInfo.getGroupID();
        int deviceNum = this.findCount(groupID) + 1;
        String groupMsgID = groupMsgService.getGroupMsgIDByGrouId(groupID);
        String showNum = this.getDeviceNum(groupMsgID, deviceNum);


        //设置设备状态
        int status = XZ_DEVICE_STATUS.ENABLE_DEVICE.getValue();
        deviceInfo.setStatus(status);

        // 判断设备编号showNum是否存在 存在则抛出异常
        if (this.showNumExist(showNum)) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "设备编号已存在");
        }


        deviceInfo.setInnerIndex(deviceNum);
        deviceInfo.setShowNum(showNum);
        //生成搜索字段
        String searchIndexTmp = deviceInfo.makeSearchIndexStr();


        String deviceID = this.saveDevice(deviceInfo);

        //设置qrCode 修改为新版本的二维码生成方式
        QrcodeInfo qrcodeInfo = new QrcodeInfo.Builder()
                .withClientID(deviceInfo.getClientID())
                .withGroupID(groupID)
                .withDeviceID(deviceID)
                .withActionType(XZ_QRCODE_TYPE.DEVICE.getValue())
                .build();

        String qrcode = qrcodeService.saveQrcode(qrcodeInfo, true);
        deviceInfo.setQrCode(qrcode);
        //设置deviceID
        deviceInfo.setDeviceID(deviceID);
        this.saveDevice(deviceInfo);

        //更新关系表
        DeviceRelationInfo deviceRelationInfo = new DeviceRelationInfo.Builder()
                .withGroupID(deviceInfo.getGroupID())
                .withDeviceID(deviceInfo.getDeviceID())
                .withClientID("")
                .withSearchIndex(searchIndexTmp)
                .withDeviceCreateTime(deviceInfo.getCreateTime())
                .withStatus(deviceInfo.getStatus())
                .withDeviceCreateTime(deviceInfo.getCreateTime())
                .build();


        String deviceRelationID = this.saveDeviceRelation(deviceRelationInfo, 0);
        deviceRelationInfo.setDeviceRelationID(deviceRelationID);
        this.saveDeviceRelation(deviceRelationInfo, 0);

        deviceRelationInfo.setDeviceRelationID(null);
        //更新保养,巡检关系表
        ArrayList<String> maintenanceList = deviceInfo.getMaintenanceList();
        ArrayList<String> pollingList = deviceInfo.getPollingList();

        //计算巡检，保养时间
        if (CollectionUtil.isNotEmpty(maintenanceList)) {
            this.updateMaintenanceRelation(deviceRelationInfo, nowTime, deviceInfo.getMaintenanceCycle(), maintenanceList);
        }

        if (CollectionUtil.isNotEmpty(pollingList)) {
            this.updatePollingRelation(deviceRelationInfo, nowTime, deviceInfo.getPollingCycle(), pollingList);
        }

        //调用定时任务
        ArrayList<DeviceRelationInfo> maintenanceRelationList = this.getDeviceRelationByStatus(deviceID, XZ_DEVICE_STATUS.WAIT_MAINTENANCE_DEVICE.getValue(), null, null, null, null);
        ArrayList<DeviceRelationInfo> pollingRelationList = this.getDeviceRelationByStatus(deviceID, XZ_DEVICE_STATUS.WAIT_POLLING_DEVICE.getValue(), null, null, null, null);

        this.sendDelayMsgList(maintenanceRelationList);
        this.sendDelayMsgList(pollingRelationList);

        return deviceID;
    }

    /**
     * 查看此人所属的所有项目部,是否包含设备所在项目部
     */
    private int isGroupMember(String deviceGroupID, String clientID) {
        List<GroupMsg> groupMsgs = groupMsgMapper.findUserGroupListWithUserID(Integer.parseInt(clientID));
        int isGroupMember = 0;
        if (CollectionUtil.isNotEmpty(groupMsgs)) {
            for (GroupMsg groupMsg : groupMsgs) {
                if (groupMsg.getGroupId().equals(deviceGroupID)) {
                    isGroupMember = 1;
                    break;
                }
            }
        }
        return isGroupMember;
    }


    /**
     * 2.获取一个设备
     */
    @Override
    public ResDeviceItem getOneDeviceInfo(ReqDeviceItem reqDeviceItem) throws BusinessException {
        if (reqDeviceItem == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        long nowTime = SKTools.getNowTimeStamp();
        String deviceID = reqDeviceItem.getDeviceID();
        String clientID = reqDeviceItem.getClientID();
        ResDeviceItem resDeviceItem = this.getOneDeviceItem(deviceID);
        //如果此人属于设备所在的项目部，则抛出异常
        String deviceGroupID = resDeviceItem.getGroupID();

        if (!ConstantDefine.WECHAT_SCAN_CLIENTID.equals(clientID)) {
            int isGroupMember = this.isGroupMember(deviceGroupID, clientID);

            if (isGroupMember != 1) {
                throw new BusinessException(ReturnCode.CODE_DEVICE_CAN_NOT_VIEW, "抱歉，您不属于此项目部，无法查看设备");
            }
        }


        ArrayList<ResCommentItem> list = this.listDeviceComment(deviceID);
        resDeviceItem.setCommentList(list);
        //设置最后维修时间
        resDeviceItem.setLatestRepairTime(this.getLastFinishTime(deviceID));
        //设置最后保养时间
        long lastMaintenanceTime = this.getLastMaintenanceTime(deviceID);
        resDeviceItem.setLatestMaintenanceTime(lastMaintenanceTime);
        //设置最后停用时间
        resDeviceItem.setLatestDisableTime(this.getLastDisableTime(deviceID));
        //设置最后巡检时间
        resDeviceItem.setLatestPollingTime(this.getLastPollingTime(deviceID));
        ArrayList<String> maintenaceClientList = resDeviceItem.getMaintenanceList();
        ArrayList<String> pollingClientList = resDeviceItem.getPollingList();
        if (!ConstantDefine.WECHAT_SCAN_CLIENTID.equals(clientID)) {
            //是否需要巡检，是否需要保养，是否需要维修 1.4 修改需求 只要是巡检和保养人 一直为true
            BasicDBObject gt = new BasicDBObject("$lt", nowTime + 24 * 3600 * 1000);
            ArrayList<DeviceRelationInfo> finishList = this.getDeviceRelationByStatus(deviceID, XZ_DEVICE_STATUS.WAIT_FINISH_DEVICE.getValue(), clientID, null, null, gt);

            int isFinish = CollectionUtil.isEmpty(finishList) ? 0 : 1;
            int isPolling = 0;
            int isMaintenance = 0;
            if (CollectionUtil.isNotEmpty(maintenaceClientList) && maintenaceClientList.contains(clientID)) {
                isMaintenance = 1;
            }
            if (CollectionUtil.isNotEmpty(pollingClientList) && pollingClientList.contains(clientID)) {
                isPolling = 1;
            }
            resDeviceItem.setIsFinish(isFinish);
            resDeviceItem.setIsMaintenance(isMaintenance);
            resDeviceItem.setIsPolling(isPolling);
        }
        // 4.make所有allclients
        ArrayList<String> clientIDSTmp = new ArrayList<>();
        //添加报废设备的人到clientIDs
        if (resDeviceItem.getScrap() != null) {
            clientIDSTmp.add(resDeviceItem.getScrap().getPostClientID());
        }
        clientIDSTmp.add(resDeviceItem.getPostClientID());
        if (!CollectionUtil.isEmpty(resDeviceItem.getPollingList())) {
            clientIDSTmp.addAll(resDeviceItem.getPollingList());
        }
        if (!CollectionUtil.isEmpty(resDeviceItem.getMaintenanceList())) {
            clientIDSTmp.addAll(resDeviceItem.getMaintenanceList());
        }

        for (ResCommentItem resCommentItem : list) {
            clientIDSTmp.add(resCommentItem.getPostClientID());

        }
        SKTools.removeDuplicate(clientIDSTmp);
        ArrayList<TinyClientItem> clientList = this.getTinyClientList(clientIDSTmp, resDeviceItem.getGroupID());
        resDeviceItem.setClientIDS(clientList);

        int status = this.getDeviceStatus(deviceID, clientID, deviceGroupID).getValue();
        resDeviceItem.setStatus(status);
        return resDeviceItem;
    }

    /**
     * 获取所有的设备
     */
    @Override
    public ArrayList<DeviceInfo> listAllDeviceInfo() throws BusinessException {
        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_INFO);

        DBObject sortObj = new BasicDBObject();
        sortObj.put("createTime", -1);

        DBCursor cursor = dbCollection.find().sort(sortObj);

        List<DBObject> list = MongoConnFactory.toList(cursor);
        ArrayList<DeviceInfo> deviceInfoList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(list)) {
            for (DBObject dbObject : list) {
                DeviceInfo deviceInfo = (DeviceInfo) SKTools.convertDBObjectToBean(dbObject, DeviceInfo.class);

                deviceInfoList.add(deviceInfo);
            }
        }
        return deviceInfoList;
    }


    /**
     * 更新设备qrcode
     */
    @Override
    public void updateQrcode(DeviceInfo deviceInfo) throws BusinessException {
        log.error("==={}", deviceInfo.getQrCode());
        saveDevice(deviceInfo);
    }

    /**
     * 3.更新设备
     */
    @Override
    public String updateDevice(DeviceInfo deviceInfo) throws BusinessException {
        if (deviceInfo == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        long nowTime = SKTools.getNowTimeStamp();
        String deviceID = deviceInfo.getDeviceID();
        //1.4.0 判断 修改设备时 修改的图片有了才修改,没有则不修改
        ArrayList<String> imgs = deviceInfo.getImgs();
        DeviceInfo oldDeviceInfo = this.getOneDevice(deviceID);
        ArrayList<String> oldImgs = oldDeviceInfo.getImgs();

        if (CollectionUtil.isNotEmpty(imgs)) {
            ArrayList<String> realImgs = new ArrayList<>();
            for (String img : imgs) {
                if (StrUtil.isNotBlank(img)) {
                    realImgs.add(img);
                }
            }
            if (CollectionUtil.isNotEmpty(realImgs)) {
                deviceInfo.setImgs(realImgs);
            } else {
                deviceInfo.setImgs(oldImgs);
            }
        }
        if (CollectionUtil.isEmpty(deviceInfo.getDisableList())) {
            deviceInfo.setDisableList(new ArrayList<DisableInfo>());
        }
        if (CollectionUtil.isEmpty(deviceInfo.getMaintenanceList())) {
            deviceInfo.setMaintenanceList(new ArrayList<String>());
        }
        if (CollectionUtil.isEmpty(deviceInfo.getPollingList())) {
            deviceInfo.setPollingList(new ArrayList<String>());
        }
        deviceInfo.setDeviceID(null);

        DeviceInfo deviceInfoTmp = this.getOneDevice(deviceID);

        deviceInfo.setQrCode(deviceInfoTmp.getQrCode());
        deviceInfo.setShowNum(deviceInfoTmp.getShowNum());
        deviceInfo.setDeviceID(deviceID);
        deviceInfo.setGroupID(deviceInfoTmp.getGroupID());
        deviceInfo.setClientID(deviceInfoTmp.getClientID());
        deviceInfo.setStatus(deviceInfoTmp.getStatus());
        deviceInfo.setInnerIndex(deviceInfoTmp.getInnerIndex());
        this.saveDevice(deviceInfo);
        deviceInfo.setDeviceID(null);

        //重新生成搜索字段
        String searchIndexTmp = deviceInfo.makeSearchIndexStr();
        //更新关系表
        DeviceRelationInfo deviceRelationInfo = new DeviceRelationInfo.Builder()
                .withGroupID(deviceInfo.getGroupID())
                .withDeviceID(deviceID)
                .withClientID("")
                .withSearchIndex(searchIndexTmp)
                .withStatus(deviceInfo.getStatus())
                .build();

        //删除关系表，除了client=""  deviceID="deviceID" 的记录
        this.removeRelationByDeviceIDAndStatus(deviceID, XZ_DEVICE_STATUS.WAIT_MAINTENANCE_DEVICE);
        this.removeRelationByDeviceIDAndStatus(deviceID, XZ_DEVICE_STATUS.WAIT_POLLING_DEVICE);

        if (deviceInfoTmp.getStatus() == XZ_DEVICE_STATUS.ENABLE_DEVICE.getValue()) {
            //更新保养,巡检关系表
            ArrayList<String> maintenanceList = deviceInfo.getMaintenanceList();
            ArrayList<String> pollingList = deviceInfo.getPollingList();
            //计算巡检，保养时间
            if (CollectionUtil.isNotEmpty(maintenanceList)) {
                this.updateMaintenanceRelation(deviceRelationInfo, nowTime, deviceInfo.getMaintenanceCycle(), maintenanceList);
            }

            if (CollectionUtil.isNotEmpty(pollingList)) {
                this.updatePollingRelation(deviceRelationInfo, nowTime, deviceInfo.getPollingCycle(), pollingList);
            }
        }

        //调用定时任务
        ArrayList<DeviceRelationInfo> maintenanceRelationList = this.getDeviceRelationByStatus(deviceID, XZ_DEVICE_STATUS.WAIT_MAINTENANCE_DEVICE.getValue(), null, null, null, null);
        ArrayList<DeviceRelationInfo> pollingRelationList = this.getDeviceRelationByStatus(deviceID, XZ_DEVICE_STATUS.WAIT_POLLING_DEVICE.getValue(), null, null, null, null);

        this.sendDelayMsgList(maintenanceRelationList);
        this.sendDelayMsgList(pollingRelationList);

        return deviceID;
    }

    /**
     * 3.废弃设备
     */
    @Override
    public String scrapDevice(ScrapInfo scrapInfo) throws BusinessException {
        //1.修改设备的状态为废弃
        if (scrapInfo == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        String deviceID = scrapInfo.getDeviceID();
        scrap(deviceID,scrapInfo);
        return scrapInfo.getDeviceID();
    }

    /**
     *报废的方法抽取
     */
    public void scrap(String deviceID,ScrapInfo scrapInfo){
        long nowTime = SKTools.getNowTimeStamp();
        DeviceInfo deviceInfo = this.getOneDevice(deviceID);
        scrapInfo.setScrapTime(nowTime);
        deviceInfo.setScrap(scrapInfo);
        deviceInfo.setStatus(XZ_DEVICE_STATUS.SCRAP_DEVICE.getValue());
        this.saveDevice(deviceInfo);
        //2.删除保养,巡检关系表,维修表
        this.removeRelationByDeviceID(deviceID);

        //3.修改关系表clientID=""的状态为废弃
        //新增clientID为空的关系表
        DeviceRelationInfo deviceRelationInfoTmp = new DeviceRelationInfo.Builder()
                .withGroupID(deviceInfo.getGroupID())
                .withDeviceID(deviceInfo.getDeviceID())
                .withSearchIndex(deviceInfo.makeSearchIndexStr())
                .withClientID("")
                .withDeviceCreateTime(deviceInfo.getCreateTime())
                .withStatus(XZ_DEVICE_STATUS.SCRAP_DEVICE.getValue())
                .build();
        String deviceRelationID = this.saveDeviceRelation(deviceRelationInfoTmp, 0);
        deviceRelationInfoTmp.setDeviceRelationID(deviceRelationID);
        this.saveDeviceRelation(deviceRelationInfoTmp, 0);
    }
    /**
     * 4.删除设备
     */
    @Override
    public String removeDevice(ReqDeviceItem reqDeviceItem) throws BusinessException {
        if (reqDeviceItem == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        String deviceID = reqDeviceItem.getDeviceID();
        //1.删除设备表
        this.removeDeviceInfoByDeviceID(deviceID);
        //2.删除关系表
        this.removeRelationByDeviceID(deviceID);
        //3.删除评论表
        this.removeCommentByDeviceID(deviceID);
        //4.删除故障表
        this.removeBreakdownByDeviceID(deviceID);
        //5.删除维修表
        this.removeFinishByDeviceID(deviceID);
        //6.删除保养表
        this.removeMaintenanceByDeviceID(deviceID);
        //7.删除巡检表
        this.removePollingByDeviceID(deviceID);
        //8.删除停用表
        this.removeDisableByDeviceID(deviceID);

        return deviceID;
    }

    /**
     * 5.全部设备
     */
    @Override
    public ArrayList<ResDeviceInfo> listOfChooseInfo(ReqDeviceSearchItem reqDeviceSearchItem) throws BusinessException {
        if (reqDeviceSearchItem == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        int status = reqDeviceSearchItem.getQueryType().get(0);
        String groupID = reqDeviceSearchItem.getGroupID();
        ArrayList<ResDeviceInfo> list = this.listDevice(null, groupID);

        //根据状态筛选
        if (status != XZ_DEVICE_STATUS.ALL_DEVICE.getValue()) {
            ArrayList<ResDeviceInfo> listTmp = new ArrayList<>();
            for (ResDeviceInfo resDeviceInfo : list) {
                if (resDeviceInfo.getStatus() == status) {
                    listTmp.add(resDeviceInfo);
                }
            }
            return listTmp;
        }
        return this.mergeDevice(list);
    }

    /**
     * 查询等待我处理的设备
     */
    @Override
    public ArrayList<ResDeviceInfo> listWaitMeToDoDevice(String clientID, String groupID) throws BusinessException {
        ArrayList<ResDeviceInfo> returnList = new ArrayList<>();
        if (StrUtil.isNotBlank(clientID) && StrUtil.isNotBlank(groupID)) {

            ReqDeviceSearchItem reqDeviceSearchItem = new ReqDeviceSearchItem.Builder()
                    .withClientID(clientID)
                    .withGroupID(groupID)
                    .build();

            ArrayList<Integer> queryType = new ArrayList<>(1);
            ArrayList<ResDeviceInfo> allDeviceList = new ArrayList<>();
            //查询等待我巡检的设备
            queryType.add(0, 1);
            reqDeviceSearchItem.setQueryType(queryType);
            ArrayList<ResDeviceInfo> poolList = this.listOfOwnDevice(reqDeviceSearchItem);
            if (CollectionUtil.isNotEmpty(poolList)) {
                allDeviceList.addAll(poolList);
            }
            //查询等待我保养的设备
            queryType.add(0, 2);
            reqDeviceSearchItem.setQueryType(queryType);
            ArrayList<ResDeviceInfo> mantanceList = this.listOfOwnDevice(reqDeviceSearchItem);
            if (CollectionUtil.isNotEmpty(mantanceList)) {
                allDeviceList.addAll(mantanceList);
            }
            //查询等待我维修的设备
            queryType.add(0, 3);
            reqDeviceSearchItem.setQueryType(queryType);
            ArrayList<ResDeviceInfo> finishList = this.listOfOwnDevice(reqDeviceSearchItem);
            if (CollectionUtil.isNotEmpty(finishList)) {
                allDeviceList.addAll(finishList);
            }

            //合并带我巡检和带我保养的设备
            if (CollectionUtil.isNotEmpty(allDeviceList)) {
                returnList = this.mergeDevice(allDeviceList);
            }
        }
        return returnList;
    }

    /**
     * 合并待保养和待巡检设备
     */
    private ArrayList<ResDeviceInfo> mergeDevice(ArrayList<ResDeviceInfo> list) {
        ArrayList<ResDeviceInfo> resultList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(list)) {
            HashSet<String> deviceIDList = new HashSet<>();
            ArrayList<String> repeatList = new ArrayList<>();
            for (ResDeviceInfo deviceInfo : list) {
                String deviceID = deviceInfo.getDeviceID();
                if (!deviceIDList.add(deviceID)) {
                    repeatList.add(deviceID);
                }
            }
            if (CollectionUtil.isEmpty(repeatList)) {
                resultList.addAll(list);
            } else {
                //如果设备是重复的
                for (ResDeviceInfo deviceInfo : list) {
                    for (String deviceID : repeatList) {
                        if (deviceID.equals(deviceInfo.getDeviceID())) {
                            deviceInfo.setStatus(XZ_DEVICE_STATUS.TWO_STATUS_DEVICE.getValue());
                        }
                        resultList.add(deviceInfo);
                    }
                }
                SKTools.removeDuplicate(resultList);
            }
        }
        List<ResDeviceInfo> resList = CollectionUtil.sort((Collection<ResDeviceInfo>) resultList, new ResDeviceInfoComparator());

        return CollectionUtil.newArrayList(resList);
    }

    /**
     * 6.搜索设备
     */
    @Override
    public ArrayList<ResDeviceInfo> listSearchDevice(ReqDeviceSearchItem reqDeviceSearchItem) throws BusinessException {
        if (reqDeviceSearchItem == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        String queryStr = reqDeviceSearchItem.getQueryStr();
        String groupID = reqDeviceSearchItem.getGroupID();
        ArrayList<ResDeviceInfo> list = this.listDevice(queryStr, groupID);
        return this.mergeDevice(list);
    }

    /**
     * 生成设备二维码
     */
    @Override
    public String getDeviceQRCode(String deviceID, String groupID, boolean refresh) throws BusinessException {
        if (Strings.isNullOrEmpty(deviceID)) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        DeviceInfo deviceInfo = this.getOneDevice(deviceID);

        String finalURL;


        if (deviceInfo != null) {
            try {
                // 如果有rq同时不会强制刷新那么直接返回
                if (!Strings.isNullOrEmpty(deviceInfo.getQrCode()) && !refresh) {
                    return deviceInfo.getQrCode();
                } else {

                    // 生成 encode值
                    ImmutableMap<String, String> bodyMap = ImmutableMap.of("deviceID", deviceID, "groupID", groupID);
                    ImmutableMap returnMap = ImmutableMap.of("actionType", XZ_QRCODE_TYPE.DEVICE.getValue(), "body", bodyMap);
                    Gson gson = new Gson();
                    String gsonStr = gson.toJson(returnMap);
                    String resultStr = SKTools.base64Encode(gsonStr);
                    String patternStr = "\\s*|\t|\r|\n";
                    Pattern p = Pattern.compile(patternStr);
                    Matcher m = p.matcher(resultStr);
                    resultStr = m.replaceAll("");

                    // 获取目录
                    String filesLocationTmp = System.getProperty("evan.webapp") + "gouliaoweb-1.0/upload/images/";

                    String userIDF = deviceID + SKTools.getNowTimeStamp();
                    String finalQRcodeName = SKTools.getMD5(userIDF) + ".jpg";
                    String qrOroName = "device_" + SKTools.getMD5(userIDF) + ".jpg";


                    // 去生成图片
                    ZXingPic zXingPic = new ZXingPic.Builder()
                            .withFinalPicPath(filesLocationTmp + finalQRcodeName)
                            .withContentStr(resultStr)
                            .withLogoPath("")
                            .withOriQRPath(filesLocationTmp + qrOroName)
                            .withFilePath(filesLocationTmp)
                            .build();
                    log.debug("QRcode maker info is " + zXingPic.toString());
                    String finalPathStr = zXingPic.makeQrcode();

                    log.info("finalPathStr is " + finalPathStr);

                    // 上传
                    QiniuUtil qiniuUtil = new QiniuUtil();
                    qiniuUtil.saveFileConfig(finalPathStr, finalQRcodeName);
                    finalURL = qiniuUtil.upload();

                    // 删除原图
                    this.rmFile(filesLocationTmp + finalQRcodeName);
                    this.rmFile(filesLocationTmp + qrOroName);

                }

            } catch (Exception exp) {

                throw new BusinessException(ReturnCode.CODE_FAIL, "检查参数");
            }

        } else {
            throw new BusinessException(ReturnCode.CODE_FAIL, "设备不存在");
        }


        return finalURL;
    }

    /**
     * 5.待我巡检，待我保养，待我维修列表
     */
    @Override
    public ArrayList<ResDeviceInfo> listOfOwnDevice(ReqDeviceSearchItem reqDeviceSearchItem) throws BusinessException {
        if (reqDeviceSearchItem == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        long nowTime = SKTools.getNowTimeStamp();
        String clientID = reqDeviceSearchItem.getClientID();
        String groupID = reqDeviceSearchItem.getGroupID();
        int status = reqDeviceSearchItem.getQueryType().get(0);
        BasicDBObject gt = new BasicDBObject("$lte", nowTime);
        ArrayList<DeviceRelationInfo> relationInfoList = this.getDeviceRelationByStatus(null, status, clientID, groupID, null, gt);
        ArrayList<ResDeviceInfo> listTmp = new ArrayList<>();
        for (DeviceRelationInfo deviceRelationInfo : relationInfoList) {
            String deviceID = deviceRelationInfo.getDeviceID();
            DeviceInfo deviceInfo = this.getOneDevice(deviceID);
            ResDeviceInfo resDeviceInfo = new ResDeviceInfo.Builder()
                    .withDeviceID(deviceID)
                    .withShowNum(deviceInfo.getShowNum())
                    .withDeviceName(deviceInfo.getDeviceName())
                    .withDeviceModel(deviceInfo.getDeviceModel())
                    .withImgs(deviceInfo.getImgs())
                    .withStatus(status)
                    .build();
            if (deviceInfo.getStatus() != XZ_DEVICE_STATUS.DISABLE_DEVICE.getValue()) {
                listTmp.add(resDeviceInfo);
            }
        }

        return listTmp;
    }

    /**
     * 6.搜索待我巡检，待我保养，待我维修设备列表
     */
    @Override
    public ArrayList<ResDeviceInfo> listOwnSearchDevice(ReqDeviceSearchItem reqDeviceSearchItem) throws BusinessException {
        if (reqDeviceSearchItem == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        long nowTime = SKTools.getNowTimeStamp();
        String clientID = reqDeviceSearchItem.getClientID();
        int status = reqDeviceSearchItem.getQueryType().get(0);
        String groupID = reqDeviceSearchItem.getGroupID();
        String queryStr = reqDeviceSearchItem.getQueryStr();
        BasicDBObject gt = new BasicDBObject("$lt", nowTime + 24 * 3600 * 1000);
        ArrayList<DeviceRelationInfo> relationInfoList = this.getDeviceRelationByStatus(null, status, clientID, groupID, queryStr, gt);
        ArrayList<ResDeviceInfo> list = new ArrayList<>();
        for (DeviceRelationInfo deviceRelationInfo : relationInfoList) {
            String deviceID = deviceRelationInfo.getDeviceID();
            DeviceInfo deviceInfo = this.getOneDevice(deviceID);
            ResDeviceInfo resDeviceInfo = new ResDeviceInfo.Builder()
                    .withDeviceID(deviceID)
                    .withShowNum(deviceInfo.getShowNum())
                    .withDeviceName(deviceInfo.getDeviceName())
                    .withDeviceModel(deviceInfo.getDeviceModel())
                    .withImgs(deviceInfo.getImgs())
                    .withStatus(status)
                    .build();
            list.add(resDeviceInfo);

        }
        ArrayList<ResDeviceInfo> listTmp = new ArrayList<>();
        for (ResDeviceInfo resDeviceInfo : list) {
            if (resDeviceInfo.getStatus() == status) {
                listTmp.add(resDeviceInfo);
            }
        }
        return listTmp;
    }

    /**
     * 7.停用/启用
     */
    @Override
    public ResDisableItem changeStatus(DisableInfo disableInfo) throws BusinessException {
        if (disableInfo == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        String deviceID = disableInfo.getDeviceID();
        ResDisableItem resDisableItem = this.getResDisableItem(disableInfo, deviceID);
        return resDisableItem;
    }

    /**
     * author:Alemand 停用、启用的方法抽取
     */
    private ResDisableItem getResDisableItem(DisableInfo disableInfo, String deviceID) throws BusinessException {
        long nowTime = SKTools.getNowTimeStamp();
        ResDisableItem resDisableItem = null;
        DeviceInfo deviceInfo = this.getOneDevice(deviceID);
        int status = disableInfo.getStatus();
        if (status == XZ_DEVICE_STATUS.DISABLE_DEVICE.getValue()) {

            //删除关系表，除了client=""  deviceID="deviceID" 的记录
            this.removeRelationByDeviceIDAndStatus(deviceID, XZ_DEVICE_STATUS.WAIT_MAINTENANCE_DEVICE);
            this.removeRelationByDeviceIDAndStatus(deviceID, XZ_DEVICE_STATUS.WAIT_POLLING_DEVICE);

            //修改关系表的设备状态为停用
            DeviceRelationInfo deviceRelationInfo = this.getDeviceRelation(deviceID, "");
            deviceRelationInfo.setStatus(XZ_DEVICE_STATUS.DISABLE_DEVICE.getValue());
            this.saveDeviceRelation(deviceRelationInfo, 0);
            //停用时需要加入停用次数
            int disableIDNum = this.getDisableIDNum(deviceID) + 1;
            disableInfo.setDisableIDNum(disableIDNum);

            //停用时更新设备info的状态为停用
            deviceInfo.setStatus(XZ_DEVICE_STATUS.DISABLE_DEVICE.getValue());
            //更新设备info表状态为启用
            this.saveDevice(deviceInfo);

        }
        if (status == XZ_DEVICE_STATUS.ENABLE_DEVICE.getValue()) {
            deviceInfo.setStatus(XZ_DEVICE_STATUS.ENABLE_DEVICE.getValue());
            //更新设备info表状态为启用
            this.saveDevice(deviceInfo);
            //修改关系表的设备状态为启用
            DeviceRelationInfo deviceRelationInfo = this.getDeviceRelation(deviceID, "");

            //添加新的保养，巡检状态
            //保存关系表巡检 保养 记录
            DeviceRelationInfo deviceRelationInfoTmp = new DeviceRelationInfo.Builder()
                    .withGroupID(deviceInfo.getGroupID())
                    .withDeviceID(deviceInfo.getDeviceID())
                    .withSearchIndex(deviceInfo.makeSearchIndexStr())
                    .withStatus(deviceInfo.getStatus())
                    .withDeviceCreateTime(deviceInfo.getCreateTime())
                    .build();
            ArrayList<DeviceRelationInfo> list = this.getDeviceRelationByStatus(deviceID, XZ_DEVICE_STATUS.WAIT_FINISH_DEVICE.getValue(), null, null, null, null);
            //如果不为空，说明设备停用前为维修状态，修改设备为维修状态，设备的待巡检，待保养状态为false
            if (CollectionUtil.isNotEmpty(list)) {
                //1 false 0 true
                resDisableItem = new ResDisableItem.Builder()
                        .withStatus(XZ_DEVICE_STATUS.WAIT_FINISH_DEVICE.getValue())
                        .withIsFinish(1)
                        .withIsMaintenance(0)
                        .withIsPolling(0)
                        .build();
                //修改设备info的状态为维修
                deviceInfo.setStatus(XZ_DEVICE_STATUS.WAIT_FINISH_DEVICE.getValue());
                this.saveDevice(deviceInfo);

                deviceRelationInfo.setStatus(XZ_DEVICE_STATUS.WAIT_FINISH_DEVICE.getValue());
                this.saveDeviceRelation(deviceRelationInfo, 0);


            } else {
                //修改设备info的状态为启用
                deviceInfo.setStatus(XZ_DEVICE_STATUS.ENABLE_DEVICE.getValue());
                this.saveDevice(deviceInfo);

                deviceRelationInfo.setStatus(XZ_DEVICE_STATUS.ENABLE_DEVICE.getValue());
                this.saveDeviceRelation(deviceRelationInfo, 0);
                //更新保养,巡检关系表
                ArrayList<String> maintenanceList = deviceInfo.getMaintenanceList();
                ArrayList<String> pollingList = deviceInfo.getPollingList();
                //计算巡检，保养时间
                if (CollectionUtil.isNotEmpty(maintenanceList)) {
                    this.updateMaintenanceRelation(deviceRelationInfoTmp, nowTime, deviceInfo.getMaintenanceCycle(), maintenanceList);
                }

                if (CollectionUtil.isNotEmpty(pollingList)) {
                    this.updatePollingRelation(deviceRelationInfoTmp, nowTime, deviceInfo.getPollingCycle(), pollingList);
                }

                //调用定时任务
                ArrayList<DeviceRelationInfo> maintenanceRelationList = this.getDeviceRelationByStatus(deviceID, XZ_DEVICE_STATUS.WAIT_MAINTENANCE_DEVICE.getValue(), null, null, null, null);
                ArrayList<DeviceRelationInfo> pollingRelationList = this.getDeviceRelationByStatus(deviceID, XZ_DEVICE_STATUS.WAIT_POLLING_DEVICE.getValue(), null, null, null, null);

                this.sendDelayMsgList(maintenanceRelationList);
                this.sendDelayMsgList(pollingRelationList);

                //生成返回对象
                String clientID = disableInfo.getClientID();
                int isMaintenance = maintenanceList.contains(clientID) ? 1 : 0;
                int isFinish = deviceInfo.getStatus() == XZ_DEVICE_STATUS.WAIT_FINISH_DEVICE.getValue() ? 0 : 1;
                int isPolling = pollingList.contains(clientID) ? 1 : 0;

                resDisableItem = new ResDisableItem.Builder()
                        .withStatus(XZ_DEVICE_STATUS.ENABLE_DEVICE.getValue())
                        .withIsFinish(isFinish)
                        .withIsMaintenance(isMaintenance)
                        .withIsPolling(isPolling)
                        .build();
            }

        }
        //保存停用记录
        disableInfo.setDisableTime(nowTime);
        this.saveDisableInfo(disableInfo);

        return resDisableItem;

    }

    /**
     * 8.单个设备的停用记录列表
     */
    @Override
    public ResDisableList listDisableInfo(ReqDeviceItem reqDeviceItem) throws BusinessException {

        if (reqDeviceItem == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        String deviceID = reqDeviceItem.getDeviceID();
        ArrayList<ResDisableInfo> list = this.listResDisableInfo(deviceID);
        ArrayList<String> clientIDsTmp = new ArrayList<>();
        for (ResDisableInfo resDisableInfoTmp : list) {
            clientIDsTmp.add(resDisableInfoTmp.getDisableClientID());
        }

        SKTools.removeDuplicate(clientIDsTmp);
        ArrayList<TinyClientItem> clientList = this.getTinyClientList(clientIDsTmp, reqDeviceItem.getGroupID());

        ResDisableList resDisableList = new ResDisableList.Builder()
                .withList(list)
                .withClientIDS(clientList)
                .build();

        return resDisableList;
    }

    /**
     * 9.获取设备管理页面待我保养 待我巡检 待我维修count,是否是管理员
     */
    @Override
    public DeviceStatusItem getDeviceStatusNum(ReqDeviceItem reqDeviceItem) throws BusinessException {
        if (reqDeviceItem == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        long nowTime = SKTools.getNowTimeStamp();
        String clientID = reqDeviceItem.getClientID();
        String groupID = reqDeviceItem.getGroupID();

        // 2.isAdmin 是否为管理员
        GroupPermission groupPermissionTmp = groupAdminService.getGroupPremission(groupID, clientID);
        int isAdmin = groupPermissionTmp.getDeviceLevel();


        ReqDeviceSearchItem reqDeviceSearchItem = new ReqDeviceSearchItem();
        reqDeviceSearchItem.setClientID(clientID);
        reqDeviceSearchItem.setGroupID(groupID);
        ArrayList<Integer> querytype = new ArrayList<>(1);

        querytype.add(0, XZ_DEVICE_STATUS.WAIT_MAINTENANCE_DEVICE.getValue());
        reqDeviceSearchItem.setQueryType(querytype);
        int maintenanceCount = listOfOwnDevice(reqDeviceSearchItem).size();

        querytype.add(0, XZ_DEVICE_STATUS.WAIT_POLLING_DEVICE.getValue());
        reqDeviceSearchItem.setQueryType(querytype);
        int pollingCount = listOfOwnDevice(reqDeviceSearchItem).size();

        querytype.add(0, XZ_DEVICE_STATUS.WAIT_FINISH_DEVICE.getValue());
        reqDeviceSearchItem.setQueryType(querytype);
        int finishCount = listOfOwnDevice(reqDeviceSearchItem).size();


        return new DeviceStatusItem.Builder()
                .withIsFinishNum(finishCount)
                .withIsMaintenanceNum(maintenanceCount)
                .withIsPollingNum(pollingCount)
                .withIsAdmin(isAdmin)
                .build();
    }

    /**
     * 10.发现故障
     */
    @Override
    public String createBreakdown(BreakdownInfo breakdownInfo) throws BusinessException {
        long nowTime = SKTools.getNowTimeStamp();

        if (breakdownInfo == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        String deviceID = breakdownInfo.getDeviceID();


        DeviceInfo deviceInfo = this.getOneDevice(deviceID);

        if (deviceInfo.getStatus() == XZ_DEVICE_STATUS.DISABLE_DEVICE.getValue()) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "设备状态异常");
        }
        String clientID = breakdownInfo.getClientID();
        NewClient newClient = clientService.findById(Integer.parseInt(clientID));
        //更新设备info表状态为维修
        deviceInfo.setStatus(XZ_DEVICE_STATUS.WAIT_FINISH_DEVICE.getValue());
        this.saveDevice(deviceInfo);

        //删除关系表，除了client=""  deviceID="deviceID" 的记录
        this.removeRelationByDeviceIDAndStatus(deviceID, XZ_DEVICE_STATUS.WAIT_MAINTENANCE_DEVICE);
        this.removeRelationByDeviceIDAndStatus(deviceID, XZ_DEVICE_STATUS.WAIT_POLLING_DEVICE);
        //添加 维修的 关系表记录
        ArrayList<String> fixClientList = breakdownInfo.getFixClientList();
        //发送消息卡片
        // 1.4.1 修改 姓名发起的设备名称(编号:设备编号)待维修
        String title = StrUtil.format("{}发起的{}(编号:{})待维修", newClient.getUserName(), deviceInfo.getDeviceName(), deviceInfo.getShowNum());
        SubMsgDevice subMsgDevice = new SubMsgDevice.Builder()
                .withDeviceID(deviceID)
                .withShowNum(deviceInfo.getShowNum())
                .withFromID(ConstantDefine.MSG_ASSISTANT_DEVICE_ID)
                .withFromName(ConstantDefine.MSG_ASSISTANT_DEVICE_NAME)
                .withDeviceName(deviceInfo.getDeviceName())
                .withDeviceModel(deviceInfo.getDeviceModel())
                .withGroupID(deviceInfo.getGroupID())
                .withDeviceModel(deviceInfo.getDeviceModel())
                .withStatus(XZ_DEVICE_STATUS.WAIT_FINISH_DEVICE.getValue())
                .withDeviceInfo(deviceInfo.getInfo())
                .withBreakdownDes(breakdownInfo.getDes())
                .withTitle(title)
                .build();
        if (CollectionUtil.isNotEmpty(fixClientList)) {
            for (String clientIDtmp : fixClientList) {
                groupSendMsgService.sendDeviceMsg(subMsgDevice, clientService.findById(Integer.parseInt(clientIDtmp)));
            }
        }
        int isMustArrvied = breakdownInfo.getIsMustArrvied(); //0 false 1 true

        if (isMustArrvied == 1) {
            this.sendBreakdownMustArrice(breakdownInfo, deviceInfo, title, nowTime, fixClientList);
        }


        DeviceRelationInfo deviceRelationInfo = new DeviceRelationInfo.Builder()
                .withGroupID(deviceInfo.getGroupID())
                .withDeviceID(deviceInfo.getDeviceID())
                .withSearchIndex(deviceInfo.makeSearchIndexStr())
                .withStatus(deviceInfo.getStatus())
                .withTimestamp(nowTime)
                .build();

        this.updateFinishRelation(deviceRelationInfo, fixClientList);
        //更新关系表client="" and deviceID="deviceID" 的状态为待维修
        DeviceRelationInfo deviceRelationInfoTmp = this.getDeviceRelation(deviceID, "");
        deviceRelationInfoTmp.setStatus(XZ_DEVICE_STATUS.WAIT_FINISH_DEVICE.getValue());
        this.updateDeviceRelation(deviceRelationInfoTmp, 0);

        if (CollectionUtil.isEmpty(breakdownInfo.getImgList())) {
            breakdownInfo.setImgList(new ArrayList<String>());
        }

        breakdownInfo.setBreakdownTime(nowTime);
        String breakdownID = this.saveBreakdownInfo(breakdownInfo);
        breakdownInfo.setBreakdownID(breakdownID);
        this.saveBreakdownInfo(breakdownInfo);
        return breakdownID;


    }

    /*
     * 发送故障必达
     */
    private String sendBreakdownMustArrice(BreakdownInfo breakdownInfo, DeviceInfo deviceInfo, String title, long nowTime, ArrayList<String> fixClientList) throws BusinessException {
        SubMustArriveDevice subMustArriveDevice = new SubMustArriveDevice.Builder()
                .withTextStr("")
                .withPostClientID(breakdownInfo.getClientID())
                .withGroupID(breakdownInfo.getGroupID())
                .withDeviceID(deviceInfo.getDeviceID())
                .withTitle(title)
                .withShowNum(deviceInfo.getShowNum())
                .withDeviceName(deviceInfo.getDeviceName())
                .withDeviceModel(deviceInfo.getDeviceModel())
                .withStatus(XZ_DEVICE_STATUS.WAIT_FINISH_DEVICE.getValue())
                .withDeviceInfo(deviceInfo.getInfo())
                .withBreakdownDes(breakdownInfo.getDes())
                .withMustArriveType(XZ_MUSTARRIVE_TYPE.NORMAL_MSG_DEVICEMANAGE.getValue())
                .build();
        /*
         *发送 必达
         *
         */
        MustArriveItem mustArriveItem = new MustArriveItem.Builder()
                .withPostClientID(breakdownInfo.getClientID())
                .withType(XZ_MUSTARRIVE_TYPE.NORMAL_MSG_DEVICEMANAGE.getValue())
                .withImgs(new ArrayList<String>())
                .withReciveList(fixClientList)
                .withSendType(XZ_MUSTARRIVED_SEND_TYPE.App.getValue())
                .withPostTime(nowTime)
                .withSendTime(nowTime)
                .withTimeDelay(XZ_MUSTARRIVED_TIMEDELAY_TYPE.SendNow.getValue())
                .build();

        mustArriveItem.setBody(subMustArriveDevice);

        return mustArriveService.createMustArriveServie(mustArriveItem);
    }


    /**
     * 修改故障
     */
    @Override
    public String updateBreakdown(BreakdownInfo breakdownInfo) throws BusinessException {
        long nowTime = SKTools.getNowTimeStamp();

        if (breakdownInfo == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        String deviceID = breakdownInfo.getDeviceID();

        DeviceInfo deviceInfo = this.getOneDevice(deviceID);

        if (deviceInfo.getStatus() != XZ_DEVICE_STATUS.WAIT_FINISH_DEVICE.getValue()) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "设备状态异常");
        }
        String postClientID = breakdownInfo.getClientID();
        NewClient newClient = clientService.findById(Integer.parseInt(postClientID));

        //删除原有的故障记录
        this.removeRelationByDeviceIDAndStatus(deviceID, XZ_DEVICE_STATUS.WAIT_FINISH_DEVICE);
        //新增为空的关系表
        DeviceRelationInfo deviceRelationInfoTmp = new DeviceRelationInfo.Builder()
                .withGroupID(deviceInfo.getGroupID())
                .withDeviceID(deviceInfo.getDeviceID())
                .withClientID("")
                .withSearchIndex(deviceInfo.makeSearchIndexStr())
                .withStatus(deviceInfo.getStatus())
                .withTimestamp(nowTime)
                .build();


        String deviceRelationID = this.saveDeviceRelation(deviceRelationInfoTmp, deviceInfo.getCreateTime());
        deviceRelationInfoTmp.setDeviceRelationID(deviceRelationID);
        this.saveDeviceRelation(deviceRelationInfoTmp, deviceInfo.getCreateTime());

        //添加 维修的 关系表记录
        ArrayList<String> fixClientList = breakdownInfo.getFixClientList();

        //发送消息卡片
        String title = StrUtil.format("{}发起的{}(编号:{})待维修", newClient.getUserName(), deviceInfo.getDeviceName(), deviceInfo.getShowNum());
        SubMsgDevice subMsgDevice = new SubMsgDevice.Builder()
                .withDeviceID(deviceID)
                .withShowNum(deviceInfo.getShowNum())
                .withFromID(ConstantDefine.MSG_ASSISTANT_DEVICE_ID)
                .withFromName(ConstantDefine.MSG_ASSISTANT_DEVICE_NAME)
                .withDeviceName(deviceInfo.getDeviceName())
                .withDeviceModel(deviceInfo.getDeviceModel())
                .withGroupID(deviceInfo.getGroupID())
                .withDeviceModel(deviceInfo.getDeviceModel())
                .withStatus(XZ_DEVICE_STATUS.WAIT_FINISH_DEVICE.getValue())
                .withDeviceInfo(deviceInfo.getInfo())
                .withBreakdownDes(breakdownInfo.getDes())
                .withTitle(title)
                .build();
        if (CollectionUtil.isNotEmpty(fixClientList)) {
            for (String clientIDtmp : fixClientList) {
                groupSendMsgService.sendDeviceMsg(subMsgDevice, clientService.findById(Integer.parseInt(clientIDtmp)));
            }
        }

        int isMustArrvied = breakdownInfo.getIsMustArrvied(); //0 false 1 true

        if (isMustArrvied == 1) {
            this.sendBreakdownMustArrice(breakdownInfo, deviceInfo, title, nowTime, fixClientList);
        }

        DeviceRelationInfo deviceRelationInfo = new DeviceRelationInfo.Builder()
                .withGroupID(deviceInfo.getGroupID())
                .withDeviceID(deviceInfo.getDeviceID())
                .withSearchIndex(deviceInfo.makeSearchIndexStr())
                .withStatus(deviceInfo.getStatus())
                .build();

        this.updateFinishRelation(deviceRelationInfo, fixClientList);


        if (CollectionUtil.isEmpty(breakdownInfo.getImgList())) {
            breakdownInfo.setImgList(new ArrayList<String>());
        }
        String breakdownID = breakdownInfo.getBreakdownID();
        breakdownInfo.setBreakdownTime(nowTime);
        this.saveBreakdownInfo(breakdownInfo);
        return breakdownID;
    }

    /**
     * 故障详情
     */
    @Override
    public ResBreakdownItem getOneBreakdown(ReqDeviceItem reqDeviceItem) throws BusinessException {
        if (reqDeviceItem == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        String clientID = reqDeviceItem.getClientID();
        String deviceID = reqDeviceItem.getDeviceID();
        String groupID = reqDeviceItem.getGroupID();

        DeviceInfo deviceInfo = this.getOneDevice(deviceID);
        String deviceGroupID = deviceInfo.getGroupID();

        int isGroupMember = this.isGroupMember(deviceGroupID, clientID);

        if (isGroupMember != 1) {
            throw new BusinessException(ReturnCode.CODE_DEVICE_CAN_NOT_VIEW, "抱歉，您不属于此项目部，无法查看设备");

        }
        ResBreakdownItem resBreakdownItem = this.getBreakdown(deviceID);
        long nowTime = SKTools.getNowTimeStamp();
        BasicDBObject gt = new BasicDBObject("$lt", nowTime + 24 * 3600 * 1000);
        ArrayList<DeviceRelationInfo> finishList = this.getDeviceRelationByStatus(deviceID, XZ_DEVICE_STATUS.WAIT_FINISH_DEVICE.getValue(), clientID, groupID, null, gt);
        ArrayList<DeviceRelationInfo> pollingList = this.getDeviceRelationByStatus(deviceID, XZ_DEVICE_STATUS.WAIT_POLLING_DEVICE.getValue(), clientID, groupID, null, gt);
        ArrayList<DeviceRelationInfo> maintenanceList = this.getDeviceRelationByStatus(deviceID, XZ_DEVICE_STATUS.WAIT_MAINTENANCE_DEVICE.getValue(), clientID, groupID, null, gt);
        int isFinish = CollectionUtil.isEmpty(finishList) ? 0 : 1;
        int isPolling = CollectionUtil.isEmpty(pollingList) ? 0 : 1;
        int isMaintenance = CollectionUtil.isEmpty(maintenanceList) ? 0 : 1;
        resBreakdownItem.setIsFinish(isFinish);
        resBreakdownItem.setIsPolling(isPolling);
        resBreakdownItem.setIsMaintenance(isMaintenance);
        return resBreakdownItem;
    }

    /**
     * 11.维修回复(故障恢复)
     */
    @Override
    public String finishBreakdown(FinishInfo finishInfo) throws BusinessException {
        if (finishInfo == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        long nowTime = SKTools.getNowTimeStamp();
        String breakdownID = finishInfo.getBreakdownID();
        String deviceID = finishInfo.getDeviceID();
        String clientID = finishInfo.getClientID();
        int finishIDNumTmp = this.getFinishIDNum(deviceID) + 1;
        ResBreakdownItem resBreakdownItem = this.getBreakdown(deviceID);
        DeviceInfo deviceInfo = this.getOneDevice(deviceID);

        int status = deviceInfo.getStatus();
        if (status == XZ_DEVICE_STATUS.SCRAP_DEVICE.getValue()) {
            throw new BusinessException(ReturnCode.CODE_DEVICE_SCRAP, "此设备已报废!");
        }
        ArrayList<DeviceRelationInfo> finishList = this.getDeviceRelationByStatus(deviceID, XZ_DEVICE_STATUS.WAIT_FINISH_DEVICE.getValue(), null, null, null, null);
        if (CollectionUtil.isEmpty(finishList)) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "此设备已经完成维修，请勿重复提交");
        }

        finishInfo.setFinishDate(nowTime);
        List<String> fixClients = resBreakdownItem.getBreakdown().getFixClientList();

        //是否还是维修人
        int isFinishMember = fixClients.contains(clientID) ? 1 : 0;


        finishInfo.setFinishIDNum(finishIDNumTmp);
        finishInfo.setIsFinishMember(isFinishMember);
        finishInfo.setCurrentStatus(status);

        String finishID = this.saveDeviceFinish(finishInfo);
        finishInfo.setFinishID(finishID);
        this.saveDeviceFinish(finishInfo);

        //删除关系表的维修关系
        this.removeRelationByDeviceIDAndStatus(deviceID, XZ_DEVICE_STATUS.WAIT_FINISH_DEVICE);

        //如果设备的状态为维修状态，修改为正常，停用，不修改设备状态，不添加维修保养关系
        if (deviceInfo.getStatus() == XZ_DEVICE_STATUS.WAIT_FINISH_DEVICE.getValue()) {
            deviceInfo.setStatus(XZ_DEVICE_STATUS.ENABLE_DEVICE.getValue());
            this.saveDevice(deviceInfo);

            //更新关系表 添加 保养,巡检状态
            DeviceRelationInfo deviceRelationInfo = new DeviceRelationInfo.Builder()
                    .withGroupID(deviceInfo.getGroupID())
                    .withDeviceID(deviceInfo.getDeviceID())
                    .withClientID("")
                    .withStatus(deviceInfo.getStatus())
                    .withSearchIndex(deviceInfo.makeSearchIndexStr())
                    .build();
            String deviceRelationID = this.saveDeviceRelation(deviceRelationInfo, deviceInfo.getCreateTime());
            deviceRelationInfo.setDeviceRelationID(deviceRelationID);
            this.saveDeviceRelation(deviceRelationInfo, deviceInfo.getCreateTime());
            deviceRelationInfo.setDeviceRelationID(null);
            ArrayList<String> maintenanceList = deviceInfo.getMaintenanceList();
            ArrayList<String> pollingList = deviceInfo.getPollingList();
            //计算巡检，保养时间
            if (CollectionUtil.isNotEmpty(maintenanceList)) {
                this.updateMaintenanceRelation(deviceRelationInfo, nowTime, deviceInfo.getMaintenanceCycle(), maintenanceList);
            }
            if (CollectionUtil.isNotEmpty(pollingList)) {
                this.updatePollingRelation(deviceRelationInfo, nowTime, deviceInfo.getPollingCycle(), pollingList);
            }

            //调用定时任务
            ArrayList<DeviceRelationInfo> maintenanceRelationList = this.getDeviceRelationByStatus(deviceID, XZ_DEVICE_STATUS.WAIT_MAINTENANCE_DEVICE.getValue(), null, null, null, null);
            ArrayList<DeviceRelationInfo> pollingRelationList = this.getDeviceRelationByStatus(deviceID, XZ_DEVICE_STATUS.WAIT_POLLING_DEVICE.getValue(), null, null, null, null);

            this.sendDelayMsgList(maintenanceRelationList);
            this.sendDelayMsgList(pollingRelationList);
        }

        //1.4.1 优化  维修完成给故障发起人发送消息
        BreakdownInfo breakdownInfo = this.getOneBreakdownInfo(breakdownID);
        String findBreakClientID = breakdownInfo.getClientID();
        NewClient findBreakClient = clientService.findById(Integer.parseInt(findBreakClientID));
        String finishClientID = finishInfo.getClientID();
        NewClient finishClient = clientService.findById(Integer.parseInt(finishClientID));
        // 姓名已维修设备名称(编号:设备编号)
        String title = StrUtil.format("{}已维修{}(编号:{})", finishClient.getUserName(), deviceInfo.getDeviceName(), deviceInfo.getShowNum());
        SubMsgDevice subMsgDevice = new SubMsgDevice.Builder()
                .withDeviceID(deviceID)
                .withShowNum(deviceInfo.getShowNum())
                .withFromID(ConstantDefine.MSG_ASSISTANT_DEVICE_ID)
                .withFromName(ConstantDefine.MSG_ASSISTANT_DEVICE_NAME)
                .withDeviceName(deviceInfo.getDeviceName())
                .withDeviceModel(deviceInfo.getDeviceModel())
                .withGroupID(deviceInfo.getGroupID())
                .withDeviceModel(deviceInfo.getDeviceModel())
                .withStatus(XZ_DEVICE_STATUS.ALREADY_FINISH_BREAKDOWN.getValue())
                .withDeviceInfo(deviceInfo.getInfo())
                .withBreakdownDes(breakdownInfo.getDes())
                .withTitle(title)
                .build();
        groupSendMsgService.sendDeviceMsg(subMsgDevice, findBreakClient);

        return finishID;
    }


    /**
     * 13.单个设备的维修记录列表
     */
    @Override
    public ResRepairInfo listRepairInfo(ReqDeviceItem reqDeviceItem) throws BusinessException {
        if (reqDeviceItem == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        ResRepairInfo resRepairInfo = new ResRepairInfo();
        String deviceID = reqDeviceItem.getDeviceID();
        ArrayList<ResFixItem> list = this.listResFixItem(deviceID);
        ArrayList<String> clientIDSTmp = new ArrayList<>();
        for (ResFixItem resFixItem : list) {
            ResFinishItem resFinishItem = resFixItem.getFinish();
            clientIDSTmp.add(resFinishItem.getFinishClientID());
            ResFixBreakdownItem resFixBreakdownItem = resFixItem.getBreakdown();
            clientIDSTmp.add(resFixBreakdownItem.getPostClientID());
            if (!CollectionUtil.isEmpty(resFixBreakdownItem.getFixClientList())) {
                clientIDSTmp.addAll(resFixBreakdownItem.getFixClientList());
            }
        }
        SKTools.removeDuplicate(clientIDSTmp);
        ArrayList<TinyClientItem> clientList = this.getTinyClientList(clientIDSTmp, reqDeviceItem.getGroupID());
        resRepairInfo.setList(list);
        resRepairInfo.setClientIDS(clientList);
        return resRepairInfo;
    }

    /**
     * 14.保养回复
     */
    @Override
    public String finishMaintenance(MaintenanceInfo maintenanceInfo) throws BusinessException {
        long nowTime = SKTools.getNowTimeStamp();
        if (maintenanceInfo == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        String deviceID = maintenanceInfo.getDeviceID();
        DeviceInfo deviceInfo = this.getOneDevice(deviceID);
        int status = deviceInfo.getStatus();
        if (status == XZ_DEVICE_STATUS.SCRAP_DEVICE.getValue()) {
            throw new BusinessException(ReturnCode.CODE_DEVICE_SCRAP, "此设备已报废!");
        }
        String clientID = maintenanceInfo.getClientID();

        if (deviceInfo.getStatus() != XZ_DEVICE_STATUS.DISABLE_DEVICE.getValue()) {
            int maintenanceCycle = deviceInfo.getMaintenanceCycle();
            //更新关系表的下次保养时间
            this.updateDeviceRelationTimestamp(deviceID, XZ_DEVICE_STATUS.WAIT_MAINTENANCE_DEVICE.getValue(), nowTime, maintenanceCycle);

            //发送保养消息 调用定时任务
            ArrayList<DeviceRelationInfo> maintenanceRelationList = this.getDeviceRelationByStatus(deviceID, XZ_DEVICE_STATUS.WAIT_MAINTENANCE_DEVICE.getValue(), null, null, null, null);
            this.sendDelayMsgList(maintenanceRelationList);

        }

        //保养时设备的状态
        int currentStatus = deviceInfo.getStatus();
        // 第几次保养
        int maintenanceIDNum = this.getMaintenanceIDNum(deviceID) + 1;
        // 是否还是保养人
        ArrayList<String> maintenanceClientList = deviceInfo.getMaintenanceList();
        int isMaintenanceMember = maintenanceClientList.contains(clientID) ? 1 : 0;

        maintenanceInfo.setTimestamp(nowTime);
        maintenanceInfo.setCurrentStatus(currentStatus);
        maintenanceInfo.setIsMaintenanceMember(isMaintenanceMember);
        maintenanceInfo.setMaintenanceIDNum(maintenanceIDNum);
        String maintenanceID = this.saveMaintenanceInfo(maintenanceInfo);
        maintenanceInfo.setMaintenanceID(maintenanceID);
        this.saveMaintenanceInfo(maintenanceInfo);
        return maintenanceID;
    }


    /**
     * 16.单个设备的保养记录列表
     */
    @Override
    public ResMaintenanceList listMaintenanceInfo(ReqDeviceItem reqDeviceItem) throws BusinessException {
        if (reqDeviceItem == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        String deviceID = reqDeviceItem.getDeviceID();
        ArrayList<ResMaintenanceItem> list = this.listResMaintenanceInfo(deviceID);
        ArrayList<String> clientIDSTmp = new ArrayList<>();
        for (ResMaintenanceItem resMaintenanceItem : list) {
            clientIDSTmp.add(resMaintenanceItem.getMaintenanceClientID());
        }
        SKTools.removeDuplicate(clientIDSTmp);
        ArrayList<TinyClientItem> clientList = this.getTinyClientList(clientIDSTmp, reqDeviceItem.getGroupID());

        ResMaintenanceList resMaintenanceList = new ResMaintenanceList.Builder()
                .withList(list)
                .withClientIDS(clientList)
                .build();

        return resMaintenanceList;
    }

    /**
     * 17.巡检回复
     */
    @Override
    public String finishPolling(PollingInfo pollingInfo) throws BusinessException {
        long nowTime = SKTools.getNowTimeStamp();
        if (pollingInfo == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        String deviceID = pollingInfo.getDeviceID();
        String clientID = pollingInfo.getClientID();
        DeviceInfo deviceInfo = this.getOneDevice(deviceID);
        int pollingCycle = deviceInfo.getPollingCycle();
        int status = deviceInfo.getStatus();
        if (status == XZ_DEVICE_STATUS.SCRAP_DEVICE.getValue()) {
            throw new BusinessException(ReturnCode.CODE_DEVICE_SCRAP, "此设备已报废!");
        }
        //更新关系表的下次巡检时间
        this.updateDeviceRelationTimestamp(deviceID, XZ_DEVICE_STATUS.WAIT_POLLING_DEVICE.getValue(), nowTime, pollingCycle);


        //调用定时任务 发送巡检消息
        ArrayList<DeviceRelationInfo> pollingRelationList = this.getDeviceRelationByStatus(deviceID, XZ_DEVICE_STATUS.WAIT_POLLING_DEVICE.getValue(), null, null, null, null);

        this.sendDelayMsgList(pollingRelationList);

        //巡检时设备的状态
        int currentStatus = deviceInfo.getStatus();
        // 第几次巡检
        int pollingIDNum = this.getPollingIDNum(deviceID) + 1;
        // 是否还是巡检人
        ArrayList<String> pollingClientList = deviceInfo.getPollingList();
        int isPollingMember = pollingClientList.contains(clientID) ? 0 : 1;

        pollingInfo.setTimestamp(nowTime);
        pollingInfo.setCurrentStatus(currentStatus);
        pollingInfo.setIsPollingMember(isPollingMember);
        pollingInfo.setPollingIDNum(pollingIDNum);
        String pollingID = this.savePollingInfo(pollingInfo);
        pollingInfo.setPollingID(pollingID);
        this.savePollingInfo(pollingInfo);
        return pollingID;
    }


    /**
     * 19.单个设备的巡检记录列表
     */
    @Override
    public ResPollingList listPollingInfo(ReqDeviceItem reqDeviceItem) throws BusinessException {
        if (reqDeviceItem == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        String deviceID = reqDeviceItem.getDeviceID();
        ArrayList<ResPollingItem> list = this.listResPollingInfo(deviceID);
        ArrayList<String> clientIDSTmp = new ArrayList<>();
        for (ResPollingItem resPollingItem : list) {
            clientIDSTmp.add(resPollingItem.getPollingClientID());
        }
        SKTools.removeDuplicate(clientIDSTmp);
        ArrayList<TinyClientItem> clientList = this.getTinyClientList(clientIDSTmp, reqDeviceItem.getGroupID());

        ResPollingList resMaintenanceList = new ResPollingList.Builder()
                .withList(list)
                .withClientIDS(clientList)
                .build();

        return resMaintenanceList;
    }

    /**
     * 20.添加评论
     */
    @Override
    public String addComment(DeviceCommentItem deviceCommentItem) throws BusinessException {
        if (deviceCommentItem == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        long nowTime = SKTools.getNowTimeStamp();

        if (CollectionUtil.isEmpty(deviceCommentItem.getImgList())) {
            deviceCommentItem.setImgList(new ArrayList<String>());
        }
        deviceCommentItem.setCommentTime(nowTime);

        String commentID = this.saveComment(deviceCommentItem);

        //发送消息
        String deviceID = deviceCommentItem.getDeviceID();
        DeviceInfo deviceInfo = this.getOneDevice(deviceID);
        String commentClientID = deviceCommentItem.getClientID();
        NewClient commentClient = clientService.findById(Integer.parseInt(commentClientID));
        String title = StrUtil.format("{}评论了{}（编号:{}）", commentClient.getUserName(), deviceInfo.getDeviceName(), deviceInfo.getShowNum());
        SubMsgDevice subMsgDevice = new SubMsgDevice.Builder()
                .withDeviceID(deviceID)
                .withShowNum(deviceInfo.getShowNum())
                .withFromID(ConstantDefine.MSG_ASSISTANT_DEVICE_ID)
                .withFromName(ConstantDefine.MSG_ASSISTANT_DEVICE_NAME)
                .withDeviceName(deviceInfo.getDeviceName())
                .withDeviceModel(deviceInfo.getDeviceModel())
                .withGroupID(deviceInfo.getGroupID())
                .withDeviceModel(deviceInfo.getDeviceModel())
                .withDeviceInfo(deviceInfo.getInfo())
                .withTimestamp(nowTime)
                .withTitle(title)
                .withCommentClientID(commentClientID)
                .withCommentDetails(deviceCommentItem.getComment())
                .build();


        ArrayList<String> memberIDList = new ArrayList<>();

        //评论消息只是推送给总、子、设备管理员
        String groupID = deviceInfo.getGroupID();
        //获取项目部所有成员的ID
        ArrayList<String> memberList = groupMemberService.getGroupAllClientID(groupID);
        if (CollectionUtil.isNotEmpty(memberList)) {
            for (String clientID : memberList) {
                boolean groupModelAdmin = groupAdminService.isGroupModelAdmin(clientID, groupID, XZ_ADMIN_TYPE.Device.getValue());
                if (groupModelAdmin) {
                    memberIDList.add(clientID);
                }
            }
        }
        SKTools.removeDuplicate(memberIDList);


        for (String clientID : memberIDList) {
            //不给评论人发送消息
            if (!commentClientID.equals(clientID)) {
                NewClient sendToClient = clientService.findById(Integer.parseInt(clientID));
                if (sendToClient != null) {
                    groupSendMsgService.sendDeviceCommentMsg(subMsgDevice, sendToClient);
                }
            }
        }


        return commentID;

    }

    /**
     * 21.评论列表
     */
    @Override
    public ArrayList<ResCommentItem> listDeviceComment(ReqDeviceItem reqDeviceItem) throws BusinessException {
        if (reqDeviceItem == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        return this.listDeviceComment(reqDeviceItem.getDeviceID());
    }

    /**
     * 保养，巡检，回调发送消息
     */
    @Override
    public void nowSendDelaySend(TimerNewJob timerNewJob) throws BusinessException {
        if (timerNewJob == null) {
            return;
        }

        String jobCont = SKTools.base64Decode(timerNewJob.getJobContent());
        Gson gson = new Gson();
        Map sendObj = gson.fromJson(jobCont, Map.class);

        String deviceRelationID = (String) sendObj.get("deviceRelationID");
        DBObject searchDB = new BasicDBObject().append("_id", new ObjectId(deviceRelationID));

        DeviceRelationInfo deviceRelationInfo = this.getDeviceRelationInfo(searchDB);
        if (deviceRelationInfo == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "设备不存在");
        }
        //若果当前时间>发消息时间 才发送消息
        long timestamp = deviceRelationInfo.getTimestamp();
        if (SKTools.getNowTimeStamp() > timestamp) {
            this.nowSendMsg(deviceRelationInfo);
        }
    }

    /**
     * 获取自己发现的所有故障的设备
     */
    @Override
    public ArrayList<ResDeviceInfo> listOwnFindBreakdownDevice(ReqDeviceItem reqDeviceItem) throws BusinessException {
        if (reqDeviceItem == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "对象不能为空");

        }
        String clientID = reqDeviceItem.getClientID();
        String groupID = reqDeviceItem.getGroupID();
        ArrayList<BreakdownInfo> findList = this.listOwnFindBreakdown(clientID, groupID);
        ArrayList<ResDeviceInfo> returnList = new ArrayList<>();
        for (BreakdownInfo breakInfo : findList) {
            String deviceID = breakInfo.getDeviceID();
            int status = this.getDeviceStatus(deviceID, clientID, groupID).getValue();
            DeviceInfo deviceInfo = this.getOneDevice(deviceID);
            ResDeviceInfo resDeviceInfo = new ResDeviceInfo.Builder()
                    .withDeviceID(deviceID)
                    .withDeviceModel(deviceInfo.getDeviceModel())
                    .withDeviceName(deviceInfo.getDeviceName())
                    .withShowNum(deviceInfo.getShowNum())
                    .withImgs(deviceInfo.getImgs())
                    .withStatus(status)
                    .withLocation(deviceInfo.getLocation())
                    .withQrCode(deviceInfo.getQrCode())
                    .build();
            returnList.add(resDeviceInfo);
        }
        SKTools.removeDuplicateKeepOrder(returnList);
        return returnList;
    }

    /**
     * 记录维修 1.3.10添加
     */
    @Override
    public void ownSaveBreakAndFinish(ReqOwnSaveBreakdown reqOwnSaveBreakdown) throws BusinessException {
        if (reqOwnSaveBreakdown == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "对象不能为空");
        }
        String deviceID = reqOwnSaveBreakdown.getDeviceID();
        DeviceInfo deviceInfo = this.getOneDevice(deviceID);
        if (null == deviceInfo) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "设备不存在");
        }
        /**
         * 首先保存故障
         */
        BreakdownInfo breakdownInfo = reqOwnSaveBreakdown.makeBreakdownInfo();
        String breakID = this.saveBreakdownInfo(breakdownInfo);
        breakdownInfo.setBreakdownID(breakID);
        this.saveBreakdownInfo(breakdownInfo);

        ArrayList<String> fixClientList = reqOwnSaveBreakdown.getFixClientList();
        ArrayList<String> names = new ArrayList<>();
        for (String clientIDTmp : fixClientList) {
            NewClient newClient = clientService.findById(Integer.parseInt(clientIDTmp));
            names.add(newClient.getUserName());
        }
        String fixNameStr = StrUtil.join("、", names);
        /**
         * 再保存维修
         */
        int finishIDNumTmp = this.getFinishIDNum(deviceID) + 1;
        ArrayList<String> fixImgs = CollectionUtil.isNotEmpty(reqOwnSaveBreakdown.getFinishImg()) ? reqOwnSaveBreakdown.getFinishImg() : new ArrayList<String>();
        FinishInfo finishInfo = new FinishInfo.Builder()
                .withFixClientList(reqOwnSaveBreakdown.getFixClientList())
                .withFixNameStr(fixNameStr)
                .withFinishIDNum(finishIDNumTmp)
                .withImgList(fixImgs)
                .withFinishDate(SKTools.getNowTimeStamp())
                .withBreakdownID(breakID)
                .withInfo(reqOwnSaveBreakdown.getFinishDes())
                .withDeviceID(deviceID)
                .withClientID(reqOwnSaveBreakdown.getClientID())
                .withIsFinishMember(1)
                .withCurrentStatus(deviceInfo.getStatus())
                .build();
        String finishID = this.saveDeviceFinish(finishInfo);
        finishInfo.setFinishID(finishID);
        this.saveDeviceFinish(finishInfo);


    }

    /**
     * 单独设备发现故障详情页面
     */
    @Override
    public ResOwnFixInfo getOneDeviceFixInfo(ReqDeviceItem reqDeviceItem) throws BusinessException {
        if (reqDeviceItem == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "对象不能为空");
        }
        String clientID = reqDeviceItem.getClientID();
        String groupID = reqDeviceItem.getGroupID();
        String deviceID = reqDeviceItem.getDeviceID();
        if (StrUtil.isBlank(clientID) ||
                StrUtil.isBlank(groupID) ||
                StrUtil.isBlank(deviceID)) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "clientID groupID deviceID can not be null");

        }
        DeviceInfo deviceInfo = this.getOneDevice(deviceID);
        if (null == deviceInfo) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "设备不存在");
        }
        int status = this.getDeviceStatus(deviceID, clientID, groupID).getValue();
        ResDeviceInfo resDeviceInfo = new ResDeviceInfo.Builder()
                .withDeviceID(deviceID)
                .withDeviceModel(deviceInfo.getDeviceModel())
                .withDeviceName(deviceInfo.getDeviceName())
                .withShowNum(deviceInfo.getShowNum())
                .withImgs(deviceInfo.getImgs())
                .withStatus(status)
                .withLocation(deviceInfo.getLocation())
                .withQrCode(deviceInfo.getQrCode())
                .build();
        ArrayList<TinyClientItem> clients;
        ArrayList<ResFixItem> fixItems = new ArrayList<>();
        ArrayList<String> clientIDList = new ArrayList<>();

        /**
         * 查询出所有的故障
         */
        ArrayList<BreakdownInfo> breakdownInfos = this.getOneDeviceAllBreakdown(clientID, deviceID);
        for (int i = 0; i < breakdownInfos.size(); i++) {
            BreakdownInfo breakdownInfo = breakdownInfos.get(i);
            ResFixBreakdownItem breakdown = new ResFixBreakdownItem.Builder()
                    .withPostClientID(breakdownInfo.getClientID())
                    .withDes(breakdownInfo.getDes())
                    .withDeviceID(deviceID)
                    .withImgList(breakdownInfo.getImgList())
                    .withFixClientList(breakdownInfo.getFixClientList())
                    .build();
            clientIDList.add(breakdownInfo.getClientID());
            String breakdownID = breakdownInfo.getBreakdownID();
            FinishInfo finishInfo = this.getFinishInfoByBreakdownID(breakdownID);
            ResFinishItem finish = null;
            if (finishInfo != null) {
                clientIDList.add(finishInfo.getClientID());
                ArrayList<String> fixClientList = finishInfo.getFixClientList();
                String fixNameStr = "";
                if (CollectionUtil.isNotEmpty(fixClientList)) {
                    clientIDList.addAll(fixClientList);
                    fixNameStr = finishInfo.getFixNameStr();
                } else {
                    ArrayList<String> nowFixList = breakdownInfo.getFixClientList();
                    String cancelStr = nowFixList.contains(finishInfo.getClientID()) ? "" : "(已取消维修人)";
                    NewClient newClient = clientService.findById(Integer.parseInt(finishInfo.getClientID()));
                    fixNameStr = StrUtil.format("{}{}", newClient.getUserName(), cancelStr);
                }
                finish = new ResFinishItem.Builder()
                        .withFinishClientID(finishInfo.getClientID())
                        .withFinishID(finishInfo.getFinishID())
                        .withImgList(finishInfo.getImgList())
                        .withFinishDate(finishInfo.getFinishDate())
                        .withFixClientList(finishInfo.getFixClientList())
                        .withFixNameStr(fixNameStr)
                        .build();


            }


            int fixTime = breakdownInfos.size() - i;
            int currentStatus = finishInfo == null ? deviceInfo.getStatus() : finishInfo.getCurrentStatus();
            ResFixItem resFixItem = new ResFixItem.Builder()
                    .withBreakdown(breakdown)
                    .withCurrentStatus(currentStatus)
                    .withFinish(null)
                    .withIsFinishMember(1)
                    .withFinishIDNum(fixTime)
                    .withFinish(finish)
                    .build();
            fixItems.add(resFixItem);
            /**
             *  public ResFixBreakdownItem breakdown;
             public ResFinishItem finish;
             public int currentStatus;
             public int isFinishMember;
             public int finishIDNum;
             */

        }
        SKTools.removeDuplicate(clientIDList);
        clients = this.getTinyClientList(clientIDList, reqDeviceItem.getGroupID());


        ResOwnFixInfo resOwnFixinfo = new ResOwnFixInfo.Builder()
                .withDeviceInfo(resDeviceInfo)
                .withClientIDS(clients)
                .withList(fixItems)
                .build();
        return resOwnFixinfo;
    }


    /**
     * 获取设备的状态
     */
    private XZ_DEVICE_STATUS getDeviceStatus(String deviceID, String clientID, String groupID) throws BusinessException {

        if (StrUtil.isBlank(deviceID)) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "deviceID cannot be null");

        }
        if (StrUtil.isBlank(clientID) || StrUtil.isBlank(groupID)) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "clientID or groupID cannot be null");

        }
        ReqDeviceSearchItem reqDeviceSearchItem = new ReqDeviceSearchItem();
        reqDeviceSearchItem.setClientID(clientID);
        reqDeviceSearchItem.setGroupID(groupID);
        reqDeviceSearchItem.setQueryType(CollectionUtil.newArrayList(0));
        ArrayList<ResDeviceInfo> list = this.listOfChooseInfo(reqDeviceSearchItem);
        Map<String, ResDeviceInfo> deviceMap = new HashMap<>();
        for (ResDeviceInfo resDeviceInfo : list) {
            deviceMap.put(resDeviceInfo.getDeviceID(), resDeviceInfo);
        }

        ResDeviceInfo resDeviceInfo = deviceMap.get(deviceID);
        if (resDeviceInfo == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "设备不存在");

        }
        return XZ_DEVICE_STATUS.valueOf(resDeviceInfo.getStatus());
    }

    /**
     * 延时发送消息
     */
    private void sendDelayMsgList(ArrayList<DeviceRelationInfo> relationList) throws BusinessException {
        if (CollectionUtil.isEmpty(relationList)) {
            return;
        }
        //获取所有的设备关系，然后发送消息
        for (DeviceRelationInfo deviceRelationInfoTmp : relationList) {
            this.delayedSend(deviceRelationInfoTmp);
        }


    }

    /**
     * 定时发送消息
     */
    private void delayedSend(DeviceRelationInfo deviceRelationInfo) throws BusinessException {

        // 延迟发送
        ImmutableMap<String, String> delay = ImmutableMap.of("deviceRelationID", deviceRelationInfo.getDeviceRelationID());
        Gson gson = new Gson();
        String sendObj = gson.toJson(delay);

        sendObj = SKTools.base64Encode(sendObj);

        //获取巡检和或者保养的时间
        long workTime = deviceRelationInfo.getTimestamp();

        //获取巡检，保养当天的8点
        long sendTime = this.getDayEightTimestamp(workTime);
        TimerNewJob timerNewJob = new TimerNewJob.Builder()
                .withTimestamp(sendTime) //保养，巡检时间到的当天的8点，开始推送消息
                .withJobContent(sendObj)
                .withCallBackURL(callBackURL)
                .build();

        OKHTTPUtil okhttpUtil = new OKHTTPUtil();
        try {
            String spoen = okhttpUtil.configClient(timerURL, timerNewJob).excuteRequest();
            log.info("send delayed  info is " + spoen);
        } catch (Exception e) {
            log.error("定时任务发送失败" + e.getMessage());
        }

    }


    private void nowSendMsg(DeviceRelationInfo deviceRelationInfo) throws BusinessException {
        NewClient postClientTmp = clientService.findById(Integer.valueOf(deviceRelationInfo.getClientID()));

        if (postClientTmp == null) {
            return;
        }
        String deviceID = deviceRelationInfo.getDeviceID();
        DeviceInfo deviceInfo = this.getOneDevice(deviceID);
        int status = deviceRelationInfo.getStatus();
        String needDown = "";
        if (status == XZ_DEVICE_STATUS.WAIT_POLLING_DEVICE.getValue()) {
            needDown = "需巡检";
        }
        if (status == XZ_DEVICE_STATUS.WAIT_MAINTENANCE_DEVICE.getValue()) {
            needDown = "需保养";
        }

        String title = StrUtil.format("{}（编号:{}）{}", deviceInfo.getDeviceName(), deviceInfo.getShowNum(), needDown);
        SubMsgDevice subMsgDevice = new SubMsgDevice.Builder()
                .withDeviceID(deviceID)
                .withShowNum(deviceInfo.getShowNum())
                .withFromID(ConstantDefine.MSG_ASSISTANT_DEVICE_ID)
                .withFromName(ConstantDefine.MSG_ASSISTANT_DEVICE_NAME)
                .withDeviceName(deviceInfo.getDeviceName())
                .withDeviceModel(deviceInfo.getDeviceModel())
                .withGroupID(deviceInfo.getGroupID())
                .withDeviceModel(deviceInfo.getDeviceModel())
                .withStatus(status)
                .withDeviceInfo(deviceInfo.getInfo())
                .withBreakdownDes("")
                .withTitle(title)
                .build();

        groupSendMsgService.sendDeviceMsg(subMsgDevice, postClientTmp);

    }

    private DeviceRelationInfo getDeviceRelationInfo(DBObject searchCondTmp) {
        if (null == searchCondTmp) {
            return null;
        }

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_RELATION);

        DeviceRelationInfo deviceRelationInfo = null;
        try {
            DBCursor cursor = dbCollection.find(searchCondTmp).limit(1);
            List<DBObject> resAry = MongoConnFactory.toList(cursor);
            if (resAry.size() > 0) {
                DBObject dbOBJTmp = resAry.get(0);
                deviceRelationInfo = (DeviceRelationInfo) SKTools.convertDBObjectToBean(dbOBJTmp, DeviceRelationInfo.class);
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }

        return deviceRelationInfo;
    }

    /**
     * 根据项目部ID和数字获取设备编号
     */
    private String getDeviceNum(String groupMsgID, int deviceNum) {
        return groupMsgID + StrUtil.padPre(String.valueOf(deviceNum), 4, '0');

    }

    //////////////////////////////// DevicerelationDB //////////////////////////////////////////

    /**
     * 保存关系
     */
    private String saveDeviceRelation(DeviceRelationInfo deviceRelationInfo, long timestamp) {

        return updateDeviceRelation(deviceRelationInfo, timestamp);
    }

    /**
     * 更新关系
     */
    private String updateDeviceRelation(DeviceRelationInfo deviceRelationInfo, long timestamp) {

        if (null == deviceRelationInfo) {
            return "";
        }
        if (timestamp != 0) {
            deviceRelationInfo.setDeviceCreateTime(timestamp);
        }

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_RELATION);
        DBObject queryObj = SKTools.convertBeanToDBObject(deviceRelationInfo);
        // 如果更新直接保存
        if (deviceRelationInfo.getDeviceRelationID() != null) {
            queryObj.put("_id", new ObjectId(deviceRelationInfo.getDeviceRelationID()));
        }

        if (dbCollection != null) {
            dbCollection.save(queryObj);
        }

        return queryObj.get("_id").toString();
    }

    /**
     * 更新保养时间,巡检时间，状态确定
     */
    private void updateDeviceRelationTimestamp(String deviceID, int status, long timestamp, int cycle) {


        DBCollection dbCollection = MongoConnFactory.getDBCollectionWithName(ConstantDefine.MONGO_COLLECTION_DEVICE_RELATION);

        DBObject searchCond = new BasicDBObject();
        searchCond.put("deviceID", deviceID);
        searchCond.put("status", status);
        long dayEightMs = this.getDayEightTimestamp(timestamp);
        //数字月结导致的设备维修保养时间异常
        long cycleLong = cycle;
        long timeSpan = cycleLong * 24 * 3600 * 1000;
        long remindTime = dayEightMs + timeSpan;
        BasicDBObject statusCond = new BasicDBObject().append("timestamp", remindTime);
        BasicDBObject updateCond = new BasicDBObject().append("$set", statusCond);
        // 更新
        dbCollection.update(searchCond, updateCond, false, true);

    }

    /**
     * 获取关系
     */
    private DeviceRelationInfo getDeviceRelation(String deviceID, String clientID) {
        DeviceRelationInfo deviceRelationInfo = null;
        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_RELATION);
        DBObject filterObjTmp = new BasicDBObject();
        filterObjTmp.put("deviceID", deviceID);
        filterObjTmp.put("clientID", clientID);

        DBCursor cursor = dbCollection.find(filterObjTmp);

        List<DBObject> resAry = MongoConnFactory.toList(cursor);

        try {
            deviceRelationInfo = (DeviceRelationInfo) SKTools.convertDBObjectToBean(resAry.get(0), DeviceRelationInfo.class);
        } catch (Exception e) {
            log.info("can't find device in {} ", deviceID);
            deviceRelationInfo = null;
        }

        return deviceRelationInfo;
    }

    /**
     * 根据设备ID和设备状态获取关系列表
     */
    private ArrayList<DeviceRelationInfo> getDeviceRelationByStatus(String deviceID, int status, String clientID, String groupID, String queryStr, BasicDBObject gt) {
        ArrayList<DeviceRelationInfo> list = new ArrayList<>();
        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_RELATION);
        DBObject filterObjTmp = new BasicDBObject();
        if (StrUtil.isNotBlank(deviceID)) {
            filterObjTmp.put("deviceID", deviceID);
        }
        if (StrUtil.isNotBlank(queryStr)) {
            Pattern pattern = Pattern.compile("^.*" + queryStr + ".*$", Pattern.CASE_INSENSITIVE);
            filterObjTmp.put("searchIndex", pattern);
        }
        filterObjTmp.put("status", status);
        if (status == XZ_DEVICE_STATUS.WAIT_MAINTENANCE_DEVICE.getValue() || status == XZ_DEVICE_STATUS.WAIT_POLLING_DEVICE.getValue()) {
            if (gt != null) {
                filterObjTmp.put("timestamp", gt);
            }
        }
        if (StrUtil.isNotBlank(clientID)) {
            filterObjTmp.put("clientID", clientID);
        }
        if (StrUtil.isNotBlank(groupID)) {
            filterObjTmp.put("groupID", groupID);
        }
        DBObject sortObject = new BasicDBObject("timestamp", -1);

        DBCursor cursor = dbCollection.find(filterObjTmp).sort(sortObject);

        List<DBObject> resAry = MongoConnFactory.toList(cursor);
        for (DBObject dbObjectTmp : resAry) {
            DeviceRelationInfo deviceRelationInfo = (DeviceRelationInfo) SKTools.convertDBObjectToBean(dbObjectTmp, DeviceRelationInfo.class);
            list.add(deviceRelationInfo);

        }

        return list;
    }

    /**
     * 根据clientID和设备状态获取关系列表
     */
    private ArrayList<DeviceRelationInfo> getDeviceRelationByClinetIDAndStatus(String clientID, String groupID, int status, DBObject gt) {
        ArrayList<DeviceRelationInfo> list = new ArrayList<>();
        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_RELATION);
        DBObject filterObjTmp = new BasicDBObject();
        filterObjTmp.put("clientID", clientID);
        filterObjTmp.put("status", status);
        if (status == XZ_DEVICE_STATUS.WAIT_MAINTENANCE_DEVICE.getValue() || status == XZ_DEVICE_STATUS.WAIT_POLLING_DEVICE.getValue()) {
            if (gt != null) {
                filterObjTmp.put("timestamp", gt);
            }
        }
        DBCursor cursor = dbCollection.find(filterObjTmp);
        List<DBObject> resAry = MongoConnFactory.toList(cursor);
        for (DBObject dbObjectTmp : resAry) {
            DeviceRelationInfo deviceRelationInfo = (DeviceRelationInfo) SKTools.convertDBObjectToBean(dbObjectTmp, DeviceRelationInfo.class);
            list.add(deviceRelationInfo);

        }

        return list;
    }

    /**
     * 保存保养关系表
     */
    private void updateMaintenanceRelation(DeviceRelationInfo deviceRelationInfo, long timestamp, int cycle, ArrayList<String> maintenanceClientList) {
        long dayEightMs = this.getDayEightTimestamp(timestamp);
        //数字月结导致的设备维修保养时间异常
        long cycleLong = cycle;
        long timeSpan = cycleLong * 24 * 3600 * 1000;
        long remindTime = dayEightMs + timeSpan;
        for (String maintenanceClientID : maintenanceClientList) {
            deviceRelationInfo.setStatus(XZ_DEVICE_STATUS.WAIT_MAINTENANCE_DEVICE.getValue());
            deviceRelationInfo.setTimestamp(remindTime);
            deviceRelationInfo.setClientID(maintenanceClientID);
            String relationIDTmp = this.saveDeviceRelation(deviceRelationInfo, deviceRelationInfo.getDeviceCreateTime());
            deviceRelationInfo.setDeviceRelationID(relationIDTmp);
            this.saveDeviceRelation(deviceRelationInfo, deviceRelationInfo.getDeviceCreateTime());
            deviceRelationInfo.setDeviceRelationID(null);
        }

    }


    /**
     * 保存巡检关系表
     */
    private void updatePollingRelation(DeviceRelationInfo deviceRelationInfo, long timestamp, int cycle, ArrayList<String> pollingClientList) {
        long dayEightMs = this.getDayEightTimestamp(timestamp);
        //数字月结导致的设备维修保养时间异常
        long cycleLong = cycle;
        long timeSpan = cycleLong * 24 * 3600 * 1000;
        long remindTime = dayEightMs + timeSpan;
        for (String pollingClientID : pollingClientList) {
            deviceRelationInfo.setStatus(XZ_DEVICE_STATUS.WAIT_POLLING_DEVICE.getValue());
            deviceRelationInfo.setTimestamp(remindTime);
            deviceRelationInfo.setClientID(pollingClientID);
            String relationIDTmp = this.saveDeviceRelation(deviceRelationInfo, deviceRelationInfo.getDeviceCreateTime());
            deviceRelationInfo.setDeviceRelationID(relationIDTmp);
            this.saveDeviceRelation(deviceRelationInfo, deviceRelationInfo.getDeviceCreateTime());
            deviceRelationInfo.setDeviceRelationID(null);
        }
    }

    /**
     * 保存维修关系表
     */
    private void updateFinishRelation(DeviceRelationInfo deviceRelationInfo, ArrayList<String> fixClientList) {
        for (String pollingClientID : fixClientList) {
            deviceRelationInfo.setClientID(pollingClientID);
            String relationIDTmp = this.saveDeviceRelation(deviceRelationInfo, deviceRelationInfo.getDeviceCreateTime());
            deviceRelationInfo.setDeviceRelationID(relationIDTmp);
            this.saveDeviceRelation(deviceRelationInfo, deviceRelationInfo.getDeviceCreateTime());
            deviceRelationInfo.setDeviceRelationID(null);
        }
    }


    /**
     * 删除关系表 根据deviceID status
     */
    private boolean removeRelationByDeviceIDAndStatus(String deviceID, XZ_DEVICE_STATUS type) {
        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_RELATION);
        DBObject filterObjTmp = new BasicDBObject();
        int status = type.getValue();
        filterObjTmp.put("deviceID", deviceID);
        filterObjTmp.put("status", status);

        boolean isSuc = false;
        try {
            dbCollection.remove(filterObjTmp);
            isSuc = true;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return isSuc;
    }

    /**
     * 删除关系表 根据deviceID
     */
    private boolean removeRelationByDeviceID(String deviceID) {
        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_RELATION);
        DBObject filterObjTmp = new BasicDBObject();
        filterObjTmp.put("deviceID", deviceID);

        boolean isSuc = false;
        try {
            dbCollection.remove(filterObjTmp);
            isSuc = true;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return isSuc;
    }

    /**
     * 查询关系表，查询所有的设备，有巡检，保养，维修的 过滤掉clientID=""空的记录
     */

    private ArrayList<ResDeviceInfo> listDevice(String queryStr, String groupID) {
        long nowTime = SKTools.getNowTimeStamp();
        ArrayList<ResDeviceInfo> list = new ArrayList<>();
        ArrayList<DeviceRelationInfo> listRelation = new ArrayList<>();
        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_RELATION);
        DBObject filterDbobject = new BasicDBObject();
        if (StrUtil.isNotBlank(queryStr)) {
            Pattern pattern = Pattern.compile("^.*" + queryStr + ".*$", Pattern.CASE_INSENSITIVE);
            filterDbobject.put("searchIndex", pattern);
        }

        //timestamp=0 OR timestamp<nowTime + 24 * 3600 * 1000;
        BasicDBList values = new BasicDBList();
        values.add(new BasicDBObject("timestamp", new BasicDBObject("$eq", 0)));
        values.add(new BasicDBObject("timestamp", new BasicDBObject("$lte", nowTime)));
        filterDbobject.put("$or", values);


        if (StrUtil.isNotBlank(groupID)) {
            filterDbobject.put("groupID", groupID);
        }

        DBObject sortObject = new BasicDBObject();
        sortObject.put("deviceCreateTime", -1);

        DBCursor cursor = dbCollection.find(filterDbobject).sort(sortObject);
        List<DBObject> resAry = MongoConnFactory.toList(cursor);
        for (DBObject dbObjectTmp : resAry) {
            DeviceRelationInfo deviceRelationInfo = (DeviceRelationInfo) SKTools.convertDBObjectToBean(dbObjectTmp, DeviceRelationInfo.class);
            listRelation.add(deviceRelationInfo);

        }


        //记录有巡检，保养，维修的设备id,过滤掉clientID=""的记录

        Map<String, String> existsMap = new HashMap<>();
        for (DeviceRelationInfo deviceRelationInfo : listRelation) {
            String deviceID = deviceRelationInfo.getDeviceID();
            int status = deviceRelationInfo.getStatus();
            if ((status == XZ_DEVICE_STATUS.WAIT_POLLING_DEVICE.getValue())
                    || (status == XZ_DEVICE_STATUS.WAIT_MAINTENANCE_DEVICE.getValue())
                    || status == XZ_DEVICE_STATUS.WAIT_FINISH_DEVICE.getValue()
                    ) {
                existsMap.put(deviceID, "");
            }
        }


        ArrayList<DeviceRelationInfo> willRemoveList = new ArrayList<>();
        for (DeviceRelationInfo deviceRelationInfo : listRelation) {

            String deviceIDTmp = deviceRelationInfo.getDeviceID();
            if (StrUtil.isEmpty(deviceIDTmp)) {
                continue;
            }

            if (existsMap.containsKey(deviceIDTmp)) {
                if (StrUtil.isEmpty(deviceRelationInfo.getClientID())) {
                    willRemoveList.add(deviceRelationInfo);
                }
            }

        }
        // 移除重复的
        listRelation.removeAll(willRemoveList);

        ArrayList<DeviceInfo> listTmp = new ArrayList<>();
        // 查询设备相关信息
        for (DeviceRelationInfo deviceRelationInfo : listRelation) {
            DeviceInfo deviceInfo = this.getOneDevice(deviceRelationInfo.getDeviceID());
            deviceInfo.setStatus(deviceRelationInfo.getStatus());
            listTmp.add(deviceInfo);
        }
        SKTools.removeDuplicate(listTmp);

        List<DeviceInfo> newListTmp = CollectionUtil.sort((Collection<DeviceInfo>) listTmp, new DeviceInfoComparator());
        // 查询设备相关信息

        for (DeviceInfo deviceInfo : newListTmp) {
            String deviceID = deviceInfo.getDeviceID();
            DeviceInfo deviceInfoTmp = this.getOneDevice(deviceID);
            int status = deviceInfoTmp.getStatus();
            ArrayList<String> imgs = deviceInfo.getImgs();
            ArrayList<String> newImgs = new ArrayList<>();
            if (CollectionUtil.isNotEmpty(imgs)) {
                for (String img : imgs) {
                    if (StrUtil.isNotBlank(img)) {
                        newImgs.add(img);
                    }
                }
            }
            ResDeviceInfo resDeviceInfo = new ResDeviceInfo.Builder()
                    .withDeviceID(deviceInfo.getDeviceID())
                    .withShowNum("")
                    .withDeviceName(deviceInfo.getDeviceName())
                    .withDeviceModel(deviceInfo.getDeviceModel())
                    .withImgs(newImgs)
                    .withStatus(deviceInfo.getStatus())
                    .withLocation(deviceInfo.location)
                    .build();
            if (status == XZ_DEVICE_STATUS.DISABLE_DEVICE.getValue()) {
                resDeviceInfo.setStatus(XZ_DEVICE_STATUS.DISABLE_DEVICE.getValue());
            }
            list.add(resDeviceInfo);
        }
        return list;

    }

    //////////////////////////////// DeviceDB //////////////////////////////////////////


    /**
     * 保存设备
     */
    private String saveDevice(DeviceInfo deviceInfo) {

        return updateDeviceDB(deviceInfo);
    }


    /**
     * 更新设备
     */
    private String updateDeviceDB(DeviceInfo deviceInfo) {

        if (null == deviceInfo) {
            return "";
        }

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_INFO);
        DBObject queryObj = SKTools.convertBeanToDBObject(deviceInfo);
        // 如果更新直接保存
        if (deviceInfo.getDeviceID() != null) {
            queryObj.put("_id", new ObjectId(deviceInfo.getDeviceID()));
        }

        if (dbCollection != null) {
            dbCollection.save(queryObj);
        }

        return queryObj.get("_id").toString();
    }

    /**
     * 删除设备info 根据deviceID
     */
    private boolean removeDeviceInfoByDeviceID(String deviceID) {
        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_INFO);
        DBObject filterObjTmp = new BasicDBObject();
        filterObjTmp.put("deviceID", deviceID);

        boolean isSuc = false;
        try {
            dbCollection.remove(filterObjTmp);
            isSuc = true;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return isSuc;
    }

    /**
     * 设备编号使用
     */
    private int findCount(String groupID) {

        int deviceCountTmp = 0;

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_INFO);

        DBObject filterDbobject = new BasicDBObject();
        filterDbobject.put("groupID", groupID);

        DBObject sortObj = new BasicDBObject();
        sortObj.put("innerIndex", -1);

        DBCursor cursor = dbCollection.find(filterDbobject).sort(sortObj).limit(1);
        List<DBObject> resAry = MongoConnFactory.toList(cursor);

        try {
            DeviceInfo deviceInfo = (DeviceInfo) SKTools.convertDBObjectToBean(resAry.get(0), DeviceInfo.class);
            deviceCountTmp = deviceInfo.getInnerIndex();
        } catch (Exception e) {
            log.info("can't find device in {} ", groupID);
            deviceCountTmp = 0;
        }

        return deviceCountTmp;
    }

    /**
     * 判断设备编号是否存在
     */
    private boolean showNumExist(String showNum) {
        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_INFO);

        BasicDBObject filterObj = new BasicDBObject();
        filterObj.put("showNum", showNum);

        DBCursor cursor = dbCollection.find(filterObj).limit(1);
        List<DBObject> resAry = MongoConnFactory.toList(cursor);

        return CollectionUtil.isNotEmpty(resAry);

    }


    /**
     * 获取一个设备信息
     */
    @Override
    public DeviceInfo getOneDevice(String deviceID) {

        DeviceInfo deviceInfo = null;

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_INFO);

        DBObject filterObj = new BasicDBObject();
        filterObj.put("_id", new ObjectId(deviceID));

        DBCursor cursor = dbCollection.find(filterObj);
        List<DBObject> resAry = MongoConnFactory.toList(cursor);

        try {
            deviceInfo = (DeviceInfo) SKTools.convertDBObjectToBean(resAry.get(0), DeviceInfo.class);
        } catch (Exception e) {
            log.info("can't find device in {} ", deviceID);
            deviceInfo = null;
        }

        return deviceInfo;
    }

    /**
     * 获取一个设备信息
     */
    private ResDeviceItem getOneDeviceItem(String deviceID) {

        ResDeviceItem resDeviceItem = null;

        try {
            DeviceInfo deviceInfo = this.getOneDevice(deviceID);
            ScrapInfo scrapInfo = deviceInfo.getScrap();
            ResScrapItem scrap = null;
            if (scrapInfo != null) {
                scrap = new ResScrapItem.Builder()
                        .withPostClientID(deviceInfo.getScrap().getClientID())
                        .withScrapTime(deviceInfo.getScrap().getScrapTime())
                        .withContent(deviceInfo.getScrap().getContent())
                        .build();
            }

            int status = deviceInfo.getStatus() == 3 ? 4 : deviceInfo.getStatus();
            resDeviceItem = new ResDeviceItem.Builder()
                    .withDeviceID(deviceInfo.getDeviceID())
                    .withShowNum(deviceInfo.getShowNum())
                    .withQrCode(deviceInfo.getQrCode())
                    .withPostClientID(deviceInfo.getClientID())
                    .withDeviceName(deviceInfo.getDeviceName())
                    .withGroupID(deviceInfo.getGroupID())
                    .withDeviceModel(deviceInfo.getDeviceModel())
                    .withLocation(deviceInfo.getLocation())
                    .withOwnType(deviceInfo.getOwnType())
                    .withInfo(deviceInfo.getInfo())
                    .withImgs(deviceInfo.getImgs())
                    .withPollingList(deviceInfo.getPollingList())
                    .withPollingCycle(deviceInfo.getPollingCycle())
                    .withMaintenanceList(deviceInfo.getMaintenanceList())
                    .withMaintenanceCycle(deviceInfo.getMaintenanceCycle())
                    .withStatus(status)
                    .withIsFinish(deviceInfo.getIsFinish())
                    .withIsMaintenance(deviceInfo.getIsMaintenance())
                    .withIsPolling(deviceInfo.getIsPolling())
                    .withScrap(scrap)
                    .build();

        } catch (Exception e) {
            log.info("can't find device in {} ", deviceID);
        }

        return resDeviceItem;
    }


    //////////////////////////////// commentDB //////////////////////////////////////////

    /**
     * 保存评论
     */
    private String saveComment(DeviceCommentItem deviceCommentItem) {

        if (null == deviceCommentItem) {
            return "";
        }

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_COMMENT);
        DBObject queryObj = SKTools.convertBeanToDBObject(deviceCommentItem);


        if (dbCollection != null) {
            dbCollection.save(queryObj);
        }

        return queryObj.get("_id").toString();
    }

    /**
     * 查看评论列表
     */

    private ArrayList<ResCommentItem> listDeviceComment(String deviceID) {

        ArrayList<ResCommentItem> list = new ArrayList<>();

        if (StrUtil.isEmpty(deviceID)) {
            log.warn("deviceID is empty");
            return list;

        }

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_COMMENT);

        DBObject filterObj = new BasicDBObject();
        filterObj.put("deviceID", deviceID);

        DBObject sortObj = new BasicDBObject();
        sortObj.put("commentTime", -1);

        DBCursor cursor = dbCollection.find(filterObj).sort(sortObj);
        List<DBObject> resAry = MongoConnFactory.toList(cursor);
        if (CollectionUtil.isNotEmpty(resAry)) {
            for (DBObject dbObjTmp : resAry) {
                DeviceCommentItem deviceCommentItem = (DeviceCommentItem) SKTools.convertDBObjectToBean(dbObjTmp, DeviceCommentItem.class);
                ResCommentItem resCommentItem = new ResCommentItem.Builder()
                        .withPostClientID(deviceCommentItem.getClientID())
                        .withImgList(deviceCommentItem.getImgList())
                        .withContent(deviceCommentItem.getComment())
                        .withCreateTime(deviceCommentItem.getCommentTime())
                        .build();
                list.add(resCommentItem);
            }
        }

        return list;
    }

    /**
     * 删除设备评论 根据deviceID
     */
    private boolean removeCommentByDeviceID(String deviceID) {
        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_COMMENT);
        DBObject filterObjTmp = new BasicDBObject();
        filterObjTmp.put("deviceID", deviceID);

        boolean isSuc = false;
        try {
            dbCollection.remove(filterObjTmp);
            isSuc = true;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return isSuc;
    }

    //////////////////////////////// breakDownDB //////////////////////////////////////////

    /**
     * 保存故障，如果有ID,则更新
     */
    private String saveBreakdownInfo(BreakdownInfo breakdownInfo) {
        return updateBreakdownInfo(breakdownInfo);

    }

    /**
     * 更新故障
     */
    private String updateBreakdownInfo(BreakdownInfo breakdownInfo) {
        if (null == breakdownInfo) {
            return "";
        }

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_BREAKDOWN);
        DBObject queryObj = SKTools.convertBeanToDBObject(breakdownInfo);

        if (breakdownInfo.getBreakdownID() != null) {
            queryObj.put("_id", new ObjectId(breakdownInfo.getBreakdownID()));
        }


        if (dbCollection != null) {
            dbCollection.save(queryObj);
        }

        return queryObj.get("_id").toString();
    }

    /**
     * 故障详情
     */
    private ResBreakdownItem getBreakdown(String deviceID) throws BusinessException {


        ResBreakdownItem resBreakdownItem = new ResBreakdownItem();
        if (StrUtil.isEmpty(deviceID)) {
            log.warn("deviceID is empty");
            return resBreakdownItem;

        }
        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_BREAKDOWN);

        BasicDBObject filterObj = new BasicDBObject();
        filterObj.put("deviceID", deviceID);

        DeviceInfo deviceInfo = this.getOneDevice(deviceID);

        DBObject sortObj = new BasicDBObject();
        sortObj.put("breakdownTime", -1);

        try {
            DBCursor cursor = dbCollection.find(filterObj).sort(sortObj).limit(1);
            List<DBObject> resAry = MongoConnFactory.toList(cursor);
            BreakdownInfo breakdownInfo = (BreakdownInfo) SKTools.convertDBObjectToBean(resAry.get(0), BreakdownInfo.class);
            ResBreakdownInfo resBreakdownInfo = new ResBreakdownInfo.Builder()
                    .withBreakdownID(breakdownInfo.getBreakdownID())
                    .withPostClientID(breakdownInfo.getClientID())
                    .withDes(breakdownInfo.getDes())
                    .withImgList(breakdownInfo.getImgList())
                    .withFixClientList(breakdownInfo.getFixClientList())
                    .withIsMustArrvied(breakdownInfo.getIsMustArrvied())
                    .build();
            ArrayList<ResCommentItem> list = this.listDeviceComment(deviceID);
            resBreakdownItem = new ResBreakdownItem.Builder()
                    .withBreakdown(resBreakdownInfo)
                    .withBreakdownID(breakdownInfo.getBreakdownID())
                    .withDeviceID(deviceID)
                    .withStatus(deviceInfo.getStatus())
                    .withShowNum(deviceInfo.getShowNum())
                    .withDeviceName(deviceInfo.getDeviceName())
                    .withQrCode(deviceInfo.getQrCode())
                    .withDeviceModel(deviceInfo.getDeviceModel())
                    .withCommentList(list)
                    .build();


            // 4.make所有allclients
            ArrayList<String> clientIDSTmp = new ArrayList<>();
            clientIDSTmp.add(breakdownInfo.getClientID());
            if (!CollectionUtil.isEmpty(breakdownInfo.getFixClientList())) {
                clientIDSTmp.addAll(breakdownInfo.getFixClientList());
            }

            for (ResCommentItem resCommentItem : list) {
                clientIDSTmp.add(resCommentItem.getPostClientID());

            }
            SKTools.removeDuplicate(clientIDSTmp);
            ArrayList<TinyClientItem> clientList = this.getTinyClientList(clientIDSTmp, breakdownInfo.getGroupID());

            resBreakdownItem.setClientIDS(clientList);

        } catch (NullPointerException e) {
            e.printStackTrace();
            resBreakdownItem = null;
        }

        return resBreakdownItem;
    }

    /**
     * 获取一个故障
     */
    private BreakdownInfo getOneBreakdownInfo(String breakdownID) {
        BreakdownInfo breakdownInfo = new BreakdownInfo();

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_BREAKDOWN);

        BasicDBObject queryObj = new BasicDBObject();
        queryObj.put("_id", new ObjectId(breakdownID));
        try {
            DBCursor cursor = dbCollection.find(queryObj);
            List<DBObject> resAry = MongoConnFactory.toList(cursor);
            breakdownInfo = (BreakdownInfo) SKTools.convertDBObjectToBean(resAry.get(0), BreakdownInfo.class);
        } catch (NullPointerException e) {
            e.printStackTrace();
            breakdownInfo = null;
        }
        return breakdownInfo;
    }

    /**
     * 获取一个设备的所有故障
     */
    private ArrayList<BreakdownInfo> listBreakdownInfo(String deviceID) {
        ArrayList<BreakdownInfo> list = new ArrayList<>();

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_BREAKDOWN);

        BasicDBObject queryObj = new BasicDBObject();
        queryObj.put("deviceID", deviceID);
        try {
            DBCursor cursor = dbCollection.find(queryObj);
            List<DBObject> resAry = MongoConnFactory.toList(cursor);
            if (CollectionUtil.isNotEmpty(resAry)) {
                for (DBObject dbObj : resAry) {
                    BreakdownInfo breakdownInfo = (BreakdownInfo) SKTools.convertDBObjectToBean(dbObj, BreakdownInfo.class);
                    list.add(breakdownInfo);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取一个设备的,某个人发出的所有故障
     */
    private ArrayList<BreakdownInfo> getOneDeviceAllBreakdown(String clientID, String deviceID) throws BusinessException {
        if (StrUtil.isBlank(clientID) || StrUtil.isBlank(deviceID)) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "clientID OR groupID can not be null");

        }
        ArrayList<BreakdownInfo> list = new ArrayList<>();
        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_BREAKDOWN);

        BasicDBObject queryObj = new BasicDBObject();
        queryObj.put("clientID", clientID);
        queryObj.put("deviceID", deviceID);
        DBObject sortObj = new BasicDBObject();
        sortObj.put("breakdownTime", -1);
        try {
            DBCursor cursor = null;
            if (dbCollection != null) {
                cursor = dbCollection.find(queryObj).sort(sortObj);
            }
            List<DBObject> resAry = MongoConnFactory.toList(cursor);
            if (CollectionUtil.isNotEmpty(resAry)) {
                for (DBObject dbObj : resAry) {
                    BreakdownInfo breakdownInfo = (BreakdownInfo) SKTools.convertDBObjectToBean(dbObj, BreakdownInfo.class);
                    list.add(breakdownInfo);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 根据clientID和groupID,查询所有的故障
     */
    private ArrayList<BreakdownInfo> listOwnFindBreakdown(String clientID, String groupID) throws BusinessException {
        if (StrUtil.isBlank(clientID) || StrUtil.isBlank(groupID)) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "clientID OR groupID can not be null");

        }
        ArrayList<BreakdownInfo> list = new ArrayList<>();
        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_BREAKDOWN);

        BasicDBObject queryObj = new BasicDBObject();
        queryObj.put("clientID", clientID);
        queryObj.put("groupID", groupID);
        try {
            DBCursor cursor = null;
            if (dbCollection != null) {
                cursor = dbCollection.find(queryObj);
            }
            List<DBObject> resAry = MongoConnFactory.toList(cursor);
            if (CollectionUtil.isNotEmpty(resAry)) {
                for (DBObject dbObj : resAry) {
                    BreakdownInfo breakdownInfo = (BreakdownInfo) SKTools.convertDBObjectToBean(dbObj, BreakdownInfo.class);
                    list.add(breakdownInfo);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 删除设备故障 根据deviceID
     */
    private boolean removeBreakdownByDeviceID(String deviceID) {
        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_BREAKDOWN);
        DBObject filterObjTmp = new BasicDBObject();
        filterObjTmp.put("deviceID", deviceID);

        boolean isSuc = false;
        try {
            dbCollection.remove(filterObjTmp);
            isSuc = true;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return isSuc;
    }


    //////////////////////////////// 维修FinishDB //////////////////////////////////////////

    /**
     * 保存维修
     */
    private String saveDeviceFinish(FinishInfo finishInfo) {

        return updateDeviceFinish(finishInfo);
    }

    /**
     * 更新维修
     */
    private String updateDeviceFinish(FinishInfo finishInfo) {

        if (null == finishInfo) {
            return "";
        }

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_FINISH);
        DBObject queryObj = SKTools.convertBeanToDBObject(finishInfo);
        // 如果更新直接保存
        if (finishInfo.getFinishID() != null) {
            queryObj.put("_id", new ObjectId(finishInfo.getFinishID()));
        }

        if (dbCollection != null) {
            dbCollection.save(queryObj);
        }

        return queryObj.get("_id").toString();
    }

    /**
     * 获取维修记录数
     */
    private int getFinishIDNum(String deviceID) {

        int finishIDNumTmp = 0;

        if (StrUtil.isEmpty(deviceID)) {
            log.warn("deviceID is empty");
            return finishIDNumTmp;

        }

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_FINISH);

        DBObject filterDbobject = new BasicDBObject();
        filterDbobject.put("deviceID", deviceID);

        DBObject sortObj = new BasicDBObject();
        sortObj.put("finishIDNum", -1);

        DBCursor cursor = dbCollection.find(filterDbobject).sort(sortObj).limit(1);
        List<DBObject> resAry = MongoConnFactory.toList(cursor);

        try {
            FinishInfo finishInfo = (FinishInfo) SKTools.convertDBObjectToBean(resAry.get(0), FinishInfo.class);
            finishIDNumTmp = finishInfo.getFinishIDNum();
        } catch (Exception e) {
            log.info("can't find device in {} ", deviceID);
            finishIDNumTmp = 0;
        }

        return finishIDNumTmp;
    }

    /**
     * 删除设备维修 根据deviceID
     */
    private boolean removeFinishByDeviceID(String deviceID) {
        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_FINISH);
        DBObject filterObjTmp = new BasicDBObject();
        filterObjTmp.put("deviceID", deviceID);

        boolean isSuc = false;
        try {
            dbCollection.remove(filterObjTmp);
            isSuc = true;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return isSuc;
    }

    /**
     * 根据deviceID 查询FinshItemList
     */
    private ArrayList<FinishInfo> getResFinishItem(String deviceID) {
        ArrayList<FinishInfo> list = new ArrayList<>();

        if (StrUtil.isEmpty(deviceID)) {
            log.warn("deviceID is empty");
            return list;

        }

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_FINISH);

        DBObject filterObj = new BasicDBObject();
        filterObj.put("deviceID", deviceID);

        DBObject sortObj = new BasicDBObject();
        sortObj.put("finishDate", -1);

        DBCursor cursor = dbCollection.find(filterObj).sort(sortObj);
        List<DBObject> resAry = MongoConnFactory.toList(cursor);
        if (CollectionUtil.isNotEmpty(resAry)) {
            list = new ArrayList<>();
            for (DBObject dbObjTmp : resAry) {
                FinishInfo finishInfo = (FinishInfo) SKTools.convertDBObjectToBean(dbObjTmp, FinishInfo.class);

                list.add(finishInfo);
            }
        }

        return list;

    }

    /**
     * 根据故障ID获取维修信息
     */
    private FinishInfo getFinishInfoByBreakdownID(String breakdownID) throws BusinessException {
        if (StrUtil.isBlank(breakdownID)) {
            return null;
        }
        FinishInfo finishInfo = null;
        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_FINISH);

        DBObject filterObj = new BasicDBObject();
        filterObj.put("breakdownID", breakdownID);

        DBObject sortObj = new BasicDBObject();
        sortObj.put("finishDate", -1);

        if (dbCollection != null) {
            DBObject result = dbCollection.findOne(filterObj);
            if (result != null) {
                finishInfo = (FinishInfo) SKTools.convertDBObjectToBean(result, FinishInfo.class);
            }
        }
        return finishInfo;
    }

    /**
     * 获取最后维修时间
     */
    private long getLastFinishTime(String deviceID) {
        long time = 0;
        if (StrUtil.isEmpty(deviceID)) {
            log.warn("deviceID is empty");
            return time;

        }
        ArrayList<FinishInfo> list = this.getResFinishItem(deviceID);
        if (CollectionUtil.isNotEmpty(list)) {
            time = list.get(0).getFinishDate();
        }
        return time;

    }

    /**
     * 获取维修列表
     */
    private ArrayList<ResFixItem> listResFixItem(String deviceID) {
        ArrayList<ResFixItem> list = new ArrayList<>();
        if (StrUtil.isEmpty(deviceID)) {
            log.warn("deviceID is empty");
            return list;

        }
        ArrayList<FinishInfo> reslist = this.getResFinishItem(deviceID);
        for (FinishInfo finishInfoTmp : reslist) {

            String breakdownID = finishInfoTmp.getBreakdownID();
            BreakdownInfo breakdownInfo = this.getOneBreakdownInfo(breakdownID);
            ArrayList<String> fixClientList = breakdownInfo.getFixClientList();
            String fixClientID = finishInfoTmp.getClientID();
            int isFinishMember = fixClientList.contains(fixClientID) ? 1 : 0;
            ResFixBreakdownItem resFixBreakdownItem = new ResFixBreakdownItem.Builder()
                    .withPostClientID(breakdownInfo.getClientID())
                    .withDeviceID(breakdownInfo.getDeviceID())
                    .withDes(breakdownInfo.getDes())
                    .withImgList(breakdownInfo.getImgList())
                    .withFixClientList(breakdownInfo.getFixClientList())
                    .build();
            ResFinishItem resFinishItem = new ResFinishItem.Builder()
                    .withFinishClientID(finishInfoTmp.getClientID())
                    .withFinishID(finishInfoTmp.getFinishID())
                    .withInfo(finishInfoTmp.getInfo())
                    .withImgList(finishInfoTmp.getImgList())
                    .withFinishDate(finishInfoTmp.getFinishDate())
                    .build();
            ResFixItem resFixItem = new ResFixItem.Builder()
                    .withBreakdown(resFixBreakdownItem)
                    .withFinish(resFinishItem)
                    .withCurrentStatus(finishInfoTmp.getCurrentStatus())
                    .withIsFinishMember(isFinishMember)
                    .withFinishIDNum(finishInfoTmp.getFinishIDNum())
                    .build();
            list.add(resFixItem);
        }
        return list;

    }

    //////////////////////////////// 停用disableDB //////////////////////////////////////////

    /**
     * 保存停用信息
     */
    private String saveDisableInfo(DisableInfo disableInfo) {

        return updateDisableInfo(disableInfo);
    }

    /**
     * 更新停用信息
     */
    private String updateDisableInfo(DisableInfo disableInfo) {

        if (null == disableInfo) {
            return "";
        }

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_DISABLE);
        DBObject queryObj = SKTools.convertBeanToDBObject(disableInfo);
        // 如果更新直接保存
        if (disableInfo.getDisableID() != null) {
            queryObj.put("_id", new ObjectId(disableInfo.getDisableID()));
        }

        if (dbCollection != null) {
            dbCollection.save(queryObj);
        }

        return queryObj.get("_id").toString();
    }

    /**
     * 删除设备停用 根据deviceID
     */
    private boolean removeDisableByDeviceID(String deviceID) {
        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_DISABLE);
        DBObject filterObjTmp = new BasicDBObject();
        filterObjTmp.put("deviceID", deviceID);

        boolean isSuc = false;
        try {
            dbCollection.remove(filterObjTmp);
            isSuc = true;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return isSuc;
    }

    /**
     * 获取停用记录数
     */
    private int getDisableIDNum(String deviceID) {

        int disableIDNum = 0;

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_DISABLE);

        DBObject filterDbobject = new BasicDBObject();
        filterDbobject.put("deviceID", deviceID);

        DBObject sortObj = new BasicDBObject();
        sortObj.put("disableIDNum", -1);

        DBCursor cursor = dbCollection.find(filterDbobject).sort(sortObj).limit(1);
        List<DBObject> resAry = MongoConnFactory.toList(cursor);

        try {
            DisableInfo disableInfo = (DisableInfo) SKTools.convertDBObjectToBean(resAry.get(0), DisableInfo.class);
            disableIDNum = disableInfo.getDisableIDNum();
        } catch (Exception e) {
            log.info("can't find device in {} ", deviceID);
            disableIDNum = 0;
        }

        return disableIDNum;
    }

    /**
     * 获取停用记录表
     */
    private ArrayList<ResDisableInfo> listResDisableInfo(String deviceID) {
        ArrayList<ResDisableInfo> list = new ArrayList<>();

        if (StrUtil.isEmpty(deviceID)) {
            log.warn("deviceID is empty");
            return list;

        }

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_DISABLE);

        DBObject filterObj = new BasicDBObject();
        filterObj.put("deviceID", deviceID);
        filterObj.put("status", XZ_DEVICE_STATUS.DISABLE_DEVICE.getValue());

        DBObject sortObj = new BasicDBObject();
        sortObj.put("disableIDNum", -1);

        DBCursor cursor = dbCollection.find(filterObj).sort(sortObj);
        List<DBObject> resAry = MongoConnFactory.toList(cursor);
        if (CollectionUtil.isNotEmpty(resAry)) {
            for (DBObject dbObjTmp : resAry) {

                DisableInfo disableInfo = (DisableInfo) SKTools.convertDBObjectToBean(dbObjTmp, DisableInfo.class);
                ResDisableInfo resDisableInfo = new ResDisableInfo.Builder()
                        .withDisableClientID(disableInfo.getClientID())
                        .withDisableIDNum(disableInfo.getDisableIDNum())
                        .withDisableDate(disableInfo.getDisableTime())
                        .withContent(disableInfo.getContent())
                        .build();
                list.add(resDisableInfo);
            }
        }

        return list;
    }

    /**
     * 获取最后停用时间
     */
    private long getLastDisableTime(String deviceID) {
        long time = 0;
        if (StrUtil.isEmpty(deviceID)) {
            log.warn("deviceID is empty");
            return time;

        }
        ArrayList<ResDisableInfo> list = this.listResDisableInfo(deviceID);
        if (CollectionUtil.isNotEmpty(list)) {
            time = list.get(0).getDisableDate();
        }

        return time;

    }

    //////////////////////////////// 保养记录表  //////////////////////////////////////////

    /**
     * 保存保养信息
     */
    private String saveMaintenanceInfo(MaintenanceInfo maintenanceInfo) {

        return updateMaintenanceInfo(maintenanceInfo);
    }

    /**
     * 更新保养信息
     */
    private String updateMaintenanceInfo(MaintenanceInfo maintenanceInfo) {

        if (null == maintenanceInfo) {
            return "";
        }

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_MAINTENANCE);
        DBObject queryObj = SKTools.convertBeanToDBObject(maintenanceInfo);
        // 如果更新直接保存
        if (maintenanceInfo.getMaintenanceID() != null) {
            queryObj.put("_id", new ObjectId(maintenanceInfo.getMaintenanceID()));
        }

        if (dbCollection != null) {
            dbCollection.save(queryObj);
        }

        return queryObj.get("_id").toString();
    }

    /**
     * 获取保养记录数
     */
    private int getMaintenanceIDNum(String deviceID) {

        int maintenance = 0;

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_MAINTENANCE);

        DBObject filterDbobject = new BasicDBObject();
        filterDbobject.put("deviceID", deviceID);

        DBObject sortObj = new BasicDBObject();
        sortObj.put("maintenanceIDNum", -1);

        DBCursor cursor = dbCollection.find(filterDbobject).sort(sortObj).limit(1);
        List<DBObject> resAry = MongoConnFactory.toList(cursor);

        try {
            MaintenanceInfo maintenanceInfo = (MaintenanceInfo) SKTools.convertDBObjectToBean(resAry.get(0), MaintenanceInfo.class);
            maintenance = maintenanceInfo.getMaintenanceIDNum();
        } catch (Exception e) {
            log.info("can't find device in {} ", deviceID);
            maintenance = 0;
        }

        return maintenance;
    }

    /**
     * 删除设备保养 根据deviceID
     */
    private boolean removeMaintenanceByDeviceID(String deviceID) {
        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_MAINTENANCE);
        DBObject filterObjTmp = new BasicDBObject();
        filterObjTmp.put("deviceID", deviceID);

        boolean isSuc = false;
        try {
            dbCollection.remove(filterObjTmp);
            isSuc = true;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return isSuc;
    }

    /**
     * 获取保养记录表
     */
    private ArrayList<ResMaintenanceItem> listResMaintenanceInfo(String deviceID) {
        ArrayList<ResMaintenanceItem> list = new ArrayList<>();

        if (StrUtil.isEmpty(deviceID)) {
            log.warn("deviceID is empty");
            return list;

        }

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_MAINTENANCE);

        DBObject filterObj = new BasicDBObject();
        filterObj.put("deviceID", deviceID);


        DBObject sortObj = new BasicDBObject();
        sortObj.put("maintenanceIDNum", -1);

        DBCursor cursor = dbCollection.find(filterObj).sort(sortObj);
        List<DBObject> resAry = MongoConnFactory.toList(cursor);
        if (CollectionUtil.isNotEmpty(resAry)) {
            for (DBObject dbObjTmp : resAry) {
                MaintenanceInfo maintenanceInfo = (MaintenanceInfo) SKTools.convertDBObjectToBean(dbObjTmp, MaintenanceInfo.class);
                String clientID = maintenanceInfo.getClientID();
                DeviceInfo deviceInfo = this.getOneDevice(deviceID);
                ArrayList<String> maintenanceList = deviceInfo.getMaintenanceList();
                int isMaintenanceMember = maintenanceList.contains(clientID) ? 1 : 0;
                ResMaintenanceItem resMaintenanceItem = new ResMaintenanceItem.Builder()
                        .withMaintenanceClientID(clientID)
                        .withTimestamp(maintenanceInfo.getTimestamp())
                        .withInfo(maintenanceInfo.getInfo())
                        .withImgList(maintenanceInfo.getImgList())
                        .withMaintenanceIDNum(maintenanceInfo.getMaintenanceIDNum())
                        .withCurrentStatus(maintenanceInfo.getCurrentStatus())
                        .withIsMaintenanceMember(isMaintenanceMember)
                        .build();
                list.add(resMaintenanceItem);
            }
        }

        return list;
    }

    /**
     * 获取最后保养时间
     */
    private long getLastMaintenanceTime(String deviceID) {
        long time = 0;
        if (StrUtil.isEmpty(deviceID)) {
            log.warn("deviceID is empty");
            return time;

        }
        ArrayList<ResMaintenanceItem> list = this.listResMaintenanceInfo(deviceID);
        if (CollectionUtil.isNotEmpty(list)) {
            time = list.get(0).getTimestamp();
        }
        return time;

    }


    //////////////////////////////// 巡检记录表  //////////////////////////////////////////

    /**
     * 保存保养信息
     */
    private String savePollingInfo(PollingInfo pollingInfo) {

        return updatePollingInfo(pollingInfo);
    }

    /**
     * 更新保养信息
     */
    private String updatePollingInfo(PollingInfo pollingInfo) {

        if (null == pollingInfo) {
            return "";
        }

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_POLLING);
        DBObject queryObj = SKTools.convertBeanToDBObject(pollingInfo);
        // 如果更新直接保存
        if (pollingInfo.getPollingID() != null) {
            queryObj.put("_id", new ObjectId(pollingInfo.getPollingID()));
        }

        if (dbCollection != null) {
            dbCollection.save(queryObj);
        }

        return queryObj.get("_id").toString();
    }

    /**
     * 删除设备巡检 根据deviceID
     */
    private boolean removePollingByDeviceID(String deviceID) {
        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_POLLING);
        DBObject filterObjTmp = new BasicDBObject();
        filterObjTmp.put("deviceID", deviceID);

        boolean isSuc = false;
        try {
            dbCollection.remove(filterObjTmp);
            isSuc = true;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return isSuc;
    }

    /**
     * 获取巡检记录数
     */
    private int getPollingIDNum(String deviceID) {

        int pollingIDNum = 0;

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_POLLING);

        DBObject filterDbobject = new BasicDBObject();
        filterDbobject.put("deviceID", deviceID);

        DBObject sortObj = new BasicDBObject();
        sortObj.put("pollingIDNum", -1);

        DBCursor cursor = dbCollection.find(filterDbobject).sort(sortObj).limit(1);
        List<DBObject> resAry = MongoConnFactory.toList(cursor);

        try {
            PollingInfo pollingInfo = (PollingInfo) SKTools.convertDBObjectToBean(resAry.get(0), PollingInfo.class);
            pollingIDNum = pollingInfo.getPollingIDNum();
        } catch (Exception e) {
            log.info("can't find device in {} ", deviceID);
            pollingIDNum = 0;
        }

        return pollingIDNum;
    }

    /**
     * 获取巡检记录表
     */
    private ArrayList<ResPollingItem> listResPollingInfo(String deviceID) {
        ArrayList<ResPollingItem> list = new ArrayList<>();

        if (StrUtil.isEmpty(deviceID)) {
            log.warn("deviceID is empty");
            return list;

        }

        DBCollection dbCollection = MongoConnFactory.getDBCollection(ConstantDefine.MONGO_COLLECTION_DEVICE_POLLING);

        DBObject filterObj = new BasicDBObject();
        filterObj.put("deviceID", deviceID);


        DBObject sortObj = new BasicDBObject();
        sortObj.put("pollingIDNum", -1);

        DBCursor cursor = dbCollection.find(filterObj).sort(sortObj);
        List<DBObject> resAry = MongoConnFactory.toList(cursor);
        if (CollectionUtil.isNotEmpty(resAry)) {
            for (DBObject dbObjTmp : resAry) {

                PollingInfo pollingInfo = (PollingInfo) SKTools.convertDBObjectToBean(dbObjTmp, PollingInfo.class);
                String clientID = pollingInfo.getClientID();
                DeviceInfo deviceInfo = this.getOneDevice(deviceID);
                ArrayList<String> pollingList = deviceInfo.getPollingList();
                int isPollingMember = pollingList.contains(clientID) ? 1 : 0;
                ResPollingItem resPollingItem = new ResPollingItem.Builder()
                        .withPollingClientID(clientID)
                        .withTimestamp(pollingInfo.getTimestamp())
                        .withInfo(pollingInfo.getInfo())
                        .withImgList(pollingInfo.getImgList())
                        .withPollingIDNum(pollingInfo.getPollingIDNum())
                        .withCurrentStatus(pollingInfo.getCurrentStatus())
                        .withIsPollingMember(isPollingMember)
                        .build();
                list.add(resPollingItem);
            }
        }

        return list;
    }

    /**
     * 获取最后保养时间
     */
    private long getLastPollingTime(String deviceID) {
        long time = 0;
        if (StrUtil.isEmpty(deviceID)) {
            log.warn("deviceID is empty");
            return time;

        }
        ArrayList<ResPollingItem> list = this.listResPollingInfo(deviceID);
        if (CollectionUtil.isNotEmpty(list)) {
            time = list.get(0).getTimestamp();
        }
        return time;

    }

    //////////////////////////////// 获取所有用户信息 //////////////////////////////////////////

    /**
     * 获取所有的用户信息
     */
    private ArrayList<TinyClientItem> getTinyClientList(ArrayList<String> clients, String groupID) throws BusinessException {

        ArrayList<TinyClientItem> clinetIDs = new ArrayList<>();
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

                TinyClientItem qulityClient = new TinyClientItem.Builder()
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
     * 删除文件
     */
    private void rmFile(String path) {

        File removeFile = new File(path);

        if (removeFile.exists()) {
            removeFile.delete();
        }

    }

    /**
     * 获取当天8点的时间戳，精确到毫秒
     */
    private long getDayEightTimestamp(long timestamp) {
        long zero = timestamp / (1000 * 3600 * 24) * (1000 * 3600 * 24);
        return zero;
    }

    /**
     * author:Alemand 更据类型来获取
     */
    @Override
    public DeviceStatusList findListType(ReqDeviceSearchItem reqDeviceSearchItem) throws BusinessException {
        if (reqDeviceSearchItem == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        String groupID = reqDeviceSearchItem.getGroupID();
        ArrayList<ResDeviceInfo> list = this.listDevice(null, groupID);
        ArrayList<ResDeviceInfo> useList = new ArrayList<>();
        ArrayList<ResDeviceInfo> breakList = new ArrayList<>();
        ArrayList<ResDeviceInfo> exitList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(list)) {
            for (ResDeviceInfo resDeviceInfo : list) {
                int status = resDeviceInfo.getStatus();
                int type = reqDeviceSearchItem.getResType();

                if (type == XZ_DEVICE_RES.USEING.getValue() || type == XZ_DEVICE_RES.ALL.getValue()) {
                    //正常、待维修、待巡检、待保养
                    if (status == XZ_DEVICE_STATUS.ENABLE_DEVICE.getValue() || status == XZ_DEVICE_STATUS.WAIT_FINISH_DEVICE.getValue()
                            || status == XZ_DEVICE_STATUS.WAIT_POLLING_DEVICE.getValue() || status == XZ_DEVICE_STATUS.WAIT_MAINTENANCE_DEVICE.getValue()) {
                        useList.add(resDeviceInfo);
                    }
                }
                if (type == XZ_DEVICE_RES.BREAKDOWN.getValue() || type == XZ_DEVICE_RES.ALL.getValue()) {
                    //已停用
                    if (status == XZ_DEVICE_STATUS.DISABLE_DEVICE.getValue()) {
                        breakList.add(resDeviceInfo);
                    }
                }
                if (type == XZ_DEVICE_RES.EXIT.getValue() || type == XZ_DEVICE_RES.ALL.getValue()) {
                    //退场报废
                    if (status == XZ_DEVICE_STATUS.EXIT_DEVICE.getValue() || status == XZ_DEVICE_STATUS.SCRAP_DEVICE.getValue()) {
                        exitList.add(resDeviceInfo);
                    }
                }
            }
        }
        DeviceStatusList deviceStatusList = new DeviceStatusList.Builder()
                .withUseList(useList)
                .withBreakList(breakList)
                .withExitList(exitList)
                .build();
        return deviceStatusList;
    }

    /**
     * author:Alemand 批量启用或停用
     */
    @Override
    public void changeManyStatus(DisableInfo disableInfo) throws BusinessException {
        if (disableInfo == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        ArrayList<String> deviceIDs = disableInfo.getDeviceIDs();
        if (CollectionUtil.isNotEmpty(deviceIDs)) {
            for (String deviceID : deviceIDs) {
                disableInfo.setDeviceID(deviceID);
                disableInfo.setDeviceIDs(null);
                this.getResDisableItem(disableInfo, deviceID);
            }

        }
    }

    /**
     * author:Alemand 退场以及批量退场
     */
    @Override
    public void exitDevice(ExitInfo exitInfo) throws BusinessException {
        long nowTime = SKTools.getNowTimeStamp();
        if (exitInfo == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        ArrayList<String> deviceIDs = exitInfo.getDeviceIDs();
        if (CollectionUtil.isNotEmpty(deviceIDs)) {
            for (String deviceID : deviceIDs) {
                DeviceInfo deviceInfo = this.getOneDevice(deviceID);
                exitInfo.setExitTime(nowTime);
                exitInfo.setDeviceID(deviceID);
                exitInfo.setDeviceIDs(null);
                deviceInfo.setExit(exitInfo);
                deviceInfo.setStatus(XZ_DEVICE_STATUS.EXIT_DEVICE.getValue());
                this.saveDevice(deviceInfo);
                //2.删除保养,巡检关系表,维修表
                this.removeRelationByDeviceID(deviceID);
                //3.修改关系表clientID=""的状态为退场
                //新增clientID为空的关系表
                DeviceRelationInfo deviceRelationInfo = new DeviceRelationInfo.Builder()
                        .withGroupID(deviceInfo.getGroupID())
                        .withDeviceID(deviceInfo.getDeviceID())
                        .withSearchIndex(deviceInfo.makeSearchIndexStr())
                        .withClientID("")
                        .withDeviceCreateTime(deviceInfo.getCreateTime())
                        .withStatus(XZ_DEVICE_STATUS.EXIT_DEVICE.getValue())
                        .build();
                String deviceRelationID = this.saveDeviceRelation(deviceRelationInfo, 0);
                deviceRelationInfo.setDeviceRelationID(deviceRelationID);
                this.saveDeviceRelation(deviceRelationInfo, 0);
            }
        }

    }

    //TODO 设置回调的路径就可以了
    private static String callBackUrl = mainConfig.getMustArriveConfig().getServerURL() + "/gouliaoweb-1.0/app/device/file/update";
    private static String uploadURL = mainConfig.getCallNodeConfig().getExportURL() + "node的地址";

    @Override
    public void makeQrcode(ReqDeviceItem reqDeviceItem) throws BusinessException {
        if (reqDeviceItem == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "对象不能为空");
        }
        ArrayList<NodeFileDevice> list = new ArrayList<>();

        ArrayList<String> deviceIDs = reqDeviceItem.getDeviceIDs();
        if(CollectionUtil.isNotEmpty(deviceIDs)){
            for (String deviceID:deviceIDs) {
                DeviceInfo deviceInfo = this.getOneDevice(deviceID);
                NodeFileDevice nodeFileDevice = this.makeNodePostBody(deviceInfo);
                list.add(nodeFileDevice);
            }
        }
        XZPostBuilder postBuilder = new XZPostBuilder()
                .addRequestURL(uploadURL)
                .addTag(DeviceServiceImpl.class)
                .addJsonData(list);
        log.info("调用node生成地址为{},回调地址为{}", uploadURL, callBackUrl);
        try {
            String result = postBuilder.syncOutRequest(String.class);
        } catch (XZHTTPException e) {
            log.error(e.toString());
            throw new BusinessException(ReturnCode.CODE_FAIL, "请求失败");
        }
    }

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
        String deviceID = reqFileItem.getDeviceID();
        DeviceInfo deviceInfo = this.getOneDevice(deviceID);
        if (deviceInfo == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "不存在整改");
        }
        String fileName = StrUtil.format("{}{}", deviceInfo.getDeviceName(), IDGenerator.getNextID());
        long deadTime = nowTime + 7 * 24 * 60 * 60 * 1000;
        FileItem fileItem = reqFileItem.makeFileItem();
        String downloadUrl = fileItem.getDownloadURL();
        log.info("node回调downloadURL为{}", downloadUrl);
        fileName = StrUtil.format("{}{}", fileName, SKTools.getFileTypeByStr(downloadUrl));
        fileItem.setCreateDate(nowTime);
        fileItem.setDeadTime(deadTime);
        fileItem.setFileName(fileName);
        fileItem.setTitle(fileName);
        String fileID = fileInfoDao.saveFileInfo(fileItem);
        fileItem.setFileID(fileID);
        String shareURL = StrUtil.format("{}/files/{}", mainConfig.getMustArriveConfig().getQrcodeCallback(), fileID);
        String key = "180862506";
        String previewURL = StrUtil.format("http://dcsapi.com?k={}&url={}", key, downloadUrl);
        fileItem.setShareURL(shareURL);
        fileItem.setPreviewURL(previewURL);
        fileInfoDao.saveFileInfo(fileItem);
        String postClientID = fileItem.getPostClientID();
        NewClient sendToclient = newClientService.findById(Integer.parseInt(postClientID));
        String groupID = deviceInfo.getGroupID();
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

    @Override
    public void scrapManyDevice(ScrapInfo scrapInfo) throws BusinessException {
        if (scrapInfo == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "数据为空");
        }
        ArrayList<String> deviceIDs = scrapInfo.getDeviceIDs();
        if(CollectionUtil.isNotEmpty(deviceIDs)){
            for (String deviceID:deviceIDs) {
                //将设备id放回去同时将集合置空
                scrapInfo.setDeviceID(deviceID);
                scrapInfo.setDeviceIDs(null);
                scrap(deviceID,scrapInfo);
            }
        }
    }

    private NodeFileDevice makeNodePostBody(DeviceInfo deviceInfo) {
        if (StrUtil.isEmpty(deviceInfo.getDeviceID())) {
            log.debug("deviceID is null,{}", deviceInfo);
            return null;
        }
        String pngName = String.format("{}{}", deviceInfo.getDeviceID(), SKTools.getNowTimeStamp());
        String md5Str = SKTools.getMD5(pngName);
        String finalStr = StrUtil.format("{}{}", "device_", md5Str);
        return new NodeFileDevice.Builder()
                .withDeviceID(deviceInfo.getDeviceID())
                .withGroupID(deviceInfo.getGroupID())
                .withFileName(finalStr)
                .withPostClientID(deviceInfo.getClientID())
                .withCallbackURL(callBackUrl)
                .withQrCode(deviceInfo.getQrCode())
                .build();
    }

}
