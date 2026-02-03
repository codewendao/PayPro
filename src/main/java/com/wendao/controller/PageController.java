package com.wendao.controller;

import com.wendao.common.utils.StringUtils;
import com.wendao.enums.OrderStatesEnum;
import com.wendao.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;


/**
 * @author lld
 */
@Controller
public class PageController {

    private static final Logger log= LoggerFactory.getLogger(PageController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @RequestMapping("/")
    public String index(Model model){
        return "index";
    }

    @RequestMapping("/{page}")
    public String showPage(@PathVariable String page,
                           HttpServletRequest request){

        if(page.contains("order-success")){
            return "index";
        }
        String id = request.getParameter("id");
        if("openAlipay".equals(page)&&StringUtils.isNotBlank(id)){
            // 已扫码状态
            try{
                orderService.changeOrderState(id, OrderStatesEnum.SCANE_QR.getState());
                Set<String> keys = redisTemplate.keys("pay:*");
                redisTemplate.delete(keys);
            }catch (Exception e){

            }
        }
        return page;
    }

    /**
     * 图标
     */
    @GetMapping({"favicon.ico", "favicon"})
    @ResponseBody
    public void favicon() {
    }
}
