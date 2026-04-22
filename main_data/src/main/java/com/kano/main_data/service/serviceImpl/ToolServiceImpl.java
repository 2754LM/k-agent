package com.kano.main_data.service.serviceImpl;

import com.kano.main_data.service.ToolService;
import com.kano.main_data.agent.tools.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolServiceImpl implements ToolService {
    @Autowired
    List<Tool> tools;

    @Override
    public List<Tool> getAllTools() {
        return tools;
    }


}
