package com.hangtuo.service;

import com.hangtuo.common.response.BusinessException;
import com.hangtuo.entity.TimerNewJob;
import com.hangtuo.entity.device.BreakdownInfo;
import com.hangtuo.entity.device.DeviceCommentItem;
import com.hangtuo.entity.device.DeviceInfo;
import com.hangtuo.entity.device.DeviceStatusItem;
import com.hangtuo.entity.device.DeviceStatusList;
import com.hangtuo.entity.device.DisableInfo;
import com.hangtuo.entity.device.ExitInfo;
import com.hangtuo.entity.device.FinishInfo;
import com.hangtuo.entity.device.MaintenanceInfo;
import com.hangtuo.entity.device.PollingInfo;
import com.hangtuo.entity.device.ReqDeviceItem;
import com.hangtuo.entity.device.ReqDeviceSearchItem;
import com.hangtuo.entity.device.ReqOwnSaveBreakdown;
import com.hangtuo.entity.device.ResBreakdownItem;
import com.hangtuo.entity.device.ResCommentItem;
import com.hangtuo.entity.device.ResDeviceInfo;
import com.hangtuo.entity.device.ResDeviceItem;
import com.hangtuo.entity.device.ResDisableItem;
import com.hangtuo.entity.device.ResDisableList;
import com.hangtuo.entity.device.ResMaintenanceList;
import com.hangtuo.entity.device.ResOwnFixInfo;
import com.hangtuo.entity.device.ResPollingList;
import com.hangtuo.entity.device.ResRepairInfo;
import com.hangtuo.entity.device.ScrapInfo;
import com.hangtuo.entity.file.ReqFileItem;

import java.util.ArrayList;

/**
 *
 * @author Shawn
 * @since  2017/5/23
 */
public interface DeviceService {
    /**
     * 1.创建一个设备
     */
    String createDevice(DeviceInfo deviceInfo) throws BusinessException;


    /**
     * 2.获取一个设备
     */
    ResDeviceItem getOneDeviceInfo(ReqDeviceItem reqDeviceItem) throws BusinessException;


    ArrayList<DeviceInfo> listAllDeviceInfo() throws BusinessException;

    /**
     * 更新设备qrcode
     */
    void updateQrcode(DeviceInfo deviceInfo) throws BusinessException;

    /**
     * 3.更新设备
     */
    String updateDevice(DeviceInfo deviceInfo) throws BusinessException;


    /**
     * 3.废弃设备
     */
    String scrapDevice(ScrapInfo scrapInfo) throws BusinessException;

    /**
     * 4.删除设备
     */
    String removeDevice(ReqDeviceItem reqDeviceItem) throws BusinessException;


    //////////////////////////////// 搜索，筛选设备 //////////////////////////////////////////

    /**
     * 5.筛选设备
     */
    ArrayList<ResDeviceInfo> listOfChooseInfo(ReqDeviceSearchItem reqDeviceSearchItem) throws BusinessException;

    /**
     * 查询等待我处理的设备
     */
    ArrayList<ResDeviceInfo> listWaitMeToDoDevice(String clientID, String groupID) throws BusinessException;

    DeviceInfo getOneDevice(String deviceID);

    /**
     * 6.搜索设备
     */
    ArrayList<ResDeviceInfo> listSearchDevice(ReqDeviceSearchItem reqDeviceSearchItem) throws BusinessException;

    /**
     * 生成设备二维码
     */

    String getDeviceQRCode(String deviceID, String groupID, boolean refresh) throws BusinessException;

    /**
     * 5.待我巡检，待我保养，待我维修列表
     */
    ArrayList<ResDeviceInfo> listOfOwnDevice(ReqDeviceSearchItem reqDeviceSearchItem) throws BusinessException;


    /**
     * 6.搜索待我巡检，待我保养，待我维修设备列表
     */
    ArrayList<ResDeviceInfo> listOwnSearchDevice(ReqDeviceSearchItem reqDeviceSearchItem) throws BusinessException;


