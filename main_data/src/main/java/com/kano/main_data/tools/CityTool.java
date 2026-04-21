package com.kano.main_data.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class CityTool implements com.kano.main_data.tools.Tool {
    @Tool(name = "get_city_info", description = "获取当前城市信息")
    public String getCityInfo() {
        return "当前城市是：北京";
    }
}
