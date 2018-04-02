package request;

import java.util.Map;

import entity.XZ_REQUEST_TYPE;
import entity.XZ_SUBMIT_TYPE;

/**
 * <p>
 * 用来定义方法
 * </p>
 *
 * @author Alemand
 * @since 2018/4/2
 */
public interface HttpRequest<T> {

    /**
     * 设置请求路径
     *
     * @param url 请求路径
     * @return 泛型
     */
    T setRequestUrl(String url);

    /**
     * 设置请求头
     *
     * @param hand 请求头
     * @return 泛型
     */
    T setRequestHand(Map<String, String> hand);

    /**
     * 设置请求方式
     *
     * @param type 请求方式
     * @return 泛型
     */
    T setRequestType(XZ_REQUEST_TYPE type);

    /**
     *设置请求提交方式 form 还是 json
     *
     * @param type 请求方式
     * @return 泛型
     */
    T setSubmitType(XZ_SUBMIT_TYPE type);




}
