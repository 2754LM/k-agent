package com.kano.main_data.service.serviceImpl;

import com.kano.main_data.service.TokenService;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.stereotype.Service;

@Service
public class TokenServiceImpl implements TokenService {

    TokenCountEstimator tokenCountEstimator = new JTokkitTokenCountEstimator();

    @Override
    public int countTokens(String text) {
       return tokenCountEstimator.estimate(text);
    }

}
