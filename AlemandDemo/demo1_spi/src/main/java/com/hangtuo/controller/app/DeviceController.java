package com.hangtuo.controller.app;

import com.google.common.collect.ImmutableMap;

import com.hangtuo.common.Enum.XZ_DEVICE_STATUS;
import com.hangtuo.common.response.ApiResponse;
import com.hangtuo.common.response.BusinessException;
import com.hangtuo.common.response.ReturnCode;
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
import com.hangtuo.service.AsyncService;
import com.hangtuo.service.DeviceService;
import com.hangtuo.service.annotation.ControllerAround;
import com.xiaoleilu.hutool.log.Log;
import com.xiaoleilu.hutool.log.LogFactory;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;

import javax.annotation.Resource;

/**
 *
 * @author Shawn
 * @since  2017/5/23
 */
@Controller
@RequestMapping("/app/device")
public class DeviceController extends AbstractController {
    private static final Log log = LogFactory.get();

    @Resource(name = "deviceServiceImpl")
    private DeviceService deviceService;
    @Resource(name = "asyncServiceImpl")
    private AsyncService asyncService;

    /**
     * 新增设备
     */
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse createDevice(@Validated @RequestBody DeviceInfo deviceInfo, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            String deviceID = deviceService.createDevice(deviceInfo);
            ImmutableMap immutableMap = ImmutableMap.of("deviceID", deviceID);
            return ApiResponse.successOf(immutableMap);
        } catch (BusinessException e) {
            log.info("error");

            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 更新设备
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse updateDevice(@Validated @RequestBody DeviceInfo deviceInfo, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            deviceService.updateDevice(deviceInfo);
            return ApiResponse.SUC;
        } catch (BusinessException e) {
            log.info("error");

            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 废弃
     */
    @RequestMapping(value = "/scrap", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse scrapDevice(@Validated @RequestBody ScrapInfo scrapInfo, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            deviceService.scrapDevice(scrapInfo);
            return ApiResponse.SUC;
        } catch (BusinessException e) {
            log.info("error");

            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 查看人与设备的关系
     */
    @RequestMapping(value = "/status", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse getDeviceStatus(@Validated @RequestBody ReqDeviceItem reqDeviceItem, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            ResDeviceItem resDeviceItem = deviceService.getOneDeviceInfo(reqDeviceItem);
            int status = resDeviceItem.getIsFinish() == 0 ? 0 : 3;

            ImmutableMap immutableMap = ImmutableMap.of("status", status);
            return ApiResponse.successOf(immutableMap);
        } catch (BusinessException e) {
            log.info("error");

            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 查看人与设备的关系
     */
    @RequestMapping(value = "/isscrap", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse getDeviceIsScrap(@Validated @RequestBody ReqDeviceItem reqDeviceItem, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            ResDeviceItem resDeviceItem = deviceService.getOneDeviceInfo(reqDeviceItem);
            int status = resDeviceItem.getStatus();
            if (status == XZ_DEVICE_STATUS.SCRAP_DEVICE.getValue()) {
                throw new BusinessException(ReturnCode.CODE_DEVICE_SCRAP, "此设备已报废!");

            }
            ImmutableMap immutableMap = ImmutableMap.of("status", status);
            return ApiResponse.successOf(immutableMap);
        } catch (BusinessException e) {
            log.info("error");

            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }


    /**
     * 获取一个设备
     */
    @RequestMapping(value = "/one", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse getOneDevice(@Validated @RequestBody ReqDeviceItem reqDeviceItem, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            ResDeviceItem resDeviceItem = deviceService.getOneDeviceInfo(reqDeviceItem);
            return ApiResponse.successOf(resDeviceItem);
        } catch (BusinessException e) {
            log.info("error");

            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 筛选设备，包括全部，0 为全部设备 其它状态删选
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse listDevice(@Validated @RequestBody ReqDeviceSearchItem reqDeviceSearchItem, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            ArrayList<ResDeviceInfo> list = deviceService.listOfChooseInfo(reqDeviceSearchItem);
            ImmutableMap immutableMap = ImmutableMap.of("list", list);
            return ApiResponse.successOf(immutableMap);
        } catch (BusinessException e) {
            log.info("error");

            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

       /**
     * 搜索设备,根据设备编号，名称，型号搜索
     */
    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse queryDevice(@Validated @RequestBody ReqDeviceSearchItem reqDeviceSearchItem, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            ArrayList<ResDeviceInfo> list = deviceService.listSearchDevice(reqDeviceSearchItem);
            ImmutableMap immutableMap = ImmutableMap.of("list", list);
            return ApiResponse.successOf(immutableMap);
        } catch (BusinessException e) {
            log.info("error");

            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 获取设备的二维码
     */
    @RequestMapping(value = "/qrcode", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse DeviceQrcode(@Validated @RequestBody ReqDeviceItem reqDeviceItem, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        String groupID = reqDeviceItem.getGroupID();
        try {
            String deviceID = reqDeviceItem.getDeviceID();
            String path = deviceService.getDeviceQRCode(deviceID, groupID, true);
            return ApiResponse.successOf(path);
        } catch (BusinessException e) {
            log.info("error");

            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 获取所有自己发现故障的设备
     */
    @RequestMapping(value = "/own/breakdown/list", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse listOwnFindDevice(@Validated @RequestBody ReqDeviceItem reqDeviceItem, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            ArrayList<ResDeviceInfo> list = deviceService.listOwnFindBreakdownDevice(reqDeviceItem);
            ImmutableMap immutableMap = ImmutableMap.of("list", list);
            return ApiResponse.successOf(immutableMap);
        } catch (BusinessException e) {
            log.info("error");

            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 发现故障详情页面
     */
    @RequestMapping(value = "/breakdown/details", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse deviceFixList(@Validated @RequestBody ReqDeviceItem reqDeviceItem, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            ResOwnFixInfo resOwnFixInfo = deviceService.getOneDeviceFixInfo(reqDeviceItem);
            return ApiResponse.successOf(resOwnFixInfo);
        } catch (BusinessException e) {
            log.info("error");

            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }
    /**
     * 新增维修记录,故障,维修,一起添加
     */
    @RequestMapping(value = "/own/breakdown/create", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse saveBreakdownAndFinsh(@Validated @RequestBody ReqOwnSaveBreakdown reqOwnSaveBreakdown, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            deviceService.ownSaveBreakAndFinish(reqOwnSaveBreakdown);
            return ApiResponse.SUC;
        } catch (BusinessException e) {
            log.info("error");

            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }


    /**
     * 替换所有的二维码
     */
    @RequestMapping(value = "/qrcode/replace", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse replaceDeviceQrcode(@Validated @RequestBody ReqDeviceItem reqDeviceItem, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        String groupID = reqDeviceItem.getGroupID();
        try {
            asyncService.replaceAllDeviceQrcode();
            return ApiResponse.SUC;
        } catch (BusinessException e) {
            log.info("error");

            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 搜索设备,根据设备编号，名称，型号搜索
     */
    @RequestMapping(value = "/own/list", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse listOwnDevice(@Validated @RequestBody ReqDeviceSearchItem reqDeviceSearchItem, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            ArrayList<ResDeviceInfo> list = deviceService.listOfOwnDevice(reqDeviceSearchItem);
            ImmutableMap immutableMap = ImmutableMap.of("list", list);
            return ApiResponse.successOf(immutableMap);
        } catch (BusinessException e) {
            log.info("error");

            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 搜索设备,根据设备编号，名称，型号搜索
     */
    @RequestMapping(value = "/own/query", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse queryOwnDevice(@Validated @RequestBody ReqDeviceSearchItem reqDeviceSearchItem, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            ArrayList<ResDeviceInfo> list = deviceService.listOwnSearchDevice(reqDeviceSearchItem);
            ImmutableMap immutableMap = ImmutableMap.of("list", list);
            return ApiResponse.successOf(immutableMap);
        } catch (BusinessException e) {
            log.info("error");

            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 删除一个设备
     */
    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse removeOneDevice(@Validated @RequestBody ReqDeviceItem reqDeviceItem, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            deviceService.removeDevice(reqDeviceItem);
            return ApiResponse.SUC;
        } catch (BusinessException e) {
            log.info("error");

            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 设备评论
     */
    @RequestMapping(value = "/comment/create", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse createComment(@Validated @RequestBody DeviceCommentItem deviceCommentItem, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            String commentID = deviceService.addComment(deviceCommentItem);
            ImmutableMap immutableMap = ImmutableMap.of("commentID", commentID);
            return ApiResponse.successOf(immutableMap);
        } catch (BusinessException e) {
            log.info("error");

            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 评论列表
     */
    @RequestMapping(value = "/comment/list", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse listComment(@Validated @RequestBody ReqDeviceItem reqDeviceItem, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            ArrayList<ResCommentItem> list = deviceService.listDeviceComment(reqDeviceItem);
            ImmutableMap immutableMap = ImmutableMap.of("deviceCommentList", list);
            return ApiResponse.successOf(immutableMap);
        } catch (BusinessException e) {
            log.info("error");

            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }


    /**
     * 发现故障
     */
    @RequestMapping(value = "/breakdown/create", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse createBreakdown(@Validated @RequestBody BreakdownInfo breakdownInfo, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            String breakdownID = deviceService.createBreakdown(breakdownInfo);
            ImmutableMap immutableMap = ImmutableMap.of("breakdownID", breakdownID);
            return ApiResponse.successOf(immutableMap);
        } catch (BusinessException e) {
            log.info("error");

            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 更新故障
     */
    @RequestMapping(value = "/breakdown/update", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse updateBreakdown(@Validated @RequestBody BreakdownInfo breakdownInfo, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            deviceService.updateBreakdown(breakdownInfo);
            return ApiResponse.SUC;
        } catch (BusinessException e) {
            log.info("error");

            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 获取故障详情
     */
    @RequestMapping(value = "/breakdown/info", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse getOneBreakdown(@Validated @RequestBody ReqDeviceItem reqDeviceItem, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            ResBreakdownItem resBreakdownItem = deviceService.getOneBreakdown(reqDeviceItem);
            return ApiResponse.successOf(resBreakdownItem);
        } catch (BusinessException e) {
            log.info("error");

            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 维修回复
     */
    @RequestMapping(value = "/breakdown/finish", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse fixBreakdown(@Validated @RequestBody FinishInfo finishInfo, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            String finishdownID = deviceService.finishBreakdown(finishInfo);
            return ApiResponse.successOf(finishdownID);
        } catch (BusinessException e) {
            log.info("error");
            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 维修记录列表
     */
    @RequestMapping(value = "/breakdown/list", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse listFixBreakdownList(@Validated @RequestBody ReqDeviceItem reqDeviceItem, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            ResRepairInfo resRepairInfo = deviceService.listRepairInfo(reqDeviceItem);
            return ApiResponse.successOf(resRepairInfo);
        } catch (BusinessException e) {
            log.info("error");
            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 停用，启用
     */
    @RequestMapping(value = "/status/change", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse changeDeviceStatus(@Validated @RequestBody DisableInfo disableInfo, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            ResDisableItem resDisableItem = deviceService.changeStatus(disableInfo);
            return ApiResponse.successOf(resDisableItem);
        } catch (BusinessException e) {
            log.info("error");
            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 停用列表
     */
    @RequestMapping(value = "/disable/list", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse listDisableRecord(@Validated @RequestBody ReqDeviceItem reqDeviceItem, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            ResDisableList resDisableList = deviceService.listDisableInfo(reqDeviceItem);
            return ApiResponse.successOf(resDisableList);
        } catch (BusinessException e) {
            log.info("error");
            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 保养回复
     */
    @RequestMapping(value = "/maintenance/finish", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse finishMaintenance(@Validated @RequestBody MaintenanceInfo maintenanceInfo, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            String maintenanceID = deviceService.finishMaintenance(maintenanceInfo);
            return ApiResponse.successOf(maintenanceID);
        } catch (BusinessException e) {
            log.info("error");
            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 单个设备的保养记录列表
     */
    @RequestMapping(value = "/maintenance/list", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse maintenanceList(@Validated @RequestBody ReqDeviceItem reqDeviceItem, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            ResMaintenanceList maintenanceList = deviceService.listMaintenanceInfo(reqDeviceItem);
            return ApiResponse.successOf(maintenanceList);
        } catch (BusinessException e) {
            log.info("error");
            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 巡检回复
     */
    @RequestMapping(value = "/polling/finish", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse finishPolling(@Validated @RequestBody PollingInfo pollingInfo, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            String pollingID = deviceService.finishPolling(pollingInfo);
            return ApiResponse.successOf(pollingID);
        } catch (BusinessException e) {
            log.info("error");
            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 单个设备的巡检记录列表
     */
    @RequestMapping(value = "/polling/list", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse pollingList(@Validated @RequestBody ReqDeviceItem reqDeviceItem, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            ResPollingList resPollingList = deviceService.listPollingInfo(reqDeviceItem);
            return ApiResponse.successOf(resPollingList);
        } catch (BusinessException e) {
            log.info("error");
            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }

    /**
     * 设备管理带我保养、巡检、维修数字
     */
    @RequestMapping(value = "/info", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse getCount(@Validated @RequestBody ReqDeviceItem reqDeviceItem, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }
        try {
            DeviceStatusItem deviceStatusItem = deviceService.getDeviceStatusNum(reqDeviceItem);
            return ApiResponse.successOf(deviceStatusItem);
        } catch (BusinessException e) {
            log.info("error");
            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }


    /**
     * 保养，巡检，回调发送消息
     */
    @RequestMapping(value = "/callback", method = RequestMethod.POST)
    public
    @ResponseBody
    ApiResponse callbackSendMsg(@Validated @RequestBody TimerNewJob timerNewJob, BindingResult bindingResult) {

        ApiResponse apiResponse = validate(bindingResult);
        if (apiResponse.hasError()) {
            return apiResponse;
        }

        try {
            deviceService.nowSendDelaySend(timerNewJob);
            return ApiResponse.SUC;
        } catch (BusinessException e) {
            log.info("error");

            return ApiResponse.immediateOf(e.getErrCode(), e.getMessage());
        } catch (Exception e) {
            log.error("");
            return ApiResponse.FAIL;
        }
    }


    /**
     *author:Alemand
     * 全部设备的分类列表
     */
    @RequestMapping(value = "/alltype/list", method = RequestMethod.POST)
    @ResponseBody
    @ControllerAround
    public ApiResponse findTypeList(@Validated @RequestBody ReqDeviceSearchItem reqDeviceSearchItem,BindingResult bindingResult)  throws Exception{
            DeviceStatusList deviceStatusList = deviceService.findListType(reqDeviceSearchItem);
            ImmutableMap immutableMap = ImmutableMap.of("deviceStatusList", deviceStatusList);
            return ApiResponse.successOf(immutableMap);
    }

    /**
     * author:Alemand
     * 批量的停用，启用
     */
    @RequestMapping(value = "/manystatus/change", method = RequestMethod.POST)
    @ResponseBody
    @ControllerAround
    public ApiResponse changeDevicesStatus(@Validated @RequestBody DisableInfo disableInfo, BindingResult bindingResult) throws Exception{
            deviceService.changeManyStatus(disableInfo);
            return ApiResponse.SUC;
    }

    /**
     *author:Alemand
     * 批量报废
     */
    @RequestMapping(value = "/many/scrap",method = RequestMethod.POST)
    @ResponseBody
    @ControllerAround
    public ApiResponse deviceExit(@Validated @RequestBody ScrapInfo scrapInfo, BindingResult bindingResult) throws Exception{
            deviceService.scrapManyDevice(scrapInfo);
            return ApiResponse.SUC;
    }

    /**
     * author:Alemand
     *退场接口的编写
     */

    @RequestMapping(value = "/exit",method = RequestMethod.POST)
    @ResponseBody
    @ControllerAround
    public ApiResponse deviceExit(@Validated @RequestBody ExitInfo exitInfo, BindingResult bindingResult) throws Exception{
            deviceService.exitDevice(exitInfo);
            return ApiResponse.SUC;

    }

    @RequestMapping(value = "/qrcode/makefile",  method = RequestMethod.POST)
    @ResponseBody
    @ControllerAround
    public ApiResponse deviceQrcodeExport(@Validated @RequestBody ReqDeviceItem reqDeviceItem, BindingResult bindingResult) throws Exception{
            deviceService.makeQrcode(reqDeviceItem);
            return ApiResponse.SUC;
    }

    @RequestMapping(value = "/file/update",method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse saveFile(@Validated @RequestBody ReqFileItem reqFileItem,BindingResult bindingResult) throws Exception{
            String fileID = deviceService.saveUploadFileAndSendMsg(reqFileItem);
            ImmutableMap<String, String> returnMap = ImmutableMap.of("fileID", fileID);
            return ApiResponse.successOf(returnMap);
    }



}