    //////////////////////////////// 停用 //////////////////////////////////////////

    /**
     * 7.停用/启用
     */
    ResDisableItem changeStatus(DisableInfo disableInfo) throws BusinessException;


    /**
     * 8.单个设备的停用记录列表
     */
    ResDisableList listDisableInfo(ReqDeviceItem reqDeviceItem) throws BusinessException;


    /**
     * 9.获取设备管理页面待我保养 待我巡检 待我维修count
     */
    DeviceStatusItem getDeviceStatusNum(ReqDeviceItem reqDeviceItem) throws BusinessException;


    //////////////////////////////// 维修 //////////////////////////////////////////

    /**
     * 10.发现故障
     */
    String createBreakdown(BreakdownInfo breakdownInfo) throws BusinessException;

    /**
     * 修改故障
     */
    String updateBreakdown(BreakdownInfo breakdownInfo) throws BusinessException;

    /**
     * 11.故障详情
     */
    ResBreakdownItem getOneBreakdown(ReqDeviceItem reqDeviceItem) throws BusinessException;

    /**
     * 12.维修回复(故障恢复)
     */
    String finishBreakdown(FinishInfo finishInfo) throws BusinessException;


    /**
     * 13.单个设备的维修记录列表
     */
    ResRepairInfo listRepairInfo(ReqDeviceItem reqDeviceItem) throws BusinessException;

    //////////////////////////////// 保养 //////////////////////////////////////////

    /**
     * 14.保养回复
     */
    String finishMaintenance(MaintenanceInfo maintenanceInfo) throws BusinessException;


    /**
     * 15.单个设备的保养记录列表
     */
    ResMaintenanceList listMaintenanceInfo(ReqDeviceItem reqDeviceItem) throws BusinessException;


    //////////////////////////////// 巡检 //////////////////////////////////////////

    /**
     * 16.巡检回复
     */
    String finishPolling(PollingInfo pollingInfo) throws BusinessException;


    /**
     * 17.单个设备的巡检记录列表
     */
    ResPollingList listPollingInfo(ReqDeviceItem reqDeviceItem) throws BusinessException;


    //////////////////////////////// 评论 //////////////////////////////////////////

    /**
     * 18.添加评论
     */
    String addComment(DeviceCommentItem deviceCommentItem) throws BusinessException;

    /**
     * 19.评论列表
     */
    ArrayList<ResCommentItem> listDeviceComment(ReqDeviceItem reqDeviceItem) throws BusinessException;


    /**
     * 保养，巡检，回调发送消息
     */
    void nowSendDelaySend(TimerNewJob timerNewJob) throws BusinessException;

    /**
     * 获取自己发现的所有故障的设备
     */
    ArrayList<ResDeviceInfo> listOwnFindBreakdownDevice(ReqDeviceItem reqDeviceItem)throws BusinessException;

    /**
     * 记录维修 1.3.10添加
     */
    void ownSaveBreakAndFinish(ReqOwnSaveBreakdown reqOwnSaveBreakdown) throws BusinessException;

    /**
     * 单独设备发现故障详情页面
     */
    ResOwnFixInfo getOneDeviceFixInfo(ReqDeviceItem reqDeviceItem) throws BusinessException;



    DeviceStatusList findListType(ReqDeviceSearchItem reqDeviceSearchItem) throws BusinessException;

    /**
     *批量启用以及批量停用的接口
     */
    void changeManyStatus(DisableInfo disableInfo) throws BusinessException;

    /**
     *退场及批量退场
     */
    void exitDevice(ExitInfo exitInfo) throws BusinessException;

    /**
     *导出
     */
    void makeQrcode(ReqDeviceItem reqDeviceItem) throws BusinessException;

    String saveUploadFileAndSendMsg(ReqFileItem reqFileItem) throws BusinessException;

    /**
     *批量报废
     */
    void scrapManyDevice(ScrapInfo scrapInfo) throws BusinessException;
}
