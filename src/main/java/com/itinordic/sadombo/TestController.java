package com.itinordic.sadombo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author developer
 */
@Controller
public class TestController {
    
    @ResponseBody
    @RequestMapping(value = "mine")
    public String testMethod(){
        return "OK ok ok";
    }
    
}
